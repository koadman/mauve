package org.gel.mauve;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.gel.mauve.color.LCBColorScheme;
import org.gel.mauve.color.NormalizedOffsetColorScheme;

/**
 * 
 * Methods to parse data files into models.
 *  
 */
public class ModelBuilder
{

    // Delimiter for headers
    private final static String DELIMS = "\t";

    public static BaseViewerModel buildModel(URL url, String auth_token, ModelProgressListener listener) throws IOException, MauveFormatException
    {
        if (listener != null)
        {
            listener.buildStart();
            listener.downloadStart();
        }
        
        
        // Find cached directory, if it exists.
        File dir = null;
        try
        {
            dir = getCachedDirectory(url);
        }
        catch (BackingStoreException e)
        {
            System.err.println("Error reading preferences.  Error follows.  Will load from server.");
            e.printStackTrace();
        }
        
        if (dir == null)
        {
	        // create the Jar URL
	        String jar_url_str = "jar:" + url;
	        if( auth_token != null )
	        	jar_url_str += "&" + auth_token;
			jar_url_str += "!/";
	        URL jar_url = new URL(jar_url_str);
	        System.err.println("Loading alignment from " + url.toString());
	
	        // Get URL connection.
	        URLConnection urlConn = jar_url.openConnection();
	        if (! (urlConn instanceof JarURLConnection))
	        {
	            throw new IOException("URL does not point to a jar file.");
	        }
	        JarURLConnection conn = (JarURLConnection) urlConn;
	
	        // Create a temporary directory.
	        dir = File.createTempFile("mauve", "dir");
	        dir.delete();
	        dir.mkdir();
	        if (!dir.exists() || !dir.isDirectory())
	        {
	            throw new IOException("Couldn't create temporary directory.");
	        }
	        System.err.println("Saving alignment to: " + dir.toString());
	        
	        // Unpack contents into directory.
	        JarFile jarFile = conn.getJarFile();
	        System.err.println("jar location: " + jarFile.getName() );
	        
	        // Create directory structure.
	        Enumeration entries = jarFile.entries();
	        while (entries.hasMoreElements())
	        {
	            JarEntry entry = (JarEntry) entries.nextElement();
	            
	            if (entry.isDirectory())
	            {
	                File newFile = new File(dir, entry.getName());
	                newFile.mkdirs();
	            }
	        }
	
	        // Output files.
	        entries = jarFile.entries();
	        while (entries.hasMoreElements())
	        {
	            JarEntry entry = (JarEntry) entries.nextElement();
	
	            if (!entry.isDirectory())
	            {
	                File newFile = new File(dir, entry.getName());
	                FileOutputStream fs = new FileOutputStream(newFile);
	                InputStream is = jarFile.getInputStream(entry);
	                byte[] buf = new byte[1024];
	                int len;
	                while((len = is.read(buf)) != -1)
	                {
	                    fs.write(buf, 0, len);
	                }
	                is.close();
	                fs.close();
	            }
	        }

	        saveCachedDirectory(url, dir);
        }
        
        // Find alignment file and other data in manifest.
        Manifest mf = new Manifest(new FileInputStream(new File(dir, "meta-inf/manifest.mf")));
        String alignmentName = mf.getMainAttributes().getValue("Mauve-Alignment");
        File alignmentFile = new File(dir, alignmentName);
        BaseViewerModel model = buildModel(alignmentFile, listener);
        
        model.setSourceURL(url);
        
        // Assign database ids.
        for (int i = 0; i < model.getSequenceCount(); i++)
        {
            Genome g = model.getGenomeByViewingIndex(i);
            g.setID(mf.getMainAttributes().getValue("Sequence-" + (i + 1) + "-ID"));
        }
        return model;
    }
    
    public static File getCachedDirectory(URL url) throws BackingStoreException
    {
        Preferences prefs = getPreferencesForUrl(url);
        String path = prefs.get("dir", null);
        if (path == null) return null;
        File f = new File(path);
        if (!f.canRead() || !f.isDirectory()) return null;
        return f;
    }
    
