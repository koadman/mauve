package org.gel.mauve.histogram;

import java.io.Serializable;

import org.gel.mauve.Genome;

/**
 * Meant to represent data that varies over a range and needs to be viewed at
 * multiple resolutions.
 * 
 * @author Aaron Darling
 *
 */
public class ZoomHistogram implements Serializable {
	
	static final long serialVersionUID = 2;
	
	/** < Never index fewer than this many alignment columns */
	protected int max_resolution = 5;

	/** < The number of characters covered by an index entry at each level */
	protected long [] resolutions;

	/** < The number of index entries at each level */
	protected long [] level_sizes;

	/** < The number of resolution levels */
	protected int levels;

	/** < The similarity index, similarity values are discretized to a byte value */
	protected byte [] sim_index;


	protected long seq_length;


	protected int index_factor = 8;

	/** < Index granularity grows in multiples of this number */
	
	protected int min_index_values = 500;

	/** < Never create a resolution level with fewer than this many values */

	protected int max_index_mb = 300;

	/** < Maximum size in MB for the index */

	protected byte [] min_vals;	/**< min values for levels above 0 */
	protected byte [] max_vals; /**< max values for levels above 0 */

	protected ZoomHistogram () {
	}
	
	public ZoomHistogram(Genome g){
		this.seq_length = g.getLength ();		
		allocateIndex ();
	}
	
	public ZoomHistogram (long seq_length, int level, int max_res, long [] sizes, long [] res) {
		init (seq_length, level, max_res, sizes, res);
	}
	
	protected void init (long seq_length, int levels, int max_res, long [] sizes, long [] res) {
		this.seq_length = seq_length;
		this.levels = levels;
		max_resolution = max_res;
		level_sizes = sizes;
		resolutions = res;
		allocateIndex ();
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
	
	public byte getValueForRange (long left, long right) {
		return getValueForRange(left,right,0);
	}
	// if asked for value of a range that spans beyond legal coordinates
	// this function will return the average for the portion of the requested
	// range that lies within bounds
	// @argument type Whether to get the mean, min, or max.  mean = 0, min = -1, max = 1
	public byte getValueForRange (long left, long right, int type) {
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
		byte sim;
		if(type==0)
			sim = averageValues (start_ind, end_ind);
		else 
			sim = minOrMaxValues (start_ind, end_ind,type);

		// recursively get the beginning and ending pieces
		// only recurse if enough space remains on either side and we
		// haven't already recursed down to the lowest resolution level
		long left_size = firstI * resolutions[levelI] - left;
		byte left_sim = 0;
		if (left_size > resolutions[0] && levelI > 0){
			long newr = firstI * resolutions[levelI];
			// TODO: fix whatever bug causes this infinite loop condition!
			if(newr==right){left_sim=0;}else{
				left_sim = getValueForRange (left, newr,type);
			}
		}else
			left_sim = sim;
		long right_size = right - (lastI + 1) * resolutions[levelI];
		byte right_sim = 0;
		if (right_size > resolutions[0] && levelI > 0)
		{
			long newl = (lastI + 1) * resolutions[levelI];
			if(newl==left){right_sim = 0;}else{
				right_sim = getValueForRange (newl, right,type);
			}
		}else
			right_sim = sim;

		// average the three together
		if(type==0){
			double asim_singh = left_size * left_sim + right_size * right_sim + sim
					* (range_size - left_size - right_size);
			asim_singh /= range_size;
			byte final_sim = (byte) asim_singh;
			return final_sim;
		}else if(type==1){
			byte m = right_sim > sim ? right_sim : sim;
			m = left_sim > m ? left_sim : m;
			return m;
		}else{
			byte m = right_sim < sim ? right_sim : sim;
			m = left_sim < m ? left_sim : m;
			return m;
		}
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
			sim_sum += getValue (indexI);
		}
		return (byte) (sim_sum / (indexI - start_ind));
	}
	
	/**
	 * returns the minimum value of the given range, inclusive
	 * @param start_ind
	 * @param end_ind
	 * @return
	 */
	public byte minOrMaxValues (int start_ind, int end_ind, int type) {
		if (end_ind < start_ind) {
			throw new RuntimeException ("Corrupt SimilarityIndex");
		}
		end_ind++;
		byte minnow = 127;
		if(type>0)	minnow = -128;
		byte[] arr = null;
		if(start_ind < level_sizes[0]){
			arr = sim_index;
		}else{
			start_ind -= level_sizes[0];
			end_ind -= level_sizes[0];
			arr = type < 0 ? min_vals : max_vals;
		}
		int indexI = start_ind;
		for (; indexI < end_ind; indexI++) {			
			if(type < 0){	
				byte cur = arr[indexI];
				minnow = cur < minnow ? cur : minnow;
			}
			if(type > 0){
				byte cur = arr[indexI];
				minnow = cur > minnow ? cur : minnow;
			}
		}
		return minnow;
	}


