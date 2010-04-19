package org.gel.mauve.analysis;

import java.util.Comparator;
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
	
	private XmfaViewerModel model;
	
	
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
		this.chrom = model.getGenomeBySourceIndex(genSrcIdx).getChromosomeAt(position);
		this.posInCtg = (int) (position - this.chrom.getStart()+1);
	}
	
	private Gap(Gap gap){
		this.genSrcIdx = gap.genSrcIdx;
		this.position = gap.position;
		this.length = gap.length;
		this.lcbId = gap.lcbId;
		this.model = gap.model;
		this.chrom = gap.chrom;
		this.posInCtg = gap.posInCtg;
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
	
}
