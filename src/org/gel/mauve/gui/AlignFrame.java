/** 
 * AlignFrame.java
 *
 * Description:	
 * @author			koadman
 * @version			
 */

package org.gel.mauve.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.gel.mauve.MyConsole;
import org.gel.mauve.gui.dnd.DnDList;

/**
 * A dialog box implementing a graphical interface to the command-line
 * mauveAligner tool. Allows the user to manipulate various alignment options.
 * Originally created with Metrowerks java gui designer.
 */
public class AlignFrame extends java.awt.Frame
{
    // Use for post-alignment file loading
    protected String read_filename;

    // member declarations
    JPanel parameterPanel = new JPanel();
    JCheckBox defaultSeedCheckBox = new JCheckBox();
    JCheckBox determineLCBsCheckBox = new JCheckBox();
    JSlider seedLengthSlider = new JSlider();
    JLabel seedLengthLabel = new JLabel();
    JCheckBox recursiveCheckBox = new JCheckBox();
    JLabel minLcbWeightLabel = new JLabel();
    JTextField minLcbWeightText = new JTextField();
    JCheckBox collinearCheckBox = new JCheckBox();

    public JButton alignButton = new JButton();
    public JButton cancelButton = new JButton();

    JPanel sequencesPanel = new JPanel();
    JButton addButton = new JButton();
    JButton removeButton = new JButton();
    JTextField outputFileText = new JTextField();
    JButton outputButton = new JButton();
    DnDList sequenceList = new DnDList();
    JLabel outputLabel = new JLabel();
    JLabel sequencesLabel = new JLabel();

    JPanel parentPanel = new JPanel();
    JTabbedPane alignmentOptionPane = new JTabbedPane();
    /** < contains the various parameter panels */

    final JFileChooser fc = new JFileChooser();
    JScrollPane listScrollPane = new JScrollPane();
    DefaultListModel sequenceListModel = new DefaultListModel();
    Dimension d;

    Mauve mauve;
    AlignWorker worker;
    
    public AlignFrame(Mauve mauve)
    {
        setResizable(false);
        this.mauve = mauve;
    }

