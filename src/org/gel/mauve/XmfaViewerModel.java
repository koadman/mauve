package org.gel.mauve;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.BackingStoreException;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.SymbolList;
import org.gel.mauve.analysis.PermutationExporter;
import org.gel.mauve.backbone.BackboneList;
import org.gel.mauve.backbone.BackboneListBuilder;
import org.gel.mauve.color.BackboneLcbColor;
import org.gel.mauve.color.LCBColorScheme;
import org.gel.mauve.histogram.HistogramBuilder;
import org.gel.mauve.remote.MauveDisplayCommunicator;
import org.gel.mauve.remote.WargDisplayCommunicator;

/**
 * @author pinfield
 * 
 * A viewer model backed by an XMFA file. Models a global gapped sequence
 * alignment in an XMFA format file
 */
public class XmfaViewerModel extends LcbViewerModel {
	private XMFAAlignment xmfa;
	// Sequence similarity profiles calculated over the length of each sequence
	// represented in an XMFA file
	private SimilarityIndex [] sim;
	private long [] highlights;
	private BackboneList bb_list;

	public void setSequenceCount (int sequenceCount) {
		super.setSequenceCount (sequenceCount);
		sim = new SimilarityIndex [sequenceCount];
	}

	public XmfaViewerModel (File src, ModelProgressListener listener)
			throws IOException {
		super (src);
		super.setDrawLcbBounds(false);
		init (listener, false);
	}

