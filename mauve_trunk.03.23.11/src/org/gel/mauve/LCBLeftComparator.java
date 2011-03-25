package org.gel.mauve;

import java.util.Comparator;

/**
 * Compares left end of LCBs.
 */
public class LCBLeftComparator implements Comparator<LCB> {
	private Genome g;

	public LCBLeftComparator (Genome g) {
		this.g = g;
	}

	public int compare (LCB o_a, LCB o_b) {

		LCB a = (LCB) o_a;
		LCB b = (LCB) o_b;

		long a_start = a.getLeftEnd (g);
		long b_start = b.getLeftEnd (g);
		if (a_start == 0 || b_start == 0) {
			if (b_start != 0)
				return 1;
			return -1;
		}

		long diff = a_start - b_start;
		return (int) diff;
	}

}