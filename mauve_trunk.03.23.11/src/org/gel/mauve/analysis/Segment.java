package org.gel.mauve.analysis;

import java.io.Serializable;

import java.util.Comparator;

import org.gel.mauve.Match;

public class Segment implements Serializable {
	static final long serialVersionUID = 1;
	
	/**
	 *  The start coordinate of this match in each sequence
	 */
	public long [] left;

	/**
	 *  The ends of this match in each sequence
	 */
	public long [] right;
	
	/**
	 * The direction of each match. false is forward, true is reverse
	 */
	public boolean [] reverse;

	public Segment [] prevs;

	public Segment [] nexts;
	
	public String typed_id;

	public static final Segment END = new Segment (0, false);

	public static final String MULTIPLICITY_STRING = "multiplicity";

	// represents which sequences this segment is part of
	protected long mult_type;

	public Segment (long [] l, long [] r, boolean [] c) {
		left = l;
		right = r;
		reverse = c;
	}

	public Segment (int count, boolean links) {
		left = new long [count];
		right = new long [count];
		reverse = new boolean [count];
		if (links) {
			prevs = new Segment [count];
			nexts = new Segment [count];
		}
	}

	public Segment (int count) {
		this (count, false);
	}
	
	public Segment () {
	}

	/**
	 * compute a number indicating which genomes the local alignment is defined
	 * in. This is a binary number where a 1 indicates that the match is defined
	 * and a 0 indicates it is undefined. For example, a local alignment shared
	 * by the first and the fourth (out of five) genomes would be represented as
	 * 10010.
	 */
	public long multiplicityType () {
		if (mult_type == 0) {
			// determine the match's multiplicity type.
			for (int seqI = 0; seqI < left.length; seqI++) {
				mult_type <<= 1;
				if (left[seqI] != Match.NO_MATCH)
					mult_type |= 1;
			}
		}
		return mult_type;
	}

	public void append (Segment add, boolean remove) {
		for (int i = 0; i < left.length; i++) {
			if (prevs[i] == add)
				left[i] = add.left[i];
			else if (nexts[i] == add)
				right[i] = add.right[i];
			else if (left[i] != Match.NO_MATCH) {
				System.out.println ("Tried to add in inconsistent order: "
						+ add + " " + this);
			}
			if (remove)
				add.remove (i);
		}
	}

	public void remove (int seq_index) {
		if (prevs[seq_index] != null)
			prevs[seq_index].nexts[seq_index] = nexts[seq_index];
		nexts[seq_index].prevs[seq_index] = prevs[seq_index];
	}

	/** format the ungapped local alignment coordinates into a string */
	public String toString () {
		String rval = new String ();
		for (int seqI = 0; seqI < left.length; seqI++) {
			rval += "<";
			if (left[seqI] != Match.NO_MATCH) {
				if (reverse != null && reverse[seqI])
					rval += "-";
				rval += left[seqI];
				rval += ",";
				rval += right[seqI];
			}
			rval += "> ";
		}
		return rval;
	}

	public long [] getSegmentLengths () {
		long [] sizes = new long [left.length];
		for (int i = 0; i < sizes.length; i++)
			sizes[i] = getSegmentLength (i);
		return sizes;
	}

	public long getSegmentLength (int sequence) {
		if (left[sequence] == 0)
			return 0;
		else
			return right[sequence] - left[sequence] + 1;
	}

	public double getAvgSegmentLength () {
		long [] sizes = getSegmentLengths ();
		long total = 0;
		int present_in = 0;
		for (int i = 0; i < left.length; i++) {
			if (sizes[i] != 0) {
				total += sizes[i];
				present_in++;
			}
		}
		return ((double) total) / present_in;
	}
	
	public static Comparator<Segment> getGenPositionComparator(int genSrcIdx){
		return new GenomePositionComparator(genSrcIdx);
	}
	
	private static class GenomePositionComparator implements Comparator<Segment> {
		private int src;
		
		public GenomePositionComparator(int genSrcIdx){
			src = genSrcIdx;
		}
		
		public int compare(Segment a, Segment b){
			long leftA = Math.abs(a.reverse[src]? a.right[src]:a.left[src]);
			long leftB = Math.abs(b.reverse[src]? b.right[src]:b.left[src]);
			return (int) (leftA - leftB);
		}
	}
	
	


}

