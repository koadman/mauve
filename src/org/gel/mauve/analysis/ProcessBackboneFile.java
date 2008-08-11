package org.gel.mauve.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import org.gel.mauve.MauveConstants;
import org.gel.mauve.analysis.output.SegmentDataProcessor;

public class ProcessBackboneFile implements MauveConstants {

	protected BufferedReader in;

	protected String in_file;

	protected StringTokenizer current_input;

	protected Iterator sub_iterator;

	protected String not_header;

	public final static String INPUT_FILE = "input_file";

	protected Vector backbone;

	protected int count;

	protected String [] current_row;

	public ProcessBackboneFile (Hashtable args) {
		init ((String) args.get (INPUT_FILE));
	}
	
	public ProcessBackboneFile (String file) {
		init (file);
	}

	protected void init (String file) {
		try {
			in_file = file;
			backbone = new Vector ();
			in = new BufferedReader (new FileReader (in_file));
			loadData ();
			in.close ();
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}

	protected void loadData () {
		count = getSequenceCount ();
		current_row = new String [count];
		processFirstRow ();
		int i = 0;
		String row = null;
		if (not_header != null) {
			current_row[0] = not_header;
			not_header = null;
			i = 1;
		} else {
			try {
				row = in.readLine ();
				current_input = new StringTokenizer (row);
			} catch (Exception e) {
				System.out.println ("No data to print.");
				e.printStackTrace ();
			}
		}
		while (row != null) {
			try {
				for (; i < current_row.length; i++)
					current_row[i] = current_input.nextToken ();
				storeRow ();
				i = 0;
				row = in.readLine ();
				if (row != null)
					current_input = new StringTokenizer (row);
			} catch (Exception e) {
				e.printStackTrace ();
				// System.exit(0);
			}

		}
	}

	protected void storeRow () {
		Segment segment = new Segment (count, true, true);
		for (int i = 0, j = 0; j < count; i++, j++) {
			segment.starts[j] = Long.parseLong (current_row[i]);
			segment.ends[j] = Long.parseLong (current_row[++i]);
			if (segment.starts[j] < 0) {
				segment.starts[j] *= -1;
				segment.ends[j] *= -1;
				segment.reverse[j] = true;
			}
		}
		backbone.add (segment);
	}

	protected int getSequenceCount () {
		try {
			current_input = new StringTokenizer (in.readLine ());
			return current_input.countTokens ();
		} catch (IOException e) {
			System.out.println ("Couldn't read first line of file.");
			e.printStackTrace ();
			return -1;
		}
	}

	protected void processFirstRow () {
		Vector titles = null;
		try {
			StringTokenizer toke = current_input;
			if (count % 2 == 1)
				throw new IOException ("Odd number of column headers.");
			String first = toke.nextToken ();
			try {
				Long.parseLong (first);
				not_header = first;
				current_input = toke;
			} catch (NumberFormatException e) {
				titles = new Vector (count);
				boolean trim = first.indexOf ("_leftend") > -1;
				for (int i = 0; i < count; i++) {
					if (i != 0)
						first = toke.nextToken ();
					if (trim) {
						if (i % 2 == 0)
							titles.add (first
									.substring (0, first.length () - 8));
						else
							titles.add (first
									.substring (0, first.length () - 9));
					} else
						titles.add (first);
				}
			}
		} catch (IOException e) {
			System.out.println ("Can't read column headers");
			e.printStackTrace ();
			titles = null;
		}
		count /= 2;
	}

	public Vector getBackboneSegments () {
		return backbone;
	}
	
	public static SegmentDataProcessor getProcessor (String f_name, 
			Hashtable spec_args) {
		try {
			File file = new File (f_name);
			if (!new File (file.getAbsolutePath() + ".backbone").exists()) {
				int end = file.getName ().lastIndexOf ('.');
				if (end > -1)
					file = new File (file.getParentFile (), 
							file.getName ().substring (0, end));
				if (!new File (file.getAbsolutePath() + ".backbone").exists())
					throw new FileNotFoundException ("Couldn't find backbone file");
			}
			spec_args.put (INPUT_FILE, file.getAbsolutePath() + ".backbone");
			System.out.println ("file: " + file);
			spec_args.put (SegmentDataProcessor.FILE_STUB, file.getAbsolutePath());
			spec_args.put (SegmentDataProcessor.BACKBONE,
					new ProcessBackboneFile (spec_args).getBackboneSegments ());
			return new SegmentDataProcessor (spec_args);
		} catch (Exception e) {
			e.printStackTrace ();
			return null;
		}
	}

	/**
	 * Main.
	 * 
	 * @param args
	 *            The backbone file to read and create islands from
	 */
	public static void main (String [] args) {
		getProcessor ((String) args [0], new Hashtable ());
	}

}
