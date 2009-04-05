package org.gel.mauve.analysis;

import java.util.Hashtable;
import java.util.Iterator;

import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.operon.Operon;
import org.gel.mauve.operon.OperonHandler;

public class PhyloMultiplicity {
	
	public static final int NOT_ALIGNED = 0;
	public static final int REARRANGEMENT = 1;
	
	protected Hashtable <Segment, Double> mult_percents;
	protected long best_mult;
	protected Hashtable <Long, Segment> end_segs;
	protected double last_percent;
	protected int last_description;
	
	public PhyloMultiplicity (Hashtable <Segment, Double> percents,
			long best, Hashtable <Long, Segment> ends) {
		mult_percents = percents;
		best_mult = best;
		end_segs = ends;
	}
	
	public Hashtable <Segment, Double> getMultPercents () {
		return mult_percents;
	}
	
	public long getBestMult () {
		return best_mult;
	}
	
	public int getLastMismatch () {
		return last_description; 
	}
	
	public double getLastPercent () {
		return last_percent;
	}
	
	
	//change so considers subsets; start with all in set, remove smallest 1, etc.
	
	public long [] inMultiplicity (long start, long end, XmfaViewerModel model,  
			int seq, int ref, double min_prct) {
		Iterator <Segment> itty = mult_percents.keySet().iterator();
		long mult = MauveHelperFunctions.multiplicityForGenome(seq, 
				mult_percents.keys().nextElement().starts.length);
		double prct = 0.0;
		Segment first = null;
		Segment last = null;
		while (itty.hasNext()) {
			Segment seg = itty.next();
			if ((seg.multiplicityType() & mult) == mult) {
				prct += mult_percents.get(seg);
				if (first == null || seg.getStart(ref) < first.getStart(ref))
					first = seg;
				if (end_segs.containsKey(seg.multiplicityType()))
					seg = end_segs.get(seg.multiplicityType());
				if (last == null || seg.getEnd(ref) > last.getEnd(ref))
					last = seg;
			}
		}	
		if (prct > min_prct) {
			long r_start = Math.max(first.getStart(ref), start);
			long r_end = Math.min(last.getEnd(ref), end);
			boolean reverse = first.reverse [seq] != first.reverse [ref];
			long s_start = model.getAlignCoords (
					model.getGenomeBySourceIndex(
					ref), reverse ? r_end : r_start) [seq];
			long s_end = model.getAlignCoords (
					model.getGenomeBySourceIndex(
					ref), reverse ? r_start : r_end) [seq];
			long span = s_end - s_start;
			prct = span / ((double) end - start);
			prct *= 100;
			if (prct > min_prct && prct < (200 - min_prct))
				return new long [] {s_start, s_end};
			else
				last_description = REARRANGEMENT;
		}
		else {
			last_description = NOT_ALIGNED;
		}
		last_percent = prct;
		return null;
	}

}
