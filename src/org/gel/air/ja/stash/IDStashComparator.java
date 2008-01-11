package org.gel.air.ja.stash;

import java.util.Comparator;

public class IDStashComparator implements Comparator <Stash>, StashConstants {

	protected boolean is_number;
	
	public IDStashComparator (boolean num) {
		is_number = num;
	}
	
	public int compare(Stash o1, Stash o2) {
		String val1 = o1.getString(ID);
		String val2 = o2.getString(ID);
		if (is_number) {
			int ind = val1.indexOf(KEY_SEPARATOR);
			if (ind > -1)
				val1 = val1.substring (ind + 1);
			ind = val2.indexOf(KEY_SEPARATOR);
			if (ind > -1)
				val2 = val2.substring (ind + 1);
			return (int) (2 * Math.signum (Double.parseDouble (val1)
					- Double.parseDouble (val2)));
		}
		else
			return val1.compareTo (val2);
	}

}
