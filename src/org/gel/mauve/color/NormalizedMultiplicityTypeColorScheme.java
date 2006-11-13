package org.gel.mauve.color;

import java.awt.Color;
import java.util.Comparator;
import java.util.Vector;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.ColorScheme;
import org.gel.mauve.Match;

/**
 * Color matches by their multiplicity and the sequences they match in. Will not
 * work with more than 62 sequences.
 */
public class NormalizedMultiplicityTypeColorScheme implements ColorScheme
{
    private final static Comparator MULTIPLICITY_TYPE_COMPARATOR = new Comparator()
    {
        public int compare(Object o1, Object o2)
        {
            Match a = (Match) o1;
            Match b = (Match) o2;
            return (int) (a.multiplicityType() - b.multiplicityType());
        }
    };

    public void apply(BaseViewerModel model)
    {

        if (model.getSequenceCount() > 62)
        {
            throw new RuntimeException("Can't color by multiplicity type with more than 62 sequences.");
        }

        if (model.getMatchCount() == 0)
            return; // no sense in trying to color what's not there!

        Vector matchesByMT = null;
        long unique_mt_count = 0;
        if (matchesByMT == null)
        {
            matchesByMT = model.sortedMatches(MULTIPLICITY_TYPE_COMPARATOR);
            unique_mt_count = 1;
            long last_mt = ((Match) matchesByMT.get(0)).multiplicityType();
            // go thru the sorted list in order counting the number of different
            // multiplicity types
            for (int matchI = 1; matchI < matchesByMT.size(); matchI++)
            {
                long cur_mt = ((Match) matchesByMT.get(matchI)).multiplicityType();
                if (cur_mt != last_mt)
                    unique_mt_count++;
                last_mt = cur_mt;
            }
        }

        long cur_mt_count = 0;
        long prev_mt = ((Match) matchesByMT.get(0)).multiplicityType();
        // go thru the sorted list in order assigning a new color to each new
        // offset
        for (int matchI = 0; matchI < matchesByMT.size(); matchI++)
        {
            long cur_mt = ((Match) matchesByMT.get(matchI)).multiplicityType();
            if (cur_mt != prev_mt)
                cur_mt_count++;
            prev_mt = cur_mt;

            // map the generalized offset of this match to a hue
            Match cur_match = (Match) matchesByMT.get(matchI);
            float hue = (float) ((double) cur_mt_count / (double) unique_mt_count);
            cur_match.color = Color.getHSBColor(hue, MATCH_SAT, MATCH_BRIGHT);
        }
    }

    public String toString()
    {
        return "Normalized multiplicity type";
    }

}