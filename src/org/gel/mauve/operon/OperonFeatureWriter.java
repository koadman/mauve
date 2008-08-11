package org.gel.mauve.operon;

import java.util.Hashtable;
import java.util.Vector;

import org.gel.air.util.GroupUtils;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.analysis.Segment;
import org.gel.mauve.analysis.output.AbstractTabbedDataWriter;
import org.gel.mauve.gui.sequence.FlatFileFeatureConstants;

public class OperonFeatureWriter extends AbstractTabbedDataWriter implements
		MauveConstants, FlatFileFeatureConstants {

	public static final int MULTIPLICITY_INDEX = 6;
	public static final String ISLAND = "island";
	protected int count;
	protected Operon first;
	protected Operon current;
	
	protected OperonFeatureWriter (String file, Hashtable args) {
		super (file, args);
		printOperonFeatures ();
	}

	protected void initSubClassParticulars (Hashtable args) {
		first = current = (Operon) args.get(FIRST_OPERON);
		super.initSubClassParticulars (args);
	}

	protected boolean moreRowsToPrint() {
		current = current.next;
		count++;
		return !(current == first);
	}
	
	public void printOperonFeatures () {
		printHeaders ();
		printData ();
		doneWritingFile ();
	}

	protected String getData (int column, int row) {
		long value = 0;
		switch (column) {
			case TYPE:
				return OPERON_STRING;
			case LABEL:
				return count + "";
			case CONTIG:
				return "Chromosome";
			case STRAND:
				return current.genes.getFirst ().getStrand ().toString ();
			case LEFT:
				value = current.genes.getFirst ().getLocation().getMin();
				break;
			case RIGHT:
				value = current.genes.getLast ().getLocation().getMax();
				break;
			default:
				return null;
		}
		return value + "";
	}

	public Vector setColumnHeaders () {
		String [] cols = new String [] {TYPE_STRING, LABEL_STRING,
				CONTIG_STRING, STRAND_STRING, LEFT_STRING, RIGHT_STRING};
		Vector vect = new Vector ();
		GroupUtils.arrayToCollection (vect, cols);
		return vect;
	}


	public boolean shouldPrintRow (int row) {
		return true;
	}

}

