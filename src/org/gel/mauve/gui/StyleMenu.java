package org.gel.mauve.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.LcbViewerModel;
import org.gel.mauve.XmfaViewerModel;

public class StyleMenu extends JMenu implements ActionListener {

    JCheckBoxMenuItem jMenuViewStyleLcbOutlines = new JCheckBoxMenuItem();
    JCheckBoxMenuItem jMenuViewStyleSimilarityPlot = new JCheckBoxMenuItem();
    JCheckBoxMenuItem jMenuViewStyleSolidBlocks = new JCheckBoxMenuItem();
    JCheckBoxMenuItem jMenuViewStyleLcbStrikethroughLines = new JCheckBoxMenuItem();
    JCheckBoxMenuItem jMenuViewStyleLcbConnectingLines = new JCheckBoxMenuItem();
    JCheckBoxMenuItem jMenuViewStyleChromosomeBoundaries = new JCheckBoxMenuItem();
    JCheckBoxMenuItem jMenuViewStyleMouseHighlighting = new JCheckBoxMenuItem();
    JCheckBoxMenuItem jMenuViewStyleDrawAttributes = new JCheckBoxMenuItem();
    
    BaseViewerModel model;
    RearrangementPanel rrpanel;

    public StyleMenu()
    {
        jMenuViewStyleLcbOutlines.setToolTipText("Draw outlines around each Locally Collinear Block");
        jMenuViewStyleLcbOutlines.setVisible(true);
        jMenuViewStyleLcbOutlines.setText("LCB outlines");
        jMenuViewStyleLcbOutlines.setMnemonic('o');
        jMenuViewStyleLcbOutlines.setActionCommand("ToggleLcbBounds");
        jMenuViewStyleLcbOutlines.addActionListener(this);

        jMenuViewStyleSimilarityPlot.setToolTipText("Draw a similarity plot indicating average similarity of each region");
        jMenuViewStyleSimilarityPlot.setVisible(true);
        jMenuViewStyleSimilarityPlot.setText("Similarity plot");
        jMenuViewStyleSimilarityPlot.setMnemonic('S');
        jMenuViewStyleSimilarityPlot.setActionCommand("ToggleDrawMatches");
        jMenuViewStyleSimilarityPlot.addActionListener(this);

        jMenuViewStyleSolidBlocks.setToolTipText("Draw solid colors for each locally collinear block");
        jMenuViewStyleSolidBlocks.setVisible(true);
        jMenuViewStyleSolidBlocks.setText("Solid LCB coloring");
        jMenuViewStyleSolidBlocks.setMnemonic('L');
        jMenuViewStyleSolidBlocks.setActionCommand("ToggleFillBoxes");
        jMenuViewStyleSolidBlocks.addActionListener(this);

        jMenuViewStyleLcbStrikethroughLines.setToolTipText("Draw connecting lines that strike through LCBs");
        jMenuViewStyleLcbStrikethroughLines.setVisible(true);
        jMenuViewStyleLcbStrikethroughLines.setText("LCB strikethrough lines");
        jMenuViewStyleLcbStrikethroughLines.setMnemonic('t');
        jMenuViewStyleLcbStrikethroughLines.setActionCommand("ToggleStrikethrough");
        jMenuViewStyleLcbStrikethroughLines.addActionListener(this);

        jMenuViewStyleLcbConnectingLines.setToolTipText("Draw lines connecting homologous LCBs across genomes");
        jMenuViewStyleLcbConnectingLines.setVisible(true);
        jMenuViewStyleLcbConnectingLines.setText("LCB connecting lines");
        jMenuViewStyleLcbConnectingLines.setMnemonic('l');
        jMenuViewStyleLcbConnectingLines.setActionCommand("ToggleLCBlines");
        jMenuViewStyleLcbConnectingLines.addActionListener(this);
    	
        jMenuViewStyleChromosomeBoundaries.setToolTipText("Draw red lines at chromosome or contig boundaries");
        jMenuViewStyleChromosomeBoundaries.setVisible(true);
        jMenuViewStyleChromosomeBoundaries.setText("Chromosome/contig boundaries");
        jMenuViewStyleChromosomeBoundaries.setMnemonic('c');
        jMenuViewStyleChromosomeBoundaries.setActionCommand("ToggleChromosomeBoundaries");
        jMenuViewStyleChromosomeBoundaries.addActionListener(this);

        jMenuViewStyleMouseHighlighting.setToolTipText("Define whether the mouse cursor should show orthologous regions");
        jMenuViewStyleMouseHighlighting.setVisible(true);
        jMenuViewStyleMouseHighlighting.setText("Show mouse highlighting");
        jMenuViewStyleMouseHighlighting.setMnemonic('m');
        jMenuViewStyleMouseHighlighting.setActionCommand("ToggleMouseCursor");
        jMenuViewStyleMouseHighlighting.addActionListener(this);
        
        jMenuViewStyleDrawAttributes.setToolTipText("Should attributes such as genome-wide histograms be drawn");
        jMenuViewStyleDrawAttributes.setVisible(true);
        jMenuViewStyleDrawAttributes.setText("Draw attributes (histograms)");
        jMenuViewStyleDrawAttributes.setMnemonic('a');
        jMenuViewStyleDrawAttributes.setActionCommand("ToggleDrawAttributes");
        jMenuViewStyleDrawAttributes.addActionListener(this);

        add(jMenuViewStyleLcbOutlines);
        add(jMenuViewStyleSimilarityPlot);
        add(jMenuViewStyleSolidBlocks);
        add(jMenuViewStyleLcbStrikethroughLines);
        add(jMenuViewStyleLcbConnectingLines);
        add(jMenuViewStyleChromosomeBoundaries);
        add(jMenuViewStyleMouseHighlighting);
        add(jMenuViewStyleDrawAttributes);
    }
    
