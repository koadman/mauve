package org.gel.mauve.analysis;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

public class BrokenCDS {
	
	private LiteWeightFeature cds;
	
	
	private TreeMap<Integer, Character> prmtrStops;
	
	/**
	 * Values are length-2 arrays where element 0
	 * contains the initial amino acid at this postion
	 * and element 1 contains the final amino acid at 
	 * this position
	 */
	private TreeMap<Integer,char[]> naSubs;
	
	private TreeMap<Integer, char[]> aaSubs;
	
	private TreeMap<Integer,Integer> frmShfts;
	
	private TreeSet<Integer> insStops;
	
	/** Segments in amino acid sequence where the assembly is out of frame */
	private Vector<int[]> bfSegs;
	
	private Vector<int[]> gapSegs;

	private double aaSubRate;
	
	public BrokenCDS(LiteWeightFeature cds){
		if (cds.getLength() % 3 != 0) {
			throw new IllegalArgumentException("CDS length must be divisible by 3.");
		}
		this.cds = cds;
		prmtrStops = new TreeMap<Integer,Character>();
		naSubs = new TreeMap<Integer, char[]>();
		aaSubs = new TreeMap<Integer, char[]>();
		frmShfts = new TreeMap<Integer, Integer>();
		bfSegs = new Vector<int[]>();
		gapSegs = new Vector<int[]>();
		insStops = new TreeSet<Integer>();
	}
	
	public void addNASubstitution(int pos, char from, char to){
		char[] pat = {from, to};
		naSubs.put(pos, pat);
	}
	
	public void addAASubstitution(int pos, char from, char to) {
		char[] pat = {from, to};
		aaSubs.put(pos, pat);
	}
	
	public void setAASubRate(double perc) {
		if (perc < 0 || perc > 1) {
			throw new IllegalArgumentException("Error rates must be between 0-1, inclusive.");
		}
		aaSubRate = perc;
	}
	
	public void addPrmtrStop(int pos, char from){
		prmtrStops.put(pos,from);
	}
	
	public void addInsertionStop(int pos){
		insStops.add(pos);
	}
	
	public void addBFSegment(int[] range) {
		if (bfSegs.contains(range)){
			System.out.flush();
		}
		bfSegs.add(range);
	}
	
	public void addGapSegment(int[] range) {
		gapSegs.add(range);
	}
	
	public void addFrameShift(int pos, int length) throws IllegalArgumentException{
		if (length !=1 && length !=2){
			throw new IllegalArgumentException("Illegal length: " + length + " - Frameshifts can only be of length 1 or 2.");
		} else {
			frmShfts.put(pos, length);
		}
	}
	
	public String toString(){
		/*FeatureID Peptide_Length PercIncAAs BrokenFrameSegments GapSegments Subst_Positions Subst_Patterns PrmtrStop_Positions PrmtrStop_OrigRes Frame_Shift_Stops*/
		StringBuilder sb = new StringBuilder();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(6);
		
		sb.append(cds.getID()+"\t"+getPeptideLength()+"\t"+nf.format(aaSubRate));
		
		String tmp = sb.toString();
		if (bfSegs.size() > 0) {
			Iterator<int[]> bfIt = bfSegs.iterator();
			boolean first = true;
			while(bfIt.hasNext()){
				int[] seg = bfIt.next();
				if (first) {
					first = false;
					sb.append("\t["+seg[0]+","+seg[1]+"]");
				} else {
					sb.append(",["+seg[0]+","+seg[1]+"]");
				}
			}
		} else {
			sb.append("\t-");
		}
		
		tmp = sb.toString();
		if (gapSegs.size() > 0) {
			Iterator<int[]> gapIt = gapSegs.iterator();
			boolean first = true;
			while (gapIt.hasNext()) {
				int[] seg = gapIt.next();
				if (first) {
					first = false;
					sb.append("\t["+seg[0]+","+seg[1]+"]");
				} else {
					sb.append(",["+seg[0]+","+seg[1]+"]");
				}
			}
		} else {
			sb.append("\t-");
		}
		
		
		tmp = sb.toString();
		if (aaSubs.size() > 0) {
			Iterator<Integer> it = aaSubs.keySet().iterator();
			StringBuilder subPos = new StringBuilder();
			StringBuilder subPat = new StringBuilder();
			boolean first = true;
			while(it.hasNext()){
				int pos = it.next();
				char[] pat = aaSubs.get(pos);
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
		} else {
			sb.append("\t-\t-");
		}
		if (prmtrStops.size() > 0) {
			Iterator<Integer> it = prmtrStops.keySet().iterator();
			StringBuilder subPos = new StringBuilder();
			StringBuilder subPat = new StringBuilder();
			subPat = new StringBuilder();
			boolean first = true;
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
			sb.append("\t"+subPos.toString()+"\t"+subPat.toString());
		} else {
			sb.append("\t-\t-");
		}
		
		if (insStops.size() > 0) {
			Iterator<Integer> it = insStops.iterator();
			boolean first = true;
			while (it.hasNext()) {
				int pos = it.next();
				if (first) {
					sb.append("\t"+Integer.toString(pos));
					first = false;
				} else {
					sb.append(","+Integer.toString(pos));
				}
			}
		}
		
		return sb.toString();
	}
	
	public int getPeptideLength(){
		return (cds.getRight() - cds.getLeft() + 1) / 3;
	}
	
	public double getPercIncPeptides(){
		return aaSubRate;
	}
	
}