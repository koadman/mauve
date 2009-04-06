package org.gel.mauve.analysis.output;

import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.gel.air.util.MathUtils;
import org.gel.mauve.Match;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.analysis.Segment;
import org.gel.mauve.analysis.SegmentComparator;
import org.gel.mauve.gui.sequence.FlatFileFeatureConstants;

public class TroubleMatchWriter extends AbstractMatchDataWriter implements
		MauveConstants {

	protected double avg_length;

	protected double ratio;

	protected int length_column;

	protected int percent_column;

	public static final String RATIO = "diff_to_lngth";

	public TroubleMatchWriter (SegmentDataProcessor processor) {
		super ("problembb", processor);
		findOddLengthedMatches ();
		doneWritingFile ();
	}

	protected void initSubClassParticulars (Hashtable args) {
		super.initSubClassParticulars (args);
		length_column = count * (contig_handler instanceof AbstractMatchDataWriter ? 2 : 3);
		percent_column = length_column + 1;
	}

	// probably not quite right currently
	public void printHeaderInfoForFile () {
		out.println ("Sequence " + reference + " is the reference sequence.");
		out
				.println ("Each backbone segment with unclear information will be printed");
		out.println ("If the segment is complementary in direction to the "
				+ "reference sequence, a negative sign is shown before the "
				+ "coordinates for that sequence");
	}

	public void findOddLengthedMatches () {
		out.println ("identifying odd lengthed matches");
		printHeaders ();
		printData (BY_BB_LIST);
	}

	public boolean shouldPrintRow (int row) {
		long [] lengths = current.getSegmentLengths ();
		if (lengths.length == 0)
			return false;
		int contains = 0;
		Arrays.sort (lengths);
		for (int i = 0; i < count; i++) {
			if (lengths[i] != 0)
				contains++;
		}
		if (contains == 0)
			return false;
		long difference = lengths[count - 1] - lengths[count - contains];
		avg_length = current.getAvgSegmentLength ();
		ratio = difference / avg_length;
		if (avg_length > backbone_min && ratio > max_length_ratio)
			return true;
		else
			return false;
	}

	public Vector setColumnHeaders () {
		Vector titles = super.setColumnHeaders ();
		titles.add (AVERAGE_LENGTH);
		titles.add (RATIO);
		return titles;
	}

	protected String getData (int col, int row) {
		if (col < count * (contig_handler instanceof AbstractMatchDataWriter ? 2 : 3))
			return super.getData (col, row);
		else {
			double data;
			int decimals = 4;
			if (col == length_column) {
				data = avg_length;
				decimals = 1;
			} else
				data = ratio;
			return MathUtils.doubleToString (data, decimals);
		}

	}

}
