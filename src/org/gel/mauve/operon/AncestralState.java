package org.gel.mauve.operon;

import java.util.HashSet;
import java.util.Hashtable;

import javax.swing.tree.DefaultMutableTreeNode;

public class AncestralState {
	
	//HashSet <DifferentOperon> diffs;
	protected Hashtable <Operon, Hashtable <String, DifferentOperon>> ops_to_feats;
	
	protected Hashtable <Operon, HashSet <String>> sames;

	public AncestralState() {
		//diffs = new HashSet <DifferentOperon> ();
		ops_to_feats = new Hashtable <Operon, Hashtable <String, DifferentOperon>> ();
		sames = new Hashtable <Operon, HashSet <String>> ();
	}
	
	public void addDifference (Operon one, int seq2, String feat, Difference diff) {
		Hashtable <String, DifferentOperon> holder = ops_to_feats.get(feat);
		if (holder == null) {
			holder = new Hashtable <String, DifferentOperon> ();
			ops_to_feats.put(one, holder);
		}
		DifferentOperon mother_diff = holder.get(feat);
		if (mother_diff == null) {
			mother_diff = new DifferentOperon (one);
			holder.put(feat, mother_diff);
		}
		mother_diff.diffs.put(seq2, diff);
	}
	
	public void addSame (Operon one, String feat) {
		HashSet <String> holder = sames.get(feat);
		if (holder == null) {
			holder = new HashSet <String> ();
			sames.put(one, holder);
		}
		holder.add(feat);
	}
	
	
	public static class DifferentOperon {
		
		protected Hashtable <Integer, Difference> diffs;
		protected Operon ref_operon;
		//in containing hashtable
		//protected String feature;
		
		public DifferentOperon (Operon ref) {
			diffs = new Hashtable <Integer, Difference> ();
			ref_operon = ref;
			//feature = feat;
		}
				
	}
	
	public static class Difference {
		
		protected String difference;
		
		public Difference (String diff) {
			difference = diff;
		}
	}

}
