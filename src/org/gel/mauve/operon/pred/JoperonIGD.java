package org.gel.mauve.operon.pred;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;

import org.biojava.bio.seq.StrandedFeature;
import org.gel.mauve.operon.pred.PredictionHandler.IGD;
import org.gel.mauve.operon.pred.PredictionHandler.OperonGene;

public class JoperonIGD  {
	
	protected PredictionHandler handler;
	protected Vector <Run> runs;
	int run_ind;
	protected Hashtable <String, IGD> firsts;
	protected Hashtable <String, IGD> lasts;
	
	
	public JoperonIGD (PredictionHandler handler) {
		this.handler = handler;;
		mapIGDs (handler.igds);
		loadRuns ("c:\\joperon\\output\\rankall.0");
		getRunProbs ();
		summarize ();
	}
	
	/*public void getRunProbs () {
		OperonGene begin = null;
		for (int i = 0; i < runs.size (); i++) {
			Run run = runs.get (i);
			System.out.println ("run: " + run);
			double prob = 1;
			IGD igd = null;
			if (begin != null && run.genes.getFirst().name.equals(begin.name)) {
				prob = runs.get(i - 1).prob;
				igd = lasts.get(run.genes.getLast().name);
				if (igd != null) {
					if (igd.ratio == 0)
						handler.bk.classifyIGD(igd);
					prob *= igd.intProb () / (1-igd.intProb ());
					System.out.println ("igd: " + run.genes.getLast().name);
				}
			}
			else {
				igd = lasts.get(run.genes.getFirst().name);
				if (igd != null) {
					if (igd.ratio == 0)
						handler.bk.classifyIGD(igd);
					prob *= (1-igd.intProb ());
					System.out.println ("first: " + igd.gene2.name);
				}
			}
			System.out.println ("prob: " + prob);
			igd = firsts.get(run.genes.getLast().name);
			if (igd != null)  {
				if (igd.ratio == 0)
					handler.bk.classifyIGD(igd);
				prob *= (1 - igd.intProb ());
			}
			System.out.println ("prob2: " + prob);
			run.prob = prob;
			begin = run.genes.getFirst();
		}
	}*/
	
	public void getRunProbs () {
		for (int i = 0; i < runs.size (); i++) {
			Run run = runs.get (i);
			System.out.println ("run: " + run);
			double prob = 1;
			IGD igd = null;
			Iterator <OperonGene> itty = run.genes.iterator();
			OperonGene gene = itty.next();
			igd = lasts.get(gene.name);
			if (igd != null) {
				if (igd.ratio == 0)
					handler.bk.classifyIGD(igd);
				prob *= (1-igd.intProb ());
				System.out.println ("first: " + igd.gene2.name);
			}
			while (itty.hasNext()) {
				gene = itty.next();
				igd = lasts.get(gene.name);
				if (igd != null) {
					if (igd.ratio == 0)
						handler.bk.classifyIGD(igd);
					prob *= igd.intProb ();
					System.out.println ("igd: " + gene.name);
				}
			}
			System.out.println ("prob: " + prob);
			igd = firsts.get(gene.name);
			if (igd != null)  {
				if (igd.ratio == 0)
					handler.bk.classifyIGD(igd);
				prob *= (1 - igd.intProb ());
			}
			System.out.println ("prob2: " + prob);
			run.prob = prob;
		}
	}
	
	public void summarize () {
		try {
			PrintStream out = new PrintStream (new FileOutputStream (
					"c:\\mauvedata\\operon\\regdb\\op_preds.tab"));
			out.println ("real\tprob");
			Iterator <Run> itty = runs.iterator();
			while (itty.hasNext()) {
				Run run = itty.next ();
				out.println (run.operon + "\t" + run.prob + "\t" + run);
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void mapIGDs (Collection igds) {
		firsts = new Hashtable <String, IGD> (igds.size ());
		lasts = new Hashtable <String, IGD> (igds.size ());
		Iterator <IGD> itty = igds.iterator();
		while (itty.hasNext()) {
			IGD igd = itty.next();
			String name = igd.gene1.name;
			if (name != null)
				firsts.put(name, igd);
			name = igd.gene2.name;
			if (name != null)
			lasts.put(name, igd);
		}
	}
	
	/*public void loadRuns (String file) {
		runs = new Vector <Run> ();
		try {
			BufferedReader in = new BufferedReader (new FileReader (file));
			String first = null;
			boolean use = true;
			String input = in.readLine();
			input = in.readLine();
			while (input != null) {
				StringTokenizer toke = new StringTokenizer (input);
				Run run = new Run ();
				run.operon = Boolean.parseBoolean(toke.nextToken ());
				run.joe_prob = Double.parseDouble(toke.nextToken ());
				toke.nextToken ();
				toke = new StringTokenizer (toke.nextToken(), "-");
				String cur = toke.nextToken();
				if (first != null && cur.equals(first)) {
					if (!use) {
						input = in.readLine();
						continue;
					}
					else {
						run.genes.addAll(runs.get(run_ind - 1).genes);
						while (toke.hasMoreTokens())
							cur = toke.nextToken();
					}
				}
				else
					use = true;
				StrandedFeature feat = handler.bnums.get(cur);
				if (feat == null)
					use = false;
				else {
					OperonGene gene = firsts.get(cur) == null ? lasts.get(cur).gene2 :
						firsts.get(cur).gene1;
					if (gene.isReversed()) {
						run.genes.addFirst(gene);
						first = null;
					}
					else {
						run.genes.addLast(gene);
						first = run.genes.getFirst().name;
					}
					runs.add(run);
					run_ind++;
				}
				input = in.readLine();
			}
			in.close ();
			System.out.println ("runs: " + runs.size ());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	public void loadRuns (String file) {
		runs = new Vector <Run> ();
		try {
			BufferedReader in = new BufferedReader (new FileReader (file));
			boolean use = true;
			String input = in.readLine();
			input = in.readLine();
			outer : while (input != null) {
				StringTokenizer toke = new StringTokenizer (input);
				Run run = new Run ();
				run.operon = Boolean.parseBoolean(toke.nextToken ());
				run.joe_prob = Double.parseDouble(toke.nextToken ());
				toke.nextToken ();
				toke = new StringTokenizer (toke.nextToken(), "-");
				while (toke.hasMoreTokens()) {
					String cur = toke.nextToken();
				 	StrandedFeature feat = handler.bnums.get(cur);
					if (feat == null) {
						input = in.readLine();
						continue outer;
					}
					else {
						OperonGene gene = firsts.get(cur) == null ? 
								lasts.get(cur).gene2 : firsts.get(cur).gene1;
						if (gene.isReversed())
							run.genes.addFirst(gene);
						else
							run.genes.addLast(gene);
					}
				}
				runs.add(run);
				run_ind++;
				input = in.readLine();
			}
			in.close ();
			System.out.println ("runs: " + runs.size ());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class Run {
		
		double prob;
		double joe_prob;
		boolean operon;
		int index;
		LinkedList <OperonGene> genes;
		
		public Run () {
			genes = new LinkedList <OperonGene> ();
			index = run_ind;
		}
		
		public String toString () {
			Iterator <OperonGene> itty = genes.iterator();
			StringBuffer buffy = new StringBuffer ();
			while (itty.hasNext())
				buffy.append(itty.next().name + "-");
			return buffy.substring(0, buffy.length() - 1);
		}
		
	}

}
