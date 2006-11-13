/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.gel.mauve.gui.sequence;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import org.biojava.bio.gui.sequence.AbstractBeadRenderer;
import org.biojava.bio.gui.sequence.SequenceRenderContext;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;

/**
 * <p><code>RectangularBeadRenderer</code> renders features as simple
 * rectangles. Their outline and fill <code>Paint</code>,
 * <code>Stroke</code>, feature depth, Y-axis displacement are
 * configurable. The height of the rectangle will be equal to half its
 * width, but not greater than the <code>beadDepth</code> set in the
 * constructor.</p>
 *
 * <p>An alternative bead height behaviour is available where the
 * rectangle height does not scale with its current width. The
 * <code>setHeightScaling</code> method should be passed a boolean
 * value to change this. The default is to use height scaling.</p>
 *
 * @author Keith James
 * @author Aaron Darling
 */
public class ZiggyRectangularBeadRenderer extends AbstractBeadRenderer
{
    /**
     * Constant <code>HEIGHTSCALING</code> indicating a change to the
     * feature height scaling policy.
     */
    public static final ChangeType HEIGHTSCALING =
	new ChangeType("The height scaling policy of the features has changed",
		       "org.gel.mauve.gui.sequence.ZiggyRectangularBeadRenderer",
		       "HEIGHTSCALING", SequenceRenderContext.LAYOUT);

    protected Rectangle2D rect;
    protected boolean scaleHeight;

    /**
     * Creates a new <code>ZiggyRectangularBeadRenderer</code> with the
     * default settings.
     */
    public ZiggyRectangularBeadRenderer()
    {
        super();
        rect = new Rectangle2D.Double();
        scaleHeight = true;
    }

    /**
     * Creates a new <code>ZiggyRectangularBeadRenderer</code>.
     *
     * @param beadDepth a <code>double</code>.
     * @param beadDisplacement a <code>double</code>.
     * @param beadOutline a <code>Paint</code>.
     * @param beadFill a <code>Paint</code>.
     * @param beadStroke a <code>Stroke</code>.
     */
    public ZiggyRectangularBeadRenderer(double beadDepth,
                                   double beadDisplacement,
                                   Paint  beadOutline,
                                   Paint  beadFill,
                                   Stroke beadStroke)
    {
        super(beadDepth, beadDisplacement, beadOutline, beadFill, beadStroke);
        rect = new Rectangle2D.Double();
        scaleHeight = true;
    }

    /* TODO: Implement scaling */
    public void renderBead(
    	    Graphics2D g, Feature f, SequenceRenderContext context
    	  ) {
    	    Location loc = f.getLocation();
    	    Iterator i = loc.blockIterator();
    	    Location last = null;
    	    if(i.hasNext()) {
    	      last = (Location) i.next();
    	      renderLocation(g, last, context);
    	    }
    	    while(i.hasNext()) {
    	      Location next = (Location) i.next();
    	      renderLink(g, f, last, next, context);
    	      renderLocation(g, next, context);
    	      last = next;
    	    }
    	  }

    	  private void renderLocation(
    	    Graphics2D g, Location loc, SequenceRenderContext context
    	  ) {
    	    Rectangle2D.Double block = new Rectangle2D.Double();
    	    double min = context.sequenceToGraphics(loc.getMin());
    	    double max = context.sequenceToGraphics(loc.getMax()+1);
    	    if(context.getDirection() == SequenceRenderContext.HORIZONTAL) {
    	      block.setFrame(
    	        min, beadDisplacement,
    	        max - min, beadDepth
    	      );
    	    } else {
    	      block.setFrame(
    	    		  beadDisplacement, min,
    	    		  beadDepth, max - min
    	      );
    	    }
    	    if(beadFill != null) {
    	      g.setPaint(beadFill);
    	      g.fill(block);
    	    }
    	    if(beadOutline != null) {
    	    	g.setStroke(beadStroke);
    	    	g.setPaint(beadOutline);
    	      	g.draw(block);
    	    }
    	  }

    	    private StrandedFeature.Strand getStrand(Feature f) {
    		if (f instanceof StrandedFeature) {
    		    return ((StrandedFeature) f).getStrand();
    		} else {
    		    FeatureHolder fh = f.getParent();
    		    if (fh instanceof Feature) {
    			return getStrand((Feature) fh);
    		    } else {
    			return StrandedFeature.UNKNOWN;
    		    }
    		}
    	    }

