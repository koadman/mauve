package org.gel.mauve.operon;

import java.util.HashSet;

import org.gel.air.bioj.BioJavaConstants;

public interface OperonConstants extends BioJavaConstants {
	
	public static final String [] TERNAL_STRINGS = {"internal", "external"};
	public static final int INTERNAL = 0;
	public static final int EXTERNAL = 1;
	public static final int UNCLEAR = 2;
	public static final int UNDERLENGTH = 3;
	public static final int STRAND_SWITCH = 4;
	public static final String REGDB_PSEUDO = "Phantom Gene";
	public static final HashSet <String> FEAT_TYPES = new HashSet <String> () {
		{
			add (GENE);
			add (CDS);
			add (RNA);
		}
	};

}
