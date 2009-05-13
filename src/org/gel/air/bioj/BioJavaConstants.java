package org.gel.air.bioj;

import java.util.Hashtable;

public interface BioJavaConstants {
	
	/**
	 * Strings representing groups of similar annotations
	 */
	public static final String PRODUCT_NAME = "product/alternate_product_name";

	public static final String LOC_NAME = "gene/synonym/standard_name/name/"
			+ "locus_tag/label/old_locus_tag";

	public static final String GO_FEATS = "go_biological_process/"
			+ "go_cellular_component/go_molecular_function";

	public static final String ID_NUMBER = "ec_number/db_xref/protein_id";
	
	
	public final static String FORWARD = "forward";
	
	public final static String COMPLEMENT = "complement";
	
	public static final String RNA = "rna";
	public static final String GENE = "gene";
	public static final String CDS = "cds";
	
	public static final int A = 0;
	public static final int G = 1;
	public static final int C = 2;
	public static final int T = 3;
	public static final Character [] BPS = {'A', 'G', 'C', 'T'};
	
	public static final class BPTable extends Hashtable <Character, Integer> {
		public BPTable () {
			super (4);
			for (int i = 0; i < BPS.length; i++) {
				put(BPS [i], i);
				put(Character.toLowerCase(BPS [i]), i);
			}
		}
	};
	
	public static final BPTable BP_TO_IND = new BPTable ();

}
