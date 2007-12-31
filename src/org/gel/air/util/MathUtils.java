package org.gel.air.util;

public class MathUtils {
	
	public static double percentContained (long in_left, long in_right, 
			long out_left, long out_right) {
		long length = in_right - in_left;
		long hang = 0;
		if (in_left < out_left)
			hang = out_left - in_left;
		if (in_right > out_right)
			hang += in_right - out_right;
		return ((double) length - hang) / length * 100;
	}
	
	public static int compareByStartThenLength (int start1, int end1, int start2,
			int end2) {
		/*rval = a.starts[i] - b.starts[i];
		if (rval < 0)
			ret = -1;
		else if (rval > 0)
			ret = 1;
		else if (i > BY_MULTIPLICITY){
			rval = a.lengths [i] - b.lengths [i];
			ret = rval > 0 ? 1 : (rval == 0) ? 0 : -1;
		}*/
		int ret = start1 - start2;
		if (ret == 0)
			ret = end1 - end2;
		return ret;
	}
	

}
