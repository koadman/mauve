package org.gel.mauve.contigs;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.gel.mauve.Chromosome;
import org.gel.mauve.analysis.output.AbstractTabbedDataWriter;
import org.gel.mauve.gui.sequence.FlatFileFeatureConstants;

public class ContigFeatureWriter extends AbstractTabbedDataWriter 
		implements FlatFileFeatureConstants {

	public static final String REVERSES = "Contigs to reverse";
	public static final String ORDERED_CONTIGS = "Ordered Contigs";
	public static final String CONFLICTED_CONTIGS = 
			"Contigs with conflicting ordering information";
	protected Iterator contigs;
	protected Chromosome current;
	protected Hashtable inverters;
	protected Hashtable use;
	
	public ContigFeatureWriter (String file, Hashtable args) {
		super(file, args);
	}
	
	protected void initSubClassParticulars (Hashtable args) {
		inverters = (Hashtable) args.get (REVERSES);
		Vector sorted = new Vector ((inverters).keySet ());
		Collections.sort (sorted);
		use = inverters;
		super.initSubClassParticulars (args);
		printContigs (sorted, REVERSES);
		printContigs ((LinkedList) args.get (ORDERED_CONTIGS), ORDERED_CONTIGS);
		use = (Hashtable) args.get (CONFLICTED_CONTIGS);
		if (use != null &&  use.size () > 0)
		printContigs (use.keySet (), CONFLICTED_CONTIGS);
		doneWritingFile ();
	}

	protected void printContigs (Collection conts, String descriptor) {
		System.out.println ("printing");
		if (conts.size() > 0) {
			contigs = conts.iterator ();
			out.println (descriptor);
			printHeaders ();
			printData ();
			out.println ("\n");
		}
	}

	protected String getData (int column, int row) {
		switch (column) {
			case TYPE:
				return CONTIG_STRING;
			case LABEL:
				return current.getName ();
			case CONTIG:
				return "chromosome";
			case STRAND:
				return FORWARD;
			case LEFT:
				return current.getStart () + "";
			case RIGHT:
				return current.getEnd () + "";
			/*case LEFT:
				return 1 + "";
			case RIGHT:
				return current.relativeLocation (current.getEnd ()) + "";*/
			default:
				return null;
		}
	}

	protected boolean moreRowsToPrint () {
		return contigs.hasNext ();
	}

	protected Vector setColumnHeaders () {
		return null;
	}
	public void setColumnHeaders (Vector v) {
		headers = FLAT_FEATURE_REQ_INFO;
		current_row = new String [headers.length];
	}

	protected boolean shouldPrintRow (int row) {
		Object obj = contigs.next ();
		if (obj instanceof Chromosome)
			current = (Chromosome) obj;
		else
			current = (Chromosome) use.get (obj);
		return true;
	}

}
