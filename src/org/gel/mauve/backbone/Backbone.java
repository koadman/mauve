package org.gel.mauve.backbone;

import java.awt.Color;

import org.gel.mauve.Genome;

public class Backbone {

	protected int lcb_index;
	protected long left_col;
	protected long length;
	protected boolean seqs[];
	protected long left_end[];
	protected long right_end[];
	protected Color color;
	
	public int getLcbIndex(){ return lcb_index; }
	public void setLcbIndex( int lcb_index ){ this.lcb_index = lcb_index; }

	public long getLeftColumn(){ return left_col; }
	public void setLeftColumn( long left_col ){ this.left_col = left_col; }
	
	public long getLength(){ return length; }
	public void setLength( long length ){ this.length = length; }
	
	public boolean[] getSeqs(){ return seqs; }
	public void setSeqs( boolean[] seqs ){ this.seqs = seqs; }
	
	public long getLeftEnd(Genome g)
	{
		int gi = g.getSourceIndex();
		return left_end[gi]; 
	}
	public void setLeftEnd( long[] left_end ){this.left_end = left_end;}
	public long getRightEnd(Genome g)
	{
		int gi = g.getSourceIndex();
		return right_end[gi]; 
	}
	public void setRightEnd( long[] right_end ){this.right_end = right_end;}

	public boolean exists(Genome g)
	{
		int sI = g.getSourceIndex();
		return seqs[sI];
	}
	
	public Color getColor(){ return color; }
	public void setColor(Color color){ this.color = color; }
}
