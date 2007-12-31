package org.gel.mauve.ext.io;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.StringTokenizer;

import org.gel.air.ja.stash.Stash;
import org.gel.air.ja.stash.StashLoader;
import org.gel.mauve.LCB;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.backbone.Backbone;
import org.gel.mauve.backbone.BackboneListBuilder;
import org.gel.mauve.ext.MauveStoreConstants;
import org.gel.mauve.tree.GISTree;
import org.gel.mauve.tree.GapKey;
import org.gel.mauve.tree.Key;

/**
 * Assumes bblist is in same order as bbcols file.
 * 
 * @author Anna I Rissman/James Lowden
 *
 */
public class AlignmentConverter implements MauveStoreConstants {
	
	protected XmfaViewerModel model;
	protected Stash align;
	protected String bb_file;
	protected LCB [] lcb_list;
	protected Backbone [] bb_list;
	protected long gap_file_pos;
	protected DataOutputStream gap_out;
	protected boolean use_ints;
	protected StashLoader loader;

	public AlignmentConverter (XmfaViewerModel mod) {
		model = mod;
		loader = new StashLoader (null, null);
		convertToStash ();
		loader.writeXMLFile (align, "c:\\align.xml");
	}
	
	protected void convertToStash () {
		align = new Stash (ALIGNMENT_CLASS);
		align.put(URI, model.getSrc ().toURI().toString());
		lcb_list = model.getFullLcbList();
		bb_list = model.getBackboneList().getBackboneArray();
		align.put(LCB_COUNT, lcb_list.length + "");
		String bb_file = "c:\\bb.bbcols";
		align.put(BACKBONE_FILE, bb_file);
		writeBbcolsFile (bb_file);
		//SegmentWriter.getSegmentWriter("c:\\lcbs.txt", model);
		Stash genome_list = new Stash (LIST_CLASS, GENOMES);
		align.put(GENOMES, genome_list);
		for (int i = 0; i < model.getSequenceCount(); i++) {
			Stash genome = new Stash (ALIGNED_GENOME_CLASS);
			genome_list.put(genome.get (ID), genome.get (ID));
			genome.put(GENOME, new GbkConverter (
					model.getGenomeBySourceIndex(i), ASAP, loader).genome.get(ID));
			String gap_file = "c:\\" + genome.get(ID) + ".gaps";
			genome.put(GAP_FILE, gap_file);
			if (model.getGenomeBySourceIndex(i).getLength() < Integer.MAX_VALUE)
				use_ints = true;
			try {
				gap_out = new DataOutputStream (new BufferedOutputStream (
						new FileOutputStream (gap_file)));
				gap_file_pos = 0;
				gap_out.writeBoolean(use_ints);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Stash lcbs = new Stash (LIST_CLASS, LCBS);
			genome.put(LCBS, lcbs);
			convertLCBs (lcbs, i);
			Stash backbone = new Stash (LIST_CLASS, BACKBONE_IDS);
			genome.put(BACKBONE, backbone);
			convertBackbone (backbone, i);
			try {
				gap_out.close();
				loader.writeXMLFile(genome, "c:\\" + genome.getInt(ID) + ".xml");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void writeBbcolsFile (String file) {
		try {
			BufferedReader in = new BufferedReader (new FileReader (
					BackboneListBuilder.getBbFile(model, model.getXmfa())));
			PrintStream out = new PrintStream (new BufferedOutputStream (
					new FileOutputStream (file)));
			String input = in.readLine();
			while (input != null) {
				StringTokenizer toke = new StringTokenizer (input);
				out.println (toke.nextToken() + "\t" + toke.nextToken() + "\t" +
						toke.nextToken());
				input = in.readLine();
			}
			in.close();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	protected void convertLCBs (Stash lcbs, int genome_ind) {
		for (int i = 0; i < lcb_list.length; i++) {
			if (lcb_list [i].starts [genome_ind] != 0) {
				Stash lcb = new Stash (LCB_CLASS, lcb_list [i].id + "");
				addLocation (lcb, lcb_list [i].starts [genome_ind],
						lcb_list [i].ends [genome_ind]);
				lcb.put(REVERSE, lcb_list [i].reverse [genome_ind] ? REVERSE_SYMBOL : 
						FORWARD_SYMBOL);
				lcb.put(GAP_FILE_START, gap_file_pos + "");
				lcb.put(STARTS_WITH_GAP, writeGapData (model.getXmfa().getLCB (
						model.getGenomeBySourceIndex(genome_ind), 
						lcb_list [i].starts [genome_ind]),
						genome_ind,	lcb_list [i].starts [genome_ind],
						lcb_list [i].ends [genome_ind]) + "");
				lcb.put(GAP_FILE_END, (gap_file_pos - 1) + "");
			}
		}
	}
	
	protected void convertBackbone (Stash backbone, int genome_ind) {
		for (int i = 0; i < bb_list.length; i++) {
			if (bb_list [i].starts [genome_ind]!= 0)
				backbone.put(i + "", "");
		}
	}
	
	protected void addLocation (Stash stash, long left, long right) {
		stash.put(LEFT_STRING, left + "");
		stash.put(RIGHT_STRING, right + "");
	}
	
	/**
	 * Assumes key lengths fit within an int value; ie, assumes, like biojava,
	 * sequence length is und
	 * @param interval
	 * @param genome
	 * @param start
	 * @return			True if the first key is a gap key
	 */
	protected boolean writeGapData (int interval, int genome, long start, long end) {
		try {
			GISTree tree = model.getXmfa().getGISTree (interval, 
					model.getGenomeBySourceIndex(genome));
			long column = tree.seqPosToColumn(start);
			int index = tree.find(column);
			Key key = tree.getKey(index);
			boolean gap = key instanceof GapKey;
			int k = 0;
			long last_column = tree.seqPosToColumn(end);
			while (column <= last_column) {
				if (k < 1000)
					System.out.println (index + " " + column + " " + key.getLength () + " " + last_column);
				k++;
				if (use_ints)
					gap_out.writeInt ((int) key.getLength());
				else
					gap_out.writeLong(key.getLength());
				column += key.getLength();
				index = tree.find((int)(Math.random () * 5000000));
				key = tree.getKey(index);
				gap_file_pos++;
			}
			System.out.println ("done w/ loop");
			return gap;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
