package org.gel.mauve.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.gel.mauve.FilterCacheSpec;
import org.gel.mauve.MauveConstants;

public class INSDseqFormat extends GenbankEmblFormat {

	public SequenceIterator readFile(File file) {
        BufferedReader reader;
        try
        {
            reader = new BufferedReader(new FileReader(file));
        }
        catch (FileNotFoundException e)
        {
            // This exception is not expected, because file is required to
            // exist.
            throw new RuntimeException(e);
        }
        SimpleNamespace namesp = new SimpleNamespace(file.getName());
        return RichSequence.IOTools.readINSDseqDNA(reader, namesp);
	}

    public boolean isRich(){ return true; }
    
    public String getFormatName () {
    	return MauveConstants.INSD_FORMAT;
    }
}
