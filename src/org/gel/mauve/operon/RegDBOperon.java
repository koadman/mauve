package org.gel.mauve.operon;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.biojava.bio.seq.StrandedFeature;
import org.gel.air.bioj.BioJavaUtils;
import org.gel.air.bioj.FeatureRelator;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.operon.Operon.OpIterator;

public class RegDBOperon implements MauveConstants {
	
	protected OperonHandler handler;
	protected int num_operons;
	protected Operon first;
	protected int num_ops;
	protected int num_genes;
	protected HashSet <StrandedFeature> f_pos, f_neg;
	protected Hashtable <StrandedFeature, Operon> map;
	protected Hashtable <String, StrandedFeature> ids_to_feats; 
	protected int seq;
	
	
	public RegDBOperon (String file, OperonHandler op_handler) {
		handler = op_handler;
		f_pos = new HashSet <StrandedFeature> ();
		f_neg = new HashSet <StrandedFeature> ();
		map = new Hashtable <StrandedFeature, Operon> ();
		ids_to_feats = new Hashtable <String, StrandedFeature> ();
		seq = 0;
		if (op_handler != null)
			readOperons (file, op_handler.loci);
	}
	
	public RegDBOperon (String file) {
		this (file, null);
	}
	
	public Hashtable <StrandedFeature, Operon> getRegDBOperons () {
		return map;
	}
	
	protected void restrictRightRegDB () {
		compare ();
		Operon [] firsts = handler.firsts;
		FeatureRelator relate = handler.getFeatureRelator();
		Hashtable <String, StrandedFeature> temp = ids_to_feats;
		Hashtable <String, StrandedFeature> k12 = new Hashtable <
				String, StrandedFeature> (ids_to_feats);
		for (int i = 0; i < firsts.length; i++) {
			if (i == seq) {
				OpIterator oppy = new OpIterator (firsts [i]);
				int count = 0;
				while (oppy.hasNext()) {
					boolean keep = true;
					Operon op = oppy.next();
					Iterator <StrandedFeature> genes = op.genes.iterator();
					while (keep && genes.hasNext()) {
						StrandedFeature gene = genes.next();
						if (!map.containsKey(gene) || f_pos.contains(gene) ||
								f_neg.contains(gene)) {
							keep = false;
						}
					}
					if (keep && op.genes.size() != map.get(op.genes.get(0)).genes.size())
						keep = false;
					if (!keep) {
						genes = op.genes.iterator();
						while (genes.hasNext ()) {
							k12.keySet().remove(MauveHelperFunctions.
									getTruncatedDBXrefID(
									genes.next (), ASAP));
						}
						oppy.remove();
						handler.counts [i]--;
						count++;
					}
				}
				firsts [i] = oppy.getStart();
				ids_to_feats = k12;
				System.out.println ("after restrict right: " + (handler.counts [seq] - count));
			}
			else {
				restrictRightRegDB (i, relate, firsts);
			}
		}
		ids_to_feats = temp;
	}
	
	protected void restrictAllRegDB () {
		Operon [] firsts = handler.firsts;
		FeatureRelator relate = handler.getFeatureRelator();
		for (int i = 0; i < firsts.length; i++) {
			restrictAllRegDB (i, relate, firsts);
		}
	}
	
