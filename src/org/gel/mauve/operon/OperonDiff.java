package org.gel.mauve.operon;

import java.util.HashSet;

/**
 * Compares two operons with respect to one specific feature
 *
 */
public interface OperonDiff {
	
	/**
	 * returns true if the sequence specified by seq2 contains an
	 * operon that matches one according to the feature this OperonDiff
	 * is concerned with.
	 */
	public boolean isSame (Operon one, int seq2);
	
	public HashSet getRelatedOperons (Operon one, int seq2);
	
	public String getFeature ();
	
	public AncestralState.Difference getLastDifference ();

}
