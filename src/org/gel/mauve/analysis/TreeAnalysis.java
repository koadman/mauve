package org.gel.mauve.analysis;

import java.util.Vector;


import org.gel.mauve.Genome;
import org.gel.mauve.LCB;
import org.gel.mauve.XMFAAlignment;
import org.gel.mauve.XmfaViewerModel;

import pal.distance.DistanceMatrix;
import pal.misc.IdGroup;
import pal.misc.Identifier;
import pal.misc.SimpleIdGroup;

public class TreeAnalysis {
	
	
	
	
	public static DistanceMatrix getLocalDistMat(XmfaViewerModel model, XMFAAlignment xmfa, LCB lcb){
		long[] starts = lcb.starts;
		int taxaCount = 0;
		Vector<Genome> vNames = new Vector<Genome>();
		for (int i = 0; i < starts.length; i++){
			if (starts[i]>0){
				String name = model.getGenomeBySourceIndex(i).getDisplayName();
				int lastIdx = name.lastIndexOf(".");
				name = name.substring(name.lastIndexOf("/")+1, 
						(lastIdx>=0?lastIdx:name.length()));
				vNames.add(model.getGenomeBySourceIndex(i));
				taxaCount++;
			}
			
		}
		Genome[] genomes = vNames.toArray(new Genome[taxaCount]);
		double[][] dist = new double[taxaCount][taxaCount];
		SNP[] snps = SnpExporter.getLocalSNPs(model, xmfa, lcb);
		for (int snpI = 0; snpI < snps.length; snpI++){
			SNP tmp = snps[snpI];
			if (!tmp.hasAmbiguities()){
				for (int genI=0;genI<genomes.length;genI++){
					for (int genJ=0;genJ<genI;genJ++){
						dist[genI][genJ]+=tmp.areEqual(genomes[genI], genomes[genJ])?0:1;
					}
				}
				
			}
		}
		String[] names = new String[genomes.length];
		for (int i = 0; i < genomes.length; i++)
			names[i] = genomes[i].getDisplayName();
		return new DistanceMatrix(dist, new SimpleIdGroup(names));
	}
	
	
	

}
