package org.gel.air.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public class GroupHelpers {

	public static void arrayToCollection (Collection group, Object [] data) {
		for (int i = 0; i < data.length; i++)
			group.add (data[i]);
	}
	
	public static boolean collectionsEqual (Collection a, Collection b, 
			Comparator comp) {
		if (a.size() == b.size()) {
			Iterator one = a.iterator();
			Iterator two = b.iterator();
			while (one.hasNext()) {
				if (comp.compare(one.next(), two.next ()) != 0)
					return false;
			}
			return true;
		}
		else
			return false;
	}

}
