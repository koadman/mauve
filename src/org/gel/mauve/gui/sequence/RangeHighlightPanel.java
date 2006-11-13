package org.gel.mauve.gui.sequence;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Genome;
import org.gel.mauve.HighlightListener;
import org.gel.mauve.ModelEvent;


public class RangeHighlightPanel extends AbstractSequencePanel implements MouseMotionListener, MouseListener, HighlightListener
{
	float alpha = .15f;
	Color highlight_color = Color.cyan;
	Point drag_start = null;

	public RangeHighlightPanel(BaseViewerModel model, Genome genome)
    {
        super(model, genome);
        setOpaque(false);
        model.addHighlightListener(this);
    }

	public void highlightChanged(ModelEvent evt)
    {
		if( model.getRangeHighlightGenome() != null )
			repaint();
    }
	
	public void mousePressed(MouseEvent e)
	{
		if((e.getModifiers() & InputEvent.SHIFT_MASK) != 0)
			drag_start = e.getPoint();
		else
			drag_start = null;
	}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mouseClicked(MouseEvent e)
	{
		drag_start = null;
		// if shift was down then clear the highlight
		if((e.getModifiers() & InputEvent.SHIFT_MASK) != 0)
			model.highlightRange(null, 0, 0);
	}
	public void mouseReleased(MouseEvent e){}
	
       
    public void mouseDragged(MouseEvent e)
    {
    	if( drag_start != null)
    	{
	    	int left_x = drag_start.x < e.getX() ? drag_start.x : e.getX();
	    	int right_x = drag_start.x > e.getX() ? drag_start.x : e.getX();
	    	long left_coord = this.pixelToLeftSequenceCoordinate(left_x);
	    	long right_coord = this.pixelToRightSequenceCoordinate(right_x);
	    	model.highlightRange(this.getGenome(), left_coord, right_coord);
    	}
    }

    public void mouseMoved(MouseEvent e)
    {
    }

    /**
     * draw a translucent highlighting over a range of the display
     */
    public void paintComponent(Graphics graphics)
    {
    	if( model.getRangeHighlightGenome() == this.getGenome() )
    	{
	    	Graphics2D g2d = (Graphics2D)graphics;
	    	Composite tmp_composite = g2d.getComposite();
	        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
	        g2d.setColor(highlight_color);
	        long left_end = model.getRangeHighlightLeft();
	        long right_end = model.getRangeHighlightRight();
	        int left_pix = sequenceCoordinateToLeftPixel(left_end);
	        int right_pix = sequenceCoordinateToRightPixel(right_end);
	        g2d.fillRect(left_pix, 0, right_pix - left_pix, getHeight());
	        g2d.setComposite(tmp_composite);
    	}
    }
}
