package org.gel.mauve.operon.pred;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import org.gel.air.R.RUtils;
import org.gel.air.bioj.BioJavaConstants;
import org.gel.mauve.operon.pred.PredictionHandler.IGD;

public class GC implements BioJavaConstants {

	long [][] counts;
	long [] total_bps;
	LinkedHashSet <BPBreakdown> [] gcs;

	public GC(HashSet <IGD> [] data) {
		counts = new long [2][4];
		total_bps = new long [2];
		train (data);
		plotLengthVsPrct ();
	}
	
	public void train (HashSet <IGD> [] igds) {
		gcs = new LinkedHashSet [2];
		double [][] tot_prcts = new double [2][4];
		for (int i = 0; i < igds.length; i++) {
			gcs [i] = new LinkedHashSet <BPBreakdown> (igds [i].size());
			Iterator <IGD> itty = igds [i].iterator ();
			while (itty.hasNext()) {
				BPBreakdown bd = new BPBreakdown (itty.next(), 5);
				for (int j = 0; j < counts [i].length; j++)
					counts [i][j] += bd.counts [j];
					//tot_prcts [i][j] += bd.prcts [j];
				gcs [i].add(bd);
			}
			for (int j = 0; j < counts [i].length; j++)
				total_bps [i] += counts [i][j];
			System.out.println ((i == PredictionHandler.INTERNAL ? 
					"INTERNALS" : "EXTERNALS") + " length: " + total_bps [i]);
			for (int j = 0; j < counts [i].length; j++)
				System.out.println (BPS [j] + ": " + (counts [i][j] * 100.0 / 
						total_bps [i]));
			/*System.out.println (BPS [j] + ": " + (tot_prcts [i][j] / 
					80));*/
		}
	}
	
	public void plotLengthVsPrct () {
		for (int i = 0; i < 2; i++) {
			String prefix = PredictionHandler.TERNAL_STRINGS [i];
			System.out.println (RUtils.toRConcat(prefix
					 + "_p", gcs [i]));
			System.out.println (RUtils.toRConcat(
					prefix + "_l", getLengths (i)));
			System.out.println ("xyplot(" + prefix + "_p~" + prefix + "_l)");
		}
	}
	
	public LinkedList getLengths (int which) {
		LinkedList <Long> list = new LinkedList <Long> ();
		Iterator <BPBreakdown> itty = gcs [which].iterator();
		while (itty.hasNext())
			list.add(itty.next().igd.getLength());
		return list;
	}
	
	public class BPBreakdown  {
		IGD igd;
		long [] counts;
		double [] prcts;
		
		public BPBreakdown (IGD dist) {
			igd = dist;
			counts = new long [4];
			prcts = new double [4];
			String bps = igd.dna;
			for (int i = 0; i < bps.length(); i++) {
				counts [BP_TO_IND.get(bps.charAt(i))]++;
			}
			for (int i = 0; i < prcts.length; i++)
				prcts [i] = counts [i] * 100.0 / igd.getLength();
		}
		
		public BPBreakdown (IGD dist, int length) {
			igd = dist;
			counts = new long [4];
			prcts = new double [4];
			String bps = igd.dna;
			for (int i = 0; i < length; i++) {
				counts [BP_TO_IND.get(bps.charAt(i))]++;
			}
			for (int i = bps.length() - 1; i > bps.length() - (length + 1); i--) {
				counts [BP_TO_IND.get(bps.charAt(i))]++;
			}
			for (int i = 0; i < prcts.length; i++)
				prcts [i] = counts [i] * 100.0 / (length * 2);
		}

		public String toString () {
			return "" + (prcts [G] + prcts [C]);
		}
	}

}
