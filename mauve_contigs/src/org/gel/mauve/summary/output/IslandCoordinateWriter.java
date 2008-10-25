package org.gel.mauve.summary.output;

import java.util.Vector;

import org.gel.mauve.MauveConstants;

public class IslandCoordinateWriter extends AbstractIslandWriter implements
		MauveConstants {

	public IslandCoordinateWriter (SegmentDataProcessor processor) {
		super ("islandcoords", processor);
	}

	// probably not quite right currently
	public void printHeaderInfoForIslandFile () {
		out.println ("Sequence " + reference + " is the reference sequence.");
		out.println ("For each island, thes left and right coordinate are shown"
						+ " for each sequence");
		out.println ("If the island is complementary in direction to the "
				+ "reference sequence, a negative sign is shown before the "
				+ "coordinates for that sequence");
		out.println ("Ignores islands segments under " + island_min
				+ " bp and backbone segments under " + backbone_min + " bp.");
	}

	public void printIslands () {
		printHeaders ();
		printData (BY_ALL_AND_BB);
	}
	
	protected String getData (int col, int row) {
		if (col == headers.length - 1)
			return current.typed_id;
		else
			return super.getData (col, row);
	}

	protected boolean shouldPrintRow (int row) {
		boolean ok = false;
		if (by_genome) {
			if (current.multiplicityType () == multiplicity)
				ok = true;
		} else if (current.multiplicityType () != all_seq_multiplicity)
			ok = true;
		ok = ok && (current.getAvgSegmentLength () > island_min);
		return ok;
	}
	
	public Vector setColumnHeaders () {
		Vector cols = super.setColumnHeaders ();
		cols.add (LABEL_STRING);
		return cols;
	}

}
