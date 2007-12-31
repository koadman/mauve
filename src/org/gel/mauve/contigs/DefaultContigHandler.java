package org.gel.mauve.contigs;

import java.util.Iterator;
import java.util.List;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.analysis.Segment;

public class DefaultContigHandler implements ContigHandler, MauveConstants {
	
	//reference to model that contains sequence data
	protected BaseViewerModel model;
	
	public DefaultContigHandler (BaseViewerModel mod) {
		model = mod;
	}

	public long getContigCoord(int sequence, long loci) {
		Genome genome = model.getGenomeBySourceIndex (sequence);
		if (loci == 0 || genome.getChromosomes () == null ||
				genome.getChromosomeAt (loci) == null)
			return loci;
		else
			return genome.getChromosomeAt (loci).relativeLocation (loci);
	}
	
	public long getPseudoCoord (int sequence, long loci, String contig) {
		Chromosome chrom = getChromosomeFromName (sequence, contig);
		if (chrom != null)
			return loci + chrom.getStart() - 1;
		else
			return loci;
	}
	
	public Chromosome getChromosomeFromName (int sequence, String contig) {
		Iterator chroms = model.getGenomeBySourceIndex(sequence).getChromosomes().iterator();
		while (chroms.hasNext()) {
			Chromosome chrom = (Chromosome) chroms.next ();
			if (chrom.getName().indexOf(contig) > -1)
				return chrom;
		}
		return null;
	}

	public void fixSegmentByContigs(int sequence, Segment segment) {
		// TODO Auto-generated method stub

	}

	public String getContigName(int sequence, long loci) {
		if (loci == 0)
			return "";
		Chromosome chrom = model.getGenomeBySourceIndex (sequence).getChromosomeAt (loci);
		return chrom == null ? DEFAULT_CONTIG : chrom.getName ();
	}

}
