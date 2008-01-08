package org.gel.air.ja.stash;

import java.util.Comparator;

public class StashComparator implements Comparator <Stash> {

	protected String field;
	protected boolean is_number;
	
	public StashComparator (String f, boolean num) {
		field = f;
		is_number = num;
	}
	
	public int compare(Stash o1, Stash o2) {
		String val1 = o1.getString(field);
		String val2 = o2.getString(field);
		if (is_number) {
			return (int) (2 * Math.signum (Double.parseDouble (val1)
					- Double.parseDouble (val2)));
		}
		else
			return val1.compareTo (val2);
	}

}
