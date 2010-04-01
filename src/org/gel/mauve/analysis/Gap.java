package org.gel.mauve.analysis;

public class Gap implements Comparable<Gap>{

	
	private int genSrcIdx;
	
	private int lcbId;
	
	private long position;
	
	private long length;
	
	public Gap(int genomeSrcIdx, int lcb, long pos, long len){
		this.genSrcIdx = genomeSrcIdx;
		this.position = pos;
		this.length = len;
		this.lcbId = lcb;
	}
	
	
	public String toString(){
		return Integer.toString(genSrcIdx) +"\t"+
				Long.toString(position) +"\t"+ Long.toString(length);
	}
	
	public String toString(String genomeName){
		return genomeName +"\t"+
		Long.toString(position) +"\t"+ Long.toString(length);
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
