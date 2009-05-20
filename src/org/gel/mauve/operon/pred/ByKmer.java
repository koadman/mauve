package org.gel.mauve.operon.pred;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import org.gel.air.util.MathUtils;
import org.gel.mauve.operon.OperonConstants;
import org.gel.mauve.operon.pred.PredictionHandler.IGD;

public class ByKmer implements OperonConstants {

	HashSet <IGD> [] igds;
	Hashtable <String, Kmer> kmers;
	int [] total_mers;
	int [] total_trans;
	int length;
	boolean pseudo_counts;
	protected PredictionHandler handler;
	
	public ByKmer(HashSet <IGD> [] train, PredictionHandler pred) {
		handler = pred;
		igds = train;
		length = 3;
		kmers = new Hashtable <String, Kmer> ();
		total_mers = new int [2];
		total_trans = new int [total_mers.length];
		if (pseudo_counts) {
			total_mers [0] = (int) Math.pow(4, length);
			total_mers [1] = total_mers [0];
			total_trans [0] = (int) Math.pow(4, length + 1);
			total_trans [1] = total_trans [0];
		}
		train ();
		printKmerLogOdds ();
		calcKmerPvalLogLikelihood ();
		printConditionalKmerLogOdds ();
		calcConditionalKmerLikelihood ();
	}
	
	public void classify (HashSet <IGD> igds) {
		Iterator <IGD> itty = igds.iterator();
		while (itty.hasNext()) {
			IGD igd = itty.next();
			classifyIGD (igd);
		}
	}
	
	public void classifyIGD (IGD igd) {
		String dna = igd.dna;
		if (!igd.sameStrand() || dna.length() > handler.min_without) {
			igd.s_ext_prob = 0;
			igd.s_int_prob = Double.NEGATIVE_INFINITY;
			igd.ratio = Double.NEGATIVE_INFINITY;
		}
		else if (dna.length() < handler.max_within/*length*/) {
			igd.ratio = Double.POSITIVE_INFINITY;
			igd.s_int_prob = 0;
			igd.s_ext_prob = Double.NEGATIVE_INFINITY;
		}
		else {
			Kmer prev = kmers.get(dna.substring(0, length));
			igd.s_int_prob = prev.marg_probs [PredictionHandler.INTERNAL];
			igd.s_ext_prob = prev.marg_probs [PredictionHandler.EXTERNAL];
			for (int i = 1; i + length <= igd.dna.length(); i++) {
				Kmer cur = kmers.get(dna.substring (i, i + length));
				igd.s_int_prob += prev.trans_probs [INTERNAL].get(
						cur.symbols);
				igd.s_ext_prob += prev.trans_probs [PredictionHandler.EXTERNAL].get(
						cur.symbols);
				prev = cur;
			}
			igd.ratio = igd.s_int_prob - igd.s_ext_prob;
		}
	}
	
