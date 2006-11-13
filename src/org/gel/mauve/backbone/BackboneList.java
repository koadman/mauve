package org.gel.mauve.backbone;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import org.gel.mauve.Genome;
import org.gel.mauve.XMFAAlignment;

/**
 * Compares LCB ids and left-columns of backbone segments
 */
class BbComparator implements Comparator
{
	protected int seq;
	BbComparator(int seq){ this.seq = seq; }
    public int compare(Object o_a, Object o_b)
    {
    	Backbone a = (Backbone) o_a;
    	Backbone b = (Backbone) o_b;
    	boolean a_def = a.getSeqs()[seq];
    	boolean b_def = b.getSeqs()[seq];
    	if(!a_def && !b_def)
    		return 0;
    	if(!a_def)
    		return -1;
    	if(!b_def)
    		return 1;
    	int a_lcb_id = a.getLcbIndex();
    	int b_lcb_id = b.getLcbIndex();
    	if(a_lcb_id != b_lcb_id)
    		return a_lcb_id - b_lcb_id;
        return (int)(a.getLeftColumn() - b.getLeftColumn());
    }
}

/**
 * Compares LCB ids and left-columns of backbone segments
 */
class BbLeftEndComparator implements Comparator
{
	protected Genome g;
	BbLeftEndComparator(Genome g){ this.g = g; }
    public int compare(Object o_a, Object o_b)
    {
    	Backbone a = (Backbone) o_a;
    	Backbone b = (Backbone) o_b;
    	boolean a_def = a.getSeqs()[g.getSourceIndex()];
    	boolean b_def = b.getSeqs()[g.getSourceIndex()];
    	if(!a_def && !b_def)
    		return 0;
    	if(!a_def)
    		return -1;
    	if(!b_def)
    		return 1;
    	long a_lend = a.getLeftEnd(g);
    	long b_lend = b.getLeftEnd(g);
    	if( a_lend == b_lend )
    		return (int)(a.getRightEnd(g) - b.getRightEnd(g));
        return (int)(a_lend - b_lend);
    }
}

public class BackboneList {
	protected Vector seq_bb;
	protected Backbone[] bb_array;
	protected XMFAAlignment xmfa;
	public void setXmfa(XMFAAlignment xmfa){ this.xmfa = xmfa; }
	public void setBackbone(Backbone[] bb_array){ this.bb_array = bb_array; }
	public void setSeqBackbone(Vector seq_bb){ this.seq_bb = seq_bb; }
	public Backbone getNextBackbone( Genome g, long position )
	{
		Backbone[] seq_bb_array = (Backbone[])seq_bb.elementAt(g.getSourceIndex());
		if(seq_bb_array.length == 0)
			return null;
		if(position < 1)
			position = 1;
		if(position > g.getLength())
			position = g.getLength();
		long[] lcb_and_column = xmfa.getLCBAndColumn(g,position);
		Backbone key = new Backbone();
		key.setLcbIndex((int)lcb_and_column[0]);
		key.setLeftColumn(lcb_and_column[1]);
		key.setLength(0);
		boolean[] seqs = new boolean[seq_bb.size()];
		seqs[g.getSourceIndex()] = true;
		key.setSeqs(seqs);
		long[] left_ends = new long[seq_bb.size()];
		left_ends[g.getSourceIndex()] = position;
		key.setLeftEnd(left_ends);
		key.setRightEnd(left_ends);
		BbLeftEndComparator comp = new BbLeftEndComparator(g);
		int indie = Arrays.binarySearch(seq_bb_array, key, comp);
		if( indie < 0 )
		{
			indie = -indie-1;
			// scan backwards to see whether the position was contained in any previous
			// bb segments
			if( indie >= seq_bb_array.length || position < seq_bb_array[indie].getLeftEnd(g) )
			{
				int prev_i = indie - 1;
				while(prev_i >= 0)
				{
					if( seq_bb_array[prev_i].getLeftEnd(g) <= position &&
							position <= seq_bb_array[prev_i].getRightEnd(g))
					{
							indie = prev_i;	// contained by the previous bb seg
							break;
					}
					if( prev_i > 0 && seq_bb_array[prev_i].getLeftEnd(g) == seq_bb_array[prev_i-1].getLeftEnd(g) )
						prev_i--;
					else
						break;
				}
			}
		}

		if(indie == seq_bb_array.length)
			return null;
		return seq_bb_array[indie];
	}
	public Backbone getBackbone( Genome g, long position )
	{
		Backbone bb = getNextBackbone(g,position);
		if( bb != null && bb.getLeftEnd(g) <= position &&
			position <= bb.getRightEnd(g))
			return bb;
		return null;
	}
	public Backbone[] getBackboneArray(){ return bb_array; }
}
