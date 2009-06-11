package org.gel.mauve.contigs;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.StrandedFeature;
import org.gel.air.util.GroupUtils;
import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.analysis.output.AbstractTabbedDataWriter;
import org.gel.mauve.gui.sequence.FlatFileFeatureConstants;

public class ChangedFeatureWriter extends AbstractTabbedDataWriter 
		implements FlatFileFeatureConstants {
	
	protected Iterator feats;
	protected Feature feature;
	protected Genome genome;
	protected Chromosome chrom;
	//protected Hashtable inverters;
	protected FeatureReverser reverser;
	public static final int CONT_IND = 0;
	public static final int LAB_IND = 1;
	public static final int STR_IND = 2;
	public static final int LEF_IND = 3;
	public static final int RIGHT_IND = 4;
	public static final int OLD_L_IND = 5;
	public static final int OLD_R_IND = 6;
	public static final int CHANGED_IND = 7;
	public static final int TYPE_IND = 8;
	public static final String OLD_L_STR = "prev_left";
	public static final String OLD_R_STR = "prev_right";
	public static final String REVERSED = "reversed";

	public ChangedFeatureWriter (String file, Hashtable args, Iterator itty, Genome fix) {
		super (file, args);
		feats = itty;
		if (!itty.hasNext ())
			return;
		genome = fix;
		printHeaders ();
		printData ();
		doneWritingFile ();
	}
	
	protected void initSubClassParticulars (Hashtable args) {
		reverser = (FeatureReverser) args.get (ContigFeatureWriter.REVERSES);
		super.initSubClassParticulars(args);
	}

	protected String getData (int column, int row) {
		long ret = 0;
		boolean changed = reverser.isReversed(feature, chrom, genome);
		boolean rev = changed;
		if (feature.getAnnotation ().containsProperty (REVERSED)) {
			rev = new Boolean ((String) feature.getAnnotation ().getProperty (
					REVERSED)).booleanValue ();
			if (changed)
				rev = !rev;
		}
		if (!changed && (column == LEF_IND || column == RIGHT_IND))
			column += 2;
		switch (column) {
			case CONT_IND:
				return chrom.getName ();
			case LAB_IND:
				return MauveHelperFunctions.getUniqueId (feature);
			case STR_IND:
				return ((StrandedFeature) feature).getStrand () == (changed ?
					StrandedFeature.NEGATIVE : StrandedFeature.POSITIVE) ? FORWARD : COMPLEMENT;
			case LEF_IND:
			case OLD_R_IND:
				ret = feature.getLocation ().getMax ();
				break;
			case RIGHT_IND:
			case OLD_L_IND:
				ret = feature.getLocation ().getMin (); 
				break;
			case CHANGED_IND:
				return rev + "";
			case TYPE_IND:
				return "asap";
		}
		ret = ret - chrom.getStart () + 1;
		if (changed && column < OLD_L_IND)
			ret = reverser.reverseStart (feature, chrom, genome) + 
			reverser.reverseEnd(feature, chrom, genome) - ret;
		return ret + "";
	}

	protected boolean moreRowsToPrint () {
		return feats.hasNext ();
	}

	protected Vector setColumnHeaders () {
		String [] headers = {CONTIG_STRING, LABEL_STRING, STRAND_STRING,
				LEFT_STRING, RIGHT_STRING, OLD_L_STR, OLD_R_STR, REVERSED, TYPE_STRING};
		Vector heads = new Vector (headers.length);
		GroupUtils.arrayToCollection (heads, headers);
		return heads;
	}

	protected boolean shouldPrintRow (int row) {
		feature = (Feature) feats.next ();
		chrom = genome.getChromosomeAt (feature.getLocation ().getMin ());
		return true;
	}
	
	public interface FeatureReverser {
		
		public boolean isReversed (Feature feat, Chromosome chrom, Genome gen);
		
		public long reverseStart (Feature feat, Chromosome chrom, Genome gen);
		
		public long reverseEnd (Feature feat, Chromosome chrom, Genome gen);
	}

}
