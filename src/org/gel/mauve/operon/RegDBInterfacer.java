package org.gel.mauve.operon;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.biojava.bio.seq.StrandedFeature;
import org.gel.air.bioj.BioJavaUtils;
import org.gel.air.bioj.FeatureRelator;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.operon.Operon.OpIterator;

public class RegDBInterfacer implements MauveConstants {
	
	protected OperonHandler handler;
	protected int num_operons;
	protected Operon first;
	protected int num_ops;
	protected int num_genes;
	protected HashSet <StrandedFeature> f_pos, f_neg;
	protected Hashtable <StrandedFeature, Operon> map;
	protected Hashtable <String, StrandedFeature> ids_to_feats; 
	protected int seq;
	
	
	public RegDBInterfacer (String file, OperonHandler op_handler) {
		handler = op_handler;
		f_pos = new HashSet <StrandedFeature> ();
		f_neg = new HashSet <StrandedFeature> ();
		map = new Hashtable <StrandedFeature, Operon> ();
		ids_to_feats = new Hashtable <String, StrandedFeature> ();
		seq = 0;
		read (file);
	}
	
	public Hashtable <StrandedFeature, Operon> getRegDBOperons () {
		return map;
	}
	
	protected void restrict () {
		FeatureRelator relate = new FeatureRelator ();
		relate.load(FeatureRelator.ORTHOLOGS, 
				"c:\\mauvedata\\operon\\orthos\\ecoli_orthos.txt");
		relate.load(FeatureRelator.PARALOGS, 
				"c:\\mauvedata\\operon\\orthos\\ecoli_paras.txt");
		Operon [] firsts = handler.firsts;
		Hashtable <Integer, String> prefixes = new Hashtable <
			Integer, String> (firsts.length);
		for (int i = 0; i < firsts.length; i++) {
			String id = MauveHelperFunctions.getTruncatedDBXrefID(
					firsts [i].genes.get(0), ASAP);
			prefixes.put(i, id.substring(0, 3));
		}
		relate.setPrefixes (prefixes);
		for (int i = 0; i < firsts.length; i++) {
			System.out.println ("five: " + i);
			int count = 0;
			/*if (i == 1) {
				Operon first = firsts [1];
				first.next.next = first;
				first.prev = first.next;
			}
			else*/ if (i != 10) {
				OpIterator oppy = new OpIterator (firsts [i]);
				while (oppy.hasNext()) {
					boolean keep = false;
					Iterator <StrandedFeature> itty = oppy.next().genes.iterator();
					while (itty.hasNext()) {
						StrandedFeature feat = itty.next();
						/*System.out.println ("key: " + MauveHelperFunctions.getTruncatedDBXrefID(
								feat, ASAP));*/
						String related_id = relate.getFeatureForSequence(
								FeatureRelator.ORTHOLOGS, 
								MauveHelperFunctions.getTruncatedDBXrefID(
										feat, ASAP), seq);
						if (related_id != null && ids_to_feats.containsKey(related_id)) {
							keep = true;
							break;
						}
					}
					/*if (keep)
						System.out.println ("keep!!!!!");*/
					if (!keep) {
						count++;
						oppy.remove();
					}
				}
				firsts [i] = oppy.getStart();
			}
			System.out.println ("removed " + count + " from " + i);
		}
	}
	
	public void performComparison () {
		compare ();
		removeUnique (0);
	}
	
	public void compare () {
		Operon current = first;
		while (current != null) {
			StrandedFeature a = null;
			StrandedFeature b = null;
			Operon predict = null;
			int inc = -1;
			if (current.genes.get(0).getStrand().equals(StrandedFeature.POSITIVE)) {
				inc = 1;
				a = current.genes.getFirst();
			}
			else
				a = current.genes.getLast();
			predict = handler.maps.get (a);
			b = inc == 1 ? predict.genes.getFirst() :
					predict.genes.getLast();
			if (!a.equals (b))
				f_neg.add(a);
			int index = inc == 1 ? 1 : current.genes.size() - 2;
			while (index < current.genes.size() && index > -1) {
				predict = handler.maps.get(current.genes.get (index));
				if (predict != null && current.genes.get(index).equals(inc == 1 ? 
						predict.genes.getFirst() : predict.genes.getLast()))
					f_pos.add(current.genes.get(index));
				index += inc;
			}
			current = current.next;
		}
		System.out.println ("features: " + num_genes);
		System.out.println ("regdb operons: " + num_ops);
		System.out.println ("asap operons: " + handler.counts [0]);
		System.out.println ("false negatives (first gene in experimental operon, "
				+ "but not in predicted): " + f_neg.size() + "/" + num_ops);
		System.out.println ("true positives (first in both experimental"
				+ " and predicted operon): " + (num_ops - f_neg.size()) + "/" 
				+ num_ops);
		//double fp = handler.counts [0] - (num_ops - f_neg.size());
		double fp = f_pos.size();
		System.out.println ("false positives (first in predicted but not "
				+ "experimental): " +  fp + "/" + (num_genes - num_ops));
		System.out.println ("true negatives (not first in either experimental"
				+ " or predicted): " + (num_genes - num_ops - 
				fp) + "/" + (num_genes - num_ops));
		System.out.println ("fpos " + f_pos.size());
	}

	public void read (String file) {
		try {
			Operon.reset();
			BufferedReader in = new BufferedReader (new FileReader (
					file));
			String input = null;
			do {
				input = in.readLine();
			} while (!input.trim().startsWith("(5)"));
			input = in.readLine();
			StringTokenizer toke = null;
			HashSet <String> unique_bnums = new HashSet <String> ();
			while (input != null) {
				if (input.indexOf("experiment") < 0) {
					input = in.readLine();
					continue;
				}
				toke = new StringTokenizer (input);
				toke.nextToken();
				toke.nextToken();
				toke = new StringTokenizer (toke.nextToken(), ",");
				Operon current = null;
				while (toke.hasMoreTokens()) {
					String bnum = toke.nextToken();
					if (!bnum.endsWith("|")) {
						bnum = bnum.substring(bnum.indexOf('|') + 1);
						if (handler.loci.containsKey (bnum)) {
							if (current == null)
								current = new Operon (seq);
							StrandedFeature feat = handler.loci.remove(bnum);
							current.addGene(feat, -1);
							map.put(feat, current);
							ids_to_feats.put(MauveHelperFunctions.
									getTruncatedDBXrefID(feat, ASAP), feat);
							num_genes++;
						}
						else
							unique_bnums.add(bnum);
					}
				}
				if (current != null) {
					Collections.sort(current.genes, 
							BioJavaUtils.FEATURE_START_COMPARATOR);
				}
				input = in.readLine();
			}
			System.out.println ("unique bnums from regdb (" + 
					unique_bnums.size () + "): " + unique_bnums);
			//if more than one genome has bnumbers, count will be wrong
			/*System.out.println ("unique bnums from asap (" + 
					handler.loci.size() + "): " + handler.loci.keySet ());*/
			first = Operon.first;
			num_ops = Operon.count;
			Operon.reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * recalcs operons w/o features not present in regdb operons and
	 * recalcs comparison
	 */
	public void removeUnique (int seq) {
		handler.findOperons (seq, handler.getWriterData (), map.keySet());
		f_pos.clear();
		f_neg.clear();
		compare ();
	}
	
	
}
