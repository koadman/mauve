package org.gel.mauve.analysis;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;
import org.gel.mauve.LCB;
import org.gel.mauve.LCBlist;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.XmfaViewerModel;
	
public class PermutationExporter {

	public static final char LCB_BND = 'l';
	public static final char CTG_BND = 'c';
	
	private static Map<String,Integer> sharedBoundaryCounts = new HashMap<String, Integer>();
	
	public static LCB[] getSplitLCBs(XmfaViewerModel model){
		System.err.println(model.getVisibleLcbList().length + " LCBs before splitting with getSplitLCBs(XmfaViewerModel).");
		Genome[] genomes = model.getGenomes().toArray(new Genome[model.getGenomes().size()]);
		LCB[] lcbList = projectLcbList(model, model.getVisibleLcbList(),genomes);
		lcbList = splitLcbList(model, lcbList, genomes);
		if (lcbList != null){
			System.err.println(lcbList.length + " LCBs after splitting with getSplitLCBs(XmfaViewerModel).");
		}
		return lcbList;
	}
	
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
	@SuppressWarnings("unchecked")
	public static LCB[] projectLcbList(XmfaViewerModel model, LCB[] lcbList, Genome[] genomes)
	{
		if (!sharedBoundaryCounts.containsKey(model.getSrc().getAbsolutePath()))
			sharedBoundaryCounts.put(model.getSrc().getAbsolutePath(), 0);
		//System.err.println(model.getSrc().getAbsolutePath());
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
		
		LCB[] plist = new LCB[projlcbs.size()];
		projlcbs.toArray(plist);
		// now that we've got all the LCBs we need, compute adjacencies
		LCBlist.computeLCBAdjacencies(plist, model);
		return plist;
	}
	
	public static LCB[] splitLcbList(XmfaViewerModel model, LCB[] lcbList, Genome[] genomes){
		HashMap<Integer, Set<Long>> break_map = new HashMap<Integer, Set<Long>>();
		// for each LCB, accumulate the breakpoint column coordinates across all genomes for the LCB
		for (int g = 0; g < genomes.length; g++){
			List chrom = genomes[g].getChromosomes();
			Iterator it = chrom.iterator();				
			while(it.hasNext()){
				Chromosome chr = (Chromosome) it.next();
				long[] chrbnds = new long[2];
				chrbnds[0] = chr.getStart()-1;
				chrbnds[1] = chr.getEnd();
				for(long chrb:chrbnds){
					long[] splitBnd = model.getLCBAndColumn(genomes[g], chrb);
					Integer inty = new Integer((int)(splitBnd[0]));
					if(break_map.get(inty) == null){
						break_map.put(inty, new TreeSet<Long>());
					}
					break_map.get(inty).add(splitBnd[1]);
				}
			}
		}
		
		// now break up each LCB on the identified columns
		Vector<LCB> projlcbs = new Vector<LCB>();
		for(LCB lcb: lcbList){
			Set<Long> breakset = break_map.get(new Integer(lcb.id));
			if(breakset == null) continue;
			LCB lcbCopy = new LCB(lcb);
			long prev = 0;
			for(Long bp: breakset){
				LCB lcbCrop = cropLeft(model, genomes, lcbCopy, bp.longValue() - prev);
				projlcbs.add(lcbCrop);
				prev = bp.longValue();
			}
			projlcbs.add(lcbCopy);
		}
		
		LCB[] plist = new LCB[projlcbs.size()];
		projlcbs.toArray(plist);
		// now that we've got all the LCBs we need, compute adjacencies
		LCBlist.computeLCBAdjacencies(plist, model);
		return plist;		
	}
		
	/**
	 * Returns the number of shared boundaries between contigs/chromosomes and LCB 
	 * @param model XmfaViewerModel to get this info for
	 * @return the number of shared boundaries between contigs/chromosomes and LCBs if 
	 * 			this number has been calculated already, -1 otherwise
	 */
	public static int getSharedBoundaryCount(XmfaViewerModel model){
		Genome[] genomes = new Genome[model.getGenomes().size()];
		model.getGenomes().toArray(genomes);
		LCB[] lcbs = model.getFullLcbList();
		return getSharedBoundaryCount(model, lcbs, genomes);		
	}
	
	public static int getSharedBoundaryCount(XmfaViewerModel model, LCB[] lcbs, Genome[] genomes){
		int ret = 0;
		for (Genome g: genomes){
			Vector<Long> bndryPtList = new Vector<Long>();
			loadChromosomeBoundaries(bndryPtList, g);
			loadLcbBoundaries(bndryPtList, g, lcbs);
			ret += sortAndCount(bndryPtList);
		}
		return ret;
	}
	
	private static void loadChromosomeBoundaries(Vector<Long> bndryPts, Genome g){
		Chromosome chr = null;
		Iterator<Chromosome> it = g.getChromosomes().iterator();
		while(it.hasNext()){
			chr = it.next();
			bndryPts.add(chr.getStart());
			bndryPts.add(chr.getEnd());
		}
	}
	
	private static void loadLcbBoundaries(Vector<Long> bndryPts, Genome g, LCB[] lcbs){
		for (LCB lcb: lcbs){
			bndryPts.add(lcb.getLeftEnd(g));
			bndryPts.add(lcb.getRightEnd(g));
		}
	}
	
