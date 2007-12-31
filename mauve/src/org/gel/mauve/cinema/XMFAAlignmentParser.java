package org.gel.mauve.cinema;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import uk.ac.man.bioinf.io.AlignmentOutputParser;
import uk.ac.man.bioinf.io.ParserExceptionHandler;
import uk.ac.man.bioinf.io.parsers.AbstractProteinAlignmentInputParser;
import uk.ac.man.bioinf.sequence.Sequences;
import uk.ac.man.bioinf.sequence.alignment.DefaultSequenceAlignment;
import uk.ac.man.bioinf.sequence.alignment.GappedSequence;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;
import uk.ac.man.bioinf.sequence.event.SequenceVetoException;
import uk.ac.man.bioinf.sequence.identifier.Identifier;
import uk.ac.man.bioinf.sequence.types.ProteinSequenceType;
import uk.ac.man.bioinf.util.IntArrayList;

public class XMFAAlignmentParser extends AbstractProteinAlignmentInputParser
		implements AlignmentOutputParser {
	private final static int LINE_WRAP = 80;

	private int block;

	private String text;

	private String sequence;

	private long range[];

	private BufferedReader r;

	public SequenceAlignment parse (Identifier ident, Reader reader,
			ParserExceptionHandler eh) throws IOException {
		XMFASource xmfaSource = (XMFASource) ident.getSource ();

		block = 0;
		r = new BufferedReader (reader);

		xmfaSource.getLcbIndex ();
		goToBlock (xmfaSource.getLcbIndex ());

		List sequences = new ArrayList ();
		IntArrayList insets = new IntArrayList ();
		readSequence ();
		int sourceIndex = 0;
		while (sequence != null) {
			String title = xmfaSource.getModel ().getGenomeBySourceIndex (
					sourceIndex).getDisplayName ();
			if (range != null) {
				title += " [" + range[0] + "-" + range[1] + "]";
			}

			GappedSequence gappedSequence = toSequence (
					sequence.toUpperCase (), "sequence " + sourceIndex, eh);
			try {
				insets.add (Sequences.chompLeadingGaps (gappedSequence));
			} catch (SequenceVetoException e) {
				// Shouldn't happen here.
				throw new RuntimeException (e);
			}
			gappedSequence = Sequences.getElementsAsGappedSequence (
					gappedSequence.getGappedSequenceAsElements (),
					ProteinSequenceType.getInstance (),
					new XMFASequenceIdentifier (title, range));
			sequences.add (gappedSequence);
			readSequence ();
			sourceIndex++;
		}

		// Rearrange display of sequences to correspond to model.
		int [] viewingOrder = xmfaSource.getViewingOrder ();
		GappedSequence [] ar = new GappedSequence [sequences.size ()];
		for (int i = 0; i < viewingOrder.length; i++) {
			ar[i] = (GappedSequence) sequences.get (viewingOrder[i]);
		}

		return new DefaultSequenceAlignment (ar, ProteinSequenceType
				.getInstance (), insets.toArray (), ident);

	}

	public SequenceAlignment parse (Reader reader, ParserExceptionHandler eh)
			throws IOException {
		throw new RuntimeException ("Identifier required.");
	}

	public Writer write (SequenceAlignment sa, Writer writer,
			ParserExceptionHandler eh) {
		// Assumption: the alignments identifier will be changed to the target
		// for output.

		XMFASource xmfaSource = (XMFASource) sa.getIdentifier ().getSource ();
		File src = xmfaSource.getModel ().getSrc ();

		BufferedWriter w = new BufferedWriter (writer);

		try {
			BufferedReader r = new BufferedReader (new FileReader (src));
			// Copy everything up until the beginning of the block.
			int section = 0;

			while (section < xmfaSource.getLcbIndex ()) {
				String s = r.readLine ();
				if (s.charAt (0) == '=') {
					section++;
				}
				w.write (s);
				w.newLine ();
			}

			int [] viewingOrder = xmfaSource.getViewingOrder ();

			// Insert the new stuff.
			for (int i = 1; i <= sa.getNumberSequences (); i++) {
				int pos = 0;
				for (; pos < viewingOrder.length; pos++) {
					// Sequences in editor are indexed by 1, so we need to
					// adjust.
					if (viewingOrder[pos] == i - 1) {
						break;
					}
				}

				GappedSequence gs = sa.getSequenceAt (pos + 1);

				// Skip to next header, and write it.
				String s = r.readLine ();
				while (s.charAt (0) != '>') {
					s = r.readLine ();
				}
				w.write (s);
				w.newLine ();

				// Use line wrapping on sequence data.
				LineWrapWriter lw = new LineWrapWriter (w, LINE_WRAP);

				// Write leading gaps.
				int leadingGaps = sa.getInset (i);
				for (int j = 0; j < leadingGaps; j++) {
					lw.write ('-');
				}

				// Write sequence data, wrapped every LINE_WRAP characters.
				char [] data = gs.getGappedSequenceAsChars ();
				lw.write (data);

				// Write trailing gaps to make sequences same length.
				int len = gs.getGappedLength () + sa.getInset (i);
				for (int j = len; j < sa.getLength (); j++) {
					lw.write ('-');
				}

				// Add a trailing newline, regardless of line length.
				// Note that this is using the writer, not linewrap writer.
				w.newLine ();
			}

			// Move to end of section.
			String s = r.readLine ();
			while (s.charAt (0) != '=') {
				s = r.readLine ();
			}
			w.write (s);
			w.newLine ();

			// Copy remainder of data.
			s = r.readLine ();
			while (s != null) {
				w.write (s);
				w.newLine ();
				s = r.readLine ();
			}

			// Close stuff.
			r.close ();
			w.flush ();

			return writer;
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
	}

	private void goToBlock (int targetBlock) throws IOException {
		text = r.readLine ();

		// Go to start of first block.
		while (text.charAt (0) != '>') {
			text = r.readLine ();
		}

		while (block < targetBlock) {
			// At start of loop, we are at beginning of block.

			// Find next block divider.
			text = r.readLine ();
			while (text.charAt (0) != '=') {
				text = r.readLine ();
			}

			// Find next start.
			text = r.readLine ();
			while (text.charAt (0) != '>') {
				text = r.readLine ();
			}

			block++;
		}
	}

	private void readSequence () throws IOException {
		StringBuffer buf = new StringBuffer ();

		if (text.charAt (0) == '=') {
			sequence = null;
			return;
		}

		// Sanity check.
		if (text.charAt (0) != '>') {
			throw new RuntimeException ("Parsing implementation error.");
		}

		// Parse out the range for use in title.
		if (text.indexOf ('+') == -1) {
			text = text.substring (1).trim ();
		} else {
			text = text.substring (1, text.indexOf ('+') - 1);
		}
		String [] parts = text.split (":");
		if (parts.length > 0) {
			String rangeParts[] = parts[1].trim ().split ("-");
			range = new long [2];
			range[0] = Long.parseLong (rangeParts[0].trim ());
			range[1] = Long.parseLong (rangeParts[1].trim ());
		}

		// Get the rest of the data.
		text = r.readLine ();
		while ((text.charAt (0) != '>') && (text.charAt (0) != '=')) {
			buf.append (text.trim ());
			text = r.readLine ();
		}

		sequence = buf.toString ();
	}

	public String getDescription () {
		return "XMFA Alignment parser";
	}

}
