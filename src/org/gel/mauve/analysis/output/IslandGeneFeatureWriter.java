package org.gel.mauve.analysis.output;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Vector;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;
import org.gel.air.bioj.BioJavaUtils;
import org.gel.air.util.MathUtils;
import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.analysis.Segment;

public class IslandGeneFeatureWriter extends IslandFeatureWriter {
	
	public static final String PERCENT = "prct_on_is";
	public static final int ISLAND_COL = -2;
	public static final int PERCENT_COL = -1;
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
	
	
	protected IslandGeneFeatureWriter (SegmentDataProcessor processor, 
			String file_tail) {
		this ((processor.get (
				BACKBONE_MASK) != null ? "backbone" : "island") + "_" +
				file_tail, processor);
	}
	
	protected IslandGeneFeatureWriter (String file_part, SegmentDataProcessor proc) {
		super (file_part, proc);
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
		System.out.println ("minPrc " + minimum_percent);
		if (args.get (BACKBONE_MASK) != null)
			backbone_instead = true;
		num_per_multiplicity = ((int [][]) args.get (NUM_GENES_PER_MULT));
		Iterator itty = MauveHelperFunctions.getFeatures (model, seq_index);
		Vector vector = new Vector ();
		while (itty.hasNext ())
			vector.add (itty.next ());
		Collections.sort (vector, BioJavaUtils.FEATURE_START_COMPARATOR);
		num_features = (int []) args.get (TOTAL_GENES);
		num_features [seq_index] = vector.size ();
		iterator = vector.listIterator ();
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
				if (id.length() > 5 && !backbone_instead && current.multiplicityType () < multiplicity << 1) {
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
	
	/**
	 * determines if feasture is an appropriate type for consideration to print
	 * If not, decrements the count of num_features for this sequence
	 * 
	 * @param feat
	 * @return
	 */
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
		if (iterator.hasNext())
			super.printData();
	}
	
	public boolean shouldPrintRow (int row) {
		Location loci = null;
		boolean print = false;
		if (cur_feat != null)
			loci = cur_feat.getLocation ();
		if (loci != null) {
			if (cur_feat instanceof StrandedFeature) {
				double running = 0;
				final Hashtable <Segment, Double> mults = new Hashtable <
						Segment, Double> ();
				Hashtable <Long, Segment> segs = new Hashtable <
						Long, Segment> ();
				Segment add = current;
				while (add != Segment.END && 
						add.getStart(seq_index) < loci.getMax()) { 
					if (segs.containsKey(add.multiplicityType())) {
						cur_percent = mults.get(segs.get(add.multiplicityType()));
					}
					else {
						cur_percent = 0;
						segs.put(add.multiplicityType (), add);
					}
					cur_percent += MathUtils.percentContained (loci.getMin (), loci.getMax (), 
							add.starts [seq_index], add.ends [seq_index]);
					mults.put(segs.get(add.multiplicityType()), cur_percent);
					add = add.getNext(seq_index);
				}
				ArrayList <Segment> keys = new ArrayList <Segment> (mults.keySet());
				Collections.sort(keys, new Comparator <Segment> () {
					public int compare (Segment one, Segment two) {
						int val = MULT_COMP.compare(one, two);
						if (val == 0) {
							double temp = mults.get(one) - mults.get(two);
							if (temp < 0)
								val = -1;
							else if (temp > 0)
								val = 1;
						}
						return val;
					}
				});
				for (int i = 0; i < keys.size (); i++) {
					if (mults.get(keys.get(i)) > minimum_percent) {
						current = keys.get(i);
						if (shouldPrintSegment (row)) {
							cur_percent = mults.get (current);
							num_per_multiplicity [seq_index][(int)
							     current.multiplicityType () - 1] += 1;
							print = true;
							break;
						}
					}
				}
			}
		}
		if (!print)
			performComplexIteration ();
		return print;
	}
	
	protected void performComplexIteration () {
		do {
			cur_feat = iterator.hasNext () ? (Feature) iterator.next () : null;
		} while (cur_feat != null && badType (cur_feat));
		if (cur_feat != null) {
			while (cur_feat.getLocation ().getMin () < 
					current.starts [seq_index] && current.prevs [seq_index] != Segment.END) {
				current = current.prevs [seq_index];
			}
			while (cur_feat.getLocation().getMin() > current.getEnd(seq_index) &&
					current.getNext(seq_index) != Segment.END) {
				current = current.getNext(seq_index);
			}
		}
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
		else
			return true;
	}
	
	public static void printIslandsAsFeatures (SegmentDataProcessor processor) {
		initializeVars (processor);
		int count = ((Object []) processor.get (FIRSTS)).length;
		for (int i = 0; i < count; i++) {
			processor.put (SEQUENCE_INDEX, new Integer (i));
			new IslandGeneFeatureWriter (processor, "genes");
			if (i == count - 1 && processor.get (BACKBONE_MASK) == null) {
				processor.put (BACKBONE_MASK, new Object ());
				i = -1;
			}
		}
	}
	
	public static void initializeVars (SegmentDataProcessor processor) {
		int count = ((Object []) processor.get (FIRSTS)).length;
		long all_mult = ((Long) processor.get (ALL_MULTIPLICITY)).longValue ();
		processor.put (NUM_GENES_PER_MULT, new int [count][(int) all_mult]);
		processor.put (TOTAL_GENES, new int [count]);
	}

}