    /**
     * @param listener
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void init(ModelProgressListener listener, boolean isReloading) throws FileNotFoundException, IOException
    {
        // attempt to load data such as SimilarityIndexes from a
    	// disk-based cache file
        // Find cached directory, if it exists.
    	URL xmfa_url = new URL("file://" + getSrc());
        File dir = null;
        if(ModelBuilder.getUseDiskCache())
        {
	        try
	        {
	            dir = ModelBuilder.getCachedDirectory(xmfa_url);
	        }
	        catch (BackingStoreException e)
	        {
	            System.err.println("Error reading preferences.  Error follows.  Will load from server.");
	            e.printStackTrace();
	        }
        }
        if( ModelBuilder.getUseDiskCache() && dir == null ){
	        // Create a temporary directory.
	        dir = File.createTempFile("mauve", "dir");
	        dir.delete();
	        dir.mkdir();
	        if (!dir.exists() || !dir.isDirectory())
	        {
	            throw new IOException("Couldn't create temporary directory.");
	        }
	        ModelBuilder.saveCachedDirectory(xmfa_url, dir);
        }
        
        if (listener != null)
        {
            listener.alignmentStart();
        }

        // check whether XMFA has changed since the
        // cache was created!!
        // open object I/O for caching
        File cache_file = null;
        ObjectInputStream cache_instream = null;
        if(ModelBuilder.getUseDiskCache())
        {
            cache_file = new File(dir, "mauve.cache");
	        if(cache_file.exists() && cache_file.canRead() &&
	        		getSrc().lastModified() < cache_file.lastModified())
	        {
	        	cache_instream = new ObjectInputStream(new java.io.FileInputStream(cache_file));
	        }
        }

        RandomAccessFile inputFile = new RandomAccessFile(getSrc(), "r");
        
        // read XMFA from object cache if possible
        if(cache_instream != null){
            try{
            	xmfa = (XMFAAlignment)cache_instream.readObject();
            	xmfa.setFile(inputFile);
            }catch(ClassNotFoundException cnfe){
            	// cache must be corrupt
            	cache_instream = null;
            }catch(ClassCastException cce){
            	// cache must be corrupt
            	cache_instream = null;
            }catch(InvalidClassException ice){
            	cache_instream = null;
            }
        }
        // it didn't get read from the cache
        if(cache_instream == null)
            xmfa = new XMFAAlignment(inputFile);

        // If no sequences are found, this is certainly an invalid file.
        if (xmfa.seq_count == 0)
        {
            throw new IOException("Not an XMFA file.  Please check that the" +
            		" input file is a properly formatted alignment.");
        }
        
        if (listener != null)
        {
            listener.alignmentEnd(xmfa.seq_count);
        }

        if (!isReloading)
        {
            setSequenceCount(xmfa.seq_count);
        }

        // now build genomes
        for (int seqI = 0; seqI < xmfa.seq_count; seqI++)
        {
            if (listener != null)
            {
                listener.featureStart(seqI);
            }

            Genome g = null;
            if (!isReloading)
            {
                g = GenomeBuilder.buildGenome(seqI, this);
                setGenome(seqI, g);
            }
            else
            {
                // If reloading, reorder the genomes to the same order as
                // in the file.  reload() will take care of the reordering.
            	g = getGenomeBySourceIndex(seqI);
            }
        }
        
        // now try to read a backbone list
        try{
        	bb_list = BackboneListBuilder.build(this,xmfa);
        	if( bb_list != null )
        	{
            	// if the backbone is newer than the cache then clear the cache
	        	File bb_file = BackboneListBuilder.getFileByKey(this,xmfa,"BackboneFile");
	            if(	ModelBuilder.getUseDiskCache() && bb_file.lastModified() > cache_file.lastModified())
	            	cache_instream = null;
        	}
        }catch(IOException ioe)
        {
        	bb_list = null;
        }

        // now compute SimilarityIndex
        for (int seqI = 0; seqI < xmfa.seq_count; seqI++)
        {
            Genome g = getGenomeBySourceIndex(seqI);
            // read the SimilarityIndex from object cache if possible
            if(cache_instream != null){
                try{
                	sim[seqI] = (SimilarityIndex)cache_instream.readObject();
                }catch(ClassNotFoundException cnfe){
                	// cache must be corrupt
                	cache_instream = null;
                }catch(ClassCastException cce){
                	// cache must be corrupt
                	cache_instream = null;
                }catch(InvalidClassException ice){
                	cache_instream = null;
                }
            }
            // it didn't get read from the cache
            if(cache_instream == null)
            	sim[seqI] = new SimilarityIndex(g, xmfa, bb_list);
        }

        // if cache_instream is null there must have been a problem
        // reading the cache.  write out all objects that should be cached
        if(cache_instream == null && ModelBuilder.getUseDiskCache()){
        	ObjectOutputStream cache_outstream = null;
        	cache_outstream = new ObjectOutputStream(new FileOutputStream(cache_file));
        	cache_outstream.writeObject(xmfa);
        	for( int seqI = 0; seqI < xmfa.seq_count; seqI++ )
            	cache_outstream.writeObject(sim[seqI]);
        }
        
        // copy the LCB list
        setFullLcbList(new LCB[xmfa.lcb_list.length]);
        System.arraycopy(xmfa.lcb_list, 0, getFullLcbList(), 0, xmfa.lcb_list.length);

        setDelLcbList(new LCB[0]);
        setLcbCount(getFullLcbList().length);
        
        highlights = new long[getSequenceCount()];
        Arrays.fill(highlights, Match.NO_MATCH);

        // set LCB colors first
    	setColorScheme(new LCBColorScheme());
        if( bb_list != null )
        	setColorScheme(new BackboneLcbColor());
        initModelLCBs();
        
        // check if there's a histogram
        File histFile = BackboneListBuilder.getFileByKey(this, xmfa, "HistogramFile");
        if(histFile != null){
	        RandomAccessFile raf = new RandomAccessFile(histFile, "r");
	        HistogramBuilder.build(raf, this);
        }
        
        // Now that we've initialized everything, we can make split LCBs
        LCB[] splitLCBs = PermutationExporter.getSplitLCBs(this); 
        setSplitLcbList(splitLCBs);
      //  LCB[] testSplitLCBs = PermutationExporter.splitLcbList(this, splitLCBs, genomes);
      //  System.err.println("");
        // try publishing this viewer model via DBus
    }

    MauveDisplayCommunicator mdCommunicator = null;
    WargDisplayCommunicator wdCommunicator = null;
    /*
     * Attempts to open two-way DBus communication with the weakarg app for this viewer model
     * Warning, when this fails, it currently fails silently!!
     */
    public void initDbusCommunication(){
        try{
    	try{
        try{
	       	mdCommunicator = new MauveDisplayCommunicator(this);
        }catch(UnsatisfiedLinkError ule){}
        }catch(NoClassDefFoundError ncdfe){}
        }catch(Exception e){}
        // try connecting to a warg instance
        try{
    	try{
        try{
        	wdCommunicator = new WargDisplayCommunicator(this);
        	
        }catch(UnsatisfiedLinkError ule){}
        }catch(NoClassDefFoundError ncdfe){}
        }catch(Exception e){ }
    }

