package org.gel.mauve.format;

import java.io.File;

import org.gel.mauve.MauveConstants;
import org.gel.mauve.SupportedFormat;

//changed so creates new instance each time called; is lightweight
//and all formats share one cache, not all of one type
//allows genome specific access in delegating sequences
public class SupportedFormatFactory implements MauveConstants {

    public static SupportedFormat guessFormatFromFilename(String filename)
    {
        File f = new File(trimWhiteAndQuotes(filename));
        String name = f.getName().toLowerCase();

        if (name.endsWith(".gbk"))
        {
            return new GenbankFileFormat();
        }
        else if (name.endsWith(".raw"))
        {
            return new RawFormat();
        }
        else if (name.endsWith(".seq"))
        {
            return new GenbankFileFormat();
        }
        else if (name.endsWith(".embl"))
        {
            return new EmblFormat();
        }
        else if (name.endsWith(".xml"))
        {
            return new INSDseqFormat();
        }
        else
        {
            return new FastaFormat();
        }
    }

    public static SupportedFormat formatNameToFormat(String name)
    {
        if (name.equals(GENBANK_FORMAT))
        {
            return new GenbankFileFormat();
        }
        else if (name.equals(FASTA_FORMAT))
        {
            return new FastaFormat();
        }
        else if (name.equals(RAW_FORMAT))
        {
            return new RawFormat();
        }
        else if (name.equals(EMBL_FORMAT))
        {
            return new EmblFormat();
        }
        else if (name.equals(INSD_FORMAT))
        {
            return new INSDseqFormat();
        }

        throw new RuntimeException("Unexpected format: " + name);
    }

    public static String trimWhiteAndQuotes(String path)
    {
        // try stripping quote characters
        String nameSansQuotes = new String(path);
        nameSansQuotes = nameSansQuotes.trim();
        if(nameSansQuotes.length() > 1 &&
        		nameSansQuotes.charAt(0) == '"' && 
        		nameSansQuotes.charAt(nameSansQuotes.length()-1) == '"')
            nameSansQuotes = nameSansQuotes.substring(1, nameSansQuotes.length()-1);
        return nameSansQuotes;
    }
}