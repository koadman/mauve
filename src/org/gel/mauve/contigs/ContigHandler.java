package org.gel.mauve.contigs;

//import org.gel.mauve.analysis.Segment;

public interface ContigHandler {
	
	/**
	 * changes a pseudolocation to a location relative to the
	 * start of the contig the the location is on.
	 * 
	 * @param sequence The integer representing the model index of the desired sequence
	 * @param loci		The coordinate (location) of interest
	 * @return		A long representing the location relative to the beginning of the
	 * 				contig it's on.
	 */
	public long getContigCoord (int sequence, long loci);
	
	/**
	 * returns the name of the contig containing the desired location
	 * 
	 * @param sequence		The integer representing the model index of the desired sequence
	 *  @param loci		The coordinate (location) of interest
	 *  @return			The name of the contig containing the desired location
	 */
	//public String getContigName (int sequence, long loci);
	
	/**
	 * Splits a segment into pieces separated at contig boundaries
	 * 
	 * @param sequence		The integer representing the model index of the desired sequence
	 * @param segment		The coordinate (location) of interest
	 */
	//public void fixSegmentByContigs (int sequence, Segment segment);
	
	public long getPseudoCoord (int sequence, long loci, String contig);

}
