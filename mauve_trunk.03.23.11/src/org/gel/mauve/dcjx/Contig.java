package org.gel.mauve.dcjx;

import java.util.Comparator;



import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.gel.mauve.MauveConstants;

//import org.halophiles.assembly.stat.LCB;

// This might just be over kill, but it might help.
/**
 * 
 * @author atritt
 */
public class Contig {
	
	private static String VALID_BLOCK_NAME = "[A-Za-z0-9_-]+";
	
	private boolean isCirc;
	
	private int numBlocks;
	
	private Block[] blocks;
	
	private String id;
	
	private String name = "";
	
	public Contig (String ch){
		if (ch.endsWith(MauveConstants.CIRCULAR_CHAR)) {
			isCirc = true;
			ch = ch.substring(0,ch.length()-1);
		} else {
			isCirc = false;
		}
		StringTokenizer tok = new StringTokenizer(ch,",");
		numBlocks = 0;
		while(tok.hasMoreTokens()){
			String tmp = tok.nextToken().trim();
			if (tmp.matches(VALID_BLOCK_NAME)){
				numBlocks++;
			}
		}
		tok =  new StringTokenizer(ch,",");
		blocks = new Block[numBlocks];
		int i = 0;  // i := block index/count
		while (tok.hasMoreTokens()){
			String block = tok.nextToken().trim();
			if (!block.matches(VALID_BLOCK_NAME)){
				continue;
			} 
			// check to see if this block is inverted. 
			boolean inv = false;
			if (block.startsWith("-")){ 
				inv = true;
				// remove "-" to get the actual name
				block = block.substring(1);
			}
			blocks[i] = new Block(i,block,inv);
			i = i + 1;
			// don't forget to manage our blockIdMap!
		//	if (!blockIdMap.containsKey(block) ){
		//		blockIdMap.put(block, blockIdMap.size());
		//	}
		}
	}
	
	public Contig(String ch,  String name){
		// Map<String,Integer> blockIdMap,
		this(ch);
		this.name = name;
	}
	
	public boolean isCirc(){
		return isCirc;
	}
	
	public Block[] getBlocks(){
		return blocks;
	}
	
	public boolean hasBlocks(){
		return blocks.length > 0;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < blocks.length; i++){
			sb.append(blocks[i].toString());
			if (i < blocks.length - 1){
				sb.append(",");
			}
		}
		if(isCirc){
			sb.append(MauveConstants.CIRCULAR_CHAR);
		}
		return sb.toString();
	}
	
	public boolean equals(Contig o){
		return id.equalsIgnoreCase(o.id);
	}
	
} 