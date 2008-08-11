package org.gel.mauve.operon;

import java.util.Hashtable;
import java.util.LinkedList;

import org.biojava.bio.seq.Feature;
import org.gel.mauve.analysis.output.IslandGeneFeatureWriter;
import org.gel.mauve.analysis.output.SegmentDataProcessor;

public class OperonMultiplicityWriter extends IslandGeneFeatureWriter {
	
	protected double min_percent_on_island;
	protected LinkedList <Feature> non_conserved_ops;

	protected OperonMultiplicityWriter(SegmentDataProcessor processor) {
		super("operon_mults", processor);
	}
	
	

	protected void initSubClassParticulars(Hashtable args) {
		min_percent_on_island = 55.0;
		non_conserved_ops = new LinkedList <Feature> ();
		args.put(MINIMUM_PERCENT_CONTAINED, min_percent_on_island);
		super.initSubClassParticulars(args);
	}



	public boolean badType(Feature feat) {
		if (feat.getType ().toLowerCase ().equals (OPERON_STRING) ||
				(feat.getAnnotation() != null && feat.getAnnotation().containsProperty(
						TYPE_STRING) && feat.getAnnotation().getProperty(
								TYPE_STRING).equals(OPERON_STRING))) {
			non_conserved_ops.add(feat);
			return false;
		}
		else
			return true;
	}
	
	

}
