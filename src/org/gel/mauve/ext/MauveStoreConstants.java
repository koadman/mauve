package org.gel.mauve.ext;

import org.gel.air.ja.stash.StashConstants;
import org.gel.mauve.MauveConstants;

public interface MauveStoreConstants extends StashConstants, MauveConstants {

	/**
	 * for histograms
	 */
	public static final String RES_LEVELS = "resolution_levels";
	public static final String MAX_RES = "maximum_resolution";
	public static final String LEVEL_LENGTHS = "level_lengths";
	public static final String LEVEL_RES = "level_resolutions";
	public static final String HIST_VALS = "histogram_values";
	
	/**
	 * for stash
	 */
	public static final String FEATURES = "features";
	public static final String POSITION = "position";
	public static final String CONTIG = "contig";
	public static final String CONTIG_INDEX = "contig_index";
	public static final String URI = "uri";
	public static final String FEATURE_INDEX = "feature_index";
	public static final String FEATURE_FILES = "feature_files";
	public static final String FORMAT = "format";
	public static final String FEATURE_LABELS = "feature_labels";
	public static final String FEATURE_TYPE = "feature_type";
	public static final String BACKBONE_IDS = "backbone_ids";
	public static final String GENOMES = "genomes";
	public static final String LCBS = "lcbs";
	public static final String GENOME = "genome";
	public static final String GAP_FILE_START = "gap_file_start";
	public static final String GAP_FILE_END = "gap_file_end";
	public static final String REVERSE = "reverse";
	public static final String LCB_COUNT = "lcb_count";
	
	public static final String GENOME_CLASS = "Genome";
	public static final String CONTIG_CLASS = "Contig";
	public static final String FEATURE_CLASS = "Feature";
	public static final String GENOME_LABEL_CLASS = "GenomeLabel";
	public static final String FEATURE_INDEX_CLASS = "FeatureIndex";
	public static final String FEATURE_FILE_CLASS = "FeatureFile";
	public static final String ALIGNMENT_CLASS = "Alignment";
	public static final String ALIGNED_GENOME_CLASS = "AlignedGenome";
	public static final String LCB_CLASS = "LCB";
	
	public static final String FORWARD_SYMBOL = "+";
	public static final String COMPLEMENT_SYMBOL = "-";
	public static final String REVERSE_SYMBOL = "-";

}
