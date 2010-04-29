package org.gel.mauve.dcjx;

import java.util.HashMap;
import java.util.Map;

public class DCJ {
	
	private Map<String,Integer> blockIdMap;
	
	private Permutation x;
	
	private Permutation y;
	
	private AdjacencyGraph adjXY;
	
	private int dcjDist;
	
	private int scjDist;
	
	private int bpDist;
	
	private int bpReuse;
	
	public DCJ(String genomeX, String genomeY){
		blockIdMap = new HashMap<String,Integer>();
		loadBlockIDMap(genomeX, blockIdMap);
		loadBlockIDMap(genomeY, blockIdMap);
		x = new Permutation(genomeX,blockIdMap);
		y = new Permutation(genomeY,blockIdMap);
		adjXY = new AdjacencyGraph(x,y);
		calculateDistances();
	}
	
	private void calculateDistances(){
		dcjDist = blockIdMap.size() - 
			(adjXY.numCycles() + adjXY.numOddPaths()/2);
		bpDist = blockIdMap.size() - 
			(adjXY.numLen2Cycles() + adjXY.numLen1Paths()/1);
		scjDist = 2*bpDist - adjXY.numPaths2();
	}
	
	public int numBlocks(){
		return blockIdMap.size();
	}
	
	/**
	 * dcj = N - (C-P(odd)/2)
	 * @return
	 */
	public int dcjDistance(){
		return dcjDist;
	}
	/**
	 * scj = 2*bp - P<sub>>=2</sub> 
	 * @return
	 */
	public int scjDistance(){
		return scjDist;
	}
	/**
	 * bp = N - (C<sub>2</sub> + P<sub>1</sub>/2)
	 *  
	 * @return breakpoint distance
	 */
	public int bpDistance(){
		return bpDist;
	}
	
	public AdjacencyGraph getAdjacencyGraph(){
		return adjXY;
	}
	
	private static void loadBlockIDMap(String perm, Map<String,Integer> map){
		String[] blks = perm.split("[\\*$, ]+");
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
	
	public static void main(String[] args){
		
	}
	

}
