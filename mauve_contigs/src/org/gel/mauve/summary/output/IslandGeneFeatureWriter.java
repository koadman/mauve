package org.gel.mauve.summary.output;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;
import org.gel.air.util.MathUtils;
import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.summary.Segment;

public class IslandGeneFeatureWriter extends IslandFeatureWriter {
	
	public static final String PERCENT = "prct_on_is";
	public static final int ISLAND_COL = -2;
	public static final int PERCENT_COL = -1;
	public static final String BACKBONE_MASK = "backbone_mask";
	public static StringBuffer ids = new StringBuffer ();
	public static int buffer_count;
	
	protected BaseViewerModel model;
	protected ListIterator iterator;
	protected Feature cur_feat;
	protected double cur_percent;
	protected double minimum_percent;
	protected int [][] num_per_multiplicity;
	protected boolean backbone_instead;
	protected int [] num_features;
	
	public static final String ISLAND_GENE = "island_gene";
	
	
	protected IslandGeneFeatureWriter (SegmentDataProcessor processor) {
		super (MauveHelperFunctions.getSeqPartOfFile (processor) + (processor.get (
				BACKBONE_MASK) != null ? "backbone" : "island") + "_genes", processor);
	}
	
	protected void initSubClassParticulars (Hashtable args) {
		super.initSubClassParticulars (args);
		model = (BaseViewerModel) args.get (MODEL);
		if (args.get (MINIMUM_PERCENT_CONTAINED) != null)
			minimum_percent = ((Double) args.get (
					MINIMUM_PERCENT_CONTAINED)).doubleValue ();
		else {
			minimum_percent = DEFAULT_MIN_PERCENT_CONTAINED;
			args.put (MINIMUM_PERCENT_CONTAINED, new Double (minimum_percent));
		}
		if (args.get (BACKBONE_MASK) != null)
			backbone_instead = true;
		num_per_multiplicity = ((int [][]) args.get (NUM_GENES_PER_MULT));
		Iterator itty = MauveHelperFunctions.getFeatures (model, seq_index);
		Vector vector = new Vector ();
		while (itty.hasNext ())
			vector.add (itty.next ());
		Collections.sort (vector, MauveHelperFunctions.FEATURE_COMPARATOR);
		num_features = (int []) args.get (TOTAL_GENES);
		num_features [seq_index] = vector.size ();
		System.out.println ("seq: " + seq_index + " features: " + num_features [seq_index]);
		iterator = vector.listIterator ();
		if (iterator.hasNext ())
			cur_feat = (Feature) iterator.next ();
	}
	
	public Vector setColumnHeaders () {
		Vector vect = super.setColumnHeaders ();
		vect.remove (vect.size () - 1);
		vect.add (0, PERCENT);
		vect.add (0, ISLAND);
		vect.add (Segment.MULTIPLICITY_STRING);
		return vect;
	}
	
	protected String getData (int col, int row) {
		col -= 2;
		Location loci = cur_feat.getLocation ();
		long value = 0;
		switch (col) {
			case ISLAND_COL:
				return current.typed_id;
			case PERCENT_COL:
				return MauveHelperFunctions.doubleToString (cur_percent, 2);
			case TYPE:
				return ISLAND_GENE;
			case LABEL:
				String id = MauveHelperFunctions.getUniqueId (cur_feat);
				if (!backbone_instead && current.multiplicityType () < multiplicity << 1) {
					buffer_count++;
					ids.append (id.substring (5));
					ids.append (',');
				}
				return id;
			case CONTIG:
				return contig_handler.getContigName (seq_index, loci.getMin ());
			case STRAND:
				if (!(cur_feat instanceof StrandedFeature))
					System.out.println ("bad cast");
				return ((StrandedFeature) cur_feat).getStrand () == 
					StrandedFeature.NEGATIVE ? COMPLEMENT : FORWARD;
			case LEFT:
				value = loci.getMin ();
				break;
			case RIGHT:
				value = loci.getMax ();
				break;
			case MULTIPLICITY_INDEX:
				String mult = MauveHelperFunctions.getReadableMultiplicity (current);
				performComplexIteration ();
				return mult;
		}
		return adjustForContigs (seq_index, value) + "";
	}
	
	public boolean badType (Feature feat) {
		String type = feat.getType ().toLowerCase ();
		if (type.indexOf ("rna") > -1 || type.indexOf ("gene") > -1 || 
				type.indexOf ("cds") > -1 || type.indexOf ("asap") > -1)
			return false;
		else {
			num_features [seq_index]--;
			return true;
		}
	}
	
	public void printData () {
		if (cur_feat != null)
			super.printData();
	}

	public boolean shouldPrintRow (int row) {
		Location loci = cur_feat.getLocation ();
		boolean print = false;
		while ((badType (cur_feat) || loci.getMax () <= current.starts [seq_index]) &&
				iterator.hasNext ()) {
			//cur_feat = (Feature) iterator.next ();
			performComplexIteration ();
			if (cur_feat != null)
				loci = cur_feat.getLocation ();
			else
				loci = null;
		}
		if (loci != null && shouldPrintSegment (row) && loci.getMin () < 
				current.ends [seq_index]) {
			if (cur_feat instanceof StrandedFeature) {
				cur_percent = MathUtils.percentContained (loci.getMin (), loci.getMax (), 
						current.starts [seq_index], current.ends [seq_index]);
				if (!(cur_percent >= minimum_percent)) {
					if (loci.getMax () < current.ends [seq_index] || 
							current.nexts [seq_index] == Segment.END) {
						performComplexIteration ();
					}
					else {
						current = current.nexts [seq_index];
					}
				}
				else {
					print = true;
					num_per_multiplicity [seq_index][(int) current.multiplicityType () - 1] += 1;
				}
			}
		}
		return print;
	}
	
	protected void performComplexIteration () {
		cur_feat = iterator.hasNext () ? (Feature) iterator.next () : null;
		while (cur_feat != null && cur_feat.getLocation ().getMin () < 
				current.starts [seq_index] && current.prevs [seq_index] != Segment.END)
			current = current.prevs [seq_index];
	}
	
	protected boolean shouldPrintSegment (int row) {
		if (!backbone_instead)
			return super.shouldPrintRow (row);
		else
			return current.multiplicityType () == all_seq_multiplicity ? true : false;
	}
	
	protected boolean moreRowsToPrint () {
		if (cur_feat == null)
			return false;
		Location loci = cur_feat.getLocation ();
		if (loci.getMin () >= current.ends [seq_index] || !shouldPrintSegment (row_number))
			return super.moreRowsToPrint ();
		else
			return true;
	}
	
	public static void printIslandsAsFeatures (SegmentDataProcessor processor) {
		int count = ((Object []) processor.get (FIRSTS)).length;
		long all_mult = ((Long) processor.get (ALL_MULTIPLICITY)).longValue ();
		processor.put (NUM_GENES_PER_MULT, new int [count][(int) all_mult]);
		processor.put (TOTAL_GENES, new int [count]);
		for (int i = 0; i < count; i++) {
			processor.put (SEQUENCE_INDEX, new Integer (i));
			new IslandGeneFeatureWriter (processor);
			if (i == count - 1 && processor.get (BACKBONE_MASK) == null) {
				processor.put (BACKBONE_MASK, new Object ());
				i = -1;
			}
		}
	}

}
