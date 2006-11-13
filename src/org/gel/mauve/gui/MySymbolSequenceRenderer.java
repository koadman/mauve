package org.gel.mauve.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.biojava.bio.BioRuntimeException;
import org.biojava.bio.gui.sequence.GUITools;
import org.biojava.bio.gui.sequence.SequenceRenderContext;
import org.biojava.bio.gui.sequence.SequenceRenderer;
import org.biojava.bio.gui.sequence.SequenceViewerEvent;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.SymbolList;

/**
 * A slightly revised version of Biojava's SymbolSequenceRenderer, which prints
 * symbols near center of range for location, and offset a little further down.
 */
public class MySymbolSequenceRenderer implements SequenceRenderer
{
    private double depth = 51.0;
    private Paint outline;

    public MySymbolSequenceRenderer()
    {
        outline = Color.black;
    }

    public double getDepth(SequenceRenderContext context)
    {
        return depth + 1.0;
    }

    public double getMinimumLeader(SequenceRenderContext context)
    {
        return 0.0;
    }

    public double getMinimumTrailer(SequenceRenderContext context)
    {
        return 0.0;
    }

    public void paint(Graphics2D g2, SequenceRenderContext context)
    {
        Rectangle2D prevClip = g2.getClipBounds();
        AffineTransform prevTransform = g2.getTransform();

        g2.setPaint(outline);

        Font font = context.getFont();

        Rectangle2D maxCharBounds = font.getMaxCharBounds(g2.getFontRenderContext());

        double scale = context.getScale();

        if (scale >= (maxCharBounds.getWidth() * 0.3) && scale >= (maxCharBounds.getHeight() * 0.3))
        {
            double xFontOffset = 0.0;
            double yFontOffset = 0.0;

            // These offsets are not set quite correctly yet. The
            // Rectangle2D from getMaxCharBounds() seems slightly
            // off. The "correct" application of translations based on
            // the Rectangle2D seem to give the wrong results. The
            // values below are mostly fudges.
            if (context.getDirection() == SequenceRenderContext.HORIZONTAL)
            {
                xFontOffset = maxCharBounds.getCenterX() * 0.2;
                yFontOffset = -maxCharBounds.getCenterY() + (depth * 0.5);
            }
            else
            {
                xFontOffset = -maxCharBounds.getCenterX() + (depth * 0.5);
                yFontOffset = -maxCharBounds.getCenterY() * 3.0;
            }

            SymbolList seq = context.getSymbols();
            SymbolTokenization toke = null;
            try
            {
                toke = seq.getAlphabet().getTokenization("token");
            }
            catch (Exception e)
            {
                throw new BioRuntimeException(e);
            }

            Location visible = GUITools.getVisibleRange(context, g2);
            for (int sPos = visible.getMin(); sPos <= visible.getMax(); sPos++)
            {
                double gPos = (context.sequenceToGraphics(sPos) + context.sequenceToGraphics(sPos + 1)) / 2.0;
                String s = "?";
                try
                {
                    s = toke.tokenizeSymbol(seq.symbolAt(sPos));
                }
                catch (Exception ex)
                {
                    // We'll ignore the case of not being able to tokenize it
                }

                if (context.getDirection() == SequenceRenderContext.HORIZONTAL)
                {
                    g2.drawString(s, (float) (gPos + xFontOffset), (float) yFontOffset);
                }
                else
                {
                    g2.drawString(s, (float) xFontOffset, (float) (gPos + yFontOffset));
                }
            }
        }

        g2.setClip(prevClip);
        g2.setTransform(prevTransform);
    }

    public SequenceViewerEvent processMouseEvent(SequenceRenderContext context, MouseEvent me, List path)
    {
        path.add(this);
        int sPos = context.graphicsToSequence(me.getPoint());
        return new SequenceViewerEvent(this, null, sPos, me, path);
    }
}