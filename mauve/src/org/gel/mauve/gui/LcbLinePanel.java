package org.gel.mauve.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import javax.swing.JComponent;

import org.gel.mauve.Genome;
import org.gel.mauve.LCB;
import org.gel.mauve.LcbViewerModel;
import org.gel.mauve.ModelEvent;
import org.gel.mauve.ModelListener;
import org.gel.mauve.gui.sequence.RRSequencePanel;
import org.gel.mauve.gui.sequence.SeqPanel;

/**
 * This class is intended to overlay a RearrangementPanel with lines that
 * connect each locally collinear block. It reads the LCB boundaries and the
 * current view range from various classes contained by a RearrangementPanel. A
 * very, very dirty hack.
 */
class LcbLinePanel extends JComponent implements ModelListener
{
    boolean[] highlighted; // The LCBs that should be highlighted

    RearrangementPanel rrpanel;

    int menubar_height;

    boolean draw_strikethrough = true;
    
    boolean hidden = false;
    
    boolean getHidden(){ return hidden; }
    void setHidden(boolean hidden)
    {
    	if(this.hidden != hidden)
    		setVisible(!hidden);
    	this.hidden = hidden; 
    }

    LcbViewerModel model;

    LcbLinePanel(RearrangementPanel rrpanel, LcbViewerModel model)
    {
        this.rrpanel = rrpanel;
        this.model = model;
        this.menubar_height = 0;
        highlighted = new boolean[model.getVisibleLcbCount()];
        for (int hI = 0; hI < highlighted.length; hI++)
        {
            highlighted[hI] = true;
        }
        model.addModelListener(this);
    }

    public void paintComponent(Graphics g)
    {
        paint(g);
    }
    
    int nextVisibleGenome(LCB lcb, int fromSeq)
    {
    	int seqJ = fromSeq;
    	for (; seqJ < model.getSequenceCount(); seqJ++)
        {
    		Genome lg = model.getGenomeByViewingIndex(seqJ);
    		if(lcb.getLeftEnd(lg) != 0 && lg.getVisible())
    			break;
        }
    	return seqJ;
    }

