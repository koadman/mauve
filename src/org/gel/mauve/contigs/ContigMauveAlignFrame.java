package org.gel.mauve.contigs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;

import org.gel.mauve.gui.Mauve;
import org.gel.mauve.gui.ProgressiveMauveAlignFrame;

public class ContigMauveAlignFrame extends ProgressiveMauveAlignFrame {
	
	protected ContigOrderer orderer;
	protected boolean first;
	protected File current_dir;

	public ContigMauveAlignFrame(Mauve mauve, ContigOrderer orderer) {
		super(mauve);
		this.orderer = orderer;
		initComponents ();
	}
	
	public void initComponents () {
		super.initComponents();
		setTitle("Align and Reorder Contigs");
		first = true;
		minLcbWeightText.setText("200");
		refineCheckBox.setSelected(false);
		seedFamiliesCheckBox.setSelected(false);
		JLabel seq_label = new JLabel ("<html>Reference sequence expected first,<br/>" +
				" draft second.</html>");
		seq_label.setLocation(new Point(30, 184));
		seq_label.setVisible(true);
		seq_label.setSize(new Dimension (290, 25));
		outputFileText.setLocation(new Point (85, 240));
		outputButton.setLocation(new Point (310, 240));
		outputLabel.setText("Output: ");
		outputLabel.setLocation(new Point (10, 240));
		alignButton.setText("Start");
		sequencesPanel.add(seq_label);
	}
	
	public void setVisible (boolean show) {
		System.out.println ("done with init");
		if (show) {
			String text = "Cancel ";
			if (orderer.count < orderer.iterations)
				text += "reorder " + (orderer.count - orderer.start);
			else
				text += "final";
			cancelButton.setText(text);
		}
		super.setVisible(show);
		System.out.println ("shown");
	}
	
	public void displayFileInput () {
		try {
			sequenceListModel.clear ();
			current_dir = orderer.getAlignDir ();
			//current_dir.mkdirs ();
			setOutput(current_dir.getParentFile ().getAbsolutePath ());
			if (first) {
				outputFileText.setEditable(false);
				outputFileText.setBackground(Color.white);
			}
			current_dir = new File (current_dir, orderer.DIR_STUB + orderer.count);
			JScrollBar scroller = listScrollPane.getHorizontalScrollBar ();
			scroller.setValue (scroller.getMaximum ());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void alignButtonActionPerformed (ActionEvent e) {
		DefaultListModel model = (DefaultListModel) sequenceList.getModel ();
		if (first) {
			sequenceList.setDropActive (false);
			if (sequenceList.getModel ().getSize() != 2) {
				JOptionPane.showMessageDialog(this,	"Alignment should be two sequences;" +
						"reference\nfirst followed by sequence to reorder.",
						"Wrong Number of Sequences", JOptionPane.ERROR_MESSAGE);
				return;
			}
			orderer.reference = new File ((String) model.getElementAt (0));
			orderer.unordered = new File ((String) model.getElementAt (1));
			orderer.copyInputFiles ();
			sequencesPanel.remove (addButton);
			sequencesPanel.remove (removeButton);
			first = false;
			orderer.directory = new File (getOutput ());
		}
		model.clear();
		model.addElement (orderer.reference.getAbsolutePath ());
		model.addElement (orderer.unordered.getAbsolutePath ());
		setOutput (current_dir.getAbsolutePath());
		super.alignButtonActionPerformed(e);
		setOutput (current_dir.getParentFile().getParentFile().getAbsolutePath());
	}
	
	public void addButtonActionPerformed (ActionEvent e) {
		fc.setFileSelectionMode(fc.FILES_ONLY);
		super.addButtonActionPerformed(e);
	}
	
	public void outputButtonActionPerformed (ActionEvent e) {
		fc.setFileSelectionMode(fc.DIRECTORIES_ONLY);
		super.outputButtonActionPerformed(e);
	}
	
}
