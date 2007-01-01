package org.gel.mauve.gui.navigation;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureHolder;
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
	
	/**
	 * The viewer model associated with these sequences
	 */
	protected BaseViewerModel model;

	/**
	 * maps each genome to set of keys present in at least one
	 * feature of the genome
	 */
	protected Hashtable genome_keys;
	
	/**
	 * Constructs a new wrapper for Genomes viewed by this mauve frame
	 * and collates information on the features contained within
	 * 
	 * @param mod			The BaseViewerModel to associate this with
	 */
	public SeqFeatureData (BaseViewerModel mod) {
		model = mod;
		genome_keys = new Hashtable ();
		for (int i = 0; i < model.getSequenceCount (); i++) {
			Genome genome = model.getGenomeBySourceIndex(i);
			FeatureHolder holder = genome.getAnnotationSequence ();
			HashSet set = new HashSet ();
			fillKeys (holder, set);
			genome_keys.put(genome, set);
		}
	}
	
	/**
	 * puts all keys in feature holder into hashset
	 * 
	 * @param holder
	 * @param set
	 */
	protected void fillKeys (FeatureHolder holder, HashSet set) {
		if (holder instanceof Feature) {
			Feature feature = (Feature) holder;
			if (feature.getAnnotation () != null)
				set.addAll(feature.getAnnotation().keys());
		}
		Iterator itty = holder.features ();
		while (itty.hasNext ()) {
			fillKeys ((Feature) itty.next (), set);
		}
	}
	
	
	/**
	 * Finds features matching specified constraints
	 * 
	 * @param data			Data representing the constraints to apply
	 * @param genome		Which genome to get the features out of
	 * @return				A list of all features matching the given constraints
	 */
	public LinkedList findFeatures (String [] data, Genome genome) {
		StringTokenizer fields = separateFields ( readToActual (
				data [FIELD]));
		LinkedList list = new LinkedList ();
		HashSet keys = (HashSet) genome_keys.get(genome);
		while (fields.hasMoreTokens ()) {
			String field = fields.nextToken();
			if (keys.contains(field)) {
				findFeatures (list, field, data [NavigationPanel.VALUE], 
						Boolean.valueOf(data [NavigationPanel.EXACT]).booleanValue(), genome);
			}
		}
		return list;
	}
	
	
	/**
	 * finds features matching specific constraints
	 * 
	 * @param features	list results will be added to
	 * @param field		The field (qualifier) to search by
	 * @param val		The value the field should have
	 * @param exact		Whether the value is exact or should contain the desired string
	 * @param genome	The genome to find the gene in
	 */
	public static LinkedList findFeatures (LinkedList features, String field, String val,
			boolean exact, Genome genome) {
		return findFeatures (genome.getAnnotationSequence(), field, val.toLowerCase(),
				exact, features);
	}
	
	/**
	 * finds features matching specific constraints
	 * 
	 * @param field			The field (qualifier) to search by
	 * @param val			The exact value the field should have
	 * @param genome		The genome to find the gene inS
	 */
	public static LinkedList findFeatures (String field, String val, Genome genome) {
		LinkedList list = new LinkedList ();
		return findFeatures (list, field, val, true, genome);
	}
	
	/**
	 * finds features matching specific constraints
	 * 
	 * @param holder	The feature holder containing all features to search
	 * @param field		The field (qualifier) to search by
	 * @param value		The value the field should have
	 * @param exact		Whether the value is exact or should contain the desired string
	 * @param results	The LinkedList in which results will be stored
	 */
	private static LinkedList findFeatures (FeatureHolder holder, String field, 
			String value, boolean exact, LinkedList results) {
		if (holder instanceof Feature && matchesConstraint ((Feature) holder,
				field, value, exact)) {
			results.add (holder);
		}
		Iterator itty = holder.features ();
		while (itty.hasNext ())
			findFeatures ((Feature) itty.next (), value, field, exact, results);
		return results;
	}
	
	/**
	 * Determines if the given feature matches the given constraints
	 * 
	 * @param feature		The feature in question
	 * @param fields		A list of fields, one of which should contain the desired value
	 * @param value			The value to be found in one of the fields
	 * @param exact			Whether the field is an exact match, or should contain the value
	 * @return				True if the value matches, false otherwise
	 */
	public static boolean matchesConstraints (Feature feature, StringTokenizer fields, 
			String value, boolean exact) {
		while (fields.hasMoreTokens()) {
			if (matchesConstraint (feature, fields.nextToken(), value, exact))
				return true;
		}
		return false;
	}
	
	/**
	 * Determines if the given feature matches the given constraints
	 * 
	 * @param feature		The feature in question
	 * @param field			The field (qualifier) in question
	 * @param value			The value to be found in the field
	 * @param exact			Whether the field is an exact match, or should contain the value
	 * @return				True if the value matches, false otherwise
	 */
	public static boolean matchesConstraint (Feature feature, String field, 
			String value, boolean exact) {
		Annotation notes = feature.getAnnotation ();
		boolean match = false;
		if (notes != null) {
			if (notes.keys ().contains (field)) {
				Object val = notes.getProperty (field);
				if (val instanceof String)
					match = containsOrEquals (((String) val).toLowerCase(), value, exact);					
				else if (val instanceof List) {
					Iterator itty = ((List) val).iterator ();
					while (itty.hasNext() && !match)
						match = containsOrEquals (((String) itty.next ()).toLowerCase(),
								value, exact);
				}
				else if (val instanceof Boolean) {
					match = val.equals (new Boolean (value));
				}
				else
					System.out.println ("unsearchable field: " + field +
							" value type: " + val.getClass());
			}
		}
		return match;
	}
	
	/**
	 * tests if a string contains or equals a given value
	 * 
	 * @param field		The string in question
	 * @param value		The value the string should contain
	 * @param exact		Whether the match should be exact or contains
	 * @return			True if the field contains the value, false otherwise
	 */
	public static boolean containsOrEquals (String field, String value, boolean exact) {
		if ((exact && field.equals (value)) || (!exact && 
				field.indexOf (value) != -1))
			return true;
		else
			return false;
	}
	
	/**
	 * Returns all the fields present in a given genome
	 * 
	 * @param genome		The genome in question
	 * @return				The list of qualifier names present in the genome
	 */
	public HashSet getGenomeKeys (Genome genome) {
		return (HashSet) genome_keys.get (genome);
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
