package org.gel.mauve.contigs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;

import org.gel.air.util.GroupHelpers;
import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;
import org.gel.mauve.LCB;
import org.gel.mauve.LCBLeftComparator;
import org.gel.mauve.LCBlist;
import org.gel.mauve.LcbIdComparator;
import org.gel.mauve.LcbViewerModel;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.analysis.Segment;
import org.gel.mauve.backbone.BackboneList;
import org.gel.mauve.contigs.ContigGrouper.ContigGroup;
import org.gel.mauve.gui.Mauve;
import org.gel.mauve.gui.MauveFrame;

public class ContigReorderer extends Mauve implements MauveConstants {

	public static final int REF_IND = 1;
	public static final int REORDER_IND = 2;
	public static final int FILE_IND = 3;
	public static final int MAX_IGNORABLE_DIST = 50;
	public static final double MIN_LENGTH_RATIO = .01;
	protected int ref_ind;
	protected int reorder_ind;
	protected LcbViewerModel model;
	public LcbIdComparator id_compare;
	protected LCB dummy;
	protected Hashtable inverters;
	protected LinkedList ordered;
	protected Hashtable conflicts;
	protected Hashtable nexts;
	protected LCBLeftComparator left_compare;
	protected String input_file;
	protected String slast_ordered;
	protected String feature_file;
	public static final String CONTIG_EXT = "_contigs.tab";
	public static final String FEATURE_EXT = "_features.tab";
	protected MauveFrame frame;
	protected Hashtable args;
	protected String file;
	protected File directory;
	protected LCB[] lcbs;
	protected LCB [] fix_lcbs;
	protected Genome ref;
	protected Genome fix;
	protected Hashtable lcb_table;
	protected Hashtable comparator_table;
	protected Genome [] ordered_genomes;
	protected ContigGrouper grouper;
	protected ContigOrderer orderer;
	protected boolean active;
	protected HashSet inverted_from_start;
	protected HashSet inverted_from_read;
	protected boolean skip_first_frame;
	
	public ContigReorderer (ContigOrderer order) {
		active = true;
		check_updates = false;
		orderer = order;
	}
	
	public ContigReorderer () {
		this (null);
	}
	
	/**
	 * Initializes Mauve environment.
	 * 
	 * @param args		See description of args in main (String [] args).
	 */
	public void init (String [] args) {
		ref_ind = Integer.parseInt (args [REF_IND]);
		reorder_ind = Integer.parseInt (args [REORDER_IND]);
		if (args.length > 3) {
			input_file = (String) args [FILE_IND];
			if (args.length > 4) {
				feature_file = args [4];
				System.out.println ("ff: " + feature_file);
			}
		}
		super.init (args [0]);
	}
	
	/**
	 * Makes a new Mauve frame that calls fixContigs once the alignment has
	 * been set up.
	 */
	protected MauveFrame makeNewFrame () {
		if (skip_first_frame) {
			skip_first_frame = false;
			return null;
		}
		if (active) {
			frame = new ReordererMauveFrame (this);
			frames.add (frame);
			return frame;
		}
		else
			return super.makeNewFrame ();
	}
	
	protected void initMauveData () {
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
		grouper =  new ContigGrouper (this, MAX_IGNORABLE_DIST, MIN_LENGTH_RATIO);
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
		if (feature_file != null)
			frame.getFeatureImporter ().importAnnotationFile (new File (
					feature_file), fix);
		if (ordered == null) {
			ordered = new LinkedList (fix.getChromosomes());
			args.put (ContigFeatureWriter.ORDERED_CONTIGS, ordered);
			output (false);
		}
		directory = new File (directory + File.separator +
				CONTIG_OUTPUT);
		ordered = new LinkedList ();
		args.put (ContigFeatureWriter.ORDERED_CONTIGS, ordered);
		if (!directory.exists())
			directory.mkdir();

	}
	
	protected void orderGenomes () {
		ordered_genomes = new Genome [model.getSequenceCount () - 1];
		for (int i = ref_ind; i < model.getSequenceCount (); i++) {
			if (i != reorder_ind)
				ordered_genomes [i] = model.getGenomeBySourceIndex(i);
		}
		ref = ordered_genomes [ref_ind];
	}
	
	protected void fixContigs () {
		process ();
		output (true);
	}
	
	public void process () {
		adjustLCBs ();
		LCB [] ids = (LCB []) lcb_table.get(fix);
		fix_lcbs = new LCB [ids.length];
		System.arraycopy (ids, 0, fix_lcbs, 0, ids.length);
		Arrays.sort (fix_lcbs, id_compare);
		if (input_file == null)
			new ContigInverter (model, this);
		else
			new ContigInverter (model, this, input_file);
	}
	