    static void saveCachedDirectory(URL url, File dir) throws IOException
    {
        try
        {
            Preferences prefs = getPreferencesForUrl(url);
            prefs.put("dir", dir.getCanonicalPath());
            prefs.flush();
        }
        catch (BackingStoreException e)
        {
            System.err.println("Couldn't store preferences.  Exception follows.");
            e.printStackTrace();
        }
        
    }

    /** configures whether a disk cache can be used to speed up repeated loading of alignments */
    static protected boolean useDiskCache = true;
    
    /** configures whether a disk cache can be used to speed up repeated loading of alignments */
    public static void setUseDiskCache(boolean useCache)
    {
    	useDiskCache = useCache;
    }
    /** returns whether a disk cache should be used to speed up repeated loading of alignments */
    public static boolean getUseDiskCache()
    {
    	return useDiskCache;
    }
    
    private static boolean deleteDirectory(File path) 
    {
    	if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}
    
    public static void clearDataCache() throws BackingStoreException
    {
        Preferences prefs = Preferences.userNodeForPackage(ModelBuilder.class);
        String[] children = prefs.childrenNames();
        
        for (int i = 0; i < children.length; i++)
        {
            Preferences child = prefs.node(children[i]);
            String dir = child.get("dir", null);
            if (dir != null)
            {
            	// delete this directory
            	deleteDirectory(new File(dir));
            }
            child.removeNode();
        }
    }
    
    private static Preferences getPreferencesForUrl(URL url) throws BackingStoreException
    {
        Preferences prefs = Preferences.userNodeForPackage(ModelBuilder.class);
        String[] children = prefs.childrenNames();
        
        for (int i = 0; i < children.length; i++)
        {
            Preferences child = prefs.node(children[i]);
            String urlValue = child.get("url", null);
            if (url.toString().equals(urlValue))
            {
                return child;
            }
        }
        
        // Didn't find one, so create one.
        
        int nodeNumber = 0;
        Preferences newChild = prefs.node("child" + nodeNumber);
        while (newChild.get("url", null) != null)
        {
            nodeNumber++;
            newChild = prefs.node("child" + nodeNumber);
        }
        
        newChild.put("url", url.toString());
        return newChild;
    }
    
    /**
     * @throws IOException
     * @throws MauveFormatException
     * 
     * Read some genome comparison data from a file. Attempts to detect the file
     * format. Valid input file formats are XMFA, Mauve FormatVersion 4
     * (possibly containing gapped alignments), and Mauve FormatVersion 3,
     * containing only ungapped alignments.
     */
    public static BaseViewerModel buildModel(File src, ModelProgressListener listener) throws IOException, MauveFormatException
    {
        if (listener != null)
        {
            listener.buildStart();
            listener.alignmentStart();
        }
        
        RandomAccessFile inputFile = new RandomAccessFile(src, "r");
        String inputLine = inputFile.readLine();
        if(inputLine == null)
        	throw new IOException("Empty alignment file.  If the alignment file was generated by Mauve then the genomes may be unrelated.");
        String[] params = inputLine.split(DELIMS);

        // Try to find a version number at the head of the file.
        int versionNumber;
        try
        {
            versionNumber = Integer.parseInt(params[1]);
        }
        catch (Exception e)
        {
            // probably a NumberFormatException or ArrayIndexOfOutBounds because
            // this is not an XMFA file
            versionNumber = -1;
            inputFile.seek(0);
        }

        if (versionNumber == -1) // XMFA file
        {
            XmfaViewerModel model = new XmfaViewerModel(src, listener);
            if (listener != null)
            {
                listener.done();
            }
            model.setReference(model.getGenomeBySourceIndex(0));
            return model;
        }
        else if (versionNumber == 3) // MUMS file
        {
            BaseViewerModel model = new BaseViewerModel(src);
            readCommon(inputFile, model);
            readMums(inputFile, model);
            model.setColorScheme(new NormalizedOffsetColorScheme());
            if (listener != null)
            {
                listener.done();
            }
            model.setReference(model.getGenomeBySourceIndex(0));
            return model;
        }
        else if (versionNumber == 4) // Mauve file
        {
            LcbViewerModel model = new LcbViewerModel(src);
            readCommon(inputFile, model);
            readMauveAlignment(inputFile, model);
            initMauveLCBs(model);
            model.setColorScheme(new LCBColorScheme());
            model.initModelLCBs();
            if (listener != null)
            {
                listener.done();
            }
            model.setReference(model.getGenomeBySourceIndex(0));
            return model;
        }
        else if (versionNumber == 1)
        {
            throw new MauveFormatException("Format version 1 no longer supported.");
        }
        else if (versionNumber == 2)
        {
            throw new MauveFormatException("Format version 2 no longer supported.");
        }
        else
        {
            throw new MauveFormatException("Rearrangement data format version unsupported\n");
        }
    }

