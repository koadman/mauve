package org.gel.mauve.analysis;

public class SNP {

	private char[] pattern;
	
	private long[] pos;
	
	/**
	 * Creates a <code>SNP</code> object with <code>numTaxa</code> taxa
	 * 
	 * @param numTaxa the number of taxa at this SNP
	 */
	public SNP(int numTaxa){
		pattern = new char[numTaxa];
		pos = new long[numTaxa];
		
		for (int i = 0; i < numTaxa; i++){
			pattern[i] = '-';
			pos[i] = 0;
		}
	}
	
	/**
	 * Creates a <code>SNP</code> object with the given patterns and
	 * genome positions. 
	 * 
	 * @param pat the pattern at this SNP
	 * @param pos the positions in each of the genomes
	 */
	public SNP(char[] pat, long[] pos){
		this.pattern = pat;
		this.pos = pos;
	}
	
	/**
	 * Returns a tab-delimited description of SNP
	 * 
	 * Example:
	 * 		ACCA	12354	1658	-13558	986
	 * 
	 * @return a String representation of this SNP
	 * 		
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(pattern);
		for (int i = 0; i < pos.length; i++){
			sb.append("\t"+Long.toString(pos[i]));
		}
		return sb.toString();
	}
	
	/**
	 * Add the base for the specified genome.
	 * 
	 * @param genomeSrcIdx the source index of the genome to be added
	 * @param base the base to be added for genome <code>genomeSrcIdx</code>
	 * @param position the position where this base is located in genome <code>genomeSrcIdx</code> 
	 */
	public void addTaxa(int genomeSrcIdx, char base, long position){
		if (genomeSrcIdx >= pattern.length)
			throw new IllegalArgumentException(genomeSrcIdx+" : source index out of bounds");
		pattern[genomeSrcIdx] = base;
		pos[genomeSrcIdx] = position;
	}
	
	/**
	 * Returns the character for genome <code>genomeSrcIdx</code>
	 * 
	 * @param genomeSrcIdx the source index of the genome in query
	 * @return the character for genome <code>genomeSrcIdx</code> at this SNP
	 */
	public char getChar(int genomeSrcIdx){
		if (genomeSrcIdx >= pattern.length)
			throw new IllegalArgumentException(genomeSrcIdx+" : source index out of bounds");
		else
			return pattern[genomeSrcIdx];
	}
	
	/**
	 * Returns the location of this SNP in genome <code>genomeSrcIdx</code>
	 * 
	 * @param genomeSrcIdx the source index of the genome in query 
	 * @return the  character for genome <code>genomeSrcIdx</code> at this SNP
	 */
	public long getPos(int genomeSrcIdx){
		if (genomeSrcIdx >= pattern.length)
			throw new IllegalArgumentException(genomeSrcIdx+" : source index out of bounds");
		else
			return pos[genomeSrcIdx];
	}
}