	public void output (boolean fasta) {
		if (fasta)
			new FastAContigChangeWriter (this);
		new ContigFeatureWriter (new File (
				directory, file + CONTIG_EXT).getAbsolutePath (), args);
		Iterator feats = MauveHelperFunctions.getFeatures (model, reorder_ind);
		if (feats.hasNext ()) {
			new ChangedFeatureWriter (new File (
					directory, file + FEATURE_EXT).getAbsolutePath (), args, feats, fix);
		}
	}
	
	public void setReference (Genome gen) {
		ref = gen;
		lcbs = (LCB []) lcb_table.get(ref);
		left_compare = (LCBLeftComparator) comparator_table.get(ref);
	}
	
	protected void removeBadLCBs () {
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
	}
	
	protected void adjustLCBs () {
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
	}
	
	protected void trimLCBs (String last_ordered) {
		if (last_ordered != null) {
			Iterator itty = ref.getChromosomes ().iterator ();
			long last = ref.getLength ();
			while (itty.hasNext ()) {
				Chromosome chrom = (Chromosome) itty.next ();
				System.out.println ("chrom: " + chrom.getName ());
				if (last_ordered.equals (chrom.getName ())) {
					last = chrom.getEnd ();
					System.out.println ("setting last: " + last);
					break;
				}
			}
			System.out.println ("start size: " + lcbs.length);
			for (int i = 0; i < lcbs.length; i++) {
				if (lcbs [i].getLeftEnd (ref) > last) {
					LCB [] temp = new LCB [i + 1];
					System.arraycopy (lcbs, 0, temp, 0, i + 1);
					lcbs = temp;
					break;
				}
			}
			System.out.println ("end size: " + lcbs.length);
		}
	}
	
