package org.gel.mauve.ext.lazy;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.gel.air.ja.stash.Stash;
import org.gel.air.ja.stash.StashComparator;
import org.gel.air.ja.stash.StashList;
import org.gel.air.ja.stash.StashXMLLoader;
import org.gel.mauve.LCB;
import org.gel.mauve.LCBLeftComparator;
import org.gel.mauve.LCBlist;
import org.gel.mauve.Match;
import org.gel.mauve.MauveAlignment;
import org.gel.mauve.ext.MauveInterfacer;
import org.gel.mauve.ext.MauveStoreConstants;
import org.gel.mauve.tree.GISTree;
import org.gel.mauve.tree.GapKey;
import org.gel.mauve.tree.Key;
import org.gel.mauve.tree.SequenceKey;
import org.gel.mauve.tree.TreeStore;

public class StashedAlignment extends MauveAlignment implements MauveStoreConstants {
	
	public static final byte GAP_SYMBOL = (byte) '-';
	
	
	protected StashViewerModel model;
	protected Stash alignment;
	protected DataInputStream key_data;
	protected boolean ints;
	protected int pos;
	protected int iv;

	
	public StashedAlignment (StashViewerModel mod, Stash align) {
		alignment = align;
		model = mod;
		init ();
	}
	
	public void init () {
		model.loadGenomes ();
		lcb_list = new LCB [alignment.getInt (LCB_COUNT)];
		seq_count = model.getSequenceCount();
		intervals = new Match [alignment.getInt(INTERVAL_COUNT)];
		gis_tree = new GISTree [intervals.length][seq_count];
		for (int i = 0; i < seq_count; i++) {
			Stash aligned_gen = model.aligned_genomes.get (i);
			loadGenome (aligned_gen, i);
		}
		makeIslands ();
		for (int i = 0; i < lcb_list.length; i++)
			lcb_list [i].calculateWeight();
		LCBlist.computeLCBAdjacencies(lcb_list, model);
		ts.pruneArrays();
	}
	
