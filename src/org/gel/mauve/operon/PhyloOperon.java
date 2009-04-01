package org.gel.mauve.operon;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.gel.air.util.MathUtils;
import org.gel.mauve.analysis.Segment;
import org.gel.mauve.analysis.SegmentComparator;

public class PhyloOperon {
	
	protected OperonHandler handler;
	protected Hashtable <String, OperonDiff> comps;
	protected Hashtable <Operon, Hashtable <String, HashSet <Operon>>> sames;
	protected HashSet <Operon> done;
	protected DefaultMutableTreeNode seq1;
	protected DefaultMutableTreeNode seq2;
	protected AncestralState parent;

	
	public PhyloOperon (OperonHandler hand) {
		handler = hand;
		init ();
	}
	
	public void init () {
		comps = new Hashtable <String, OperonDiff> ();
		sames = new Hashtable <Operon, Hashtable <String, HashSet <
				Operon>>> ();
	}
	
	protected Object performParsimony (DefaultMutableTreeNode one, 
			DefaultMutableTreeNode two) {
		parent = new AncestralState ();
		done = new HashSet ();
		Iterator <Operon> itty = null;
		itty = one.isLeaf () ? new Operon.OpIterator ((Operon) one.getUserObject()) :
				((AncestralState) one.getUserObject()).sames.keySet ().iterator ();
		seq1 = one;
		seq2 = two;
		while (itty.hasNext())
			compareOperons (itty.next(), seq2, comps);
		itty = two.isLeaf () ? new Operon.OpIterator ((Operon) two.getUserObject()) :
			((AncestralState) two.getUserObject()).sames.keySet ().iterator ();
		seq1 = two;
		seq2 = one;
		while (itty.hasNext()) {
			Operon op = itty.next();
			if (!done.contains (op))
				compareOperons (op, seq2, comps);
		}
		return parent;
	}
	
	protected void compareOperons (Operon one, DefaultMutableTreeNode right,
			Hashtable <String, OperonDiff> comps) {
		/*if (parent.ops_to_feats.containsKey(one))
			return;*/
		DefaultMutableTreeNode cur_leaf = right.getFirstLeaf ();
		int seq = ((Operon) cur_leaf.getUserObject ()).seq;
		HashSet <String> diffs = new HashSet <String> ();
		Iterator <String> itty = comps.keySet().iterator();
		Hashtable <String, OperonDiff> sub_comps =
				new Hashtable <String, OperonDiff> (comps); 
		while (itty.hasNext()) {
			String feat = itty.next ();
			OperonDiff diff = comps.get(feat);
			if (!diff.isSame(one, seq)) {
				parent.addDifference (one, seq, feat, diff.getLastDifference ());
			}
			else {
				sub_comps.remove (feat);
				parent.addSame (one, feat);
				addSame (one, (Operon) diff.getRelatedOperons(
						one, seq).iterator().next(), feat);
			}
		}
		// this next loop is probably wrong
		while (cur_leaf != right) {
			cur_leaf = (DefaultMutableTreeNode) cur_leaf.getParent ();
			if (sub_comps.size () > 0) {
				compareOperons (one, (DefaultMutableTreeNode) cur_leaf.getChildAt(
						1), sub_comps);
			}
		}
	}
	
	protected void addSame (Operon one, Operon two, String feat) {
		Hashtable <String, HashSet <Operon>> holder = sames.get(one);
		if (holder == null) {
			holder = sames.get (two);
			if (holder == null) {
				holder = new Hashtable <String, HashSet <Operon>> ();
				sames.put(two, holder);
			}
			sames.put(one, holder);
		}
		HashSet <Operon> mother_same = holder.get(feat);
		if (mother_same == null) {
			mother_same = new HashSet <Operon> ();
			holder.put(feat, mother_same);
		}
		mother_same.add(two);	
	}
	
	public void addDiff (OperonDiff diff) {
		comps.put(diff.getFeature(), diff);
	}
	
	//code that currently has no real home
	public void compareGenes (Operon one) {
		
	}

}
