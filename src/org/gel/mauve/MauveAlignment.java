package org.gel.mauve;

import java.io.Serializable;

import org.gel.mauve.tree.SequenceKey;
import org.gel.mauve.tree.GISTree;
import org.gel.mauve.tree.GapKey;
import org.gel.mauve.tree.TreeStore;


//TODO Should also make a superclass of SequenceKey that is a Sequence or segment 
//key also independent of a file, and pass this into method that takes SequenceKeys 
//in MauveAlignment.
abstract public class MauveAlignment implements Serializable {
	
	/** Versioning for serializations of this object */
	static final long serialVersionUID = 1;

	// The match class instances store the boundaries of each aligned segment
	protected Match [] intervals;
	
	// The number of sequences aligned
	protected int seq_count = 0;

	// The length of each sequence
	protected long [] seq_length;

	// A list of LCBs contained in this alignment
	protected LCB [] lcb_list;
	
	// A set of gist's that map sequence index and column index to file
	// offset. indexed by [interval][sequence]
	protected GISTree [][] gis_tree;

	protected TreeStore ts = new TreeStore ();
	
	/**
	 * Extracts columns from the sequence alignment containing the specified
	 * range of the specified sequence. The returned alignment columns will
	 * contain gaps and any whitespace in the XMFA source (e.g. newlines)
	 * 
	 * @param seqI
	 *            The index of the sequence of interest
	 * @param lend
	 *            The left end coordinate of the range to be extracted
	 * @param rend
	 *            The right end coordinate of the range to be extracted
	 * @return A set of alignment columns stored as an array of byte arrays
	 *         indexed as [sequence][column]
	 */
	public Object [] getRange (Genome g, long lend, long rend) {
		long cur_offset = lend;
		Object [] cols = new Object [seq_count];
		int seqJ = 0;
		for (seqJ = 0; seqJ < seq_count; seqJ++)
			cols[seqJ] = new byte [0];

		while (cur_offset <= rend) {
			// determine which LCB we start in
			int ivI = getLCB (g, cur_offset);

			// determine the file offset of the left end of this sequence
			// read a bunch o' columns
			long cur_iv_lend = intervals[ivI].getStart (g);
			long read_size = intervals[ivI].getEnd (g) - rend > 0 ? rend
					- cur_offset + 1 : intervals[ivI].getEnd (g)
					- cur_offset + 1;
			while (read_size > 0) {
				// assume forward orientation
				// plan b: get the range of columns that need to be read
				// for each seq, read the cols directly into a byte buffer
				// reverse the sequences if necessary
				long lcb_offset = cur_offset - cur_iv_lend;
				long lcb_right_offset = intervals[ivI].getReverse (g) ? intervals[ivI]
						.getEnd (g)
						- intervals[ivI].getStart (g) - lcb_offset + 1
						: lcb_offset + read_size;
				lcb_offset = intervals[ivI].getReverse (g) ? lcb_right_offset
						- read_size : lcb_offset;

				long fk_left_col = gis_tree[ivI][g.getSourceIndex ()]
						.seqPosToColumn (lcb_offset);
				long fk_right_col = gis_tree[ivI][g.getSourceIndex ()]
						.seqPosToColumn (lcb_right_offset);

				Object [] byte_bufs = new Object [seq_count];
				for (seqJ = 0; seqJ < seq_count; seqJ++) {
					byte_bufs[seqJ] = readRawSequence (ivI, seqJ, fk_left_col,
							fk_right_col - fk_left_col);
				}

				// just assume that the correct number of columns was read
				cur_offset += read_size;
				read_size = 0;

				// reverse the columns if necessary
				if (intervals[ivI].getReverse (g)) {
					for (seqJ = 0; seqJ < seq_count; seqJ++) {
						reverse ((byte []) byte_bufs[seqJ]);
					}
				}
				// append the columns to cols
				for (seqJ = 0; seqJ < seq_count; seqJ++) {
					byte [] tmp_buf = new byte [((byte []) cols[seqJ]).length
							+ ((byte []) byte_bufs[seqJ]).length];
					System.arraycopy ((byte []) cols[seqJ], 0, tmp_buf, 0,
							((byte []) cols[seqJ]).length);
					System.arraycopy ((byte []) byte_bufs[seqJ], 0, tmp_buf,
							((byte []) cols[seqJ]).length,
							((byte []) byte_bufs[seqJ]).length);
					cols[seqJ] = tmp_buf;
				}
			}
		}
		return cols;
	}
	