	public void summarize (HashSet <IGD> igds, IGDSource typer) {
		int [][] counts = new int [2][2];
		try {
			PrintStream out = new PrintStream (new FileOutputStream (
					"c:\\mauvedata\\operon\\regdb\\preds.tab"));
			Iterator <IGD> itty = igds.iterator();
			out.println ("start\tlength\treal\tpred\tprob");
			while (itty.hasNext()) {
				IGD igd = itty.next ();
				int real = typer.getType(igd);
				int pred  = igd.ratio > 0 ? INTERNAL : 
					PredictionHandler.EXTERNAL;
				out.println(igd.getStart() + "\t" + igd.getLength () + "\t" + 
						real + "\t" + pred + "\t" + MathUtils.infToExtreme(
						igd.ratio));
				counts [real][pred]++;
			}
			out.close();
			int real_ints = counts [INTERNAL][0] +
					counts [INTERNAL][1];
			int real_exts = igds.size() - real_ints;
			double correct_int = counts [INTERNAL]
			        [INTERNAL] / (double) real_ints; 
			double correct_ext = counts [PredictionHandler.EXTERNAL]
			        [PredictionHandler.EXTERNAL] / (double) real_exts;
			double correct_total = (counts [INTERNAL]
			        [INTERNAL] +
			        counts [PredictionHandler.EXTERNAL]
			        [PredictionHandler.EXTERNAL]) / (double) igds.size ();
			double incorrect_int = counts [INTERNAL]
   			        [PredictionHandler.EXTERNAL] / (double) real_ints; 
   			double incorrect_ext = counts [PredictionHandler.EXTERNAL]
   			        [INTERNAL] / (double) real_exts;
   			double incorrect_total = (counts [INTERNAL]
   			        [PredictionHandler.EXTERNAL] +
   			        counts [PredictionHandler.EXTERNAL]
   			        [INTERNAL]) / (double) igds.size ();
			System.out.println ("real internals: " + real_ints);
			System.out.println ("real externals: " + real_exts);
			System.out.println ("correct internals: " + correct_int);
			System.out.println ("correct externals: " + correct_ext);
			System.out.println ("total correct: " + correct_total);
			System.out.println ("incorrect internals: " + incorrect_int);
			System.out.println ("incorrect externals: " + incorrect_ext);
			System.out.println ("total incorrect: " + incorrect_total);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void train () {
		for (int i = INTERNAL; i <= 
			PredictionHandler.EXTERNAL; i++) {
			Iterator <IGD> itty = igds [i].iterator();
			while (itty.hasNext()) {
				IGD igd = itty.next();
				String dna = igd.dna;
				if (dna.length() >= length) {
					int count = dna.length() - length;
					total_mers [i] += count + 1;
					total_trans [i] += count;
					Kmer prior = null;
					for (int j = 0; j + length <= dna.length (); j++) {
						String mer = dna.substring(j, j + length);
						Kmer kmer = kmers.get(mer);
						if (kmer == null) {
							kmer = new Kmer (mer);
							kmers.put(mer, kmer);
						}
						kmer.counts [i]++;
						if (prior != null) {
							int t_count = prior.trans_counts [i].get(mer) + 1;
							prior.trans_counts [i].put(mer, t_count);
							prior.transitions [i]++;
						}
						prior = kmer;
					}
				}
			}
		}
	}
	
	public void printMarginalCounts () {
		try {
			PrintStream out = new PrintStream (new FileOutputStream (
					"c:\\mauvedata\\operon\\regdb\\kmers.tab"));
		for (int i = 0; i <= PredictionHandler.EXTERNAL; i++) {
			out.println ("total: " + total_mers [i]);
			Iterator <Kmer> itty = kmers.values().iterator();
			while (itty.hasNext()) {
				Kmer kmer = itty.next();
				out.println (kmer.symbols + "\t" + kmer.counts [i]);
			}
			
		}
		out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void printConditionalKmerLogOdds () {
		try {
			PrintStream out = new PrintStream (new FileOutputStream (
					"c:\\mauvedata\\operon\\regdb\\kmer_cond.tab"));
			Iterator <Kmer> itty = kmers.values().iterator();
			while (itty.hasNext()) {
				Kmer kmer = itty.next ();
				Iterator <String> keys = kmer.trans_counts 
						[INTERNAL].keySet().iterator();
				while (keys.hasNext()) {
					String next = keys.next();
					out.print(kmer.symbols + "\t" + next + "\t");
					for (int i = 0; i <= PredictionHandler.EXTERNAL; i++) {
						/*if (kmer.trans_counts [i].get (next) == 0) {
							//System.out.println ("zero num " + kmer.symbols + " " + next);
							kmer.trans_counts [i].put (next, 1);
							kmer.transitions [i]++;
						}*/
						double prob = 0;
						if (kmer.trans_counts [i].get (next) != 0) {
						prob = Math.log (kmer.trans_counts [i].get (next) / 
						((double) kmer.transitions [i]));
						}
						kmer.trans_probs [i].put(next, prob);
					}
					out.println (kmer.trans_probs [INTERNAL].get(
							next) - kmer.trans_probs [PredictionHandler.EXTERNAL].
							get (next));
				}
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void calcKmerPvalLogLikelihood () {
		double [] thetas = new double [kmers.size()];
		Iterator <Kmer> itty = kmers.values().iterator ();
		int i = 0;
		double total = total_mers [INTERNAL] + total_mers 
				[PredictionHandler.EXTERNAL];
		System.out.println ("kmer tot: " + total);
		System.out.println ("kmer int: " + total_mers [INTERNAL]);
		System.out.println ("kmer ext: " + total_mers [EXTERNAL]);
		double ratio = 0;
		while (itty.hasNext()) {
			Kmer kmer = itty.next();
			int count = kmer.counts [INTERNAL] + kmer.
					counts [PredictionHandler.EXTERNAL];
			thetas [i] = count / total;
			ratio += count * Math.log(thetas [i++]);
			for (int j = 0; j < total_mers.length; j++)
				ratio -= kmer.counts [j] * kmer.marg_probs [j];
		}
		ratio *= -2;
		System.out.println ("likelihood value: " + ratio);
	}

	public void calcConditionalKmerLikelihood () {
		double [] thetas = new double [kmers.size() * 4];
		Iterator <Kmer> itty = kmers.values().iterator ();
		int i = 0;
		double ratio = 0;
		while (itty.hasNext()) {
			Kmer kmer = itty.next();
			double total = kmer.transitions [INTERNAL] + 
					kmer.transitions [PredictionHandler.EXTERNAL];
			Iterator <String> keys = kmer.trans_probs 
					[INTERNAL].keySet().iterator();
			while (keys.hasNext()) {
				String next = keys.next();
				int count = kmer.trans_counts [INTERNAL].get(
						next) + kmer.trans_counts [PredictionHandler.EXTERNAL].get(
						next);
				thetas [i] = count / total;
				ratio += count * Math.log(thetas [i++]);
				for (int j = 0; j < total_mers.length; j++) {
					ratio -= kmer.trans_counts [j].get (next) * kmer.trans_probs 
					[j].get(next);
				}
			}
		}
		ratio *= -2;
		System.out.println ("likelihood value: " + ratio);
	}
	
	public void printKmerLogOdds () {
		try {
			PrintStream out = new PrintStream (new FileOutputStream (
					"c:\\mauvedata\\operon\\kmer_marg.tab"));
			Iterator <Kmer> itty = kmers.values().iterator();
			out.println ("kmer\tlog");
			while (itty.hasNext()) {
				Kmer kmer = itty.next();
				for (int i = 0; i < total_mers.length; i++)
					if (kmer.counts [i] != 0)
						kmer.marg_probs [i] = Math.log((kmer.counts [i] / 
								(double) total_mers [i]));
				out.println (kmer.symbols + "\t" + (kmer.marg_probs [
				        INTERNAL] -
						kmer.marg_probs [EXTERNAL]));
			}

			out.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class Kmer {
		public String symbols;
		int [] counts;
		double [] marg_probs;
		protected Hashtable <String, Integer> [] trans_counts;
		protected Hashtable <String, Double> [] trans_probs;
		int [] transitions;
		
		public Kmer (String symbs) {
			symbols = symbs;
			counts = new int [2];
			trans_probs = new Hashtable [2];
			trans_counts = new Hashtable [2];
			transitions = new int [trans_probs.length];
			marg_probs = new double [trans_probs.length];
			for (int i = 0; i < trans_probs.length; i++) {
				trans_probs [i] = new Hashtable <String, Double> (4);
				trans_counts [i] = new Hashtable <String, Integer> (4);
			}
			int val = 0;
			if (pseudo_counts) {
				counts [0] = counts [1] = 1;
				val = 1;
			}
			String sub = symbols.substring(1);
			for (int i = 0; i < trans_counts.length; i++) {
				trans_counts [i].put(sub + 'a', val);
				trans_counts [i].put(sub + 'c', val);
				trans_counts [i].put(sub + 'g', val);
				trans_counts [i].put(sub + 't', val);
				transitions [i] += val*4;
			}
		}
	}
	
}


