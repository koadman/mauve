package org.gel.mauve;

import java.io.Serializable;

/**
 * Meant to represent data that varies over a range and needs to be viewed at
 * multiple resolutions.
 * 
 * @author Aaron Darling
 *
 */
public class ZoomHistogram implements Serializable {
	
	static final long serialVersionUID = 1;
	
	/** < Never index fewer than this many alignment columns */
	protected int max_resolution = 5;

	/** < The number of characters covered by an index entry at each level */
	protected long [] resolutions;

	/** < The number of index entries at each level */
	protected long [] level_sizes;

	/** < The number of resolution levels */
	protected int levels;
	
	/**
	 * whether the entire histogram is loaded
	 */
	protected Handler handler;

	
	protected ZoomHistogram () {
	}
	
	public ZoomHistogram (int level, int max_res, long [] sizes, long [] res, Handler load) {
		handler = load;
		init (level, max_res, sizes, res);
	}
	
	protected void init (int levels, int max_res, long [] sizes, long [] res) {
		this.levels = levels;
		max_resolution = max_res;
		level_sizes = sizes;
		resolutions = res;
	}
	/**
	 * Returns the distance into the values where a particular
	 * resolution level's data begins
	 */
	public long getLevelOffset (int level) {
		long level_offset = 0;
		for (int levelI = 0; levelI < level; levelI++) {
			level_offset += level_sizes[levelI];
		}
		return level_offset;
	}
	
	/**
	 * Returns the size of the specified level
	 */
	public long getLevelSize (int level) {
		return level_sizes [level];
	}

	public long getResolution (int level) {
		return resolutions[level];
	}

	public int getMaxResolution() {
		return max_resolution;
	}

	public int getLevels () {
		return levels;
	}
	
	// if asked for value of a range that spans beyond legal coordinates
	// this function will return the average for the portion of the requested
	// range that lies within bounds
	public byte getValueForRange (long left, long right) {
		// never allow the range to be less than window_size--
		// why not max_resolution * 2? with max_res * 2 we can ensure that at
		// least
		// one similarity value gets returned (in rare cases 2)
		if (right - left + 1 < max_resolution * 2) {
			long extra = (max_resolution * 2 - (right - left)) / 2;
			left -= extra;
			left = left < 0 ? 0 : left;
			right = left + max_resolution * 2;
		}

		// find the highest resolution completely within a range of right - left
		long range_size = right - left;
		int levelI = resolutions.length - 1;
		for (; levelI > 0; levelI--) {
			if (resolutions[levelI] * 2 < range_size)
				break; // this level must contain at least part of what we're
			// looking for
		}

		// get the first resolution index at this level
		long firstI = left / resolutions[levelI];
		if (left % resolutions[levelI] != 0)
			firstI++;
		long lastI = right / resolutions[levelI];

		// truncate them if they are out-of-range
		firstI = firstI < level_sizes[levelI] ? firstI : level_sizes[levelI] - 1;
		lastI = lastI < level_sizes[levelI] ? lastI : level_sizes[levelI] - 1;
		firstI = firstI < 0 ? 0 : firstI;
		lastI = lastI < 0 ? 0 : lastI;

		lastI = lastI < firstI ? firstI : lastI;
		long lev_offset = getLevelOffset (levelI);
		int start_ind = (int) (lev_offset + firstI);
		int end_ind = (int) (lev_offset + lastI);
		byte sim = averageValues (start_ind, end_ind);

		// recursively get the beginning and ending pieces
		// only recurse if enough space remains on either side and we
		// haven't already recursed down to the lowest resolution level
		long left_size = firstI * resolutions[levelI] - left;
		byte left_sim = 0;
		if (left_size > resolutions[0] && levelI > 0)
			left_sim = getValueForRange (left, firstI * resolutions[levelI]);
		else
			left_sim = sim;
		long right_size = right - (lastI + 1) * resolutions[levelI];
		byte right_sim = 0;
		if (right_size > resolutions[0] && levelI > 0)
			right_sim = getValueForRange (
					(lastI + 1) * resolutions[levelI], right);
		else
			right_sim = sim;

		// average the three together
		double asim_singh = left_size * left_sim + right_size * right_sim + sim
				* (range_size - left_size - right_size);
		asim_singh /= range_size;
		byte final_sim = (byte) asim_singh;
		return final_sim;
	}
	
	/**
	 * returns the average of the value of the given range, inclusive
	 * @param start_ind
	 * @param end_ind
	 * @return
	 */
	public byte averageValues (int start_ind, int end_ind) {
		if (end_ind < start_ind) {
			throw new RuntimeException ("Corrupt SimilarityIndex");
		}
		end_ind++;
		long sim_sum = 0;
		int indexI = start_ind;
		for (; indexI < end_ind; indexI++) {
			sim_sum += handler.getValue (indexI);
		}
		return (byte) (sim_sum / (indexI - start_ind));
	}
	
	
	public static interface Handler {
		
		/**
		 * Returns the specified value
		 * 
		 * @param index
		 * @return
		 */
		public byte getValue (int index);
		
	}

}
