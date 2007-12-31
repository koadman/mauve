package org.gel.mauve;

import java.util.Comparator;

/**
 * Compares LCB ids.
 */
public class LcbIdComparator implements Comparator {
	public int compare (Object o_a, Object o_b) {

		LCB a = (LCB) o_a;
		LCB b = (LCB) o_b;
		return a.id - b.id;
	}
}