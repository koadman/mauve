package org.gel.mauve.ext.lazy;

import java.util.Arrays;

import org.gel.air.ja.stash.Stash;
import org.gel.air.ja.stash.StashLoader;
import org.gel.mauve.MauveAlignment;
import org.gel.mauve.ext.MauveInterfacer;
import org.gel.mauve.tree.GapKey;
import org.gel.mauve.tree.Key;
import org.gel.mauve.tree.SequenceKey;

public class StashedAlignment extends MauveAlignment {
	
	public static final byte GAP_SYMBOL = (byte) '-';
	protected Stash alignment;
	public StashedAlignment (String alignment_id) {
		init (alignment_id);
	}
	
	public void init (String alignment_id) {
		StashLoader loader = MauveInterfacer.getStashDir();
		alignment = 
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
				System.arraycopy(, index, bps, index, current.getLength());
			index += current.getLength();
		} 
		return null;
	}

}