	/**
	 * Read sequence data (and any gap characters) from a file, filtering
	 * newlines column index starts at 0!!
	 * 
	 * @param ivI
	 *            The interval to read
	 * @param seqI
	 *            The sequence to read
	 * @param left_col
	 *            Left column index (interval local coordinates)
	 * @param length
	 *            Length to read in columns (includes gaps)
	 */
	protected byte [] readRawSequence (int ivI, int seqI, long left_col, long length) {
		// check boundary condition
		if (gis_tree[ivI][seqI].length () == 0) {
			if (length == 0)
				return new byte [0];
			else
				throw new ArrayIndexOutOfBoundsException ();
		}

		int l_iter = gis_tree[ivI][seqI].find (left_col);
		int r_iter = gis_tree[ivI][seqI].find (left_col + length - 1);
		SequenceKey l_fk, r_fk;

		// if the requested region contains nothing but gap then just return a
		// buffer of gaps
		long seq_off = gis_tree[ivI][seqI].getSequenceStart (l_iter);
		if (gis_tree[ivI][seqI].sequenceLength () <= left_col
				&& gis_tree[ivI][seqI].getSeqLength (l_iter) == 0
				&& seq_off <= left_col) {
			if (left_col + length - 1 > gis_tree[ivI][seqI].length ())
				throw new ArrayIndexOutOfBoundsException ();
			byte [] bb = new byte [(int) length];
			for (int bbI = 0; bbI < bb.length; bbI++)
				bb[bbI] = (byte) '-';
			return bb;
		}

		// check for the case where the requested region lies within
		// the same gap in the middle of the sequence
		/** removed
		 * gis_tree[ivI][seqI].getKey (r_iter) instanceof GapKey &&
		 * from if statement; redundant if they pass == test.
		 */
		if (gis_tree[ivI][seqI].getKey (l_iter) instanceof GapKey
				&& gis_tree[ivI][seqI].getKey (l_iter) == gis_tree[ivI][seqI]
						.getKey (r_iter)) {
			byte [] bb = new byte [(int) length];
			for (int bbI = 0; bbI < bb.length; bbI++)
				bb[bbI] = (byte) '-';
			return bb;
		}

		if (gis_tree[ivI][seqI].getKey (l_iter) instanceof SequenceKey) {
			l_fk = (SequenceKey) gis_tree[ivI][seqI].getKey (l_iter);
		} else {
			// if we started in a gap the next one should be a SequenceKey
			seq_off = gis_tree[ivI][seqI].getSequenceStart (l_iter);
			l_iter = gis_tree[ivI][seqI].find_seqindex (seq_off);
			l_fk = (SequenceKey) gis_tree[ivI][seqI].getKey (l_iter);
		}

		if (gis_tree[ivI][seqI].getKey (r_iter) instanceof SequenceKey) {
			r_fk = (SequenceKey) gis_tree[ivI][seqI].getKey (r_iter);
		} else {
			// if we started in a gap the next one should be a SequenceKey
			seq_off = gis_tree[ivI][seqI].getSequenceStart (r_iter) - 1;
			r_iter = gis_tree[ivI][seqI].find_seqindex (seq_off);
			r_fk = (SequenceKey) gis_tree[ivI][seqI].getKey (r_iter);
		}
		
		return readSequenceFromKeys (l_fk, r_fk, left_col, length, ivI, 
				seqI, l_iter, r_iter);
	}
	
	/**
	 * reverse entries in a byte array
	 */
	void reverse (byte [] byte_buf) {
		int max = byte_buf.length / 2;
		for (int byteI = 0; byteI < max; byteI++) {
			byte tmp = byte_buf[byteI];
			byte_buf[byteI] = byte_buf[byte_buf.length - byteI - 1];
			byte_buf[byte_buf.length - byteI - 1] = tmp;
		}
	}
	
