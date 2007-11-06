package org.gel.mauve.gui;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.gel.mauve.MyConsole;

public class MauveAlignFrame extends AlignFrame {
	
    // member declarations
    JLabel islandSizeLabel = new JLabel();
    JTextField islandSizeText = new JTextField();
    JLabel backboneGapSizeLabel = new JLabel();
    JTextField backboneGapSizeText = new JTextField();
    JLabel backboneSizeLabel = new JLabel();
    JTextField backboneSizeText = new JTextField();
    JCheckBox extendLcbsCheckBox = new JCheckBox();

    JComboBox alignerChoice = new JComboBox(new String[] {"Muscle 3.6", "ClustalW 1.8.4"});
    JLabel alignerChoiceLabel = new JLabel("Aligner:", JLabel.RIGHT);

    Dimension d;
    
    public MauveAlignFrame(Mauve mauve)
    {
    	super(mauve);
    }

    public void initComponents()
    {
    	super.initComponents();

    	// set locations for components in AlignFrame superclass
        defaultSeedCheckBox.setLocation(new java.awt.Point(10, 10));
        determineLCBsCheckBox.setLocation(new java.awt.Point(10, 90));
        seedLengthSlider.setLocation(new java.awt.Point(200, 30));
        seedLengthLabel.setLocation(new java.awt.Point(210, 10));
        recursiveCheckBox.setLocation(new java.awt.Point(10, 145));
        collinearCheckBox.setLocation(new java.awt.Point(10, 110));
        d = minLcbWeightLabel.getPreferredSize();
        minLcbWeightLabel.setLocation(new java.awt.Point(265 - d.width, 90));
        minLcbWeightText.setLocation(new java.awt.Point(270, 90));
    	
        // initialize mauveAligner specific components
        extendLcbsCheckBox.setVisible(true);
        extendLcbsCheckBox.setSize(new java.awt.Dimension(120, 20));
        extendLcbsCheckBox.setText("Extend LCBs");
        extendLcbsCheckBox.setSelected(true);
        extendLcbsCheckBox.setLocation(new java.awt.Point(10, 165));
        extendLcbsCheckBox.setToolTipText("Disabling this may speed up alignment at the expense of sensitivity to some rearrangements.");
        d = alignerChoice.getPreferredSize();
        alignerChoice.setSize(d);
        alignerChoice.setLocation(new java.awt.Point(330 - d.width, 145));
        alignerChoice.setVisible(true);
        d = alignerChoiceLabel.getPreferredSize();
        alignerChoiceLabel.setSize(new Dimension(d.width, 20));
        alignerChoiceLabel.setLocation(alignerChoice.getLocation().x - d.width - 10, 145);
        alignerChoiceLabel.setVisible(true);
        islandSizeLabel.setSize(new java.awt.Dimension(135, 20));
        islandSizeLabel.setLocation(new java.awt.Point(135, 190));
        islandSizeLabel.setVisible(true);
        islandSizeLabel.setText("Minimum Island Size:");
        islandSizeText.setVisible(true);
        islandSizeText.setSize(new java.awt.Dimension(60, 20));
        islandSizeText.setLocation(new java.awt.Point(270, 190));
        islandSizeText.setText("50");
        backboneGapSizeLabel.setSize(new java.awt.Dimension(185, 20));
        backboneGapSizeLabel.setLocation(new java.awt.Point(85, 220));
        backboneGapSizeLabel.setVisible(true);
        backboneGapSizeLabel.setText("Maximum Backbone Gap Size:");
        backboneGapSizeText.setVisible(true);
        backboneGapSizeText.setSize(new java.awt.Dimension(60, 20));
        backboneGapSizeText.setLocation(new java.awt.Point(270, 220));
        backboneGapSizeText.setText("50");
        backboneGapSizeText.setToolTipText("Segments of backbone may not contain gaps larger than the given size");
        backboneSizeLabel.setSize(new java.awt.Dimension(155, 20));
        backboneSizeLabel.setLocation(new java.awt.Point(115, 250));
        backboneSizeLabel.setVisible(true);
        backboneSizeLabel.setText("Minimum Backbone Size:");
        backboneSizeText.setVisible(true);
        backboneSizeText.setSize(new java.awt.Dimension(60, 20));
        backboneSizeText.setLocation(new java.awt.Point(270, 250));
        backboneSizeText.setText("50");
        backboneSizeText.setToolTipText("Segments of backbone must contain at least this many nucleotides without a gap larger than the minimum backbone gap size");
        alignButton.setLocation(new java.awt.Point(250, 320));

        parameterPanel.add(extendLcbsCheckBox);
        parameterPanel.add(alignerChoice);
        parameterPanel.add(alignerChoiceLabel);
        parameterPanel.add(islandSizeLabel);
        parameterPanel.add(islandSizeText);
        parameterPanel.add(backboneGapSizeLabel);
        parameterPanel.add(backboneGapSizeText);
        parameterPanel.add(backboneSizeLabel);
        parameterPanel.add(backboneSizeText);

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
    }
    
