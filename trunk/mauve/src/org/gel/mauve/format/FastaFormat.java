package org.gel.mauve.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.io.SeqIOTools;
import org.gel.mauve.FilterCacheSpec;
import org.gel.mauve.MauveConstants;

public class FastaFormat extends BaseFormat {
	public SequenceIterator readFile (File file) {
		BufferedReader reader = openFile (file);
		return SeqIOTools.readFastaDNA (reader);
	}

	protected BufferedReader openFile (File file) {
		try {
			return new BufferedReader (new FileReader (file));
		} catch (FileNotFoundException e) {
			// This exception is not expected, because file is required to
			// exist.
			throw new RuntimeException (e);
		}
	}

	public String getSequenceName (Sequence s) {
		return s.getName ();
	}

	public String getChromosomeName (Sequence s) {
		return s.getName ();
	}

	public FilterCacheSpec [] getFilterCacheSpecs () {
		return new FilterCacheSpec [0];
	}

    public boolean isRich(){ return false; }
    
    public String getFormatName () {
    	return MauveConstants.FASTA_FORMAT;
    }
}