    /**
     * Sets the viewer model and display panel that the StyleMenu will configure.
     * Also adds keystroke shortcuts to the display panel, and enables or
     * disables the menu according to the model type being displayed.
     * @param model
     * @param rrpanel
     */
    public void setTarget(BaseViewerModel model, RearrangementPanel rrpanel)
    {
    	this.model = model;
    	this.rrpanel = rrpanel;
    	if( model instanceof LcbViewerModel || model instanceof XmfaViewerModel )
    	{
    		setEnabled(true);
            rrpanel.addKeyMapping("typed r", "ToggleStrikethrough", this);
            rrpanel.addKeyMapping("typed L", "ToggleLCBlines", this);
            rrpanel.addKeyMapping("typed q", "ToggleLcbBounds", this);
            rrpanel.addKeyMapping("typed w", "ToggleFillBoxes", this);
            rrpanel.addKeyMapping("typed e", "ToggleDrawMatches", this);

            jMenuViewStyleLcbConnectingLines.setSelected(true);
            jMenuViewStyleLcbOutlines.setSelected(true);
            jMenuViewStyleLcbStrikethroughLines.setSelected(true);
            jMenuViewStyleSimilarityPlot.setSelected(true);
            jMenuViewStyleSolidBlocks.setSelected(false);
            jMenuViewStyleChromosomeBoundaries.setSelected(true);
            jMenuViewStyleMouseHighlighting.setSelected(true);
    	}else
    		setEnabled(false);
        rrpanel.addKeyMapping("typed h", "ToggleDrawAttributes", this);
        jMenuViewStyleDrawAttributes.setSelected(model.getDrawAttributes());
    }
    
    public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equals("ToggleLCBlines"))
        {
        	if(rrpanel != null)
        	{
        		rrpanel.lcbLinePanel.setHidden(!rrpanel.lcbLinePanel.getHidden());
        		jMenuViewStyleLcbConnectingLines.setSelected(!rrpanel.lcbLinePanel.getHidden());
        	}
        }
        else if (e.getActionCommand().equals("ToggleStrikethrough"))
        {
        	if(rrpanel != null)
        	{
	        	rrpanel.lcbLinePanel.draw_strikethrough = !rrpanel.lcbLinePanel.draw_strikethrough;
	        	rrpanel.lcbLinePanel.repaint();
	        	jMenuViewStyleLcbStrikethroughLines.setSelected(rrpanel.lcbLinePanel.draw_strikethrough);
        	}
        }
        else if (e.getActionCommand().equals("ToggleLcbBounds"))
        {
            if (model instanceof LcbViewerModel)
            {
                LcbViewerModel lv = (LcbViewerModel) model;
                lv.setDrawLcbBounds(!lv.getDrawLcbBounds());
                jMenuViewStyleLcbOutlines.setSelected(lv.getDrawLcbBounds());
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
                    jMenuViewStyleSolidBlocks.setSelected(lv.getFillLcbBoxes());
                }
            }
        }
        else if (e.getActionCommand().equals("ToggleDrawMatches"))
        {
            if (model != null)
            {
                model.setDrawMatches(!model.getDrawMatches());
                jMenuViewStyleSimilarityPlot.setSelected(model.getDrawMatches());
            }
        }
        else if (e.getActionCommand().equals("ToggleChromosomeBoundaries"))
        {
            if (model != null)
            {
                model.setDrawChromosomeBoundaries(!model.getDrawChromosomeBoundaries());
                jMenuViewStyleChromosomeBoundaries.setSelected(model.getDrawChromosomeBoundaries());
            }
        }
        else if (e.getActionCommand().equals("ToggleMouseCursor"))
        {
            if (model != null)
            {
                model.setDrawMouseCursor(!model.getDrawMouseHighlighting());
                jMenuViewStyleMouseHighlighting.setSelected(model.getDrawMouseHighlighting());
            }
        }
        else if (e.getActionCommand().equals("ToggleDrawAttributes"))
        {
            if (model != null)
            {
                model.setDrawAttributes(!model.getDrawAttributes());
                jMenuViewStyleDrawAttributes.setSelected(model.getDrawAttributes());
            }
        }
    	
    }
}