    /**
     * @param inputFile
     * @param model
     * @throws IOException
     * 
     * All non-XMFA formats begin with a common header: FormatVersion <int>
     * SequenceCount <int>Sequence <N>File <String>Sequence <N>Length <int>
     * 
     * This reads in this data and constructs genomes for each genetic sequence,
     * adding them to the model.
     */
    private static void readCommon(RandomAccessFile inputFile, BaseViewerModel model) throws IOException
    {
        String inputLine = inputFile.readLine();
        String[] params = inputLine.split(DELIMS);
        model.setSequenceCount(Integer.parseInt(params[1]));

        for (int seqI = 0; seqI < model.getSequenceCount(); seqI++)
        {
            // read file name
            inputLine = inputFile.readLine();
            StringTokenizer st = new StringTokenizer(inputLine);
            st.nextToken();
            String name = st.nextToken();

            // read sequence length
            inputLine = inputFile.readLine();
            st = new StringTokenizer(inputLine, DELIMS);
            st.nextToken();
            long length = Long.parseLong(st.nextToken());

            Genome g = GenomeBuilder.buildGenome(length, name, model, seqI);
            model.setGenome(seqI, g);
        }

        // The next line is ignored, regardless of format!
        // it either holds a number called "IntervalCount" (MAUVE) or
        // "MatchCount" (MUMS)
        // of the form <Name> <int>
        inputFile.readLine();
    }

    /**
     * @param inputFile
     * @param model
     * @throws IOException
     * @throws MauveFormatException
     * 
     * Read a MUMS-format file, which contains only ungapped alignments.
     *  
     */
    private static void readMums(RandomAccessFile inputFile, BaseViewerModel model) throws IOException, MauveFormatException
    {

        String inputLine;

        while ((inputLine = inputFile.readLine()) != null)
        {

            Match new_match = new Match(model.getSequenceCount());
            long m_length = 0;
            int tokenI = 0;
            StringTokenizer st = new StringTokenizer(inputLine, DELIMS);
            int genomeIndex = 0;
            while (st.hasMoreTokens())
            {
                String s = st.nextToken();

                if (tokenI == 0)
                {
                    m_length = Integer.parseInt(s);
                }
                else if (tokenI == model.getSequenceCount() + 1)
                {
                    tokenI++;
                    continue;
                }
                else if (tokenI > model.getSequenceCount())
                {
                    if (Long.parseLong(s) != 0)
                    {
                        throw new MauveFormatException("Match data must have all linked inclusions removed\n");
                    }
                }
                else
                {
                    Genome g = model.getGenomeByViewingIndex(genomeIndex);
                    
                    new_match.setLength(g, m_length);
                    new_match.setStart(g, Long.parseLong(s));
                    if (new_match.getStart(g) < 0)
                    {
                        new_match.setReverse(g, true);
                        new_match.setStart(g, -new_match.getStart(g));
                    }
                    else if (new_match.getStart(g) == Match.NO_MATCH)
                    {
                        new_match.setLength(g, 0);
                    }
                    genomeIndex++;
                }
                tokenI++;
            }
            model.addMatch(new_match);
            if (model.getMatchCount() % 1000 == 0)
            {
                MyConsole.out().println("Processed matches: " + model.getMatchCount());
            }
        }
    }

