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

public class RegDBInterfacer {
	
	protected OperonHandler handler;
	protected int num_operons;
	protected Operon first;
	protected int num_ops;
	protected int num_genes;
	protected HashSet <StrandedFeature> f_pos, f_neg;
	protected Hashtable <StrandedFeature, Operon> map;
	
	
	public RegDBInterfacer (String file, OperonHandler op_handler) {
		handler = op_handler;
		f_pos = new HashSet <StrandedFeature> ();
		f_neg = new HashSet <StrandedFeature> ();
		map = new Hashtable <StrandedFeature, Operon> ();
		read (file);
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
								current = new Operon ();
							current.addGene(handler.loci.remove(bnum), -1);
							map.put(current.genes.getLast(), current);
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
			System.out.println ("unique bnums from asap (" + 
					handler.loci.size() + "): " + handler.loci.keySet ());
			first = Operon.first;
			num_ops = Operon.count;
			Operon.reset();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void removeUnique (int seq) {
		handler.findOperons (seq, handler.getWriterData (), map.keySet());
		f_pos.clear();
		f_neg.clear();
		compare ();
	}
	
	
}
