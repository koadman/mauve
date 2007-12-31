package org.gel.mauve.format;

import java.io.File;

import org.gel.mauve.MauveConstants;
import org.gel.mauve.SupportedFormat;

public class SupportedFormatFactory implements MauveConstants
{
    final static SupportedFormat GENBANK = new GenbankFileFormat();
    final static SupportedFormat EMBL = new EmblFormat();
    final static SupportedFormat FASTA = new FastaFormat();
    final static SupportedFormat RAW = new RawFormat();
    final static SupportedFormat INSDSEQ = new INSDseqFormat();

    public static SupportedFormat guessFormatFromFilename(String filename)
    {
        File f = new File(trimWhiteAndQuotes(filename));
        String name = f.getName().toLowerCase();

        if (name.endsWith(".gbk"))
        {
            return SupportedFormatFactory.GENBANK;
        }
        else if (name.endsWith(".raw"))
        {
            return SupportedFormatFactory.RAW;
        }
        else if (name.endsWith(".seq"))
        {
            return SupportedFormatFactory.GENBANK;
        }
        else if (name.endsWith(".embl"))
        {
            return SupportedFormatFactory.EMBL;
        }
        else if (name.endsWith(".xml"))
        {
            return SupportedFormatFactory.INSDSEQ;
        }
        else
        {
            return SupportedFormatFactory.FASTA;
        }
    }

    public static SupportedFormat formatNameToFormat(String name)
    {
        if (name.equals(GENBANK_FORMAT))
        {
            return SupportedFormatFactory.GENBANK;
        }
        else if (name.equals(FASTA_FORMAT))
        {
            return SupportedFormatFactory.FASTA;
        }
        else if (name.equals(RAW_FORMAT))
        {
            return SupportedFormatFactory.RAW;
        }
        else if (name.equals(EMBL_FORMAT))
        {
            return SupportedFormatFactory.EMBL;
        }
        else if (name.equals(INSD_FORMAT))
        {
            return SupportedFormatFactory.INSDSEQ;
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