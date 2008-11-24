package org.gel.mauve.gui.sequence;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JLabel;

import org.biojava.bio.gui.sequence.FeatureBlockSequenceRenderer;
import org.biojava.bio.gui.sequence.FilteringRenderer;
import org.biojava.bio.gui.sequence.MultiLineRenderer;
import org.biojava.bio.gui.sequence.OverlayRendererWrapper;
import org.biojava.bio.gui.sequence.TranslatedSequencePanel;
import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.FilterCacheSpec;
import org.gel.mauve.Genome;
import org.gel.mauve.MauveConstants;

public class TranslatedSequenceContainer extends AbstractSequencePanel
{
    //for all the following feature renderers and panels.
    protected Map trans_multi;
    protected Map multi_trans;
    ////
    private FeaturePanel feature;
    private BaseViewerModel model;
    
	public TranslatedSequenceContainer(FeaturePanel feature, Genome genome, BaseViewerModel model) 
	{
		super(model, genome);
		this.feature = feature;
		this.model = model;
		trans_multi = new Hashtable();
		multi_trans = new Hashtable();
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBackground(Color.ORANGE);
		//this.add(new JLabel("this is new guy"));
		
    	if (! trans_multi.keySet().isEmpty())
    	{
    		for (Object o : trans_multi.keySet().toArray())
    		{
    			this.add((TranslatedSequencePanel) o);
    		}
    	}
	}
	
    /*public void setBounds(int arg0, int arg1, int arg2, int arg3)
    {
        super.setBounds(arg0, arg1, arg2, arg3);
        
        if (! trans_multi.keySet().isEmpty())
        {
            adjustScaleAndTranslation();
        }
    } */

    protected void adjustScaleAndTranslation()
    {
        if (getSize().width != 0)
        {
            double scale = feature.trans.getScale();
            if (! feature.trans.isVisible())
            {
                // TranslatedSequencePanel can't handle being translated out of
                // visibility, and we want to limit the viewable range for 
            	// better performance
            	this.setVisible(false);
            	if (! trans_multi.keySet().isEmpty())
            	{
            		for (Object o : trans_multi.keySet().toArray())
            		{
            			((TranslatedSequencePanel) o).setVisible(false);
            		}
            	}
            }
            else
            {
            	this.setVisible(true);
            	if (! trans_multi.keySet().isEmpty())
            	{
            		for (Object o : trans_multi.keySet().toArray())
            		{
            			((TranslatedSequencePanel) o).setScale(scale);
            			//((TranslatedSequencePanel) o).setSymbolTranslation((int) getGenome().getViewStart());
            			((TranslatedSequencePanel) o).setVisible(true);
            			this.add((TranslatedSequencePanel) o);
            		}
            	}
            }
        }
    }
    
	public MultiLineRenderer put(TranslatedSequencePanel trans, MultiLineRenderer multi)
	{
		trans.setRenderer(multi);
		trans_multi.put(trans, multi);
		multi_trans.put(multi, trans);
		return multi;
	}
	
	//FlatFileFeatureImporter takes care of creating Trans; benefit: can make a trans from importing file
	public void makeRendererForTranslatedSequence(TranslatedSequencePanel trans, FilterCacheSpec spec)
	{
		MultiLineRenderer multi = this.put(trans, new MultiLineRenderer());
		FeatureBlockSequenceRenderer fbr = new FeatureBlockSequenceRenderer ();
		fbr.setFeatureRenderer (spec.getFeatureRenderer ());
		fbr.setCollapsing (false);
		OverlayRendererWrapper over = new OverlayRendererWrapper (
				new FilteringRenderer (fbr, spec.getFilter (), true));
		FeatureFilterer.getFilterer (model).addOverlayRenderer (multi, over);
		multi.addRenderer (over);
		
		//add(trans);
	}
	
	public Map getTranslatedSequenceToMultiLineRenderer()
	{
		return trans_multi;
	}
	
	public Map getMultiLineRendererToTranslatedSequence()
	{
		return multi_trans;
	}
	
	
}
