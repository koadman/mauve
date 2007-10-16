package org.gel.mauve.gui;

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Vector;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.attribute.standard.PrinterResolution;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.ColorScheme;
import org.gel.mauve.HighlightListener;
import org.gel.mauve.LcbViewerModel;
import org.gel.mauve.ModelEvent;
import org.gel.mauve.ModelListener;
import org.gel.mauve.MyConsole;
import org.gel.mauve.ViewerMode;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.color.BackboneLcbColor;
import org.gel.mauve.color.BackboneMultiplicityColor;
import org.gel.mauve.color.LCBColorScheme;
import org.gel.mauve.color.MultiplicityColorScheme;
import org.gel.mauve.color.MultiplicityTypeColorScheme;
import org.gel.mauve.color.NormalizedMultiplicityTypeColorScheme;
import org.gel.mauve.color.NormalizedOffsetColorScheme;
import org.gel.mauve.color.OffsetColorScheme;
import org.gel.mauve.gui.sequence.RRSequencePanel;
import org.gel.mauve.gui.sequence.SeqPanel;

/**
 * The primary container class for the visualization interface. For every genome
 * being displayed, a RearrangementPanel contains a ruler (RulerPanel) and a
 * sequence display (RRSequencePanel). For each genome, the ruler displays
 * sequence coordinates currently in view, while the RRSequencePanel displays
 * some sort of similarity information: either the location of exact matches or
 * a similarity profile. The RearrangementPanel sets up the entire display and
 * coordinates its interface. It mediates shifts in the viewable range and other
 * user interaction. There are three primary information display modes supported
 * by the RearrangementPanel and associated classes. Mode 1) Display a set of
 * ungapped local alignments among multiple genomes. Mode 2) Display a set of
 * ungapped local alignments that have been grouped into locally collinear
 * blocks (LCBs). The LCBs are assumed to not overlap each other. In this
 * display mode, a bounding box around each LCB is usually drawn and an
 * LCBLinePanel can be used to draw connecting lines between LCB bounding boxes
 * Mode 3) Display a gapped global alignment of locally collinear blocks (LCBs)
 * stored in an XMFAAlignment object. A SimilarityIndex object is used to
 * calculate average sequence similarity among the genomes over any arbitrary
 * interval in one sequence. LCB bounding boxes are drawn around each LCB and
 * within each LCB the sequence similarity profile gets displayed. An
 * LCBLinePanel draws connecting lines among LCB bounding boxes in each genome.
 */
public class RearrangementPanel extends JLayeredPane implements ActionListener, ChangeListener, Scrollable, Printable, ModelListener
{
    private final static Integer LCB_PANEL = new Integer(2);
    private final static Integer SEQ_PANEL = new Integer(1);

    // The frame containing this RearrangementPanel
    public MauveFrame mauveFrame;

    // New-style SeqPanels.
    Vector newPanels = new Vector();

    // Panel that show lines connecing LCBs
    LcbLinePanel lcbLinePanel;

    // Panel showing the sequences, rulers, etc.
    JPanel sequencePanel;

    // Color scheme selector for matches
    JComboBox color_selector;

    // A toolbar for various alignment manipulation tools, given by the parent
    // frame
    JToolBar toolbar;

    // A slider to control the minimum weight of LCBs that get displayed. Greedy
    // breakpoint elimination is used to remove LCBs with weights below the
    // selected value.
    JSlider weight_slider = new JSlider(0, 100, 0);

    // A text field for weight slider
    JTextField weight_value_text = new JTextField(5);

    static final int CS_SELECT = 0;
    static final int CS_UNIV_ZOOM = 1;
    static final int CS_UNIV_MOVE = 2;
    static final int CS_SEQ_ZOOM = 3;
    static final int CS_SEQ_MOVE = 4;

    public static final Color bg_color = new Color(.85f, .85f, .85f);

    // Printer settings for this panel.
    PageFormat pageFormat = new PageFormat();

    // Used for printing.
    private double printingScale = -1;
    private int printingResolution = 600;
    
    // Data model.
    BaseViewerModel model;

