package org.gel.mauve.gui.sequence;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLayeredPane;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Genome;
import org.gel.mauve.ModelEvent;
import org.gel.mauve.ModelListener;
import org.gel.mauve.ViewerMode;
import org.gel.mauve.gui.FillLayout;
import org.gel.mauve.gui.RearrangementPanel;

public class RRSequencePanel extends JLayeredPane implements ModelListener
{
    private MatchPanel matchPanel;
    private HighlightPanel highlightPanel;
    public RangeHighlightPanel rangeHighlightPanel;
    public RRSequencePanel(RearrangementPanel rrpanel, BaseViewerModel model, Genome genome)
    {
        setLayout(new FillLayout());
        matchPanel = new MatchPanel(rrpanel, model, genome);
        add(matchPanel, new Integer(1));
        highlightPanel = new HighlightPanel(model, genome);
        add(highlightPanel, new Integer(2));
        rangeHighlightPanel = new RangeHighlightPanel(model, genome);
        add(rangeHighlightPanel, new Integer(3));
        setMinimumSize( new Dimension( 10000, 100 ) );
        setMaximumSize( new Dimension( 10000, 175 ) );
        addMouseListener(matchPanel);
        addMouseListener(rangeHighlightPanel);
        addMouseMotionListener(highlightPanel);
        addMouseMotionListener(rangeHighlightPanel);
        model.addModelListener(this);
    }
    
    /**
     * goTo - scrolls viewer to particular coordinate of genome
     * 
     * @param position The position in the sequence to view
     */
    public int goTo (long position) {
    	return matchPanel.goTo (position);
    }
    
    /**
     * isForGenome returns true if this RRSequencePanel is associated
     * with the specified genome, and false otherwise
     * 
     * @param genome - the genome in question
     */
    public boolean isForGenome (Genome genome) {
    	return matchPanel.isForGenome (genome);
    }
    
    public void setBackground(Color bg)
    {
    	super.setBackground(bg);
    	if(matchPanel != null)
    		matchPanel.setBackground(bg);
    }
    
    public int boxHeight()
    {
        return matchPanel.boxHeight();
    }
    
    public int boxTop()
    {
        return matchPanel.boxTop();
    }
    
    public int sequenceCoordinateToCenterPixel(long coord)
    {
        return matchPanel.sequenceCoordinateToCenterPixel(coord);
    }

    public void colorChanged(ModelEvent event)
    {
        // Ignored
    }

    public void weightChanged(ModelEvent event)
    {
        // Ignored.
    }
    
    public void referenceChanged(ModelEvent event)
    {
        // Ignored.
    }

    public void drawingSettingsChanged(ModelEvent event)
    {
        // Ignored.
    }

    public void modeChanged(ModelEvent event)
    {
        BaseViewerModel model = (BaseViewerModel) event.getSource();
        
        if (model.getMode() == ViewerMode.NORMAL)
        {
            addMouseListener(matchPanel);
            addMouseListener(rangeHighlightPanel);
            addMouseMotionListener(highlightPanel);
            addMouseMotionListener(rangeHighlightPanel);
        }
        else
        {
            removeMouseListener(matchPanel);
            removeMouseListener(rangeHighlightPanel);
            removeMouseMotionListener(highlightPanel);
            removeMouseMotionListener(rangeHighlightPanel);
        }
    }

    public void viewableRangeChanged(ModelEvent event)
    {
        // Ignored.
    }

    public void viewableRangeChangeStart(ModelEvent event)
    {
        // Ignored.
    }

    public void viewableRangeChangeEnd(ModelEvent event)
    {
        // Ignored.
    }

    public void modelReloadStart(ModelEvent event)
    {
        // Ignored.
    }
    
    public void modelReloadEnd(ModelEvent event)
    {
        // Ignored
    }

    public void genomesReordered(ModelEvent event)
    {
        // Ignored
    }
} 

