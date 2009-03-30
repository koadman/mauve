package org.gel.mauve;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Properties;
import java.util.Vector;

import org.gel.mauve.tree.FileKey;
import org.gel.mauve.tree.GISTree;
import org.gel.mauve.tree.GapKey;
import org.gel.mauve.tree.TreeStore;

/**
 * XMFA file class A technical cross-product of insomnia and indulgence
 */
public class XMFAAlignment implements Serializable {
	/** Versioning for serializations of this object */
	static final long serialVersionUID = 3;

	// The file containing alignment data (transient, can't be serialized)
	protected transient RandomAccessFile xmfa_file;

	// The match class instances store the boundaries of each aligned segment
	protected Match [] intervals;

	// These store the offset within each file where an interval's sequence
	protected Match [] file_pos;

	// The number of newline bytes, as detected on the first line of the file
	protected int newline_size = 0;

	// The number of chars on each line
	protected int line_width = 80;

	// The number of sequences aligned
	protected int seq_count = 0;

	// The length of each sequence
	protected long [] seq_length;

	// A list of LCBs contained in this alignment
	protected LCB [] lcb_list;

	// A list of LCBs as they appear in the file
	private LCB [] source_lcb_list;

	// Array of comment lines for each alignment entry in the XMFA
	protected String [] comments;

	// Array of names for each sequence in the first alignment entry
	protected String [] names;

	// A set of gist's that map sequence index and column index to file
	// offset. indexed by [interval][sequence]
	GISTree [][] gis_tree;

	protected TreeStore ts = new TreeStore ();

	// The size of data chunks to read from disk
	protected int buffer_size = 500000;

	public Properties metadata = new Properties ();

	/**
	 * Sets the file used as backing store for this alignment Call this method
	 * to set the file after reading a serialized XMFAAlignment
	 * 
	 * @param f
	 *            The xmfa file corresponding to this alignment
	 */
	public void setFile (RandomAccessFile f) {
		xmfa_file = f;
	}

