package org.gel.mauve.operon.pred;

import java.util.Iterator;
import java.util.LinkedList;

import org.biojava.bio.seq.StrandedFeature;
import org.gel.air.bioj.BioJavaUtils;
import org.gel.mauve.operon.pred.PredictionHandler.IGD;
import org.gel.mauve.operon.pred.PredictionHandler.OperonGene;

public class MauveFeatureIGD implements IGDSource {
	
	protected LinkedList <OperonGene> genes;
	protected PredictionHandler handler;
	
	public MauveFeatureIGD(PredictionHandler handler) {
		this.handler = handler;
		makeOperonGenes (handler.restricted_genes.iterator());
	}
	
	protected void makeOperonGenes (Iterator <StrandedFeature> feats) {
		genes = new LinkedList <OperonGene> ();
		while (feats.hasNext()) {
			StrandedFeature feat = feats.next ();
			OperonGene gene = new OperonGene (feat);
			genes.addLast(gene);
		}
	}

	public int getType(IGD igd) {
		if (!igd.sameStrand())
			return STRAND_SWITCH;
		else if (igd.getLength() < handler.length_restriction)
			return UNDERLENGTH;
		else if (igd.getLength() <= handler.max_within)
			return INTERNAL;
		else if (igd.getLength() >= handler.min_without)
			return EXTERNAL;
		else
			return UNCLEAR;
	}

	public LinkedList<OperonGene> getGenes() {
		return genes;
	}
	
	

}
