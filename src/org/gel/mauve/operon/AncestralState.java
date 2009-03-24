package org.gel.mauve.operon;

import java.util.HashSet;
import java.util.Hashtable;

import javax.swing.tree.DefaultMutableTreeNode;

public class AncestralState {
	
	HashSet <Operon> sames;
	HashSet <DifferentOperon> diffs;
	

	public AncestralState() {
		sames = new HashSet <Operon> ();
		diffs = new HashSet <DifferentOperon> ();
	}
	
	
	
	
	public class DifferentOperon {
		
		Hashtable <String, HashSet <Operon>> diff_feats;
		
		public DifferentOperon () {
			diff_feats = new Hashtable <String, HashSet <Operon>> ();
		}
		
		
	}

}
