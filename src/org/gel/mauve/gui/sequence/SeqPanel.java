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
public class SeqPanel extends AbstractSequencePanel implements DragGestureListener, DragSourceListener, DropTargetListener, MouseListener
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
            my_size.height += FeaturePanel.DEFAULT_HEIGHT;
            my_max_size.height += FeaturePanel.DEFAULT_HEIGHT;
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
        
        if(genome.getVisible())
        	doVisibleLayout();
        else
        	doInvisibleLayout();
    }
    
    protected void doVisibleLayout()
    {
    	removeAll();
        GridBagLayout layoutManager = new GridBagLayout();
        setLayout(layoutManager);
        GridBagConstraints c = new GridBagConstraints();

        // Add the ruler.
        // add the control panel
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
        add(controls);
        layoutManager.setConstraints(controls, c);

    	
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = 1;
        c.gridx = GridBagConstraints.RELATIVE;
        c.weightx = 1.0;

        c.weighty = RULER_RATIO;
        add(ruler);
        layoutManager.setConstraints(ruler, c);

        c.weighty = 2.0;
        add(sequence);
        layoutManager.setConstraints(sequence, c);

        if (getGenome().getAnnotationSequence() != null)
        {
            addFeatures (c);
        }

        // Add the name.
        c.weighty = 0.1;
        add(label);
        layoutManager.setConstraints(label, c);
        setPreferredSize(my_size);
        setMaximumSize(my_max_size);
        setMinimumSize(min_size);
        setSize(my_size);
    }
    
    protected void addFeatures (GridBagConstraints c) {
    	GridBagLayout layoutManager = (GridBagLayout) getLayout ();
    	add(feature);
        c.weighty = BOX_FEATURE_WEIGHT;
        layoutManager.setConstraints(feature, c);
    }

    public void doInvisibleLayout()
    {
    	removeAll();
        GridBagLayout layoutManager = new GridBagLayout();
        setLayout(layoutManager);
        GridBagConstraints c = new GridBagConstraints();

        // Add the ruler.
        // add the control panel
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
        add(controls);
        layoutManager.setConstraints(controls, c);

    	
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = 1;
        c.gridx = GridBagConstraints.RELATIVE;
        c.weightx = 1.0;

        // Add the name.
        c.weighty = 0.1;
        add(label);
        layoutManager.setConstraints(label, c);

        setPreferredSize(invisible_size);
        setMaximumSize(invisible_size);    	
        setMinimumSize(invisible_size);
        setSize(invisible_size);
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
        if(getGenome().getVisible())
        	doVisibleLayout();
        else
        	doInvisibleLayout();
    }
    /**
     * @see java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.DragGestureEvent)
     */
    public void dragGestureRecognized(DragGestureEvent event)
    {
        final Integer index = new Integer(getGenome().getViewIndex());
        Transferable transfer = new TransferableObject(index.getClass(), new TransferableObject.Fetcher()
        {
            /**
             * This will be called when the transfer data is requested at the
             * very end. At this point we can remove the object from its
             * original place in the list.
             */
            public Object getObject()
            {
                return index;
            }
        });

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.DragSourceDragEvent)
     */
    public void dragEnter(DragSourceDragEvent arg0)
    {
        // This space intentionally left blank.
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent)
     */
    public void dragOver(DragSourceDragEvent arg0)
    {
        // This space intentionally left blank.
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.DragSourceDragEvent)
     */
    public void dropActionChanged(DragSourceDragEvent arg0)
    {
        // This space intentionally left blank.
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.dnd.DragSourceListener#dragDropEnd(java.awt.dnd.DragSourceDropEvent)
     */
    public void dragDropEnd(DragSourceDropEvent arg0)
    {
        // This space intentionally left blank.
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
     */
    public void dragExit(DragSourceEvent arg0)
    {
        // This space intentionally left blank.
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
     */
    public void dragEnter(DropTargetDragEvent evt)
    {
        evt.acceptDrag(DnDConstants.ACTION_MOVE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
     */
    public void dragOver(DropTargetDragEvent arg0)
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
     */
    public void dropActionChanged(DropTargetDragEvent evt)
    {
        evt.acceptDrag(DnDConstants.ACTION_MOVE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
     */
    public void drop(DropTargetDropEvent evt)
    {
        Transferable t = evt.getTransferable();

        // If it's our native TransferableObject, use that
        if (t.isDataFlavorSupported(REARRANGEMENT_FLAVOR))
        {
            evt.acceptDrop(DnDConstants.ACTION_MOVE);
            Object obj = null;
            try
            {
                obj = t.getTransferData(REARRANGEMENT_FLAVOR);
            }
            catch (UnsupportedFlavorException e)
            {
                // We just checked, so this is quite unexpected.
                throw new RuntimeException(e);
            }
            catch (IOException e)
            {
                MyConsole.err().println("Drag and drop data no longer available.");
                e.printStackTrace(MyConsole.err());
                evt.rejectDrop();
                return;
            }

            int sourceIndex = -1;

            // See where in the list we dropped the element.
            sourceIndex = ((Integer) obj).intValue();
            if (getGenome().getViewIndex() != sourceIndex)
            {
                int[] new_order = new int[model.getSequenceCount()];
                int seqI = 0, seqJ = 0;
                for (; seqI < model.getSequenceCount();)
                {
                    if (seqI == getGenome().getViewIndex())
                    {
                        new_order[seqI] = sourceIndex;
                        seqI++;
                    }
                    else if (seqJ == sourceIndex)
                    {
                        // skip this one
                        seqJ++;
                    }
                    else
                    {
                        new_order[seqI] = seqJ;
                        seqI++;
                        seqJ++;
                    }
                }
                rrPanel.reorderSequences(new_order);
            }

            evt.getDropTargetContext().dropComplete(true);
        }
        else
        {
            evt.rejectDrop();
        }
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
    /*
     * (non-Javadoc)
     * 
     * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
     */
    public void dragExit(DropTargetEvent arg0)
    {
    }

    public void modeChanged(ModelEvent event)
    {
        // Hand mode toggles drag and drop.
        if (model.getMode() == ViewerMode.NORMAL)
        {
            disableZoom();
        }
        else if (model.getMode() == ViewerMode.ZOOM)
        {
            enableZoom();
        }
    }

    /***************** Zoom stuff ********************************/

    private void disableZoom()
    {
        removeMouseListener(this);
    }
    
    private void enableZoom()
    {
        addMouseListener(this);
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