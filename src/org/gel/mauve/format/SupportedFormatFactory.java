package org.gel.mauve.format;

import java.io.File;

import org.gel.mauve.SupportedFormat;

public class SupportedFormatFactory
{
    final static SupportedFormat GENBANK = new GenbankFileFormat();
    final static SupportedFormat EMBL = new EmblFormat();
    final static SupportedFormat FASTA = new FastaFormat();
    final static SupportedFormat RAW = new RawFormat();

    public static SupportedFormat guessFormatFromFilename(String filename)
    {
        File f = new File(filename);
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
        else
        {
            return SupportedFormatFactory.FASTA;
        }
    }

    public static SupportedFormat formatNameToFormat(String name)
    {
        if (name.equals("GenBank"))
        {
            return SupportedFormatFactory.GENBANK;
        }
        else if (name.equals("FastA"))
        {
            return SupportedFormatFactory.FASTA;
        }
        else if (name.equals("Raw"))
        {
            return SupportedFormatFactory.RAW;
        }
        else if (name.equals("EMBL"))
        {
            return SupportedFormatFactory.EMBL;
        }

        throw new RuntimeException("Unexpected format: " + name);
    }

}