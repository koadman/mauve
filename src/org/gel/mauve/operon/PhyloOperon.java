package org.gel.mauve.operon;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.tree.DefaultMutableTreeNode;

import org.gel.air.util.MathUtils;
import org.gel.mauve.analysis.Segment;

public class PhyloOperon {
	
	protected OperonHandler handler;
	protected Hashtable <String, OperonDiff> comps;
	protected HashSet done;
	protected int seq1;
	protected int seq2;
	protected AncestralState parent;
	
	public PhyloOperon (OperonHandler hand) {
		handler = hand;
		init ();
	}
	
	public void init () {
		comps = new Hashtable <String, OperonDiff> ();
	}
	
	protected Object performParsimony (DefaultMutableTreeNode one, 
			DefaultMutableTreeNode two) {
		parent = new AncestralState ();
		done = new HashSet ();
		Iterator <Operon> itty = null;
		if (one.isLeaf()) {
			itty =  new Operon.OpIterator (
					(Operon) one.getUserObject());
		}
		else {
			itty = ((AncestralState) one.getUserObject()).sames.iterator ();
		}
		seq1 = handler.getSeqInd((Operon) 
				one.getFirstLeaf().getUserObject());
		seq2 = handler.getSeqInd((Operon) 
				two.getFirstLeaf().getUserObject());
		while (itty.hasNext())
			compareOperons (itty.next());
		return null;
	}
	
	protected HashSet compareOperons (Operon one) {
		HashSet <String> diffs = new HashSet <String> ();
		Iterator <String> itty = comps.keySet().iterator();
		while (itty.hasNext()) {
			String feat =itty.next ();
			if (!comps.get(feat).isSame(one, two))
				diffs.add(feat);
		}
		return diffs;
	}
	
	public void addDiff (OperonDiff diff) {
		comps.put(diff.getFeature(), diff);
	}

}
