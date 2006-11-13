package org.gel.mauve;

import java.awt.Color;
import java.io.Serializable;

/**
 * Records an ungapped local alignment among multiple sequences. Sequence
 * coordinates start at 1 and a coordinate of 0 indicates that the alignment is
 * undefined in that sequence.
 */
public class Match implements Serializable
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
    private long[] starts;
    // The lengths of this match in each sequence
    private long[] lengths;
    // The direction of each match. false is forward, true is reverse
    private boolean[] reverse;

    public Match(int sequenceCount)
    {
        starts = new long[sequenceCount];
        lengths = new long[sequenceCount];
        reverse = new boolean[sequenceCount];
    }

    public Match(Match m)
    {
        starts = new long[m.starts.length];
        lengths = new long[m.lengths.length];
        reverse = new boolean[m.reverse.length];
        System.arraycopy(m.starts, 0, starts, 0, starts.length);
        System.arraycopy(m.lengths, 0, lengths, 0, lengths.length);
        System.arraycopy(m.reverse, 0, reverse, 0, reverse.length);

        color = m.color;
        lcb = m.lcb;
    }

    public long getStart(Genome g)
    {
        return starts[g.getSourceIndex()]; 
    }
    
    /**
     * @deprecated
     * @param sourceIndex
     * @return
     */
    public long getStart(int sourceIndex)
    {
        return starts[sourceIndex];
    }
    
    public void setStart(Genome g, long start)
    {
        starts[g.getSourceIndex()] = start;
    }
    
    /**
     * @deprecated
     * @param sourceIndex
     * @param start
     */public void setStart(int sourceIndex, long start)
    {
        starts[sourceIndex] = start;
    }
    
    public long getLength(Genome g)
    {
        return lengths[g.getSourceIndex()];
    }
    
    /**
     * @deprecated
     * @param sourceIndex
     * @return
     */
    public long getLength(int sourceIndex)
    {
        return lengths[sourceIndex];
    }
    
    public void setLength(Genome g, long length)
    {
        lengths[g.getSourceIndex()] = length;
    }
    
    /**
     * @deprecated
     * @param sourceIndex
     * @param length
     */
    public void setLength(int sourceIndex, long length)
    {
        lengths[sourceIndex] = length;
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
        for (; seqI < starts.length; seqI++)
        {
            if (starts[seqI] != NO_MATCH)
                break;
        }
        ref = starts[seqI];
        for (; seqI < starts.length; seqI++)
        {
            long cur_start = starts[seqI];
            if (reverse[seqI])
                cur_start = -cur_start;
            g_offset += ref - cur_start;
        }

        return g_offset;
    }

    /** format the ungapped local alignment coordinates into a string */
    public String toString()
    {
        String rval = new String();
        for (int seqI = 0; seqI < starts.length; seqI++)
        {
            rval += "<";
            if (starts[seqI] != NO_MATCH)
            {
                if (reverse[seqI])
                    rval += "-";
                rval += starts[seqI];
                rval += ",";
                rval += lengths[seqI];
            }
            rval += "> ";
        }
        return rval;
    }

    /**
     * compute a number indicating which genomes the local alignment is defined
     * in. This is a binary number where a 1 indicates that the match is defined
     * and a 0 indicates it is undefined. For example, a local alignment shared
     * by the first and the fourth (out of five) genomes would be represented as
     * 10010.
     */
    public long multiplicityType()
    {
        // determine the match's multiplicity type.
        long mult_type = 0;
        for (int seqI = 0; seqI < starts.length; seqI++)
        {
            mult_type <<= 1;
            if (starts[seqI] != Match.NO_MATCH)
                mult_type |= 1;
        }
        return mult_type;
    }

    public void copyArrays(LCB lcb, long[] starts, long[] lengths, boolean[] reverse, int seq_count)
    {
        System.arraycopy(this.starts, 0, starts, 0, seq_count);
        System.arraycopy(this.lengths, 0, lengths, 0, seq_count);
        System.arraycopy(this.reverse, 0, reverse, 0, seq_count);
    }

}