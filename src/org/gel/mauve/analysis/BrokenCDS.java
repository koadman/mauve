package org.gel.mauve.analysis;

import java.util.Iterator;
import java.util.TreeMap;

public class BrokenCDS {
	
	private LiteWeightFeature cds;
	
	
	private TreeMap<Integer, Character> prmtrStops;
	
	/**
	 * Values are length-2 arrays where element 0
	 * contains the initial amino acid at this postion
	 * and element 1 contains the final amino acid at 
	 * this position
	 */
	private TreeMap<Integer,char[]> subs;
	
	private TreeMap<Integer,Integer> frmShfts;

	
	public BrokenCDS(LiteWeightFeature cds){
		this.cds = cds;
		prmtrStops = new TreeMap<Integer,Character>();
		subs = new TreeMap<Integer, char[]>();
		frmShfts = new TreeMap<Integer, Integer>();
	}
	
	public void addSubstitution(int pos, char from, char to){
		char[] pat = {from, to};
		subs.put(pos, pat);
	}
	
	public void addPrmtrStop(int pos, char from){
		prmtrStops.put(pos,from);
	}
	
	public void addFrameShift(int pos, int length) throws IllegalArgumentException{
		if (length !=1 && length !=2){
			throw new IllegalArgumentException("Illegal length: " + length + " - Frameshifts can only be of length 1 or 2.");
		} else {
			frmShfts.put(pos, length);
		}
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(cds.getID());
		Iterator<Integer> it = subs.keySet().iterator();
		StringBuilder subPos = new StringBuilder();
		StringBuilder subPat = new StringBuilder();
		boolean first = true;
		while(it.hasNext()){
			int pos = it.next();
			char[] pat = subs.get(pos);
			if (first){
				subPat.append(pat[0] +"->"+ pat[1]);
				subPos.append(pos);
				first = false;
			} else {
				subPat.append(","+pat[0] +"->"+ pat[1]);
				subPos.append(","+pos);
			}
		}
		sb.append("\t"+subPos.toString()+"\t"+subPat.toString());
		it = prmtrStops.keySet().iterator();
		subPos = new StringBuilder();
		subPat = new StringBuilder();
		first = true;
		while(it.hasNext()){
			int pos = it.next();
			char c = prmtrStops.get(pos);
			if (first) {
				subPos.append(pos);
				subPat.append(c);
				first = false;
			} else {
				subPos.append(","+pos);
				subPat.append(","+c);
			}
		}
		return sb.toString();
	}
}