package org.gel.mauve.color;

import java.awt.Color;
import java.util.Comparator;
import java.util.Vector;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.ColorScheme;
import org.gel.mauve.Match;

/**
 * Colors the matches based on their generalized offsets using the entire color
 * spectrum evenly. Uses a very simple linear mapping approach for now. It can
 * be tricked up later.
 */
public class NormalizedOffsetColorScheme implements ColorScheme
{
    private final static Comparator MATCH_OFFSET_COMPARATOR = new Comparator()
    {
        public int compare(Object o1, Object o2)
        {
            Match a = (Match) o1;
            Match b = (Match) o2;
            return (int) (a.offset() - b.offset());
        }
    };
    private Vector matchesByOffset;
    private long uniqueOffsetCount;

    public void apply(BaseViewerModel model)
    {
        if (model.getMatchCount() == 0)
            return; // no sense in trying to color what's not there!

        Vector matchesByOffset = getMatchesByOffset(model);

        double spectrum_bump = SPECTRUM_GAP / 2d;
        long cur_offset_count = 0;
        long prev_offset = ((Match) matchesByOffset.get(0)).offset();
        // go thru the sorted list in order assigning a new color to each new
        // offset
        for (int matchI = 0; matchI < matchesByOffset.size(); matchI++)
        {
            long cur_offset = ((Match) matchesByOffset.get(matchI)).offset();
            if (cur_offset != prev_offset)
                cur_offset_count++;
            prev_offset = cur_offset;

            // map the generalized offset of this match to a hue
            //			Match cur_match = (Match) matchVector.elementAt( matchI );
            Match cur_match = (Match) matchesByOffset.get(matchI);
            float hue = (float) (((1d - SPECTRUM_GAP) * ((double) cur_offset_count / (double) uniqueOffsetCount)) + spectrum_bump);
            cur_match.color = Color.getHSBColor(hue, MATCH_SAT, MATCH_BRIGHT);
        }
    }

    /**
     * @return Returns the list of the matches sorted by their generalized
     *         offset.
     */
    private Vector getMatchesByOffset(BaseViewerModel model)
    {
        // if the array of matches sorted by offset hasn't been created then
        // create it!
        if (matchesByOffset == null)
        {
            matchesByOffset = model.sortedMatches(MATCH_OFFSET_COMPARATOR);
            this.uniqueOffsetCount = 1;
            long last_offset = ((Match) matchesByOffset.get(0)).offset();
            // go thru the sorted list in order counting the number of different
            // offsets
            for (int matchI = 1; matchI < matchesByOffset.size(); matchI++)
            {
                long cur_offset = ((Match) matchesByOffset.get(matchI)).offset();
                if (cur_offset != last_offset)
                {
                    this.uniqueOffsetCount = uniqueOffsetCount + 1;
                }
                last_offset = cur_offset;
            }
        }
        return matchesByOffset;
    }

    public String toString()
    {
        return "Normalized Offset";
    }
}