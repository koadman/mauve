package org.gel.mauve.operon;

import java.util.HashSet;
import java.util.Hashtable;

import javax.swing.tree.DefaultMutableTreeNode;

public class AncestralState {
	
	//HashSet <DifferentOperon> diffs;
	/** 
	 *  before PhyloOperon.pruneTree (), contains all operons are different
	 *  between children
	 *  
	 *  after PhyloOperon.pruneTree (), will contain operons absent here that
	 *  are present in exactly one immediate child
	 */
	protected Hashtable <Operon, Hashtable <String, DifferentOperon>> differences;
	
	/** 
	 *  after PhyloOperon.pruneTree (), will contain operons present here that
	 *  are missing in exactly one immediate child
	 */
	protected Hashtable <Operon, Hashtable <String, DifferentOperon>> differences2;
	
	protected Hashtable <Operon, HashSet <String>> sames;
	
	protected Hashtable <Operon, HashSet <String>> unclears;
	
	protected HashSet <Integer> seqs;

	public AncestralState() {
		//diffs = new HashSet <DifferentOperon> ();
		differences = new Hashtable <Operon, Hashtable <
				String, DifferentOperon>> ();
		differences2 = new Hashtable <Operon, Hashtable <
		String, DifferentOperon>> ();
		sames = new Hashtable <Operon, HashSet <String>> ();
		unclears = new Hashtable <Operon, HashSet <String>> ();
		//seqs = new HashSet <Integer> ();
	}
	
	public void makePresent (Operon op, String feat) {
		Hashtable <String, DifferentOperon> diffs = differences2.get(op);
		if (diffs == null) {
			diffs = new Hashtable <String, DifferentOperon> ();
			differences2.put(op, diffs);
		}
		Hashtable <String, DifferentOperon> old = differences.get(op);
		diffs.put(feat, old.remove(feat));
		if (old.size() == 0)
			differences.remove(op);
	}
	
	public void addDifference (Operon one, int seq2, String feat, Difference diff) {
		Hashtable <String, DifferentOperon> holder = differences.get(one);
		if (holder == null) {
			holder = new Hashtable <String, DifferentOperon> ();
			differences.put(one, holder);
		}
		DifferentOperon mother_diff = holder.get(feat);
		if (mother_diff == null) {
			mother_diff = new DifferentOperon (one);
			holder.put(feat, mother_diff);
		}
		mother_diff.diffs.put(seq2, diff);
	}
	
	public void addSame (Operon one, String feat) {
		HashSet <String> holder = sames.get(one);
		if (holder == null) {
			holder = new HashSet <String> ();
			sames.put(one, holder);
		}
		holder.add(feat);
	}
	
	public void addUnclear (Operon one, String feat) {
		HashSet <String> holder = unclears.get(one);
		if (holder == null) {
			holder = new HashSet <String> ();
			unclears.put(one, holder);
		}
		holder.add(feat);
	}
	
	public boolean isOption (Operon op, String feat) {
		boolean option = inSames (op, feat);
		if (!option && differences.containsKey(op) && differences.get (
				op).containsKey(feat))
			option = true;
		return option;
	}
	
	public boolean inSames (Operon op, String feat) {
		return sames.containsKey(op) && sames.get(op).contains(feat);
	}
	
	public boolean definitelyPresent (Operon op, String feat) {
		return sames.containsKey(op) && sames.get(op).contains(feat) &&
				!(unclears.containsKey(op) && unclears.get(op).contains(feat));
	}
	
	public boolean inUnclears (Operon op, String feat) {
		return unclears.containsKey(op) && unclears.get(op).contains(feat);
	}
	
	public boolean isDifference (Operon op, String feat) {
		boolean diff = differences.containsKey (op) && differences.get(
				op).containsKey (feat);
		if (!diff)
			diff = inUnclears (op, feat);
		return diff;
	}
	
	public String toString () {
		return seqs.toString ();
		//return "sames: " + sames.size() + "  diffs: " + differences.size () + "  unclear: " + unclears.size ();
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
		
		public String toString () {
			return difference;
		}
	}

}