    protected String[] makeAlignerCommand()
    {
        Vector cmd_vec = new Vector();
        read_filename = null;
        String cur_cmd;
        boolean detect_lcbs = true;
        String os_type = System.getProperty("os.name");
        String os_arch = System.getProperty("os.arch");

        MyConsole.out().println("OS name is: " + os_type + " arch: " + os_arch);
        if (os_type.startsWith("Windows"))
        {
        	if(os_arch.indexOf("64") >= 0)
        		cmd_vec.addElement("win64/");
            cmd_vec.addElement("mauveAligner");
        }
        else if (os_type.startsWith("Mac"))
        {
            String mauve_path = System.getProperty("user.dir");
            mauve_path += "/Mauve.app/Contents/MacOS/mauveAligner";
            cmd_vec.addElement(mauve_path);
        }
        else
        {
        	File f = new File("./mauveAligner");
        	if( f.exists())
        		cmd_vec.addElement("./mauveAligner");
        	else
        		cmd_vec.addElement("mauveAligner");
        }

        if (getSeedWeight() > 0)
        {
            cur_cmd = "--seed-size=";
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

        detect_lcbs = isLCBSearchEnabled();
        if (detect_lcbs)
        {

            if (!getRecursive())
            {
                cmd_vec.addElement("--no-recursion");
            }

            if (!getExtendLcbs())
            {
                cmd_vec.addElement("--no-lcb-extension");
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

            if (getRecursive())
            {
            	if( alignerChoice.getSelectedIndex() == 1 )
            	{
            		cur_cmd = "--gapped-aligner=clustal";
            		cmd_vec.addElement(cur_cmd);
            	}
                if (getIslandSize() != -1)
                {
                    cur_cmd = "--island-size=";
                    cur_cmd += Integer.toString(getIslandSize());
                    cmd_vec.addElement(cur_cmd);

                    cur_cmd = "--island-output=" + output_file + ".islands";
                    cmd_vec.addElement(cur_cmd);
                }

                if (getBackboneSize() != -1 && getBackboneGapSize() != -1)
                {
                    cur_cmd = "--backbone-size=";
                    cur_cmd += Integer.toString(getBackboneSize());
                    cmd_vec.addElement(cur_cmd);

                    cur_cmd = "--max-backbone-gap=";
                    cur_cmd += Integer.toString(getBackboneGapSize());
                    cmd_vec.addElement(cur_cmd);

                    cur_cmd = "--backbone-output=" + output_file + ".backbone";
                    cmd_vec.addElement(cur_cmd);
                }
                // make an identity matrix file name
                cur_cmd = "--id-matrix=" + output_file + ".id_matrix";
                cmd_vec.addElement(cur_cmd);

                // make an alignment file name
                cur_cmd = "--output-alignment=" + output_file + ".alignment";
                cmd_vec.addElement(cur_cmd);

                read_filename = output_file + ".alignment";
            }

            // make a guide tree file name
            cur_cmd = "--output-guide-tree=" + output_file + ".guide_tree";
            cmd_vec.addElement(cur_cmd);
        }
        else if (!detect_lcbs)
        {
            cur_cmd = "--mums";
            cmd_vec.addElement(cur_cmd);
            cur_cmd = "--eliminate-inclusions";
            cmd_vec.addElement(cur_cmd);
        }

        String[] sequences = getSequences();
        for (int seqI = 0; seqI < sequences.length; seqI++)
        {
            cmd_vec.addElement(sequences[seqI]);
            if (sequences.length > 1)
            {
                cur_cmd = sequences[seqI] + ".sml";
                cmd_vec.addElement(cur_cmd);
            }
        }

        String[] mauve_cmd = new String[cmd_vec.size()];
        mauve_cmd = (String[]) (cmd_vec.toArray(mauve_cmd));

        return mauve_cmd;
    }

    public void determineLCBsCheckBoxActionPerformed(java.awt.event.ActionEvent e)
    {
    	super.determineLCBsCheckBoxActionPerformed(e);
        if (determineLCBsCheckBox.isSelected())
        {
            recursiveCheckBox.setEnabled(true);
            if (recursiveCheckBox.isSelected())
            {
                islandSizeLabel.setEnabled(true);
                islandSizeText.setEnabled(true);
                backboneGapSizeLabel.setEnabled(true);
                backboneGapSizeText.setEnabled(true);
                backboneSizeLabel.setEnabled(true);
                backboneSizeText.setEnabled(true);
                extendLcbsCheckBox.setEnabled(true);
                alignerChoice.setEnabled(true);
                alignerChoiceLabel.setEnabled(true);
            }
        }
        else
        {
            recursiveCheckBox.setEnabled(false);
            islandSizeLabel.setEnabled(false);
            islandSizeText.setEnabled(false);
            backboneGapSizeLabel.setEnabled(false);
            backboneGapSizeText.setEnabled(false);
            backboneSizeLabel.setEnabled(false);
            backboneSizeText.setEnabled(false);
            extendLcbsCheckBox.setEnabled(false);
            alignerChoice.setEnabled(false);
            alignerChoiceLabel.setEnabled(false);
        }
    }

    public void recursiveCheckBoxActionPerformed(java.awt.event.ActionEvent e)
    {
        if (recursiveCheckBox.isSelected())
        {
            islandSizeLabel.setEnabled(true);
            islandSizeText.setEnabled(true);
            backboneGapSizeLabel.setEnabled(true);
            backboneGapSizeText.setEnabled(true);
            backboneSizeLabel.setEnabled(true);
            backboneSizeText.setEnabled(true);
            extendLcbsCheckBox.setEnabled(true);
        }
        else
        {
            islandSizeLabel.setEnabled(false);
            islandSizeText.setEnabled(false);
            backboneGapSizeLabel.setEnabled(false);
            backboneGapSizeText.setEnabled(false);
            backboneSizeLabel.setEnabled(false);
            backboneSizeText.setEnabled(false);
            extendLcbsCheckBox.setEnabled(false);
        }
    }



    public boolean getExtendLcbs()
    {
        return extendLcbsCheckBox.isSelected();
    }

    public boolean isLCBSearchEnabled()
    {
        return determineLCBsCheckBox.isSelected();
    }

    public int getIslandSize()
    {
        try
        {
            return Integer.parseInt(islandSizeText.getText());
        }
        catch (NumberFormatException nfe)
        {
            return -1;
        }
    }

    public int getBackboneSize()
    {
        try
        {
            return Integer.parseInt(backboneSizeText.getText());
        }
        catch (NumberFormatException nfe)
        {
            return -1;
        }
    }

    public int getBackboneGapSize()
    {
        try
        {
            return Integer.parseInt(backboneGapSizeText.getText());
        }
        catch (NumberFormatException nfe)
        {
            return -1;
        }
    }


}
