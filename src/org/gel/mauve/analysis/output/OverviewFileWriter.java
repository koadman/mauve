package org.gel.mauve.analysis.output;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import org.gel.mauve.MauveConstants;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.analysis.Segment;

public class OverviewFileWriter extends AbstractTabbedDataWriter implements MauveConstants {
	
	protected SegmentDataProcessor processor;
	
	public final static String NUMBER_GENES = "num_genes";
	public final static String NUMBER_ISLANDS = "num_islands";
	public final static String NUM_BASE_PAIRS = "num_bp";
	public final static String PERCENT_TOTAL = "percent";
	public final static String UNKNOWN = "unknown";
	
	protected int [][] gene_data;
	protected int [] island_data;
	protected long [] bp_data;
	protected long [] long_totals;
	protected double [] double_totals;
	
	protected int [] num_genes;
	protected int [] num_segments;
	protected long [] lengths;
	protected Segment [] firsts;
	
	protected int sequence;
	protected int total;
	protected long cur_multiplicity;
	protected int min_size;
	
	public OverviewFileWriter (SegmentDataProcessor proc) {
		super (proc.get (SegmentDataProcessor.FILE_STUB) + "_overview.tab", proc);
	}
	
	protected void initSubClassParticulars (Hashtable args) {
		processor = (SegmentDataProcessor) args;
		setMinSize ();
		firsts = (Segment []) processor.get (FIRSTS);
		if (processor.get (GENOME_LENGTHS) != null)
			lengths = (long []) processor.get (GENOME_LENGTHS);
		num_genes = (int []) args.get (TOTAL_GENES);
		num_segments = new int [num_genes.length];
		gene_data = (int [][]) processor.get (NUM_GENES_PER_MULT);
		System.out.println ("num_genes: " + gene_data [0].length);
		island_data = new int [gene_data [0].length];
		bp_data = new long [gene_data [0].length];
		row_number = gene_data [0].length + 2;
		sequence = -1;
		cur_multiplicity = ((Long) args.get (ALL_MULTIPLICITY)).longValue () + 1;
		super.initSubClassParticulars (args);
		writeHeaderInfo ();
		printGeneInformation ();
		doneWritingFile ();
	}
	
	protected void setMinSize () {
		int isl_min = ((Integer) processor.get (ISLAND_MIN)).intValue();
		int bb_min = ((Integer) processor.get (BACKBONE_MIN)).intValue();
		min_size = Math.min(isl_min, bb_min);
	}
	
	protected void performCalculations () {
		Segment seg = firsts [sequence];
		do {
			num_segments [sequence] += 1;
			if (seg.getSegmentLength (sequence) > min_size) {
				island_data [(int) seg.multiplicityType () - 1] += 1;
				bp_data [(int) seg.multiplicityType () - 1] += seg.getSegmentLength (sequence);
			}
			seg = seg.nexts [sequence];
		} while (seg != Segment.END);
	}

	public void writeHeaderInfo () {
		try {
			out.println ("Sequence " + processor.get (REFERENCE) + 
					" is the reference sequence.");
			out.println ("Island minimum: " + processor.get (ISLAND_MIN));
			out.println ("Backbone minimum: " + processor.get (BACKBONE_MIN));
			out.println ("Minimum length ratio considered a problem: " + 
					processor.get (MAX_LENGTH_RATIO));
			out.println ("Ratio represents the difference in length between the " +
					"longest and shortest pieces over the average length.");
			out.println ("Minimum percent of gene that must be on island: " + 
					processor.get (MINIMUM_PERCENT_CONTAINED));
			out.println ("File explanations: ");
			out.println ("_islandscoords.mo contains island id and coordinate information " +
					"for all islands in all sequences");
			out.println ("_problembb.mo contains backbone segments whose lengths vary" +
					"widely between sequences.");
			out.println ("_islands contains information on all the islands in a particular sequence." +
					"\nIt can be loaded into Mauve as features.  A file is generated per sequence");
			out.println ("_island_genes contains similar information as _islands, but"
					+ " by gene\n");
		} catch (Exception e) {
			System.out.println ("Couldn't write overview file.");
			e.printStackTrace ();
		}
	}
	
