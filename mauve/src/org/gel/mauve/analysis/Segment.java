package org.gel.mauve.analysis;

import java.io.Serializable;

import org.gel.mauve.Match;

public class Segment implements Serializable {
	static final long serialVersionUID = 1;
	
	// The start coordinate of this match in each sequence
	public long [] starts;

	/**
	 * only one of the next two variables is necessary for the
	 * class; and only one should be set.  By default, it is ends.
	 */
	// The ends of this match in each sequence
	public long [] ends;
	
	//Alternately, lengths could be set.
	public long [] lengths;

	// The direction of each match. false is forward, true is reverse
	public boolean [] reverse;

	public Segment [] prevs;

	public Segment [] nexts;
	
	public String typed_id;

	public static final Segment END = new Segment ();

	public static final String MULTIPLICITY_STRING = "multiplicity";

	// represents which sequences this segment is part of
	protected long mult_type;

	public Segment (long [] l, long [] r, boolean [] c, boolean end) {
		starts = l;
		setEndOrLength (r, end);
		reverse = c;
	}

	public Segment (int count, boolean links, boolean end) {
		starts = new long [count];
		setEndOrLength (new long [count], end);
		reverse = new boolean [count];
		if (links) {
			prevs = new Segment [count];
			nexts = new Segment [count];
		}
	}

	public Segment (int count, boolean end) {
		this (count, false, end);
	}
	
	protected Segment () {
	}
	
	protected void setEndOrLength (long [] data, boolean end) {
		if (end)
			ends = data;
		else
			lengths = data;
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
			for (int seqI = 0; seqI < starts.length; seqI++) {
				mult_type <<= 1;
				if (starts[seqI] != Match.NO_MATCH)
					mult_type |= 1;
			}
		}
		return mult_type;
	}

	public void append (Segment add, boolean remove) {
		for (int i = 0; i < starts.length; i++) {
			if (prevs[i] == add)
				starts[i] = add.starts[i];
			else if (nexts[i] == add)
				ends[i] = add.ends[i];
			else if (starts[i] != Match.NO_MATCH) {
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
		for (int seqI = 0; seqI < starts.length; seqI++) {
			rval += "<";
			if (starts[seqI] != Match.NO_MATCH) {
				if (reverse != null && reverse[seqI])
					rval += "-";
				rval += starts[seqI];
				rval += ",";
				rval += ends[seqI];
			}
			rval += "> ";
		}
		return rval;
	}

	public long [] getSegmentLengths () {
		long [] sizes = new long [starts.length];
		for (int i = 0; i < sizes.length; i++)
			sizes[i] = getSegmentLength (i);
		return sizes;
	}

	public long getSegmentLength (int sequence) {
		if (starts[sequence] == 0)
			return 0;
		else if (lengths == null)
			return ends[sequence] - starts[sequence] + 1;
		else
			return lengths [sequence];
	}
	
	public long getSegmentEnd (int sequence) {
		if (ends == null)
			return lengths [sequence] + starts [sequence] - 1;
		else
			return ends [sequence];
	}

	public double getAvgSegmentLength () {
		long [] sizes = getSegmentLengths ();
		long total = 0;
		int present_in = 0;
		for (int i = 0; i < starts.length; i++) {
			if (sizes[i] != 0) {
				total += sizes[i];
				present_in++;
			}
		}
		return ((double) total) / present_in;
	}

}
