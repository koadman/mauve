package org.gel.mauve.ext.lazy;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Iterator;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.gel.air.ja.stash.Stash;
import org.gel.air.ja.stash.StashList;
import org.gel.air.ja.stash.StashLoader;
import org.gel.mauve.Chromosome;
import org.gel.mauve.FilterCacheSpec;
import org.gel.mauve.Genome;
import org.gel.mauve.bioj.ListSequenceIterator;
import org.gel.mauve.ext.MauveInterfacer;
import org.gel.mauve.ext.MauveStoreConstants;
import org.gel.mauve.format.BaseFormat;

public class LazyFormat extends BaseFormat implements MauveStoreConstants {
	
	protected Stash genome_data;
	public static final String STASH_FORMAT = "st_format";
	protected StashLoader loader = MauveInterfacer.getLoader();
	protected static final FilterCacheSpec [] specs = new FilterCacheSpec [0];
	
	public LazyFormat (Stash data) {
		genome_data = data;
	}

	public String getChromosomeName(Sequence s) {
		return s.getName();
	}

	public FilterCacheSpec[] getFilterCacheSpecs() {
		return specs;
	}

	public String getFormatName() {
		return STASH_FORMAT;
	}

	public String getSequenceName(Sequence s) {
		return genome_data.getString(NAME);
	}

	public boolean isRich() {
		return false;
	}

	public SequenceIterator readFile (File file) {
		try {
			ListSequenceIterator iter = new ListSequenceIterator ();
			StashList contigs = MauveInterfacer.getLoader ().populateVector (
					genome_data.getHashtable (CONTIG_INDEX), false);
			System.out.println ("contigs: " + contigs.size());
			Collections.sort(contigs, START_COMP);
			BufferedInputStream in = new BufferedInputStream (new FileInputStream (
					loader.getFileByID (genome_data.getString (ID) + ".sba")));
			int genome_length = genome_data.getInt(LENGTH);
			in.mark(genome_length);
			for (int i = 0; i < contigs.size(); i++) {
				Stash contig = contigs.get(i);
				LazySymbolList list = new LazySymbolList (in, contig.getLong(LEFT_STRING),
						contig.getLong(RIGHT_STRING), genome_length);
				LazySequence seq = new LazySequence (list);
				seq.setName(contig.getString(NAME));
				iter.add(seq);	
			}
			return iter;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

}
