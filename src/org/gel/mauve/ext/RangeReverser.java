package org.gel.mauve.ext;

import java.util.HashSet;
import java.util.Iterator;

import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.symbol.RangeLocation;
import org.gel.mauve.Genome;

//for now, only contains one continuous range
public class RangeReverser {
	
	protected RangeLocation reversed;
	protected HashSet cut_features;
	protected Genome genome;
	
	public RangeReverser (Genome gen) {
		genome = gen;
		reversed = new RangeLocation (0, 0);
		cut_features = new HashSet ();
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
