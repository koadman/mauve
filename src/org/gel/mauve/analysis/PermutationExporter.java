package org.gel.mauve.analysis;

import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;
import org.gel.mauve.LCB;
import org.gel.mauve.LCBlist;
import org.gel.mauve.MyConsole;
import org.gel.mauve.XMFAAlignment;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.gui.ExportFrame;
import org.gel.mauve.gui.MauveRenderingHints;
import org.gel.mauve.gui.RearrangementPanel;

public class PermutationExporter {
	public static LCB[] projectLcbList(XmfaViewerModel model, LCB[] lcbList, Genome[] genomes)
	{
		// make a list of undesired genomes
		Genome[] others = new Genome[model.getSequenceCount()-genomes.length];
		int k=0;
		for(int i=0; i<model.getSequenceCount(); i++)
		{
			int j=0;
			for(; j<genomes.length; j++)
				if(model.getGenomes().elementAt(i) == genomes[j])
					break;
			if(j==genomes.length)
				others[k++]=(Genome)model.getGenomes().elementAt(i);
		}

		Vector projlcbs = new Vector();
		for(int i=0; i<lcbList.length; i++)
		{
			LCB lcb = lcbList[i];
			int g=0;
			for(; g<genomes.length; g++)
				if(lcb.getLeftEnd(genomes[g])==0)
					break;
			if(g<genomes.length)
				continue;
			LCB newlcb = new LCB(lcb);
			for(int o=0; o<others.length; o++)
			{
				newlcb.setLeftEnd(others[o], 0);
				newlcb.setRightEnd(others[o], 0);
				newlcb.setReverse(others[o], false);
			}
			
			projlcbs.add(newlcb);
		}
		LCB[] plist = new LCB[projlcbs.size()];
		plist = (LCB[])projlcbs.toArray(plist);
		LCBlist.computeLCBAdjacencies(plist, model);
		return plist;
	}
	
	public static Vector[] computeSignedPermutation(XmfaViewerModel model, Genome[] genomes)
	{
		int seq_count = genomes.length;
		LCB[] lcbList = model.getVisibleLcbList();
		lcbList = projectLcbList(model, lcbList, genomes);
		Vector[] signed_perms = new Vector[seq_count];
		

		for (int seqI = 0; seqI < seq_count; seqI++)
			signed_perms[seqI] = new Vector();

		// first construct a matrix of chromosome lengths
		int max_chr_count = 0;
		for (int seqI = 0; seqI < seq_count; seqI++) {
			int cur_count = genomes[seqI]
					.getChromosomes ().size ();
			max_chr_count = cur_count > max_chr_count ? cur_count
					: max_chr_count;
		}
		long [][] chr_lens = new long [seq_count] [max_chr_count];
		for (int seqI = 0; seqI < seq_count; seqI++) {
			List chromo = genomes[seqI].getChromosomes ();
			for (int chrI = 0; chrI < chromo.size (); chrI++) {
				chr_lens[seqI][chrI] = ((Chromosome) chromo.get (chrI))
						.getEnd ();
			}
		}

		boolean single_chromosome = true;
		boolean all_circular = true;
		for (int seqI = 0; seqI < seq_count; seqI++) {
			Genome g = genomes[seqI];
			List chromo = g.getChromosomes ();
			int leftmost_lcb = 0;
			for (; leftmost_lcb < lcbList.length; leftmost_lcb++)
				if (lcbList[leftmost_lcb].getLeftAdjacency (g) == LCBlist.ENDPOINT)
					break;

			int adjI = leftmost_lcb;
			int cur_chromosome = 0;
			all_circular = all_circular
					&& ((Chromosome) chromo.get (cur_chromosome))
							.getCircular ();

			signed_perms[seqI].add(new Vector());	// initialize a new chromosome
			Vector cur_chromo = (Vector)signed_perms[seqI].lastElement();
			while (adjI != LCBlist.ENDPOINT && adjI != LCBlist.REMOVED
					&& adjI < lcbList.length) {
				while (lcbList[adjI].getLeftEnd (g) > chr_lens[seqI][cur_chromosome]) {
					signed_perms[seqI].add(new Vector());
					cur_chromo = (Vector)signed_perms[seqI].lastElement();
					cur_chromosome++;
					single_chromosome = false;
					all_circular = all_circular
							&& ((Chromosome) chromo.get (cur_chromosome))
									.getCircular ();
				} 
				if (lcbList[adjI].getReverse (g)) {
					cur_chromo.add(new Integer(-(adjI + 1)));
				}else{
					cur_chromo.add(new Integer(adjI + 1));
				}
				adjI = lcbList[adjI].getRightAdjacency (g);
			}
		}
		return signed_perms;
	}
	public static void export( XmfaViewerModel model, BufferedWriter output, Genome[] genomes ) throws IOException
	{
		Vector[] perms = computeSignedPermutation(model, genomes);
		for(int i = 0; i < perms.length; i++)
		{
			for(int j = 0; j < perms[i].size(); j++)
			{
				Vector cur = (Vector)perms[i].elementAt(j);
				for(int k = 0; k < cur.size(); k++)
				{
					if(k>0)
						output.write(",");
					output.write(cur.elementAt(k).toString());
				}
				output.write(" $ ");
			}
			output.write("\n");
		}
		output.flush();
	}

