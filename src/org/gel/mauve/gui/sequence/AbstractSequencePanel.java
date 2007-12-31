package org.gel.mauve.gui.sequence;

import java.util.List;

import javax.swing.JPanel;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Genome;
import org.gel.mauve.LcbViewerModel;
import org.gel.mauve.ModelEvent;
import org.gel.mauve.ModelListener;


public abstract class AbstractSequencePanel extends JPanel implements ModelListener
{
    // The Genome this panel displays
    private Genome genome;
    // Model for this component.
    protected BaseViewerModel model;
    // The percentage height of each rearrangement bar. The rest is used to draw highlighting.
    public final static double BOX_FILL = 0.95;
    
    public AbstractSequencePanel(BaseViewerModel model, Genome genome)
    {
        this.model = model;
        this.genome = genome;
        model.addModelListener(this);
    }
    
    protected final int boxTop()
    {
        return (int) (((1.0 - AbstractSequencePanel.BOX_FILL) / 2) * getHeight());
    }

    protected final int boxHeight()
    {
        return (int) (AbstractSequencePanel.BOX_FILL * getHeight());
    }

    public final long pixelToRightSequenceCoordinate(int pixel)
    {
        if (getWidth() == 0)
            return 0;
    
        return (long) ((pixel * getGenome().getViewLength() / (double) getWidth()) + getGenome().getViewStart() + 2);
    }

    public final long pixelToLeftSequenceCoordinate(int pixel)
    {
        if (getWidth() == 0)
            return 0;
    
        return (long) ((pixel * getGenome().getViewLength() / (double) getWidth()) + getGenome().getViewStart());
    }

    public long pixelToCenterSequenceCoordinate(double pixel)
    {
        if (getWidth() == 0)
            return 0;
        
        return (long) ((pixel * getGenome().getViewLength() / getWidth()) + getGenome().getViewStart() + 1);
    }

    public final long pixelToCenterSequenceCoordinate(int pixel)
    {
        if (getWidth() == 0)
            return 0;
    
        return (long) ((pixel * getGenome().getViewLength() / (double) getWidth()) + getGenome().getViewStart() + 1);
    }

    public final int sequenceCoordinateToRightPixel(long pos)
    {
        if (getGenome().getViewLength() == 0)
            return 0;
    
        return (int) ((pos - getGenome().getViewStart()) * getWidth() / getGenome().getViewLength());
    }

    public final int sequenceCoordinateToLeftPixel(long pos)
    {
        if (getGenome().getViewLength() == 0)
            return 0;
    
        return (int) ((pos - getGenome().getViewStart() - 1) * getWidth() / getGenome().getViewLength());
    }

    public final int sequenceCoordinateToCenterPixel(long pos)
    {
        if (getGenome().getViewLength() == 0)
            return 0;
    
        return (int) ((pos - getGenome().getViewStart() - 0.5) * getWidth() / getGenome().getViewLength());
    }

    /**
     * Converts a range of pixel coordinates to sequence coordinates.
     */
    protected final void pixelRangeToSequenceCoordinates(int start_pixel, int end_pixel, long[] seq_range)
    {
        seq_range[0] = pixelToLeftSequenceCoordinate(start_pixel);
        seq_range[1] = pixelToRightSequenceCoordinate(end_pixel);
        seq_range[1] = seq_range[0] > seq_range[1] ? seq_range[0] : seq_range[1];
    }

    /**
     * Finds all LCBs intersecting a specified range of pixels in the current
     * view.
     * 
     * @param start_pixel
     *            The first coordinate of the intersection range
     * @param end_pixel
     *            The last coordinate of the intersection range
     */
    protected final List getLCBPixelRange(int start_pixel, int end_pixel)
    {
        long[] seq_range = new long[2];
        pixelRangeToSequenceCoordinates(start_pixel, end_pixel, seq_range);
    
        if (model instanceof LcbViewerModel)
        {
            return ((LcbViewerModel) model).getLCBRange(getGenome(), seq_range[0], seq_range[1]);
        }
        else
        {
            return null;
        }
    }

    protected final Genome getGenome()
    {
        return genome;
    }
    
    // Empty implementations of ModelListenerEvents, for clarity of subclasses.
    public void colorChanged(ModelEvent event) {}
    public void weightChanged(ModelEvent event) {}
    public void drawingSettingsChanged(ModelEvent event) {}
    public void modeChanged(ModelEvent event) {}
    public void modelReloadStart(ModelEvent event) {}
    public void modelReloadEnd(ModelEvent event) {}
    public void viewableRangeChangeStart(ModelEvent event) {}
    public void viewableRangeChanged(ModelEvent event) {}
    public void viewableRangeChangeEnd(ModelEvent event) {}
    public void genomesReordered(ModelEvent event) {}
    public void referenceChanged(ModelEvent event) {}
    public void genomeVisibilityChanged(ModelEvent event) {}
    
}
