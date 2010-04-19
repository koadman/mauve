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
	
	/**
	 * initializes a bunch of member variables.
	 */
/*	private void initMauveData () {
		if (inverted_from_start == null)
			inverted_from_start = new HashSet ();
		args = new Hashtable ();
		inverters = new Hashtable ();
		conflicts = new Hashtable ();
		nexts = new Hashtable ();
		lcb_table = new Hashtable ();
		comparator_table = new Hashtable ();
		orderGenomes ();
		if (input_file != null && input_file.indexOf (FEATURE_EXT) > 0) {
			feature_file = input_file;
			input_file = null;
		}
		fix = model.getGenomeBySourceIndex (reorder_ind);
		id_compare = new LcbIdComparator ();
		grouper =  new ContigGrouper (reorderer, MAX_IGNORABLE_DIST, MIN_LENGTH_RATIO);
		args.put (ContigFeatureWriter.REVERSES, inverters);
		args.put (ContigFeatureWriter.CONFLICTED_CONTIGS, conflicts);
		args.put(ContigFeatureWriter.COMPLEMENT, inverted_from_start);
		file = fix.getDisplayName ();
		int period = file.toLowerCase ().indexOf (".fas");
		if (period > -1)
			file = file.substring (0, period);
		if (file.endsWith("."))
				file = file.substring (0, file.length() - 1);
		directory = MauveHelperFunctions.getRootDirectory (model);
		File feats = new File (directory, file + ContigReorderer.FEATURE_EXT);
		if (feats.exists ())
			feature_file = feats.getAbsolutePath ();
		if (feature_file != null && frame != null)
			frame.getFeatureImporter ().importAnnotationFile (new File (
					feature_file), fix);
		if (ordered == null) {
			ordered = new LinkedList (fix.getChromosomes());
			args.put (ContigFeatureWriter.ORDERED_CONTIGS, ordered);
			output (false);
		}
		directory = new File (directory + File.separator /*+ TODO
				CONTIG_OUTPUT*);
		ordered = new LinkedList ();
		args.put (ContigFeatureWriter.ORDERED_CONTIGS, ordered);
		if (!directory.exists())
			directory.mkdir();

	}*/
	
/*	protected void orderGenomes () {
		ordered_genomes = new Genome [model.getSequenceCount () - 1];
		for (int i = ref_ind; i < model.getSequenceCount (); i++) {
			if (i != reorder_ind)
				ordered_genomes [i] = model.getGenomeBySourceIndex(i);
		}
		ref = ordered_genomes [ref_ind];
	}*/
	
	/**
	 * If the reorder process isn't done, this gets called at initModelData
	 * 
	 */
	
	/* This commented-out block of code below is in ContigReorder */
/*	protected void fixContigs () {
		process ();
		output (true);
	}*/
	
/*	public void process () {
		adjustLCBs ();
		LCB [] ids = (LCB []) lcb_table.get(fix);
		fix_lcbs = new LCB [ids.length];
		System.arraycopy (ids, 0, fix_lcbs, 0, ids.length);
		Arrays.sort (fix_lcbs, id_compare);
		if (input_file == null)
			new ContigInverter (model, reorderer);
		else
			new ContigInverter (model, reorderer, input_file);
	}*/
	
/*	public void output (boolean fasta) {
		if (fasta) {
			new FastAContigChangeWriter(this.fix, this.inverters, 
					this.ordered, this.conflicts, this.nexts, this.directory);
		}
		new ContigFeatureWriter (new File (
				directory, file + CONTIG_EXT).getAbsolutePath (), args);
		Iterator feats = MauveHelperFunctions.getFeatures (model, reorder_ind);
		if (feats.hasNext ()) {
			new ChangedFeatureWriter (new File (
					directory, file + FEATURE_EXT).getAbsolutePath (), args, feats, fix);
		}
	}*/

/*	protected void adjustLCBs () {
		dummy = new LCB (0);
		removeBadLCBs ();
		for (int i = 0; i < model.getSequenceCount(); i++) {
			Genome cur = model.getGenomeBySourceIndex(i);
			left_compare = new LCBLeftComparator (cur);
			Arrays.sort (lcbs, left_compare);
			Vector vec = new Vector ();
			for (int ind = 0; ind < lcbs.length; ind++) {
				if (lcbs [ind].getRightEnd(cur) > 0 && lcbs [ind].getRightEnd(fix) > 0)
					vec.add(lcbs [ind]);
			}
			LCB [] temp = new LCB [vec.size()];
			vec.toArray(temp);
			lcb_table.put(cur, temp);
			comparator_table.put (cur, left_compare);
		}
		setReference (ref);
		System.out.println ("new lcbs: " + lcbs.length);
	}*/
	
/*	public void setReference (Genome gen) {
		ref = gen;
		lcbs = (LCB []) lcb_table.get(ref);
		left_compare = (LCBLeftComparator) comparator_table.get(ref);
	}*/
	
/*	protected void removeBadLCBs () {
		left_compare = new LCBLeftComparator (fix);
		Arrays.sort (lcbs, left_compare);
		fix_lcbs = new LCB [lcbs.length];
		System.arraycopy (lcbs, 0, fix_lcbs, 0, lcbs.length);
		Vector data = new Vector (lcbs.length);
		Arrays.sort (fix_lcbs, id_compare);
		GroupHelpers.arrayToCollection (data, lcbs);
		Iterator itty = data.iterator ();
		while (itty.hasNext ()) {
			LCB lcb = null;
			try {
				lcb = (LCB) itty.next ();
				if (lcb.getLeftEnd (fix) > 0) {
					ContigGrouper.ContigGroup group = grouper.getContigGroup (lcb);
				}
			} catch (Exception e) {
				System.out.println ("removed lcb: " + lcb.getLeftEnd (fix));
				itty.remove ();
			}
		}
		lcbs = new LCB [data.size ()];
		data.toArray (lcbs);
		LCBlist.computeLCBAdjacencies (lcbs, model);
	}*/
	
	
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
