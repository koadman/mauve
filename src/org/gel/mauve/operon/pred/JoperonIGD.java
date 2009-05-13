package org.gel.mauve.operon.pred;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;

import org.gel.mauve.operon.pred.PredictionHandler.OperonGene;

public class JoperonIGD extends RegDBIGD {
	
	protected Vector <Run> runs;
	int run_ind;
	
	public JoperonIGD (PredictionHandler handler) {
		super (handler);
		loadRuns ("c:\\joperon\\output\\rankall.0");
	}
	
	public void loadRuns (String file) {
		runs = new Vector <Run> ();
		try {
			BufferedReader in = new BufferedReader (new FileReader (file));
			String first = null;
			boolean use = false;
			while (in.readLine() != null) {
				use = true;
				StringTokenizer toke = new StringTokenizer (in.readLine());
				Run run = new Run ();
				run.operon = Boolean.parseBoolean(toke.nextToken ());
				run.joe_prob = Double.parseDouble(toke.nextToken ());
				toke.nextToken ();
				toke = new StringTokenizer (toke.nextToken(), "-");
				String cur = toke.nextToken();
				if (cur == first) {
					if (!use)
						continue;
					else {
						run.genes.addAll(runs.get(run_ind).genes);
						while (toke.hasMoreTokens())
							cur = toke.nextToken();
					}
				}
				OperonGene gene = 
			}
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
	}

}
