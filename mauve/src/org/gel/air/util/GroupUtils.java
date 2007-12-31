package org.gel.air.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;


/**
 * 
 * @author Anna I Rissman/James Lowden
 *
 */
public class GroupUtils {

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

	public static int findIfNotPresent (int start, int end, Object [] data, Object what) {
		int index = GroupUtils.findPosition (start, end, data, what);
		if (index < end && ((Comparable) data [index]).compareTo (what) != 0)
			return index;
		else	{
			while (index < end && ((Comparable) data [index]).compareTo (what) == 0 &&
					!data [index].equals (what))
				index++;
			if (index < end && data [index].equals (what))
				return -1;
			else
				return index--;
		}
	}//method findIfNotPresent

	public static int linearFind (int start, int end, Object [] data, Object what, boolean sorted) {
		if (sorted) {
			for (int i = start; i < end; i++)
				if (((Comparable) data [i]).compareTo (what) >= 0)
					return i;
		}
		else {
			for (int i = start; i < end; i++)
				if (data [i].equals (what))
					return i;
		}
		return end;
	}//method linearFind

	/**public static int findIndex (int start, int end, Object [] data, 
			Object what) {
		if (what == null)
			return -1;
		int mid = (end + start) / 2;
		int compare = ((Comparable) what).compareTo (data [mid]);
		if (compare < 0)
			if (start == end)
				return mid;
			else
				return findIndex (start, mid, data, what);
		else if (compare > 0)
			if (start == end)
				return mid + 1;
			else
				return findIndex (mid + 1, end, data, what);
		else {
			do {
				mid--;
			} while (mid >= start && ((Comparable)what).compareTo (
					data [mid]) == 0);
			return ++mid;
		}
	}//method findIndex**/
	
	
	public static int findPosition (int start, int end, Object [] data, 
			Object what) {
		if (end - start < 7)
			return linearFind (start, end, data, what, true);
		int mid = (end + start) / 2;
		int compare = ((Comparable) what).compareTo (data [mid]);
		if (compare < 0)
			return findPosition (start, mid, data, what);
		else if (compare > 0)
			return findPosition (mid + 1, end, data, what);
		else {
			do {
				mid--;
			} while (mid >= start && ((Comparable)what).compareTo (
					data [mid]) == 0);
			return ++mid;
		}
	}//method findPosition

}
