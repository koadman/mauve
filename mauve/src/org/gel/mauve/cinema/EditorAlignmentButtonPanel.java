package org.gel.mauve.cinema;

import java.awt.Insets;

import javax.swing.JButton;

import uk.ac.man.bioinf.gui.viewer.JAlignmentButtonPanel;
import uk.ac.man.bioinf.sequence.alignment.GappedSequence;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;

public class EditorAlignmentButtonPanel extends JAlignmentButtonPanel {

	public EditorAlignmentButtonPanel () {
		super ();
	}

	public EditorAlignmentButtonPanel (SequenceAlignment msa) {
		super (msa);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.man.bioinf.gui.viewer.JAlignmentButtonPanel#setButton(uk.ac.man.bioinf.sequence.alignment.GappedSequence,
	 *      javax.swing.JButton)
	 */
	public void setButton (GappedSequence seq, JButton button) {
		super.setButton (seq, button);
		button.setMargin (new Insets (0, 0, 0, 0));
		button.setToolTipText (button.getText ());
		button.setFont (button.getFont ().deriveFont (10f));
	}
}
