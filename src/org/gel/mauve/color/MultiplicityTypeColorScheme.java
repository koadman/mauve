package org.gel.mauve.color;

import java.awt.Color;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.ColorScheme;
import org.gel.mauve.Genome;
import org.gel.mauve.Match;

/**
 * Color matches by their multiplicity and the sequences they match in. Will not
 * work with more than 62 sequences.
 */
public class MultiplicityTypeColorScheme implements ColorScheme
{

    public void apply(BaseViewerModel model)
    {
        if (model.getSequenceCount() > 62)
        {
            throw new RuntimeException(" Can't color by multiplicity type with more than 62 sequences.");
        }

        double mult_range = Math.pow(2, model.getSequenceCount());

        for (int matchI = 0; matchI < model.getMatchCount(); matchI++)
        {
            Match cur_match = model.getMatch(matchI);

            // color the match based on its multiplicity.
            long color_type = 0;
            for (int seqI = 0; seqI < model.getSequenceCount(); seqI++)
            {
                Genome g = model.getGenomeByViewingIndex(seqI);
                color_type <<= 1;
                if (cur_match.getStart(g) != Match.NO_MATCH)
                {
                    color_type |= 1;
                }
            }
            double hue = (double) color_type / mult_range;
            cur_match.color = Color.getHSBColor((float) hue, MATCH_SAT, MATCH_BRIGHT);
        }
    }

    public String toString()
    {
        return "Multiplicity type";
    }

}