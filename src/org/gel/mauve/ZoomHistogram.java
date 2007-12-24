package org.gel.mauve;

import java.util.Hashtable;

import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.LocationTools;
import org.biojava.bio.symbol.RangeLocation;

/**
 * Meant to represent data that varies over a range and needs to be viewed at
 * multiple resolutions.
 * 
 * @author Aaron Darling
 *
 */
public class ZoomHistogram {
	
	/** < Never index fewer than this many alignment columns */
	protected int max_resolution = 5;

	/** < The number of characters covered by an index entry at each level */
	protected long [] resolutions;

	/** < The number of index entries at each level */
	protected long [] index_size;

	/** < The similarity index, similarity values are discretized to a byte value */
	protected byte [] values;

	/** < The number of resolution levels */
	protected int levels;
	
	/**
	 * whether the entire histogram is loaded
	 */
	protected Loader loader;
	
	/**
	 * Represents loaded range
	 */
	Location loaded_range;
	
	protected ZoomHistogram () {
	}
	
	public ZoomHistogram (int level, int max_res, long [] sizes, long [] res, Loader load) {
		loader = load;
		loaded_range = new RangeLocation (0, 0);
		long total = 0;
		for (int i = 0; i < sizes.length; i++)
			total += sizes [i];
		byte [] sims = new byte [(int) total];
		init (level, max_res, sizes, res, sims);
	}
	
	public ZoomHistogram (int level, int max_res, long [] sizes, long [] res, byte [] vals) {
		init (level, max_res, sizes, res, vals);
	}
	
	protected void init (int levels, int max_res, long [] sizes, long [] res, byte [] vals) {
		this.levels = levels;
		max_resolution = max_res;
		index_size = sizes;
		resolutions = res;
		values = vals;
	}
	/**
	 * Returns the distance into the values where a particular
	 * resolution level's data begins
	 */
	public long getLevelOffset (int level) {
		long level_offset = 0;
		for (int levelI = 0; levelI < level; levelI++) {
			level_offset += index_size[levelI];
		}
		return level_offset;
	}

	/**
	 * get an individual value
	 */
	public byte getValue (int level, int index) {
		int level_offset = 0;
		for (int levelI = 0; levelI < level; levelI++) {
			level_offset += index_size[levelI];
		}
		if (index >= index_size[level])
			throw new ArrayIndexOutOfBoundsException ();

		return values[level_offset + index];
	}

	/**
	 * Set an individual similarity value
	 */
	protected void setValue (int level, int index, byte val) {
		int level_offset = 0;
		for (int levelI = 0; levelI < level; levelI++) {
			level_offset += index_size[levelI];
		}
		if (index > index_size[level])
			throw new ArrayIndexOutOfBoundsException ();

		values[level_offset + index] = val;
	}

	public long getResolution (int level) {
		return resolutions[level];
	}

	public long getLevels () {
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
		firstI = firstI < index_size[levelI] ? firstI : index_size[levelI] - 1;
		lastI = lastI < index_size[levelI] ? lastI : index_size[levelI] - 1;
		firstI = firstI < 0 ? 0 : firstI;
		lastI = lastI < 0 ? 0 : lastI;

		lastI = lastI < firstI ? firstI : lastI;
		
		if (loader != null)
			loadNecessary (firstI, lastI);
		long sim_sum = 0;
		int indexI = 0;
		for (; indexI <= lastI - firstI; indexI++) {
			sim_sum += getValue (levelI, (int) (firstI + indexI));
		}
		if (lastI < firstI) {
			throw new RuntimeException ("Corrupt SimilarityIndex");
		}
		byte sim = (byte) (sim_sum / (indexI));

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
	
	protected void loadNecessary (long start, long end) {
		Location current = new RangeLocation ((int) start, (int) end);
		current = LocationTools.subtract(current, loaded_range);
		if (!LocationTools.areEqual(current, Location.empty)) {
			loader.loadRanges (current, this);
			loaded_range = loaded_range.union(current);
		}
	}
	
	
	public static interface Loader {
		
		/**
		 * Loads the ranges specified; can be gapped.
		 * Should block thread until all is loaded.
		 * 
		 * @param locations
		 */
		public void loadRanges (Location locations, ZoomHistogram hist);
		
	}

}