	protected void loadGenome (Stash aligned_gen, int i) {
		try {
			pos = 0;
			iv = 0;
			key_data = new DataInputStream (new BufferedInputStream (new 
					FileInputStream (model.loader.getFileByID(aligned_gen.getString(ID) +
							".gaps"))));
			ints = key_data.readBoolean();
			StashList lcb_data = model.getLoader ().populateVector(
					aligned_gen.getHashtable (LCBS), false);
			Collections.sort (lcb_data, ID_COMP);
			for (int j = 0; j < lcb_list.length; j++) {
				Stash lcb = lcb_data.get (j);
				int file_pos = lcb.getInt(GAP_FILE_START);
				int next_file_pos = Integer.MAX_VALUE;
				if (j < lcb_list.length - 1)
					next_file_pos = lcb_data.get (j + 1).getInt (GAP_FILE_START);
				loadInterval (lcb, i, next_file_pos == Integer.MAX_VALUE ?
						Integer.MAX_VALUE : (next_file_pos - file_pos));
				if (lcb_list [j] == null)
					lcb_list [j] = new LCB (intervals [iv - 1], j, seq_count);
				else {
					lcb_list [j].starts [i] = lcb.getLong (LEFT_STRING);
					lcb_list [j].ends [i] = lcb.getLong (RIGHT_STRING);
				}
				lcb_list [j].reverse [i] = lcb.getString (
						REVERSE).equals (REVERSE_SYMBOL);
				intervals [iv - 1].reverse [i] = lcb_list [j].reverse [i];
			}
			key_data.close();
			key_data = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//this sort should not be done this way;call adjacencies first and use that instead
	protected void makeIslands () {
		LCB [][] lcbs = new LCB [model.getSequenceCount ()][lcb_list.length];
		for (int i = 0; i < lcbs.length; i++) {
			System.arraycopy (lcb_list, 0, lcbs [i], 0, lcb_list.length);
			Arrays.sort (lcbs [i], new LCBLeftComparator (model.getGenomeBySourceIndex (i)));
		}
		for (int i = 0; i < lcb_list.length; i++) {
			for (int j = 0; j < model.getSequenceCount (); j++) {
				LCB [] lcb_list = lcbs [j];
				long last = i == 0 ? 0 : lcb_list [i - 1].ends [j];
				long length = lcb_list [i].starts [j] - last - 1;
				if (length != 0) {
					makeInterval (last + 1, length, j, lcb_list [i].starts [j] - 1);
				}
				length = model.getGenomeBySourceIndex(j).getLength();
				long start = lcb_list [i].ends [j] + 1;
				if (i == lcb_list.length - 1 && start < length) {
					makeInterval (start, length - start + 1, j, length);
				}
			}
		}
	}
	
	protected void makeInterval (long start, long length, int genome, long end) {
		try {
			intervals [iv] = new Match (seq_count, true);
			intervals [iv].starts [genome] = start;
			intervals [iv].ends [genome] = end;
			for (int k = 0; k < model.getSequenceCount(); k++)
				gis_tree [iv][k] = makeSingleton (length, k != genome);
			iv++;
			//System.out.println ("e: " + end);
		} catch (Exception e) {
			System.out.println ("before");
			for (int i = 0; i < seq_count; i++) {
				length = 0;
				for (int j = 0; j < intervals.length; j++) {
					length += intervals [j].getLength(model.getGenomeBySourceIndex(i));
				}
				System.out.println ("length: " + length);
			}
			System.out.println ("b");
			e.printStackTrace();
		}

	}
	
	protected GISTree makeSingleton (long length, boolean gap) {
		Key key;
		if (gap) {
			key = new GapKey (length);
		}
		else {
			key = new SequenceKey (0);
			((SequenceKey) key).setLength(length);
		}
		GISTree tree = new GISTree (ts);
		tree.insert(key, GISTree.end);
		return tree;
	}
	
	/**
	 * loads a GISTree from specified keys
	 * 
	 * @param genome	
	 * @param num_keys	number of keys to read; if this is Integer.MAX_VALUE,
	 * 			read the remainder of the file
	 * @throws Exception
	 */
	protected void loadInterval (Stash lcb, int genome, int num_keys) throws Exception {
		if (genome == 0)
			intervals [iv] = new Match (seq_count, true);
		else if (intervals [iv] == null)
			System.out.println ("interval null here");
		gis_tree [iv][genome] = new GISTree (ts);
		long lend = 0;
		intervals [iv].starts [genome] = lcb.getLong(LEFT_STRING);
		for (int i = 0; i < num_keys; i++) {
			long length = 0;
			try {
				length = ints ? key_data.readInt() : key_data.readLong();
			}
			catch (Exception e) {
				if (num_keys < Integer.MAX_VALUE) {
					e.printStackTrace();
					return;
				}
				else
					break;
			}
			pos++;
			Key current;
			if (length < 0)
				current = new GapKey (-length);
			else {
				//TODO seq_pos is only right if intervals are in order.
				current = new SequenceKey (lend);
				lend += length;
				((SequenceKey) current).setLength (length);
			}
			gis_tree [iv][genome].insert(current, GISTree.end);
		}
		/*System.out.println (gis_tree [iv][genome].sequenceLength() + " " +
				(lcb.getLong(RIGHT_STRING) - lcb.getLong(LEFT_STRING) + 1));*/
		intervals [iv].ends [genome] = lcb.getLong(RIGHT_STRING);
		//System.out.println (intervals [iv].ends [genome]);
		iv++;
	}

	protected byte[] readSequenceFromKeys(SequenceKey l_fk, SequenceKey r_fk,
			long left_col, long length, int ivI, int seqI, int l_iter,
			int r_iter) {
		// get the file offsets to read from these
		int l_off = (int) (left_col - gis_tree[ivI][seqI].getStart (l_iter));
		//how much of right key is needed, or how many gaps passed right key are
		//needed if r_off is greater then the right filekey's length.
		int r_off = (int) (left_col + length - 1 - gis_tree[ivI][seqI].getStart (r_iter));
		int index = 0;
		byte [] bps = new byte [(int) length];
		if (l_off < 0) {
			Arrays.fill (bps, index, index - l_off, GAP_SYMBOL);
			index -= l_off;
			l_off = 0;
		}
		Key current = l_fk;
		Key last = null;
		if (r_off > r_fk.getLength()) {
			last = ts.key [gis_tree [ivI][seqI].find(
				r_iter + r_fk.getLength())];
			r_off -= r_fk.getLength();
		}
		else
			last = r_fk;
		while (current != last) {
			if (current instanceof GapKey)
				Arrays.fill(bps, index, index + (int) current.getLength(), GAP_SYMBOL);
			else
				;//System.arraycopy(, index, bps, index, current.getLength());
			index += current.getLength();
		} 
		return null;
	}
	
	public LCB [] getLCBList () {
		return lcb_list;	
	}

}
