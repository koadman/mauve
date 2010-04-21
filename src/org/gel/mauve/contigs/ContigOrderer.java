package org.gel.mauve.contigs;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.prefs.BackingStoreException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.gel.air.util.IOUtils;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.ModelBuilder;
import org.gel.mauve.MyConsole;
import org.gel.mauve.XMFAAlignment;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.gui.AlignFrame;
import org.gel.mauve.gui.AlignWorker;
import org.gel.mauve.gui.AlignmentProcessListener;
import org.gel.mauve.gui.MauveFrame;

public class ContigOrderer implements MauveConstants, AlignmentProcessListener {
	

	private static String[] DEFAULT_ARGS = {"--skip-refinement","--weight=200"};
	public static final String DIR_STUB = "alignment";
	public static final int DEFAULT_ITERATIONS = 2;
	public static final String ALIGN_START = "Start from alignment file.";
	public static final String SEQ_START = "Start from sequence files.";
	protected static final String OUTPUT_DIR = "-output";
	protected static final String REF_FILE = "-ref";
	protected static final String DRAFT_FILE = "-draft";
	
	protected File directory;
	protected File unordered;
	protected File reference;
	protected File align_dir;
	protected int count = 1;
	protected int start = 0;
	protected ContigReorderer reorderer;
	protected ContigReordererGUI reordererGUI;
	protected MauveFrame parent;
	protected ContigMauveAlignFrame align_frame;
	protected int iterations;
	protected boolean align_start;
	protected Vector past_orders;
	protected boolean gui;
	
	private ContigMauveDataModel data;
	
	private File alnmtFile;
	
	private String[] aln_cmd;

	private static final String USAGE = 
		"Usage: java -cp path_to_jar/Mauve.jar org.gel.mauve.ContigOrderer [options]\n" +
		"  where [options] are:\n" +
		"\t"+OUTPUT_DIR+" <directory_path>\n" +
		"\t\tthe directory to store output\n" +
		"\t"+REF_FILE+" <file_path>\n" +
		"\t\tthe path to the reference genome\n" +
		"\t"+DRAFT_FILE+" <file_path>\n" +
		"\t\tthe path to the draft genome\n";
	
	/**
	 * 
	 * @param args
	 * @param frames 
	 * @param gui true if instantiate GUI, false otherwise
	 */
	public ContigOrderer (String [] args, Vector frames, boolean gui) {
		
		// init (args, frames, gui);
		
		
////////// The code below came from init(String[],Vector,boolean)
		this.gui = gui;
		past_orders = new Vector ();
		iterations = 25;//DEFAULT_ITERATIONS;
		if (args != null && args.length > 0) {
			try {
				iterations = Integer.parseInt (args [0]);
			} catch (NumberFormatException e) {
			}
		}
		reorderer = new ContigReorderer (this /*, frames*/);
//		MyConsole.setUseSwing (gui);
//		MyConsole.showConsole ();
		if (gui) {
			reordererGUI = new ContigReordererGUI(reorderer, frames);
			align_frame = new ContigMauveAlignFrame (reordererGUI, this);
		} else {
			data = new ContigMauveDataModel();
		}
///////////////////////////////////////////////////////////////////
		
		if (gui) {
			initGUI ();
			startAlignment(true);
		} else {
			initParamsNoGUI (args);
			startAlignment(false);
		}
	}
	
	/**
	 * Creates ContigOrderer with a GUI.
	 * 
	 * @param args
	 * @param frames
	 */
	public ContigOrderer (String [] args, Vector frames) {
		this (args, frames, true);
	}
	
	/**
		 * This is what Anna has written to run the Reoderer without the GUI components.
		 * Too bad it actually instantiates the GUI components.
		 * 
		 * @param args
		 */
		private void initParamsNoGUI (String[] args) {
			Hashtable <String, String> pairs = IOUtils.parseDashPairedArgs(args);
			String error = null;
			try {
				if (pairs.containsKey(OUTPUT_DIR)) {
					directory = new File (pairs.get(OUTPUT_DIR));
					if (!directory.exists()) {
						if (!directory.mkdirs())
							error = "Couldn't create output directory";
					}
				//	else if (getAlignDir ().exists())
				//		error = "Directory already contains reorder";
				}
				else
					error = "Output dir not given";
				if (pairs.containsKey(REF_FILE)) {
					System.out.println("Setting reference file: " + pairs.get(REF_FILE));
					data.setRefPath(pairs.get(REF_FILE));
					reference = new File(pairs.get(REF_FILE));
				}
				else
					error = "no reference file given";
				if (pairs.containsKey(DRAFT_FILE)) {
					System.out.println("Setting draft file: " + pairs.get(DRAFT_FILE));
					data.setDraftPath(pairs.get(DRAFT_FILE));
					unordered = new File(pairs.get(DRAFT_FILE));
				}
				else
					error = "no draft file given";
			} catch (Exception e) {
				e.printStackTrace();
				error = e.getMessage();
			}
			if (error != null) { 
				System.err.println(error);
			//	JOptionPane.showMessageDialog(null, error);
				System.exit(0);
			} // if no errors, start the alignment
	//		else {
				//	System.err.println("Calling startAlignment()");
	//				startAlignment (false);
	//			}
			copyInputFiles();
		}

