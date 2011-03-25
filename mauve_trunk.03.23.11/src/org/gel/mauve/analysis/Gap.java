package org.gel.mauve.analysis;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.biojava.bio.seq.FeatureHolder;
import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;
import org.gel.mauve.LCB;
import org.gel.mauve.XmfaViewerModel;

public class Gap implements Comparable<Gap>{

	
	private int genSrcIdx;
	
	private int lcbId;
	
	private int lcbCol;
	
	private long position;
	
	private int posInCtg;
	
	private Chromosome chrom;
	
	private long length;
	
	private XmfaViewerModel model;
	
	private long[] pos;
	
	private Chromosome[] chromosomes;
	
	
	/**
	 * Creates a gap with the given arguments.
	 * 
	 * @param genomeSrcIdx source index of Genome this Gap belongs to
	 * @param lcb the ID of the LCB that this Gap belongs to
	 * @param pos the position that this Gap starts at
	 * @param len the length of the Gap
	 * @param model the XmfaViewerModel holding the alignment that this Gap was determined under
	 */
	public Gap(int genomeSrcIdx, int lcb, long pos, long len, XmfaViewerModel model){
		this.genSrcIdx = genomeSrcIdx;
		this.position = pos;
		this.length = len;
		this.lcbId = lcb;
		this.model = model;
		Genome[] genomes = model.getGenomes().toArray(new Genome[model.getGenomes().size()]);
		Genome g = model.getGenomeBySourceIndex(genSrcIdx);
		this.chrom = g.getChromosomeAt(position);
		if (this.chrom == null) System.err.println("Null Chromosome");
		this.posInCtg = (int) (position - this.chrom.getStart()+1);
		this.lcbCol = (int) model.getLCBAndColumn(g, position)[1];
		this.pos = new long[model.getGenomes().size()];
		long[] ar = model.getLCBAndColumn(genomeSrcIdx, pos);
		boolean[] gap = new boolean[model.getGenomes().size()];
		model.getColumnCoordinates((int)ar[0], ar[1], this.pos, gap);
		chromosomes = new Chromosome[genomes.length];
		for (int i = 0; i < genomes.length; i++){
			chromosomes[i] = genomes[i].getChromosomeAt(this.pos[i]);
		}
	}
	
	private Gap(Gap gap){
		this.genSrcIdx = gap.genSrcIdx;
		this.position = gap.position;
		this.length = gap.length;
		this.lcbId = gap.lcbId;
		this.model = gap.model;
		this.chrom = gap.chrom;
		this.posInCtg = gap.posInCtg;
		this.lcbCol = gap.lcbCol;
		this.pos = new long[gap.pos.length];
		System.arraycopy(gap.pos, 0, this.pos, 0, this.pos.length);
		this.chromosomes = new Chromosome[gap.chromosomes.length];
		System.arraycopy(gap.chromosomes, 0, this.chromosomes, 0, this.chromosomes.length);
	}
	
	
	/**
	 * Genome  Contig   Position_in_Contig   GenomeWide_Position   Length
	 * 
	 * @return a tab-delimited String with the information ordered as given above
	 */
	public String toString(){
		return this.toString("sequence_"+Integer.toString(genSrcIdx));
	}
	
	
	
	/**
	 * Genome  Contig   Position_in_Contig   GenomeWide_Position   Length  seqi_pos seqi_ctg seqi_posInCtg
	 * 
	 * Replaces genome id with the given name
	 * 
	 * @param genomeName the name to put in place of the genome source index 
	 * 
	 * @return a tab-delimited String with the information ordered as given above
	 */
	public String toString(String genomeName){
		StringBuilder sb = new StringBuilder();
		sb.append(genomeName +"\t"+ chrom.getName() + "\t"+
				Integer.toString(posInCtg)+"\t" + 
				Long.toString(position) +"\t"+ Long.toString(length));
		for (int i = 0; i < pos.length; i++){
			int ctg_pos = (int)(pos[i] - chromosomes[i].getStart()+1); 
			sb.append("\t"+pos[i]+"\t"+chromosomes[i].getName()+"\t"+ctg_pos);
		}
		return sb.toString();
	}
	
