package org.gel.mauve.operon;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.gel.air.util.MathUtils;
import org.gel.mauve.analysis.Segment;
import org.gel.mauve.analysis.SegmentComparator;
import org.gel.mauve.operon.AncestralState.Difference;
import org.gel.mauve.operon.AncestralState.DifferentOperon;

public class PhyloOperon {
	
	protected OperonHandler handler;
	protected Hashtable <String, OperonDiff> comps;
	protected Hashtable <Operon, Hashtable <String, HashSet <Operon>>> sames;
	protected HashSet <Operon> done;
	protected AncestralState parent;
	protected Difference op_diff;
	protected int op_diff_seq;
	protected Operon two;
	
	public PhyloOperon (OperonHandler hand) {
		handler = hand;
		init ();
	}
	
	public void init () {
		comps = new Hashtable <String, OperonDiff> ();
		sames = new Hashtable <Operon, Hashtable <String, HashSet <
				Operon>>> ();
		addDiff (new OperonDiff.MultiplicityDiff (
				OperonDiff.MULTIPLICITY_FEAT, this));
	}
	
	protected void pruneTree (DefaultMutableTreeNode node) {
		if (!node.isRoot()) {
			AncestralState current = (AncestralState) node.getUserObject();
			AncestralState parent = (AncestralState) ((DefaultMutableTreeNode) 
					node.getParent ()).getUserObject();
			HashSet other_seqs = ((AncestralState) getOtherChild (
					node).getUserObject ()).seqs;
			
			Iterator <Operon> itty = new LinkedList (
					current.differences.keySet()).iterator();
			while (itty.hasNext()) {
				Operon op = itty.next();
				Hashtable <String, DifferentOperon> diffs = 
					current.differences.get(op);
				Iterator <String> feats = new LinkedList (
						diffs.keySet()).iterator();
				while (feats.hasNext()) {
					String feat = feats.next();
					if (parent.definitelyPresent (op, feat, other_seqs, sames))
						current.makePresent (op, feat);
					else if (!parent.isOption(op, feat, other_seqs, sames)) {
						if (op.getName().contains("ydjS"))
							System.out.println ("removing it");
						diffs.remove(feat);
						if (diffs.size () == 0)
							current.differences.remove (op);
					}
				}
			}
			
			itty = new LinkedList (current.unclears.keySet ()).iterator ();
			while (itty.hasNext()) {
				Operon op = itty.next();
				HashSet <String> diffs = current.unclears.get(op);
				Iterator <String> feats = new LinkedList (diffs).iterator();
				while (feats.hasNext()) {
					String feat = feats.next();
					if (parent.definitelyPresent (op, feat, other_seqs, sames)) {
						diffs.remove (feat);
						if (diffs.size () == 0)
							current.unclears.remove(op);
						else if (!parent.isOption(op, feat, other_seqs, sames)) {
							diffs.remove(feat);
							if (diffs.size () == 0)
								current.unclears.remove (op);
						}
					}
				}
			}
			
		}
		for (int i = 0; i < 2; i++) {
			DefaultMutableTreeNode kid = (DefaultMutableTreeNode) node.getChildAt (i);
			if (!kid.isLeaf())
				pruneTree (kid);
		}
	}
	
	
	protected AncestralState buildAncestor (DefaultMutableTreeNode one, 
			DefaultMutableTreeNode two) {
		parent = new AncestralState ();
		HashSet <Integer> seqs = new HashSet <Integer> ();
		if (one.isLeaf())
			seqs.add(((Operon) one.getUserObject()).seq);
		else
			seqs.addAll(((AncestralState) one.getUserObject()).seqs);
		if (two.isLeaf())
			seqs.add(((Operon) two.getUserObject()).seq);
		else
			seqs.addAll(((AncestralState) two.getUserObject()).seqs);
		parent.seqs = seqs;
		
		done = new HashSet ();
		Iterator <Operon> itty = null;
		itty = one.isLeaf () ? new Operon.OpIterator ((Operon) one.getUserObject()) :
				((AncestralState) one.getUserObject()).sames.keySet ().iterator ();
		DefaultMutableTreeNode seq1 = one;
		DefaultMutableTreeNode seq2 = two;
		while (itty.hasNext()) {
			Operon op = itty.next();
			compareOperons (op, seq2, comps, true, false);
			done.add (op);
		}
		System.out.println ("done1: " + done.size());
		if (!one.isLeaf()) {
			itty = ((AncestralState) one.getUserObject()).differences.keySet (
					).iterator ();
			while (itty.hasNext()) {
				Operon op = itty.next();
				if (!done.contains (op)) {
					compareOperons (op, seq2, comps, true, true);
				}
				done.add (op);
			}
			System.out.println ("done3: " + done.size());
		}
		seq1 = two;
		seq2 = one;
		itty = two.isLeaf () ? new Operon.OpIterator ((Operon) two.getUserObject()) :
			((AncestralState) two.getUserObject()).sames.keySet ().iterator ();
		while (itty.hasNext()) {
			Operon op = itty.next();
			if (!done.contains (op))
				compareOperons (op, seq2, comps, true, false);
			done.add (op);
		}
		System.out.println ("done2: " + done.size());
	
		if (!two.isLeaf()) {
			itty = ((AncestralState) two.getUserObject()).differences.keySet (
					).iterator ();
			while (itty.hasNext()) {
				Operon op = itty.next();
				if (!done.contains (op))
					compareOperons (op, seq2, comps, true, true);
				done.add (op);
			}
			System.out.println ("done4: " + done.size());
		}
		return parent;
	}
	
