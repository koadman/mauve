package org.gel.mauve.analysis;

import java.util.Hashtable;
import java.util.Iterator;

import org.gel.mauve.MauveHelperFunctions;

public class PhyloMultiplicity {
	
	protected Hashtable <Segment, Double> mult_percents;
	protected long best_mult;
	protected Hashtable <Long, Segment> end_segs;
	
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
	
	public boolean inMultiplicity (long length, int seq, int ref, 
			double min_prct) {
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
				if (last == null || seg.getEnd(ref) > last.getEnd(ref))
					last = seg;
			}
		}
		//doing comparison based on ends of segment, not operon
		//ok if bb segments are close to the same length, which I don't think
		//is necessarily true.
		if (prct > min_prct) {
			long span = 0;
			if (first.reverse [seq] == first.reverse [ref])
				span = last.getEnd(seq) - first.getStart(seq);
			else
				span = first.getEnd(seq) - last.getStart(seq);
			prct = span / ((double) last.getEnd(ref) - first.getStart(ref));
			if (prct > min_prct && prct < (200 - min_prct))
				return true;
		}

		return false;
	}

}