    /**
     * Extract the pixel coordinates of visible LCBs and draw lines that connect
     * them.
     */
    public void paint(Graphics g1d)
    {
        Graphics2D g2d = (Graphics2D) g1d;
        g2d.setColor(Color.black);
        Rectangle bounds = getBounds();
        
        int[] boxTops = new int[model.getSequenceCount()];
        int[] boxHeights = new int[model.getSequenceCount()];
        Rectangle boxBounds[] = new Rectangle[model.getSequenceCount()];
        Genome genomes[] = new Genome[model.getSequenceCount()];
        SeqPanel seqpanels[] = new SeqPanel[model.getSequenceCount()];
        RRSequencePanel rrseqpanels[] = new RRSequencePanel[model.getSequenceCount()];
        int clipx = 0;
        for (int seqI = 0; seqI < model.getSequenceCount(); seqI++)
        {
            genomes[seqI] = model.getGenomeByViewingIndex(seqI);
            seqpanels[seqI] = rrpanel.getNewPanel(seqI);
            rrseqpanels[seqI] = seqpanels[seqI].getSequencePanel();

            boxTops[seqI] = rrseqpanels[seqI].boxTop() + 1;
            boxHeights[seqI] = rrseqpanels[seqI].boxHeight() + 1;

            boxBounds[seqI] = rrseqpanels[seqI].getBounds();
            boxBounds[seqI].x += seqpanels[seqI].getBounds().x;
            boxBounds[seqI].y += seqpanels[seqI].getBounds().y;
            clipx = seqpanels[seqI].getControlPanel().getWidth();
        }
        g2d.clipRect(clipx, 0, this.getWidth(), this.getHeight());

        for (int lcbI = 0; lcbI < model.getVisibleLcbCount(); lcbI++)
        {
            
            if (!highlighted[lcbI])
                continue;

            LCB lcb = model.getVisibleLcb(lcbI);

            int firstVisible = nextVisibleGenome(lcb,0);
            int seqJ = model.getSequenceCount();
            for (int seqI = firstVisible; seqI + 1 < model.getSequenceCount(); seqI = seqJ)
            {
            	Genome g = model.getGenomeByViewingIndex(seqI);
            	seqJ = nextVisibleGenome(lcb,seqI+1);
            	if(seqJ == model.getSequenceCount())
            		continue;

            	Genome upperGenome = model.getGenomeByViewingIndex(seqI);
	            Genome lowerGenome = model.getGenomeByViewingIndex(seqJ);
	            
	            RRSequencePanel upperPanel = rrseqpanels[seqI];
	            RRSequencePanel lowerPanel = rrseqpanels[seqJ];
	
	            int upperBoxTop = boxTops[seqI];
	            int upperBoxHeight = boxHeights[seqI];
	
	            int lowerBoxTop = boxTops[seqJ];
	            int lowerBoxHeight = boxHeights[seqJ];
	
	            // Here's what needs to be fixed.
	
	            // Translate bounds, since RRSequencePanels are contained in
	            // SeqPanels.
	            Rectangle upperBounds = boxBounds[seqI];
	            Rectangle lowerBounds = boxBounds[seqJ];

                Color base_color = Color.BLACK;
                if (lcb.color != null)
                {
                	base_color = lcb.color;
                }
                g2d.setColor(base_color);
                int upperMidpoint = upperPanel.sequenceCoordinateToCenterPixel(lcb.midpoint(upperGenome));
                int lowerMidpoint = lowerPanel.sequenceCoordinateToCenterPixel(lcb.midpoint(lowerGenome));

                upperMidpoint += upperBounds.x;
                lowerMidpoint += lowerBounds.x;
                // draw the connecting bars if at least one of the LCBs is
                // visible
                if (barVisible(bounds, upperMidpoint, lowerMidpoint))
                {
                    

                    if (draw_strikethrough)
                    {
                        if (seqI == firstVisible)
                        {
                            if (!lcb.getReverse(upperGenome))
                            {
                                g2d.drawLine(upperMidpoint, upperBounds.y + upperBoxTop + upperBoxHeight / 2, upperMidpoint, upperBounds.y + upperBoxTop + upperBoxHeight);
                            }
                        }
                        else
                        {
                            int y_offset = lcb.getReverse(lowerGenome) ? 0 : upperBoxHeight / 2;
                            g2d.drawLine(upperMidpoint, upperBounds.y + upperBoxTop + y_offset, upperMidpoint, upperBounds.y + upperBoxTop + y_offset + upperBoxHeight / 2);
                        }
                    }
                                        
                    if (draw_strikethrough)
                    {
                        // If there are more genomes below the lower genome, the strikethrough line extends beyond the bottom of the LCB
                    	int seqK = nextVisibleGenome(lcb,seqJ+1);
                    	if(seqK < model.getSequenceCount())
                        {
                            int y_offset = lcb.getReverse(lowerGenome) ? 0 : lowerBoxHeight / 2;
                            g2d.drawLine(lowerMidpoint, lowerBounds.y + lowerBoxTop + y_offset, lowerMidpoint, lowerBounds.y + lowerBoxTop + y_offset + lowerBoxHeight / 2);
                        }
                        else if (lcb.getReverse(lowerGenome))
                        {
                            g2d.drawLine(lowerMidpoint, lowerBounds.y + lowerBoxTop, lowerMidpoint, lowerBounds.y + lowerBoxTop + lowerBoxHeight / 2);
                        }
                    }
                    g2d.drawLine(upperMidpoint, upperBounds.y + upperBoxTop + upperBoxHeight, lowerMidpoint, lowerBounds.y + lowerBoxTop);
                }
            }
        }
    }

    /**
     * @param bounds
     * @param start
     * @param end
     * @return
     */
    private boolean barVisible(Rectangle bounds, int start, int end)
    {
        return (start > 0 && start < bounds.width) || (end > 0 && end < bounds.width);
    }

    public void colorChanged(ModelEvent event)
    {
        // Color scheme doesn't affect lcb lines.
    }

    public void weightChanged(ModelEvent event)
    {
        repaint();
    }

    public void drawingSettingsChanged(ModelEvent event)
    {
        // Box fill etc doesn't affect lines.
    }

    public void modeChanged(ModelEvent event)
    {
        // Mode doesn't affect lines
    }
    
    public void viewableRangeChanged(ModelEvent event)
    {
        // Ignored.
    }

    public void viewableRangeChangeStart(ModelEvent event)
    {
    	if(!getHidden())
    		setVisible(false);
    }

    public void viewableRangeChangeEnd(ModelEvent event)
    {
    	if(!getHidden())
    		setVisible(true);
    }

    public void modelReloadStart(ModelEvent event)
    {
        // Ignored.
    }

    public void modelReloadEnd(ModelEvent event)
    {
        repaint();
    }

    public void genomesReordered(ModelEvent event)
    {
        repaint();
    }
    
    public void referenceChanged(ModelEvent event)
    {
        repaint();
    }

    public void genomeVisibilityChanged(ModelEvent event)
    {
        // Ignored.
    }
}