    	  private void renderLink(
    	    Graphics2D g, Feature f, Location source, Location dest,
    	    SequenceRenderContext context
    	  ) {
    	    Line2D line = new Line2D.Double();
    	    Point2D startP;
    	    Point2D midP;
    	    Point2D endP;
    	    double half = beadDisplacement + beadDepth * 0.5;
    	    if(context.getDirection() == SequenceRenderContext.HORIZONTAL) {
    	      if(getStrand(f) == StrandedFeature.NEGATIVE) {
    	        double start = context.sequenceToGraphics(dest.getMin());
    	        double end = context.sequenceToGraphics(source.getMax()+1);
    	        double mid = (start + end) * 0.5;
    	        startP = new Point2D.Double(start, half);
    	        midP   = new Point2D.Double(mid,   beadDisplacement + beadDepth);
    	        endP   = new Point2D.Double(end,   half);
    	      } else {
    	        double start = context.sequenceToGraphics(source.getMax()+1);
    	        double end = context.sequenceToGraphics(dest.getMin());
    	        double mid = (start + end) * 0.5;
    	        startP = new Point2D.Double(start, half);
    	        midP   = new Point2D.Double(mid,   beadDisplacement);
    	        endP   = new Point2D.Double(end,   half);
    	      }
    	    } else {
    	      if (getStrand(f) == StrandedFeature.NEGATIVE) {
    	        double start = context.sequenceToGraphics(dest.getMin()+1);
    	        double end = context.sequenceToGraphics(source.getMax());
    	        double mid = (start + end) * 0.5;
    	        startP = new Point2D.Double(half,       start);
    	        midP   = new Point2D.Double(beadDisplacement + beadDepth, mid);
    	        endP   = new Point2D.Double(half,       end);
    	      } else {
    	        double start = context.sequenceToGraphics(source.getMax());
    	        double end = context.sequenceToGraphics(dest.getMin()+1);
    	        double mid = (start + end) * 0.5;
    	        startP = new Point2D.Double(half, start);
    	        midP   = new Point2D.Double(beadDisplacement,  mid);
    	        endP   = new Point2D.Double(half, end);
    	      }
    	    }
    	    g.setStroke(beadStroke);
    	    g.setPaint(beadOutline);
    	    line.setLine(startP, midP);
    	    g.draw(line);
    	    line.setLine(midP, endP);
    	    g.draw(line);
    	  }

    /**
     * <code>getDepth</code> calculates the depth required by this
     * renderer to display its beads.
     *
     * @param context a <code>SequenceRenderContext</code>.
     *
     * @return a <code>double</code>.
     */
    public double getDepth(SequenceRenderContext context)
    {
        // Get max depth of delegates using base class method
        double maxDepth = super.getDepth(context);
        return Math.max(maxDepth, (beadDepth + beadDisplacement));
    }

    /**
     * <code>getHeightScaling</code> returns the state of the height
     * scaling policy.
     *
     * @return a <code>boolean</code> true if height scaling is
     * enabled.
     */
    public boolean getHeightScaling()
    {
        return scaleHeight;
    }

    /**
     * <code>setHeightScaling</code> sets the height scaling
     * policy. Default behaviour is for this to be enabled leading to
     * features being drawn with a height equal to half their width,
     * subject to a maximum height restriction equal to the
     * <code>beadDepth</code> set in the constructor. If disabled,
     * features will always be drawn at the maximum height allowed by
     * the <code>beadDepth</code> parameter.
     *
     * @param isEnabled a <code>boolean</code>.
     *
     * @exception ChangeVetoException if an error occurs.
     */
    public void setHeightScaling(boolean isEnabled) throws ChangeVetoException
    {
        if (hasListeners())
	{
	    ChangeSupport cs = getChangeSupport(SequenceRenderContext.LAYOUT);
	    synchronized(cs)
	    {
		ChangeEvent ce = new ChangeEvent(this, SequenceRenderContext.LAYOUT,
						 null, null,
						 new ChangeEvent(this, HEIGHTSCALING,
								 new Boolean(scaleHeight),
								 new Boolean(isEnabled)));
		cs.firePreChangeEvent(ce);
                scaleHeight = isEnabled;
		cs.firePostChangeEvent(ce);
	    }
	}
	else
	{
            scaleHeight = isEnabled;
	}
    }
}
