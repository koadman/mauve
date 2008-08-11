package org.gel.mauve.operon;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;

import javax.swing.tree.DefaultMutableTreeNode;

import org.biojava.bio.seq.StrandedFeature;

public class Operon {
	
	LinkedList <StrandedFeature> genes;
	
	//contains distance to previous gene
	LinkedList <Integer> distances;
	
	//for operon list
	protected Operon next;
	protected Operon prev;
	protected static Operon first;
	protected static Operon last;
	protected static int count;

	public Operon (boolean connect) {
		init ();
		if (connect) {
			connect ();
		}
	}
	
	public Operon () {
		this (true);
	}
	
	protected void init () {
		genes = new LinkedList <StrandedFeature> ();
		distances = new LinkedList <Integer> ();
	}
	
	protected void connect () {
		if (first == null)
			last = first = this;
		else {
			last.next = this;
			this.prev = last;
			last = this;
		}
		count++;
	}
	public void addGene (StrandedFeature gene, int distance) {
		genes.add(gene);
		distances.add(distance);
	}
	
	public int getStart () {
		return genes.getFirst().getLocation().getMin();
	}
	
	public int getEnd () {
		return genes.getLast().getLocation().getMax();
	}
	
	public static void reset () {
		first = last = null;
		count = 0;
		
	}

}
