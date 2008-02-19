package org.gel.mauve.ext;

import java.util.HashSet;
import java.util.Iterator;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.LocationTools;
import org.biojava.bio.symbol.RangeLocation;
import org.gel.mauve.Genome;

//for now, only contains one continuous range
public class RangeReverser {
	
	protected RangeLocation reversed;
	protected HashSet cut_features;
	protected Genome genome;
	
	public RangeReverser (Genome gen) {
		if (gen.getSourceIndex() > 0) {
			genome = gen;
			reversed = new RangeLocation (1, (int) gen.getLength());
			cut_features = new HashSet ();
		}
	}
	
	/**
	 * Reverses the subsection of sequence, from start to end inclusive
	 * @param start
	 * @param end
	 */
	public synchronized void reverseRange (long start, long end) {
		if (reversed != null)
			unreverse ();
		reversed = new RangeLocation ((int) start, (int) end);
		Iterator itty = genome.getAnnotationSequence().filter(new FeatureFilter.
				ContainedByLocation (reversed)).features();
		while (itty.hasNext()) {
			Location loci = ((Feature) itty.next()).getLocation();
			if (loci.getMin() < start && !(loci.getMax() > end))
				start = loci.getMin();
			else if (loci.getMax() > end && !(loci.getMin() < start))
				end = loci.getMax();
		}
		reversed = new RangeLocation ((int) start, (int) end);
	}
	
	public FeatureFilter fixRange (FeatureFilter filt) {
		if (reversed != null) {
			if (filt instanceof FeatureFilter.ContainedByLocation) {
				Location filt_loc = ((
						FeatureFilter.ContainedByLocation)
						filt).getLocation ();
				filt = new FeatureFilter.ContainedByLocation (
						inverseNecessary (filt_loc));
			}
			else if (filt instanceof FeatureFilter.OverlapsLocation) {
				Location filt_loc = ((
						FeatureFilter.OverlapsLocation)
						filt).getLocation ();
				filt = new FeatureFilter.OverlapsLocation (
						inverseNecessary (filt_loc));
			}
			else if (filt instanceof FeatureFilter.ShadowContainedByLocation) {
				Location filt_loc = ((
						FeatureFilter.ShadowContainedByLocation)
						filt).getLocation ();
				filt = new FeatureFilter.ShadowContainedByLocation (
						inverseNecessary (filt_loc));
			}
			else if (filt instanceof FeatureFilter.ShadowOverlapsLocation) {
				Location filt_loc = ((
						FeatureFilter.ShadowOverlapsLocation)
						filt).getLocation ();
				filt = new FeatureFilter.ShadowOverlapsLocation (
						inverseNecessary (filt_loc));
			}
			else if (filt instanceof FeatureFilter.Not) {
				filt = new FeatureFilter.Not (fixRange ((
						(FeatureFilter.Not) filt).getChild ()));
			}
			else if (filt instanceof FeatureFilter.And) {
				filt = new FeatureFilter.And (fixRange ((
						(FeatureFilter.And) filt).getChild1 ()), fixRange (
						((FeatureFilter.And) filt).getChild2 ()));
			}
			else if (filt instanceof FeatureFilter.Or) {
				filt = new FeatureFilter.Or (fixRange ((
						(FeatureFilter.Or) filt).getChild1 ()), fixRange (
						((FeatureFilter.Or) filt).getChild2 ()));
			}
		}
		return filt;
	}
	
	public void fixFeatures (FeatureHolder holder) {
		Iterator itty = holder.features ();
		while (itty.hasNext()) {
			Feature feat = (Feature) itty.next();
			feat.setLocation(inverseNecessary (feat.getLocation()));
		}
	}
	
	protected Location inverseNecessary (Location filt_loc) {
		Location loci = LocationTools.intersection (filt_loc, reversed);
		filt_loc = LocationTools.subtract (filt_loc, loci);
		loci = new RangeLocation (reversed.getMax () - (loci.getMin () - reversed.getMin ()),
				reversed.getMin () + (reversed.getMin () - loci.getMax ()));
		filt_loc = LocationTools.union(filt_loc, loci);	
		return filt_loc;
	}
	
	//just resets values; convenience method for using when part of a series of changes
	private void unreverse () {
	}
	
	public RangeLocation getReversed () {
		return reversed;
	}
	
	public synchronized void resetReversed () {
		unreverse ();
		//put repaint code here
	}

}
