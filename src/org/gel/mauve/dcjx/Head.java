package org.gel.mauve.dcjx;

public class Head implements BlockEnd {

	private String name;
	
//	private Block block;
	
	public Head(Block blk){
//		block = blk;
		name = blk.getName() + BlockEnd.HEAD_TAG;
	}
	
	public String getName() {
		return name;
	}

}
