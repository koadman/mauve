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

	// 'Pointers' (actually IDs) to the LCBs on the left in each sequence
	private int [] left_adjacency;

	// 'Pointers' (actually IDs) to the LCBs on the right in each sequence
	private int [] right_adjacency;

	// A numerical ID that can be assigned to this LCB
	public int id;

	// The weight (or coverage) of this LCB
	public long weight;

	// The color of the LCB frame
	public Color color;

	// The color of matches within the LCB
	public Color match_color;

	// set this to true to keep this LCB even when it's weight is too low
	boolean keep;

	public LCB (int seq_count) {
		starts = new long [seq_count];
		ends = new long [seq_count];
		reverse = new boolean [seq_count];
		left_adjacency = new int [seq_count];
		right_adjacency = new int [seq_count];
	}

	public LCB (Match m, int id, int seq_count) {
		starts = new long [seq_count];
		ends = new long [seq_count];
		reverse = new boolean [seq_count];
		left_adjacency = new int [seq_count];
		right_adjacency = new int [seq_count];
		this.id = id;

		m.copyArrays (this, starts, ends, reverse, seq_count);

		// set weight to average lcb length for now...
		calculateWeight ();
		keep = false;
	}

	public LCB (LCB l) {
		int seq_count = l.starts.length;
		starts = new long [seq_count];
		ends = new long [seq_count];
		left_adjacency = new int [seq_count];
		right_adjacency = new int [seq_count];
		reverse = new boolean [seq_count];
		id = l.id;
		weight = l.weight;
		color = l.color;
		match_color = l.match_color;
		keep = l.keep;

		System.arraycopy (l.starts, 0, starts, 0, seq_count);
		System.arraycopy (l.ends, 0, ends, 0, seq_count);
		System.arraycopy (l.left_adjacency, 0, left_adjacency, 0, seq_count);
		System.arraycopy (l.right_adjacency, 0, right_adjacency, 0, seq_count);
		System.arraycopy (l.reverse, 0, reverse, 0, seq_count);
	}
	
	public void calculateWeight () {
		long len_sum = 0;
		for (int seqI = 0; seqI < starts.length; seqI++) {
			len_sum += ends[seqI] - starts[seqI];
		}
		weight = len_sum / starts.length;
	}

	public long midpoint (Genome g) {
		return (ends[g.getSourceIndex ()] + starts[g.getSourceIndex ()]) / 2;
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
		return ends[g.getSourceIndex ()] - starts[g.getSourceIndex ()];
	}

	public long getLeftEnd (Genome g) {
		return starts[g.getSourceIndex ()];
	}

	public void setLeftEnd (Genome g, long leftEnd) {
		starts[g.getSourceIndex ()] = leftEnd;
	}

	public long getRightEnd (Genome g) {
		return ends[g.getSourceIndex ()];
	}

	public void setRightEnd (Genome g, long rightEnd) {
		ends[g.getSourceIndex ()] = rightEnd;
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
		for (int i = 0; i < starts.length; ++i) {
			if (starts[i] != 0)
				mult++;
		}
		return mult;
	}
}
