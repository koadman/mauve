package org.gel.mauve.summary.output;

import java.util.Hashtable;
import java.util.Vector;

import org.gel.air.util.GroupHelpers;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.analysis.Segment;
import org.gel.mauve.gui.sequence.FlatFileFeatureConstants;

public class IslandFeatureWriter extends AbstractIslandWriter implements
		MauveConstants, FlatFileFeatureConstants {

	public static final int MULTIPLICITY_INDEX = 6;
	public static final String ISLAND = "island";

	protected IslandFeatureWriter (SegmentDataProcessor processor) {
		super (MauveHelperFunctions.getSeqPartOfFile (processor) + "islands", processor);
	}
	
	protected IslandFeatureWriter (String file, SegmentDataProcessor processor) {
		super (file, processor);
	}

	protected void initSubClassParticulars (Hashtable args) {
		seq_index = ((Integer) args.get (SEQUENCE_INDEX)).intValue ();
		super.initSubClassParticulars (args);
	}

	public void printIslands () {
		printHeaders ();
		printData (BY_ONE_GENOME);
	}

	protected String getData (int column, int row) {
		long value = 0;
		switch (column) {
			case TYPE:
				return ISLAND;
			case LABEL:
				return current.typed_id;
			case CONTIG:
				return contig_handler.getContigName (seq_index, current.starts [seq_index]);
			case STRAND:
				return current.reverse [seq_index] ? COMPLEMENT : FORWARD;
			case LEFT:
				value = current.starts [seq_index];
				break;
			case RIGHT:
				value = current.ends [seq_index];
				break;
			case MULTIPLICITY_INDEX:
				return MauveHelperFunctions.getReadableMultiplicity (current);
			default:
				return null;
		}
		return adjustForContigs (seq_index, value) + "";
	}

	public Vector setColumnHeaders () {
		String [] cols = new String [] {TYPE_STRING, LABEL_STRING,
				CONTIG_STRING, STRAND_STRING, LEFT_STRING, RIGHT_STRING,
				Segment.MULTIPLICITY_STRING};
		Vector vect = new Vector ();
		GroupHelpers.arrayToCollection (vect, cols);
		return vect;
	}

	public static void printIslandsAsFeatures (SegmentDataProcessor processor) {
		int count = ((Object []) processor.get (FIRSTS)).length;
		for (int i = 0; i < count; i++) {
			processor.put (SEQUENCE_INDEX, new Integer (i));
			new IslandFeatureWriter (processor);
		}
	}

	public boolean shouldPrintRow (int row) {
		long cur = current.multiplicityType ();
		return (cur & multiplicity) == multiplicity && cur != all_seq_multiplicity &&
				current.getSegmentLength (seq_index) > island_min;
	}

}
