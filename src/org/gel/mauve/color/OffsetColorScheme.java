package org.gel.mauve.color;

import java.awt.Color;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.ColorScheme;
import org.gel.mauve.Match;

/**
 * Colors the matches based on their generalized offsets. Uses a very simple
 * linear mapping.
 * 
 * @see org.gel.mauve.color.NormalizedOffsetColorScheme for a different
 *      approach.
 */
public class OffsetColorScheme implements ColorScheme
{
    private boolean initialized = false;
    private long maxOffset = Long.MIN_VALUE;
    private long minOffset = Long.MAX_VALUE;

    public void apply(BaseViewerModel model)
    {
        if (!initialized)
        {
            initOffsets(model);
            initialized = true;
        }

        // color by generalized offset (COLOR_OFFSET)
        long offset_range = maxOffset - minOffset;
        // bump offset_range up by a designated amount to stay out of the deep
        // violet part of the spectrum
        offset_range += (offset_range * SPECTRUM_GAP);
        long offset_bump = (long) (offset_range * (SPECTRUM_GAP / 2));
        for (int matchI = 0; matchI < model.getMatchCount(); matchI++)
        {
            // map the generalized offset of this match to a hue
            Match cur_match = model.getMatch(matchI);
            long m_offset = cur_match.offset() - minOffset + offset_bump;
            float hue = (float) m_offset / (float) offset_range;
            cur_match.color = Color.getHSBColor(hue, MATCH_SAT, MATCH_BRIGHT);
        }
    }

    private void initOffsets(BaseViewerModel model)
    {
        for (int matchI = 0; matchI < model.getMatchCount(); matchI++)
        {
            Match match = model.getMatch(matchI);
            long offset = match.offset();
            maxOffset = offset > maxOffset ? offset : maxOffset;
            minOffset = offset < minOffset ? offset : minOffset;
        }
    }

    public String toString()
    {
        return "Offset";
    }

}