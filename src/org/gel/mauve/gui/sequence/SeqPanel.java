/*
 * Created on Jan 19, 2005
 */
package org.gel.mauve.gui.sequence;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.TooManyListenersException;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Genome;
import org.gel.mauve.ModelEvent;
import org.gel.mauve.MyConsole;
import org.gel.mauve.ViewerMode;
import org.gel.mauve.gui.MauveFrame;
import org.gel.mauve.gui.RearrangementPanel;
import org.gel.mauve.gui.dnd.TransferableObject;

/**
 * @author Paul Infield-Harm
 * 
 * A panel that shows a ruler, sequence (and eventually features) for a genome
 * sequence.
 */
public class SeqPanel extends AbstractSequencePanel implements MouseListener
{
    // Default percentage of space for ruler in this panel
    static final float RULER_RATIO = .05f;
    
    //weighty for boxlayout for each layer of features
    public static final float BOX_FEATURE_WEIGHT = .01f;
    private RRSequencePanel sequence;
    private RulerPanel ruler;
    protected FeaturePanel feature;
    private ControlPanel controls;
    private JLabel label;
    
        
    private RearrangementPanel rrPanel;
    private static final DataFlavor REARRANGEMENT_FLAVOR = new DataFlavor(Integer.class, TransferableObject.MIME_TYPE);

    private Dimension min_size;
    private Dimension my_size;
    private Dimension my_max_size;
    
    private Dimension invisible_size;

    public SeqPanel(BaseViewerModel model, Genome genome, RearrangementPanel rearrangementPanel)
    {
        super(model, genome);
        
        this.rrPanel = rearrangementPanel;
        
        // set the minimum dimensions
        min_size = this.getSize();
        min_size.height = 100;
        this.setMinimumSize(min_size);

        // calculate the preferred and maximum dimensions, set them below
        my_size = this.getSize();
        my_size.height = 115;	// start with 115, add more if a FeaturePanel is used
        my_size.width = 10000;
        my_max_size = this.getSize();
        my_max_size.height = 175;	// start with 175, add more if a FeaturePanel is used
        my_max_size.width = 10000;
        
        invisible_size = this.getSize();
        invisible_size.width = 10000;
        invisible_size.height = 20;
        this.setMinimumSize(invisible_size);        
        
        controls = new ControlPanel(model, genome, rearrangementPanel);
        controls.setOpaque(true);

        ruler = new RulerPanel(model, genome);
        ruler.setOpaque(true);

        // Add the sequence
        sequence = new RRSequencePanel(rearrangementPanel, model, genome);
        sequence.setOpaque(true);

        // Add the feature panel, if desired.
        if (genome.getAnnotationSequence() != null)
        {
            feature = new FeaturePanel(genome, model);
            addToSize (FeaturePanel.DEFAULT_HEIGHT, false);
        }


        // Add message if no annotations found, and this is XMFA.
        if (genome.getAnnotationSequence() == null)
        {
            label = new JLabel(genome.getDisplayName() + " (no annotations loaded)");
        }
        else
        {
            label = new JLabel(genome.getDisplayName());
        }
        label.setMaximumSize(new Dimension(100000, 15 ));
        
        if(genome.getViewIndex() % 2 == 1)
        	setBackground(Color.WHITE);
        
        configureLayout();
    }
    
    private GridBagConstraints getDefaultControlPanelGridBagConstraints()
    {
    	GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.VERTICAL;
        c.gridx=GridBagConstraints.RELATIVE;
        c.gridy=GridBagConstraints.RELATIVE;
        c.gridwidth = 1;
        c.gridheight=GridBagConstraints.REMAINDER;
        c.insets = new Insets(0,0,0,0);
        c.ipadx = 0;
        c.ipady = 0;
        c.weighty=0;
        c.weightx=0;
        return c;
    }
    
    private GridBagConstraints getDefaultContentPanelGridBagConstraints()
    {
    	GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.gridx=GridBagConstraints.RELATIVE;
        c.gridy=GridBagConstraints.RELATIVE;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.insets = new Insets(0,0,0,0);
        c.ipadx = 0;
        c.ipady = 0;
        c.weighty=0;
        c.weightx = 1.0;
        return c;
    }

