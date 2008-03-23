package org.gel.mauve.gui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.ModelProgressListener;

public class MauveFrame extends JFrame implements ModelProgressListener {
	
	protected Mauve mauve;
	protected MauvePanel mauve_panel;
	protected BaseViewerModel model;
	protected RearrangementPanel rrpanel;
	
	
    static ImageIcon home_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/Home16.gif"));
    static ImageIcon left_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/Back16.gif"));
    static ImageIcon right_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/Forward16.gif"));
    static ImageIcon zoomin_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/ZoomIn16.gif"));
    static ImageIcon zoomout_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/ZoomOut16.gif"));
    static ImageIcon zoom_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/Zoom16.gif"));
    public static ImageIcon mauve_icon = new ImageIcon(MauveFrame.class.getResource("/images/mauve_icon.gif"));
    static ImageIcon find_feature_icon = new ImageIcon(MauveFrame.class.getResource("/images/searchBinoculars16.png"));
    static ImageIcon dcj_icon = new ImageIcon(MauveFrame.class.getResource("/images/Dcj16.gif"));
    static ImageIcon grimm_icon = new ImageIcon(MauveFrame.class.getResource("/images/Grimm16.gif"));
    static Cursor zoom_in_cursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(MauveFrame.class.getResource("/images/ZoomIn24.gif")).getImage(), new Point(8, 8), "zoomin");
    static Cursor zoom_out_cursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(MauveFrame.class.getResource("/images/ZoomOut24.gif")).getImage(), new Point(8, 8), "zoomout");

	public MauveFrame (MauvePanel mv) {
		mauve_panel = mv;
		mauve = mauve_panel.mauve;
		model = mauve_panel.model;
		rrpanel = mauve_panel.rrpanel;
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Mauve " + mauve.getVersion() + " - Genome Alignment Visualization");
        setIconImage(mauve_icon.getImage());
        getContentPane ().add (mauve_panel);
		setLocation(new java.awt.Point(0, 0));
		mauve_panel.setPanelCloseListener(this);
        Dimension dim = new java.awt.Dimension(791, 500);
        setSize(dim);
        setPreferredSize (dim);
		setVisible (true);
	}
	
	public MauveFrame (Mauve mv) {
		this (new MauvePanel (mv));
	}
	
	public void getFocus () {
		toFront ();
	}
	
	public MauvePanel getPanel () {
		return mauve_panel;
	}

	
	//following methods for backward compatibility
    public void buildStart()
    {
        mauve_panel.buildStart();
    }

    public void downloadStart()
    {
    	mauve_panel.downloadStart ();
    }

    public void alignmentStart()
    {
    	mauve_panel.alignmentStart ();
    }

    public void alignmentEnd(int sequenceCount)
    {
    	mauve_panel.alignmentEnd (sequenceCount);
    }

    public void featureStart(int sequenceIndex)
    {
        mauve_panel.featureStart(sequenceIndex);
    }

    public void done()
    {
    	mauve_panel.done ();
    }
    
    public BaseViewerModel getModel()
    {
        return mauve_panel.getModel();
    }
    
	public RearrangementPanel getRearrangementPanel () {
		return mauve_panel.getRearrangementPanel();
	}
    
    public void setModel(BaseViewerModel model)
    {
    	mauve_panel.setModel(model);
    }
    
    public void reset()
    {
    	mauve_panel.reset();
    }
	
}
