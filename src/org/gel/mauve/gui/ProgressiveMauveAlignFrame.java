package org.gel.mauve.gui;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.gel.mauve.MyConsole;

public class ProgressiveMauveAlignFrame extends AlignFrame implements ChangeListener {
	
    // member declarations
    Dimension d;
    JCheckBox refineCheckBox = new JCheckBox();
    JLabel islandScoreLabel = new JLabel();
    JTextField islandScoreText = new JTextField();
    
    JCheckBox sumOfPairsCheckBox = new JCheckBox();
    JSlider breakpointWeightScaleSlider = new JSlider();
    JLabel breakpointWeightScaleLabel = new JLabel();
    JTextField breakpointWeightScaleText = new JTextField(5);
    JSlider conservationWeightScaleSlider = new JSlider();
    JLabel conservationWeightScaleLabel = new JLabel();
    JTextField conservationWeightScaleText = new JTextField(5);

    public ProgressiveMauveAlignFrame(Mauve mauve)
    {
    	super(mauve);
    }

    public void initComponents()
    {
    	super.initComponents();
    	
        // the following code sets the frame's initial state
        defaultSeedCheckBox.setLocation(new java.awt.Point(10, 10));
        determineLCBsCheckBox.setLocation(new java.awt.Point(10, 30));
        seedLengthSlider.setLocation(new java.awt.Point(200, 30));
        seedLengthLabel.setLocation(new java.awt.Point(210, 10));
        recursiveCheckBox.setLocation(new java.awt.Point(10, 70));
        collinearCheckBox.setLocation(new java.awt.Point(10, 50));
        d = minLcbWeightLabel.getPreferredSize();
        minLcbWeightLabel.setLocation(new java.awt.Point(265 - d.width, 90));
        minLcbWeightText.setLocation(new java.awt.Point(270, 90));
        alignButton.setLocation(new java.awt.Point(250, 320));

        // initialize progressiveMauve-specific configuration options
        refineCheckBox.setVisible(true);
        refineCheckBox.setSize(new java.awt.Dimension(160, 20));
        refineCheckBox.setText("Iterative refinement");
        refineCheckBox.setSelected(true);
        refineCheckBox.setLocation(new java.awt.Point(10, 90));
        refineCheckBox.setToolTipText("Iteratively refines the alignment, significantly improving accuracy");
        islandScoreLabel.setSize(new java.awt.Dimension(175, 20));
        islandScoreLabel.setLocation(new java.awt.Point(135, 115));
        islandScoreLabel.setVisible(true);
        islandScoreLabel.setText("Island Score Threshold:");
        islandScoreText.setVisible(true);
        islandScoreText.setSize(new java.awt.Dimension(60, 20));
        islandScoreText.setLocation(new java.awt.Point(270, 115));
        islandScoreText.setText("2727");

        sumOfPairsCheckBox.setVisible(true);
        sumOfPairsCheckBox.setSize(new java.awt.Dimension(180, 20));
        sumOfPairsCheckBox.setText("Sum-of-pairs LCB scoring");
        sumOfPairsCheckBox.setSelected(true);
        sumOfPairsCheckBox.setLocation(new java.awt.Point(10, 135));
        sumOfPairsCheckBox.setToolTipText("Set to use sum-of-pairs scoring instead of scoring LCBs against an inferred ancestral order");

        breakpointWeightScaleLabel.setSize(new java.awt.Dimension(195, 20));
        breakpointWeightScaleLabel.setVisible(true);
        breakpointWeightScaleLabel.setText("Breakpoint dist. weight scaling:");
        breakpointWeightScaleLabel.setLocation(new java.awt.Point(10, 155));

        breakpointWeightScaleSlider.setMinimum(0);
        breakpointWeightScaleSlider.setMaximum(100);
        breakpointWeightScaleSlider.setValue(50);
        breakpointWeightScaleSlider.setMinorTickSpacing(5);
        breakpointWeightScaleSlider.setMajorTickSpacing(10);
        breakpointWeightScaleSlider.setToolTipText("Set the pairwise breakpoint distance scaling for LCB weight");
        breakpointWeightScaleSlider.setPaintTicks(true);
        breakpointWeightScaleSlider.setPaintLabels(false);
        breakpointWeightScaleSlider.setSnapToTicks(false);
        d = breakpointWeightScaleSlider.getPreferredSize();
//        d.setSize(125, d.getHeight());
        breakpointWeightScaleSlider.setPreferredSize(d);
        breakpointWeightScaleSlider.setMaximumSize(d);
        breakpointWeightScaleSlider.addChangeListener(this);
        breakpointWeightScaleSlider.setLocation(new java.awt.Point(10, 175));
        breakpointWeightScaleSlider.setVisible(true);
        breakpointWeightScaleSlider.setEnabled(true);

        d = breakpointWeightScaleText.getPreferredSize();
        d.setSize(40, d.getHeight());
        breakpointWeightScaleText.setPreferredSize(d);
        breakpointWeightScaleText.setMaximumSize(d);
        breakpointWeightScaleText.setHorizontalAlignment(JTextField.RIGHT);
        breakpointWeightScaleText.setText("0.500");
        breakpointWeightScaleText.setLocation(new java.awt.Point(200, 175));
        

        conservationWeightScaleLabel.setSize(new java.awt.Dimension(195, 20));
        conservationWeightScaleLabel.setVisible(true);
        conservationWeightScaleLabel.setText("Conservation dist. weight scaling:");
        conservationWeightScaleLabel.setLocation(new java.awt.Point(10, 210));

        conservationWeightScaleSlider.setMinimum(0);
        conservationWeightScaleSlider.setMaximum(100);
        conservationWeightScaleSlider.setValue(50);
        conservationWeightScaleSlider.setMinorTickSpacing(5);
        conservationWeightScaleSlider.setMajorTickSpacing(10);
        conservationWeightScaleSlider.setToolTipText("Set the pairwise conservation distance scaling for LCB weight");
        conservationWeightScaleSlider.setPaintTicks(true);
        conservationWeightScaleSlider.setPaintLabels(false);
        conservationWeightScaleSlider.setSnapToTicks(false);
        d = conservationWeightScaleSlider.getPreferredSize();
        d.setSize(125, d.getHeight());
        conservationWeightScaleSlider.setPreferredSize(d);
        conservationWeightScaleSlider.setMaximumSize(d);
        conservationWeightScaleSlider.addChangeListener(this);
        conservationWeightScaleSlider.setLocation(new java.awt.Point(10, 230));
        conservationWeightScaleSlider.setVisible(true);
        conservationWeightScaleSlider.setEnabled(true);

        d = conservationWeightScaleText.getPreferredSize();
        d.setSize(40, d.getHeight());
        conservationWeightScaleText.setPreferredSize(d);
        conservationWeightScaleText.setMaximumSize(d);
        conservationWeightScaleText.setHorizontalAlignment(JTextField.RIGHT);
        conservationWeightScaleText.setText("0.500");
        conservationWeightScaleText.setLocation(new java.awt.Point(200, 230));

        parameterPanel.add(refineCheckBox);
        parameterPanel.add(sumOfPairsCheckBox);
        parameterPanel.add(islandScoreLabel);
        parameterPanel.add(islandScoreText);

        parameterPanel.add(breakpointWeightScaleLabel);
        parameterPanel.add(breakpointWeightScaleSlider);
        parameterPanel.add(breakpointWeightScaleText);

        parameterPanel.add(conservationWeightScaleLabel);
        parameterPanel.add(conservationWeightScaleSlider);
        parameterPanel.add(conservationWeightScaleText);

        // event handling
        determineLCBsCheckBox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent e)
            {
                determineLCBsCheckBoxActionPerformed(e);
            }
        });

        recursiveCheckBox.addActionListener(new java.awt.event.ActionListener()
                {
                    public void actionPerformed(java.awt.event.ActionEvent e)
                    {
                        recursiveCheckBoxActionPerformed(e);
                    }
                });
        collinearCheckBox.addActionListener(new java.awt.event.ActionListener()
                {
                    public void actionPerformed(java.awt.event.ActionEvent e)
                    {
                    	collinearCheckBoxActionPerformed(e);
                    }
                });
    }

    File getDefaultFile() throws IOException
    {
        return File.createTempFile("mauve", ".xmfa");
    }

    public void stateChanged(ChangeEvent e)
    {
        if (e.getSource() == breakpointWeightScaleSlider)
        {
            double w = (double)breakpointWeightScaleSlider.getValue();
            Double d = new Double(w/100);
            breakpointWeightScaleText.setText(d.toString());
        }
        if (e.getSource() == breakpointWeightScaleText)
        {
        }
    }
    
    protected String[] makeAlignerCommand()
    {
        Vector cmd_vec = new Vector();
        read_filename = null;
        String cur_cmd;
        boolean detect_lcbs = true;
        String os_type = System.getProperty("os.name");

        MyConsole.out().println("OS name is: " + os_type);
        if (os_type.startsWith("Windows"))
        {
            cmd_vec.addElement("progressiveMauve");
        }
        else if (os_type.startsWith("Mac"))
        {
            String mauve_path = System.getProperty("user.dir");
            mauve_path += "/Mauve.app/Contents/MacOS/progressiveMauve";
            cmd_vec.addElement(mauve_path);
        }
        else
        {
        	File f = new File("./progressiveMauve");
        	if( f.exists())
        		cmd_vec.addElement("./progressiveMauve");
        	else
        		cmd_vec.addElement("progressiveMauve");
        }

        if (getSeedWeight() > 0)
        {
            cur_cmd = "--seed-weight=";
            cur_cmd += Integer.toString(getSeedWeight());
            cmd_vec.addElement(cur_cmd);
        }

        // get a good output file name
        String output_file = getOutput();
        cur_cmd = "--output=";
        try
        {
            output_file = makeOutputFile(output_file);
        }
        catch (IOException e)
        {
            MyConsole.err().println("Couldn't create output file.");
            e.printStackTrace(MyConsole.err());
            return null;
        }
        cur_cmd += output_file;
        cmd_vec.addElement(cur_cmd);

        read_filename = output_file;
        
    	cmd_vec.addElement("--backbone-output=" + output_file + ".backbone");

        detect_lcbs = isLCBSearchEnabled();
        if (detect_lcbs)
        {

            if (!getRecursive())
            {
                cmd_vec.addElement("--skip-gapped-alignment");
            }
            if(!getRefine())
            {
            	cmd_vec.addElement("--skip-refinement");
            }
            if(!getSumOfPairs())
            {
            	cmd_vec.addElement("--ancestral-scoring");
            }

            if( getCollinear() )
            {
            	cmd_vec.addElement("--collinear");
            }

            if (getMinLcbWeight() != -1)
            {
                cur_cmd = "--weight=";
                cur_cmd += Integer.toString(getMinLcbWeight());
                cmd_vec.addElement(cur_cmd);
            }

            // make a guide tree file name
            cur_cmd = "--output-guide-tree=" + output_file + ".guide_tree";
            cmd_vec.addElement(cur_cmd);
        }
        else if (!detect_lcbs)
        {
            cur_cmd = "--mums";
            cmd_vec.addElement(cur_cmd);
        }

        String[] sequences = getSequences();
        for (int seqI = 0; seqI < sequences.length; seqI++)
        {
            cmd_vec.addElement(sequences[seqI]);
        }

        String[] mauve_cmd = new String[cmd_vec.size()];
        mauve_cmd = (String[]) (cmd_vec.toArray(mauve_cmd));

        return mauve_cmd;
    }

    public void updateEnabledStates()
    {
        if (determineLCBsCheckBox.isSelected())
        {
            refineCheckBox.setEnabled(true);
            islandScoreText.setEnabled(true);
            islandScoreLabel.setEnabled(true);
        }
        else
        {
        	refineCheckBox.setEnabled(false);
            islandScoreText.setEnabled(false);
            islandScoreLabel.setEnabled(false);
        }
    	if(collinearCheckBox.isSelected() || !determineLCBsCheckBox.isSelected())
    	{
        	sumOfPairsCheckBox.setEnabled(false);
    		breakpointWeightScaleText.setEnabled(false);
    		breakpointWeightScaleSlider.setEnabled(false);
    		breakpointWeightScaleLabel.setEnabled(false);
    		conservationWeightScaleText.setEnabled(false);
    		conservationWeightScaleSlider.setEnabled(false);
    		conservationWeightScaleLabel.setEnabled(false);
    	}else if(determineLCBsCheckBox.isSelected())
    	{
        	sumOfPairsCheckBox.setEnabled(true);
    		breakpointWeightScaleText.setEnabled(true);
    		breakpointWeightScaleSlider.setEnabled(true);
    		breakpointWeightScaleLabel.setEnabled(true);
    		conservationWeightScaleText.setEnabled(true);
    		conservationWeightScaleSlider.setEnabled(true);
    		conservationWeightScaleLabel.setEnabled(true);
    	}
    }
    public void determineLCBsCheckBoxActionPerformed(java.awt.event.ActionEvent e)
    {
    	super.determineLCBsCheckBoxActionPerformed(e);
    	updateEnabledStates();
    }

    public void collinearCheckBoxActionPerformed(java.awt.event.ActionEvent e)
    {
    	super.collinearCheckBoxActionPerformed(e);
    	updateEnabledStates();
    }
    public void recursiveCheckBoxActionPerformed(java.awt.event.ActionEvent e)
    {
    	updateEnabledStates();
    }

    public boolean getRefine()
    {
    	if(refineCheckBox.isEnabled())
    		return refineCheckBox.isSelected();
    	return false;
    }

    public boolean getSumOfPairs()
    {
    	if(sumOfPairsCheckBox.isEnabled())
    		return sumOfPairsCheckBox.isSelected();
    	return false;
    }

    public int getIslandScoreThreshold()
    {
        try
        {
            return Integer.parseInt(islandScoreText.getText());
        }
        catch (NumberFormatException nfe)
        {
            return -1;
        }
    }
}