	public static class ExportFrame extends JFrame
	{	    
	    private JTextField outputFile = new JTextField();
	    private JFileChooser fc = new JFileChooser();
	    private JComboBox formatSelector = new JComboBox();
	    XmfaViewerModel model;
	    
	    public ExportFrame(XmfaViewerModel model)
	    {
	    	this.model = model;
	        setSize(300,150);
	        
	        getContentPane().setLayout(new GridBagLayout());
	        GridBagConstraints c = new GridBagConstraints();

	        setTitle("Export Genome Arrangement");

	        fc.setDialogTitle("Export permutation file to...");

	        c.insets = new Insets(2,2,2,2);

	        // Format label.
	        c.gridx = 0;
	        c.gridy = 0;
	        c.gridwidth = 1;
	        c.anchor = GridBagConstraints.EAST;
	        c.fill = GridBagConstraints.NONE;
	        getContentPane().add(new JLabel("Genomes:"), c);
	        
	        // Format selector.
	        c.gridx = 1;
	        c.gridy = 0;
	        c.gridwidth = 2;
	        c.anchor = GridBagConstraints.WEST;
	        c.fill = GridBagConstraints.HORIZONTAL;
	        formatSelector.addItem("Visible");
	        formatSelector.addItem("All");
	        getContentPane().add(formatSelector, c);
	        
	        // File label.
	        c.gridx = 0;
	        c.gridy = 6;
	        c.gridwidth = 1;
	        c.fill = GridBagConstraints.NONE;
	        c.anchor = GridBagConstraints.SOUTHEAST;
	        c.weighty = 0;
	        getContentPane().add(new JLabel("Output file:"), c);
	        
	        // File text box
	        c.gridx = 1;
	        c.gridy = 6;
	        c.gridwidth = 1;
	        c.weighty = 1;
	        c.fill = GridBagConstraints.HORIZONTAL;
	        c.anchor = GridBagConstraints.SOUTHWEST;
	        c.weightx = 1;
	        getContentPane().add(outputFile, c);
	        
	        // File browse button.
	        JButton fileButton = new JButton("Browse...");
	        fileButton.addActionListener(new ActionListener()
	                {

	                    public void actionPerformed(ActionEvent e)
	                    {
	                        int ret = fc.showDialog(ExportFrame.this, "Select");
	                        if (ret == JFileChooser.APPROVE_OPTION)
	                        {
	                            File f = fc.getSelectedFile();
	                            outputFile.setText(f.getAbsolutePath());
	                        }
	                    }
	                }
	        );
	        c.gridx = 2;
	        c.gridy = 6;
	        c.gridwidth = 1;
	        c.fill = GridBagConstraints.NONE;
	        c.anchor = GridBagConstraints.SOUTHWEST;
	        c.weightx = 0;
	        getContentPane().add(fileButton, c);
	        
	        // Export button.
	        JPanel buttonPanel = new JPanel();
	        
	        JButton exportButton = new JButton("Export");
	        exportButton.addActionListener(new ActionListener()
	                {
	        
	                    public void actionPerformed(ActionEvent e)
	                    {
	                        doExport();
	                    }
	            
	                }
	        );
	        
	        buttonPanel.add(exportButton);
	        
	        JButton cancelButton = new JButton("Cancel");
	        cancelButton.addActionListener(new ActionListener()
	                {
	        
	                    public void actionPerformed(ActionEvent e)
	                    {
	                        setVisible(false);
	                    }
	                }
	        );
	        
	        buttonPanel.add(cancelButton);
	        
	        c.gridx = 0;
	        c.gridy = 7;
	        c.gridwidth = 3;
	        c.weighty = 0;
	        c.fill = GridBagConstraints.NONE;
	        c.anchor = GridBagConstraints.SOUTHEAST;

	        getContentPane().add(buttonPanel, c);
	        this.setVisible(true);
	    }
	    
	    private void doExport()
	    {
	        String format = (String) formatSelector.getSelectedItem();
	    	File f = new File(outputFile.getText());
	        if (f.exists())
	        {
	            int result = JOptionPane.showConfirmDialog(this, "The file " + f + " already exists.  Overwrite?", "File exists", JOptionPane.YES_NO_OPTION);
	            if (result == JOptionPane.NO_OPTION)
	            {
	                return;
	            }
	        }
	        
	        // create a list of desired genomes in viewing index order
	        boolean all = format.equalsIgnoreCase("All");
        	Vector<Genome> gg = new Vector<Genome>();
        	for(int i=0; i<model.getSequenceCount(); i++)
        	{
        		if(all || model.getGenomeByViewingIndex(i).getVisible())
        			gg.add(model.getGenomeByViewingIndex(i));
        	}
        	Genome[] genomes = new Genome[gg.size()];
        	genomes = gg.toArray(genomes);

        	try{
	        	BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		        PermutationExporter.export(model, bw, genomes);
    			bw.flush();
    			bw.close();
	        }catch(IOException ioe)
	        {
	        	ioe.printStackTrace();
	        	System.err.println("Sorry, the selected file " + f + " could not be written\n");
	        }
	        setVisible(false);
	    }
	}
}
