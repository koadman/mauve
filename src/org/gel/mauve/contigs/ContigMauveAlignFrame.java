package org.gel.mauve.contigs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;

import org.gel.mauve.gui.AlignWorker;
import org.gel.mauve.gui.Mauve;
import org.gel.mauve.gui.ProgressiveMauveAlignFrame;

public class ContigMauveAlignFrame extends ProgressiveMauveAlignFrame {
	
	protected ContigOrderer orderer;
	protected boolean first;
	protected File current_dir;
	protected Hashtable <String, String> more_args;

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
		outputFileText.setEditable(false);
		outputFileText.setBackground(Color.white);
		sequencesPanel.remove(outputButton);
		//outputButton.setLocation(new Point (310, 240));
		outputLabel.setText("Output: ");
		outputLabel.setLocation(new Point (10, 240));
		alignButton.setText("Start");
		sequencesPanel.add(seq_label);
	}
	
	public void setArgs (Hashtable <String, String> args) {
		if (args.containsKey("--seed-family")) {
			seedFamiliesCheckBox.setSelected(true);
			args.remove("--seed-family");
		}
		if (args.containsKey("--seed-weight")) {
			seedLengthSlider.setValue(Integer.parseInt(args.get("--seed-weight")));
			args.remove("--seed-weight");
		}
		args.remove("--output");
		args.remove("--mums");
		args.remove("--apply-backbone");
		args.remove("--disable-backbone");
		args.remove("--collinear");
		args.remove("--output-guide-tree");
		args.remove("--backbone-output");
		more_args = args;
	}
	
    protected String[] makeAlignerCommand() {
    	String [] cmd = super.makeAlignerCommand();
    	if (more_args == null)
    		return cmd;
    	Vector <String> extra = new Vector <String> (); 
    	Iterator  <String> itty = more_args.keySet().iterator();
    	while (itty.hasNext()) {
    		String val = itty.next();
    		if (val.charAt (1) == '-') {
    			if (more_args.get(val).length() > 0)
    				val += "=" + more_args.get(val);
    			extra.add(val);
    		}
    	}
    	String [] temp = new String [cmd.length + extra.size()];
    	System.arraycopy(cmd, 0, temp, 0, cmd.length - 2);
    	if (extra.size() > 0)
    		System.arraycopy(extra.toArray(), 0, temp, cmd.length - 2, extra.size ());
    	//two sequences are last
    	System.arraycopy(cmd, cmd.length - 2, temp, temp.length - 2, 2);
    	return temp;
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
	//	System.out.println ("shown");
	}
	
	public void setFileInput () {
		try {
			current_dir = orderer.getAlignDir ();
			//System.err.println("AJT0403: current_dir was " + current_dir.getAbsolutePath());
			//current_dir.mkdirs ();
			setOutput(current_dir.getParentFile ().getAbsolutePath ());
			if (!first)
				sequenceListModel.clear ();
			current_dir = new File (current_dir, orderer.DIR_STUB + orderer.count);
			//System.err.println("AJT0403: now current_dir is " + current_dir.getAbsolutePath());
			JScrollBar scroller = listScrollPane.getHorizontalScrollBar ();
			scroller.setValue (scroller.getMaximum ());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addSequence (String file) {
		DefaultListModel model = (DefaultListModel) sequenceList.getModel ();
		model.addElement(file);
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
		/*
		 * this call to setOutput may be redundant. It should have already been called in setFileInput 
		 */
		setOutput (current_dir.getAbsolutePath());
		super.worker = new AlignWorker(this, super.makeAlignerCommand());
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
