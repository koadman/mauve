package org.gel.mauve.cinema;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.ac.man.bioinf.apps.cinema.CinemaGuiModule;
import uk.ac.man.bioinf.gui.viewer.AlignmentSelectionModel;
import uk.ac.man.bioinf.gui.viewer.SequenceCursor;
import uk.ac.man.bioinf.gui.viewer.event.AlignmentSelectionEvent;
import uk.ac.man.bioinf.gui.viewer.event.AlignmentSelectionListener;
import uk.ac.man.bioinf.sequence.alignment.EmptySequenceAlignment;
import uk.ac.man.bioinf.sequence.alignment.GappedSequence;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;
import uk.ac.man.bioinf.sequence.geom.SequenceAlignmentPoint;
import uk.ac.man.bioinf.sequence.geom.SequenceAlignmentRectangle;
import uk.ac.man.bioinf.sequence.identifier.Identifier;
import uk.ac.man.bioinf.sequence.identifier.NoIdentifier;

// Cribbed almost entirely from CinemaStatusInformation.
public class XMFAStatusInformation extends CinemaGuiModule implements
		PropertyChangeListener {
	public void start () {

		getViewer ().addPropertyChangeListener (this);

		addSelectionListener (null, getViewer ().getSelectionModel ());
		addCursorListener (null, getViewer ().getCursorModel ());
		printSequenceInfo ();

	}

	public void propertyChange (PropertyChangeEvent event) {
		if (event.getPropertyName ().equals ("sequenceAlignment")) {
			printSequenceInfo ();
		}

		if (event.getPropertyName ().equals ("alignmentSelectionModel")) {
			addSelectionListener ((AlignmentSelectionModel) event
					.getOldValue (), (AlignmentSelectionModel) event
					.getNewValue ());
		}

		if (event.getPropertyName ().equals ("sequenceCursor")) {
			addCursorListener ((SequenceCursor) event.getOldValue (),
					(SequenceCursor) event.getNewValue ());
		}
	}

	private void printSequenceInfo () {
		SequenceAlignment seq = getViewer ().getSequenceAlignment ();

		if (!(seq instanceof EmptySequenceAlignment)) {
			sendStatusMessage ("Loaded " + seq.getIdentifier ().getTitle ()
					+ " from " + seq.getIdentifier ().getSource ().getTitle ());

			Identifier identifier;
			if ((identifier = seq.getIdentifier ()) instanceof NoIdentifier) {
				setFrameTitle (identifier.getSource ().getTitle ());
			} else {
				setFrameTitle (identifier.getTitle ());
			}
		}
	}

	private AlignmentSelectionListener selectionListener;

	private void addSelectionListener (AlignmentSelectionModel oldMod,
			AlignmentSelectionModel newMod) {
		if (selectionListener == null) {
			selectionListener = new AlignmentSelectionListener () {
				public void valueChanged (AlignmentSelectionEvent event) {
					printSelectionInfo (event.getSelectionRectangle ());
				}
			};
		}

		if (oldMod != null)
			oldMod.removeAlignmentSelectionListener (selectionListener);
		if (newMod != null)
			newMod.addAlignmentSelectionListener (selectionListener);
	}

	private void printSelectionInfo (SequenceAlignmentRectangle rect) {
		if (rect != null) {
			sendStatusMessage ("Selection: ( " + rect.getX () + " , "
					+ rect.getY () + " - " + rect.getWidth () + " x "
					+ rect.getHeight () + " )");
		}
	}

	private ChangeListener cursorListener;

	private void addCursorListener (SequenceCursor oldMod, SequenceCursor newMod) {
		if (cursorListener == null) {
			cursorListener = new ChangeListener () {
				public void stateChanged (ChangeEvent event) {
					printCursorInfo ();
				}
			};
		}

		if (oldMod != null)
			oldMod.removeChangeListener (cursorListener);
		if (newMod != null)
			newMod.addChangeListener (cursorListener);
	}

	private void printCursorInfo () {
		SequenceAlignmentPoint point = getViewer ().getPoint ();
		GappedSequence seq = getSequenceAlignment ().getSequenceAt (
				point.getY ());
		XMFASequenceIdentifier ident = (XMFASequenceIdentifier) seq
				.getIdentifier ();

		int pos = seq.getUngappedPositionOf (point.getX ());
		// A value of -1 indicates the cursor is selecting a gap.
		if (pos == -1) {
			sendStatusMessage (ident.getTitle ());
		} else {
			sendStatusMessage (ident.getTitle ()
					+ " @ "
					+ (seq.getUngappedPositionOf (point.getX ())
							+ ident.getRange ()[0] - 1));
		}
	}

	public String getVersion () {
		return "0";
	}

}// CinemaStatusInformation
