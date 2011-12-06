package org.gel.mauve.contigs;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.EventListenerList;

import org.gel.air.util.IOUtils;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.ModelListener;
import org.gel.mauve.MyConsole;
import org.gel.mauve.XMFAAlignment;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.gui.AlignmentProcessListener;
import org.gel.mauve.gui.MauveFrame;

public class ContigOrderer implements MauveConstants {
	
	protected File directory;
	protected File unordered;
	protected File reference;
	protected File align_dir;
	public static final String DIR_STUB = "alignment";
	protected int count = 1;
	protected int start = 0;
	protected ContigReorderer reorderer;
	protected MauveFrame parent;
	protected ContigMauveAlignFrame align;
	public static final int DEFAULT_ITERATIONS = 15;
	protected int iterations = DEFAULT_ITERATIONS;
	public static final String ALIGN_START = "Start from alignment file.";
	public static final String SEQ_START = "Start from sequence files.";
	protected boolean align_start;
	protected Vector past_orders;
	protected boolean gui;
	protected static final String OUTPUT_DIR = "-output";
	protected static final String REF_FILE = "-ref";
	protected static final String DRAFT_FILE = "-draft";
	private EventListenerList alnListeners;
	private File alnmtFile;
	
	public ContigOrderer (String [] args, Vector frames, boolean gui) {
		init (args, frames, gui);
		if (gui)
			initGUI ();
		else
			initParams (args);
	}
	
	public ContigOrderer (String [] args, Vector frames) {
		this (args, frames, true);
	}
	
	public void initParams (String [] args) {
		Hashtable <String, String> pairs = IOUtils.parseDashPairedArgs(args);
		String error = null;
		try {
			if (pairs.containsKey(OUTPUT_DIR)) {
				directory = new File (pairs.get(OUTPUT_DIR));
				if (!directory.exists()) {
					if (!directory.mkdirs())
						error = "Couldn't create output directory";
				}
				else if (getAlignDir ().exists())
					error = "Directory already contains reorder";
			}
			else
				error = "Output dir not given";
			if (pairs.containsKey(REF_FILE)) {
				System.out.println ("ref file: " + align);
				align.addSequence(pairs.get(REF_FILE));
			}
			else
				error = "no reference file given";
			if (pairs.containsKey(DRAFT_FILE)) {
				align.addSequence(pairs.get(DRAFT_FILE));
			}
			else
				error = "no draft file given";
		} catch (Exception e) {
			e.printStackTrace();
			error = e.getMessage();
		}
		if (error != null) {
			if(gui){
				JOptionPane.showMessageDialog(null, error);
			}else{
				System.err.println(error);
			}
			System.exit(0);
		}
		else {
			align.setArgs (pairs);
			startAlignment (false);
		}
	}
	
	public void initGUI () {
		reorderer.init();
		if (getFiles ()) {
			startAlignment (true);
		}
		else
			iterations = 0;
	}
	
	public void init (String [] args, Vector frames, boolean gui) {
		this.gui = gui;
		alnListeners = new EventListenerList();
		past_orders = new Vector ();
		iterations = DEFAULT_ITERATIONS;
		if (args != null && args.length > 0) {
			try {
				iterations = Integer.parseInt (args [0]);
			} catch (NumberFormatException e) {
			}
		}
		reorderer = new ContigReorderer (this, frames);
		MyConsole.setUseSwing (gui);
		MyConsole.showConsole ();
		reorderer.ref_ind = 0;
		reorderer.reorder_ind = 1;
		align = new ContigMauveAlignFrame (
				reorderer, this, gui);
	}
	
	protected void startAlignment (boolean show_message) {
		align.displayFileInput ();
		align.setVisible(gui);
		if (show_message) {
			JOptionPane.showMessageDialog (null, 
					"The reordering will begin when the start button is pressed.  " +
					"It is an iterative process,\nand may take anywhere " +
					"from a half hour to several hours.  It may\nbe cancelled " +
					"at any point (intermediary results will be viewable).\n" +
					"If it is cancelled after the first reorder "
					+ "the data will be\navailable in fasta files in the " +
					"corresponding output directory, although\nan alignment of" +
					" the last order will not be produced.  If the ordering" +
					" process\n is not manually ended, it will terminate when it "
					+ "finds an order has repeated.\nSometimes the order will" + 
					" cycle through several possibilities; this\nindicates it" +
					" cannot determine which of them is most likely.\n " +
					"Alignment parameters may be changed before reorder starts or " +
					"any time between alignments.");
		}
		else
			align.alignButtonActionPerformed (null);
	}
	
