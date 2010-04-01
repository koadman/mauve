package org.gel.mauve.dcjx;

public class Block {
	
	/** 
	 * The index of this block on the chromosome. 
	 * This number is only unique within the chromosome holding this block
	 * 
	 * NOTE: this is not the same as a block ID, which can be obtained from 
	 *       blockIdMap using this.name
	 */
	private int blockIdx;
	
	private String name;
	
	private boolean inv;
	
	public Block(int blockIdx, String blockName, boolean inv){
		this.blockIdx = blockIdx;
		this.name = blockName;
		this.inv = inv;
	}
	
	public String toString(){
		if (inv){
			return "-"+name;
		} else {
			return name;
		}
	}
	
	public String getLeftEnd(){
		if (inv){
			return name + BlockEnd.HEAD_TAG;
		} else {
			return name + BlockEnd.TAIL_TAG;
		}
	}
	
	public String getRightEnd(){
		if (inv){
			return name + BlockEnd.TAIL_TAG;
		} else {
			return name + BlockEnd.HEAD_TAG;
		}
	}
	
	public int getIdx(){
		return blockIdx;
	}
	
	public String getName(){
		return name;
	} 
	
	public boolean isInverted(){
		return inv;
	}
	
	
}