	protected void referenceUpdated () {
		super.referenceUpdated ();
		xmfa.setReference (getReference ());
	}

	/**
     * 
     * @return
     */
    public XMFAAlignment getXmfa()
    {
        return xmfa;
    }

	// NEWTODO: Sort sims on sourceIndex instead!
	public SimilarityIndex getSim (Genome g) {
		return sim[g.getSourceIndex ()];
	}

	/**
	 * Identifies the LCB and the column within the LCB of a given sequence
	 * position
	 * 
	 * @return an array of 2 longs. The first being the LCB by index,
	 *         and the second the column 
	 */
	public long [] getLCBAndColumn (Genome g, long position) {
		return xmfa.getLCBAndColumn (g, position);
	}
	
	public long[] getLCBAndColumn(int genSrcIdx, long position){
		return this.getLCBAndColumn(genomes[genSrcIdx], position);
	}

	public int getLCBIndex (Genome g, long position) {
		return xmfa.getLCB (g, position);
	}
	
	/**
	 * Extracts columns from the sequence alignment containing the specified
	 * range of the specified sequence. 
	 * 
	 * 
	 * @param g
	 * 			the genome whose sequence is of interest
	 * @param lend
	 *            The left end coordinate of the range to be extracted
	 * @param rend
	 *            The right end coordinate of the range to be extracted
	 * @return A set of alignment columns stored as an array of byte arrays
	 *         indexed as [sequence][column]
	 *         
	 */
	public byte[][] getSequenceRange(Genome g, long left, long right){
		byte[][] tmp = xmfa.getRange(g, left, right);
		for (int i = 0; i < tmp.length; i++){
			tmp[i] = XMFAAlignment.filterNewlines(tmp[i]);
		}
		return tmp;
	}
	
	/**
	 * Extracts columns from the sequence alignment containing the specified
	 * range of the specified sequence. 
	 * 
	 * 
	 * @param genSrcIdx
	 * 			the source index of the genome whose sequence is of interest
	 * @param lend
	 *            The left end coordinate of the range to be extracted
	 * @param rend
	 *            The right end coordinate of the range to be extracted
	 * @return A set of alignment columns stored as an array of byte arrays
	 *         indexed as [sequence][column]
	 * 
	 *     
	 */
	/* FIXME */
	public byte[][] getSequenceRange(int genSrcIdx, long left, long right){
		return this.getSequenceRange(genomes[genSrcIdx], left, right);
	}

	/**
	 * The backbone list or null if none exists
	 * 
	 * @return The backbone list or null if none exists
	 */
	public BackboneList getBackboneList () {
		return bb_list;
	}

	/**
	 * 
	 * Returns column coordinates in source genome order
	 * 
	 * @param lcb LCB id
	 * @param column column of interest
	 * @param seq_coords  The sequence coordinates (output)
	 * @param gap True whenever a given sequence has a gap in the query column
	 * @return
	 */
	public void getColumnCoordinates (int lcb, long column, long [] seq_coords, boolean [] gap) {
		xmfa.getColumnCoordinates (this, lcb, column, seq_coords, gap);
	}
	
	/**
	 * Returns the position in genome <code>genY</code> that is homologous
	 * to position <code>pos</code> in genome <code>genX</code>.
	 * 
	 * 
	 * @param genX input genome
	 * @param pos input position
	 * @param genY query genome
	 * @return 0 if no positions in genome <code>genY</code> align to 
	 * 		   <code>pos</code> in <code>genX</code>, else the homologous
	 *         position in genome <code>genY</code>
	 */
	public long getHomologousCoordinate(int genX, long pos, int genY){
		long[] lcb = getLCBAndColumn(genomes[genX], pos);
		long[] seq_coords = new long[genomes.length];
		boolean[] gap = new boolean[genomes.length];
		getColumnCoordinates((int) lcb[0], lcb[1], seq_coords, gap);
		if (!gap[genY])
			return seq_coords[genY];
		else
			return 0;
	}
	
