package org.gel.mauve.operon;

import java.util.Hashtable;

import javax.swing.tree.DefaultMutableTreeNode;

public class InternalOperon extends Operon {
	
	Hashtable <DefaultMutableTreeNode, InternalOperon> alternatives;
	Hashtable <DefaultMutableTreeNode, Operon> sames;

	public InternalOperon() {
		alternatives = new Hashtable <DefaultMutableTreeNode, InternalOperon> ();
		connect ();
	}

}
