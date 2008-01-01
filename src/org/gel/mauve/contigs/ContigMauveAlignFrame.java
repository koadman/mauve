package org.gel.mauve.contigs;

import java.awt.Dimension;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JScrollBar;

import org.gel.mauve.gui.Mauve;
import org.gel.mauve.gui.ProgressiveMauveAlignFrame;

public class ContigMauveAlignFrame extends ProgressiveMauveAlignFrame {
	
	protected ContigOrderer orderer;

	public ContigMauveAlignFrame(Mauve mauve, ContigOrderer orderer) {
		super(mauve);
		this.orderer = orderer;
		initComponents ();
	}
	
	public void initComponents () {
		super.initComponents();
		setTitle("Align and Reorder Contigs");
		sequencesPanel.remove (addButton);
		sequencesPanel.remove (removeButton);
		minLcbWeightText.setText("200");
		refineCheckBox.setSelected(false);
		JLabel seq_label = new JLabel ("1st is reference," +
				" 2nd contains contigs to reorder.");
		seq_label.setLocation(new java.awt.Point(30, 150));
		seq_label.setVisible(true);
		seq_label.setSize(new Dimension (290, 20));
		alignButton.setText("Start");
		sequencesPanel.add(seq_label);
		sequenceList.setDropActive (false);
	}
	
	public void setVisible (boolean show) {
		if (show) {
			String text = "Cancel ";
			if (orderer.count < orderer.iterations)
				text += "reorder " + (orderer.count - orderer.start);
			else
				text += "final";
			cancelButton.setText(text);
		}
		super.setVisible(show);
	}
	
	public void displayFileInput () {
		try {
			sequenceListModel.clear ();
			File dir = orderer.getAlignDir ();
			dir.mkdirs ();
			setOutput(dir.getAbsolutePath () + File.separatorChar +
					orderer.DIR_STUB + orderer.count);
			sequenceListModel.addElement (orderer.reference.getAbsolutePath ());
			sequenceListModel.addElement (orderer.unordered.getAbsolutePath ());
			JScrollBar scroller = listScrollPane.getHorizontalScrollBar ();
			scroller.setValue (scroller.getMaximum ());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
