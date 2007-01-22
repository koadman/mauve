package org.gel.mauve;

import java.awt.Frame;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.biojava.bio.seq.AbstractFeatureHolder;
import org.biojava.bio.seq.ComponentFeature;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.OptimizableFilter;
import org.biojava.bio.seq.impl.SimpleStrandedFeature;
import org.biojava.bio.symbol.Location;
import org.gel.mauve.format.GenbankEmblFormat;
import org.gel.mauve.gui.navigation.AnnotationContainsFilter;
import org.gel.mauve.gui.navigation.NavigationPanel;

/**Utility class for data specific to sequence navigator involving
 * genomes and their features.
 * 
 * @author rissman
 *
 */
public class SeqFeatureData implements MauveConstants {
	
	public static FeatureComparator feature_comp = new FeatureComparator ();
	
	/**
	 * private constructor prevents instantiation; all static
	 *
	 */
	private SeqFeatureData () {
	}
	
	/**
	 * Returns a filter that will accept annotations matching the desired keys and values.
	 * 
	 * @param data		An array of length 3 arrays, each corresponding to a key, value, and
	 * 					whether the value is exact (array indeces specified in MauveConstants)
	 * @return			A filter that can be used to accept matching features
	 */
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
		//and = new FeatureFilter.And (ass)), and);
		return and;
	}
	
	/**
	 * Finds all features associated with specified genomes matching the specified
	 * key value pairs.
	 * 
	 * @param nomes		An array of which genomes whose features should be searched.
	 * @param data		An array of arrays containing desired key-value pairs, indeces
	 * 					specified in MauveConstants
	 * @return			An array of LinkedLists.  The first value in each list is the genome
	 * 					the list corresponds to; all other values are features 
	 * 					associated with that genome
	 */
	public static LinkedList [] findFeatures (Genome [] nomes, String [][] data) {
		LinkedList [] nome_data = new LinkedList [nomes.length];
		FeatureFilter filter = getFilter (data);
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
	
	/**
	 * A comparator for features.  
	 *
	 */
	static class FeatureComparator implements Comparator { 
		/**
		 * Returns -1 if Feature a is first in the 
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
	
	/**
	 * Parses a FilterCacheSpec to see if it contains a filter that is associated
	 * with a particular type of Feature.  Will only return one type; so will not work
	 * for some or filters.
	 * 
	 * @param spec		The spec to search
	 * @return			A string corresponding to the type associated with the spec,
	 * 					or MauveConstants.ANY_FEATURE
	 */
	public static String getTypeFromFilterSpec (FilterCacheSpec spec) {
		if (spec.getFilter() != null)
			return getTypeFromFilter (spec.getFilter ());
		else
			return null;
	}
	
	/**
	 * Parses a FeatureFilter to see if it contains a filter that is associated
	 * with a particular type of Feature.  Will only return one type; so will not work
	 * for some or filters.
	 * 
	 * @param filter	The filter to search
	 * @return			A string corresponding to the type associated with the filter,
	 * 					or MauveConstants.ANY_FEATURE
	 */
	public static String getTypeFromFilter (FeatureFilter filter) {
		String type = null;
		if (filter instanceof FeatureFilter.ByType)
			type = ((FeatureFilter.ByType) filter).getType();
		else if (filter instanceof FeatureFilter.And) {
			type = getTypeFromFilter (((FeatureFilter.And) filter).getChild1 ());
			if (type == null)
				type = getTypeFromFilter (((FeatureFilter.And) filter).getChild2 ());
		}
		return type;
	}
	
	/**
	 * Pops up a dialog box that allows a user to choose a specific (or all) genome/s
	 * 
	 * @param	parent	The frame the JDialogs for user input should block
	 * @param	model	The BaseViewerModel containing all possible genome choices
	 * @param	all_ok	True if user should have the option to choose "All Genomes".
	 * @param	needs_annotations	True if a genome should only be selectable if it has
	 * 			annotations loaded
	 * @return	An array of genomes that contains all genomes the user picked.
	 */
	public static Genome [] userSelectedGenomes (Frame parent, BaseViewerModel model, 
			boolean all_ok, boolean needs_annotations) {
		Vector choices = userSelectableGenomes (model, all_ok, needs_annotations);
		Object chosen = JOptionPane.showInputDialog(parent, "Choose Sequence to Navigate", 
				"Go To. . .", JOptionPane.QUESTION_MESSAGE,
				null, choices.toArray(), choices.get (0));
		if (chosen == null)
			return null;
		else
			return SeqFeatureData.convertIndexToSequence (choices, choices.indexOf(chosen));
	}
	
	/**
	 * constructs an array of all possible genomes a user should be able to pick from.
	 * Will have an "All" option depending on parameters
	 * 
	 * @param	model	The model whose genomes are possible user selections
	 * @param	all_ok	True if user should have the option to choose "All Genomes".
	 * @param	needs_annotations	True if a genome should only be selectable if it has
	 * 			annotations loaded
	 * @return	An vector of genomes a user can choose between
	 */
	public static Vector userSelectableGenomes (BaseViewerModel model,
			boolean all_ok, boolean needs_annotations) {
		Vector choices = model.getGenomes ();
		if (needs_annotations) {
			Iterator itty = choices.iterator();
			while (itty.hasNext()) {
				Genome genome = (Genome) itty.next();
				if (genome.getAnnotationSequence() == null ||
						!(genome.getAnnotationFormat () instanceof GenbankEmblFormat))
					itty.remove();
			}
		}
		if (all_ok && choices.size() > 1)
			choices.add (0, ALL_SEQUENCES);
		return choices;
	}
	
	/**
	 * returns an array representing the user selected genomes.  If the first choices in the
	 * 
	 * 
	 * @param choices		The vector of possible genomes
	 * @param index			The index selected from the vector of choices
	 * @return				An array of 1 containing the selected sequence unless
	 * 						the user chose all sequences, in which case the array
	 * 						will return all of them
	 */
	public static Genome [] convertIndexToSequence (Vector choices, int index) {
		Genome [] nomes = null;
		if (index == 0 && choices.get(0) instanceof String && 
				((String) choices.get(0)).equals (ALL_SEQUENCES)) {
			int max = choices.size ();
			nomes = new Genome [max - 1];
			for (int i = 1; i < max; i++)
				nomes [i - 1] = (Genome) choices.get (i);
		}
		else {
			nomes = new Genome [1];
			nomes [0] = (Genome) choices.get (index);
		}
		return nomes;
	}
	
}
