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
import java.io.PrintStream;
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
import org.gel.mauve.MauveConstants;
import org.gel.mauve.MyConsole;
import org.gel.mauve.XMFAAlignment;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.gui.ExportFrame;
import org.gel.mauve.gui.MauveRenderingHints;
import org.gel.mauve.gui.RearrangementPanel;
	
public class PermutationExporter {
	
	/**
	 * Computes a refined set of LCBs, returning an array containing only those
	 * LCBs present in all of <code>genomes</code>. The return set of LCBs may contain
	 * more LCBs than present in the alignment, as LCBs are split based on contig boundaries
	 * 
	 * 
	 * @param model a model containing an alignment 
	 * @param lcbList an array of LCBs
	 * @param genomes the genomes to work with
	 * @return an array of <code>LCB</code>s 
	 */
	public static LCB[] projectLcbList(XmfaViewerModel model, LCB[] lcbList, Genome[] genomes, boolean splitOnCtgBnds)
	{
		// make a list of undesired genomes
		Genome[] others = new Genome[model.getSequenceCount()-genomes.length];
		int k=0;
		// iterate over all genomes
		for(int i=0; i<model.getSequenceCount(); i++)
		{
			int j=0;
			for(; j<genomes.length; j++)
				if(model.getGenomes().elementAt(i) == genomes[j])
					break;
			if(j==genomes.length)
				others[k++]=(Genome)model.getGenomes().elementAt(i);
		}
		
		// genomes = the genomes to export 
		// others =  the genomes not to export

		Vector projlcbs = new Vector();
		
		for(int i=0; i<lcbList.length; i++)
		{
			LCB lcb = lcbList[i];
			// check to see if all the genomes we're interested are in this LCB
			int g=0;
			for(; g<genomes.length; g++)
				if(lcb.getLeftEnd(genomes[g])==0)
					break;
			if(g<genomes.length)
				continue;
			// create a copy of the current LCB
			LCB newlcb = new LCB(lcb);
			// remove the genomes we're not interested in from this LCB
			for(int o=0; o<others.length; o++){
				newlcb.setLeftEnd(others[o], 0);
				newlcb.setRightEnd(others[o], 0);
				newlcb.setReverse(others[o], false);
			}
			
			projlcbs.add(newlcb);
		}
		
		int start = projlcbs.size();
	
		// split each LCB based on the each genome's contig boundaries
		if (splitOnCtgBnds){
			for (int g = 0; g < genomes.length; g++){
				for (int i = 0; i < projlcbs.size(); i++){
				// split each LCB based on each genome
					LCB tmp = (LCB) projlcbs.elementAt(i);
					Vector newlcbs = splitLCBbyGenome(model, tmp, genomes, g);
					// replace the old LCB with the resulting split
					projlcbs.remove(i);
					projlcbs.addAll(i, newlcbs);
					// move our "pointer" to the last element we inserted into the Vector
					i = i + newlcbs.size() - 1; 
				}
			}
		}
		LCB[] plist = new LCB[projlcbs.size()];
		plist = (LCB[])projlcbs.toArray(plist);
		// now that we've got all the LCBs we need, compute adjacencies
		LCBlist.computeLCBAdjacencies(plist, model);
		return plist;
	}
	
	/**
	 * Splits an LCB based on the contig boundaries of a given genome.
	 * 
	 * @param model
	 * @param lcb the lcb to split
	 * @param genomes the genomes to maintain
	 * @param g the index in genomes of the genome to split on
	 * @return a list of new <code>LCB</code>s resulting from splitting <code>lcb</code>
	 */
	public static Vector splitLCBbyGenome(XmfaViewerModel model, LCB lcb, Genome[] genomes, int g){
		Vector subLCBs = new Vector();
		List chrom = genomes[g].getChromosomes();
		Iterator it = chrom.iterator();
		
		LCB lcbCopy = new LCB(lcb);
		int chrIdx = 1;
		while(it.hasNext()){
			Chromosome chr = (Chromosome) it.next();
			long chrL = Long.MIN_VALUE;
			long chrR = Long.MAX_VALUE;
			if (chrIdx > 0)
				chrL = chr.getStart();  // concatenated genome coordinate
			
			if (chrIdx < chrom.size()-1)
				chrR = chr.getEnd();   // concatenated genome coordinate
			long lcbL = lcbCopy.getLeftEnd(genomes[g]);  // concatenated genome coordinate
			long lcbR = lcbCopy.getRightEnd(genomes[g]); // concatenated genome coordinate
			
			
			if (chrL < lcbR && chrL > lcbL){
				long splitBnd = model.getLCBAndColumn(genomes[g], chrL-1)[1];
				subLCBs.add(trimLCB(model, genomes, lcbCopy, splitBnd));	
			} 
			lcbL = lcbCopy.getLeftEnd(genomes[g]);  // concatenated genome coordinate
			lcbR = lcbCopy.getRightEnd(genomes[g]); // concatenated genome coordinate
			
			
			
			if (chrR < lcbR && chrR > lcbL) {
				long splitBnd = model.getLCBAndColumn(genomes[g], chrR)[1];
				subLCBs.add(trimLCB(model, genomes, lcbCopy, splitBnd));
			}
			chrIdx++;
		}
		subLCBs.add(lcbCopy);
		return subLCBs;
	}
	
