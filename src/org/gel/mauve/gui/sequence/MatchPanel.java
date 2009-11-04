package org.gel.mauve.gui.sequence;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Rectangle2D;
import java.util.EventListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.EventListenerList;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;
import org.gel.mauve.HighlightListener;
import org.gel.mauve.LCB;
import org.gel.mauve.LcbViewerModel;
import org.gel.mauve.Match;
import org.gel.mauve.ModelEvent;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.backbone.Backbone;
import org.gel.mauve.backbone.BackboneList;
import org.gel.mauve.gui.MauveRenderingHints;
import org.gel.mauve.gui.RearrangementPanel;

public class MatchPanel extends AbstractSequencePanel implements MouseListener, HighlightListener
{
    // The largest number of matches which will be shown in a popup menu
    private static final Color DELETED_COLOR = Color.getHSBColor(0.11f, 1, 1);
    private static final int MAX_POPUP_MATCHES = 15;
    // Color for the area on top and bottom around matches.
    private static final Color highlightAreaColor = new Color(24, 24, 24);
    
    // Size of region on top/bottom.
    private static final int HIGHLIGHT_AREA_HEIGHT = 2;

    // Thickness of LCB border.
    private static final double LCB_BOUNDARY_WIDTH = 2.25d;

    // Half of thickness of LCB border.
    private static final double HALF_PEN_WIDTH = 1.125d;
    
    // The last visible coordinate (a convenience)
    private long viewEnd;

    // The buffered image.
    private Image bufferedImage;

    // Last width and height when bufferedImage was updated.
    private int lastWidth = -1;
    private int lastHeight = -1;
    
    // The rearrangement panel that contains this sequence panel
    RearrangementPanel rrpanel;
    
    private int depth = 0;

    private final MatchPopupMenuBuilder mpmb = new MatchPopupMenuBuilder();
    private final MatchDisplayMenuItemBuilder mdmib = new MatchDisplayMenuItemBuilder();
    private final EditLcbMenuItemBuilder elmib = new EditLcbMenuItemBuilder();
    private final SetReferenceMenuItemBuilder srmib = new SetReferenceMenuItemBuilder();
    
    public MatchPanel(RearrangementPanel rrpanel, BaseViewerModel model, Genome genome)
    {
        super(model, genome);
        this.rrpanel = rrpanel;
        viewEnd = getGenome().getViewStart() + getGenome().getViewLength() - 1;
        if (! (model instanceof XmfaViewerModel))
        {
            model.addHighlightListener(this);            
        }
        mpmb.addMenuItemBuilder(mdmib);
        mpmb.addMenuItemBuilder(elmib);
        mpmb.addMenuItemBuilder(srmib);
    }

    public MatchPopupMenuBuilder getMatchPopupMenuBuilder(){ return mpmb; }
    public MatchDisplayMenuItemBuilder getMatchDisplayMenuItemBuilder(){ return mdmib; }
    public EditLcbMenuItemBuilder getEditLcbMenuItemBuilder(){ return elmib; }
    public SetReferenceMenuItemBuilder getSetReferenceMenuItemBuilder(){ return srmib; }

    // extra methods which aren't implemented
    /** not implemented */
    public void mouseEntered(MouseEvent e)
    {
    }

    /** not implemented */
    public void mouseExited(MouseEvent e)
    {
    }

    /** not implemented */
    public void mousePressed(MouseEvent e)
    {
    }

    /** not implemented */
    public void mouseReleased(MouseEvent e)
    {
    }