    /**
     * Read matches from a Mauve FormatVersion 4 alignment. Ignores any gapped
     * alignments that may be contained in the file. Creates a locally collinear
     * block (LCB) object for each aligned "interval" in the file.
     */
    private static void readMauveAlignment(RandomAccessFile match_reader, LcbViewerModel model) throws IOException
    {

        String inputLine;
        int clustal_line = 0;
        int cur_lcb = 0;
        int lcb_start = 0;
        model.setLcbCount(1);
        Vector lcbVector = new Vector();
        
        while ((inputLine = match_reader.readLine()) != null)
        {
            if (clustal_line == model.getSequenceCount() + 2)
                clustal_line = 0;
            if (clustal_line > 0)
            {
                clustal_line++;
                continue;
            }

            Match new_match = new Match(model.getSequenceCount());
            new_match.lcb = cur_lcb;
            long m_length = 0;
            int tokenI = 0;
            StringTokenizer st = new StringTokenizer(inputLine, DELIMS);
            int genomeIndex = 0;
            while (st.hasMoreTokens())
            {
                String s = st.nextToken();

                if (tokenI == 0)
                {
                    if (s.equals("GappedAlignment") || s.equals("ClustalResult"))
                    {
                        clustal_line = 1;
                        new_match = null;
                        break;
                    }
                    else if (s.startsWith("Interval"))
                    {
                        if (model.getMatchCount() > 0)
                        {
                            LCB new_lcb = model.getLCB(lcb_start, model.getMatchCount());
                            lcb_start = model.getMatchCount();
                            if (new_lcb != null)
                            {
                                lcbVector.add(new_lcb);
                                cur_lcb++;
                                model.setLcbCount(model.getLcbCount() + 1);
                            }
                            new_match = null;
                        }
                        break;
                    }
                    else
                        m_length = Integer.parseInt(s);
                }
                else
                {
                    Genome g = model.getGenomeByViewingIndex(genomeIndex);
                    
                    new_match.setLength(g, m_length);
                    new_match.setStart(g, Long.parseLong(s));
                    if (new_match.getStart(g) < 0)
                    {
                        new_match.setReverse(g, true);
                        new_match.setStart(g, -new_match.getStart(g));
                    }
                    else if (new_match.getStart(g) == Match.NO_MATCH)
                    {
                        new_match.setLength(g, 0);
                    }
                    genomeIndex++;
                }
                tokenI++;
            }
            if (tokenI != model.getSequenceCount() + 1)
            {
                continue;
            }
            int count = 0;
            for (int seqI = 0; seqI < model.getSequenceCount(); seqI++)
            {
                Genome g = model.getGenomeByViewingIndex(seqI);
                if (new_match.getLength(g) != Match.NO_MATCH)
                {
                    count++;
                }
            }
            if (count > 1)
            {
                model.addMatch(new_match);
            }
            if (model.getMatchCount() % 1000 == 0)
            {
                MyConsole.out().println("Processed matches: " + model.getMatchCount());
            }
        }
        LCB new_lcb = model.getLCB(lcb_start, model.getMatchCount());
        if (new_lcb != null)
            lcbVector.add(new_lcb);

        model.setFullLcbList(new LCB[lcbVector.size()]);
        model.setFullLcbList((LCB[]) lcbVector.toArray(model.getFullLcbList()));
    }

    /**
     * @param model
     */
    private static void initMauveLCBs(LcbViewerModel model)
    {
        model.setVisibleLcbList(new LCB[model.getFullLcbList().length]);
        model.setDelLcbList(new LCB[0]);
        System.arraycopy(model.getFullLcbList(), 0, model.getVisibleLcbList(), 0, model.getVisibleLcbList().length);
    }

}