    private boolean sliderAdjustmentInProcess = false;
    private boolean oldDrawMatches;
    private boolean oldFillBoxes;
    private JToggleButton zoom_button;
    private JToggleButton hand_button;
    
    // A weird hack to change cursor for zooming.
    private CtrlKeyDetector ctrlDetector = new CtrlKeyDetector();
    
    /** a list of objects listening for hint messages */
    private EventListenerList hintMessageListeners = new EventListenerList();
    
    /** variables controlling the display layout */
    private GridBagLayout gbl = new GridBagLayout();
    private GridBagConstraints gbc = new GridBagConstraints();
    static final double gbc_weighty = 5.0;

    /**
     * Does basic initialization for a RearrangementPanel. Call
     * readRearrangementData() to load and display data.
     * 
     * @param toolbar
     *            A toolbar which can be manipulated by this panel, or null to indicate no
     *            toolbar will be available
     */
    public RearrangementPanel(JToolBar toolbar)
    {
        setLayout(new FillLayout());
        this.toolbar = toolbar;
    }

    /**
     * Initialize all the GUI elements.
     */
    public void init(BaseViewerModel model)
    {
        this.model = model;
        initMatchDisplay();
        if (model instanceof LcbViewerModel)
        {
            initLCBTools();
        }
        model.addModelListener(this);
    }

    public BaseViewerModel getModel()
    {
        return model;
    }

    void setNewPanels(Vector v)
    {
        newPanels = v;
    }

    public SeqPanel getNewPanel(int i)
    {
        return (SeqPanel) newPanels.elementAt(i);
    }

    public RRSequencePanel getSequencePanel(int i)
    {
        return getNewPanel(i).getSequencePanel();
    }

    /**
     * This function reorders sequence data structures when the user has
     * requested a new ordering of sequences in the display.
     */
    public void reorderSequences(int new_order[])
    {
        // reorder the new panels, names etc.
        Vector tmp_newPanels = new Vector();
        for (int seqI = 0; seqI < model.getSequenceCount(); seqI++)
        {
            tmp_newPanels.addElement(getNewPanel(new_order[seqI]));
        }
        setNewPanels(tmp_newPanels);
        sequencePanel.removeAll();
        for (int seqI = 0; seqI < model.getSequenceCount(); seqI++)
        {
        	SeqPanel sp = getNewPanel(seqI);
        	if( seqI % 2 == 0 )
        		sp.setBackground(null);
        	else
        		sp.setBackground(Color.WHITE);
            sequencePanel.add(sp);
            gbc.weighty = model.getGenomeByViewingIndex(new_order[seqI]).getVisible() ? gbc_weighty : 0;
            gbl.setConstraints(sp, gbc);
        }

        // Reorder the underlying model (at some point, perhaps this
        // will no longer be necessary?
        model.reorderSequences(new_order);

        validate();
    }

    /**
     * Initialize the display environment. Set up a RRSequencePanel and a
     * RulerPanel for each sequence. also populate the toolbar and set up the
     * LCB line panel Register key bindings for various interface elements
     */
    protected void initMatchDisplay()
    {
        sequencePanel = new JPanel();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.ipadx = 0;
        gbc.ipady = 0;
        gbc.weightx = 1;
        gbc.weighty = gbc_weighty;
        gbc.gridy = GridBagConstraints.RELATIVE;
        sequencePanel.setLayout(gbl);
        add(sequencePanel, SEQ_PANEL);
        for (int seqI = 0; seqI < model.getSequenceCount(); seqI++)
        {
            SeqPanel seqPanel = new SeqPanel(model, model.getGenomeByViewingIndex(seqI), this);
            sequencePanel.add(seqPanel);
            gbl.setConstraints(seqPanel, gbc);
            newPanels.add(seqPanel);
        }
        initKeyBindings();
        initToolbar();
    }
    