	/** 
	 * Trims off everything to the left of and including col from lcb
	 * and returns the removed segment as an LCB object.
	 * 
	 * col must be less than or equal to LCB length
	 * 
	 * @param model the overlying XmfaViewerModel
	 * @param genomes the genomes of interest
	 * @param lcb the LCB to be trimmed
	 * @param col the alignment column to trim up to
	 * @return the LCB trimmed off 
	 */
	/* FIXME - off by one error - read FIXME statement below. 
	 * Turn on error messages below and run Permutation Exporter or DCJ Analysis for example */
	public static LCB trimLCB (XmfaViewerModel model, Genome[] genomes, LCB lcb, long col){	

		long lcbLen = model.getXmfa().getLcbLength(lcb.id);
		if (col > lcbLen)
			throw new IllegalArgumentException("col == " + col + " : greater than LCB length (length == " + lcbLen+")");
		long[] start_coords = new long[model.getSequenceCount()];
		long[] end_coords = new long[model.getSequenceCount()];
		boolean[] gapStart = new boolean[model.getSequenceCount()];
		boolean[] gapEnd = new boolean[model.getSequenceCount()];
		
		// Create a copy of the LCB we're trimming. Then shift the copy's 
		// right coordinates left to the split boundary and the original's 
		// left coordinates right to the split boundary.
		
		LCB newLCB = new LCB(lcb);
			// Now we need to set new end coordinates for   
			// newLCB and new start coordinates for lcb
		// Get end coordinates for newLCB...
		model.getColumnCoordinates(lcb.id, col, end_coords, gapEnd);
		// Get start coordinates for lcb...
		if (col < lcbLen)
			model.getColumnCoordinates(lcb.id, col+1, start_coords, gapStart);
		else { // col == lcbLen
			return lcb;
		}
		
/* FIXME - When LCBs are split, some get split based on contig boundaries 
 *         lying at the end of the LCB, resulting in boundaries erroneous 
 *         boundaries (i.e. leftBound > rightBound)
 *         
 *         I don't believe this results in erroneous results in the 
 *         permutation. 
 *    
		boolean doPrint = false;
		for (int i = 0; i < genomes.length; i++){
			Genome g = genomes[i];
			if (g.getLength() < start_coords[g.getSourceIndex()]){
				long bad = start_coords[g.getSourceIndex()];
				start_coords[g.getSourceIndex()] = g.getLength();
				
				doPrint = true;
				System.err.print("Bad Mauve! (at start_coords) genome " + g.getSourceIndex() 
						+" Length = " + g.getLength()+ " coord = " + bad+ " ");
			
			}
			if (g.getLength() < end_coords[g.getSourceIndex()]){
				long bad = end_coords[g.getSourceIndex()];
				
				end_coords[g.getSourceIndex()] = g.getLength();
				
				System.err.println("Bad Mauve! (at end_coords) genome " + g.getSourceIndex() 
						+" Length = " + g.getLength()+ " coord = " + bad+ " ");
			
				
			} else if (doPrint)
				System.err.println();
			
			doPrint = false;
		}
		
*/
		
		for (int j = 0; j < genomes.length; j++){ 
			int srcIdx = genomes[j].getSourceIndex();
			Genome g = genomes[j];
			
			if (lcb.getReverse(genomes[j])){
				/*if (start_coords[srcIdx] < end_coords[srcIdx]){
					System.err.println("REVERSE! "+ genomes[j].getSourceIndex()+ ":  start_coords[srcIdx] = " + 
							start_coords[srcIdx] + "  end_coords[srcIdx] = " + end_coords[srcIdx]);
				}*/
			
			//	if (!gapEnd[srcIdx])
					newLCB.setLeftEnd(genomes[j], end_coords[srcIdx]);
			//	if (!gapStart[srcIdx])
					lcb.setRightEnd(genomes[j], start_coords[srcIdx]);
			} else {
				/*if (start_coords[srcIdx] > end_coords[srcIdx]){
					System.err.println("FORWARD!  lcb "+ lcb.id +", seq "+ srcIdx + ",  start_coords[srcIdx] = " + 
							start_coords[srcIdx] + "  end_coords[srcIdx] = " + end_coords[srcIdx]);
				}*/
			//	if (!gapEnd[srcIdx])
					newLCB.setRightEnd(genomes[j], end_coords[srcIdx]);
			//	if (!gapStart[srcIdx])
					lcb.setLeftEnd(genomes[j], start_coords[srcIdx]);
			}
			System.err.flush();
		}
		return newLCB;
		
	}
	
