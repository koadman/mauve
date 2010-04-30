package org.gel.mauve.analysis;

import java.util.Comparator;

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
	
	public int getGenSrcIdx(){
		return genSrcIdx;
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
	
	public String getID(){
		return this.ID;
	}
	
	public static Comparator<LiteWeightFeature> getLoopingComparator(){
		return new Comparator<LiteWeightFeature>(){
			public int compare(LiteWeightFeature o1, LiteWeightFeature o2){
				// if on same strand
				if (o1.strand == o2.strand){
					if (o1.strand > 0){ // on forward strand
						if (o1.left < o2.left) 
							return -1;
						else if (o1.left > o2.left)
							return 1; 
						if (o1.right < o2.right)
							return -1;
						else if (o1.right > o2.right)
							return 1;
						else
							return 0;
					} else { // on complementary strand
						if (o1.right > o2.right)
							return -1;
						else if (o1.right < o2.right)
							return 1;
						if (o1.left > o2.left)
							return -1;
						else if (o1.left < o2.left)
							return 1;
						else
							return 0;
					}
				} else if (o1.strand > 0){ // keep the forward strand stuff first
					return -1;
				} else {
					return 1;
				}
			}
		};
	}
	
	
}
