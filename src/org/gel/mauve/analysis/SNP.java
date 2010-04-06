package org.gel.mauve.analysis;

import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;
import org.gel.mauve.XmfaViewerModel;

public class SNP {

	private char[] pattern;
	
	private long[] pos;
	
	private boolean[] present;
	
	private Chromosome[] chrom;
	
	private int[] posInCtg;
	
	private XmfaViewerModel model;
	
	/**
	 * Creates a <code>SNP</code> object with <code>numTaxa</code> taxa
	 * 
	 * @param numTaxa the number of taxa at this SNP
	 */
	public SNP(XmfaViewerModel model){
		int numTaxa = model.getSequenceCount();
		present = new boolean[numTaxa];
		pattern = new char[numTaxa];
		pos = new long[numTaxa];
		for (int i = 0; i < numTaxa; i++){
			pattern[i] = '-';
			pos[i] = 0;
		}
		this.model = model;
		chrom = new Chromosome[numTaxa];
		posInCtg = new int[numTaxa];
	}
	
	/**
	 * Creates a <code>SNP</code> object with the given patterns and
	 * genome positions. 
	 * 
	 * @param pat the pattern at this SNP
	 * @param pos the positions in each of the genomes
	 */
	private SNP(char[] pat, long[] pos, XmfaViewerModel model){
		this.pattern = pat;
		this.pos = pos;
		this.model = model;
	}
	
	/**
	 * <p>
	 * Returns a tab-delimited description of the SNP
	 * </p><p>
	 * SNP_pattern seq0-Contig seq0-Position_in_Contig seq0-GenomeWide_Position...
	 * </p><p>
	 * Example:<br/>
	 * 		AC	contig_0087 -1658 -1235 Chromosome 1090 1090</p>
	 * 
	 * @return a String representation of this SNP
	 * 		
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(pattern);
		for (int i = 0; i < pos.length; i++){
			sb.append("\t"+chrom[i].getName()
					  +"\t"+posInCtg[i]+"\t"+pos[i]);
		}
		return sb.toString();
	}
	
	/**
	 * Add the base for the specified genome.
	 * 
	 * @param genSrcIdx the source index of the genome to be added
	 * @param base the base to be added for genome <code>genSrcIdx</code>
	 * @param position the position where this base is located in genome <code>genSrcIdx</code> 
	 */
	public void addTaxa(int genSrcIdx, char base, long position){
		if (genSrcIdx >= pattern.length)
			throw new IllegalArgumentException(genSrcIdx+" : source index out of bounds");
		present[genSrcIdx] = true;
		pattern[genSrcIdx] = base;
		pos[genSrcIdx] = position;
		chrom[genSrcIdx] =
			model.getGenomeBySourceIndex(genSrcIdx).getChromosomeAt(position);
		posInCtg[genSrcIdx] = (int) (position - chrom[genSrcIdx].getStart() + 1);
		
	}
	
	/**
	 * Returns the character for genome <code>genSrcIdx</code>
	 * 
	 * @param genSrcIdx the source index of the genome in query
	 * @return the character for genome <code>genSrcIdx</code> at this SNP
	 */
	public char getChar(int genSrcIdx){
		if (genSrcIdx >= pattern.length)
			throw new IllegalArgumentException(genSrcIdx+" : source index out of bounds");
		else
			return pattern[genSrcIdx];
	}
	
	/**
	 * Returns the contig this SNP lies on in the given genome
	 * 
	 * @param genSrcIdx the source index of the genome of interest
	 * @return the contig in genome <code>genSrcIdx</code> where this SNP lies
	 */
	public Chromosome getContig(int genSrcIdx){
		return model.getGenomeBySourceIndex(genSrcIdx).
							getChromosomeAt(pos[genSrcIdx]);
	}
	
	/**
	 * Returns the position in the respective contig of this SNP
	 * for the given genome.
	 * 
	 * @param genSrcIdx the source index of the genome of interest
	 * @return the position in the contig of genome <code>genSrcIdx</code> where this SNP lies
	 */
	public int getPositionInContig(int genSrcIdx){
		return (int) (pos[genSrcIdx]-getContig(genSrcIdx).getStart() + 1);
	}
	
	/**
	 * Returns the location of this SNP in genome <code>genomeSrcIdx</code>
	 * 
	 * @param genomeSrcIdx the source index of the genome in query 
	 * @return the  character for genome <code>genomeSrcIdx</code> at this SNP
	 */
	public long getPos(int genomeSrcIdx){
		if (genomeSrcIdx >= pattern.length)
			throw new IllegalArgumentException(genomeSrcIdx+" : source index out of bounds");
		else
			return pos[genomeSrcIdx];
	}
	
	/**
	 * Returns true if any genomes have a gap in this SNP.
	 * 
	 * @return false if all genomes have a base present here, true otherwise
	 */
	public boolean hasGap(){
		for (int i = 0; i < pattern.length; i++){
			if (pattern[i] == '-')
				return true;
		}
		return false;
	}
	
	/**
	 * Returns true if the specified genome has a gap in this SNP
	 * 
	 * @param idx the genome source index 
	 * @return true if genome <code>idx</code> has a gap here, false otherwise
	 */
	public boolean hasGap(int idx){
		return pattern[idx] == '-';
	}
	
	public boolean hasAmbiguities(){
		for (int i = 0; i < pattern.length; i++){
			char c = pattern[i];
			switch(c){
			case 'k': return true;
			case 'K': return true;
			case 'm': return true;
			case 'M': return true;
			case 'r': return true;
			case 'R': return true;
			case 'y': return true;
			case 'Y': return true;
			case 's': return true;
			case 'S': return true;
			case 'w': return true;
			case 'W': return true;
			case 'b': return true;
			case 'B': return true;
			case 'v': return true;
			case 'V': return true;
			case 'h': return true;
			case 'H': return true;
			case 'd': return true;
			case 'D': return true;
			case 'x': return true;
			case 'X': return true;
			case 'n': return true;
			case 'N': return true;
			case '-': return true;
				default :
			}
		}
		return false;
	}
	
	public boolean areEqual(Genome x, Genome y){
		return pattern[x.getSourceIndex()] == pattern[y.getSourceIndex()];
	}
	
}
