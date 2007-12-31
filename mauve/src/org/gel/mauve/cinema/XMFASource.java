package org.gel.mauve.cinema;

import org.gel.mauve.XmfaViewerModel;

import uk.ac.man.bioinf.sequence.alignment.event.AlignmentEvent;
import uk.ac.man.bioinf.sequence.alignment.event.AlignmentListener;
import uk.ac.man.bioinf.sequence.identifier.Source;

public class XMFASource implements Source, AlignmentListener {
	private XmfaViewerModel model;

	private int lcbIndex;

	private int [] viewingOrder;

	private boolean dirty = false;

	public XMFASource (XmfaViewerModel model, int lcbIndex) {
		this.model = model;
		this.lcbIndex = lcbIndex;

		// The viewing order is stored locally to avoid problems wherein
		// the user rearranges the sequences with the editor open, and then
		// saves.
		viewingOrder = new int [model.getSequenceCount ()];

		// Todo: compute viewing order.
		for (int i = 0; i < model.getSequenceCount (); i++) {
			viewingOrder[i] = model.getGenomeByViewingIndex (i)
					.getSourceIndex ();
		}
	}

	public XmfaViewerModel getModel () {
		return model;
	}

	public int getLcbIndex () {
		return lcbIndex;
	}

	public int [] getViewingOrder () {
		return viewingOrder;
	}

	public String getTitle () {
		return model.getSrc ().getName ();
	}

	public boolean isDirty () {
		return dirty;
	}

	public void setDirty (boolean dirty) {
		this.dirty = dirty;
	}

	public void changeOccurred (AlignmentEvent event) {
		dirty = true;
	}
}
