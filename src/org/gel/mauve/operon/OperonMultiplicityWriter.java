package org.gel.mauve.operon;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;

import org.biojava.bio.seq.Feature;
import org.gel.mauve.analysis.PhyloMultiplicity;
import org.gel.mauve.analysis.Segment;
import org.gel.mauve.analysis.output.IslandGeneFeatureWriter;
import org.gel.mauve.analysis.output.SegmentDataProcessor;

public class OperonMultiplicityWriter extends IslandGeneFeatureWriter {
	
	protected double min_percent_on_island;
	protected HashSet <Feature> unclear_mults;
	//start coordinate of operon to multiplicity; don't have access to actual
	//operon object here
	protected Hashtable <Long, PhyloMultiplicity> mults;

	protected OperonMultiplicityWriter(SegmentDataProcessor processor) {
		super(processor, "ops");
	}

	protected void initSubClassParticulars(Hashtable args) {
		min_percent_on_island = 92.0;
		unclear_mults = (HashSet) args.get(UNCLEAR_MULTS);
		if (unclear_mults == null)
			unclear_mults = new HashSet <Feature> ();
		mults = (Hashtable <Long, PhyloMultiplicity>) args.get(MULTIPLICITIES);
		args.put(MINIMUM_PERCENT_CONTAINED, min_percent_on_island);
		if (mults == null)
			mults = new Hashtable <Long, PhyloMultiplicity> ();
		super.initSubClassParticulars(args);
	}



	public boolean badType(Feature feat) {
		if (feat.getType ().toLowerCase ().equals (OPERON_STRING) ||
				(feat.getAnnotation() != null && feat.getAnnotation().containsProperty(
						TYPE_STRING) && feat.getAnnotation().getProperty(
								TYPE_STRING).equals(OPERON_STRING))) {
			return false;
		}
		else {
			num_features [seq_index]--;
			return true;
		}
	}
	
	public boolean shouldPrintRow (int row) {
		if (!backbone_instead && !unclear_mults.contains(cur_feat)) {
			performComplexIteration ();
			return false;
		}
		else {
			Feature prev = cur_feat;
			boolean should = super.shouldPrintRow (row);
			if (prev != null) {
				mults.put((long) prev.getLocation().getMin(), 
						getCurrentMultData ());
			}
			if (!should && backbone_instead && prev != null) {
				unclear_mults.add(prev);
			}
			else if (!backbone_instead && should) {
				unclear_mults.remove(cur_feat);
			}
			return should;
		}
	}
	
	public void printData (int how) {
		super.printData(how);
		out.println ();
		if (!backbone_instead) {
			System.out.println ("uncleear: " + unclear_mults.size());
			out.println ("operons with unclear multiplicities");
			out.println (unclear_mults);
		}
	}

}
