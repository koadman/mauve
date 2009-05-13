package org.gel.air.bioj;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.StrandedFeature;
import org.gel.mauve.MauveHelperFunctions;

public class FeatureRelator {
	
	protected Hashtable <String, Hashtable <String, HashSet>> relations;
	public static final String ORTHOLOGS = "orthologs";
	public static final String PARALOGS = "paralogs";
	public static final String ALL_LOGS = "both";
	//contains prefixes by identifying int
	protected Hashtable <Integer, String> prefixes;

	
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
	
	public void setPrefixes (Hashtable <Integer, String> prefs) {
		prefixes = prefs;
	}
	
	public boolean hasGeneName (StrandedFeature feat, Hashtable <String, 
			StrandedFeature> ids, String relation) {
		try {
			String id = MauveHelperFunctions.getTruncatedDBXrefID(feat, "ASAP");
		HashSet feats = relation == null ? new HashSet <String> () : 
			relations.get(relation).get(id);
		if (!(relation == null) && relations.containsKey (relation)) {
			if (feats != null) {
				Iterator <String> itty = feats.iterator();
				while (itty.hasNext()) {
					StrandedFeature log = ids.get(itty.next());
					if (log.getAnnotation() != null && log.getAnnotation(
							).containsProperty("gene"))
						return true;
				}
			}
		}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * gets feature for specific biojava sequence from 
	 * @param seq
	 * @return
	 */
	public String getFeatureForSequence (String relation, 
			String comp_id, int seq) {
		if (relations.get(relation).containsKey(comp_id)) {
			Iterator <String> itty = relations.get(relation).get(
					comp_id).iterator();
			String prefix = prefixes.get(seq);
			while (itty.hasNext()) {
				String id = itty.next();
				if (id.startsWith(prefix))
					return id;
			}
		}
		return null;
	}
	
	public static void main (String [] args) {
		FeatureRelator relate = new FeatureRelator ();
		relate.load(args[1], args[0]);
		System.out.println (relate.relations.get(args[1]).size());
	}

}