	public boolean getFiles () {
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
				}
				else {
					/*Object val = JOptionPane.showInputDialog (parent, "Choose input type:", 
					"Start from:", JOptionPane.QUESTION_MESSAGE, null, new String [] {
					SEQ_START, ALIGN_START}, SEQ_START);
			if (val == null)
				return false;
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (val == SEQ_START) {
				return true;
			}
			else {
				JOptionPane.showMessageDialog (parent, "Alignment should contain only the " +
						"source and reference genome;\n the reference genome should be displayed " +
						"above the genome to reorder.\n  Select the alignment file and not the " +
						"directory containing the alignment.\n  For alignments generated from the " +
						"contig reordering program,\n this is the file with the identical name to " +
						"the containing directory\n (alignment followed by a number).",
						"Start from alignment", JOptionPane.INFORMATION_MESSAGE);
				chooser.setDialogTitle ("Select alignment file");
				choice = chooser.showDialog (parent, "OK");
				if (choice == JFileChooser.APPROVE_OPTION) {
					align_start = true;
					align_dir = chooser.getSelectedFile ();
					reorderer.loadFile (align_dir);
				}
			}*/
					return true;
				}
			}
			else
				return false;
		}
		return false;
	}
	
	protected void setFilesFromAlignStart () {
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
	
	public void renameLastAlignment () {
		File from = getAlignDir ();
		File to = new File (directory, "final_" + DIR_STUB);
		from.renameTo (to);
	}
	
	public void reorderDone () {
		try {
			System.out.println ("C: " + count);
			File temp = null;
			if (!align_start)
				temp = new File (getAlignDir (), CONTIG_OUTPUT);
			else {
				temp = new File (align_dir, CONTIG_OUTPUT);
			}
			// ugh, this is really ugly hardcoding.
			String alnmtFilename = directory + "/alignment" + count;
			alnmtFilename += "/alignment" + count;
			alnmtFile = new File( alnmtFilename );
			count++;
			if (orderRepeated () || count > iterations) {
				iterations = 0;
				IOUtils.deleteDir (temp);
				reorderer.active = false;
				if (gui) {
					JOptionPane.showMessageDialog(parent, "The reordering process is done.\n" +
							"Results are displayed, and data is in output directory.", 
							"Reorder Done", JOptionPane.INFORMATION_MESSAGE);
					reorderer.inverted_from_start.clear ();
				}
				else if (alnListeners != null){
					fireAlignmentEvent();
				}else {
					System.exit(0);
				}
			}
			else {
				past_orders.add(reorderer.ordered);
				File to = makeAlignDir ();
				temp.renameTo (to);
				temp = new File (to, reference.getName ());
				IOUtils.copyFile (reference, temp);
				reference = temp;
				unordered = new File (to, MauveHelperFunctions.genomeNameToFasta (
						reorderer.fix));
				reorderer.feature_file = null;
				startAlignment (align_start);
				align_start = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected boolean orderRepeated () {
		for (int i = 0; i < past_orders.size(); i++) {
			if (reorderer.ordered.equals(past_orders.get(i)))
				return true;
		}
		return false;
	}

	public void addAlignmentProcessListener(AlignmentProcessListener listener){
		alnListeners.add(AlignmentProcessListener.class, listener);
	}

	private void fireAlignmentEvent(){
		Object [] listeners = alnListeners.getListenerList ();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == AlignmentProcessListener.class) {
				((AlignmentProcessListener) listeners[i + 1]).completeAlignment (0);
			}
		}
	}

	public File getAlignmentFile(){
		return alnmtFile;
	}		
	public void copyInputFiles () {
		try {
			File dir = makeAlignDir ();
			dir.mkdirs ();
			File file = new File (dir, reference.getName ());
			IOUtils.copyFile (reference, file);
			reference = file;
			file = new File (dir, unordered.getName ());
			IOUtils.copyFile (unordered, file);
			unordered = file;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected File makeAlignDir () {
		File dir = getAlignDir ();
		while (dir.exists ()) {
			count++;
			start++;
			iterations++;
			dir = getAlignDir ();
		}
		//dir.mkdirs ();
		return dir;
	}
	
	public File getAlignDir () {
		return new File (directory.getAbsolutePath(), DIR_STUB + count);
	}
	
	
	public static void main (String [] args) {	
		new ContigOrderer (args, null, args.length == 0);
	}

}