	/**
	 * Constructor reads an XMFA alignment from an input file and indexes the
	 * location of alignment bounds
	 */
	public XMFAAlignment (RandomAccessFile ir) throws java.io.IOException {
		xmfa_file = ir;
		// simple parse of the input
		Vector seq_nums = new Vector ();
		Vector lend = new Vector ();
		Vector rend = new Vector ();
		Vector reverse = new Vector ();
		Vector f_offset = new Vector ();
		Vector f_end_offset = new Vector ();
		Vector tmp_ivs = new Vector ();
		Vector tmp_offs = new Vector ();
		Vector tmp_lcbs = new Vector ();
		Vector gis_tree_tmp = new Vector ();
		Vector gist_seqnums = new Vector ();
		Vector gist_ivnums = new Vector ();
		Vector tmp_comments = new Vector ();
		Vector tmp_names = new Vector ();
		String cur_comment = null;

		/** < true when in the midst of reading gaps */
		long seq_offset = 0;
		GISTree gist = null;

		FileKey fk = null;
		GapKey gk = null;

		// XMFA file parse states
		final int wait_defline = 0;
		final int read_defline = 1;
		final int read_comment = 2;
		final int read_sequence = 3;
		final int process_sequence = 4;
		final int read_metadata = 5;

		byte [] buf = new byte [buffer_size];
		int bufI = 0;
		int remaining_bytes = 0;
		long cur_base = 0;
		int state = wait_defline;
		int section_start = buffer_size;

		// detect the number of bytes in a newline
		xmfa_file.seek (0);
		String first_line = xmfa_file.readLine ();
		newline_size = (int) (xmfa_file.getFilePointer () - first_line
				.length ());
		xmfa_file.seek (0);

		// begin parse loop
		while (true) {
			if (remaining_bytes == 0) {
				// it's important to preserve the defline read thus far
				// shift it to the beginning of the array and then read
				// from that point onward
				if (state != read_defline && state != read_comment)
					section_start = buf.length;
				int copy_len = buf.length - section_start;
				System.arraycopy (buf, section_start, buf, 0, copy_len);

				if (state == read_defline || state == read_comment)
					section_start = 0;

				// read more data into the buffer
				cur_base += bufI;
				xmfa_file.seek (cur_base);
				remaining_bytes = xmfa_file.read (buf, copy_len, buf.length
						- copy_len);
				cur_base -= copy_len;
				if (remaining_bytes <= 0)
					break;
				bufI = copy_len; // continue wherever we left off
			}

			switch (state) {
				case wait_defline:
					if (buf[bufI] == '#') {
						// read metadata or a user comment
						section_start = bufI + 1;
						state = read_metadata;
					}
					if (buf[bufI] == '>') {
						section_start = bufI;
						state = read_defline;
						bufI--;
						remaining_bytes++;
					}
					break;
				case read_metadata:
					if (buf[bufI] == '\r' || buf[bufI] == '\n') {
						String cur_line = new String (buf, section_start, bufI
								- section_start);

						// is this a sequence count specifier?
						int sc_index = cur_line.indexOf ("SequenceCount");
						if (sc_index >= 0) {
							seq_count = Integer.parseInt (cur_line.substring (
									sc_index + 13).trim ());
						} else
						// Otherwise, add it to the properties collection.
						{
							String [] parts = cur_line.split ("\\s", 2);

							if (parts.length == 0) {
								// Do nothing
							} else if (parts.length == 1) {
								metadata.setProperty (parts[0].trim (), "");
							} else {
								metadata.setProperty (parts[0].trim (),
										parts[1].trim ());
							}
						}
						// go back to waiting for a defline
						state = wait_defline;
					}
					break;
				// when a newline rolls around, parse out the defline
				case read_defline:
					if (buf[bufI] == '\r' || buf[bufI] == '\n') {
						// read the goods into cur_line
						String cur_line = new String (buf, section_start, bufI
								- section_start);

						// parse a sequence left-end, right-end, and strand
						// combination
						// > number:start-end strand(+/-)
						int colon_pos = cur_line.indexOf (':');
						int seq_num = -1;
						java.util.StringTokenizer seq_num_strtok = new java.util.StringTokenizer (
								cur_line.substring (1, colon_pos));
						if (seq_num_strtok.hasMoreTokens ()) {
							seq_num = Integer.parseInt (seq_num_strtok
									.nextToken ()) - 1;
							seq_nums.addElement (new Integer (seq_num));
						}
						String tmp_str = cur_line.substring (colon_pos + 1);
						int dash_pos = tmp_str.indexOf ('-');
						int space_pos = tmp_str.indexOf (' ');
						long first = Long.parseLong (tmp_str.substring (0,
								dash_pos));
						long second = Long.parseLong (tmp_str.substring (
								dash_pos + 1, space_pos));
						long left = first < second ? first : second;
						long right = first < second ? second : first;
						boolean cur_reverse = true;
						// parse strand -- if + isn't found then assume - strand
						if (tmp_str.indexOf ('+', space_pos + 1) < 0)
							reverse.addElement (new Boolean (true));
						else {
							reverse.addElement (new Boolean (false));
							cur_reverse = false;
						}
						lend.addElement (new Long (left));
						rend.addElement (new Long (right));

						// if we haven't yet completed the first alignment entry
						// try to parse the sequence name
						if (tmp_comments.size () == 0) {
							int strand_pos = 0;
							if (!cur_reverse)
								strand_pos = tmp_str.indexOf ('+',
										space_pos + 1);
							else
								strand_pos = tmp_str.indexOf ('-',
										space_pos + 1);

							if (tmp_str.length () > strand_pos + 2)
								// there's a name to parse
								tmp_names.add (tmp_str
										.substring (strand_pos + 2));
							else
								tmp_names.add (new String (""));
						}

						// prepare for seq parsing
						seq_offset = 0;
						state = read_sequence;

						gist = new GISTree (ts);
						gis_tree_tmp.addElement (gist);
						gist_seqnums.addElement (new Integer (seq_num));
						gist_ivnums.addElement (new Integer (tmp_ivs.size ()));
					}
					break;

				// ride out the comment
				case read_comment:
					if (buf[bufI] == '\r' || buf[bufI] == '\n') {
						// store the comment
						if (cur_comment == null) {
							cur_comment = new String (buf, section_start, bufI
									- section_start);
							tmp_comments.add (cur_comment);
						}
						if (buf[bufI] == '\n') {
							state = wait_defline;
							cur_comment = null;
						}
					}
					break;

				// wait for a sequence entry to finish, add gist keys, etc.
				case read_sequence:
					if (buf[bufI] == '>' || buf[bufI] == '=') {
						state = process_sequence;
						remaining_bytes++;
						bufI--;
						break;
					}

					// do nothing on newlines
					if (buf[bufI] == '\r' || buf[bufI] == '\n') {
						break;
					}

					// set the sequence data file offset if this is the
					// beginning of
					// the entry
					if (fk == null && gk == null) {
						f_offset.addElement (new Long (cur_base + bufI));
					}

					// it's all sequence data
					long cur_len = gist.length ();
					if (buf[bufI] == '-') {
						if (fk != null) {
							gist.insert (fk, GISTree.end);
							if (cur_len + fk.getLength () != gist.length ()) {
								throw new RuntimeException ("Corrupt GisTree.");
							}
							fk = null;
						}
						if (gk == null)
							gk = new GapKey (0);
						gk.length++;
					} else {
						if (gk != null) {
							gist.insert (gk, GISTree.end);
							if (cur_len + gk.getLength () != gist.length ()) {
								throw new RuntimeException ("Corrupt GisTree");
							}
							gk = null;
						}
						if (fk == null) {
							fk = new FileKey (seq_offset, cur_base + bufI);
						}
						fk.incrementLength ();
						fk.setFLength (cur_base + bufI - fk.getOffset () + 1);
					}

					break;

				// process a completed sequence entry
				case process_sequence:

					// do final processing from a previously parsed sequence
					long curry_len = gist.length ();
					if (fk != null) {
						gist.insert (fk, GISTree.end);
						if (curry_len + fk.getLength () != gist.length ()) {
							throw new RuntimeException ("Corrupt GisTree");
						}
					}
					if (gk != null) {
						gist.insert (gk, GISTree.end);
						if (curry_len + gk.getLength () != gist.length ()) {
							throw new RuntimeException ("Corrupt GisTree");
						}
					}
					fk = null;
					gk = null;

					// sequence ends here
					f_end_offset.addElement (new Long (cur_base + bufI));

					if (buf[bufI] == '>') {
						section_start = bufI;
						state = read_defline;
						remaining_bytes++;
						bufI--;
					} else if (buf[bufI] == '=') {
						// process a sequence set!

						if (seq_count == 0)
							seq_count = lend.size ();
						// create a new Match element
						Match m = new Match (seq_count);
						// copy file offsets
						Match f_off_m = new Match (seq_count);
						// copy left ends into start
						int aligned_count = 0;
						// only set values for sequence numbers that were
						// actually
						// part
						// of this interval
						for (int seq_numI = 0; seq_numI < seq_nums.size (); seq_numI++) {
							int seqI = ((Integer) seq_nums.elementAt (seq_numI))
									.intValue ();
							m.setStart (seqI,
									((Long) lend.elementAt (seq_numI))
											.longValue ());
							if (m.getStart (seqI) != 0)
								aligned_count++;
							m.setLength (seqI, ((Long) rend
									.elementAt (seq_numI)).longValue ());
							m.setReverse (seqI, ((Boolean) reverse
									.elementAt (seq_numI)).booleanValue ());
							f_off_m.setStart (seqI, ((Long) f_offset
									.elementAt (seq_numI)).longValue ());
							f_off_m.setLength (seqI, ((Long) f_end_offset
									.elementAt (seq_numI)).longValue ());
						}
						// if this interval contains alignment of more than one
						// sequence then call it an LCB
						// there may be problems with the assumption that an
						// interval will always contain
						// some aligned sequence if it has more than one set of
						// genome coordinates defined,
						// but let's not worry about that now since mauveAligner
						// won't have that problem
						if (aligned_count > 1) {
							LCB lcb = new LCB (m, tmp_lcbs.size (), seq_count);
							tmp_lcbs.addElement (lcb);
						}

						// add to tmp_ivs
						tmp_ivs.addElement (m);
						tmp_offs.addElement (f_off_m);

						// clear data structures for the next alignment interval
						lend = new Vector ();
						rend = new Vector ();
						reverse = new Vector ();
						f_offset = new Vector ();
						f_end_offset = new Vector ();
						seq_nums = new Vector ();

						state = read_comment;
						section_start = bufI;
						remaining_bytes++;
						bufI--;
					}
					break;

			}

			bufI++;
			remaining_bytes--;
		}

		intervals = new Match [tmp_ivs.size ()];
		intervals = (Match []) tmp_ivs.toArray (intervals);
		file_pos = new Match [tmp_offs.size ()];
		file_pos = (Match []) tmp_offs.toArray (file_pos);
		lcb_list = new LCB [tmp_lcbs.size ()];
		lcb_list = (LCB []) tmp_lcbs.toArray (lcb_list);
		setSourceLcbList(new LCB [lcb_list.length]);
		for(int ll = 0; ll < lcb_list.length; ll++)
			getSourceLcbList()[ll] = new LCB(lcb_list[ll]);
		seq_length = new long [seq_count];
		gis_tree = new GISTree [intervals.length] [seq_count];
		for (int gistI = 0; gistI < gis_tree_tmp.size (); gistI++) {
			int ivI = ((Integer) gist_ivnums.elementAt (gistI)).intValue ();
			int seqI = ((Integer) gist_seqnums.elementAt (gistI)).intValue ();
			gis_tree[ivI][seqI] = (GISTree) gis_tree_tmp.elementAt (gistI);
		}

		for (int seqI = 0; seqI < seq_count; seqI++) {
			long seq_len = 0;
			int treeI = 0;
			for (int ivI = 0; ivI < intervals.length; ivI++) {
				if (intervals[ivI].getLength (seqI) > seq_len) {
					seq_len = intervals[ivI].getLength (seqI);
				}
				if (gis_tree[ivI][seqI] == null) {
					gis_tree[ivI][seqI] = new GISTree (ts);
					// insert a GapKey of the full length of this interval
					long iv_length = 0;
					for (int seqJ = 0; seqJ < seq_count; seqJ++) {
						if (gis_tree[ivI][seqJ] != null
								&& iv_length < gis_tree[ivI][seqJ].length ()) {
							iv_length = gis_tree[ivI][seqJ].length ();
						}
					}
					gis_tree[ivI][seqI].insert (new GapKey (iv_length), 0);
				}
				treeI++;
			}
			seq_length[seqI] = seq_len;
		}

		names = new String [tmp_names.size ()];
		names = (String []) tmp_names.toArray (names);

		comments = new String [tmp_comments.size ()];
		comments = (String []) tmp_comments.toArray (comments);

		// Prune the arrays.
		ts.pruneArrays ();
	}

