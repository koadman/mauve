package org.gel.mauve;

import java.awt.Color;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

import org.gel.mauve.analysis.Segment;

/**
 * The LCB class tracks locally collinear blocks: regions of homologous sequence
 * that do not contain rearrangements.
 */
public class LCB extends Segment {

	/**
	 *  'Pointers' (actually IDs) to the LCBs on the left in each sequence
	 */
	private int [] left_adjacency;

	/**
	 *  'Pointers' (actually IDs) to the LCBs on the right in each sequence
	 */
	private int [] right_adjacency;

	/**
	 *  A numerical ID that can be assigned to this LCB
	 */
	public int id;

	/**
	 *  The weight (or coverage) of this LCB
	 */
	public long weight;

	/**
	 *  The color of the LCB frame
	 */
	public Color color;

	/**
	 *  The color of matches within the LCB
	 */
	public Color match_color;

	/**
	 *  set this to true to keep this LCB even when it's weight is too low
	 */
	boolean keep;

	public LCB (int seq_count) {
		left = new long [seq_count];
		right = new long [seq_count];
		reverse = new boolean [seq_count];
		left_adjacency = new int [seq_count];
		right_adjacency = new int [seq_count];
	}

	public LCB (Match m, int id, int seq_count) {
		left = new long [seq_count];
		right = new long [seq_count];
		reverse = new boolean [seq_count];
		left_adjacency = new int [seq_count];
		right_adjacency = new int [seq_count];
		this.id = id;

		m.copyArrays (this, left, right, reverse, seq_count);

		// set weight to average lcb length for now...
		long len_sum = 0;
		for (int seqI = 0; seqI < seq_count; seqI++) {
			len_sum += right[seqI] - left[seqI];
		}
		weight = len_sum / seq_count;
		keep = false;
	}

	public LCB (LCB l) {
		int seq_count = l.left.length;
		left = new long [seq_count];
		right = new long [seq_count];
		left_adjacency = new int [seq_count];
		right_adjacency = new int [seq_count];
		reverse = new boolean [seq_count];
		id = l.id;
		weight = l.weight;
		color = l.color;
		match_color = l.match_color;
		keep = l.keep;

		System.arraycopy (l.left, 0, left, 0, seq_count);
		System.arraycopy (l.right, 0, right, 0, seq_count);
		System.arraycopy (l.left_adjacency, 0, left_adjacency, 0, seq_count);
		System.arraycopy (l.right_adjacency, 0, right_adjacency, 0, seq_count);
		System.arraycopy (l.reverse, 0, reverse, 0, seq_count);
	}

	public long midpoint (Genome g) {
		return (right[g.getSourceIndex ()] + left[g.getSourceIndex ()]) / 2;
	}

	public void setReference (Genome g) {
		if (getReverse (g)) {
			for (int seqI = 0; seqI < reverse.length; seqI++) {
				Genome g2 = g.getModel ().getGenomeBySourceIndex (seqI);
				setReverse (g2, !getReverse (g2));
			}
		}
	}

	public long getLength (Genome g) {
		return right[g.getSourceIndex ()] - left[g.getSourceIndex ()];
	}

	public long getLeftEnd (Genome g) {
		return left[g.getSourceIndex ()];
	}

	public void setLeftEnd (Genome g, long leftEnd) {
		left[g.getSourceIndex ()] = leftEnd;
	}

	public long getRightEnd (Genome g) {
		return right[g.getSourceIndex ()];
	}

	public void setRightEnd (Genome g, long rightEnd) {
		right[g.getSourceIndex ()] = rightEnd;
	}

	public boolean getReverse (Genome g) {
		return reverse[g.getSourceIndex ()];
	}

	public void setReverse (Genome g, boolean r) {
		reverse[g.getSourceIndex ()] = r;
	}

	public int getLeftAdjacency (Genome g) {
		return left_adjacency[g.getSourceIndex ()];
	}

	public void setLeftAdjacency (Genome g, int lcbID) {
		left_adjacency[g.getSourceIndex ()] = lcbID;
	}

	public int getRightAdjacency (Genome g) {
		return right_adjacency[g.getSourceIndex ()];
	}

	public void setRightAdjacency (Genome g, int lcbID) {
		right_adjacency[g.getSourceIndex ()] = lcbID;
	}

	public void resetAdjacencies (int genomeCount) {
		Arrays.fill (left_adjacency, 0);
		Arrays.fill (right_adjacency, 0);
	}

	public int multiplicity () {
		int mult = 0;
		for (int i = 0; i < left.length; ++i) {
			if (left[i] != 0)
				mult++;
		}
		return mult;
	}
	

	
}
