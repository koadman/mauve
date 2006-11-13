package org.gel.mauve;

import java.util.Comparator;


/**
 * Compare the start coordinates of ungapped local alignments in a particular
 * sequence Note: this comparator imposes orderings that are inconsistent with
 * the equals operator.
 */

public class MatchStartComparator implements Comparator
{
    private Genome g;

    public MatchStartComparator(Genome g)
    {
        this.g = g;
    }

    public MatchStartComparator(MatchStartComparator m)
    {
        g = m.g;
    }

    public int compare(Object o1, Object o2)
    {
        Match a = (Match) o1;
        Match b = (Match) o2;

        long rval = a.getStart(g) - b.getStart(g);
        if (rval < 0)
        {
            return -1;
        }
        return rval == 0 ? 0 : 1;
    }

    public boolean equals(Object c)
    {
        if (c == null)
            return false;

        if (c instanceof MatchStartComparator)
        {
            return g == ((MatchStartComparator) c).g;
        }
        else
        {
            return false;
        }
    }
}