	/**
	 * filter gaps from one sequence while maintaining the columns of the
	 * alignment return the number of columns remaining after filtering
	 */
	int filterGapsInOneSequence (int seqI, Object [] byte_bufs) {
		// filter gaps from a particular sequence while maintaining column
		// integrity
		int col_offset = 0;
		for (int colI = 0; colI < ((byte []) byte_bufs[seqI]).length; colI++) {
			if (((byte []) byte_bufs[seqI])[colI] != '-') {
				// copy the column to the current col_offset
				for (int seqJ = 0; seqJ < seq_count; seqJ++) {
					((byte []) byte_bufs[seqJ])[col_offset] = ((byte []) byte_bufs[seqJ])[colI];
				}
				col_offset++;
			}
		}
		return col_offset;
	}
	
	/**
	 * return the LCB index that contains the given position of the given
	 * sequence.  this is relative to the intervals array, not the lcb ids or array.
	 */
	public int getLCB (Genome g, long position) {
		int ivI = 0;
		for (; ivI < intervals.length; ivI++) {
			if (intervals[ivI].getStart (g) <= position
					&& position <= intervals[ivI].getEnd (g))
				break; // found the starting interval -- remember lengths array
			// contains r_end
		}
		// throw an exception if the requested range couldn't be found
		if (ivI == intervals.length)
			throw new ArrayIndexOutOfBoundsException ("genome " + g
					+ " position " + position);
		return ivI;
	}

	// FIXME: get rev. comp right in these two functions!
	long revCompify (long position, Genome g, int ivI) {
		try {
			return intervals[ivI].getReverse (g) ? intervals[ivI].getEnd (g)
					- intervals[ivI].getStart (g) - position : position;
		} catch (Exception e) {
			e.printStackTrace ();
			return -1;
		}
	}

	// converts an LCB local coordinate to a global sequence coordinate, taking
	// rev. comp. into account
	public long globalToLCB (long position, Genome g, int ivI) {
		long offset = position - intervals[ivI].getStart (g);
		return revCompify (offset, g, ivI);
	}

	// converts a global sequence coordinate to an LCB local coordinate, taking
	// rev. comp. into account
	long LCBToGlobal (long position, Genome g, int ivI) {
		long offset = revCompify (position, g, ivI);
		offset += intervals[ivI].getStart (g);
		if (intervals[ivI].getStart (g) == 0)
			return 0;
		return offset;
	}

	/**
	 * Identifies the LCB and the column within the LCB of a given sequence
	 * position
	 */
	public long [] getLCBAndColumn (Genome g, long position) {
		// determine the LCB
		int ivI = getLCB (g, position);

		long lcb_offset = globalToLCB (position, g, ivI);
		long fk_left_col = gis_tree[ivI][g.getSourceIndex ()]
				.seqPosToColumn (lcb_offset);

		long [] lcb_and_col = new long [2];
		lcb_and_col[0] = ivI;
		lcb_and_col[1] = fk_left_col;
		return lcb_and_col;
	}

	/**
	 * Returns the sequence coordinates aligned in a given column, ordered
	 * according to source index.
	 * 
	 * @param seq_offsets
	 *            The sequence coordinates (output)
	 * @param gap
	 *            True whenever a given sequence has a gap in the query column
	 */
	public void getColumnCoordinates (MauveAlignmentViewerModel model, int lcb,
			long column, long [] seq_offsets, boolean [] gap) {
		for (int seqI = 0; seqI < seq_count; seqI++) {
			Genome g = model.getGenomeBySourceIndex (seqI);

			seq_offsets[seqI] = gis_tree[lcb][g.getSourceIndex ()]
					.columnToSeqPos (column);
			gap[seqI] = column != gis_tree[lcb][g.getSourceIndex ()]
					.seqPosToColumn (seq_offsets[seqI]);
			seq_offsets[seqI] = LCBToGlobal (seq_offsets[seqI], g, lcb);

		}
	}

	/**
	 * Reorder the sequences to conform to the order given in new_order[]
	 */
	public void setReference (Genome g) {
		for (int lcbI = 0; lcbI < lcb_list.length; lcbI++) {
			lcb_list[lcbI].setReference (g);
		}
	}
	
	public GISTree getGISTree (int interval, Genome genome) {
		return gis_tree [interval][genome.getSourceIndex()];
	}
	
	abstract protected byte [] readSequenceFromKeys (SequenceKey l_fk, SequenceKey r_fk, 
			long left_col, long length, int ivI, int seqI, int l_iter, int r_iter);

}
