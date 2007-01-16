package org.gel.mauve.gui.sequence;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Genome;
import org.gel.mauve.HighlightListener;
import org.gel.mauve.LcbViewerModel;
import org.gel.mauve.Match;
import org.gel.mauve.ModelEvent;
import org.gel.mauve.XmfaViewerModel;


public class HighlightPanel extends AbstractSequencePanel implements MouseMotionListener, HighlightListener
{
    public HighlightPanel(BaseViewerModel model, Genome genome)
    {
        super(model, genome);
        setOpaque(false);
        
        if (model instanceof XmfaViewerModel)
        {
            ((XmfaViewerModel) model).addHighlightListener(this);
        }
        else if (model instanceof LcbViewerModel)
        {
	        ((LcbViewerModel) model).addHighlightListener(this);
	    }    
    }
    
    public void highlightChanged(ModelEvent evt)
    {
        repaint();
    }
    
    public void mouseDragged(MouseEvent e)
    {
    }

    public void mouseMoved(MouseEvent e)
    {
        int box_height = (int) (AbstractSequencePanel.BOX_FILL * getHeight());
        int box_top = (getHeight() - box_height) / 2;
        if (e.getY() >= box_top && e.getY() <= box_top + box_height)
        {
        	if( model instanceof XmfaViewerModel )
        	{
	            long seqX = pixelToCenterSequenceCoordinate(e.getX());
	            if (seqX >= 0 && seqX < getGenome().getLength())
	            {
	                model.updateHighlight(getGenome(), seqX);
	            }
        	}
        	else if( model instanceof LcbViewerModel ){
	            long seqX_left = pixelToLeftSequenceCoordinate(e.getX());
	            long seqX_right = pixelToRightSequenceCoordinate(e.getX()+1);
	            if (seqX_left >= 0 && seqX_right < getGenome().getLength())
	            {
	                ((LcbViewerModel)model).updateHighlight(getGenome(), seqX_left, seqX_right);
	            }
        	}
        }
    }

    protected static Color cursor_color = new Color( 0, 0, 0 );
    protected int half = 7;
    protected int full = 14;
    /**
     * paint the sequence display. copies a pre-computed similarity display and
     * adds any highlighting
     */
    public void paintComponent(Graphics graphics)
    {
    	long highlightCoord = Match.NO_MATCH;
		// get the highlighted coordinate in this genome
    	if( model instanceof XmfaViewerModel)
    		highlightCoord = ((XmfaViewerModel)model).getHighlight(getGenome());
    	else if (model instanceof LcbViewerModel)
    	{
    		long highlightArray[] = ((LcbViewerModel)model).getHighlightArray(getGenome());
    		highlightCoord = highlightArray[0];
    	}
    	else
    		return;
        
    	if (highlightCoord != Match.NO_MATCH)
    	{
            Graphics2D g = (Graphics2D) graphics;
            long absHighlightCoord = Math.abs(highlightCoord);
            if (absHighlightCoord >= getGenome().getViewStart() && absHighlightCoord <= getGenome().getViewStart() + getGenome().getViewLength())
            {
                int pixel = sequenceCoordinateToCenterPixel(absHighlightCoord);
                g.setColor(cursor_color);

                g.drawLine(pixel, 0, pixel, getHeight());
                if( highlightCoord > 0 )
                {
                	g.drawRect(pixel-half, 0, full, getHeight()-1);
                }
                
            }
    	}
    }
}
