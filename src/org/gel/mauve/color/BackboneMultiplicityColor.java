package org.gel.mauve.color;

import java.awt.Color;
import java.util.Arrays;
import java.util.Comparator;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.ColorScheme;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.backbone.Backbone;
import org.gel.mauve.backbone.BackboneList;

public class BackboneMultiplicityColor implements ColorScheme {
    static final float HUE_MIN = .1f;
    static final float HUE_MAX = .9f;
    static final float SAT_MIN = .7f;
    static final float SAT_MAX = 1f;
    static final float SAT_LEVELS = 5;
    static final float BRIGHT_MIN = .35f;
    static final float BRIGHT_MAX = .65f;
    static final float BRIGHT_LEVELS = 5;
    
    private final static Comparator BB_MULTIPLICITY_TYPE_COMPARATOR = new Comparator()
    {
        public int compare(Object o1, Object o2)
        {
        	Backbone a = (Backbone)o1;
        	Backbone b = (Backbone)o2;
        	boolean a_seqs[] = a.getSeqs();
        	boolean b_seqs[] = b.getSeqs();
        	for( int i = 0; i < a_seqs.length; ++i )
        	{
        		if(!a_seqs[i] && b_seqs[i])
        			return -1;
        		else if(a_seqs[i] && !b_seqs[i])
        			return 1;
        	}
        	return 0;
        }
    };

    public void apply(BaseViewerModel model)
    {

    	if( ! (model instanceof XmfaViewerModel))
    		return;
    	XmfaViewerModel xmfa = (XmfaViewerModel)model;
    	BackboneList bb_list = xmfa.getBackboneList();
    	if( bb_list == null )
    		return;

        Backbone[] all_bb_array = bb_list.getBackboneArray();
        Backbone[] bb_by_mt = new Backbone[all_bb_array.length];
        System.arraycopy(all_bb_array,0,bb_by_mt,0,bb_by_mt.length);

        
        Arrays.sort(bb_by_mt, BB_MULTIPLICITY_TYPE_COMPARATOR);
        long unique_mt_count = 1;
        for( int bbI = 1; bbI < bb_by_mt.length; ++bbI )
        {
        	if(BB_MULTIPLICITY_TYPE_COMPARATOR.compare(bb_by_mt[bbI-1],bb_by_mt[bbI]) != 0)
        		unique_mt_count++;
        }

        long cur_mt_count = 0;
        float hue = (float) ((double) cur_mt_count / (double) unique_mt_count);
        bb_by_mt[0].setColor(Color.getHSBColor(hue, .65f, .8f));
        for( int bbI = 1; bbI < bb_by_mt.length; ++bbI )
        {
        	if(BB_MULTIPLICITY_TYPE_COMPARATOR.compare(bb_by_mt[bbI-1],bb_by_mt[bbI]) != 0)
        		cur_mt_count++;
            hue = (float) ((double) cur_mt_count / (double) unique_mt_count);
            // map into hue range
            hue *= HUE_MAX - HUE_MIN;
            hue += HUE_MIN;
            float sat = (cur_mt_count % SAT_LEVELS) / SAT_LEVELS;
            sat *= SAT_MAX - SAT_MIN;
            sat += SAT_MIN;
            float bright = (cur_mt_count % BRIGHT_LEVELS) / BRIGHT_LEVELS;
            sat *= BRIGHT_MAX - BRIGHT_MIN;
            sat += BRIGHT_MIN;
            bb_by_mt[bbI].setColor(Color.getHSBColor(hue, .65f, .8f));
        }
    }

    public String toString()
    {
        return "Backbone multiplicity type";
    }

}