	/**
	 * Returns the sequence between <code>start</code> and <code>end</code>, inclusive,
	 * in the specified genome.
	 * <br>
	 * If <code>start</code> > <code>end</code>, the reverse complement is returned. 
	 * </br>
	 * @param start start of the sequence to extract
	 * @param end end of the sequence to extract
	 * @param genSrcIdx the genome
	 * @return a <code>char</code> array representation of the sequence
	 * 
	 * @author atritt
	 */
	public char[] getSequence(long start, long end, int genSrcIdx){
		Sequence annSeq = genomes[genSrcIdx].getAnnotationSequence();
		if (annSeq == null){
			return null;
		} else {
			try {
				if (start > end){
					return DNATools.reverseComplement(annSeq.subList((int)end, (int)start)).seqString().toCharArray();
				} else {
					return annSeq.subList((int) start, (int)end).seqString().toCharArray();
				}
			} catch (Exception e){
				System.err.println("Error getting sequence coordinates (" 
						+start+", "+end+ ") for " + genomes[genSrcIdx].getDisplayName());
				System.err.println("Sequence : " + annSeq.length());
				e.printStackTrace();
			}
		}
		return null;
	}

	public void updateHighlight (Genome g, long coordinate) {
		highlights = null;
		// Wait til end to call super, since it fires event.
		super.updateHighlight (g, coordinate);
	}

	public long getHighlight (Genome g) {
		if (highlights == null) {
			long [] iv_col = getLCBAndColumn (getHighlightGenome (),
					getHighlightCoordinate ());
			boolean [] gap = new boolean [this.getSequenceCount ()];
			;
			highlights = new long [this.getSequenceCount ()];
			getColumnCoordinates ((int) iv_col[0], iv_col[1], highlights, gap);
			for (int i = 0; i < highlights.length; ++i) {
				highlights[i] = Math.abs (highlights[i]);
				if (gap[i])
					highlights[i] *= -1;
			}
		}
		return highlights[g.getSourceIndex ()];
	}

	/**
	 * aligns the display to a particular position of a particular sequence.
	 * typically called by RRSequencePanel when the user clicks a part of the
	 * sequence. Used for display mode 3
	 */
	public void alignView (Genome g, long position) {
		long [] iv_col;
		try {
			iv_col = getLCBAndColumn (g, position);
		} catch (ArrayIndexOutOfBoundsException e) {
			// User clicked outside of bounds of sequence, so do nothing.
			return;
		}
		long [] coords = new long [this.getSequenceCount ()];
		;
		boolean [] gap = new boolean [this.getSequenceCount ()];
		;
		getColumnCoordinates ((int) iv_col[0], iv_col[1], coords, gap);
		alignView (coords, g);
	}

	/**
	 * Overrides setFocus() in BaseViewerModel to also align the display
	 * appropriately
	 */
	public void setFocus (String sequenceID, long start, long end, String contig) {
		super.setFocus (sequenceID, start, end, contig);
		Genome g = null;
		for (int i = 0; i < genomes.length; i++) {
			if (sequenceID.equals (genomes[i].getID ())) {
				g = genomes[i];
				break;
			}
		}
		if (g == null) {
			System.err
					.println ("Received focus request for nonexistent sequence id "
							+ sequenceID);
			return;
		}
		if (contig != null)
			start = contig_handler.getPseudoCoord(g.getSourceIndex(), start, contig);
		alignView (g, start);
	}

	public void reload () {
		fireReloadStartEvent ();

		try {
			init (null, true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace ();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace ();
		}

		fireReloadEndEvent ();
	}

	protected void fireReloadEndEvent () {
		// Guaranteed to return a non-null array
		Object [] listeners = listenerList.getListenerList ();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ModelListener.class) {
				((ModelListener) listeners[i + 1]).modelReloadEnd (modelEvent);
			}
		}
	}

	protected void fireReloadStartEvent () {
		// Guaranteed to return a non-null array
		Object [] listeners = listenerList.getListenerList ();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ModelListener.class) {
				((ModelListener) listeners[i + 1])
						.modelReloadStart (modelEvent);
			}
		}
	}

	boolean drawSimilarityRanges = true;	// whether or not the whole range of similarity values should be drawn
	public boolean getDrawSimilarityRanges() {
		return drawSimilarityRanges;
	}
	public void setDrawSimilarityRanges(boolean drawSimilarityRanges) {
		if (this.drawSimilarityRanges != drawSimilarityRanges) {
			this.drawSimilarityRanges = drawSimilarityRanges;
			fireDrawingSettingsEvent ();
		}
	}
}
