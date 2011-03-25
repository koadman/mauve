package org.gel.mauve;

import java.util.HashSet;
import java.util.Hashtable;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.OptimizableFilter;

public interface MauveConstants {

	/**
	 * Strings representing choices for ways to navigate
	 */
	public static final String PRODUCT_NAME = "product/alternate_product_name";

	public static final String LOC_NAME = "gene/synonym/standard_name/name/"
			+ "locus_tag/label/old_locus_tag";

	public static final String GO_FEATS = "go_biological_process/"
			+ "go_cellular_component/go_molecular_function";

	public static final String ID_NUMBER = "ec_number/db_xref/protein_id";

	/**
	 * Strings representing prebuilt groupings of search fields
	 */
	public static final String PRODUCT = "Product";

	public static final String NAME = "Name";

	public static final String GO = "Go Features";

	public static final String ID = "ID Number";

	/**
	 * Strings representing keys possibly present in a SegmentDataProcessor
	 * object
	 */
	public static final String ISLAND_MIN = "island_min";

	public static final String BACKBONE_MIN = "backbone_min";

	public static final String MAX_LENGTH_RATIO = "max_length_ratio";

	public static final String MINIMUM_PERCENT_CONTAINED = "minimum_percent_contained";
	
	public static final String FILE_STUB = "output_file_stub";

	public static final String DEFAULT_TITLES = "default_titles";

	public static final String REFERENCE = "reference";

	public static final String FIRSTS = "firsts";

	public static final String BACKBONE = "backbone_vector";

	public static final String ALL_MULTIPLICITY = "all_multiplicity_int";

	public static final String SEQUENCE_INDEX = "sequence index";
	
	public static final String MODEL = "model";
	
	public static final String GENOME_LENGTHS = "genome_lengths";
	
	public static final String CONTIG_HANDLER = "contig_handler";
	
	public static final String NUM_GENES_PER_MULT = "number_of_genes_per_multiplicity";
	
	public static final String TOTAL_GENES = "total_genes";
	
	public static final String TOTALS = "totals";
	
	/** */
	public static final String CIRCULAR_CHAR = "*";
	
	/**
	 * Strings representing annotation types
	 */
	public static final String DB_XREF = "db_xref";
	
	/**
	 * Integer codes for how to cycle through segments for processing
	 */
	public static final int BY_GENOMES = 1;

	public static final int BY_BB_LIST = 2;

	public static final int BY_ALL_AND_BB = 3;

	public static final int BY_ONE_GENOME = 4;

	public static final int BY_ONE_AND_BB = 6;

	/**
	 * default values for backbone processing for analysis package
	 */
	public static final int DEFAULT_ISLAND_MIN = 1;

	public static final int DEFAULT_BACKBONE_MIN = 1;

	public static final double DEFAULT_MAX_LENGTH_RATIO = .2;
	
	public static final double DEFAULT_MIN_PERCENT_CONTAINED = 99.8;

	/**
	 * dummy string representing all genomes rather than one specific sequence
	 */
	public static final String ALL_SEQUENCES = "All Sequences";

	/**
	 * Represents a feature type corresponding to any feature; used for
	 * FeatureFilter.ByType or FeatureFilter.ByAnnotationType
	 */
	public static final String ANY_FEATURE = "any";

	public static final Hashtable READ_TO_ACTUAL = new Hashtable ();

	/**
	 * contains default annotation keys to use for filters, etc.
	 */
	public static final HashSet ANNOTATION_KEYS = new HashSet ();

	/**
	 * ints representing indexes of data in data array used to represent
	 * constraints
	 */
	public static final int FIELD = 0;

	public static final int VALUE = 1;

	public static final int EXACT = 2;

	/**
	 * border between separate components in the navigation guis
	 */
	public static final int BORDER = 10;
	
	/**
	 * represents subfolder output from analysis package is written to
	 */
	public static final String ANALYSIS_OUTPUT = "analysis";
	
	/**
	 * represents subfolder output from contig package is written to
	 */
	public static final String CONTIG_OUTPUT = "contigs";
	
	/**
	 * height that should be added when a new type of feature is displayed
	 */
	public static final int FEATURE_HEIGHT = 25;
	
	/**
	 * String present in all ASAP generated db_xref values
	 */
	public static final String ASAP = "asap";
	
	public static final String ERIC = "eric";
	
	/**
	 * Fields biojava uses as constants that are protected for some reason
	 */
	public static final String LOCUS = "LOCUS";
	
	public static final OptimizableFilter NULL_AVOIDER = new OptimizableFilter () {
		public boolean accept (Feature f) {
			return f.getAnnotation () != null;
		}

		public boolean isDisjoint (FeatureFilter filt) {
			return false;
		}

		public boolean isProperSubset (FeatureFilter sup) {
			return this.equals (sup);
		}

	};

	public final static String DEFAULT_CONTIG = "chromosome";
}