	protected void compareOperons (Operon one, DefaultMutableTreeNode right,
			Hashtable <String, OperonDiff> comps, boolean top, boolean one_diff) {
		/*if (parent.ops_to_feats.containsKey(one))
			return;*/
		boolean intersect = false;
		HashSet <String> diffs = new HashSet <String> ();
		Iterator <String> itty = comps.keySet().iterator();
		Hashtable <String, OperonDiff> sub_comps =
				new Hashtable <String, OperonDiff> (comps); 
		while (itty.hasNext()) {
			sub_comps.clear ();
			DefaultMutableTreeNode cur_leaf = right.getFirstLeaf ();
			int seq = ((Operon) cur_leaf.getUserObject ()).seq;
			String feat = itty.next ();
			OperonDiff diff = comps.get(feat);
			sub_comps.put(feat, diff);
			op_diff = null;
			op_diff_seq = -1;
			//two = null;
			boolean same = diff.isSame(one, seq);
			if (one.getName ().contains("ydjS"))
				System.out.println (one.seq + " " + one.getName () +
						" " + seq + " " + same);
			if (!same) {
				op_diff = diff.getLastDifference();
				op_diff_seq = seq;
			}
			outer: do {
				while (!same && cur_leaf != right) {
					if (cur_leaf == cur_leaf.getParent().getChildAt(0)) {
						cur_leaf = ((DefaultMutableTreeNode) cur_leaf.getParent(
						).getChildAt(1)).getFirstLeaf();
						seq = ((Operon) cur_leaf.getUserObject()).seq;
						same = diff.isSame(one, seq);
						if (one.getName ().contains("ydjS"))
							System.out.println (one.seq + " " + one.getName () +
									" " + seq + " " + same);
					}
					else
						cur_leaf = (DefaultMutableTreeNode) cur_leaf.getParent ();
				}
				
				if (same) {
					two = (Operon) diff.getRelatedOperons(
							one, seq).iterator().next();
					while (cur_leaf != right) {
						cur_leaf = (DefaultMutableTreeNode) cur_leaf.getParent();
						AncestralState state = (AncestralState) cur_leaf.getUserObject();
						if (!state.isOption(two, feat)) {
							if (cur_leaf != right) {
								cur_leaf = (DefaultMutableTreeNode) cur_leaf.getParent(
								).getChildAt(1);
								compareOperons (one, cur_leaf, sub_comps, false, one_diff);
								seq = ((OperonDiff) diff).seq;
								System.out.println ("no no no!!! " + one.seq + " " + two.seq + " " + two.getName ());
								if (diff.isSame(one, seq)) {
									two = diff.getLastRelatedOperons().iterator (
									).next ();
								}
								else {
									op_diff = diff.getLastDifference();
									op_diff_seq = seq;
									same = false;
									continue outer;
								}
							}
						}
					}
				}
			} while (cur_leaf != right);

			
			if (top) {
				boolean unclear = one_diff;
				DefaultMutableTreeNode one_node = getOtherChild (right);
				if (!one_node.isLeaf() && ((AncestralState) 
						one_node.getUserObject()).inUnclears (one, feat))
					unclear = true; 
				if (same && (right.isLeaf() || ((AncestralState) right.getUserObject(
							)).isOption(two, feat))) {
					parent.addSame (one, feat);
					addSame (one, two, feat);
					done.add (two);
					if (unclear && !right.isLeaf() && ((AncestralState) right.getUserObject(
							)).isDifference(two, feat)) {
						parent.addUnclear(one, feat);
					}
				}
				if (one.getName().contains("ydjS"))
					System.out.println("same " + same + " unclear " +unclear);
				if (!same && !unclear) {
					if (one.getName().contains("ydjS"))
						System.out.println("it should really be there");
					
					parent.addDifference (one, op_diff_seq, feat, op_diff);
				}
			}

		}
	}
	
