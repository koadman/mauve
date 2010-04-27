package org.gel.mauve.analysis;

public class LiteWeightFeature implements Comparable
{
	
	private int genSrcIdx;
	private int left;
	private int right;
	private int strand;
	private String locus;
	private String type;
	
	private String ID;
	
	public LiteWeightFeature(int genSrcIdx, int l, int r, int s, String ltag, String featType){
		this.genSrcIdx = genSrcIdx;
		left = l; 
		right = r; 
		strand = s; 
		locus = ltag; 
		type = featType;
		if (locus == null){
			ID = featType+(s == -1 ? "-c-" : "-f-") + Integer.toString(l)+"-"+Integer.toString(r);
		} else 
			ID = locus;
			
	}
	
	public int hashCode(){
		return ID.hashCode();
	}
	
	public String getUniqueID(){
		return ID;
	}
	
	public String getLocus(){
		return locus;
	}
	
	public int getLeft(){
		return left;
	}
	
	public int getRight(){
		return right;
	}
	
	public boolean isReverse(){
		return strand < 0;
	}
	
	public int getStrand(){
		return strand;
	}
	
	public int compareTo(Object o)
	{
		LiteWeightFeature c = (LiteWeightFeature)o;
		if(left < c.left)
			return -1;
		else if(left > c.left)
			return 1;
		if(right < c.right)
			return -1;
		else if(right > c.right)
			return 1;
		if(strand < c.strand)
			return -1;
		else if(strand > c.strand)
			return 1;
		return 0;
	}
}