    /**
     * Don't render the control panel when printing
     */
    protected void configureLayout()
    {
    	removeAll();
        GridBagLayout layoutManager = new GridBagLayout();
        setLayout(layoutManager);        
        GridBagConstraints c = getDefaultControlPanelGridBagConstraints();

        // add the control panel only if we're not printing 
        if(!printing)
        {
	        add(controls);
	        layoutManager.setConstraints(controls, c);
        }

        c = getDefaultContentPanelGridBagConstraints();

        if(printing)
        {
        	// treat the width differently if printing
	        c.gridwidth = GridBagConstraints.REMAINDER;    	
	        c.weightx = 1.0;
	        c.gridx = 0;
        }
        
        if( getGenome().getVisible() )
        {
        	c.weighty = RULER_RATIO;
	        add(ruler);
	        layoutManager.setConstraints(ruler, c);
	
	        c.weighty = 2.0;
	        add(sequence);
	        layoutManager.setConstraints(sequence, c);
	
	        if (getGenome().getAnnotationSequence() != null)
	        {
	            add(feature);
	            c.weighty = 0.1;
	            layoutManager.setConstraints(feature, c);
	        }
	
	        // Add the name.
	        c.weighty = 0.1;
	        add(label);
	        layoutManager.setConstraints(label, c);
	        updateSize ();
	    }else
	    {
	    	// genome not visible...
	        // just add the name.
	        c.weighty = 0.1;
	        add(label);
	        layoutManager.setConstraints(label, c);
	        setAllSizes(invisible_size);
	    }
    }
    
    /**
     * add amount to size
     */
    public void addToSize (int grow, boolean min) {
        my_size.height += grow;
        my_max_size.height += grow;
        if (min)
        	min_size.height += grow;
    }
     
    /**
     * updated size using class variables
     */
    public void updateSize () {
        setPreferredSize(my_size);
        setMaximumSize(my_max_size);
        setMinimumSize(min_size);
        setSize(my_size);
        if (isVisible () && getParent () != null) {
        	getParent ().invalidate ();
        	((JComponent) getParent ()).revalidate ();
        }
    }
    
    /**
     * Sets all current, preferred max and min sizes to d
     * @param d
     */
    private void setAllSizes(Dimension d)
    {
        setPreferredSize(d);
        setMaximumSize(d);    	
        setMinimumSize(d);
        setSize(d);
    }
    
    public RRSequencePanel getSequencePanel()
    {
        return sequence;
    }
    public FeaturePanel getFeaturePanel()
    {
        return feature;
    }
    public ControlPanel getControlPanel()
    {
        return controls;
    }

    public void genomeVisibilityChanged(ModelEvent me)
    {
    	configureLayout();
    }

    public void setBackground(java.awt.Color bg)
    {
    	super.setBackground(bg);
    	if(sequence != null)
    		sequence.setBackground(bg);
    	if(ruler != null)
    		ruler.setBackground(bg);
    	if(feature != null)
    		feature.setBackground(bg);
    }
    
    /******* Get rid of controls when printing ***************/
    private boolean printing = false;
    public void printingStart(ModelEvent event)
    {
    	printing = true;
    	configureLayout();
    }
    public void printingEnd(ModelEvent event)
    {
    	printing = false;
    	configureLayout();
    }

    
    /****************** Mouse listener events ********************/
    
    public void mouseClicked(MouseEvent e)
    {
        long coord = pixelToCenterSequenceCoordinate(e.getX());
        
        if (e.isControlDown())
        {
            model.zoomAndCenter(getGenome(), 50, coord);
        }
        else
        {
            model.zoomAndCenter(getGenome(), 200, coord);
        }
        
    }

    public void mouseEntered(MouseEvent e)
    {
        // Ignored.
    }

    public void mouseExited(MouseEvent e)
    {
        // Ignored.
    }

    public void mousePressed(MouseEvent e)
    {
        // Ignored.
    }

    public void mouseReleased(MouseEvent e)
    {
        // Ignored.
    }

}