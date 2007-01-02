package org.gel.mauve.gui.navigation;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.OptimizableFilter;
import org.biojava.bio.symbol.Location;
import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Genome;

/**Utility class for data specific to sequence navigator involving
 * genomes and their features.
 * 
 * @author rissman
 *
 */
public class SeqFeatureData implements NavigationConstants {
	
	public static FeatureComparator feature_comp = new FeatureComparator ();
	
	/**
	 * Constructs a new wrapper for Genomes viewed by this mauve frame
	 * and collates information on the features contained within
	 * 
	 * @param mod			The BaseViewerModel to associate this with
	 */
	private SeqFeatureData () {
	}
	
	public static FeatureFilter getFilter (String [][] data) {
		FeatureFilter and = null;
		for (int i = 0; i < data.length; i++) {
			StringTokenizer toke = SeqFeatureData.separateFields (SeqFeatureData.readToActual (
					data[i][NavigationPanel.FIELD]));
			FeatureFilter or = null;
			while (toke.hasMoreTokens()) {
				FeatureFilter cur = null;
				cur = new AnnotationContainsFilter (toke.nextToken(),
						data [i][NavigationPanel.VALUE].toLowerCase(),
						Boolean.valueOf (data [i][NavigationPanel.EXACT]).booleanValue ());
				if (or != null) {
					or = new FeatureFilter.Or (or, cur);
	}
				else
					or = cur;
			}
			if (and != null)
				and = new FeatureFilter.And (and, or);
			else
				and = or;
		}
		and = new FeatureFilter.And (new OptimizableFilter (){
			public boolean accept (Feature f) {
				return f.getAnnotation() != null;
	}
	
			public boolean isDisjoint(FeatureFilter filt) {
				return false;
	}
	
			public boolean isProperSubset(FeatureFilter sup) {
				return this.equals(sup);
	}
	
		}, and);
		return and;
	}
	
	public static LinkedList [] findFeatures (Genome [] nomes, String [][] data) {
		LinkedList [] nome_data = new LinkedList [nomes.length];
		FeatureFilter filter = SeqFeatureData.getFilter (data);
		for (int i = 0; i < nomes.length; i++) {
			FeatureHolder hold = nomes [i].getAnnotationSequence ();
			hold = hold.filter(filter, true);
			LinkedList list = new LinkedList ();
			list.add (nomes [i]);
			Iterator itty = hold.features ();
			while (itty.hasNext ())
				list.add (itty.next ());
			nome_data [i] = list;
		}
		return nome_data;
	}
	

	/**
	 * Removes multiple features with the same location, so only one
	 * is displayed in the tree
	 * 
	 * @param feats			The list of features that might contain duplicates
	 */
	public static void removeLocationDuplicates (LinkedList feats) {
		if (feats.size() > 1) {
			Collections.sort(feats, feature_comp);
			Object first = feats.get(0);
			Object compare = null;
			int index = 1;
			do {
				compare = feats.get(index);
				if (feature_comp.compare (first, compare) == 0)
					feats.remove(index);
				else {
					first = compare;
					index++;
			}
			} while (index < feats.size ());
		}
	}
	
	
	static class FeatureComparator implements Comparator { 
	/**
		 * A comparator for features.  Returns -1 if Feature a is first in the 
		 * sequence, 1 if b is first, and 0 if they are at the same place
	 * 
		 * @param a		The first object to compare
		 * @param b		The second object to compare
	 */
		public int compare (Object a, Object b) {
			int one = ((Feature) a).getLocation ().getMin ();
			int two = ((Feature) b).getLocation ().getMin ();
			return (one == two) ?  0 : (one < two) ? -1 : 1;
		}
	}
	
	/**
	 * returns a tokenizer with string separated at '/'
	 * 
	 * @param fields
	 * @return
	 */
	public static StringTokenizer separateFields (String fields) {
		return new StringTokenizer (fields, "/", false);
	}
	
	/**
	 * converts spaces in string to underscores
	 * 
	 * @param field		The human readible version of the field
	 * @return			The qualifier name as it appears in data
	 */
	public static String readToActual (String field) {
		String actual = (String) READ_TO_ACTUAL.get (field);
		if (actual != null)
			field = actual;
		else
			field = field.toLowerCase();
		return field.replace(' ', '_');
	}
	
	/**
	 * finds the center sequence coordinate of a feature
	 * @param feature		The feature whose center should be found
	 * @return				A long representing the sequence position
	 * 						of the center of the feature
	 */
	public static long centerOfFeature (Feature feature) {
		Location loca = feature.getLocation ();
		return loca.getMin () + (loca.getMax () - loca.getMin ())/2; 
	}
	
}