	protected LCB getAdjacentLCB (boolean left, LCB cur) {
		try {
			int id = -1;
			if (left)
				id = cur.getLeftAdjacency (fix);
			else
				id = cur.getRightAdjacency (fix);
			dummy.id = id;
			id = Arrays.binarySearch (fix_lcbs, dummy, id_compare);
			return id > -1 ? fix_lcbs [id] : null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * gets next lcb that is also in the reference genome
	 */
	/*protected LCB getAdjacentLCB (boolean left, LCB cur) {
		LCB next = null;
		try {
			int id = -1;
			if (left)
				id = cur.getLeftAdjacency (fix);
			else
				id = cur.getRightAdjacency (fix);
			dummy.id = id;
			id = Arrays.binarySearch (fix_lcbs, dummy, id_compare);
			if (id != -1) {
				next = fix_lcbs [id];
				if (next.getLeftEnd(ref) == 0)
					next = getAdjacentLCB (left, next);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return next;
	}*/
	
	public boolean isReversed (LCB lcb) {
		return lcb.getReverse(fix) != lcb.getReverse(ref);
	}
	
	//from contig reorderer _contig.tab file
	/*public void readOrdered (String file) {
		try {
			System.out.println ("getting ordered");
			Hashtable chroms = new Hashtable (fix.getChromosomes ().size ());
			Iterator itty = fix.getChromosomes ().iterator ();
			while (itty.hasNext ()) {
				Chromosome chrom = (Chromosome) itty.next ();
				chroms.put (chrom.getName ().trim (), chrom);
			}
			BufferedReader in = new BufferedReader (new FileReader (file));
			String input = new String (in.readLine ());
			while (!input.trim ().equals (ContigFeatureWriter.ORDERED_CONTIGS))
				input = in.readLine ();
			input = in.readLine ();
			input = in.readLine ();
			while (input != null && !input.trim ().equals ("")) {
				StringTokenizer toke = new StringTokenizer (input);
				toke.nextToken ();
				String name = toke.nextToken ();
				System.out.println ("name: " + name);
				ordered.add (chroms.get (name));
				input = in.readLine ();
			}
			in.close ();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	//from tab or comma separated file with contig names in first row; cuts off name before
	//first underscore - specialized for projector output
	public void readOrdered (String file) {
		try {
			System.out.println ("getting ordered");
			Hashtable <String, Chromosome> chroms = new Hashtable <String, Chromosome> 
					(fix.getChromosomes ().size ());
			Iterator itty = fix.getChromosomes ().iterator ();
			while (itty.hasNext ()) {
				Chromosome chrom = (Chromosome) itty.next ();
				//System.out.println ("putting: " + chrom.getName ().trim ());
				chroms.put (chrom.getName ().trim (), chrom);
			}
			BufferedReader in = new BufferedReader (new FileReader (file));
			String input = new String (in.readLine ().trim());
			while (input != null && !input.equals ("")) {
				StringTokenizer toke = new StringTokenizer (input, "\t,", false);
				String name = toke.nextToken ().trim ();
				//for long named contigs separated by underscores
				/*int under = name.indexOf ("_bp_");
				if (under > -1) {
					name = name.substring (0, under);
					name = name.substring (0, name.lastIndexOf ('_'));
				}*/
				if (chroms.containsKey (name))
					ordered.add (chroms.get (name));
				input = in.readLine ();
			}
			in.close ();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void readKeepers (String file) {
		try {
			System.out.println ("getting keepers");
			HashSet <String> chroms = new HashSet <String> 
					(fix.getChromosomes ().size ());
			BufferedReader in = new BufferedReader (new FileReader (file));
			String input = new String (in.readLine ().trim());
			while (input != null && !input.equals ("")) {
				StringTokenizer toke = new StringTokenizer (input, "\t,", false);
				String name = toke.nextToken ().trim ();
				//for long named contigs separated by underscores
				/*int under = name.indexOf ("_bp_");
				if (under > -1) {
					name = name.substring (0, under);
					name = name.substring (0, name.lastIndexOf ('_'));
				}*/
				chroms.add(name);
				input = in.readLine ();
			}
			in.close ();

			Iterator itty = fix.getChromosomes ().iterator ();
			while (itty.hasNext ()) {
				Chromosome chrom = (Chromosome) itty.next ();
				//System.out.println ("putting: " + chrom.getName ().trim ());
				if (chroms.contains(chrom.getName ().trim ()))
					ordered.add(chrom);
			}
			System.out.println ("keepers: " + ordered.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean containsEndOfLCB (ContigGrouper.ContigGroup group, boolean left) {
		LCB lcb = left ? group.first : group.last;
		long loc = left ? lcb.getLeftEnd (ref) : lcb.getRightEnd (ref) - 10;
		Segment segment = grouper.bb.getNextBackbone (ref, loc);
		return segment.starts [reorder_ind] == 0 ? false : true;
	}
	
	public LCB getLCBAt (LCB start, long pos) {
		while (start.getRightEnd(fix) < pos)
			start = getAdjacentLCB (false, start);
		if (start.getLeftEnd(fix) > pos)
			start = null;
		if (start != null)
			System.out.println ("lcb: " + start.getLeftEnd(fix));
		return start;
	}
	
	public Genome getClosestRelation (LCB lcb) {
		for (int i = 0; i < ordered_genomes.length; i++) {
			if (lcb.getLeftEnd(ordered_genomes [i]) != 0)
				return ordered_genomes [i];
		}
		return null;
	}
	
	public void addGroupToInverters (ContigGroup group) {
		Iterator contigs = group.getNonEmpty ().iterator();
		Chromosome contig;
		while (contigs.hasNext()) {
			contig = (Chromosome) contigs.next();
			if (MauveHelperFunctions.getChromByStart(inverters, contig) == null) {
				MauveHelperFunctions.addChromByStart (inverters, 
						contig);
				switchOverallOrientation (contig);
			}
		}
	}
	
	protected void switchOverallOrientation (Chromosome contig) {
		String chrom = contig.getName();
		if (inverted_from_start.contains(chrom))
			inverted_from_start.remove(chrom);
		else
			inverted_from_start.add (chrom);
	}
	
	public void removeGroupFromInverters (ContigGroup group) {
		Iterator contigs = group.getNonEmpty ().iterator();
		Chromosome contig;
		while (contigs.hasNext()) {
			contig = (Chromosome) contigs.next();
			if (MauveHelperFunctions.removeChromByStart (inverters, 
					contig) != null)
				switchOverallOrientation (contig);
		}
	}
	
	

	protected synchronized MauveFrame getNewFrame() {
		return super.getNewFrame();
	}
	
	/**
	 * @param args			Index 0 should be the name of the alignment file,
	 * 						  1 the reference genome's index, 2 the index of the
	 * 						  genome to reorder.
	 */
	public static void main (String [] args) {
		if (args.length > 0)
			new ContigReorderer ().init (args);
		else
			new ContigOrderer (null, false);
	}

	 public class ReordererMauveFrame extends MauveFrame {
		 
		 	public ReordererMauveFrame (ContigReorderer ord) {
		 		super (ord);
				if (orderer != null)
					orderer.parent = this;
		 	}
		 	
			public void setModel (BaseViewerModel mod) {
				super.setModel (mod);
				ContigReorderer.this.model = (LcbViewerModel) mod;
				new Thread (new Runnable () {
					public void run () {
						initMauveData ();
						lcbs = ContigReorderer.this.model.getFullLcbList ();
						if (orderer == null || (active && orderer.shouldReorder ())) {
							fixContigs ();
							if (orderer != null)
								orderer.reorderDone ();
						}
					}
				}).start ();				
			}
		}
}