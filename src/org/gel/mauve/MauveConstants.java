package org.gel.mauve;

import java.util.HashSet;
import java.util.Hashtable;

public interface MauveConstants {
	
	/**
	 * Strings representing choices for ways to navigate
	 */
	public static final String PRODUCT_NAME = "product/alternate_product_name";
	public static final String LOC_NAME = "gene/synonym/standard_name/name/" +
			"locus_tag/label/old_locus_tag";
	public static final String GO_FEATS = "go_biological_process/" + 
			"go_cellular_component/go_molecular_function";
	public static final String ID_NUMBER = "ec_number/db_xref/protein_id";
	
	/**
	 * Strings representing prebuilt groupings of search fields
	 */
	public static final String PRODUCT = "Product";
	public static final String NAME = "Name";
	public static final String GO = "Go Features";
	public static final String ID = "ID Number";
	
	/**
	 * dummy string representing all genomes rather than one specific sequence
	 */
	public static final String ALL_SEQUENCES = "All Sequences";
	
	/**
	 * Represents a feature type corresponding to any feature;
	 * used for FeatureFilter.ByType or FeatureFilter.ByAnnotationType
	 */
	public static final String ANY_FEATURE = "any";
	
	public static final Hashtable READ_TO_ACTUAL = new Hashtable ();
	
	/**
	 * contains default annotation keys to use for filters, etc.
	 */
	public static final HashSet ANNOTATION_KEYS = new HashSet ();
	
	/**
	 * ints representing indexes of data in data array used to
	 * represent constraints 
	 */
	public static final int FIELD = 0;
	public static final int VALUE = 1;
	public static final int EXACT = 2;
	
	/**
	 * border between separate components in the navigation guis
	 */
	public static final int BORDER = 10;

}
