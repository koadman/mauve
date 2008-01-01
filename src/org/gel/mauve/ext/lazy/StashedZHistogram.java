package org.gel.mauve.ext.lazy;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.DataFormatException;

import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.LocationTools;
import org.biojava.bio.symbol.RangeLocation;
import org.gel.air.util.IOUtils;
import org.gel.mauve.SimilarityIndex;
import org.gel.mauve.ZoomHistogram;
import org.gel.mauve.ext.MauveStoreConstants;

public class StashedZHistogram extends ZoomHistogram implements
		MauveStoreConstants {

	transient protected RangeLoadTracker tracker;
	protected byte [] current;
	protected int current_start;
	transient protected DataInputStream cache;
	protected int last;
	transient protected Object lock;
	
	
	public StashedZHistogram (String file) throws Exception {
		init (file, "r");
	}
	
	public StashedZHistogram (RangeLoadTracker track, Object other_params) {
		tracker = track;
	}
	
	protected void init (String file, String mode) throws Exception {
		try {
			lock = new Object ();
			cache = IOUtils.getDataInputStream(file);
			int num_level = cache.readInt();
			int res = cache.readInt();
			long [] levels = IOUtils.readLongArray(cache, num_level);
			long [] resis = IOUtils.readLongArray(cache, num_level);
			init (num_level, res, levels, resis);
			last = -1;
			cache.mark(Integer.MAX_VALUE);
		} catch (IOException e) {
			DataFormatException de = new DataFormatException ("File doesn't contain " +
					"a similarity index");
			throw de;
		}
	}
	
	protected void loadRanges (int start, int end) {
		Location loc = null;
		if (tracker != null) {
			loc = tracker.getUnloadedRanges(start, end);
			if (loc != Location.empty) {
				//don't forget to return if already loaded
			}
		}
		else {
			getFromCache (start, end);
		}
	}
	
	protected void getFromCache (int start, int end) {
		try {
			int skip = start - last - 1;
			if (skip < 0) {
				cache.reset ();
				cache.mark(Integer.MAX_VALUE);
				skip = start;
			}
			if (skip > 0)
				cache.skipBytes(skip);
			int length = end - start + 1;
			if (current == null || current.length < length)
				current = new byte [length];
			current_start = start;
			cache.read(current, 0, length);
			last = end;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Makes sure range is loaded before averaging
	 */
	public byte averageValues(int start_ind, int end_ind) {
		synchronized (lock) {
			loadRanges (start_ind, end_ind);
			return super.averageValues(start_ind, end_ind);
		}
	}

	public byte getValue(int index) {
		return current [index - current_start];
	}
	
	protected void finalize () {
		try {
			cache.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static byte [] inputWholeHistogram (DataInputStream in) {
		try {
			byte [] vals = new byte [in.available()];
			in.readFully(vals);
			in.close();
			return vals;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean outputHistogram (SimilarityIndex sim_ind, String file) {
		try {
			DataOutputStream out = IOUtils.getDataOutputStream(file);
			int num_levels = sim_ind.getLevels ();
			out.writeInt(num_levels);
			out.writeInt(sim_ind.getMaxResolution ());
			for (int i = 0; i < num_levels; i++)
				out.writeLong (sim_ind.getLevelSize(i));
			for (int i = 0; i < num_levels; i++)
				out.writeLong(sim_ind.getResolution (i));
			sim_ind.writeAllVals(out);
			out.close ();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	

}
