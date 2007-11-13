package org.gel.mauve.gui;

import java.awt.Dimension;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.gel.mauve.MyConsole;

public class ProgressiveMauveAlignFrame extends AlignFrame implements ChangeListener {
	
    // member declarations
    Dimension d;
    JCheckBox refineCheckBox = new JCheckBox();
    JCheckBox seedFamiliesCheckBox = new JCheckBox();
    
    JCheckBox sumOfPairsCheckBox = new JCheckBox();
    JSlider breakpointWeightScaleSlider = new JSlider();
    JLabel breakpointWeightScaleLabel = new JLabel();
    JTextField breakpointWeightScaleText = new JTextField(5);
    JSlider conservationWeightScaleSlider = new JSlider();
    JLabel conservationWeightScaleLabel = new JLabel();
    JTextField conservationWeightScaleText = new JTextField(5);

    
    JPanel musclePanel = new JPanel();
    JComboBox matrixChoice = new JComboBox(new String[] {"HOXD (default)", "Custom"});
    JLabel matrixChoiceLabel = new JLabel();
    JTextField[][] scoreText = new JTextField[4][4];
    JLabel[] scoreLabelRow = new JLabel[4];
    JLabel[] scoreLabelCol = new JLabel[4];
    String[] scoreLabels = {"A", "C", "G", "T"};
    String[][] hoxd_matrix = {
    		{"91",   "-114", "-31",  "-123"},
    		{"-114", "100",  "-125", "-31" },
    		{"-31",  "-125", "100",  "-114"},
    		{"-123", "-31",  "-114", "91"  }
    };
    String hoxd_go = "-400";
    String hoxd_ge = "-30";
    JTextField gapOpenText = new JTextField();
    JLabel gapOpenLabel = new JLabel();
    JTextField gapExtendText = new JTextField();
    JLabel gapExtendLabel = new JLabel();
    JTextField muscleParamsText = new JTextField();
    JLabel muscleParamsLabel = new JLabel();
    
    public ProgressiveMauveAlignFrame(Mauve mauve)
    {
    	super(mauve);
    }

