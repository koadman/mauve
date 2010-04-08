package org.gel.mauve.analysis;

import java.util.Iterator;
import java.util.List;

import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;
import org.gel.mauve.XmfaViewerModel;

public class Gap implements Comparable<Gap>{

	
	private int genSrcIdx;
	
	private int lcbId;
	
	private long position;
	
	private int posInCtg;
	
	private Chromosome chrom;
	
	private long length;
	
//	private XmfaViewerModel model;
	
	public Gap(int genomeSrcIdx, int lcb, long pos, long len, XmfaViewerModel model){
		this.genSrcIdx = genomeSrcIdx;
		this.position = pos;
		this.length = len;
		this.lcbId = lcb;
	//	this.model = model;
		this.chrom = model.getGenomeBySourceIndex(genSrcIdx).getChromosomeAt(position);
		this.posInCtg = (int) (position - this.chrom.getStart()+1);
	}
	
	/**
	 * Genome  Contig   Position_in_Contig   GenomeWide_Position   Length
	 * 
	 * @return a tab-delimited String with the information ordered as given above
	 */
	public String toString(){
		return Integer.toString(genSrcIdx) +"\t"+ chrom.getName() + "\t"+
				Integer.toString(posInCtg)+"\t"+ Long.toString(position) +"\t"+ Long.toString(length);
	}
	
	
	
	/**
	 * Genome  Contig   Position_in_Contig   GenomeWide_Position   Length
	 * 
	 * Replaces genome id with the given name
	 * 
	 * @param genomeName the name to put in place of the genome source index 
	 * 
	 * @return a tab-delimited String with the information ordered as given above
	 */
	public String toString(String genomeName){
		return genomeName +"\t"+ chrom.getName() + "\t"+
		Integer.toString(posInCtg)+"\t"+ Long.toString(position) +"\t"+ Long.toString(length);
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
	
}
