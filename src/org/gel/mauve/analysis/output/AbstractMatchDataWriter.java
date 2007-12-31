package org.gel.mauve.analysis.output;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import org.gel.mauve.MauveConstants;
import org.gel.mauve.analysis.Segment;
import org.gel.mauve.analysis.SegmentComparator;
import org.gel.mauve.contigs.ContigHandler;
import org.gel.mauve.gui.sequence.FlatFileFeatureConstants;

abstract public class AbstractMatchDataWriter extends AbstractTabbedDataWriter
		implements FlatFileFeatureConstants, ContigHandler {

	protected boolean shouldPrintRow (int row) {
		// TODO Auto-generated method stub
		return false;
	}

	protected int count;

	protected int island_min;

	protected int backbone_min;

	protected double max_length_ratio;

	protected int reference = -1;

	protected long all_seq_multiplicity;

	protected Segment [] firsts;

	protected Segment current;

	protected int seq_index;

	protected Vector backbone;

	public SegmentDataProcessor processor;

	protected boolean by_genome;

	public static final String AVERAGE_LENGTH = "avg_lngth";

	protected ContigHandler contig_handler;
	
	protected long offset;

	/**
	 * Constructor.
	 * 
	 * @param model
	 *            reference to mauve data
	 * @param file_name
	 *            name of file to output to
	 */
	public AbstractMatchDataWriter (String add, SegmentDataProcessor proc) {
		super (((String) proc.get (FILE_STUB)) + "_" + add + ".tab", proc);
	}

	protected void initSubClassParticulars (Hashtable args) {
		offset = -1;
		processor = (SegmentDataProcessor) args;
		island_min = DEFAULT_ISLAND_MIN;
		backbone_min = DEFAULT_BACKBONE_MIN;
		max_length_ratio = DEFAULT_MAX_LENGTH_RATIO;
		current = Segment.END;
		if (args.get (ISLAND_MIN) != null)
			island_min = ((Integer) args.get (ISLAND_MIN)).intValue ();
		else
			args.put (ISLAND_MIN, new Integer (island_min));
		if (args.get (BACKBONE_MIN) != null)
			backbone_min = ((Integer) args.get (BACKBONE_MIN)).intValue ();
		else
			args.put (BACKBONE_MIN, new Integer (backbone_min));
		if (args.get (MAX_LENGTH_RATIO) != null)
			max_length_ratio = ((Double) args.get (MAX_LENGTH_RATIO)).doubleValue ();
		else
			args.put (MAX_LENGTH_RATIO, new Double (max_length_ratio));
		if (args.get (CONTIG_HANDLER) != null)
			contig_handler = (ContigHandler) args.get (CONTIG_HANDLER);
		else
			contig_handler = this;
		backbone = (Vector) args.get (BACKBONE);
		reference = processor.reference;
		Collections.sort (backbone, new SegmentComparator (
				SegmentComparator.BY_MULTIPLICITY, true));
		firsts = (Segment []) args.get (FIRSTS);
		count = firsts.length;
		super.initSubClassParticulars (args);
		all_seq_multiplicity = ((Long) processor.get (ALL_MULTIPLICITY))
				.longValue ();
	}

	protected void printData (int which) {
		if ((which & BY_ONE_GENOME) == BY_ONE_GENOME) {
			by_genome = true;
			current = firsts[seq_index];
			printData ();
		}
		if ((which & BY_GENOMES) == BY_GENOMES) {
			by_genome = true;
			for (int i = 0; i < count; i++) {
				seq_index = i;
				current = firsts[i];
				printData ();
			}
		}
		if ((which & BY_BB_LIST) == BY_BB_LIST) {
			by_genome = false;
			row_number = 0;
			current = (Segment) backbone.get (0);
			printData ();
		}
	}

	protected boolean moreRowsToPrint () {
		if (by_genome) {
			current = current.nexts[seq_index];
			return !(current == Segment.END);
		} else if (row_number < backbone.size ()) {
			current = (Segment) backbone.get (row_number);
			return true;
		} else
			return false;
	}

	protected String getData (int column, int row) {
		long value = 0;
		int divisor = contig_handler instanceof AbstractMatchDataWriter ? 2 : 3;
		int seq = column / divisor;
		if (column % divisor == 2)
			return contig_handler.getContigName (seq, current.starts [seq]);
		else if (column % divisor == 0) {
			value = current.starts[seq];

		}
		else {
			value = current.ends[seq];
		}
		value = adjustForContigs (seq, value);
		if (current.reverse [seq])
			value -= value * 2;
		return value + "";
	}
	
	public long adjustForContigs (int sequence, long value) {
		if (offset == -1) {
			offset = value - contig_handler.getContigCoord (sequence, value);
			value -= offset;
		}
		else {
			value -= offset;
			offset = -1;
		}
		return value;
	}

	public Vector setColumnHeaders () {
		Vector titles = null;
		if (processor.contains (DEFAULT_TITLES))
			titles = (Vector) processor.get (DEFAULT_TITLES);
		if (titles == null)
			titles = processor.makeDefaultStartEndColumnHeaders ();
		return titles;
	}
	
	public long getContigCoord (int sequence, long loci) {
		return loci;
	}
	
	public long getPseudoCoord (int sequence, long loci, String contig) {
		return loci;
	}

	public String getContigName (int sequence, long loci) {
		return MauveConstants.DEFAULT_CONTIG;
	}
	
	public void fixSegmentByContigs (int sequence, Segment segment) {
	}

}