	/**
	 * Computes the signed permutations for each genome in <code>genomes</code>. 
	 * Returns an array of <code>Vector</code>s (one per genome) of
	 * <code>Vector</code>s (one per chromosome) of <code>String</code>s (one per block). 
	 * 
	 * @param model the model containing the alignment
	 * @param genomes the genomes to compute permutations for
	 * @return an array of <code>Vector</code>s of <code>Vector</code>s of <code>String</code>s
	 */
	//@SuppressWarnings("unchecked")
	public static Vector[] computeSignedPermutation(XmfaViewerModel model, Genome[] genomes, boolean splitOnCtgBnds)
	{
		int seq_count = genomes.length;
		LCB[] lcbList = model.getVisibleLcbList();
		lcbList = projectLcbList(model, lcbList, genomes,splitOnCtgBnds);
		
		Vector[] signed_perms = new Vector[seq_count];
		

		for (int seqI = 0; seqI < seq_count; seqI++)
			signed_perms[seqI] = new Vector();

		// First, construct a matrix of chromosome lengths.
		// which means we have to compute the highest number of chromosomes
		int max_chr_count = 0;
		for (int seqI = 0; seqI < seq_count; seqI++) {
			int cur_count = genomes[seqI].getChromosomes ().size ();
			max_chr_count = cur_count > max_chr_count ? cur_count
					: max_chr_count;
		}
		
		long [][] chr_end = new long [seq_count] [max_chr_count];
		for (int seqI = 0; seqI < seq_count; seqI++) {
			List chromo = genomes[seqI].getChromosomes (); 
			// get all chromosomes, and store their lengths
			for (int chrI = 0; chrI < chromo.size (); chrI++) {
				chr_end[seqI][chrI] = ((Chromosome) chromo.get (chrI)).getEnd();
			}
		}
		
	
		for (int seqI = 0; seqI < seq_count; seqI++) {  
			Genome g = genomes[seqI];				// for each genome
			//List chromo = g.getChromosomes ();
			int leftmost_lcb = 0;         // 
			for (; leftmost_lcb < lcbList.length; leftmost_lcb++)
				if (lcbList[leftmost_lcb].getLeftAdjacency (g) == LCBlist.ENDPOINT)  // if  
					break;
			int adjI = leftmost_lcb;
			int cur_chromosome = 0;
			signed_perms[seqI].add(new Vector());	// initialize a new chromosome
			Vector cur_chromo = (Vector)signed_perms[seqI].lastElement();
		//	boolean stop = false;
			while (adjI != LCBlist.ENDPOINT && adjI != LCBlist.REMOVED && adjI < lcbList.length) {
				long l = lcbList[adjI].getLeftEnd (g);
				long r = lcbList[adjI].getRightEnd(g);
				while (chr_end[seqI][cur_chromosome] < l) {
					// add new vector for a new chromosome
					signed_perms[seqI].add(new Vector());
					cur_chromo = (Vector)signed_perms[seqI].lastElement();
					cur_chromosome++;
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
	
	/**
	 * 
	 * @param model
	 * @param output
	 * @param genomes
	 * @throws IOException
	 */
	public static void export( XmfaViewerModel model, BufferedWriter output, Genome[] genomes ) throws IOException
	{
		String[] perms = getPermStrings(model,genomes, false);
		
		for (int i = 0; i < perms.length; i++){
			output.write(perms[i]+"\n");
			
		}
		output.flush();
	}
	
	
	/**
	 * Returns an array of permutations corresponding to the <code>Genome</code>s passed in.
	 * The permutation returned will apply to only those LCBs that are present in <code>genomes</code>
	 * 
	 * @param model XmfaViewerModel holding alignment
	 * @param genomes the genomes of interest.
	 * @return an array of permutations, one for each element in <code>genomes</code>
	 */
	public static String[] getPermStrings(XmfaViewerModel model, Genome[] genomes, boolean splitOnCtgBnds) 
	{
		// perms = an array of vectors of vectors. one array element per genome.
		Vector[] perms = computeSignedPermutation(model, genomes, splitOnCtgBnds); 
		StringBuilder sb = new StringBuilder();
		
		
		for(int i = 0; i < perms.length; i++)
		{
			// perms[i] = a vector of vectors. one vector per contig 
			for(int j = 0; j < perms[i].size(); j++)
			{
				// cur = a vector of "block" elements
				Vector cur = (Vector)perms[i].elementAt(j);
				for(int k = 0; k < cur.size(); k++)
				{
					if(k>0)
						sb.append(",");
					sb.append(cur.elementAt(k).toString());
				}
				if (genomes[i].isCircular(j))
					sb.append(MauveConstants.CIRCULAR_CHAR);
				sb.append("$ ");
			}
			sb.append("\n");
		}
		return sb.toString().split("\n");
	}
	/**
	 * Returns an array of permutations for all genomes in the alignment
	 * 
	 *
	 * @param model the alignment to get permutations for
	 * @return an array of permutations, indexed by genome source index
	 */
	public static String[] getPermStrings(XmfaViewerModel model, boolean splitOnCtgBnds){
		Vector<Genome> v = model.getGenomes();
		return getPermStrings(model, v.toArray(new Genome[v.size()]), splitOnCtgBnds);
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