	public DefaultMutableTreeNode getOtherChild (DefaultMutableTreeNode node){
		DefaultMutableTreeNode one_node = (DefaultMutableTreeNode) 
		node.getParent().getChildAt(0);
		if (one_node == node) {
			one_node = (DefaultMutableTreeNode) 
			node.getParent().getChildAt(1);
		}
		return one_node;
	}
	
	/*if (top) {
	if (same && (right.isLeaf() || ((AncestralState) right.getUserObject(
			)).isOption(two, feat))) {
		parent.addSame (one, feat);
		addSame (one, two, feat);
		done.add (two);
		if (one_diff && !right.isLeaf() && ((AncestralState) right.getUserObject(
				)).isDifference(two, feat)) {
			parent.addUnclear(one, feat);
		}
	}
	if (!same && !one_diff) {
		parent.addDifference (one, op_diff_seq, feat, op_diff);
	}
}*/
	
	
	protected void addSame (Operon one, Operon two, String feat) {
		Hashtable <String, HashSet <Operon>> holder = sames.get(one);
		HashSet <Operon> one_same = null;
		if (holder == null) {
			holder = new Hashtable <String, HashSet <Operon>> ();
			sames.put (one, holder);
		}
		else
			one_same = holder.get (feat);
		Hashtable <String, HashSet <Operon>> holder2 = sames.get(two);
		HashSet <Operon> two_same = null;
		if (holder2 == null) {
			holder2 = new Hashtable <String, HashSet <Operon>> ();
			sames.put (two, holder);
		}
		else
			two_same = holder2.get (feat);
		if (one_same == null) {
			if (two_same == null) {
				one_same = new HashSet <Operon> ();
				holder2.put (feat, one_same);
				one_same.add (two);
			}
			else {
				one_same = two_same;
			}
			holder.put(feat, one_same);
			one_same.add (one);
		}
		else {
			if (two_same != null)
				one_same.addAll (two_same);
			holder2.put (feat, one_same);
			one_same.add (two);
		}
	}

	/*protected void addSame (Operon one, Operon two, String feat) {
		Hashtable <String, HashSet <Operon>> holder = sames.get(one);
		if (holder == null) {
			holder = sames.get (two);
			if (holder == null) {
				holder = new Hashtable <String, HashSet <Operon>> ();
				sames.put(two, holder);
			}
			sames.put(one, holder);
		}
		else {
			Hashtable <String, HashSet <Operon>> holder2 = sames.get(two);
			if (holder2 != null) {
				
			}
		}
		HashSet <Operon> mother_same = holder.get(feat);
		if (mother_same == null) {
			mother_same = new HashSet <Operon> ();
			holder.put(feat, mother_same);
		}
		mother_same.add(one);
		mother_same.add(two);	
	}*/
	
	public void addDiff (OperonDiff diff) {
		comps.put(diff.getFeature(), diff);
	}
	
	//code that currently has no real home
	public void compareGenes (Operon one) {
		
	}
	
}