package org.gel.mauve.analysis;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.analysis.output.SegmentDataProcessor;
import org.gel.mauve.gui.Mauve;
import org.gel.mauve.gui.MauveFrame;
import org.gel.mauve.module.MauveModule;
import org.gel.mauve.module.ModuleListener;

/**
 * This class interfaces between Mauve's existing code and modules that may or
 * may not become a part of Mauve, but require it's base classes to run
 * 
 * @author rissman
 * 
 */
// currently, is being allowed to instantiate gui; easier to do than not,
// and i don't know if it will be necessary or not
public class MauveInterfacer extends MauveModule implements MauveConstants,
		ModuleListener {


	public static LinkedList feat_files = new LinkedList ();
	
	public MauveInterfacer() {
		super();
		setListener (this);
	}
	
	/**
	 * waits for parent class to finish init processing, and then extracts
	 * necessary data
	 * 
	 * @param file
	 *            The file containing alignment data
	 */
	public void init (String file) {
		super.init (file);
	}
	
	public void startModule (MauveFrame frame) {
		AnalysisModule anal = new AnalysisModule (frame);
		Iterator itty = MauveInterfacer.feat_files.iterator (); 
		while (itty.hasNext ()) {
			Object [] data = (Object []) itty.next (); 
			frame.getPanel ().getFeatureImporter ().importAnnotationFile (
					(File) data [0], frame.getModel ().getGenomeBySourceIndex ((
					(Integer) data [1]).intValue ()));
		}
		anal.doAnalysis();
	}
	
	public static void main (String [] args) {
		try {
			int ind = 1;
			while (ind < args.length) {
				Object [] data = new Object [2];
				data [0] = new File (args [ind++]);
				data [1] = new Integer (args [ind++]);
				feat_files.add (data);
			}
			new MauveInterfacer ().init (args[0]);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println ("No filename given");
			e.printStackTrace ();
		}
	}

}