	/**
	 * Read a comment line from an LCB The LCB comment line is the terminator
	 * line that begins with =
	 * 
	 * @param lcbI
	 *            The LCB to read a comment line from
	 * @return The comment line
	 */
	public String getComment (int lcbI) {
		return comments[lcbI];
	}

	/**
	 * Read the source file name of a given sequence
	 * 
	 * @param seqI
	 *            The sequence index (starting at 0)
	 * @return The source file name
	 */
	public String getName (int seqI) {
		return names[seqI];
	}

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
			long read_size = intervals[ivI].getLength (g) - rend > 0 ? rend
					- cur_offset + 1 : intervals[ivI].getLength (g)
					- cur_offset + 1;
			while (read_size > 0) {
				// assume forward orientation
				// plan b: get the range of columns that need to be read
				// for each seq, read the cols directly into a byte buffer
				// reverse the sequences if necessary
				long lcb_offset = cur_offset - cur_iv_lend;
				long lcb_right_offset = intervals[ivI].getReverse (g) ? intervals[ivI]
						.getLength (g)
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
	 * reverse entries in a byte array
	 */
	void reverse (byte [] byte_buf) {
		for (int byteI = 0; byteI < byte_buf.length / 2; byteI++) {
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
	public byte [] readRawSequence (int ivI, int seqI, long left_col, long length) {
		// check boundary condition
		if (gis_tree[ivI][seqI].length () == 0) {
			if (length == 0)
				return new byte [0];
			else
				throw new ArrayIndexOutOfBoundsException ();
		}

		int l_iter = gis_tree[ivI][seqI].find (left_col);
		int r_iter = gis_tree[ivI][seqI].find (left_col + length - 1);
		FileKey l_fk, r_fk;
		long l_gaps = 0;

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
		if (gis_tree[ivI][seqI].getKey (l_iter) instanceof GapKey
				&& gis_tree[ivI][seqI].getKey (r_iter) instanceof GapKey
				&& gis_tree[ivI][seqI].getKey (l_iter) == gis_tree[ivI][seqI]
						.getKey (r_iter)) {
			byte [] bb = new byte [(int) length];
			for (int bbI = 0; bbI < bb.length; bbI++)
				bb[bbI] = (byte) '-';
			return bb;
		}

		if (gis_tree[ivI][seqI].getKey (l_iter) instanceof FileKey) {
			l_fk = (FileKey) gis_tree[ivI][seqI].getKey (l_iter);
		} else {
			// if we started in a gap the next one should be a FileKey
			seq_off = gis_tree[ivI][seqI].getSequenceStart (l_iter);
			l_iter = gis_tree[ivI][seqI].find_seqindex (seq_off);
			l_fk = (FileKey) gis_tree[ivI][seqI].getKey (l_iter);
		}

		if (gis_tree[ivI][seqI].getKey (r_iter) instanceof FileKey) {
			r_fk = (FileKey) gis_tree[ivI][seqI].getKey (r_iter);
		} else {
			// if we started in a gap the next one should be a FileKey
			seq_off = gis_tree[ivI][seqI].getSequenceStart (r_iter) - 1;
			r_iter = gis_tree[ivI][seqI].find_seqindex (seq_off);
			r_fk = (FileKey) gis_tree[ivI][seqI].getKey (r_iter);
		}

		// should have l_fk and r_fk now.
		// get the file offsets to read from these
		long l_off = left_col - gis_tree[ivI][seqI].getStart (l_iter);
		// calculate the number of newlines in the space between the first
		// desired
		// column and the first character in this key...
		long key_col_pos = gis_tree[ivI][seqI].getStart (l_iter) % line_width;
		long l_newlines = ((key_col_pos + l_off) / 80) * newline_size;
		if (l_off < 0) {
			// this happens when the desired column is a gap. just skip ahead
			l_gaps = -l_off; // track how many gaps we'll need later
			l_newlines = 0;
			l_off = 0;
		}
		l_off += l_fk.getOffset () + l_newlines;

		long r_off = left_col + length - 1
				- gis_tree[ivI][seqI].getStart (r_iter);
		long r_key_col = gis_tree[ivI][seqI].getStart (r_iter) % line_width;
		long r_newlines = ((r_key_col + r_off) / line_width) * newline_size;
		r_off += r_fk.getOffset () + r_newlines;

		// now that the exact file offsets of the desired sequence have been
		// calculated, read it from the XMFA.
		if (r_off - l_off + 1 + l_gaps < 0) {
			throw new RuntimeException ("Unexpected Error.");
		}
		byte [] byte_buf = new byte [(int) (r_off - l_off + 1 + l_gaps)];
		for (int l_gapI = 0; l_gapI < l_gaps; l_gapI++) {
			byte_buf[l_gapI] = (byte) '-';
		}

		try {
			xmfa_file.seek (l_off);
			xmfa_file.read (byte_buf, (int) l_gaps, byte_buf.length
					- (int) l_gaps);
		} catch (IOException e) {
			throw new RuntimeException ("Unexpected file reading error.", e);
		}
		return byte_buf;
	}

	static public byte [] filterNewlines (byte [] byte_buf) {
		// filter the newlines
		int byte_off = 0;
		for (int byteI = 0; byteI < byte_buf.length; byteI++) {
			if (byte_buf[byteI] == '\r' || byte_buf[byteI] == '\n')
				continue;
			byte_buf[byte_off] = byte_buf[byteI];
			byte_off++;
		}
		byte [] bb2 = new byte [byte_off];
		System.arraycopy (byte_buf, 0, bb2, 0, byte_off);
		return bb2;
	}

	/**
	 * return the LCB index that contains the given position of the given
	 * sequence
	 */
	int getLCB (Genome g, long position) {
		int ivI = 0;
		for (; ivI < intervals.length; ivI++) {
			if (intervals[ivI].getStart (g) <= position
					&& position <= intervals[ivI].getLength (g))
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
			return intervals[ivI].getReverse (g) ? intervals[ivI].getLength (g)
					- intervals[ivI].getStart (g) - position : position;
		} catch (Exception e) {
			e.printStackTrace ();
			return -1;
		}
	}

	// converts an LCB local coordinate to a global sequence coordinate, taking
	// rev. comp. into account
	long globalToLCB (long position, Genome g, int ivI) {
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
	public void getColumnCoordinates (XmfaViewerModel model, int lcb,
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
	 * Returns the length in alignment columns of a particular LCB
	 * @param lcbId	The index of the LCB in question
	 * @return	a long int with the length
	 */
	public long getLcbLength(int lcbId)
	{
		return gis_tree[lcbId][0].length();
	}

	/**
	 * Reorder the sequences to conform to the order given in new_order[]
	 */
	public void setReference (Genome g) {
		for (int lcbI = 0; lcbI < lcb_list.length; lcbI++) {
			lcb_list[lcbI].setReference (g);
		}
	}

	public void setSourceLcbList(LCB [] source_lcb_list) {
		this.source_lcb_list = source_lcb_list;
	}

	public LCB [] getSourceLcbList() {
		return source_lcb_list;
	}
}
