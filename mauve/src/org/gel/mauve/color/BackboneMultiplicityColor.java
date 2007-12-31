package org.gel.mauve.color;

import java.awt.Color;
import java.util.Arrays;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.ColorScheme;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.backbone.Backbone;
import org.gel.mauve.backbone.BackboneList;

// algorithm is going to be
// 1: get segment presence/absence patterns as BitSets
// 2: Create a BitSet HashMap and count the number of nucleotides covered by
//    each pattern
// 3: assign maximally distinct colors to the most common patterns

public class BackboneMultiplicityColor implements ColorScheme {
    public static final float HUE_MIN = 0f;
    public static final float HUE_MAX = 1f;
    public static final float HUE_LEVELS = 12;
    public static final float HUE_WEIGHT = 10f;
    public static final float SAT_MIN = .7f;
    public static final float SAT_MAX = 1f;
    public static final float SAT_LEVELS = 2;
    public static final float BRIGHT_MIN = .5f;
    public static final float BRIGHT_MAX = 1.0f;
    public static final float BRIGHT_LEVELS = 3;
    
    /**
     *  Returns an array of visually distinct colors
     * @param ncols	The requested number of colors.  Fewer colors may be returned than were requested.
     * @return	An array with colors
     */
    public static Color[] getColors(int ncols)
    {
    	// HSB is a cylindrical space
    	// we would like to pick an arbitrary number of maximally distinct
    	// colors from this space, limiting our range to the values above
    	// to do so.
    	// since i don't know how to solve the problem of choosing a set of
    	// n maximally distant points in a partial cylinder, i will approximate the
    	// problem as choosing maximally distant points in a 3d rectangle.
    	// compute the total volume of the target space and divide
    	// by the number of desired colors
    	
    	float max_cols = HUE_LEVELS * SAT_LEVELS * BRIGHT_LEVELS;
    	max_cols = ncols < max_cols ? ncols : max_cols;
    	Color[] cols = new Color[(int)max_cols];
    	float slice_area = (((float)Math.PI * SAT_MAX * SAT_MAX) - ((float)Math.PI * SAT_MIN * SAT_MIN));
    	float volume = (BRIGHT_MAX - BRIGHT_MIN) * slice_area * HUE_WEIGHT;
    	float cube_size = volume / max_cols;
    	float step = (float)Math.pow(cube_size, (1.0/3.0));
    	int h_steps = (int)Math.ceil(((HUE_MAX-HUE_MIN)*HUE_WEIGHT) / step);
    	int s_steps = (int)Math.ceil((SAT_MAX-SAT_MIN) / step);
    	int b_steps = (int)Math.ceil((BRIGHT_MAX-BRIGHT_MIN) / step);
    	while(h_steps*(s_steps+1)*(b_steps+1) < max_cols)
    	{
    		h_steps++;
    	}
    	
    	float s_step = (SAT_MAX-SAT_MIN) / (float)s_steps;
    	float b_step = (BRIGHT_MAX-BRIGHT_MIN) / (float)b_steps;
    	float h_step = (HUE_MAX-HUE_MIN) / (float)h_steps;
    	float h = 0;
    	int cI = 0;
    	for(int sI = 0; sI <= s_steps; sI++)
        	for(int bI = 0; bI <= b_steps; bI++)
        		for(int hI = 0; hI < h_steps; hI++)
        		{
        			cols[cI++] = Color.getHSBColor(
        					h, 
        					(s_step * (float)sI) + SAT_MIN, 
        					(b_step * (float)bI) + BRIGHT_MIN);
        			h += h_step;
        			if(cI == cols.length)
        				return cols;
        		}
    	
    	return cols;
    }
    
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
    
    /**
     * Converts an array of booleans to a BitSet
     * @param b	an array of booleans
     * @return	a bitset
     */
    private java.util.BitSet makeBitSet( boolean[] b )
    {
    	java.util.BitSet bs = new java.util.BitSet(b.length);
    	for(int i = 0; i < b.length; i++)
    		if(b[i])
    			bs.set(i);
    	return bs;
    }

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
        
        // count up nucleotides covered by each multiplicity type
        long[] nt = new long[(int)unique_mt_count];
        Object[] types = new Object[(int)unique_mt_count];
        int cur_mt = 0;
        for( int bbI = 0; bbI < bb_by_mt.length - 1; ++bbI )
        {
        	if(BB_MULTIPLICITY_TYPE_COMPARATOR.compare(bb_by_mt[bbI],bb_by_mt[bbI+1]) != 0)
        	{
        		types[cur_mt] = bb_by_mt[bbI].getSeqs();
        		cur_mt++;
        	}
        	nt[cur_mt] += bb_by_mt[bbI].getLength();
        }
		types[cur_mt] = bb_by_mt[bb_by_mt.length-1].getSeqs();
        
        // put the nucleotide counts for each SPA pattern in a map
        TreeMap sm = new java.util.TreeMap();
        for( int i = 0; i < nt.length; i++ )
        {
        	java.util.Vector l = (java.util.Vector)sm.get(new Long(nt[i]));
            if (l == null)
                sm.put(new Long(nt[i]), l=new java.util.Vector());
            l.add(types[i]);
        }
        
        // get the N most distinct colors and assign them round-robin to
        // SPA patterns in order of nt count
        Color[] cols = BackboneMultiplicityColor.getColors((int)unique_mt_count);
        java.util.Set s = sm.entrySet();
        java.util.Iterator iter = s.iterator();
        java.util.HashMap spaColors = new java.util.HashMap();

        int colI = 0;
        while(iter.hasNext())
        {
        	java.util.Vector v = (java.util.Vector)((java.util.Map.Entry)iter.next()).getValue();
        	for(int i = 0; i < v.size(); i++)
        	{
        		spaColors.put(makeBitSet((boolean[])v.elementAt(i)), cols[colI++]);
        		if(colI == cols.length)
        			colI = 0;
        	}
        }
        
        // set N-way backbone to mauve!!  :)
        java.util.BitSet nway = new java.util.BitSet(xmfa.getSequenceCount());
        nway.set(0, xmfa.getSequenceCount());
        spaColors.put(nway, java.awt.Color.getHSBColor(276f/360f, 0.31f + 0.15f, 0.97f));

        // now assign colors to segments
        for( int bbI = 0; bbI < bb_by_mt.length; ++bbI )
        {
        	java.awt.Color c = (java.awt.Color)spaColors.get(makeBitSet(bb_by_mt[bbI].getSeqs()));
        	if(c==null)
        	{
        		System.err.println("this is a bug, please report it...\n");
        		c = Color.BLACK;
        	}
        	bb_by_mt[bbI].setColor(c);
        }
    }

    public String toString()
    {
        return "Backbone multiplicity type";
    }

}
