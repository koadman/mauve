package org.gel.mauve.operon;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.biojava.bio.seq.StrandedFeature;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.analysis.output.AbstractTabbedDataWriter;

public class OperonWriter extends AbstractTabbedDataWriter implements MauveConstants {
	
	protected Operon first;
	protected Operon operon;
	protected Iterator <StrandedFeature> genes;
	protected StrandedFeature gene;
	protected int operon_count;

	public OperonWriter(String file, Hashtable args) {
		super(file, args);
		printHeaders ();
		printData ();
		doneWritingFile ();
	}
	
	protected void initSubClassParticulars (Hashtable args) {
		super.initSubClassParticulars(args);
		operon = first = (Operon) args.get(MauveConstants.FIRST_OPERON);
	}

	@Override
	protected String getData(int column, int row) {
		switch (column) {
			case 0:
				return operon_count + "";
			case 1:
				return MauveHelperFunctions.getAsapID(gene);
			case 2:
				return gene.getLocation().getMin() + "";
			case 3:
				return gene.getLocation().getMax() + "";
			case 4:
				return gene.getStrand().toString();
			case 5:
				return operon.distances.get(operon.genes.indexOf(gene)) + "";
		}
		return null;
	}

	protected boolean moreRowsToPrint() {
		return !(operon.next == first && !genes.hasNext ());
	}

	protected Vector setColumnHeaders() {
		Vector <String> headers = new Vector <String> ();
		headers.add("operon id");
		headers.add (MauveConstants.DB_XREF);
		headers.add(MauveConstants.LEFT_STRING);
		headers.add(MauveConstants.RIGHT_STRING);
		headers.add(MauveConstants.STRAND_STRING);
		headers.add("distance");
		return headers;
	}

	protected boolean shouldPrintRow(int row) {
		if (genes == null || !genes.hasNext()) {
			operon = operon.next;
			if (operon == null)
				System.out.println ("null operon");
			genes = operon.genes.iterator();
			++operon_count;
		}
		gene = genes.next();
		return true;
	}

}
