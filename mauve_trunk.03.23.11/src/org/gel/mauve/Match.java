package org.gel.mauve;

import java.awt.Color;
import java.io.Serializable;

import org.gel.mauve.analysis.Segment;

/**
 * Records an ungapped local alignment among multiple sequences. Sequence
 * coordinates start at 1 and a coordinate of 0 indicates that the alignment is
 * undefined in that sequence.
 */
public class Match extends Segment //implements Serializable
{
	static final long serialVersionUID = 1;
    public static final int NO_MATCH = 0;
    // The color code of this match
    public Color color;
    // true if the match is highlighted on screen
    public boolean highlighted = false;
    // The lcb which this match belongs to
    public int lcb = 0;

    // The start coordinate of this match in each sequence
    //private long[] starts;
    // The lengths of this match in each sequence
    //private long[] ends;
    // The direction of each match. false is forward, true is reverse
    //private boolean[] reverse;

    public Match(int sequenceCount)
    {
        left = new long[sequenceCount];
        right = new long[sequenceCount];
        reverse = new boolean[sequenceCount];
    }

    public Match(Match m)
    {
        left = new long[m.left.length];
        right = new long[m.right.length];
        reverse = new boolean[m.reverse.length];
        System.arraycopy(m.left, 0, left, 0, left.length);
        System.arraycopy(m.right, 0, right, 0, right.length);
        System.arraycopy(m.reverse, 0, reverse, 0, reverse.length);

        color = m.color;
        lcb = m.lcb;
    }

    public long getStart(Genome g)
    {
        return left[g.getSourceIndex()]; 
    }
    
    /**
     * @deprecated
     * @param sourceIndex
     * @return
     */
    public long getStart(int sourceIndex)
    {
        return left[sourceIndex];
    }
    
    public void setStart(Genome g, long start)
    {
        left[g.getSourceIndex()] = start;
    }
    
    /**
     * @deprecated
     * @param sourceIndex
     * @param start
     */public void setStart(int sourceIndex, long start)
    {
        left[sourceIndex] = start;
    }
    
    public long getLength(Genome g)
    {
        return right[g.getSourceIndex()];
    }
    
    /**
     * @deprecated
     * @param sourceIndex
     * @return
     */
    public long getLength(int sourceIndex)
    {
        return right[sourceIndex];
    }
    
    public void setLength(Genome g, long length)
    {
        right[g.getSourceIndex()] = length;
    }
    
    /**
     * @deprecated
     * @param sourceIndex
     * @param length
     */
    public void setLength(int sourceIndex, long length)
    {
        right[sourceIndex] = length;
    }
    
    public boolean getReverse(Genome g)
    {
        return reverse[g.getSourceIndex()];
    }
    

    /**
     * @deprecated
     * @param sourceIndex
     * @return
     */public boolean getReverse(int sourceIndex)
    {
        return reverse[sourceIndex];
    }
    
    public void setReverse(Genome g, boolean r)
    {
        reverse[g.getSourceIndex()] = r;
    }
    
    /**
     * @deprecated
     * @param sourceIndex
     * @param r
     */
    public void setReverse(int sourceIndex, boolean r)
    {
        reverse[sourceIndex] = r;
    }
    
    /** Compute and return the generalized offset */
    public long offset()
    {
        int seqI = 0;
        long ref;
        long g_offset = 0;
        for (; seqI < left.length; seqI++)
        {
            if (left[seqI] != NO_MATCH)
                break;
        }
        ref = left[seqI];
        for (; seqI < left.length; seqI++)
        {
            long cur_start = left[seqI];
            if (reverse[seqI])
                cur_start = -cur_start;
            g_offset += ref - cur_start;
        }

        return g_offset;
    }

    public void copyArrays(LCB lcb, long[] starts, long[] lengths, boolean[] reverse, int seq_count)
    {
        System.arraycopy(this.left, 0, starts, 0, seq_count);
        System.arraycopy(this.right, 0, lengths, 0, seq_count);
        System.arraycopy(this.reverse, 0, reverse, 0, seq_count);
    }

}