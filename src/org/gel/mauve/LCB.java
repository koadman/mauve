package org.gel.mauve;

import java.awt.Color;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

/**
 * The LCB class tracks locally collinear blocks: regions of homologous sequence
 * that do not contain rearrangements.
 */
public class LCB implements Serializable
{
	static final long serialVersionUID = 1;
    // The left end position of the LCB in each sequence
    private long[] left_end;

    // The right end position of the LCB in each sequence
    private long[] right_end;

    // 'Pointers' (actually IDs) to the LCBs on the left in each sequence
    private int[] left_adjacency;
    
    // 'Pointers' (actually IDs) to the LCBs on the right in each sequence
    private int[] right_adjacency;

    // The orientation of the LCB in each sequence
    private boolean[] reverse;

    // A numerical ID that can be assigned to this LCB
    public int id;

    // The weight (or coverage) of this LCB
    public long weight;

    // The color of the LCB frame
    public Color color;
    
    // The color of matches within the LCB
    public Color match_color;

    // set this to true to keep this LCB even when it's weight is too low
    boolean keep;


    public LCB(int seq_count)
    {
        left_end = new long[seq_count];
        right_end = new long[seq_count];
        reverse = new boolean[seq_count];
        left_adjacency = new int[seq_count];
        right_adjacency = new int[seq_count];
    }

    public LCB(Match m, int id, int seq_count)
    {
        left_end = new long[seq_count];
        right_end = new long[seq_count];
        reverse = new boolean[seq_count];
        left_adjacency = new int[seq_count];
        right_adjacency = new int[seq_count];
        this.id = id;
        
        m.copyArrays(this, left_end, right_end, reverse, seq_count);
        
        // set weight to average lcb length for now...
        long len_sum = 0;
        for (int seqI = 0; seqI < seq_count; seqI++)
        {
            len_sum += right_end[seqI] - left_end[seqI];
        }
        weight = len_sum / seq_count;
        keep = false;
    }
    
    public LCB(LCB l)
    {
        int seq_count = l.left_end.length;
        left_end = new long[seq_count];
        right_end = new long[seq_count];
        left_adjacency = new int[seq_count];
        right_adjacency = new int[seq_count];
        reverse = new boolean[seq_count];
        id = l.id;
        weight = l.weight;
        color = l.color;
        match_color = l.match_color;
        keep = l.keep;

        System.arraycopy(l.left_end, 0, left_end, 0, seq_count);
        System.arraycopy(l.right_end, 0, right_end, 0, seq_count);
        System.arraycopy(l.left_adjacency, 0, left_adjacency, 0, seq_count);
        System.arraycopy(l.right_adjacency, 0, right_adjacency, 0, seq_count);
        System.arraycopy(l.reverse, 0, reverse, 0, seq_count);
    }

    public long midpoint(Genome g)
    {
        return (right_end[g.getSourceIndex()] + left_end[g.getSourceIndex()]) / 2;
    }

    public void setReference(Genome g)
    {
        if (getReverse(g))
        {
            for (int seqI = 0; seqI < reverse.length; seqI++)
            {
                Genome g2 = g.getModel().getGenomeBySourceIndex(seqI);
                setReverse(g2, !getReverse(g2));
            }
        }
    }

    public long getLength(Genome g)
    {
        return right_end[g.getSourceIndex()] - left_end[g.getSourceIndex()];
    }
    
    public long getLeftEnd(Genome g)
    {
        return left_end[g.getSourceIndex()];
    }
    
    public void setLeftEnd(Genome g, long leftEnd)
    {
        left_end[g.getSourceIndex()] = leftEnd;
    }
    
    public long getRightEnd(Genome g)
    {
        return right_end[g.getSourceIndex()];
    }
    
    public void setRightEnd(Genome g, long rightEnd)
    {
        right_end[g.getSourceIndex()] = rightEnd;
    }
    
    public boolean getReverse(Genome g)
    {
        return reverse[g.getSourceIndex()];
    }
    
    public void setReverse(Genome g, boolean r)
    {
        reverse[g.getSourceIndex()] = r;
    }
    
    public int getLeftAdjacency(Genome g)
    {
        return left_adjacency[g.getSourceIndex()];
    }
    
    public void setLeftAdjacency(Genome g, int lcbID)
    {
        left_adjacency[g.getSourceIndex()] = lcbID;
    }
    
    public int getRightAdjacency(Genome g)
    {
        return right_adjacency[g.getSourceIndex()];
    }

    public void setRightAdjacency(Genome g, int lcbID)
    {
        right_adjacency[g.getSourceIndex()] = lcbID;
    }

    public void resetAdjacencies(int genomeCount)
    {
        Arrays.fill(left_adjacency, 0);
        Arrays.fill(right_adjacency, 0);
    }
    
    public int multiplicity()
    {
    	int mult = 0;
    	for(int i = 0; i < left_end.length; ++i)
    	{
    		if( left_end[i] != 0 )
    			mult++;
    	}
    	return mult;
    }
}

/**
 * Compares left end of LCBs.
 */
class LCBLeftComparator implements Comparator
{
    private Genome g;
    
    LCBLeftComparator(Genome g)
    {
        this.g = g;
    }

    public int compare(Object o_a, Object o_b)
    {

        LCB a = (LCB) o_a;
        LCB b = (LCB) o_b;

        long a_start = a.getLeftEnd(g);
        long b_start = b.getLeftEnd(g);
        if (a_start == 0 || b_start == 0)
        {
            if (b_start != 0)
                return 1;
            return -1;
        }

        long diff = a_start - b_start;
        return (int) diff;
    }

}


/**
 * Compares LCB ids.
 */
class LcbIdComparator implements Comparator
{
    public int compare(Object o_a, Object o_b)
    {

        LCB a = (LCB) o_a;
        LCB b = (LCB) o_b;
        return a.id - b.id;
    }
}