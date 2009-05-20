package org.gel.mauve.operon.pred;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.biojava.bio.seq.StrandedFeature;
import org.gel.mauve.operon.RegDBOperon;
import org.gel.mauve.operon.pred.PredictionHandler.IGD;
import org.gel.mauve.operon.pred.PredictionHandler.OperonGene;

public class RegDBIGD implements IGDSource {
	
	Hashtable <String, StrandedFeature> bnums;
	Hashtable <String, OperonGene> op_map;
	PredictionHandler handler;
	LinkedList <OperonGene> genes;
	
	public RegDBIGD (PredictionHandler pred) {
		handler = pred;
		op_map = new Hashtable <String, OperonGene> ();
		bnums = handler.bnums;
	}
	
	public void loadOperons (String op_file) {
		try {
			BufferedReader in = new BufferedReader (new FileReader (op_file));
			String input = RegDBOperon.getFirstRealLine(in, 5);
			StringTokenizer toke;
			String operon;
			String gene;
			int num = 0;
			while (input != null) {
				if (input.indexOf("experiment") > 0) {
					num++;
					toke = new StringTokenizer (input, "\t");
					operon = toke.nextToken();
					toke.nextToken();
					toke.nextToken();
					toke = new StringTokenizer (toke.nextToken(), ",\"");
					Hashtable <String, OperonGene> temp = new Hashtable <
							String, OperonGene> ();
 					while (toke.hasMoreTokens()) {
						gene = toke.nextToken();
						int sep = gene.indexOf ('|');
						String bnum = gene.substring(
								sep + 1, gene.length());
						if (sep < 0 || !bnums.containsKey (bnum)) {
							temp = null;
							break;
						}
						OperonGene op_gene = new OperonGene (bnum);
						op_gene.feat = bnums.get(bnum);
						temp.put(op_gene.name, op_gene);
						op_gene.operon = operon;
					}
 					if (temp != null)
 						op_map.putAll(temp);
				}
				input = in.readLine();
			}
			in.close();
			System.out.println ("number operons: " + num);
			System.out.println ("number genes in operons: " + op_map.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadGenes (String gene_file) {
		genes = new LinkedList <OperonGene> ();
		String input = null;
		try {
			BufferedReader in = new BufferedReader (new FileReader (gene_file));
			input = RegDBOperon.getFirstRealLine(in, 7);
			StringTokenizer toke;
			char pseudo = 'B';
			String operon = null;
			while (input != null) {
				toke  = new StringTokenizer (input, "\t");
				toke.nextToken();
				toke.nextToken();
				String bnum = toke.nextToken();
				if (!bnum.startsWith("b") || !bnums.containsKey(bnum)) {
					input = in.readLine();
					continue;
				}
				OperonGene gene = op_map.get(bnum);
				if (gene == null) {
					gene = new OperonGene (bnum);
					operon = null;
				}
				gene.feat = bnums.get(bnum);
				genes.add(gene);
				input = in.readLine();
			}
			in.close();
		} catch (Exception e) {
			System.out.println ("input: " + input);
			e.printStackTrace();
		}
		System.out.println ("genes not in op: " + (genes.size() - op_map.size()));
	}

	public int getType(IGD igd) {
		if (igd.getLength () < handler.length_restriction)
			return UNDERLENGTH;
		boolean one = op_map.containsKey(igd.gene1.name);
		boolean two = op_map.containsKey(igd.gene2.name);
		if (one && two && igd.gene1.operon.equals(igd.gene2.operon)) {
			if (igd.getLength () > 250)
				System.out.println ("over 150: " + igd.gene1.name + ", " + 
				igd.gene2.name);
			return INTERNAL;
		}
		else if (one || two) {
			if (igd.sameStrand ()) {
				if (igd.getLength () < 50)
					System.out.println ("small: " + igd.gene1.name + ", " + 
							igd.gene2.name + "  " + igd.getLength ());
				return EXTERNAL;
			}
			else
				return STRAND_SWITCH;
		}
		else
			return UNCLEAR;
	}

	public LinkedList<OperonGene> getGenes() {
		return genes;
	}

}
