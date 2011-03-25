package org.gel.mauve.format;

import java.io.File;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Genome;
import org.gel.mauve.SupportedFormat;

public class FileFinder {

    private static String windowsPathHack(String p)
	{
		// only do this under Mac OS X, which has an inept java.io.File
		if(System.getProperty("os.name").indexOf("indow") > 0)
			return p;
		// only operate on paths with backslashes and drive letter specs
		if(p.length() < 3 || !((p.charAt(1) == ':' && p.charAt(2) == '\\') || p.startsWith("\\\\")))
			return p;

		// replace all backslashes with forward slash and ditch the drive specifier 
		if(p.charAt(1) == ':' && p.charAt(2) == '\\')
			p = p.substring(2,p.length());
		int indie = p.indexOf('\\');
		int previa = 0;
		StringBuilder sb = new StringBuilder();
		while(indie >= 0)
		{
			if(previa < indie)
			{
				sb.append("/");	
				sb.append(p.substring(previa,indie));
			}
			previa = indie+1;
			indie = p.indexOf('\\', indie+1);
		}
		if(previa < p.length())
		{
			sb.append("/");
			sb.append(p.substring(previa, p.length()));
		}
		return sb.toString();
	}
    public static String findFile(BaseViewerModel model, String filename)
    {    	
        File f = new File(filename);
        // first try to read the file as given in the XMFA
        if (f.canRead())
            return filename;

        // try stripping quote characters
        String nameSansQuotes = org.gel.mauve.format.SupportedFormatFactory.trimWhiteAndQuotes(filename);

        f = new File(nameSansQuotes);
        if (f.canRead())
            return nameSansQuotes;

        // normalize windows path names in case we're not running Windows
        nameSansQuotes = windowsPathHack(nameSansQuotes);

        // try the directory of the source alignment with the full path
        filename = model.getSrc().getParent() + File.separatorChar + nameSansQuotes;
        f = new File(filename);
        if (f.canRead())
            return filename;

        // otherwise try the directory of the source alignment with just the filename
    	String path = "";
    	if( nameSansQuotes.length() > 0 )
    		path = model.getSrc().getParent() + File.separatorChar + f.getName();
        return path;
    }

}