    public void initComponents()
    {
    	super.initComponents();
    	
        // the following code sets the frame's initial state
        defaultSeedCheckBox.setLocation(new java.awt.Point(10, 10));
        determineLCBsCheckBox.setLocation(new java.awt.Point(10, 50));
        seedLengthSlider.setLocation(new java.awt.Point(200, 30));
        seedLengthLabel.setLocation(new java.awt.Point(210, 10));
        recursiveCheckBox.setLocation(new java.awt.Point(10, 90));
        collinearCheckBox.setLocation(new java.awt.Point(10, 70));
        d = minLcbWeightLabel.getPreferredSize();
        minLcbWeightLabel.setLocation(new java.awt.Point(265 - d.width, 90));
        minLcbWeightText.setLocation(new java.awt.Point(270, 90));
        alignButton.setLocation(new java.awt.Point(250, 320));

        //
        // add a panel to define MUSCLE behavior
        //
        musclePanel.setSize(new java.awt.Dimension(350, 150));
        musclePanel.setLocation(new java.awt.Point(0, 210));
        musclePanel.setVisible(true);
        musclePanel.setLayout(null);

        d = matrixChoice.getPreferredSize();
        matrixChoice.setSize(d);
        matrixChoice.setLocation(new java.awt.Point(110, 10));
        matrixChoice.setVisible(true);
    	musclePanel.add(matrixChoice);
        matrixChoiceLabel.getPreferredSize();
        matrixChoiceLabel.setText("Scoring matrix:");
        matrixChoiceLabel.setSize(new Dimension(100, 15));
        matrixChoiceLabel.setLocation(10, 15);
        matrixChoiceLabel.setVisible(true);
    	musclePanel.add(matrixChoiceLabel);
        
        // layout substitution scoring matrix
        int score_matrix_left = 15;
        int score_matrix_top = 35;
        int score_left = score_matrix_left + 25;
        int score_top = score_matrix_top + 25;
        int score_w_offset = 40;
        int score_w = score_w_offset - 5;
        int score_h_offset = 25;
        int score_h = score_h_offset - 5;

        int t = score_top;
    	for( int sI = 0; sI < 4; sI++ )
        {
        	int l = score_left;
        	for( int sJ = 0; sJ < 4; sJ++ )
        	{
        		scoreText[sI][sJ] = new JTextField();
                scoreText[sI][sJ].setVisible(true);
                scoreText[sI][sJ].setSize(new java.awt.Dimension(score_w, score_h));
                scoreText[sI][sJ].setLocation(new java.awt.Point(l, t));
                scoreText[sI][sJ].setHorizontalAlignment(JTextField.RIGHT);
                scoreText[sI][sJ].addActionListener(new java.awt.event.ActionListener()
                        {
                            public void actionPerformed(java.awt.event.ActionEvent e)
                            {
                                scoreTextActionPerformed(e);
                            }
                        });
            	musclePanel.add(scoreText[sI][sJ]);
                l += score_w_offset;
        	}
        	t += score_h_offset;
        }
    	setScoreEditable(false);
    	setMatrixValues(hoxd_matrix);
        int score_label_top = score_matrix_top;
        int score_label_left = score_matrix_left;
        t = score_label_top;
        int l = score_label_left;
        for( int sI = 0; sI < 4; sI++ )
        {
        	t += score_h_offset;
        	l += score_w_offset;
        	scoreLabelRow[sI] = new JLabel();
        	scoreLabelRow[sI].setSize(new java.awt.Dimension(20, 20));
        	scoreLabelRow[sI].setLocation(new java.awt.Point(l, score_label_top + 5));
        	scoreLabelRow[sI].setVisible(true);
        	scoreLabelRow[sI].setText(scoreLabels[sI]);
        	musclePanel.add(scoreLabelRow[sI]);
        	scoreLabelCol[sI] = new JLabel();
        	scoreLabelCol[sI].setSize(new java.awt.Dimension(20, 20));
        	scoreLabelCol[sI].setLocation(new java.awt.Point(score_label_left + 10, t));
        	scoreLabelCol[sI].setVisible(true);
        	scoreLabelCol[sI].setText(scoreLabels[sI]);
        	musclePanel.add(scoreLabelCol[sI]);
        }

        gapOpenText.setVisible(true);
        gapOpenText.setSize(new java.awt.Dimension(50, 20));
        gapOpenText.setLocation(new java.awt.Point(130, 160));
        gapOpenText.setText(hoxd_go);
        gapOpenText.setHorizontalAlignment(JTextField.RIGHT);
    	musclePanel.add(gapOpenText);
    	gapOpenLabel.setSize(new java.awt.Dimension(200, 20));
        gapOpenLabel.setLocation(new java.awt.Point(15, 160));
    	gapOpenLabel.setVisible(true);
    	gapOpenLabel.setText("Gap open score:");
    	musclePanel.add(gapOpenLabel);

        gapExtendText.setVisible(true);
        gapExtendText.setSize(new java.awt.Dimension(50, 20));
        gapExtendText.setLocation(new java.awt.Point(130, 185));
        gapExtendText.setText(hoxd_ge);
        gapExtendText.setHorizontalAlignment(JTextField.RIGHT);
    	musclePanel.add(gapExtendText);
    	gapExtendLabel.setSize(new java.awt.Dimension(200, 20));
        gapExtendLabel.setLocation(new java.awt.Point(15, 185));
    	gapExtendLabel.setVisible(true);
    	gapExtendLabel.setText("Gap extend score:");
    	musclePanel.add(gapExtendLabel);

    	muscleParamsLabel.setSize(new java.awt.Dimension(200, 20));
    	muscleParamsLabel.setLocation(new java.awt.Point(15, 210));
    	muscleParamsLabel.setVisible(true);
    	muscleParamsLabel.setText("Extra MUSCLE parameters:");
    	musclePanel.add(muscleParamsLabel);
    	muscleParamsText.setVisible(true);
        muscleParamsText.setSize(new java.awt.Dimension(310, 50));
        muscleParamsText.setLocation(new java.awt.Point(15, 230));
        muscleParamsText.setText("-maxmb 900");
    	musclePanel.add(muscleParamsText);

    	// custom MUSCLE scoring matrices are currently broken in the aligner
//        alignmentOptionPane.addTab("MUSCLE", musclePanel);

        // initialize progressiveMauve-specific configuration options
        seedFamiliesCheckBox.setVisible(true);
        seedFamiliesCheckBox.setSize(new java.awt.Dimension(160, 20));
        seedFamiliesCheckBox.setText("Use seed families");
        seedFamiliesCheckBox.setSelected(true);
        seedFamiliesCheckBox.setLocation(new java.awt.Point(10, 30));
        seedFamiliesCheckBox.setToolTipText("Uses multiple spaced seed patterns to identify potential homology.<br>Can substantially improve sensitivity and accuracy on divergent genomes.");

        refineCheckBox.setVisible(true);
        refineCheckBox.setSize(new java.awt.Dimension(160, 20));
        refineCheckBox.setText("Iterative refinement");
        refineCheckBox.setSelected(true);
        refineCheckBox.setLocation(new java.awt.Point(10, 110));
        refineCheckBox.setToolTipText("Iteratively refines the alignment, significantly improving accuracy");

        sumOfPairsCheckBox.setVisible(true);
        sumOfPairsCheckBox.setSize(new java.awt.Dimension(200, 20));
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

        parameterPanel.add(seedFamiliesCheckBox);
        parameterPanel.add(refineCheckBox);
        parameterPanel.add(sumOfPairsCheckBox);
// this stuff isn't working yet:
/*
        parameterPanel.add(breakpointWeightScaleLabel);
        parameterPanel.add(breakpointWeightScaleSlider);
        parameterPanel.add(breakpointWeightScaleText);

        parameterPanel.add(conservationWeightScaleLabel);
        parameterPanel.add(conservationWeightScaleSlider);
        parameterPanel.add(conservationWeightScaleText);
*/
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
        matrixChoice.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent e)
            {
                matrixChoiceActionPerformed(e);
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

        if (getSeedFamilies())
        {
        	cmd_vec.addElement("--seed-family");
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
            	cmd_vec.addElement("--scoring-scheme=ancestral");
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
        
        if(!(getScoreMatrixName().indexOf("default") > 0))
        {
        	File mat_file = null;
        	String[][] mat = getScoreMatrix();
        	// create a score matrix file
        	try{
        		mat_file = File.createTempFile("scoremat", ".txt");
        		FileWriter outtie = new FileWriter(mat_file);
//        		FileOutputStream mat_fos = new FileOutputStream(mat_file);
//        		BufferedWriter outtie = new BufferedWriter(new OutputStreamWriter(mat_fos));
        		outtie.write("# user-defined scoring matrix\n");
        		for(int i = 0; i < 4; i++)
        		{
					outtie.write("     ");
					outtie.write(scoreLabels[i]);
        		}
				outtie.write("     N");
        		outtie.write("\n");
        		for(int i = 0; i < 4; i++)
        		{
        			for(int j = 0; j < 4; j++)
        			{
        				if(j == 0)
        					outtie.write(scoreLabels[i]);

        				// space pad the score value
        				String space_str = new String();
        				for( int sI = 0; sI < 6 - mat[i][j].length(); ++sI)
        					space_str += " ";
    					outtie.write(space_str);
        				
    					// write the score value
        				outtie.write(mat[i][j]);
        			}
        			outtie.write("     0");	// for the N column
        			outtie.write("\n");
        		}
        		outtie.write("N     0     0     0     0     0");
        		outtie.flush();
        		outtie.close();
        	}catch(IOException ioe)
        	{
        		System.err.println("Error creating score matrix file");
        	}
        	if(mat_file != null)
        	{
        		cmd_vec.addElement("--substitution-matrix=" + mat_file.getAbsolutePath());
        		cmd_vec.addElement("--gap-open=" + getGapOpen());
        		cmd_vec.addElement("--gap-extend=" + getGapExtend());
        	}
        }
        String musc_params = getMuscleParameters();
        if(musc_params.length() > 0)
        {
        	cmd_vec.addElement("--muscle-args=" + musc_params);
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
        }
        else
        {
        	refineCheckBox.setEnabled(false);
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
    public void setMatrixValues(String[][] mat)
    {
    	for(int i = 0; i < 4; i++)
    		for(int j = 0; j < 4; j++)
    			scoreText[i][j].setText(mat[i][j]);
    }
    public void setScoreEditable(boolean edit)
    {
    	gapOpenText.setEditable(edit);
    	gapExtendText.setEditable(edit);
		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
				scoreText[i][j].setEditable(edit);
    }
    public void matrixChoiceActionPerformed(java.awt.event.ActionEvent e)
    {
    	if(matrixChoice.getSelectedIndex() == 0)
    	{
    		setMatrixValues(hoxd_matrix);
    		gapOpenText.setText(hoxd_go);
    		gapExtendText.setText(hoxd_ge);
    		setScoreEditable(false);
    	}
    	if(matrixChoice.getSelectedIndex() == matrixChoice.getItemCount()-1)
    	{
    		// last item is custom matrix
    		setScoreEditable(true);
    	}
    }
    public void scoreTextActionPerformed(java.awt.event.ActionEvent e)
    {
    	matrixChoice.setSelectedIndex(matrixChoice.getItemCount()-1);
    }

    public boolean getSeedFamilies()
    {
    	if(seedFamiliesCheckBox.isEnabled())
    		return seedFamiliesCheckBox.isSelected();
    	return false;
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

    public String getScoreMatrixName()
    {
    	return matrixChoice.getSelectedItem().toString();
    }
    public String[][] getScoreMatrix()
    {
    	String[][] mat = new String[4][4];
    	for(int i = 0; i < 4; i++)
    		for(int j = 0; j < 4; j++)
    			mat[i][j] = scoreText[i][j].getText();
    	return mat;
    }
    public String getGapOpen()
    {
    	return gapOpenText.getText();
    }
    public String getGapExtend()
    {
    	return gapExtendText.getText();
    }
    public String getMuscleParameters()
    {
    	return muscleParamsText.getText();
    }
}
