package org.gel.mauve.analysis;

import java.util.Comparator;

public class SegmentComparator implements Comparator {

	public int index;

	public boolean multiple;

	public static final int BY_MULTIPLICITY = -1;
	
	public SegmentComparator (int index, boolean mult) {
		this.index = index;
		multiple = mult;
	}

	public SegmentComparator (int index) {
		this (index, false);
	}

	public int compare (Object o1, Object o2) {
		if (o1 == null)
			return -1;
		else if (o2 == null)
			return 1;
		Segment seg = null;
		long one;
		long two;
		int ret = 0;
		int i = index;
		do {
			long rval = 0;
			if (i == BY_MULTIPLICITY) {
				rval = ((Segment) o1).multiplicityType () - ((Segment) o2).multiplicityType ();
				if (multiple)
					i++;
			}
			if (rval == 0 && i > BY_MULTIPLICITY) {
				if (o1 instanceof Segment) {
					seg = (Segment) o1;
					one = seg.starts [i];
				}
				else
					one = ((Long) o1).longValue ();
				if (o2 instanceof Segment) {
					seg = (Segment) o2;
					two = seg.starts [i];
				}
				else
					two = ((Long) o2).longValue ();
				rval = one - two;
			}
			if (rval < 0)
				ret = -1;
			else
				ret = rval == 0 ? 0 : 1;
			i++;
			if (seg != null && i == seg.starts.length) {
				i = index == BY_MULTIPLICITY ? BY_MULTIPLICITY : 0;
			}
		} while (multiple && ret == 0 && i != index);
		return ret;
	}

}
