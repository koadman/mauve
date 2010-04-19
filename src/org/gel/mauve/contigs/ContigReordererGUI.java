package org.gel.mauve.contigs;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.JMenuItem;

import org.gel.air.util.GroupHelpers;
import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;
import org.gel.mauve.LCB;
import org.gel.mauve.LCBLeftComparator;
import org.gel.mauve.LCBlist;
import org.gel.mauve.LcbIdComparator;
import org.gel.mauve.LcbViewerModel;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.ModelBuilder;
import org.gel.mauve.gui.Mauve;
import org.gel.mauve.gui.MauveFrame;

public class ContigReordererGUI extends Mauve {
	
	public static final int REF_IND = 1;
	public static final int REORDER_IND = 2;
	public static final int FILE_IND = 3;
	public static final int MAX_IGNORABLE_DIST = 50;
	public static final double MIN_LENGTH_RATIO = .01;
	protected int ref_ind = 0;
	protected int reorder_ind = 1;
	protected LcbViewerModel model;
	public LcbIdComparator id_compare;
	protected LCB dummy;
	protected Hashtable inverters;
	protected LinkedList<Chromosome> ordered;
	protected Hashtable<Long,Chromosome> conflicts;
	
	// this looks vestigial
	protected Hashtable nexts;
	protected LCBLeftComparator left_compare;
	protected String input_file;
//	protected String slast_ordered; // unused
	protected String feature_file;
	public static final String CONTIG_EXT = "_contigs.tab";
	public static final String FEATURE_EXT = "_features.tab";
	protected MauveFrame frame;
	protected Hashtable<String,Object> args;
	protected String file;
	protected File directory;
	protected LCB[] lcbs;
	protected LCB [] fix_lcbs;
	protected Genome ref;
	protected Genome fix;
	protected Hashtable<Genome,LCB[]>  lcb_table;
	protected Hashtable<Genome,LCBLeftComparator> comparator_table;
	protected Genome [] ordered_genomes;
	protected ContigGrouper grouper;
	protected ContigOrderer orderer;
	protected boolean active;
	protected HashSet inverted_from_start;
	protected HashSet inverted_from_read;
	
	ContigReorderer reorderer;
	
	public ContigReordererGUI(ContigReorderer reorderer, Vector frames) {
		active = true;
		this.reorderer = reorderer;
		this.orderer = reorderer.orderer;
		this.frames = frames;
	}
	
	
	
	/**
	 * Set this.lcbs to the LCBs from the model stored by 
	 * ContigReordererGUI
	 */
	protected void initModelData () {
		reorderer.initModelData();
		/*
		initMauveData ();
		/*
		 * Set this.lcbs to that of the current model.
		 *
		lcbs = ContigReordererGUI.this.model.getFullLcbList ();
		/*
		 * now check to see if we should reorder again
		 *
		if (orderer == null || (active && orderer.shouldReorder ())) {
			if (orderer == null){
				System.out.println("AJT0403: ContigOrderer orderer == null.");
			}
			fixContigs ();
			if (orderer != null)
				orderer.reorderDone ();
		}*/
	}
	
	public void init () {
		if (frames == null) 
			super.init();
	}
	
	protected MauveFrame makeNewFrame () {
		/*
		 * FIXME TODO Figure out what this was used for originally.
		 */
		if (active) {
			frame = new ReordererMauveFrame ();
	//		frames.add (frame); 
			return frame;
		}
		else
			return super.makeNewFrame ();
	}	
	
	@Override 
	public void loadFile(File rr_file) {
		if (orderer.gui)
			super.loadFile(rr_file);
		else {
			try {
				reorderer.setModel((LcbViewerModel) ModelBuilder.buildModel (rr_file, null));
				initModelData ();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected synchronized MauveFrame getNewFrame() {
		return super.getNewFrame();
	}
	
	public class ReordererMauveFrame extends MauveFrame {
		 

		 public ReordererMauveFrame () {
		 		super (ContigReordererGUI.this);
		 		if (orderer != null)
					orderer.parent = this;
		 	}
		 	
		 /**
		  * This is where the work for the iterative process is done.
		  */
			public void setModel (BaseViewerModel mod) {
				super.setModel (mod);
				ContigReordererGUI.this.reorderer.setModel((LcbViewerModel) mod);
				new Thread (new Runnable () {
					public void run () {
						ContigReordererGUI.this.initModelData ();
					}
				}).start ();				
			}
			
			 public void actionPerformed (ActionEvent ae) {
				 JMenuItem source = (JMenuItem) (ae.getSource());
				 if (source == jMenuFileOpen || ae.getActionCommand().equals("Open"))
				 {
					 ((MauveFrame) frames.get(0)).doFileOpen();
				 }
				 else if (source == jMenuFileAlign)
				 {
					 ((MauveFrame) frames.get(0)).doAlign();
				 }
				 else if (source == jMenuFileProgressiveAlign)
				 {
					 ((MauveFrame) frames.get(0)).doProgressiveAlign();
				 }
				 else
					 super.actionPerformed(ae);
			 }
	 }
	
}
