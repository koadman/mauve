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
	
	public static final String CONTIG_EXT = "_contigs.tab";
	public static final String FEATURE_EXT = "_features.tab";
	
	
	private  MauveFrame frame;
	private boolean active;
	private ContigReorderer reorderer;
	private ContigOrderer orderer;
	
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
	}
	
	public void init () {
		if (frames == null) 
			super.init();
	}
	
	protected MauveFrame makeNewFrame () {
		if (active) {
			frame = new ReordererMauveFrame ();
			frames.add (frame); 
			return frame;
		}
		else
			return super.makeNewFrame ();
	}	
/*	
	@Override 
	public void loadFile(File rr_file) {
		if (orderer.gui)
			super.loadFile(rr_file);
	}
*/
	protected synchronized MauveFrame getNewFrame() {
		return super.getNewFrame();
	}
	
	public class ReordererMauveFrame extends MauveFrame {
		 

		 public ReordererMauveFrame () {
		 		super (ContigReordererGUI.this);
		 		if (orderer != null)
					orderer.parent = this;
				super.jMenuToolsOrderContigs.setEnabled(false);
		 	}
		 	
		 /**
		  * This is where the work for the iterative process is done
		  * 
		  * this gets called by the FrameLoader created in 
		  * Mauve.loadFile which gets called in completeAlignment
		  * .
		  */
			public void setModel (BaseViewerModel mod) {
				super.setModel (mod);
				// we can just call completeAlignment because alnmtFile 
				// will always be what is given here.
				//ContigReordererGUI.this.reorderer.setModel((LcbViewerModel) mod);
				new Thread (new Runnable () {
					public void run () {
						/*
						 * TODO
						 * we should initialize everything for reorderer here
						 * and then have a function to call that checks if we 
						 * should reorder and then, if we need to reorder,
						 * we call start alignment
						 * 
						 * this gets done in orderer.completeAlignment()
						 * maybe we should just call that like so...
						 * 				orderer.completeAlignment(0);
						 */
						//ContigReordererGUI.this.initModelData ();
						orderer.completeAlignment(0);
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
