package org.gel.mauve.ext.lazy;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.gel.air.ja.stash.Stash;
import org.gel.air.ja.stash.StashXMLLoader;
import org.gel.air.util.IOUtils;
import org.gel.mauve.FilterCacheSpec;
import org.gel.mauve.Genome;
import org.gel.mauve.bioj.ListSequenceIterator;
import org.gel.mauve.ext.MauveInterfacer;
import org.gel.mauve.ext.MauveStoreConstants;
import org.gel.mauve.format.BaseFormat;

public class LazyFormat extends BaseFormat implements MauveStoreConstants {
	
	protected Stash genome_data;
	public static final String STASH_FORMAT = "st_format";
	protected StashXMLLoader loader = MauveInterfacer.getLoader();
	protected static final FilterCacheSpec [] specs = new FilterCacheSpec [0];
	protected ListSequenceIterator iter;
	protected Genome genome;
	
	public LazyFormat (Stash data) {
		genome_data = data;
		makeSequenceIterator ();
	}
	
	protected void makeSequenceIterator () {
		try {
			iter = new ListSequenceIterator ();
			BufferedInputStream in = new BufferedInputStream (new FileInputStream (
					loader.getAssociatedFile (genome_data.getString (ID), ".sba")), 
					IOUtils.BUFFER_SIZE);
			int genome_length = genome_data.getInt(LENGTH);
			in.mark(genome_length);
			LazySymbolList list = new LazySymbolList (in, 1, genome.getLength(), genome);
			LazySequence seq = new LazySequence (list);
			seq.setName(genome_data.getString(NAME));
			iter.add(seq);	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

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
		ListSequenceIterator ret = new ListSequenceIterator ();
		ret.addAll(iter);
		return ret;
	}
	
	/*public SequenceIterator makeSequenceIterator () {
		try {
			ListSequenceIterator iter = new ListSequenceIterator ();
			StashList contigs = MauveInterfacer.getLoader ().populateVector (
					genome_data.getHashtable (CONTIG_INDEX), false);
			Collections.sort(contigs, START_COMP);
			BufferedInputStream in = new BufferedInputStream (new FileInputStream (
					loader.getFileByID (genome_data.getString (ID) + ".sba")));
			int genome_length = genome_data.getInt(LENGTH);
			System.out.println ("length: " + genome_length);
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
	}*/

}