	private static int sortAndCount(Vector<Long> list){
		Long[] ar = new Long[list.size()];
		list.toArray(ar);
		Arrays.sort(ar);
		int ret = 0;
//		int[] dif = new int[ar.length-1];
		for (int i = 1; i < ar.length; i++){
			if (ar[i] == ar[i-1])
				ret++;
//			dif[i-1] = (int) (ar[i] - ar[i-1]);
		}
		return ret;
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
	public static LCB cropLeft (XmfaViewerModel model, Genome[] genomes, LCB lcb, long col){	

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
		
		for (int j = 0; j < genomes.length; j++){ 
			int srcIdx = genomes[j].getSourceIndex();
			Genome g = genomes[j];
			
			if (lcb.getReverse(genomes[j])){
			
			//	if (!gapEnd[srcIdx])
					newLCB.setLeftEnd(genomes[j], end_coords[srcIdx]);
			//	if (!gapStart[srcIdx])
					lcb.setRightEnd(genomes[j], start_coords[srcIdx]);
			} else {
				
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
	//@SuppressWarnings("unchecked")
	public static Vector[] computeSignedPermutation(XmfaViewerModel model, Genome[] genomes, boolean splitOnCtgBnds)
	{
		int seq_count = genomes.length;
/*		
		LCB[] lcbList = model.getVisibleLcbList();
		System.err.println(lcbList.length+" LCBs before splitting in computeSignedPermutation(XmfaViewerModel,Genome[],boolean).");
		lcbList = projectLcbList(model, lcbList, genomes,splitOnCtgBnds);
		System.err.println(lcbList.length+" LCBs after splitting in computeSignedPermutation(XmfaViewerModel,Genome[],boolean).");
		LCB[] lcbList2 = getSplitLCBs(model);
*/
		LCB[] lcbList = null;
		if (splitOnCtgBnds){
			lcbList = model.getSplitLcbList();
		} else {
			lcbList = model.getVisibleLcbList();
		}
		// Filter out LCBs that don't pertain to our set of genomes.
		// LCB splitting by contig boundaries has already been done for us, 
		// so no need to split again. 
		lcbList = projectLcbList(model, lcbList, genomes);
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
	 * @param splitOnCtgBnds split blocks up by contig/chromosome boundaries if true
	 * @return an array of permutations, one for each element in <code>genomes</code>
	 */
	@SuppressWarnings("unchecked")
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

/*	public static String[] getPermStrings(XmfaViewerModel model, boolean splitOnCtgBnds){
		Vector<Genome> v = model.getGenomes();
		return getPermStrings(model, v.toArray(new Genome[v.size()]), splitOnCtgBnds);
	}*/
	
	public static interface Boundary extends Comparable<Boundary> {
		public long getPos();
		
		public static class Sequence implements Boundary {
			final long pos;
			final int g;
			public Sequence (long pos){ this.pos = pos; g = -1; }
			public Sequence (long pos, int g){ this.pos = pos; this.g = g; }
			public int compareTo(Boundary o) {
				if (this.pos < o.getPos()){
					return -1;
				} else if (this.pos > o.getPos()){
					return 1;
				} else return 0;
			}
			public long getPos() {return pos;}
		}
		
		public static class Block implements Boundary {
			final long pos;
			final int lcb;
			public Block (long pos, int lcb){ this.pos = pos; this.lcb = lcb;}
			public int compareTo(Boundary o) {
				if (this.pos < o.getPos()){
					return -1;
				} else if (this.pos > o.getPos()){
					return 1;
				} else return 0;
			}
			public long getPos() {return pos;}
			public int getId() {return lcb;}
		}
	}
	
	public static int countInterBlockBounds(XmfaViewerModel model, Genome g){
		int ret = 0;
		Vector<Boundary> bnds = new Vector<Boundary>(); 
		Iterator<Chromosome> it = g.getChromosomes().iterator();
		it.next();
		while(it.hasNext()) {
			Chromosome tmp = it.next();
			bnds.add(new Boundary.Sequence(tmp.getStart()));
			//bnds.add(new Boundary.Sequence(tmp.getEnd()));
		}
		LCB[] lcbs = model.getFullLcbList();
		for (LCB lcb: lcbs){
			bnds.add(new Boundary.Block(lcb.getLeftEnd(g),lcb.id));
			bnds.add(new Boundary.Block(lcb.getRightEnd(g),lcb.id));
		}
		Boundary[] ar = new Boundary[bnds.size()];
		bnds.toArray(ar);
		Arrays.sort(ar);
	//	if (ar[0] instanceof Boundary.Sequence) ret++;
		for (int i = 1; i < ar.length-1; i++){
			Boundary l = ar[i-1];
			Boundary m = ar[i];
			Boundary r = ar[i+1];
			if (l instanceof Boundary.Block && 
				m instanceof Boundary.Sequence && 
				r instanceof Boundary.Block) {
				Boundary.Block bl = (Boundary.Block) l;
				Boundary.Block br = (Boundary.Block) r;
				if (bl.lcb != br.lcb) ret++;
			}/* else if (l instanceof Boundary.Sequence && m instanceof Boundary.Sequence && r instanceof Boundary.Block){
				System.out.print("");
			} else if (r instanceof Boundary.Sequence && m instanceof Boundary.Sequence && l instanceof Boundary.Block){
				System.out.print("");
			}*/
		}
	//	if (ar[ar.length-1] instanceof Boundary.Sequence) ret++;
		return ret;
	}
	
	public static int countInterBlockBounds(XmfaViewerModel model){
		int ret = 0;
		Iterator<Genome> it = model.getGenomes().iterator();
		while(it.hasNext()){
			ret += countInterBlockBounds(model, it.next());
		}
		return ret;
	}
	
	public static int countSharedContigBounds(XmfaViewerModel model, Genome[] genomes){
		for (int gI = 0; gI < genomes.length; gI++){
			for (int gJ = gI+1; gJ < genomes.length; gJ++){
				
			}
		}
		return 0;
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

	public int compareTo(Boundary arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
}
