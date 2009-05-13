package org.gel.mauve.operon.pred;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.StrandedFeature;
import org.gel.air.bioj.BioJavaConstants;
import org.gel.air.bioj.BioJavaUtils;
import org.gel.air.util.IOUtils;
import org.gel.mauve.gui.MauveFrame;
import org.gel.mauve.module.MauveModule;
import org.gel.mauve.module.ModuleListener;
import org.gel.mauve.operon.OperonConstants;


public class PredictionHandler implements BioJavaConstants, OperonConstants {
	
	protected Vector <IGD> igds;
	protected Sequence genome;
	protected Vector <IGD> [] typed_igds;
	protected HashSet <Integer> [] test;
	protected HashSet <Integer> [] train;
	protected int length_restriction = Integer.MIN_VALUE;
	protected Hashtable <String, StrandedFeature> bnums;
	protected Vector restricted_genes;
	protected int min_without = 250;
	protected int max_within = 50;
	protected IGDSource source;
	protected ByKmer bk;
	
	public static final String OPERON_DIR = "c:\\mauvedata\\operon\\regdb\\";


	public PredictionHandler(Sequence genome) {
		this.genome = genome;
		restricted_genes = BioJavaUtils.restrictedList(
				BioJavaUtils.getSortedStrandedFeatures(genome),FEAT_TYPES, false);
		bnums = BioJavaUtils.getLoci(restricted_genes);
		System.out.println ("bnums: " + bnums.size());
		resetIGDs ();
	}
	
	public void resetIGDs () {
		igds = new Vector <IGD> ();
		typed_igds = new Vector [5];
		for (int i = 0; i < typed_igds.length; i++)
			typed_igds [i] = new Vector <IGD> ();
	}
	
	public RegDBIGD makeRegDBIGD (String op_file, String gene_file) {
		RegDBIGD reg = new RegDBIGD (this);
		reg.loadOperons (op_file);
		reg.loadGenes (gene_file);
		return reg;
	}
	
	public void getTrainAndTestRegDB () {
		makeIGDs(source);
		loadTestSet ("kmer\\testset4");
		bk = new ByKmer (getData (true), this);
	}
	
	public void getTrainByDistTestRegDB () {
		MauveFeatureIGD mfi = new MauveFeatureIGD (this);
		makeIGDs (mfi);
		loadTestSet ("");
		bk = new ByKmer (getData (true), this);
		resetIGDs ();
		makeIGDs(source);
		loadTestSet ("kmer\\testall");
		
	}
	
	public void regDBIGDComparison () {
		source = makeRegDBIGD(
				"c:\\mauvedata\\operon\\regdb\\OperonSet.txt",
				"c:\\mauvedata\\operon\\regdb\\GeneProductSet.tab");
		getTrainByDistTestRegDB();
		//getTrainAndTestRegDB ();
		
		HashSet <IGD> tests = getData(false) [1];
		tests.addAll(getData(false) [0]);
		bk.summarize (bk.classify (tests), source);
	}
	
	public void operonComparison () {
		
	}
	
	public void loadTestSet (String dir) {
		if (dir != null && dir.length() > 0)
			dir = dir + "\\";
		test = new HashSet [2];
		train = new HashSet [2];
		test [INTERNAL] = IOUtils.readIntoSet(OPERON_DIR + dir + "internals1.txt");
		train [INTERNAL] = IOUtils.readIntoSet(OPERON_DIR + dir + "internals_train1.txt");
		test [INTERNAL].removeAll(train [INTERNAL]);
		test [EXTERNAL] = IOUtils.readIntoSet(OPERON_DIR + dir + "externals1.txt");
		train [EXTERNAL] = IOUtils.readIntoSet(OPERON_DIR + dir + "externals_train1.txt");
		test [EXTERNAL].removeAll(train [EXTERNAL]);
	}
	
	public void sameClassTest () {
		loadTestSet ("");
		train [INTERNAL] = test [EXTERNAL];
		typed_igds [INTERNAL] = typed_igds [EXTERNAL];
	}
	
	public HashSet <IGD> [] getData (boolean train_set) {
		HashSet <Integer> [] current = train_set ? train : test;
		HashSet <IGD> [] data = new HashSet [2];
		for (int j = INTERNAL; j <= EXTERNAL; j++) {
			data [j]= new HashSet <IGD> (current [j].size ());
			Iterator <Integer> itty = current [j].iterator();
			while (itty.hasNext())
				data [j].add(typed_igds [j].get(itty.next ()));
		}
		return data;
	}
	
	public void makeIGDs (IGDSource typer) {
		Iterator <OperonGene> itty = typer.getGenes ().iterator();
		OperonGene prev = itty.next();
		OperonGene current;
		int under = 0;
		/*long time = System.currentTimeMillis(); // no
		int count = 0;*/
		while (itty.hasNext()) {
			/*count++;
			if (count % 100 == 0)
				System.out.println ((System.currentTimeMillis() - time) + " " + count);*/
			current = itty.next();
			IGD igd = new IGD (prev, current);
			int type = typer.getType(igd);
			if (type == INTERNAL && igd.getLength() < 50)
				under++;
			typed_igds [type].add(igd);
			igds.add(igd);
			prev = current;
		}
		for (int i = 0; i < typed_igds.length; i++)
			System.out.println (i + ": " + typed_igds [i].size());
		System.out.println ("under 50: " + under);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MauveModule mv = new MauveModule (new ModuleListener () {
			public void startModule (MauveFrame frame) {
				PredictionHandler pred = new PredictionHandler (
						frame.getModel ().getGenomeBySourceIndex(
						0).getAnnotationSequence());
				pred.regDBIGDComparison ();
			}
		});
		MauveModule.mainHook (args, mv);
	}
	
	
	public class IGD implements Comparable <IGD> {
		
		protected OperonGene gene1;
		protected OperonGene gene2;
		protected String dna;

		public IGD (OperonGene first, OperonGene second) {
			gene1 = first;
			gene2 = second;
			dna = genome.subStr((int) getStart (), (int) getEnd ());
		}
		
		public long getStart () {
			return gene1.feat.getLocation().getMax() + 1;
		}
		
		public long getEnd () {
			return gene2.feat.getLocation().getMin() - 1;
		}
		
		public long getLength () {
			return getEnd () - getStart () + 1;
		}
		
		public int compareTo (IGD comp) {
			if (getStart () < comp.getStart())
				return -1;
			else if (getStart () > comp.getStart())
				return 1;
			else
				return 0;
		}
		
		public boolean sameStrand () {
			return gene1.isReversed () == gene2.isReversed ();
		}
		
		
	}
	
	public static class OperonGene {
		
		protected String operon;
		protected String name;
		protected StrandedFeature feat;
		
		public OperonGene (String gene) {
			name = gene;
		}
		
		public OperonGene (StrandedFeature feat) {
			this.feat = feat;
			name = BioJavaUtils.getName (feat);
		}
		
		protected boolean isReversed () {
			return feat.getStrand() == StrandedFeature.NEGATIVE;
		}
		
	}

}
