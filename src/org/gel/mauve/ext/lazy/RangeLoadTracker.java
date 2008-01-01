package org.gel.mauve.ext.lazy;

import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.LocationTools;
import org.biojava.bio.symbol.RangeLocation;

/**
 * Convenience class for dealing with partial or lazy loading of a range of data
 * @author Anna Rissman
 *
 */
public class RangeLoadTracker {

	/**
	 * Represents what has already been loaded
	 */
	protected Location loaded_range;
	
	public RangeLoadTracker () {
		loaded_range = Location.empty;
	}
	
	/**
	 * Returns range between start and end that hasn't yet been loaded.  Can be in
	 * multiple gaps.
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	protected Location getUnloadedRanges (long start, long end) {
		Location current = new RangeLocation ((int) start, (int) end);
		current = LocationTools.subtract(current, loaded_range);
		return current;
	}
}
