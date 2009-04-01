package org.gel.mauve.operon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.tree.DefaultMutableTreeNode;

import org.biojava.bio.seq.StrandedFeature;
import org.gel.air.bioj.BioJavaUtils;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.MauveHelperFunctions;

public class Operon implements MauveConstants {
	
	LinkedList <StrandedFeature> genes;
	protected StringBuffer name;
	protected String last_prefix;
	
	protected int seq;
	
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
	
	public Operon (int seq) {
		this (true);
		this.seq = seq;
	}
	
	protected void init () {
		count++;
		genes = new LinkedList <StrandedFeature> ();
		distances = new LinkedList <Integer> ();
		name =  new StringBuffer ();
		last_prefix = "xlckjfalsjfasdasldkjfalsdfj";
	}
	
	protected void connect () {
		if (first == null)
			last = first = this;
		else {
			last.next = this;
			this.prev = last;
			last = this;
		}
	}
	public void addGene (StrandedFeature feat, int distance) {
		genes.add(feat);
		distances.add(distance);
		String gene = BioJavaUtils.getName(feat);
		if (gene == null || gene.length() < 2) {
			System.out.println ("unamed feature added to operon");
			name.append("?");
		}
		else if (gene.startsWith(last_prefix))
			name.append(gene.substring(last_prefix.length()));
		else {
			name.append(gene);
			int ind = 0;
			while (ind < gene.length() && Character.isLowerCase(gene.charAt(ind)))
				ind++;
			last_prefix = gene.substring(0, ind);
		}
	}
	
	public int getStart () {
		return genes.getFirst().getLocation().getMin();
	}
	
	public int getEnd () {
		return genes.getLast().getLocation().getMax();
	}
	
	public boolean forward () {
		return genes.getFirst ().getStrand().equals(StrandedFeature.POSITIVE);
	}
	
	public static void reset () {
		first = last = null;
		count = 0;
	}
	
	public static ArrayList getFirsts (Operon start, int count) {
		Iterator <Operon> itty = new OpIterator (start);
		ArrayList firsts = null;
		if (count > 0)
			firsts = new ArrayList (count);
		else
			firsts = new ArrayList ();
		while (itty.hasNext()) {
			Operon op = itty.next();
			StrandedFeature feat = op.forward() ? op.genes.getFirst() : 
				op.genes.getLast();
			firsts.add(MauveHelperFunctions.getDBXrefID(feat, ASAP));
		}
		return firsts;
	}

	public static class OpIterator implements Iterator <Operon> {
		Operon start;
		Operon current;
		
		public OpIterator (Operon op) {
			current = op;
		}

		public boolean hasNext() {
			return current != null && current != start;
		}

		public Operon next() {
			Operon ret = current;
			if (start == null)
				start = current;
			current = current.next;
			return ret;
		}

		public void remove() {
			System.out.println ("remove not implemented");
		}
		
		
	}
}