    /**
     *  
     */
    private void initKeyBindings()
    {
        addKeyMapping("ctrl UP", "ZoomIn");
        addKeyMapping("ctrl DOWN", "ZoomOut");
        addKeyMapping("ctrl LEFT", "ScrollLeft");
        addKeyMapping("ctrl RIGHT", "ScrollRight");
        addKeyMapping("ctrl D", "DCJ");
        addKeyMapping("shift ctrl LEFT", "ShiftLeft");
        addKeyMapping("shift ctrl RIGHT", "ShiftRight");
        addKeyMapping("typed r", "ToggleStrikethrough");
        addKeyMapping("typed L", "ToggleLCBlines");
        addKeyMapping("typed h", "ToggleHandMode");
        addKeyMapping("typed q", "ToggleLcbBounds");
        addKeyMapping("typed w", "ToggleFillBoxes");
        addKeyMapping("typed e", "ToggleDrawMatches");
    }
    
    private void addKeyMapping(String stroke, String actionName)
    {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(stroke), actionName);
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(stroke), actionName);
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(stroke), actionName);
        getActionMap().put(actionName, new GenericAction(this, actionName));
    }
    
    private void removeKeyMapping(String stroke)
    {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(stroke));
    }
        

    private boolean haveNwayLcbData()
    {
    	if(model instanceof LcbViewerModel)
    	{
    		LcbViewerModel lm = (LcbViewerModel)model;
        	return lm.isNwayLcbList();
    	}else if (model instanceof XmfaViewerModel)
        {
        	XmfaViewerModel xmfa = (XmfaViewerModel)model;
        	return xmfa.isNwayLcbList();
        }
        return false;
    }
    
    private boolean haveBackboneData()
    {
        if (model instanceof XmfaViewerModel)
        {
        	XmfaViewerModel xmfa = (XmfaViewerModel)model;
        	if(xmfa.getBackboneList() != null)
        		return true;
        }
        return false;
    }
    /**
     *  
     */
    private void initToolbar()
    {
    	if(toolbar == null)
    		return;

        toolbar.removeAll();

        // When clicked, the home button resets the display to show each genome
        // in its entirety starting at position 1 */
        JButton home_button = new JButton(MauveFrame.home_button_icon);
        home_button.setToolTipText("Reset display");
        home_button.setActionCommand("Home");
        home_button.addActionListener(this);
        toolbar.add(home_button);

        // the hand button toggles between the "reorder sequences" mode
        // and standard browsing mode
        hand_button = new JToggleButton(MauveFrame.hand_button_icon);
        hand_button.setToolTipText("Toggle sequence reordering mode.");
        hand_button.setActionCommand("ToggleHandMode");
        hand_button.setPressedIcon(MauveFrame.dark_hand_button_icon);
        hand_button.setSelectedIcon(MauveFrame.dark_hand_button_icon);
        hand_button.addActionListener(this);
        toolbar.add(hand_button);

        // When clicked, the left button shifts the display 20% to the left
        JButton left_button = new JButton(MauveFrame.left_button_icon);
        left_button.setToolTipText("Shift display left (Ctrl+Left)");
        left_button.setActionCommand("ShiftLeft");
        left_button.addActionListener(this);
        toolbar.add(left_button);

        // When clicked, the right button shifts the display 20% to the right
        JButton right_button = new JButton(MauveFrame.right_button_icon);
        right_button.setToolTipText("Shift display right (Ctrl+Right)");
        right_button.addActionListener(this);
        right_button.setActionCommand("ShiftRight");
        toolbar.add(right_button);

        // When clicked, the zoom in button zooms the display 200% (halves the
        // displayed area)
        JButton zoomin_button = new JButton(MauveFrame.zoomin_button_icon);
        zoomin_button.setToolTipText("Zoom in (Ctrl+Up)");
        zoomin_button.setActionCommand("ZoomIn");
        zoomin_button.addActionListener(this);
        toolbar.add(zoomin_button);

        // When clicked, the zoom out button zooms the display 50% (doubles the
        // displayed area)
        JButton zoomout_button = new JButton(MauveFrame.zoomout_button_icon);
        zoomout_button.setToolTipText("Zoom out (Ctrl+Down)");
        zoomout_button.setActionCommand("ZoomOut");
        zoomout_button.addActionListener(this);
        toolbar.add(zoomout_button);
        
        zoom_button = new JToggleButton(MauveFrame.zoom_button_icon);
        zoom_button.setToolTipText("Zoom");
        zoom_button.setActionCommand("Zoom");
        zoom_button.addActionListener(this);
        toolbar.add(zoom_button);

        final RearrangementPanel thisrrpanel = this;
        JButton findFeatureButton = new JButton();
        findFeatureButton.setAction( new AbstractAction(){
        	public void actionPerformed(ActionEvent ae){
        		new SequenceNavigator(thisrrpanel, thisrrpanel, getModel()).showNavigator();
        	}
        });
        findFeatureButton.setToolTipText("Find an annotated feature... (Ctrl+I)");
        findFeatureButton.setIcon(MauveFrame.find_feature_icon);
        toolbar.add(findFeatureButton);

        // When clicked, the zoom out button zooms the display 50% (doubles the
        // displayed area)
        if (haveNwayLcbData())
        {
        	JButton dcj_button = new JButton(MauveFrame.dcj_icon);
	        dcj_button.setToolTipText("Perform a Block-Interchange (DCJ) rearrangement history analysis (Ctrl+D)");
	        dcj_button.setActionCommand("DCJ");
	        dcj_button.addActionListener(this);
	        toolbar.add(dcj_button);

	        JButton grimm_button = new JButton(MauveFrame.grimm_icon);
	        grimm_button.setToolTipText("Perform a GRIMM rearrangement history analysis (DCJ generalizes the GRIMM model)");
	        grimm_button.setActionCommand("GRIMM");
	        grimm_button.addActionListener(this);
	        toolbar.add(grimm_button);
        }
        
        initColorSelector();

        // Fill out the toolbar
        Dimension minSize = new Dimension(5, 3);
        Dimension prefSize = new Dimension(5, 3);
        Dimension maxSize = new Dimension(Short.MAX_VALUE, 3);
        toolbar.add(new Box.Filler(minSize, prefSize, maxSize));
    }

    /**
     * The color selector allows the choice of color schemes, in certain modes.
     */
    private void initColorSelector()
    {
    	if(toolbar == null)
    		return;
        if (model instanceof XmfaViewerModel)
        {
        	boolean have_bb = haveBackboneData();
        	if(have_bb)
        		color_selector = new JComboBox(new ColorScheme[] { new BackboneLcbColor(), new BackboneMultiplicityColor() });
        	else
        		color_selector = new JComboBox(new ColorScheme[] { new LCBColorScheme() });
            color_selector.setSelectedIndex(0);
        	if(!have_bb)
        		color_selector.setEnabled(false);
        }
        else if (model instanceof LcbViewerModel)
        {

            if (model.getSequenceCount() < 62)
            {
                color_selector = new JComboBox(new ColorScheme[] { new LCBColorScheme(), new OffsetColorScheme(), new NormalizedOffsetColorScheme(), new MultiplicityColorScheme(), new MultiplicityTypeColorScheme(), new NormalizedMultiplicityTypeColorScheme() });
            }
            else
            {
                color_selector = new JComboBox(new ColorScheme[] { new LCBColorScheme(), new OffsetColorScheme(), new NormalizedOffsetColorScheme(), new MultiplicityColorScheme() });
            }

            color_selector.setSelectedIndex(0);
        }
        else
        {
            color_selector = new JComboBox(new ColorScheme[] { new OffsetColorScheme(), new NormalizedOffsetColorScheme(), new MultiplicityColorScheme(), new MultiplicityTypeColorScheme(), new NormalizedMultiplicityTypeColorScheme() });
            color_selector.setSelectedIndex(0);

        }

        color_selector.addActionListener(this);
        toolbar.add(color_selector);
    }

    /**
     * Initialize data structures to support LCB display and manipulation.
     * Initializes the LCB weight slider.
     */
    protected void initLCBTools()
    {
        if (!(model instanceof LcbViewerModel))
            return;

        LcbViewerModel lm = (LcbViewerModel) model;

        //
        // add interface components
        //
        Dimension prefSize = new Dimension(5, 3);

        // add the line panel to the top pane
        lcbLinePanel = new LcbLinePanel(this, lm);
        add(lcbLinePanel, LCB_PANEL);
        lcbLinePanel.setVisible(true);

        // add the weight slider components, only if we have nway LCBs
        if(haveNwayLcbData() && toolbar != null)
        {
	        toolbar.add(new JLabel("LCB weight:"));
	
	        weight_slider.setMinimum(0);
	        weight_slider.setMaximum(lm.getLcbChangePoints().size() - 1);
	        weight_slider.setMinorTickSpacing(1);
	        weight_slider.setMajorTickSpacing((int) (lm.getLcbChangePoints().size() / 10));
	        weight_slider.setToolTipText("Set the minimum weight for Locally Collinear Blocks");
	        weight_slider.setPaintTicks(true);
	        weight_slider.setPaintLabels(false);
	        weight_slider.setSnapToTicks(false);
	        prefSize = weight_slider.getPreferredSize();
	        prefSize.setSize(250, prefSize.getHeight());
	        weight_slider.setPreferredSize(prefSize);
	        weight_slider.setMaximumSize(prefSize);
	        toolbar.add(weight_slider);
	        weight_slider.addChangeListener(this);
	
	        prefSize = weight_value_text.getPreferredSize();
	        prefSize.setSize(40, prefSize.getHeight());
	        weight_value_text.setPreferredSize(prefSize);
	        weight_value_text.setMaximumSize(prefSize);
	        weight_value_text.setHorizontalAlignment(JTextField.RIGHT);
	        weight_value_text.setEditable(false);
	        if(lm.getLcbChangePoints().size() > 0)
	        	weight_value_text.setText(lm.getLcbChangePoints().elementAt(0).toString());
	        toolbar.add(weight_value_text);
        }

        // tell the user how many LCBs there are
        String status_text = "There are " + lm.getVisibleLcbCount() + " LCBs with minimum weight " + lm.getMinLCBWeight();
        fireHintMessageEvent(status_text);
    }

    /**
     * Add a HintMessageListener to the list of listeners for the model.
     * 
     * @param l listener to add
     */
    public void addHintMessageListener(HintMessageListener l) {
    	hintMessageListeners.add(HintMessageListener.class, l);
    }

    /**
     * Remove a HintMessageListener from the list of listeners for this model.
     * 
     * @param l listener to remove
     */
    public void removeHintMessageListenerListener(HintMessageListener l) {
    	hintMessageListeners.remove(HintMessageListener.class, l);
    }
    
    /**
     * Invoke {@link HintMessageListener.messageChanged(ModelEvent)} on
     * this model's collection of HintMessageListener.
     */
    protected void fireHintMessageEvent(String message) {
        Object[] listeners = hintMessageListeners.getListenerList();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i]==HintMessageListener.class) {
                ((HintMessageListener)listeners[i+1]).messageChanged(new HintMessageEvent(this,message));
            }
        }
    }
    
    // CHANGE LISTENER METHODS

    public void stateChanged(ChangeEvent e)
    {
        if (e.getSource() == weight_slider)
        {

            if (!(model instanceof LcbViewerModel))
                return;

            LcbViewerModel model = (LcbViewerModel) this.model;

            // compute the updated list of LCBs
            int w_int = weight_slider.getValue();
            int min_weight = ((Integer) model.getLcbChangePoints().elementAt(w_int)).intValue();
            weight_value_text.setText(model.getLcbChangePoints().elementAt(w_int).toString());

            if (weight_slider.getValueIsAdjusting())
            {
                if (model instanceof LcbViewerModel)
                {
                    LcbViewerModel lm = (LcbViewerModel) model;
                    if (!sliderAdjustmentInProcess)
                    {
                        oldFillBoxes = lm.getFillLcbBoxes();
                    }
                    lm.setFillLcbBoxes(true);
                }
                
                if (!sliderAdjustmentInProcess)
                {
                    oldDrawMatches = model.getDrawMatches();
                }
                model.setDrawMatches(false);
                sliderAdjustmentInProcess = true;
            }
            else
            {
                if (model instanceof LcbViewerModel)
                {
                    ((LcbViewerModel) model).setFillLcbBoxes(oldFillBoxes);
                }
                model.setDrawMatches(oldDrawMatches);
                sliderAdjustmentInProcess = false;
            }
            
            model.updateLCBweight(min_weight, weight_slider.getValueIsAdjusting());

            if (weight_slider.getValueIsAdjusting())
            {
                String status_text = "There are " + model.getVisibleLcbCount() + " LCBs with minimum weight " + min_weight;
                fireHintMessageEvent(status_text);
            }
        }
    }

    // ACTION LISTENER METHODS

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == color_selector)
        {
            model.setColorScheme((ColorScheme) color_selector.getSelectedItem());
        }
        else if (e.getActionCommand().equals("Home"))
        {
            model.zoomAndMove(0, Integer.MIN_VALUE);
            
            //			zoom( 100, 100 );
        }
        else if (e.getActionCommand().equals("ShiftLeft") || e.getActionCommand().equals("ScrollLeft"))
        {
            if ((e.getModifiers() & InputEvent.SHIFT_MASK) != 0)
                // big jump left
                model.zoomAndMove(100, -50);
            else
                // scroll left
                model.zoomAndMove(100, -10);
        }
        else if (e.getActionCommand().equals("ShiftRight") || e.getActionCommand().equals("ScrollRight"))
        {
            if ((e.getModifiers() & InputEvent.SHIFT_MASK) != 0)
            {
                // big jump right
                model.zoomAndMove(100, 50);
            }
            else
            {
                // scroll right
                model.zoomAndMove(100, 10);
            }
        }
        else if (e.getActionCommand().equals("ZoomIn"))
        {
            model.zoomAndMove(200, 0);
        }
        else if (e.getActionCommand().equals("ZoomOut"))
        {
            model.zoomAndMove(50, 0);
        }
        else if (e.getActionCommand().equals("DCJ"))
        {
        	if( model instanceof LcbViewerModel )
        	{
        		LcbViewerModel lvm = (LcbViewerModel)model;
        		lvm.launchDCJ();
        	}
        }
        else if (e.getActionCommand().equals("GRIMM"))
        {
        	if( model instanceof LcbViewerModel )
        	{
        		LcbViewerModel lvm = (LcbViewerModel)model;
        		lvm.launchGrimmMGR();
        	}
        }
        else if (e.getActionCommand().equals("Zoom"))
        {
            if (model.getMode() == ViewerMode.ZOOM)
            {
                model.setMode(ViewerMode.NORMAL);
            }
            else
            {
                model.setMode(ViewerMode.ZOOM);
            }
        }
        else if (e.getActionCommand().equals("ToggleHandMode"))
        {
            if (model.getMode() == ViewerMode.HAND)
            {
                model.setMode(ViewerMode.NORMAL);
            }
            else
            {
                model.setMode(ViewerMode.HAND);
            }
        }
        else if (e.getActionCommand().equals("ToggleLCBlines"))
        {
        	lcbLinePanel.setHidden(!lcbLinePanel.getHidden());
        }
        else if (e.getActionCommand().equals("ToggleStrikethrough"))
        {
            lcbLinePanel.draw_strikethrough = !lcbLinePanel.draw_strikethrough;
            lcbLinePanel.repaint();
        }
        else if (e.getActionCommand().equals("ToggleLcbBounds"))
        {
            if (model instanceof LcbViewerModel)
            {
                LcbViewerModel lv = (LcbViewerModel) model;
                lv.setDrawLcbBounds(!lv.getDrawLcbBounds());
            }
        }
        else if (e.getActionCommand().equals("ToggleFillBoxes"))
        {
            if (model != null)
            {
                if (model instanceof LcbViewerModel)
                {
                    LcbViewerModel lv = (LcbViewerModel) model;
                    lv.setFillLcbBoxes(!lv.getFillLcbBoxes());
                }
            }
        }
        else if (e.getActionCommand().equals("ToggleDrawMatches"))
        {
            if (model != null)
            {
                model.setDrawMatches(!model.getDrawMatches());
                
            }
        }
        else if (e.getActionCommand().equals("ZoomInMode"))
        {
            setCursor(MauveFrame.zoom_in_cursor);
        }
        else if (e.getActionCommand().equals("ZoomOutMode"))
        {
            setCursor(MauveFrame.zoom_out_cursor);
        }
    }
    public Dimension getPreferredSize()
    {
    	Dimension min_size = new Dimension();
    	Dimension max_size = new Dimension();    	
    	// add up the minimum and maximum sizes of each sequence panel
    	for( int seqI = 0; seqI < model.getSequenceCount(); seqI++ ){
    		min_size.height += this.getNewPanel(seqI).getMinimumSize().height;
    		max_size.height += this.getNewPanel(seqI).getMaximumSize().height;
    	}
    	// set the preferred height to be no less than the minimum
    	// and no more than the maximum
    	Dimension preferred_size = this.getParent().getSize();
    	Dimension super_size = super.getPreferredSize();
    	preferred_size.width = super_size.width;
    	if( preferred_size.height < min_size.height )
    		preferred_size.height = min_size.height;
    	else if( preferred_size.height > max_size.height )
    		preferred_size.height = max_size.height;
    	else      						// TODO: clean up this hack!!
    		preferred_size.height -= 5;	// the height is just a little too big
    	return preferred_size;
    }

    /**
     * @see javax.swing.Scrollable#getScrollableTracksViewportHeight()
     */
    public boolean getScrollableTracksViewportHeight()
    {
        // Don't match component height to viewport height.
        return false;
    }

    boolean scrollableTracksViewportWidth = true;	// default to true
    /**
     * Match component width to viewport width?
     */ 
    public void setScrollableTracksViewportWidth(boolean matchWidth)
    {
    	scrollableTracksViewportWidth = matchWidth;
    }
    /**
     * @see javax.swing.Scrollable#getScrollableTracksViewportWidth()
     */
    public boolean getScrollableTracksViewportWidth()
    {
        // Match component width to viewport width.
        return scrollableTracksViewportWidth;
    }

    /**
     * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
     */
    public Dimension getPreferredScrollableViewportSize()
    {
        return getPreferredSize();
    }

    /**
     * @see javax.swing.Scrollable#getScrollableBlockIncrement(java.awt.Rectangle,
     *      int, int)
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return 10;
    }

    /**
     * @see javax.swing.Scrollable#getScrollableUnitIncrement(java.awt.Rectangle,
     *      int, int)
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.print.Printable#print(java.awt.Graphics,
     *      java.awt.print.PageFormat, int)
     */
    public int print(Graphics g, PageFormat pageFormat, int pageIndex)
    {
        if (pageIndex > 0)
        {
            return NO_SUCH_PAGE;
        }
        else
        {
            Graphics2D g2d = (Graphics2D) g;
            
            if (printingScale == -1)
            {
                printingScale = pageFormat.getImageableWidth() / getWidth();
            }
            
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g2d.scale(printingScale, printingScale);
            PrintUtilities.disableDoubleBuffering(this);
            
            g2d.setRenderingHint(MauveRenderingHints.KEY_SIMILARITY_DENSITY, new Double(72d / (printingResolution * printingScale)));
            paint(g2d);

            PrintUtilities.enableDoubleBuffering(this);
            return PAGE_EXISTS;
        }
    }
    
    public void print()
    {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(this, pageFormat);
        
        if (printJob.printDialog())
        {
            try
            {
                printingScale = -1;
                printingResolution = determineResolution(printJob);
                printJob.print();
            }
            catch (PrinterException pe)
            {
                MyConsole.err().println("Error printing: " + pe);
            }
        }
    }
    
    /**
     * @param printJob
     */
    private static int determineResolution(PrinterJob printJob)
    {
        PrintService ps = printJob.getPrintService();
        // If we can't tell, assume 300 dpi.
        if (ps == null || !ps.isAttributeCategorySupported(PrinterResolution.class) || ps.isDocFlavorSupported(DocFlavor.SERVICE_FORMATTED.PRINTABLE))
        {
            return 300;
        }
        
        // Try 300 dpi.
        PrinterResolution pr = new PrinterResolution(300,300,PrinterResolution.DPI);
        if (ps.isAttributeValueSupported(pr, DocFlavor.SERVICE_FORMATTED.PRINTABLE, null))
        {
            return 300;
        }
            
        // Fourth down: punt!
        return 72;
    }

    public void pageSetup()
    {
        PrinterJob junkJob = PrinterJob.getPrinterJob();
        pageFormat = junkJob.pageDialog(pageFormat);
    }

    public void colorChanged(ModelEvent event)
    {
        // Ignored.
    }

    public void weightChanged(ModelEvent event)
    {
        // Ignored.
    }

    public void drawingSettingsChanged(ModelEvent event)
    {
        // Ignored.
    }
    
    public void referenceChanged(ModelEvent event)
    {
        // Ignored.
    }

    public void modeChanged(ModelEvent event)
    {
        if (model.getMode() == ViewerMode.NORMAL)
        {
            hand_button.setSelected(false);
            zoom_button.setSelected(false);
            setCursor(null);
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(ctrlDetector);
        }
        else if (model.getMode() == ViewerMode.HAND)
        {
            zoom_button.setSelected(false);
            setCursor(MauveFrame.hand_cursor);
            
            // See javadoc for CtrlKeyDetector for an explanation of the next line.
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(ctrlDetector);
        }
        else if (model.getMode() == ViewerMode.ZOOM)
        {
            hand_button.setSelected(false);
            setCursor(MauveFrame.zoom_in_cursor);

            // See javadoc for CtrlKeyDetector for an explanation of the next line.
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ctrlDetector);
        }
    }

    
    /**
     * CtrlKeyDetector is used to capture the press and release of the Ctrl key
     * so that the cursor for zooming in and out can be changed.  This is necessary
     * because the normal key event handling routines do not provide access to events
     * where just the Ctrl key and no other keys are pressed or released.  
     */
    class CtrlKeyDetector implements KeyEventDispatcher {
        public boolean dispatchKeyEvent(KeyEvent e) {
            if( e.getKeyCode() == KeyEvent.VK_CONTROL ) {
                switch( e.getID() ) {
                	case KeyEvent.KEY_PRESSED:
                        setCursor(MauveFrame.zoom_out_cursor);
                	    break;
                	case KeyEvent.KEY_RELEASED:
                        setCursor(MauveFrame.zoom_in_cursor);
                	    break;
                }
            }
            return false;
        }
    }

    public void viewableRangeChanged(ModelEvent event)
    {
        // Ignored
    }

    public void viewableRangeChangeStart(ModelEvent event)
    {
        // Ignored.
    }

    public void viewableRangeChangeEnd(ModelEvent event)
    {
        // Ignored.
    }

    public void genomeVisibilityChanged(ModelEvent event)
    {
    	// update constraints, setting invisible genomes to weighty 0
    	for(int i = 0; i < model.getSequenceCount(); i++)
    	{
    		gbc.weighty = model.getGenomeByViewingIndex(i).getVisible() ? gbc_weighty : 0;
    		gbl.setConstraints(getNewPanel(i), gbc);
    	}
	}

    public void modelReloadStart(ModelEvent event)
    {
        // Ignored.
    }
    
    public void modelReloadEnd(ModelEvent event)
    {
        // Ignored.
    }

    public void genomesReordered(ModelEvent event)
    {
        // TODO Auto-generated method stub
        
    }

}

/**
 * Generic class to propagate action events to a listener
 */

class GenericAction extends AbstractAction
{

    GenericAction(ActionListener al, String command)
    {
        super(command);
        this.al = al;
        putValue(ACTION_COMMAND_KEY, command);
    }

    ActionListener al;

    public void actionPerformed(ActionEvent e)
    {
        al.actionPerformed(e);
    }
}


