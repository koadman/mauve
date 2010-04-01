package org.gel.mauve.dcjx;

import java.util.Map;

public class FastAccessTable {
	
	private Adjacency[] adjacencies;
	
	private int[][] locations;
	
	private Map<String,Integer> blockIdMap;
	
	public FastAccessTable(Adjacency[] adjs, int[][] locs, Map<String,Integer> blockIdMap){
		adjacencies = adjs;
		locations = locs;
		this.blockIdMap = blockIdMap;
	}
	
	public Adjacency getHead(String block){
		return adjacencies[locations[blockIdMap.get(block)][1]];
	}
	
	public Adjacency getTail(String block){
		return adjacencies[locations[blockIdMap.get(block)][0]];
	}
	
	public Adjacency getAdjacency(String blockEnd){
		if (blockEnd.endsWith(BlockEnd.HEAD_TAG)){
			String block = blockEnd.substring(0,blockEnd.length()-2);
		 	return adjacencies[locations[blockIdMap.get(block)][1]];
		} else {
			String block = blockEnd.substring(0,blockEnd.length()-2);
			return adjacencies[locations[blockIdMap.get(block)][0]];
		}
	}
	
}