	public void restrictAllRegDB (int ind, FeatureRelator relate, 
			Operon [] firsts) {
		OpIterator oppy = new OpIterator (firsts [ind]);
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
				oppy.remove();
				handler.counts [ind]--;
			}
		}
		firsts [ind] = oppy.getStart();

	}
	
	public void restrictRightRegDB (int ind, FeatureRelator relate, 
			Operon [] firsts) {
		OpIterator oppy = new OpIterator (firsts [ind]);
		while (oppy.hasNext()) {
			boolean keep = true;
			Iterator <StrandedFeature> itty = oppy.next().genes.iterator();
			while (itty.hasNext()) {
				StrandedFeature feat = itty.next();
				/*System.out.println ("key: " + MauveHelperFunctions.getTruncatedDBXrefID(
						feat, ASAP));*/
				String related_id = relate.getFeatureForSequence(
						FeatureRelator.ORTHOLOGS, 
						MauveHelperFunctions.getTruncatedDBXrefID(
								feat, ASAP), seq);
				if (related_id == null || !ids_to_feats.containsKey(related_id)) {
					keep = false;
					break;
				}
			}
			/*if (keep)
				System.out.println ("keep!!!!!");*/
			if (!keep) {
				oppy.remove();
				handler.counts [ind]--;
			}
		}
		firsts [ind] = oppy.getStart();

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

	public static String getFirstRealLine (BufferedReader in, int col) {
		String input = null;
		try {
			do {
				input = in.readLine();
			} while (!input.trim().startsWith("(" + col + ")"));
			input = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return input;
	}
	
	public void readOperons (String file, Hashtable <String, 
			StrandedFeature> loci) {
		try {
			Operon.reset();
			BufferedReader in = new BufferedReader (new FileReader (
					file));
			String input = getFirstRealLine (in, 5);
			StringTokenizer toke = null;
			HashSet <String> unique_bnums = new HashSet <String> ();
			Hashtable <String, StrandedFeature> temp = new Hashtable <String, 
					StrandedFeature> ();
			while (input != null) {
				if (input.indexOf("experiment") < 0) {
					input = in.readLine();
					continue;
				}
				toke = new StringTokenizer (input, "\t");
				LinkedList ordered_genes = getOrdered  (toke.nextToken());
				toke.nextToken();
				toke.nextToken ();
				toke = new StringTokenizer (toke.nextToken(), ",");
				Operon current = null;
				Hashtable <String, String> genes = new Hashtable <String, String> ();
				while (toke.hasMoreTokens()) {
					String bnum = toke.nextToken();
					if (!bnum.endsWith("|")) {
						int ind = bnum.indexOf('|');
						if (ind == -1)
							System.out.println ("no |: " + bnum);
						if (loci.containsKey (bnum))
							genes.put(bnum.substring (0, ind), bnum.substring (
									ind + 1));
						else
							unique_bnums.add(bnum);
					}
				}
				if (ordered_genes.size() == genes.size()) {
					Iterator <String> order = ordered_genes.iterator();
					while (order.hasNext()) {
						String gene = order.next ();
						if (genes.containsKey(gene)) {
							String bnum = genes.get(gene);
							if (current == null)
								current = new Operon (seq);
							StrandedFeature feat = loci.remove(bnum);
							temp.put (bnum, feat);
							current.addGene(feat, -1);
							map.put(feat, current);
							ids_to_feats.put(MauveHelperFunctions.
									getTruncatedDBXrefID(feat, ASAP), feat);
							num_genes++;
						}
						else
							System.out.println ("not in genes: "+ gene + " " + genes.keySet());
					}
				}
				if (current != null) {
					if (current.genes.get(0).getStrand() == 
						StrandedFeature.NEGATIVE)
					Collections.reverse(current.genes);
				}
				input = in.readLine();
			}
				
			System.out.println ("unique bnums from regdb (" + 
					unique_bnums.size () + "): " + unique_bnums);
			//if more than one genome has bnumbers, count will be wrong
			/*System.out.println ("unique bnums from asap (" + 
					handler.loci.size() + "): " + handler.loci.keySet ());*/
			loci.putAll (temp);
			first = Operon.first;
			num_ops = Operon.count;
			Operon.reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * should be outmoded; call readOperons but this might have a different
	 * set and should still be in working order.
	 * @param file
	 */
	public void readTUs (String file) {
		try {
			Operon.reset();
			BufferedReader in = new BufferedReader (new FileReader (
					file));
			String input = getFirstRealLine (in, 5);
			StringTokenizer toke = null;
			HashSet <String> unique_bnums = new HashSet <String> ();
			Hashtable <String, StrandedFeature> temp = new Hashtable <String, 
			StrandedFeature> ();
			while (input != null) {
				if (input.indexOf("experiment") < 0) {
					input = in.readLine();
					continue;
				}
				toke = new StringTokenizer (input, "\t");
				LinkedList ordered_genes = getOrdered  (toke.nextToken());
				toke.nextToken();
				toke = new StringTokenizer (toke.nextToken(), ",");
				Operon current = null;
				Hashtable <String, String> genes = new Hashtable <String, String> ();
				while (toke.hasMoreTokens()) {
					String bnum = toke.nextToken();
					if (!bnum.endsWith("|")) {
						int ind = bnum.indexOf('|');
						if (ind == -1)
							System.out.println ("no |: " + bnum);
						if (handler.loci.containsKey (bnum))
							genes.put(bnum.substring (0, ind), bnum.substring (
									ind + 1));
						else
							unique_bnums.add(bnum);
					}
				}
				if (ordered_genes.size() == genes.size()) {
					Iterator <String> order = ordered_genes.iterator();
					while (order.hasNext()) {
						String gene = order.next ();
						if (genes.containsKey(gene)) {
							String bnum = genes.get(gene);
							if (current == null)
								current = new Operon (seq);
							StrandedFeature feat = handler.loci.remove(bnum);
							temp.put(bnum, feat);
							current.addGene(feat, -1);
							map.put(feat, current);
							ids_to_feats.put(MauveHelperFunctions.
									getTruncatedDBXrefID(feat, ASAP), feat);
							num_genes++;
						}
						else
							System.out.println ("not in genes: "+ gene + " " + genes.keySet());
					}
				}
				if (current != null) {
					if (current.genes.get(0).getStrand() == 
						StrandedFeature.NEGATIVE)
					Collections.reverse(current.genes);
				}
				input = in.readLine();
			}
				
			System.out.println ("unique bnums from regdb (" + 
					unique_bnums.size () + "): " + unique_bnums);
			//if more than one genome has bnumbers, count will be wrong
			/*System.out.println ("unique bnums from asap (" + 
					handler.loci.size() + "): " + handler.loci.keySet ());*/
			handler.loci.putAll(temp);
			first = Operon.first;
			num_ops = Operon.count;
			Operon.reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	
	public static LinkedList <String> getOrdered (String name) {
		LinkedList <String> genes = new LinkedList <String> ();
		StringTokenizer toke = new StringTokenizer (name, "-");
		while (toke.hasMoreTokens()) {
			String group = toke.nextToken();
			String prefix = BioJavaUtils.getPrefix(group);
			int ind = prefix.length();
			while (ind < group.length())
				genes.add(prefix + group.charAt(ind++));
		}
		return genes;
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
