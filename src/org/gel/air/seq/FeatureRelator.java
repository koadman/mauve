package org.gel.air.seq;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class FeatureRelator {
	
	protected Hashtable <String, Hashtable <String, HashSet>> relations;
	public static final String ORTHOLOGS = "orthologs";
	public static final String PARALOGS = "paralogs";
	public static final String ALL_LOGS = "both";
	
	
	public FeatureRelator () {
		init ();
	}
	
	public void init () {
		relations = new Hashtable <String, Hashtable <String, HashSet>> ();
	}
	
	public void load (String type, String file) {
		try {
			Hashtable <String, HashSet> data = new Hashtable <
					String, HashSet> ();
			BufferedReader in = new BufferedReader (new FileReader (file));
			String input = in.readLine();
			while (input != null) {
				HashSet genes = new HashSet ();
				StringTokenizer toke = new StringTokenizer (input);
				while (toke.hasMoreElements()) {
					String gene = toke.nextToken();
					data.put(gene, genes);
					genes.add(gene);
				}
				input = in.readLine();
			}
			in.close();
			relations.put(type, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main (String [] args) {
		FeatureRelator relate = new FeatureRelator ();
		relate.load(args[1], args[0]);
		System.out.println (relate.relations.get(args[1]).size());
	}

}
