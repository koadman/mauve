package org.gel.mauve.ext.io;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.StringTokenizer;

import org.gel.air.ja.stash.Stash;
import org.gel.air.ja.stash.StashXMLLoader;
import org.gel.mauve.LCB;
import org.gel.mauve.Match;
import org.gel.mauve.XMFAAlignment;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.backbone.Backbone;
import org.gel.mauve.backbone.BackboneListBuilder;
import org.gel.mauve.ext.MauveInterfacer;
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
	protected StashXMLLoader loader;

	public AlignmentConverter (XmfaViewerModel mod, StashXMLLoader load) {
		loader = load;
		model = mod;
		convertToStash ();
		loader.writeXMLFile (align, loader.getFileForStash (align.getString (ID)));
	}
	
	protected void convertToStash () {
		align = new Stash (ALIGNMENT_CLASS);
		align.put(URI, model.getSrc ().toURI().toString());
		lcb_list = model.getFullLcbList();
		bb_list = model.getBackboneList().getBackboneArray();
		align.put(LCB_COUNT, lcb_list.length + "");
		writeBbcolsFile (align.getString(ID));
		//SegmentWriter.getSegmentWriter("c:\\lcbs.txt", model);
		Stash genome_list = loader.makeList (ALIGNED_GENOME_CLASS, GENOMES);
		align.put(GENOMES, genome_list);
		align.put(NAME, model.getSrc().getName());
		align.put(GENOME_COUNT, model.getSequenceCount() + "");
		for (int i = 0; i < model.getSequenceCount(); i++) {
			Stash genome = new Stash (ALIGNED_GENOME_CLASS);
			genome_list.put(genome.get (ID), genome.get (ID));
			genome.put(GENOME, new GbkConverter (
					model.getGenomeBySourceIndex(i), ASAP, loader).genome.get(ID));
			if (model.getGenomeBySourceIndex(i).getLength() < Integer.MAX_VALUE)
				use_ints = true;
			try {
				gap_out = new DataOutputStream (new BufferedOutputStream (
						new FileOutputStream (loader.getAssociatedFilePath (
								genome.getString (ID), ".gaps"))));
				gap_file_pos = 0;
				gap_out.writeBoolean(use_ints);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Stash lcbs = loader.makeList (GENOME_LABEL_CLASS, LCBS);
			genome.put(LCBS, lcbs);
			convertLCBs (lcbs, i);
			/*Stash backbone = new Stash (LIST_CLASS, BACKBONE_IDS);
			genome.put(BACKBONE, backbone);
			convertBackbone (backbone, i);*/
			MauveInterfacer.writeSimilarity(model, genome, i);
			try {
				gap_out.close();
				loader.writeXMLFile(genome, loader.getFileForStash (
						genome.getString (ID)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void writeBbcolsFile (String file) {
		try {
			BufferedReader in = new BufferedReader (new FileReader (
					BackboneListBuilder.getBbFile(model, ((XMFAAlignment) 
							model.getXmfa()))));
			PrintStream out = new PrintStream (new BufferedOutputStream (
					new FileOutputStream (loader.getAssociatedFilePath (file, "bbcols"))));
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
		int next_interval = 0;
		for (int i = 0; i < lcb_list.length; i++) {
			if (lcb_list [i].starts [genome_ind] != 0) {
				Stash lcb = new Stash (GENOME_LABEL_CLASS, 
						GENOME_LABEL_CLASS + KEY_SEPARATOR + lcb_list [i].id);
				addLocation (lcb, lcb_list [i].starts [genome_ind],
						lcb_list [i].ends [genome_ind]);
				lcb.put(REVERSE, lcb_list [i].reverse [genome_ind] ? REVERSE_SYMBOL : 
						FORWARD_SYMBOL);
				int interval = model.getXmfa().getLCB (model.getGenomeBySourceIndex(
						genome_ind), lcb_list [i].starts [genome_ind]);
				lcb.put(GAP_FILE_START, gap_file_pos + "");
				writeGapData (next_interval++, genome_ind);
				//lcb.put(GAP_FILE_END, (gap_file_pos - 1) + "");
				//System.out.println (lcb.get(GAP_FILE_START) + " " + lcb.get(GAP_FILE_END));
				lcbs.put(lcb.get(ID), lcb);
			}
		}
		align.put(INTERVAL_COUNT, model.getXmfa().getIntervalCount() + "");
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
	 * Stores key lengths as ints if they'll fit, otherwise as longs, and stores gap
	 * keys as negative numbers so they can be told apart.
	 * @param interval
	 * @param genome
	 * @param start
	 * @return			True if the first key is a gap key
	 */
	protected void writeGapData (int interval, int genome) {
		try {
			GISTree tree = model.getXmfa().getGISTree (interval, 
					model.getGenomeBySourceIndex(genome));
			long column = 0;
			int index = tree.find(column);
			Key key = tree.getKey(index);
			int k = 0;
			//assumes that coordinates start at 0 and go to length - 1
			while (column < tree.length()) {
				/*if (k < 1000)
					System.out.println (index + " " + column + " " + key.getLength () + " " + last_column);
				k++;*/
				long length = key instanceof GapKey ? -key.getLength() : key.getLength();
				if (use_ints)
					gap_out.writeInt ((int) length);
				else
					gap_out.writeLong(length);
				column += key.getLength();
				index = tree.find(column);
				key = tree.getKey(index);
				gap_file_pos++;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
