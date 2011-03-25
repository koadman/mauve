package org.gel.mauve.dcjx;

public class Tail implements BlockEnd {

	private String name;
	
//	private Block block;
	
	public Tail(Block blk){
//		block = blk;
		name = blk.getName() + BlockEnd.TAIL_TAG;
	}
	
	public String getName() {
		return name;
	}

}