	public int getGenomeSrcIdx(){
		return genSrcIdx;
	}
	
	public long getPosition(){
		return position;
	}
	
	public long getLength(){
		return length;
	}
	
	public int getLCB(){
		return lcbId;
	}
	
	public FeatureHolder getFeatures(int genSrcIdx){
		Genome genome = model.getGenomeBySourceIndex(genSrcIdx);
		long[] starts = new long[model.getGenomes().size()];
		boolean[] gapS = new boolean[model.getGenomes().size()];
		long[] ends = new long[model.getGenomes().size()];
		boolean[] gapE = new boolean[model.getGenomes().size()];
		model.getColumnCoordinates(lcbId, lcbCol, starts, gapS);
		model.getColumnCoordinates(lcbId, lcbCol+1, ends, gapE);
		long left = Math.min(starts[genSrcIdx],ends[genSrcIdx]);
		long right = Math.max(starts[genSrcIdx],ends[genSrcIdx]);
		boolean rev = starts[genSrcIdx] > ends[genSrcIdx];
		return genome.getAnnotationsAt(left, right, rev);
	}
	
	
	/**
	 * Returns the contig that this gap lies on
	 * 
	 * @param model
	 * @return
	 */
	public Chromosome getContig(){
		return chrom;
	}
	
	/**
	 * Returns the position of this gap in the contig
	 * in which it lies.
	 * 
	 * @param model the <code>XmfaViewerModel</code> this Gap came from
	 * 
	 * @return a non-concatenated genome coordinate
	 */
	public int getPositionInContig(){
		return posInCtg;
	}
	
	public int compareTo(Gap g){
		if (this.genSrcIdx == g.genSrcIdx){
			if (this.position == g.position){
				if (this.length == g.length){
					return 0;
				} else {
					return this.length < g.length ? -1 : 1;
				}
			} else {
				return this.position < g.position ? -1 : 1;
			}
		} else {
			return this.genSrcIdx < g.genSrcIdx ? -1 : 1;
		}
	}
	

	public static Gap mergeGaps(Gap a, Gap b){
		if (a.genSrcIdx != b.genSrcIdx)
			throw new IllegalArgumentException("Can't merge gaps that aren't from same genome");
		if (a.position != b.position)
			throw new IllegalArgumentException("Can't merge gaps that don't start at the same position");
		
		Gap ret = new Gap(a);
		ret.length += b.length;
		return ret;
		
	}
	
	public static Comparator<Gap> getAlnmtPosComparator(){
		return new Comparator<Gap>(){
			public int compare(Gap a, Gap b){
				if (a.genSrcIdx == b.genSrcIdx){
					if (a.position == b.position){
						return (int) (a.length - b.length);
					} else {
						return (int) (a.position - b.position);
					}
				} else {
					return a.genSrcIdx-b.genSrcIdx;
				}
			}
		};
	}
	/**
	 * <br>Returns a negative number, zero, or a positive number 
	 * if this Gap comes before, lies within, or comes after the
	 * given feature, respectively. </br>
	 * <br>
	 * If the feature passed in is not a feature in the genome that this
	 * gap pertains to, the relative position of this gap to the feature
	 * is determined based on the position in the genome, which the feature 
	 * belongs to, that is homologous to the position that this gap starts at.
	 * </br>
	 * <br>
	 * Strand is neglected here, as a gap affects both strands.
	 * </br>
	 * @param feat the feature to compare this Gap to
	 * 
	 * @return a negative number, zero, or a positive number
	 */
	public int relativePos(LiteWeightFeature feat){
		int pos = (int) Math.abs(this.pos[feat.getGenSrcIdx()]);
		if (pos < feat.getLeft())
			return -1;
		else if (pos <= feat.getRight())
			return 0;
		else
			return 1;
	}
	
}
