package org.gel.mauve.contigs;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.gel.mauve.Chromosome;
import org.gel.mauve.gui.sequence.FlatFileFeatureConstants;
import org.gel.mauve.summary.output.AbstractTabbedDataWriter;

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
	protected HashSet inverted;
	
	public ContigFeatureWriter (String file, Hashtable args) {
		super(file, args);
	}
	
	protected void initSubClassParticulars (Hashtable args) {
		inverters = (Hashtable) args.get (REVERSES);
		Vector sorted = new Vector ((inverters).keySet ());
		Collections.sort (sorted);
		use = inverters;
		inverted = (HashSet) args.get(COMPLEMENT);
		super.initSubClassParticulars (args);
		correctPseudoCoords ((LinkedList) args.get (ORDERED_CONTIGS));
		printContigs (sorted, REVERSES);
		printContigs ((LinkedList) args.get (ORDERED_CONTIGS), ORDERED_CONTIGS);
		use = (Hashtable) args.get (CONFLICTED_CONTIGS);
		if (use != null &&  use.size () > 0)
			printContigs (use.keySet (), CONFLICTED_CONTIGS);
		doneWritingFile ();
	}
	
	protected void correctPseudoCoords (LinkedList chroms) {
		long start = 1;
		for (int i = 0; i < chroms.size(); i++) {
			Chromosome old = (Chromosome) chroms.get(i);
			Chromosome chrom = new Chromosome (start, start + old.getLength() - 1,
					old.getName(), old.getCircular());
			chroms.set(i, chrom);
			start = chrom.getEnd() + 1;
		}
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
				boolean invert;
				if (inverted != null)
					invert = inverted.contains(current.getName());
				else
					invert = inverters.containsKey (current.getName());
				return invert ? COMPLEMENT : FORWARD;
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
	
	public void printHeaderInfoForFile () {
		out.println ("Contigs in Reversed Category are those reversed from " + 
				"the order immediately preceding.\n  The strand is forward if the " +
				"contig is oriented the same as the original input,\nand complement" +
				" otherwise.  The left and right ends are in\npseudomolecule " +
				"coordinates.  The ordered contigs contain all contigs in the " +
				"correct order,\n and those in the conflicted category had multiple "
				+ "possible orders.");
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
