package org.gel.mauve;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Properties;
import java.util.Vector;

import org.gel.mauve.tree.FileKey;
import org.gel.mauve.tree.GISTree;
import org.gel.mauve.tree.GapKey;
import org.gel.mauve.tree.SequenceKey;

/**
 * XMFA file class A technical cross-product of insomnia and indulgence
 */
public class XMFAAlignment extends MauveAlignment implements Serializable {
	/** Versioning for serializations of this object */
	static final long serialVersionUID = 3;

	// The file containing alignment data (transient, can't be serialized)
	protected transient RandomAccessFile xmfa_file;

	// The number of newline bytes, as detected on the first line of the file
	protected int newline_size = 0;

	// The number of chars on each line
	protected int line_width = 80;

	// Array of comment lines for each alignment entry in the XMFA
	protected String [] comments;

	// Array of names for each sequence in the first alignment entry
	protected String [] names;

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
						Match m = new Match (seq_count, true);
						// copy file offsets
						Match f_off_m = new Match (seq_count, true);
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
							m.setEnd (seqI, ((Long) rend
									.elementAt (seq_numI)).longValue ());
							m.setReverse (seqI, ((Boolean) reverse
									.elementAt (seq_numI)).booleanValue ());
							f_off_m.setStart (seqI, ((Long) f_offset
									.elementAt (seq_numI)).longValue ());
							f_off_m.setEnd (seqI, ((Long) f_end_offset
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
		lcb_list = new LCB [tmp_lcbs.size ()];
		lcb_list = (LCB []) tmp_lcbs.toArray (lcb_list);
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
				if (intervals[ivI].getSegmentEnd (seqI) > seq_len) {
					seq_len = intervals[ivI].getSegmentEnd (seqI);
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

	@Override
	protected byte [] readSequenceFromKeys (SequenceKey l_fk, SequenceKey r_fk, 
			long left_col, long length, int ivI, int seqI, int l_iter, int r_iter) {
		long l_gaps = 0;
		// get the file offsets to read from these
		long l_off = left_col - gis_tree[ivI][seqI].getStart (l_iter);
		//how much of right key is needed, or how many gaps passed right key are
		//needed if r_off is greater then the right filekey's length.
		long r_off = left_col + length - 1 - gis_tree[ivI][seqI].getStart (r_iter);
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
		l_off += ((FileKey) l_fk).getOffset () + l_newlines;

		long r_key_col = gis_tree[ivI][seqI].getStart (r_iter) % line_width;
		long r_newlines = ((r_key_col + r_off) / line_width) * newline_size;
		r_off += ((FileKey) r_fk).getOffset () + r_newlines;

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

	byte [] filterNewlines (byte [] byte_buf) {
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

}