	public void printGeneInformation () {
		printHeaders ();
		moreRowsToPrint ();
		printData ();
		out.println (IslandGeneFeatureWriter.buffer_count);
		out.println (IslandGeneFeatureWriter.ids);
	}
	
	protected String getData (int column, int row) {
		if (row < island_data.length) {
			double percent = -1;
			long count = -1;
			switch (column) {
				case 0:
					return MauveHelperFunctions.getReadableMultiplicity (row + 1, 
							num_genes.length);
				case 1:
					if (row == island_data.length - 1 && gene_data [sequence][row] == 0)
						gene_data [sequence][row] = num_genes [sequence] - total;
					else
						total += gene_data [sequence][row];
					count = gene_data [sequence][row];
					break;
				case 2:
					percent = gene_data [sequence][row] / (double) num_genes [sequence];
					break;
				case 3:
					count = island_data [row];
					break;
				case 4:
					percent = island_data [row] / (double) num_segments [sequence];
					break;
				case 5:
					count = bp_data [row];
					break;
				case 6:
					percent = bp_data [row] / (double) lengths [sequence];
					break;
			}
			if (percent != -1) {
				double_totals [column] += percent;
				return MauveHelperFunctions.doubleToString (percent * 100, 1);
			}
			else if (count != -1) {
				long_totals [column] += count;
				return count + "";
			}
		}
		else if (row == island_data.length){
			switch (column) {
				case 0:
					return TOTALS;
				case 1:
					return num_genes [sequence] + "";
				case 2:
					return "100";
				case 3:
					return num_segments [sequence] + "";
				case 4:
					return "100";
				case 5:
					return lengths [sequence] + "";
				case 6:
					return "100";
				default:
					return null;
			}
		}
		else {
			if (column == 0)
				return "unknown";
			if (column % 2 == 1) {
				long tot = (column == 1) ? num_genes [sequence] : 
					((column == 3) ? num_segments [sequence] : lengths [sequence]);
				return (tot - long_totals [column]) + "";
			}
			else
				return MauveHelperFunctions.doubleToString (100 - (
						double_totals [column] * 100), 1);
		}
		return null;
	}

	protected boolean moreRowsToPrint () {
		if (row_number == island_data.length + 2) {
			if (sequence == num_genes.length - 1) 
				return false;
			else {
				total = 0;
				sequence++;
				cur_multiplicity >>= 1;
				row_number = 0;
				if (sequence > 0) {
					Arrays.fill (island_data, 0);
					Arrays.fill (bp_data, 0);
					Arrays.fill (long_totals, 0);
					Arrays.fill (double_totals, 0);
				}
				performCalculations ();
				out.println ("Sequence " + sequence + ":");
				return true;
			}
		}
		else
			return true;
	}

	protected Vector setColumnHeaders () {
		Vector titles = new Vector ();
		titles.add (Segment.MULTIPLICITY_STRING);
		titles.add (NUMBER_GENES);
		titles.add (PERCENT_TOTAL);
		titles.add (NUMBER_ISLANDS);
		titles.add (PERCENT_TOTAL);
		titles.add (NUM_BASE_PAIRS);
		titles.add (PERCENT_TOTAL);
		long_totals = new long [titles.size()];
		double_totals = new double [long_totals.length];
		return titles;
	}

	protected boolean shouldPrintRow (int row) {
		if (row == island_data.length + 1 || row == island_data.length || (/*gene_data [sequence][row] != 0 &&*/
				((row + 1) & cur_multiplicity) == cur_multiplicity)) {
			return true;
		}
		else {
			return false;
		}
	}

}
