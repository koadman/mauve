package org.gel.mauve.dcjx;

import java.io.PrintStream;

import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * 
 * @author atritt
 */
public class Permutation {
	
	/** A map for holding identifiers */
//	public static HashMap<String, Integer> blockIdMap = new HashMap<String, Integer>();
	
//	public static int BLOCK_COUNT = 0;
	
//	private Map<String, Integer> blockIdMap;
	
	private int numChrom;
	
	private Adjacency[] adj;
	
	/**
	 *  a BLOCK_COUNT x 2 array storing positions of heads and tails in the Adjacency array
	 *  loc[][0] := tail loc[][1] := head 
	 */
	private int[][] loc;
	
	private FastAccessTable fat;
	
	private Contig[] chrom;
	
	private int numLinear;
	
	private String name = "";
	
	public Permutation (String g, Map<String,Integer> blockIdMap, String name){
		this(g, blockIdMap);
		this.name = name;
	}
	
	public Permutation (String g, Map<String,Integer> blockIdMap){
	//	this.blockIdMap = blockIdMap;
		StringTokenizer tok = new StringTokenizer(g,"$");
		numChrom = tok.countTokens();
		chrom = new Contig[numChrom];
		int i = 0;
		while(tok.hasMoreTokens()){
			String c = tok.nextToken();
				chrom[i] = new Contig(c);
				if (!chrom[i].isCirc() && chrom[i].hasBlocks())
					numLinear++;
			i++;
		}
		loc = new int[blockIdMap.size()][2];
		
		// for N blocks and k linear chromosomes, the number of adjacencies = N + k
		adj = new Adjacency[blockIdMap.size()+numLinear];
		addAdjacencies(chrom,adj,loc, blockIdMap);  // we're doing a lot here... lets make a function for this.
		fat = new FastAccessTable(adj, loc, blockIdMap);
	}
	
	public String getName(){
		return name;
	}

	public Adjacency[] getAdjacencies(){
		return adj;
	}
	
	public FastAccessTable getFAT(){
		return fat;
	}
	
	public void printDesc(PrintStream out){
		out.println(name + ": " + this.toString());
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < chrom.length; i++){
			sb.append(chrom[i].toString());
			sb.append("$");
		}
		return sb.toString();
	}
	public static void addLocations(Block b1, Block b2, int idx, int[][] loc, Map<String,Integer> blockIdMap){
		// a,b,c*$ a,b,-c*$ -a,b,-c*$ 
		// first is c
		// second is a
		
		if(b1.isInverted()){  // we'll get c_t
			loc[blockIdMap.get(b1.getName())][0] = idx;
		} else {                // we'll get c_h
			loc[blockIdMap.get(b1.getName())][1] = idx;
		}
		if (b2.isInverted()){  // we'll get a_h
			loc[blockIdMap.get(b2.getName())][1] = idx;
		} else {                   // we'll get a_t
			loc[blockIdMap.get(b2.getName())][0] = idx;
		}
	}

	/**
	 * 
	 * @param chrom
	 * @param adj
	 * @param loc
	 */
	public static void addAdjacencies(Contig[] chrom, Adjacency[] adj, int[][] loc, Map<String,Integer> blockIdMap){
		int adjIdx = 0;
		for (int i = 0; i < chrom.length; i++){
			Block[] blk = chrom[i].getBlocks();
			if (blk.length == 0){
				continue;
			}
			if (chrom[i].isCirc()){
				addLocations(blk[blk.length-1], blk[0], adjIdx, loc, blockIdMap);
				adj[adjIdx] = new Adjacency(blk[blk.length-1].getRightEnd(),blk[0].getLeftEnd());
				adjIdx++;
			} else {
				// add left telomere location
				if (blk[0].isInverted()){
					loc[blockIdMap.get(blk[0].getName())][1] = adjIdx;
				} else {
					loc[blockIdMap.get(blk[0].getName())][0] = adjIdx;
				}
				adj[adjIdx] = new Adjacency(blk[0].getLeftEnd());
				adjIdx++;
			}
			for (int j = 1; j < blk.length; j++){
				addLocations(blk[j-1], blk[j], adjIdx, loc, blockIdMap);
				adj[adjIdx] = new Adjacency(blk[j-1].getRightEnd(), blk[j].getLeftEnd());
				adjIdx++;
			}
			if (!chrom[i].isCirc()){
				//  add right telomere location
				if (blk[blk.length-1].isInverted()){
					loc[blockIdMap.get(blk[blk.length-1].getName())][0] = adjIdx;
				} else {
					loc[blockIdMap.get(blk[blk.length-1].getName())][1] = adjIdx;
				}
				adj[adjIdx] = new Adjacency(blk[blk.length-1].getRightEnd());
				adjIdx++;
			}
		}
	}

	/**
	 * Returns true if the two permutations have equal content, false otherwise.
	 * 
	 * complexity ~ O(max(|X|,|Y|))
	 * 
	 * @param X 
	 * @param Y
	 * @return true if (X\Y) U (Y\X) == empty set, false otherwise 
	 */
	public static boolean equalContents(String X, String Y){
		String[] blkX = X.split("[$, ]+");
		String[] blkY = Y.split("[$, ]+");
//		if (blkY.length != blkX.length)
//			return false;
		HashSet<String> set = new HashSet<String>();
		for (int i = 0; i < blkX.length; i++){
			blkX[i] = blkX[i].trim();
			if (blkX[i].length() == 0) // empty contig
				continue;
			if (blkX[i].startsWith("-")){
				blkX[i] = blkX[i].substring(1);
			}
			set.add(blkX[i]);
		}
		for (int j = 0; j < blkY.length; j++){
			blkY[j] = blkY[j].trim();
			if (blkY[j].length() == 0) // empty contig
				continue;
			if (blkY[j].startsWith("-")){
				blkY[j] = blkY[j].substring(1);
			}
			if (set.remove(blkY[j])){
				continue;
			} else {
				return false;
			}
		}
		if (set.isEmpty())
			return true;
		else 
			return false;
	}
	
	
}