	private void initGUI () {
		reordererGUI.init();
		////////////////////////////////////////////////////////////
		/*
		 * This code was originally in getFiles(), which was only ever called here though.
		 */
		JFileChooser chooser = new JFileChooser ();
		chooser.setDialogTitle("Choose location to keep output files and folders.");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setMultiSelectionEnabled (false);
		boolean active = true;
		while (active) {
			int choice = chooser.showDialog(parent, "OK");
			if (choice == JFileChooser.APPROVE_OPTION) {
				directory = chooser.getSelectedFile();
				if (getAlignDir ().exists()) {
					JOptionPane.showMessageDialog (parent, "Directory already " +
							"contains reorder; please choose new directory", 
							"Chooose new directory", 
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					active = false;
				}
				
			}
		}
	
	//	startAlignment (true);
		////////////////////////////////////////////////////////////
	/*
	 * Original code 
	 * 	if (getFiles ()) {
			startAlignment (true);
		}
		else{
			
			iterations = 0;
		}*/
	}

	/**
	 * Starts alignment. Shows info message about reordering process to
	 * user if <code>show_message</code> is <code>true</code>. 
	 * 
	 * First function in iterative loop
	 * 
	 * @param show_message true if should print GUI message, false otherwise
	 */
	protected void startAlignment (boolean show_message) {
		try{
		//	System.err.println("AJT0403: Clearing alignment cache.");
    		ModelBuilder.clearDataCache();
    	}catch(BackingStoreException bse)
    	{
    		bse.printStackTrace();
    	}
		if (gui){
			align_frame.setFileInput ();
			align_frame.setVisible(gui);
		}
		if (show_message) {
			
			JOptionPane.showMessageDialog (null, 
					"The reordering process will begin when the Start button is pressed."+ "\n\n"+

					"Contig reordering is an iterative process, and may take anywhere from a half"+"\n"+ 
					"hour to several hours.  It may be cancelled at any point (intermediary results"+ "\n"+
					"will be viewable). If it is cancelled after the first reorder, the data will be"+ "\n"+
					"available in fasta files in the corresponding output directory, although an"+ "\n"+
					"alignment of the last order will not be produced.  If the ordering process is"+"\n"+
					"not manually ended, it will terminate when it finds an order has repeated."+"\n"+
					"Sometimes the order will cycle through several possibilities; this indicates it"+"\n"+
					"cannot determine which of them is most likely. Alignment parameters may be"+ "\n"+
					"changed before reorder starts or any time between alignments.");
		}
		else {
			if (gui) {
				align_frame.alignButtonActionPerformed (null);
			} else {
			//	System.out.println("AJT0403: Running contig reorderer from command line");
				runReorderProcess();
			}
		}
	}
	
	/** 
	 * Second function in iterative loop
	 */
	private void runReorderProcess(){
		
		File outDir = getAlignDir();
		String outDirPath = outDir.getAbsolutePath();
		//outDir.mkdir();
		//copyInputFiles();
		alnmtFile = new File(outDir, DIR_STUB+count);
		String alnmtFilePath = alnmtFile.getAbsolutePath();
		aln_cmd = makeAlnCmd();
		System.out.println("Executing ");
		AlignFrame.printCommand(aln_cmd,System.out);
		AlignWorker worker = new AlignWorker(this,aln_cmd,false);
		worker.start();
	}
	

	
	public void completeAlignment(int retcode) {
		if (retcode == 0) {
			try {
				reorderer.setModel(new XmfaViewerModel(alnmtFile,null));
				reorderer.initModelData();
			} catch (IOException e){
				e.printStackTrace();
			}
		} else {
			System.err.print("Failed to complete the following progressiveMauve alignment\n\n");
			AlignFrame.printCommand(aln_cmd,System.err);
			System.err.println();
		}
	}

	private String[] makeAlnCmd(){
		String[] ret = new String[6 + DEFAULT_ARGS.length];
		int j = 0;
		ret[j++] = AlignFrame.getBinaryPath("progressiveMauve");
		for (int i = 0; i < DEFAULT_ARGS.length; i++)
			ret[j++] = DEFAULT_ARGS[i];
		ret[j++] = "--output="+alnmtFile.getAbsolutePath();
		ret[j++] = "--backbone-output=" + alnmtFile.getAbsolutePath()+".backbone";
		ret[j++] = "--output-guide-tree=" + alnmtFile.getAbsolutePath()+".guide_tree";
		ret[j++] = reference.getAbsolutePath();
		ret[j++] = unordered.getAbsolutePath();
		return ret;
	}
	
	
	
	protected void setFilesFromAlignStart () {
		System.err.println("AJT0403: Calling setFilesFromAlignStart()");
		align_dir = align_dir.getParentFile ();
		XMFAAlignment xmfa = ((XmfaViewerModel) parent.getModel ()).getXmfa ();
		reference = new File (align_dir, new File (xmfa.getName (0)).getName ());
		String name = new File (xmfa.getName (1)).getName ();
		unordered = new File (align_dir, name);
		File file = new File (align_dir,
				reorderer.file + ContigReorderer.FEATURE_EXT);
		if (file.exists ())
			parent.getFeatureImporter ().importAnnotationFile (file, reorderer.fix);
		//iterations = 0;
	}
	
	public boolean shouldReorder () {
		boolean ok = count <= iterations;
		if (align_start)
			setFilesFromAlignStart ();
		/*if (!ok)
			renameLastAlignment ();*/
		return ok;
	}
	

	/**
	 * Fourth function in iterative loop
	 */
	public void checkReorderDone () {
		try {
			System.out.println ("C: " + count);
			File temp = null;
			if (!align_start)
				temp = new File (getAlignDir (), CONTIG_OUTPUT);
			else {
				temp = new File (align_dir, CONTIG_OUTPUT);
			}
			if (orderRepeated ()) {
				System.err.println("AJT0403: Order not repeated. Reseting iteration count to zero.");
				iterations = 0;
				IOUtils.deleteDir (temp);
				reorderer.setInactive();
				if (gui) {
					JOptionPane.showMessageDialog(parent, "The reordering process is done.\n" +
							"Results are displayed, and data is in output directory.", 
							"Reorder Done", JOptionPane.INFORMATION_MESSAGE);
					reorderer.inverted_from_start.clear ();
				}
				else {
					System.exit(0);
				}
			}
			else { // 
				System.err.println("AJT0403: Order not repeated.");
				past_orders.add(reorderer.ordered);
				count++;
				File to = makeAlignDir ();
				temp.renameTo (to);
			/*	temp = new File (to, reference.getName ());
				IOUtils.copyFile (reference, temp);
				reference = temp; */
				unordered = new File (to, MauveHelperFunctions.genomeNameToFasta (reorderer.fix));
				reorderer.feature_file = null;
				// back to step 1.
				startAlignment(false);
				align_start = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected boolean orderRepeated () {
		for (int i = 0; i < past_orders.size(); i++) {
			/*
			 * TODO: Figure out a way to get reorderer.ordered
			 * without having to use reorderer 
			 */
			if (reorderer.ordered.equals(past_orders.get(i)))
				return true;
		}
		return false;
	}
	
	/**
	 * Copies the draft file over into the appropriate directory.
	 */
	public void copyInputFiles () {
		try {
			File dirTo = makeAlignDir ();
		/*	File file = new File (dir, reference.getName ());
			IOUtils.copyFile (reference, file);
			reference = file;*/ 
			File file = new File (dirTo, unordered.getName ());
			IOUtils.copyFile (unordered, file);
			unordered = file;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected File makeAlignDir () {
		File dir = getAlignDir ();
		while (dir.exists ()) {
			System.err.println("AJT0403: Directory "+dir.getName()+" already exists.");
			count++;
			start++;
			iterations++;
			dir = getAlignDir ();
		}
		dir.mkdirs ();
		return dir;
	}
	
	public File getAlignDir () {
		return new File (directory.getAbsolutePath(), DIR_STUB + count);
	}
	
	
	public static void main (String [] args) {	
	//	System.err.println("Well, well, well.... I see you want me to reorder some contigs for you... good luck... HAH!");
		if (args.length != 6){
			System.err.print(USAGE);
			System.exit(-1);
		} else  {
			String badArgs = badArgs(args);
			if (badArgs.length()==0) {
				ContigOrderer co = new ContigOrderer (args, null, false);
				//System.err.println("CALLING startAlignment()");
				//co.startAlignment(false);
			} else {
				System.err.println("The following arguments are missing or were used improperly:  " + badArgs);
				System.err.print(USAGE);
				System.exit(-1);
			}
				
		}
	}
	
	private static boolean argsGood(String[] args){
		HashSet<String> tmp = new HashSet<String>();
		tmp.add(args[0]);
		tmp.add(args[2]);
		tmp.add(args[4]);
		return  tmp.contains(OUTPUT_DIR) && 
				tmp.contains(DRAFT_FILE) && 
				tmp.contains(REF_FILE);
	}
	
	private static String badArgs(String[] args){
		HashSet<String> tmp = new HashSet<String>();
		tmp.add(args[0]);
		tmp.add(args[2]);
		tmp.add(args[4]);
		String ret = "";
		if (!tmp.contains(OUTPUT_DIR))
			ret = ret +" "+OUTPUT_DIR;
		if (!tmp.contains(DRAFT_FILE)) 
			ret = ret + " " +DRAFT_FILE;
		if (!tmp.contains(REF_FILE))
			ret = ret + " " + REF_FILE;
		return ret;
	}

}