	/**
	 * Returns the specified value
	 * 
	 * @param index
	 * @return
	 */
	public byte getValue(int index) {
		return sim_index [index];
	}


	/**
	 * Allocates space for the similarity index
	 */
	protected void allocateIndex () {
		// calculate the number of index levels
		long level_one = seq_length / max_resolution;
		long level_tmp = level_one;
		levels = seq_length > max_resolution ? 1 : 0;
		while (level_tmp > min_index_values) {
			level_tmp /= index_factor;
			levels++;
		}

		resolutions = new long [levels];
		level_sizes = new long [levels];
		level_tmp = level_one;
		long size_sum = 0;
		int levelI;
		long cur_resolution = max_resolution;
		for (levelI = 0; levelI < levels; levelI++) {
			resolutions[levelI] = cur_resolution;
			level_sizes[levelI] = level_tmp;
			size_sum += level_tmp;
			level_tmp /= index_factor;
			cur_resolution *= index_factor;
		}

		// check whether the index will fit in the max size
		if (size_sum > ((long) max_index_mb * 1024l * 1024l)) {
			throw new RuntimeException ("Similarity index is too large.");
		}

		sim_index = new byte [(int) size_sum];

		// nothing more to allocate if there are no levels
		if (levels == 0)
			return;
		
		min_vals = new byte[(int)(size_sum - level_sizes[0])];
		max_vals = new byte[(int)(size_sum - level_sizes[0])];
	}

	/* Calculates the higher-level values in the multi-level index as
	 * an average value over the subranges.
	 */
	protected void calculateHigherLevels(){
		// calculate subsequent levels using the previous level
		for (int levelI = 1; levelI < levels; levelI++) {
			int componentI = (int) getLevelOffset (levelI - 1);
			int level_offset = (int) getLevelOffset (levelI);
			for (int indexI = 0; indexI < level_sizes[levelI]; indexI++) {
				int sim_sum = 0;
				int min_val = 999;	// this is bigger than byte range
				int max_val = -999;
				for (int subI = 0; subI < index_factor; subI++) {
					sim_sum += sim_index[componentI];
					min_val = sim_index[componentI] < min_val ? sim_index[componentI] : min_val;
					max_val = sim_index[componentI] > max_val ? sim_index[componentI] : max_val;
					componentI++;
				}
				// set to the average of its components
				sim_index[level_offset + indexI] = (byte) (sim_sum / index_factor);
				min_vals[level_offset + indexI - (int)level_sizes[0]] = (byte)min_val;
				max_vals[level_offset + indexI - (int)level_sizes[0]] = (byte)max_val;
			}
		}
	}

	/**
	 * Set an individual similarity value
	 */
	protected void setSimilarity (int level, int index, byte sim_value) {
		int level_offset = 0;
		for (int levelI = 0; levelI < level; levelI++) {
			level_offset += level_sizes[levelI];
		}
		if (index > level_sizes[level])
			throw new ArrayIndexOutOfBoundsException ();

		sim_index[level_offset + index] = sim_value;
	}
	
	/**
	 * get an individual similarity
	 */
	public byte getSimilarity (int level, int index) {
		int level_offset = 0;
		for (int levelI = 0; levelI < level; levelI++) {
			level_offset += level_sizes[levelI];
		}
		if (index >= level_sizes[level])
			throw new ArrayIndexOutOfBoundsException ();

		return sim_index[level_offset + index];
	}
	
	/**
	 * Sets the histogram height values for the base level
	 * higher levels will be automatically calculated as the average over regions in the base level
	 * 
	 * @param base	array of bytes with length equal to getLevelSize(0)
	 */
	public void setBaseLevel( byte[] base ){
		System.arraycopy(base, 0, sim_index, 0, (int)getLevelSize(0));
		calculateHigherLevels();
	}

	/*
	 * Use a set of values as long as the genome to calculate the base index
	 * Calculates average values over max_resolution size chunks. Does not smooth.
	 */
	public void setGenomeLevelData( byte[] data ){
		for(int d=0; d<level_sizes[0]; d++){
			float runsum = 0;
			for(int i=0; i<max_resolution; i++)
				runsum += data[d*max_resolution+i];
			runsum /= max_resolution;
			sim_index[d] = (byte)runsum;
		}
		calculateHigherLevels();
	}
}
