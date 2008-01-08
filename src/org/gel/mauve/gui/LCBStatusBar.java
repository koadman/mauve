package org.gel.mauve.gui;

import java.awt.Dimension;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;
import org.gel.mauve.HighlightListener;
import org.gel.mauve.LCB;
import org.gel.mauve.LcbViewerModel;
import org.gel.mauve.Match;
import org.gel.mauve.ModelEvent;
import org.gel.mauve.MauveAlignmentViewerModel;


/**
 * This class displays statistics about LCBs in a status bar
 */
public class LCBStatusBar extends JPanel implements HighlightListener, HintMessageListener
{
    JLabel hint_bar = new JLabel(" ");
    JLabel LCB_length_status = new JLabel(" ");
    JLabel LCB_weight_status = new JLabel(" ");
    JLabel segment_name = new JLabel(" ");
    JLabel segment_loc = new JLabel(" ");
    private BaseViewerModel model;

    LCBStatusBar()
    {
        // set tooltip text for components
        //			hint_bar.setTooltipText("");
        LCB_length_status.setToolTipText("The length (in nucleotides) of a collinear segment");
        LCB_weight_status.setToolTipText("The alignment weight of a collinear segment");
        segment_name.setToolTipText("The chromosome or contig name");
        segment_loc.setToolTipText("The current coordinate within the chromosome or contig");

        // layout the components
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setMaximumSize(new Dimension(Short.MAX_VALUE, 15));
        setBorder(BorderFactory.createEtchedBorder());

        add(hint_bar);
        add(Box.createHorizontalGlue());

        segment_name.setBorder(BorderFactory.createLoweredBevelBorder());
        Dimension pref_dim = segment_name.getPreferredSize();
        pref_dim.setSize(150, pref_dim.getHeight());
        segment_name.setPreferredSize(pref_dim);
        add(segment_name);

        add(Box.createHorizontalStrut(2));

        segment_loc.setBorder(BorderFactory.createLoweredBevelBorder());
        pref_dim = segment_loc.getPreferredSize();
        pref_dim.setSize(150, pref_dim.getHeight());
        segment_loc.setPreferredSize(pref_dim);
        add(segment_loc);

        add(Box.createHorizontalStrut(2));

        LCB_length_status.setBorder(BorderFactory.createLoweredBevelBorder());
        pref_dim = LCB_length_status.getPreferredSize();
        pref_dim.setSize(150, pref_dim.getHeight());
        LCB_length_status.setPreferredSize(pref_dim);
        add(LCB_length_status);

        add(Box.createHorizontalStrut(2));

        LCB_weight_status.setBorder(BorderFactory.createLoweredBevelBorder());
        pref_dim = LCB_weight_status.getPreferredSize();
        pref_dim.setSize(150, pref_dim.getHeight());
        LCB_weight_status.setPreferredSize(pref_dim);
        add(LCB_weight_status);

        add(Box.createHorizontalStrut(1));
    }
    
    public void setModel(BaseViewerModel model)
    {
        this.model = model;
    }

    private void setWeightStatus(long weight)
    {
        LCB_weight_status.setText(" LCB weight: " + weight);
    }

    private void clearWeightStatus()
    {
        LCB_weight_status.setText(" ");
    }

    private void setLengthStatus(long length)
    {
        LCB_length_status.setText(" LCB length: " + length);
    }

    private void clearLengthStatus()
    {
        LCB_length_status.setText(" ");
    }

    private void setLocationStatus(String name, long coordinate)
    {
        segment_name.setText(name);
        segment_loc.setText(new Long(coordinate).toString());
    }

    private void clearLocationStatus()
    {
        segment_name.setText(" ");
        segment_loc.setText(" ");
    }

    public void messageChanged(HintMessageEvent hme)
    {
    	hint_bar.setText(hme.getMessage());
    }

    public void setHint(String hint)
    {
        hint_bar.setText(hint);
    }

    public void clearHint()
    {
        hint_bar.setText("");
    }

    /** clears all text shown in the toolbar */
    public void clear()
    {
        clearHint();
        clearLocationStatus();
        clearLengthStatus();
        clearWeightStatus();
    }

    public void highlightChanged(ModelEvent evt)
    {
        clearWeightStatus();
        clearLengthStatus();
        clearLocationStatus();
        
        if (model != null)
        {
            Genome highlight = model.getHighlightGenome();
            
            if (!(model instanceof MauveAlignmentViewerModel) && (model instanceof LcbViewerModel))
            {
                LcbViewerModel lm = (LcbViewerModel) model;
                Match lastMatch = lm.lastMatchHighlight();
                if (lastMatch != null)
                {
                    LCB cur_lcb = lm.getVisibleLcb(lastMatch.lcb);
                    setWeightStatus(cur_lcb.weight);
                    long length = cur_lcb.getRightEnd(highlight)- cur_lcb.getLeftEnd(highlight);
                    setLengthStatus(length);
                }
            }

            if (model instanceof LcbViewerModel)
            {
                List lcbs = ((LcbViewerModel) model).getLCBRange(highlight, model.getHighlightCoordinate(), model.getHighlightCoordinate());

                if (lcbs != null)
                {
                    Iterator i = lcbs.iterator();
                    if (i.hasNext())
                    {
                        LCB cur_lcb = (LCB) i.next();
                        setWeightStatus(cur_lcb.weight);
                        setLengthStatus(cur_lcb.getLength(highlight));
                    }
                }
            }
            
            Chromosome ch = highlight.getChromosomeAt(model.getHighlightCoordinate());
            if (ch != null)
            {
                setLocationStatus(ch.getName(), ch.relativeLocation(model.getHighlightCoordinate()));
            }
        }

        repaint();
    }
}