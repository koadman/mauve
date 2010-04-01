package org.gel.mauve.dcjx;

import java.util.HashMap;
import java.util.Map;

public class DCJ {
	
	private Map<String,Integer> blockIdMap;
	
	private Permutation x;
	
	private Permutation y;
	
	private AdjacencyGraph adjXY;
	
	private int numCyc;
	
	private int numOdd;
	
	public DCJ(String genomeX, String genomeY){
		blockIdMap = new HashMap<String,Integer>();
		loadBlockIDMap(genomeX, blockIdMap);
		loadBlockIDMap(genomeY, blockIdMap);
		x = new Permutation(genomeX,blockIdMap);
		y = new Permutation(genomeY,blockIdMap);
		adjXY = new AdjacencyGraph(x,y);
		numCyc = adjXY.numCycles();
		numOdd = adjXY.numOddPaths();
	}
	
	public int numBlocks(){
		return blockIdMap.size();
	}
	
	public int dcjDistance(){
		return blockIdMap.size() - (numCyc + numOdd/2);
	}
	
	public double getBPReuseRate(){
		double num = (1 - (numCyc+numOdd/2));
		double den = blockIdMap.size();
		return num/den;
	}
	
	public AdjacencyGraph getAdjacencyGraph(){
		return adjXY;
	}
	
	public static void loadBlockIDMap(String perm, Map<String,Integer> map){
		String[] blks = perm.split("[$, ]+");
		for (int i = 0; i < blks.length; i++){
			blks[i] = blks[i].trim();
			if (blks[i].length() == 0) // empty contig
				continue;
			if (blks[i].startsWith("-")){
				blks[i] = blks[i].substring(1);
			}
			if (blks[i].endsWith("*")){
				blks[i] = blks[i].substring(0,blks[i].length()-1);
			}
			if(!map.containsKey(blks[i])){
				int count = map.size();
				map.put(blks[i], count);
			}
		}
		
	}
	
	public static int computeDCJ(Permutation x, Permutation y, Map<String,Integer> blockIdMap){
		AdjacencyGraph agXY = new AdjacencyGraph(x, y);
		int C = agXY.numCycles();
		int I = agXY.numOddPaths();
		int ret = blockIdMap.size() - (C + I/2);
		return ret;
	}
	
	public static int computeDCJ(String genomeX, String genomeY){
		Map<String,Integer> blockIdMap = 
			new HashMap<String,Integer>();
		DCJ.loadBlockIDMap(genomeX, blockIdMap);
		DCJ.loadBlockIDMap(genomeY, blockIdMap);
		Permutation x = new Permutation(genomeX, blockIdMap);
		Permutation y = new Permutation(genomeX, blockIdMap);
		return computeDCJ(x, y, blockIdMap);
	}
	

}
