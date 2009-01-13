package org.gel.mauve;

import java.util.Comparator;

import org.gel.mauve.analysis.SegmentComparator;

/**
 * Compare the start coordinates of ungapped local alignments in a particular
 * sequence Note: this comparator imposes orderings that are inconsistent with
 * the equals operator.
 */

public class MatchStartComparator extends SegmentComparator implements
		Comparator <Object> {
	private Genome g;

	public MatchStartComparator (Genome g) {
		super (g.getSourceIndex ());
		this.g = g;
	}

	public MatchStartComparator (MatchStartComparator m) {
		super (m.g.getSourceIndex ());
		g = m.g;
	}

	public int compare (Object o1, Object o2) {
		return super.compare (o1, o2);
	}

	public boolean equals (Object c) {
		if (c == null)
			return false;

		if (c instanceof MatchStartComparator) {
			return g == ((MatchStartComparator) c).g;
		} else {
			return false;
		}
	}
}