    public void mouseClicked(MouseEvent e)
    {
    	model.highlightRange (null, 0, 0);
        if (e.isPopupTrigger())
        {
            mpmb.build(e,rrpanel,model,getGenome()).show(this, e.getX(), e.getY());
        }

        if (e.getClickCount() == 1)
        {
            // if it is a control click then show the popup menu
            if (e.isControlDown() || ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0))
            {
                mpmb.build(e,rrpanel,model,getGenome()).show(this, e.getX(), e.getY());
            }
            else
            {

                // first clear any previous highlights if shift isn't down
                if ((e.getModifiers() & InputEvent.SHIFT_MASK) == 0)
                {
                    model.clearMatchHighlights();
                }

                if (model instanceof XmfaViewerModel)
                {
                    XmfaViewerModel xm = (XmfaViewerModel) model;
                    if (xm.getSim(getGenome()) != null)
                    {
                        long seqX = pixelToCenterSequenceCoordinate(e.getX());
                        xm.alignView(getGenome(), seqX);
                    }
                }
                else
                if (model instanceof LcbViewerModel)
                {
                	LcbViewerModel lm = (LcbViewerModel)model;
                    long seqX_left = pixelToLeftSequenceCoordinate(e.getX());
                    long seqX_right = pixelToRightSequenceCoordinate(e.getX() + 1);
                    lm.alignView(getGenome(), seqX_left, seqX_right);
                }
                else
                {
                    Vector sortedMatches = getGenome().getSortedMatches();
                    
                    int[] match_range = new int[2];
                    this.getMatchPixelRange(e.getX(), e.getX(), match_range);
                    // highlight this and the other components of the match
                    for (int matchI = match_range[0]; matchI <= match_range[1]; matchI++)
                    {
                        if (matchI >= sortedMatches.size())
                        {
                            break;
                        }
                        model.addMatchHighlight((Match) sortedMatches.get(matchI));
                    }
                }
            }
        }
    }
    
    /**
     * centers view on selected coordinate of the genome associated with
     * this MatchPanel and aligns other genomes
     * 
     * @param coordinate  The position to go to
     * @return
     */
    public int goTo (long coordinate) {
    	model.zoomAndCenter(getGenome (), 100, coordinate);
    	if (model instanceof LcbViewerModel) {
    		((LcbViewerModel) model).alignView(getGenome (), coordinate);
    	}
    	else
    		System.out.println ("BaseViewerModel -- not moving to align");
    	return 1;
    }
    
    /**
     * isForGenome returns true if this RRSequencePanel is associated
     * with the specified genome, and false otherwise
     * 
     * @param comparator - the genome in question
     */
    public boolean isForGenome (Genome comparator) {
    	return getGenome ().equals (comparator);
    }

	public interface MatchPopupMenuItemBuilder extends EventListener
	{
		public JMenuItem[] getItem(MouseEvent evt, final RearrangementPanel rrpanel, final BaseViewerModel model, final Genome g);
	};

	private class SetReferenceMenuItemBuilder implements MatchPopupMenuItemBuilder
	{
		public JMenuItem[] getItem(MouseEvent evt, final RearrangementPanel rrpanel, final BaseViewerModel model, final Genome g)
		{
        	JMenuItem setReferenceItem = new JMenuItem("Set reference genome");
        	setReferenceItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    model.setReference(g);
                }});
        	return new JMenuItem[]{setReferenceItem};
		}
	}

	private class EditLcbMenuItemBuilder implements MatchPopupMenuItemBuilder
	{
		public JMenuItem[] getItem(MouseEvent evt, final RearrangementPanel rrpanel, final BaseViewerModel model, final Genome g)
		{
	        return new JMenuItem[0];
		}
	}
	
	private class MatchDisplayMenuItemBuilder implements MatchPopupMenuItemBuilder 
	{
		private void addItem(final Match m, Vector items, final BaseViewerModel model, final Genome g)
		{
            JMenuItem item = new JMenuItem("Align display to " + m.toString());
            item.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e)
                {
                    model.alignView(m, g);
                }});			
		}
		public JMenuItem[] getItem(MouseEvent evt, final RearrangementPanel rrpanel, final BaseViewerModel model, final Genome g)
		{
			JMenuItem[] itemArray = new JMenuItem[0];
	        if( !(model instanceof LcbViewerModel) && !(model instanceof XmfaViewerModel))
	        {
	        	Vector items = new Vector();
	            int[] match_range = new int[2];
	            getMatchPixelRange(evt.getX(), evt.getX(), match_range);
	            
	            Vector sortedMatches = getGenome().getSortedMatches();
		        if (match_range[1] - match_range[0] < MAX_POPUP_MATCHES)
		        {
		            for (int matchI = match_range[0]; matchI < match_range[1]; matchI++)
		            	addItem((Match) sortedMatches.get(matchI), items, model, g);
		        }
		        else
		        {
		            double pop_interval = (double) (match_range[1] - match_range[0]) / (double) MAX_POPUP_MATCHES;
		            double popI = match_range[0];
		            for (int matchI = match_range[0]; matchI < match_range[1]; matchI++)
		            {
		                if ((int) popI == matchI)
			            	addItem((Match) sortedMatches.get(matchI), items, model, g);
		                popI += pop_interval;
		            }
		        }
		        itemArray = new JMenuItem[items.size()];
		        items.toArray(itemArray);
	        }
	        return itemArray;
		}
	}

	public class MatchPopupMenuBuilder
	{
	    protected EventListenerList builders = new EventListenerList();
		public void addMenuItemBuilder(MatchPopupMenuItemBuilder fmib)
		{
			builders.add(MatchPopupMenuItemBuilder.class, fmib);
		}
		public JPopupMenu build(MouseEvent evt, final RearrangementPanel rrpanel, final BaseViewerModel model, final Genome g)
		{
			Object[] listeners = builders.getListenerList();
        	JPopupMenu leMenu = new JPopupMenu();
        	for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==MatchPopupMenuItemBuilder.class) {
                	JMenuItem[] items = ((MatchPopupMenuItemBuilder)listeners[i+1]).getItem(evt, rrpanel, model, g);
            		for(int j = 0; j < items.length; j++)
            			leMenu.add(items[j]);
                }
            }
			return leMenu;
		}
		public void removeMenuItemBuilder(MatchPopupMenuItemBuilder mpmib)
		{
			builders.remove(MatchPopupMenuItemBuilder.class, mpmib);
		}
	}

	MatchPopupMenu getPopup(MouseEvent evt)
    {
        MatchPopupMenu pop_menu = new MatchPopupMenu(rrpanel, model, getGenome());

        int[] match_range = new int[2];
        getMatchPixelRange(evt.getX(), evt.getX(), match_range);
        
        Vector sortedMatches = getGenome().getSortedMatches();
        
        MenuItem setReference = new MenuItem("Set reference genome");
        setReference.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                model.setReference(getGenome());
            }});
        pop_menu.add(setReference);
        
        if( !(model instanceof LcbViewerModel))
        {
	        if (match_range[1] - match_range[0] < MAX_POPUP_MATCHES)
	        {
	            for (int matchI = match_range[0]; matchI < match_range[1]; matchI++)
	                pop_menu.addMatch((Match) sortedMatches.get(matchI));
	        }
	        else
	        {
	            double pop_interval = (double) (match_range[1] - match_range[0]) / (double) MAX_POPUP_MATCHES;
	            double popI = match_range[0];
	            for (int matchI = match_range[0]; matchI < match_range[1]; matchI++)
	            {
	                if ((int) popI == matchI)
	                    pop_menu.addMatch((Match) sortedMatches.get(matchI));
	                popI += pop_interval;
	            }
	        }
        }

        MenuItem features = new MenuItem ("Filter Features");
        features.addActionListener (FeatureFilterer.getFilterer (model));
        pop_menu.add (features);
        
        return pop_menu;
    }

    /**
     * Finds all match indices which intersect with the specified range of
     * pixels in the current view.
     * 
     * @param start_pixel
     *            The first coordinate of the intersection range
     * @param end_pixel
     *            The last coordinate of the intersection range
     * @param match_range
     *            An int array with 2 elements. The resulting range of
     *            intersecting match indices will be returned as the first and
     *            second elements in the array.
     */
    protected void getMatchPixelRange(int start_pixel, int end_pixel, int[] match_range)
    {
        long[] seq_range = new long[2];
        pixelRangeToSequenceCoordinates(start_pixel, end_pixel, seq_range);
        model.getMatchRange(getGenome(), seq_range[0], seq_range[1], match_range);
    }

    /**
     * paint the sequence display. copies a pre-computed similarity display and
     * adds any highlighting
     */
    public void paintComponent(Graphics graphics)
    {
        Graphics2D g2 = (Graphics2D) graphics;
        
        // Formatting and printing bypass buffering.
        Double density = (Double) g2.getRenderingHint(MauveRenderingHints.KEY_SIMILARITY_DENSITY);
        if (density != null)
        {
            formatBoxes((Graphics2D) graphics, density.doubleValue());
        }
        else // Normal operations.
        {
            if ( bufferedImage == null || lastWidth != getWidth() || lastHeight != getHeight())
            {
                updateBuffer();
            }
            graphics.drawImage(bufferedImage, 0, 0, getWidth(), getHeight(), 0, 0, bufferedImage.getWidth(null), bufferedImage.getHeight(null), null);
        }
    }

    /**
     * 
     */
    private void updateBuffer()
    {
        bufferedImage = createImage(getWidth(), getHeight());
        Graphics g = bufferedImage.getGraphics();
        formatBoxes((Graphics2D) g, 1.0);
        lastWidth = getWidth();
        lastHeight = getHeight();
    }

    public void markDirty()
    {
        bufferedImage = null;
    }
    

    /**
     * Organize the matches into visual boxes to be displayed on screen.
     * formatBoxes will coalesce small matches into a single box to be
     * displayed.
     *
     * Draws the sequence similarity information, either based on Match objects
     * (ungapped local alignments) or based on a SimilarityIndex
     */
    private void formatBoxes(Graphics2D g2, double similarityIncrement)
    {
        double unitsPerPixel = (double) getGenome().getViewLength() / (double) getWidth();
        int box_height = (int) (AbstractSequencePanel.BOX_FILL * getHeight());
        int half_height = box_height / 2;
        double arc_size = (double) half_height / 6d;
        
        // be sure the background color matches
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        AffineTransform oldTransform = g2.getTransform();
        
        g2.translate(0, boxTop() + 1);
        
        if (model instanceof XmfaViewerModel)
        {
            drawXmfa(half_height, g2, similarityIncrement);
        }
        else if (model instanceof LcbViewerModel)
        {
            LcbViewerModel lm = (LcbViewerModel) model;
            
            // draw white backgrounds for LCB rectangles
            if (lm.getDrawLcbBounds())
            {
                drawWhiteBackgrounds(half_height, g2, arc_size);
            }
            
            if (lm.getDrawMatches())
            {
                drawMatchBoxes(unitsPerPixel, box_height, half_height, g2);
            }

            drawLcbBounds(unitsPerPixel, box_height, half_height, g2, arc_size);
        }
        else
        {
            if (model.getDrawMatches())
            {
                drawMatchBoxes(unitsPerPixel, box_height, half_height, g2);
            }
        }


        drawChromosomeBoundaries(g2);
        
        // draw the center line
        
        g2.setTransform(oldTransform);
        g2.setStroke(new BasicStroke(0f));
        g2.setColor(Color.black);
        g2.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
    }

    private void drawLcbBounds(double unitsPerPixel, int box_height, int half_height, Graphics2D matchGraphics, double arc_size)
    {
        LcbViewerModel lm = (LcbViewerModel) model;
        
        int lcbI;
        // set the pen width
        matchGraphics.setStroke(new BasicStroke((float) LCB_BOUNDARY_WIDTH));
        for (lcbI = 0; lcbI < lm.getVisibleLcbCount(); lcbI++)
        {
            LCB lcb = lm.getVisibleLcb(lcbI);
            if (lcb.getRightEnd(getGenome()) < getGenome().getViewStart() - 1)
                continue;
            if (lcb.getLeftEnd(getGenome()) > viewEnd + 1)
                continue;

            int leftPixel = sequenceCoordinateToLeftPixel(lcb.getLeftEnd(getGenome()));
            int rightPixel = sequenceCoordinateToRightPixel(lcb.getRightEnd(getGenome()));
            int pixelWidth = rightPixel - leftPixel;

            int lcb_top = lcb.getReverse(getGenome()) ? half_height : 0;

            if (lm.getVisibleLcb(lcbI).color != null)
                matchGraphics.setColor(lm.getVisibleLcb(lcbI).color);

            java.awt.geom.RoundRectangle2D.Double lcb_rect = new java.awt.geom.RoundRectangle2D.Double(leftPixel + HALF_PEN_WIDTH, lcb_top + HALF_PEN_WIDTH, pixelWidth - LCB_BOUNDARY_WIDTH, half_height - LCB_BOUNDARY_WIDTH, arc_size, arc_size);

            if (lm.getDrawLcbBounds())
            {
            	matchGraphics.draw(lcb_rect);
            }
            
            if (lm.getFillLcbBoxes())
            {
                matchGraphics.fill(lcb_rect);
            }
        }

        // now draw deleted LCBs in obnoxious yellow-orange
        matchGraphics.setColor(Color.getHSBColor(0.11f, 1, 1));
        matchGraphics.setStroke(new BasicStroke((float) LCB_BOUNDARY_WIDTH));
        for (lcbI = 0; lcbI < ((LcbViewerModel) model).getDelLcbList().length; lcbI++)
        {
            LCB delcb = ((LcbViewerModel) model).getDelLcbList()[lcbI];
            if (delcb.getRightEnd(getGenome()) < getGenome().getViewStart() - 1)
                continue;
            if (delcb.getLeftEnd(getGenome()) > viewEnd + 1)
                continue;

            int cur_start_pixel = (int) ((double) (delcb.getLeftEnd(getGenome()) - getGenome().getViewStart()) / unitsPerPixel);
            double lcb_length = delcb.getRightEnd(getGenome()) - delcb.getLeftEnd(getGenome());
            int cur_pixel_width = (int) Math.ceil(lcb_length / unitsPerPixel);

            java.awt.geom.RoundRectangle2D.Double lcb_rect = new java.awt.geom.RoundRectangle2D.Double(cur_start_pixel + HALF_PEN_WIDTH, HALF_PEN_WIDTH, cur_pixel_width - LCB_BOUNDARY_WIDTH, box_height - LCB_BOUNDARY_WIDTH, arc_size, arc_size);

            matchGraphics.fill(lcb_rect);
        }
    }
    
    
    /**
     * @param g
     */
    private void drawChromosomeBoundaries(Graphics2D g)
    {
    	if(!model.getDrawChromosomeBoundaries())
    		return;	// don't draw if we're not supposed to draw!
        g.setStroke(new BasicStroke(0f));
        g.setColor(Color.red);
        Iterator i = getGenome().getChromosomes().iterator();

        while (i.hasNext())
        {
            Chromosome c = (Chromosome) i.next();

            if (c.getStart() >= getGenome().getViewStart() && c.getStart() <= getGenome().getViewStart() + getGenome().getViewLength())
            {
                int high_pixel = sequenceCoordinateToCenterPixel(c.getStart());
                g.drawLine(high_pixel, 0, high_pixel, getHeight());
            }

            // Draw the last end line, too.
            if (!i.hasNext())
            {
                if (c.getEnd() >= getGenome().getViewStart() && c.getEnd() <= getGenome().getViewStart() + getGenome().getViewLength())
                {
                    int high_pixel = sequenceCoordinateToCenterPixel(c.getEnd());
                    g.drawLine(high_pixel, 0, high_pixel, getHeight());
                }
            }
        }
    }

    /**
     * @param lcb
     * @param half_height
     * @param arc_size
     * @return
     */
    private RoundRectangle2D getLcbRectangle(LCB lcb, int half_height)
    {
        double arc_size = (double) half_height / 6d;
        int leftPixel = sequenceCoordinateToLeftPixel(lcb.getLeftEnd(getGenome()));
        int rightPixel = sequenceCoordinateToRightPixel(lcb.getRightEnd(getGenome()));
        int pixelWidth = rightPixel - leftPixel;
        int lcb_top = lcb.getReverse(getGenome()) ? half_height : 0;
        RoundRectangle2D.Double lcb_rect = new RoundRectangle2D.Double(leftPixel + HALF_PEN_WIDTH, lcb_top + HALF_PEN_WIDTH, pixelWidth - LCB_BOUNDARY_WIDTH, half_height - LCB_BOUNDARY_WIDTH, arc_size, arc_size);
        return lcb_rect;
    }

    /**
     * @param end_view
     * @param unitsPerPixel
     * @param box_height
     * @param half_height
     * @param matchGraphics
     * @param highlightAreaGraphics
     * @param half_pen_width
     */
    private void drawMatchBoxes(double unitsPerPixel, int box_height, int half_height, Graphics2D g2)
    {
        // draw boxes for matches instead of similarity values

        int[] match_range = new int[2];
        model.getMatchRange(getGenome(), getGenome().getViewStart(), viewEnd, match_range);
        int first_match = match_range[0];
        long last_match = match_range[1];

        for (int directionI = 0; directionI < 2; directionI++)
        {
            boolean reverse_matches = false;
            if (directionI == 1)
                reverse_matches = true;
            int prev_start_pixel = -1;
            int prev_pixel_width = 0;
            int cur_start_pixel = 0;
            int cur_pixel_width = 0;
            int end_pixel = 0;

            double match_top = reverse_matches ? half_height + HALF_PEN_WIDTH : HALF_PEN_WIDTH;

            Color prev_color = null;
            boolean prev_reverse = false;
            Vector sortedMatches = getGenome().getSortedMatches();
            for (int matchI = first_match; matchI < last_match; matchI++)
            {
                Match cur_match = (Match) sortedMatches.get(matchI);
                // don't draw the match if it's not on the current strand
                // or if it's part of a 'disabled' LCB.
                boolean rev = false;
                if(model instanceof LcbViewerModel && cur_match.lcb >= 0)
                	rev = ((LcbViewerModel)model).getFullLcbList()[cur_match.lcb].getReverse(getGenome());
                else
                	rev = cur_match.getReverse(getGenome());
                if ( rev == reverse_matches && cur_match.lcb >= 0)
                {
                    cur_start_pixel = (int) ((double) (cur_match.getStart(getGenome()) - getGenome().getViewStart()) / unitsPerPixel);
                    cur_pixel_width = (int) Math.ceil((double) cur_match.getLength(getGenome()) / unitsPerPixel);
                    if (cur_match.highlighted)
                    {
                        Color oldColor = g2.getColor();
                        g2.setColor(Color.BLACK);
                        if (reverse_matches)
                        {
                            g2.fillRect(cur_start_pixel, getHeight()/2, cur_pixel_width, getHeight()/2);
                        }
                        else
                        {
                            g2.fillRect(cur_start_pixel, 0, cur_pixel_width, getHeight()/2);
                        }
                        g2.setColor(oldColor);
                    }

                    if (cur_start_pixel > prev_start_pixel)
                    {
                        end_pixel = prev_start_pixel + prev_pixel_width - 1;
                        end_pixel = cur_start_pixel < end_pixel ? cur_start_pixel : end_pixel;
                        g2.setColor(prev_color);
                        java.awt.geom.Rectangle2D.Double match_rect = new java.awt.geom.Rectangle2D.Double(prev_start_pixel, match_top, end_pixel - prev_start_pixel + 1, half_height - LCB_BOUNDARY_WIDTH);
                        g2.fill(match_rect);
                    }

                    prev_start_pixel = cur_start_pixel;
                    prev_pixel_width = cur_pixel_width;
                    prev_reverse = rev;
                    prev_color = ((Match) sortedMatches.get(matchI)).color;
                }
            }

            // draw the last match.
            g2.setColor(prev_color);
            if (prev_reverse)
            {
                g2.fillRect(prev_start_pixel, half_height, prev_pixel_width, box_height);
            }
            else
            {
                g2.fillRect(prev_start_pixel, 0, prev_pixel_width, half_height);
            }
        } // for directionI

    }
    /**
     * Creates a similarity plot fill color for an LCB of a given color
     * @param lcb_color The color of the LCB bounding rectangle
     * @return
     */
    private static Color getFillColor( Color lcb_color )
    {
    	float[] hsbvals = Color.RGBtoHSB(lcb_color.getRed(), lcb_color.getGreen(), lcb_color.getBlue(), null);
    	hsbvals[1] = hsbvals[1] - .2f > 0 ? hsbvals[1] - .2f : 0;
    	hsbvals[2] = hsbvals[2] + .2f < 1 ? hsbvals[2] + .2f : 1;
    	return Color.getHSBColor( hsbvals[0], hsbvals[1], hsbvals[2]);
    }

    /**
     * @param half_height
     * @param g
     * @param half_pen_width
     */
    private void drawXmfa(int half_height, Graphics2D g, double increment)
    {
        XmfaViewerModel xm = ((XmfaViewerModel) model);
        g.setStroke(new BasicStroke((float) LCB_BOUNDARY_WIDTH));
        double sim_height = half_height - LCB_BOUNDARY_WIDTH;
        Color sim_color = Color.GRAY;
        
        BackboneList bb_list = xm.getBackboneList();
        
        // Get the first LCB, and start drawing it.
        LCB lcb = xm.getLeftmostVisibleLCB(getGenome());
        while(lcb != null && lcb.multiplicity() == 1)
            lcb = xm.getVisibleRightNeighbor(lcb, getGenome(), 0);
        	
        RoundRectangle2D r = null;
        if (lcb != null)
        {
            r =  getLcbRectangle(lcb, half_height);
            // Draw profile data.
            if (r.getWidth() >= 1)
            {
                if (xm.getDrawLcbBounds())
                {
                    // Draw white background.
                    g.setColor(Color.WHITE);
                    g.fill(r);
                }
            }
            if( bb_list == null )
            	sim_color = getFillColor(lcb.color);
        }
        
        LCB deletedLCB = xm.getLeftmostDeletedLCB(getGenome());
        if (deletedLCB != null)
        {
	        if (deletedLCB.getRightEnd(getGenome()) > getGenome().getViewStart())
	        {
	            if (lcb.getLeftEnd(getGenome()) < viewEnd)
	            {
	                RoundRectangle2D delRec = this.getLcbRectangle(deletedLCB, half_height);
	                g.setColor(DELETED_COLOR);
	                g.fill(delRec);
	            }
	        }
        }
        
    	// if we have backbone information then find out whether we're
    	// inside a backbone segment.  if we're not in a bb segment,
    	// find the next segment since querying repeatedly would be too expensive
        Backbone bb = null;
        Backbone next_bb = null;
        if( bb_list != null )
        {
        	long pos = pixelToCenterSequenceCoordinate(0);
        	bb = bb_list.getBackbone(getGenome(), pos);
        	if( bb == null )
        	{
        		next_bb = bb_list.getNextBackbone(getGenome(), pos);
        		if( next_bb != null && pixelToCenterSequenceCoordinate(increment) >= next_bb.getLeftEnd(getGenome()) )
        		{
        			bb = next_bb;
        			sim_color = getFillColor(bb.getColor());
        		}else	// last resort: if we're completely outside backbone set the color to gray
        			sim_color = getFillColor(Color.GRAY);
        	}else
            	sim_color = getFillColor(bb.getColor());
        }
        
        for (double pixelD = 0; pixelD < getWidth(); pixelD += increment)
        {
            // Determine the range of sequence coordinates with which we are working.
            long seq_left = pixelToCenterSequenceCoordinate(pixelD);
            long seq_right = pixelToCenterSequenceCoordinate(pixelD + increment);
            // Clamp the range.
            seq_left = seq_left < 0 ? 0 : seq_left;
            seq_right = seq_right < getGenome().getLength() ? seq_right : getGenome().getLength();

            // Don't draw anything if we're out of range.
            if (seq_left > getGenome().getLength() || seq_right < 0)
            {
                continue;
            }
            
            // If we've moved past end of lcb, find next one.
            if (lcb != null && seq_left > lcb.getRightEnd(getGenome()))
            {	
                // Finish previous rectangle.
                if (xm.getFillLcbBoxes())
                {
                    g.setColor(lcb.color);
                    g.fill(r);
                }
                else if (xm.getDrawLcbBounds())
                {
                    g.setColor(lcb.color);
                    g.draw(r);
                }
                
                // Get next LCB, and start drawing it.
                lcb = xm.getVisibleRightNeighbor(lcb, getGenome(), seq_left);
                while(lcb != null && lcb.multiplicity() == 1)
                    lcb = xm.getVisibleRightNeighbor(lcb, getGenome(), seq_left);
                if (lcb != null)
                {
                    r =  getLcbRectangle(lcb, half_height);
                    // Draw profile data.
                    if (r.getWidth() >= 1)
                    {
                        if (xm.getDrawLcbBounds())
                        {
                            // Draw white background.
                            g.setColor(Color.WHITE);
                            g.fill(r);
                        }
                    }
                    if(bb_list == null)
                    	sim_color = getFillColor(lcb.color);
               }
            }
            
            // scan up to the next deleted LCB
            if (deletedLCB != null && seq_left > deletedLCB.getRightEnd(getGenome()))
            {
                deletedLCB = xm.getDeletedRightNeighbor(deletedLCB, getGenome(), seq_left);

                if (deletedLCB != null && deletedLCB.getRightEnd(getGenome()) > getGenome().getViewStart())
    	        {
    	            if (deletedLCB.getLeftEnd(getGenome()) < viewEnd)
    	            {
    	                RoundRectangle2D delRec = this.getLcbRectangle(deletedLCB, half_height);
    	                g.setColor(DELETED_COLOR);
    	                g.fill(delRec);
    	            }
    	        }
            }
 
    		// check whether we're still in range of the backbone
        	// update the current bb segment as necessary
            if( bb_list != null )
            {
	            if( (bb != null && seq_left > bb.getRightEnd(getGenome())) ||
	            	(bb == null && next_bb != null && 
		                next_bb.getLeftEnd(getGenome()) <= seq_left ))
	            {
	            	bb = bb_list.getBackbone(getGenome(), seq_left);
	            	if( bb == null )
	            	{
	            		next_bb = bb_list.getNextBackbone(getGenome(), seq_left);
	            		if( next_bb != null && seq_right >= next_bb.getLeftEnd(getGenome()) )
	            		{
	            			bb = next_bb;
	            			sim_color = getFillColor(bb.getColor());
	            		}else	// last resort: if we're completely outside backbone set the color to gray
	            			sim_color = getFillColor(Color.GRAY);
	            	}else
	                	sim_color = getFillColor(bb.getColor());
	            }
            }
            
            // don't draw anything if we're not within a valid LCB
            if (lcb != null && xm.getDrawMatches())
            {
                if (seq_right >= lcb.getLeftEnd(getGenome()) && (deletedLCB == null || seq_right < deletedLCB.getLeftEnd(getGenome())))
                {
                    Shape oldClip = g.getClip();
                    if (xm.getDrawLcbBounds() || xm.getFillLcbBoxes())
                    {
                        g.setClip(r);
                    }
                    boolean reverse = lcb.getReverse(getGenome());
                    double s = ((XmfaViewerModel) model).getSim(getGenome()).getValueForRange(seq_left, seq_right);
                    // normalize to a box_height
                    double height = (((double) s + 127d) / 256d * sim_height);
                    double match_top = reverse ? half_height + HALF_PEN_WIDTH : HALF_PEN_WIDTH + sim_height - height;
                    Rectangle2D.Double match_rect = new Rectangle2D.Double(pixelD, match_top, increment, height);
                	g.setColor(sim_color);
                    g.fill(match_rect);
                    g.setClip(oldClip);
                }
            }
        }

        // If there remains a border to be drawn, draw it.
        
        if (lcb != null)
        {
            // Finish previous rectangle.
            if (xm.getFillLcbBoxes())
            {
                g.setColor(lcb.color);
                g.fill(r);
            }
            else if (xm.getDrawLcbBounds())
            {
                // workaround for an OpenJDK 6 bug which can't handle offscreen coords bigger than 30k
                if(r.getMaxX()>getWidth() && r.getMaxX()>20000){
                	double newx = -100 > r.getX() ? -100 : r.getX();
                	double newwidth = getWidth() + 100 < r.getWidth() ? getWidth() + 100 : r.getWidth();
                	r.setRoundRect(newx, r.getY(), newwidth, r.getHeight(), r.getArcWidth(), r.getArcHeight());
                }
                g.setColor(lcb.color);
                g.draw(r);
            }
        }
    }

    private void drawWhiteBackgrounds(int half_height, Graphics2D matchGraphics, double arc_size)
    {
        int lcbI;
        matchGraphics.setColor(Color.white);
        
        for (lcbI = 0; lcbI < ((LcbViewerModel) model).getVisibleLcbCount(); lcbI++)
        {

            LCB lcb = ((LcbViewerModel) model).getVisibleLcb(lcbI);

            // skip this LCB if it's out of viewing range
            if (lcb.getRightEnd(getGenome()) < getGenome().getViewStart() - 1)
            {
                continue;
            }
            if (lcb.getLeftEnd(getGenome()) > viewEnd + 1)
            {
                continue;
            }
            

            int leftPixel = sequenceCoordinateToLeftPixel(lcb.getLeftEnd(getGenome()));
            int rightPixel = sequenceCoordinateToRightPixel(lcb.getRightEnd(getGenome()));
            int pixelWidth = rightPixel - leftPixel;
            int lcb_top = lcb.getReverse(getGenome()) ? half_height : 0;

            java.awt.geom.RoundRectangle2D.Double lcb_rect = new java.awt.geom.RoundRectangle2D.Double(leftPixel + HALF_PEN_WIDTH, lcb_top + HALF_PEN_WIDTH, pixelWidth - LCB_BOUNDARY_WIDTH, half_height - LCB_BOUNDARY_WIDTH, arc_size, arc_size);
            matchGraphics.fill(lcb_rect);
        }
    }
    
    
    public void colorChanged(ModelEvent event)
    {
        markDirty();
        repaint();
    }

    public void weightChanged(ModelEvent event)
    {
        markDirty();
        repaint();
    }

    public void highlightChanged(ModelEvent evt)
    {
        markDirty();
        repaint();
    }

    public void drawingSettingsChanged(ModelEvent event)
    {
        markDirty();
        repaint();
    }

    public void viewableRangeChanged(ModelEvent event)
    {
        viewEnd = getGenome().getViewStart() + getGenome().getViewLength() - 1;
        markDirty();
        repaint();
    }

    public void modelReloadEnd(ModelEvent event)
    {
        markDirty();
        repaint();
    }
    
    public void referenceChanged(ModelEvent event)
    {
        markDirty();
    }
    public void genomesReordered(ModelEvent event)
    {
        markDirty();	// the background color may have changed
    }
} 

