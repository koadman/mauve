package org.gel.mauve.ext.lazy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import org.gel.air.ja.stash.Stash;
import org.gel.air.ja.stash.StashList;
import org.gel.air.ja.stash.StashLoader;
import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;
import org.gel.mauve.GenomeBuilder;
import org.gel.mauve.MauveAlignmentViewerModel;
import org.gel.mauve.ModelBuilder;
import org.gel.mauve.ModelProgressListener;
import org.gel.mauve.backbone.BackboneListBuilder;
import org.gel.mauve.ext.MauveInterfacer;
import org.gel.mauve.ext.MauveStoreConstants;
import org.gel.mauve.ext.ModelFactory;

public class StashViewerModel extends MauveAlignmentViewerModel implements
		MauveStoreConstants {
	
	public static final ModelFactory factory = new StashFactory ();
	
	
	protected StashLoader loader;
	protected Stash alignment;
	protected StashList aligned_genomes;
	
	public StashViewerModel (Hashtable src, ModelProgressListener listener) 
			throws IOException {
		super (new File (((Stash) src.get (ALIGNMENT)).getString(NAME)), 
				listener, src);
	}

	protected void init (ModelProgressListener listener, 
			boolean reloading, Hashtable args) 
			throws IOException {
		loader = (StashLoader) args.get (LOADER);
		alignment = (Stash) args.get(ALIGNMENT);
		ModelBuilder.setUseDiskCache(false);
		super.init (listener, reloading, args);
	}
	
	protected void loadGenomes () {
		Stash genome_list = alignment.getHashtable (GENOMES);
		int count = alignment.getInt (GENOME_COUNT);
		setSequenceCount (count);
		aligned_genomes = loader.populateVector(genome_list, false);
		for (int i = 0; i < count; i++) {
			Genome genome = loadGenome (loader.getStash(aligned_genomes.get (
					i).getString (GENOME)), i);
			genomes [i] = genome;
			sourceGenomes [i] = genome;
		}
	}
	
	protected Genome loadGenome (Stash genome_data, int index) {
		Genome genome = GenomeBuilder.buildGenome(genome_data.getLong (LENGTH), 
				new File (genome_data.getString(ID).substring(GENOME_CLASS.length() + 1)),
				new LazyFormat (genome_data), this, -1, index);
		makeContigs (genome_data, genome);
		return genome;
	}
	
	protected void makeContigs (Stash genome_data, Genome genome) {
		StashList contigs = MauveInterfacer.getLoader ().populateVector (
				genome_data.getHashtable (CONTIG_INDEX), false);
		Collections.sort(contigs, START_COMP);
		ArrayList <Chromosome> conts = new ArrayList <Chromosome> ();
		for (int i = 0; i < contigs.size(); i++) {
			Stash contig = contigs.get(i);
			conts.add(new Chromosome (contig.getLong(LEFT_STRING), 
					contig.getLong(RIGHT_STRING), contig.getString(NAME), true));
		}
		genome.setChromosomes(conts);
	}
	
	public void setSequenceCount (int sequenceCount) {
		if (genomes == null)
			super.setSequenceCount (sequenceCount);
	}
	
	protected void makeAlignment () {
		xmfa = new StashedAlignment (this, alignment);
		setFullLcbList (((StashedAlignment) xmfa).getLCBList ());
	}
	
	protected void makeBBList () {
		try {
			BackboneListBuilder.build(this, xmfa);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected Genome makeGenome(int genome_ind) {
		return sourceGenomes [genome_ind];
	}

	public File getBbFile() {
		return loader.getFileByID(alignment.getString(ID) + ".bbcols");
	}

	protected void makeSimilarityIndex () {
		for (int seqI = 0; seqI < getSequenceCount (); seqI++) {
			MauveInterfacer.readSimilarity(this, 
					aligned_genomes.get(seqI), seqI); 
		}
	}
	
	public StashLoader getLoader () {
		return loader;
	}
    
    public static class StashFactory implements ModelFactory {

		public BaseViewerModel createModel(Object source,
				ModelProgressListener listener) {
			try {
				return new StashViewerModel ((Hashtable) source, listener);
			}
			catch (IOException e) {
				return null;
			}
		}

		public String getUniqueName() {
			return LazyFormat.STASH_FORMAT;
		}
    	
    }
}