    public void initComponents()
    {
        setIconImage(MauveFrame.mauve_icon.getImage());
        // the following code sets the frame's initial state
        parameterPanel.setSize(new java.awt.Dimension(350, 150));
        parameterPanel.setLocation(new java.awt.Point(0, 210));
        parameterPanel.setVisible(true);
        parameterPanel.setLayout(null);
        defaultSeedCheckBox.setVisible(true);
        defaultSeedCheckBox.setSize(new java.awt.Dimension(140, 20));
        defaultSeedCheckBox.setText("Default seed weight");
        defaultSeedCheckBox.setSelected(true);
        defaultSeedCheckBox.setLocation(new java.awt.Point(10, 10));
        defaultSeedCheckBox.setToolTipText("Selecting this will cause Mauve to choose the minimum seed size automatically.");
        determineLCBsCheckBox.setVisible(true);
        determineLCBsCheckBox.setSize(new java.awt.Dimension(140, 20));
        determineLCBsCheckBox.setText("Determine LCBs");
        determineLCBsCheckBox.setSelected(true);
        determineLCBsCheckBox.setLocation(new java.awt.Point(10, 90));
        determineLCBsCheckBox.setToolTipText("Selecting this will cause Mauve to determine Locally Collinear Blocks.  Without this option, all multi-MUMs will be displayed.");
        seedLengthSlider.setSize(new java.awt.Dimension(130, 50));
        seedLengthSlider.setLocation(new java.awt.Point(200, 30));
        seedLengthSlider.setVisible(true);
        seedLengthSlider.setMajorTickSpacing(4);
        seedLengthSlider.setMinorTickSpacing(2);
        seedLengthSlider.setMinimum(3);
        seedLengthSlider.setMaximum(21);
        seedLengthSlider.setPaintLabels(true);
        seedLengthSlider.setPaintTicks(true);
        seedLengthSlider.setSnapToTicks(true);
        seedLengthSlider.setValue(15);
        seedLengthSlider.setEnabled(false);
        seedLengthSlider.setToolTipText("This sets the minimum size of multi-MUMs found during the first pass of match detection");
        seedLengthLabel.setSize(new java.awt.Dimension(120, 20));
        seedLengthLabel.setLocation(new java.awt.Point(210, 10));
        seedLengthLabel.setVisible(true);
        seedLengthLabel.setText("Match Seed Weight:");
        seedLengthLabel.setEnabled(false);
        recursiveCheckBox.setVisible(true);
        recursiveCheckBox.setSize(new java.awt.Dimension(120, 20));
        recursiveCheckBox.setText("Full Alignment");
        recursiveCheckBox.setSelected(true);
        recursiveCheckBox.setLocation(new java.awt.Point(10, 145));
        recursiveCheckBox.setToolTipText("This enables recursive anchor search and gapped alignment using MUSCLE");
        collinearCheckBox.setVisible(true);
        collinearCheckBox.setSize(new java.awt.Dimension(190, 20));
        collinearCheckBox.setText("Assume collinear genomes");
        collinearCheckBox.setSelected(false);
        collinearCheckBox.setLocation(new java.awt.Point(10, 110));
        collinearCheckBox.setToolTipText("Set this when the input sequences do not have rearrangements");
        minLcbWeightLabel.setText("Min LCB weight:");
        minLcbWeightLabel.setHorizontalAlignment(JLabel.RIGHT);
        d = minLcbWeightLabel.getPreferredSize();
        minLcbWeightLabel.setSize(new Dimension( d.width, 20 ));
        minLcbWeightLabel.setLocation(new java.awt.Point(265 - d.width, 90));
        minLcbWeightLabel.setVisible(true);
        minLcbWeightText.setVisible(true);
        minLcbWeightText.setSize(new java.awt.Dimension(60, 20));
        minLcbWeightText.setLocation(new java.awt.Point(270, 90));
        minLcbWeightText.setText("default");
        minLcbWeightText.setToolTipText("LCBs below this weight will be removed from the alignment");
        alignButton.setVisible(true);
        alignButton.setSize(new java.awt.Dimension(80, 30));
        alignButton.setText("Align...");
        alignButton.setLocation(new java.awt.Point(250, 320));
        cancelButton.setVisible(true);
        cancelButton.setEnabled(false);
        cancelButton.setSize(new java.awt.Dimension(135, 30));
        cancelButton.setText("Cancel alignment");
        cancelButton.setLocation(new java.awt.Point(110, 320));
        sequencesPanel.setSize(new java.awt.Dimension(350, 210));
        sequencesPanel.setLocation(new java.awt.Point(0, 0));
        sequencesPanel.setVisible(true);
        sequencesPanel.setLayout(null);
        addButton.setVisible(true);
        addButton.setSize(new java.awt.Dimension(145, 20));
        addButton.setText("Add Sequence...");
        addButton.setLocation(new java.awt.Point(30, 150));
        removeButton.setVisible(true);
        removeButton.setSize(new java.awt.Dimension(145, 20));
        removeButton.setText("Remove Sequence");
        removeButton.setLocation(new java.awt.Point(185, 150));
        outputFileText.setVisible(true);
        outputFileText.setSize(new java.awt.Dimension(220, 20));
        outputFileText.setLocation(new java.awt.Point(85, 180));
        outputFileText.setToolTipText("The path and base file name for output files");
        outputButton.setVisible(true);
        outputButton.setSize(new java.awt.Dimension(20, 20));
        outputButton.setText("...");
        outputButton.setLocation(new java.awt.Point(310, 180));
        outputButton.setToolTipText("Set the output file location");
        sequenceList.setModel(sequenceListModel);
        sequenceList.setVisible(true);
        sequenceList.setSize(new java.awt.Dimension(320, 110));
        //		sequenceList.setLocation(new java.awt.Point(10, 30));
        listScrollPane.getViewport().setView(sequenceList);
        listScrollPane.setSize(new java.awt.Dimension(320, 110));
        listScrollPane.setLocation(new java.awt.Point(10, 30));
        outputLabel.setSize(new java.awt.Dimension(75, 20));
        outputLabel.setLocation(new java.awt.Point(10, 180));
        outputLabel.setVisible(true);
        outputLabel.setText("Output File:");
        sequencesLabel.setSize(new java.awt.Dimension(130, 20));
        sequencesLabel.setLocation(new java.awt.Point(10, 10));
        sequencesLabel.setVisible(true);
        sequencesLabel.setText("Sequences to align:");
        setLocation(new java.awt.Point(0, 0));
        setLayout(null);
        setTitle("Align sequences...");

        parameterPanel.add(defaultSeedCheckBox);
        parameterPanel.add(determineLCBsCheckBox);
        parameterPanel.add(collinearCheckBox);
        parameterPanel.add(seedLengthSlider);
        parameterPanel.add(seedLengthLabel);
        parameterPanel.add(recursiveCheckBox);
        parameterPanel.add(minLcbWeightLabel);
        parameterPanel.add(minLcbWeightText);
        sequencesPanel.add(addButton);
        sequencesPanel.add(removeButton);
        sequencesPanel.add(outputFileText);
        sequencesPanel.add(outputButton);
        sequencesPanel.add(listScrollPane);
        sequencesPanel.add(outputLabel);
        sequencesPanel.add(sequencesLabel);
        //		add(parameterPanel);
        //		add(sequencesPanel);

        parentPanel.setSize(new java.awt.Dimension(350, 360));
        parentPanel.setLocation(new java.awt.Point(0, 0));
        parentPanel.setVisible(true);
        parentPanel.setLayout(null);

        alignmentOptionPane.addTab("Files", sequencesPanel);
        alignmentOptionPane.addTab("Parameters", parameterPanel);
        //		alignmentOptionPane.setSelectedIndex( 0 );
        alignmentOptionPane.setSize(new java.awt.Dimension(350, 310));
        alignmentOptionPane.setLocation(new java.awt.Point(0, 0));
        alignmentOptionPane.setVisible(true);

        parentPanel.add(alignmentOptionPane);
        parentPanel.add(alignButton);
        parentPanel.add(cancelButton);

        add(parentPanel);

        setSize(new java.awt.Dimension(343, 383));

        // event handling

        defaultSeedCheckBox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent e)
            {
                defaultSeedCheckBoxActionPerformed(e);
            }
        });

        determineLCBsCheckBox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent e)
            {
                determineLCBsCheckBoxActionPerformed(e);
            }
        });

        collinearCheckBox.addActionListener(new java.awt.event.ActionListener()
                {
                    public void actionPerformed(java.awt.event.ActionEvent e)
                    {
                        collinearCheckBoxActionPerformed(e);
                    }
                });

        addButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent e)
            {
                addButtonActionPerformed(e);
            }
        });

        removeButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent e)
            {
                removeButtonActionPerformed(e);
            }
        });

        outputButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent e)
            {
                outputButtonActionPerformed(e);
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent e)
            {
                thisWindowClosing(e);
            }
            public void windowClosed(java.awt.event.WindowEvent e)
            {
                thisWindowClosed(e);
            }
        });

        // show the user where the output file would go by default
        File ofile;
        try
        {
            ofile = getDefaultFile();
        }
        catch (IOException e)
        {
            MyConsole.err().println("Couldn't create temporary file.");
            e.printStackTrace(MyConsole.err());
            return;
        }
        setOutput(ofile.getPath());
        ofile.delete(); 
        // delete the file in case the user chooses a different file name
        alignButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent e)
            {
                alignButtonActionPerformed(e);
            }
        });
        cancelButton.addActionListener(new java.awt.event.ActionListener()
                {
                    public void actionPerformed(java.awt.event.ActionEvent e)
                    {
                        cancelButtonActionPerformed(e);
                    }
                });
    }
    
    File getDefaultFile() throws IOException
    {
        return File.createTempFile("mauve", ".mln");
    }

    /**
     * Read the user's genome alignment parameters and start the alignment using
     * the command line mauveAligner tool. This function translates information
     * entered into the GUI aligner front-end into a command line for
     * mauveAligner.
     */
    public void alignButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
        alignButton.setEnabled(false);
        
        String[] mauve_cmd = makeAlignerCommand();

        // No command means there was an error.
        if (mauve_cmd == null)
        {
            alignButton.setEnabled(true);
            return;
        }
        MyConsole.showConsole();
        printCommand(mauve_cmd);
        
        worker = new AlignWorker(this, mauve_cmd);
        worker.start();
        cancelButton.setEnabled(true);
    }
    
    public void completeAlignment(int retcode)
    {
        alignButton.setEnabled(true);
        cancelButton.setEnabled(false);
        
        if (retcode == 0)
        {
            File readFile = new File(read_filename);
            if(!readFile.exists() || readFile.length() == 0)
            {
                JOptionPane.showMessageDialog(null, "The aligner failed to produce an alignment.  The sequences may not contain any homologous regions.", "An error occurred", JOptionPane.ERROR_MESSAGE);
            }

            mauve.loadFile(new File(read_filename));
            setVisible(false);
        }
        else if(!worker.getKilled())
        {
            JOptionPane.showMessageDialog(null, "mauveAligner exited with an error code.  Check the log window for details", "An error occurred", JOptionPane.ERROR_MESSAGE);
        }
        worker = null;
    }
    public void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
    	worker.interrupt();
    	alignButton.setEnabled(true);
    	cancelButton.setEnabled(false);
    }
    
    protected String[] makeAlignerCommand()
    {
        return new String[0];
    }
    
    protected void printCommand(String[] mauve_cmd)
    {
        // Make a readable version of command.
        String mauve_str = new String();
        for (int cmdI = 0; cmdI < mauve_cmd.length; cmdI++)
        {
            mauve_str += mauve_cmd[cmdI] + " ";
        }
        MyConsole.out().println("Executing: ");
        MyConsole.out().println(mauve_str);
    }
    
    /**
     * @param output_file
     * @return
     * @throws IOException
     */
    protected String makeOutputFile(String output_file) throws IOException
    {
        if (output_file.length() > 0)
        {
            File ofile = new File(output_file);
            if (ofile.isDirectory())
            {
                ofile = File.createTempFile("mauve", ".mln", ofile);
                output_file = ofile.getPath();
            }
        }
        else
        {
            File ofile = File.createTempFile("mauve", ".mln");
            output_file = ofile.getPath();
        }
        return output_file;
    }
    
    
    private boolean mShown = false;

    public void addNotify()
    {
        super.addNotify();

        if (mShown)
            return;

        // move components to account for insets
        Insets insets = getInsets();
        Component[] components = getComponents();
        for (int i = 0; i < components.length; i++)
        {
            Point location = components[i].getLocation();
            location.move(location.x, location.y + insets.top);
            components[i].setLocation(location);
        }

        mShown = true;
    }

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e)
    {
    	if(cancelButton.isEnabled())
    	{
    		// ask the user whether they would like to cancel the alignment
            int choice = JOptionPane.showConfirmDialog(null, "An alignment is in progress.  Closing this window will terminate the alignment.  Would you like to proceed?\n", "Alignment in progress", JOptionPane.YES_NO_OPTION);
    		if(choice == JOptionPane.NO_OPTION)
    			return;
    	}
        setVisible(false);
        dispose();
    }
    void thisWindowClosed(java.awt.event.WindowEvent e)
    {
    	if(cancelButton.isEnabled())
    		worker.interrupt();
    }

    public void defaultSeedCheckBoxActionPerformed(java.awt.event.ActionEvent e)
    {
        if (defaultSeedCheckBox.isSelected())
        {
            seedLengthSlider.setEnabled(false);
            seedLengthLabel.setEnabled(false);
        }
        else
        {
            seedLengthSlider.setEnabled(true);
            seedLengthLabel.setEnabled(true);
        }
    }

    public void determineLCBsCheckBoxActionPerformed(java.awt.event.ActionEvent e)
    {
    	System.err.println("in AlignFrame!");
        if (determineLCBsCheckBox.isSelected())
        {
            recursiveCheckBox.setEnabled(true);
            if (!collinearCheckBox.isSelected())
            {
	            minLcbWeightLabel.setEnabled(true);
	            minLcbWeightText.setEnabled(true);
            }
            collinearCheckBox.setEnabled(true);
        }
        else
        {
            recursiveCheckBox.setEnabled(false);
            minLcbWeightLabel.setEnabled(false);
            minLcbWeightText.setEnabled(false);
            collinearCheckBox.setEnabled(false);
        }
    }

    public void collinearCheckBoxActionPerformed(java.awt.event.ActionEvent e)
    {
        if (collinearCheckBox.isSelected())
        {
            minLcbWeightLabel.setEnabled(false);
            minLcbWeightText.setEnabled(false);
        }
        else
        {
        	if( determineLCBsCheckBox.isSelected() ){
	            minLcbWeightLabel.setEnabled(true);
	            minLcbWeightText.setEnabled(true);
        	}
        }
    }

    public void addButtonActionPerformed(java.awt.event.ActionEvent e)
    {
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File seq_file = fc.getSelectedFile();
            int sel_index = sequenceList.getSelectedIndex();
            if (sel_index >= 0)
                sequenceListModel.add(sel_index, seq_file.getPath());
            else
                sequenceListModel.addElement(seq_file.getPath());
        }
    }

    public void removeButtonActionPerformed(java.awt.event.ActionEvent e)
    {
        int[] selection = sequenceList.getSelectedIndices();
        for (int selI = selection.length; selI > 0; selI--)
        {
            sequenceListModel.removeElementAt(selection[selI - 1]);
        }
    }

    public void outputButtonActionPerformed(java.awt.event.ActionEvent e)
    {
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File seq_file = fc.getSelectedFile();
            outputFileText.setText(seq_file.getPath());
        }
    }

    public boolean getRecursive()
    {
        return recursiveCheckBox.isSelected();
    }

    public boolean getCollinear()
    {
        return collinearCheckBox.isSelected();
    }

    public int getSeedWeight()
    {
        if (!defaultSeedCheckBox.isSelected())
            return seedLengthSlider.getValue();
        else
            return -1;
    }

    public boolean isLCBSearchEnabled()
    {
        return determineLCBsCheckBox.isSelected();
    }

    public int getMinLcbWeight()
    {
        try
        {
            return Integer.parseInt(minLcbWeightText.getText());
        }
        catch (NumberFormatException nfe)
        {
            return -1;
        }
    }

    public String getOutput()
    {
        return outputFileText.getText();
    }

    public void setOutput(String filename)
    {
        outputFileText.setText(filename);
    }

    public String[] getSequences()
    {
        String[] seqs = new String[sequenceListModel.getSize()];
        for (int seqI = 0; seqI < seqs.length; seqI++)
            seqs[seqI] = (String) (sequenceListModel.get(seqI));
        return seqs;
    }

}
