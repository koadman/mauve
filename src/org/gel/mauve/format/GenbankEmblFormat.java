package org.gel.mauve.format;

import java.awt.BasicStroke;
import java.awt.Color;

import org.biojava.bio.Annotation;
import org.biojava.bio.gui.sequence.RectangularBeadRenderer;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.utils.ChangeVetoException;
import org.gel.mauve.FilterCacheSpec;
import org.gel.mauve.gui.sequence.ZiggyRectangularBeadRenderer;

public abstract class GenbankEmblFormat extends BaseFormat
{
    private static FilterCacheSpec[] specs = new FilterCacheSpec[10];
    static
    {
    	RectangularBeadRenderer renderer = null;
    	ZiggyRectangularBeadRenderer zrenderer;
        try
        {
        	zrenderer = new ZiggyRectangularBeadRenderer(10.0, 0.0, Color.BLACK, Color.WHITE, new BasicStroke());
	        specs[0] = new FilterCacheSpec(new FeatureFilter.And(new FeatureFilter.ByType("CDS"),new FeatureFilter.StrandFilter(StrandedFeature.POSITIVE)), new String[] { "gene", "locus_tag", "product", "db_xref" }, zrenderer);

	        renderer = new RectangularBeadRenderer(10.0, 0.0, Color.BLACK, Color.RED, new BasicStroke());
            renderer.setHeightScaling(false);
            specs[1] = new FilterCacheSpec(new FeatureFilter.And(new FeatureFilter.ByType("rRNA"),new FeatureFilter.StrandFilter(StrandedFeature.POSITIVE)), new String[] { "gene", "locus_tag", "product", "db_xref" }, renderer);

            renderer = new RectangularBeadRenderer(10.0, 0.0, Color.BLACK, Color.GREEN, new BasicStroke());
            renderer.setHeightScaling(false);
	        specs[2] = new FilterCacheSpec(new FeatureFilter.And(new FeatureFilter.ByType("tRNA"),new FeatureFilter.StrandFilter(StrandedFeature.POSITIVE)), new String[] { "gene", "locus_tag", "product", "db_xref" }, renderer);

	        renderer = new RectangularBeadRenderer(10.0, 0.0, Color.BLACK, Color.BLUE, new BasicStroke());
	        renderer.setHeightScaling(false);
	        specs[3] = new FilterCacheSpec(new FeatureFilter.And(new FeatureFilter.ByType("misc_RNA"),new FeatureFilter.StrandFilter(StrandedFeature.POSITIVE)), new String[] { "gene", "locus_tag", "product", "db_xref" }, renderer);
	
        	zrenderer = new ZiggyRectangularBeadRenderer(10.0, 10.0, Color.BLACK, Color.WHITE, new BasicStroke());
	        specs[4] = new FilterCacheSpec(new FeatureFilter.And(new FeatureFilter.ByType("CDS"),new FeatureFilter.StrandFilter(StrandedFeature.NEGATIVE)), new String[] { "gene", "locus_tag", "product", "db_xref" }, zrenderer);

	        renderer = new RectangularBeadRenderer(10.0, 10.0, Color.BLACK, Color.RED, new BasicStroke());
            renderer.setHeightScaling(false);
	        specs[5] = new FilterCacheSpec(new FeatureFilter.And(new FeatureFilter.ByType("rRNA"),new FeatureFilter.StrandFilter(StrandedFeature.NEGATIVE)), new String[] { "gene", "locus_tag", "product", "db_xref" }, renderer);

	        renderer = new RectangularBeadRenderer(10.0, 10.0, Color.BLACK, Color.GREEN, new BasicStroke());
            renderer.setHeightScaling(false);
	        specs[6] = new FilterCacheSpec(new FeatureFilter.And(new FeatureFilter.ByType("tRNA"),new FeatureFilter.StrandFilter(StrandedFeature.NEGATIVE)), new String[] { "gene", "locus_tag", "product", "db_xref" }, renderer);

	        renderer = new RectangularBeadRenderer(10.0, 10.0, Color.BLACK, Color.BLUE, new BasicStroke());
            renderer.setHeightScaling(false);
	        specs[7] = new FilterCacheSpec(new FeatureFilter.And(new FeatureFilter.ByType("misc_RNA"),new FeatureFilter.StrandFilter(StrandedFeature.NEGATIVE)), new String[] { "gene", "locus_tag", "product", "db_xref" }, renderer);
		        
	        // for repeats
	        renderer = new RectangularBeadRenderer(6.0, 20.0, Color.BLACK, Color.PINK, new BasicStroke());
            renderer.setHeightScaling(false);
	        specs[8] = new FilterCacheSpec(new FeatureFilter.And(new FeatureFilter.ByType("repeat_region"),new FeatureFilter.StrandFilter(StrandedFeature.NEGATIVE)), new String[] { "gene", "locus_tag", "product", "db_xref" }, renderer);

	        renderer.setHeightScaling(false);
	        specs[9] = new FilterCacheSpec(new FeatureFilter.ByType("source"), new String[] { FilterCacheSpec.ALL_ANNOTATIONS });
        }
        catch (ChangeVetoException e)
        {
            e.printStackTrace();
        }

    }

    public String getChromosomeName(Sequence s)
    {
        FeatureHolder fh = s.filter(new FeatureFilter.ByType("source"));
        if (fh.countFeatures() != 0)
        {
            Feature f2 = (Feature) fh.features().next();
            Annotation a = f2.getAnnotation();
            if (a.containsProperty("chromosome"))
            {
                return (String) a.getProperty("chromosome");
            }
            else if (a.containsProperty("plasmid"))
            {
                return (String) a.getProperty("plasmid");
            }
        }
        return null;
    }

    public String getSequenceName(Sequence s)
    {
        FeatureHolder fh = s.filter(new FeatureFilter.ByType("source"));
        if (fh.countFeatures() != 0)
        {
            Feature f2 = (Feature) fh.features().next();
            Annotation a = f2.getAnnotation();
            String name = "";
            if (a.containsProperty("organism"))
            {
                name += a.getProperty("organism") + " ";
            }
            if (a.containsProperty("serovar"))
            {
                name += a.getProperty("serovar") + " ";
            }
            if (a.containsProperty("strain"))
            {
                name += a.getProperty("strain") + " ";
            }
            if(name != "")
            	return name;
        }
        // if a source feature didn't exist
        // try getting the source from the headers
        try{
	        Object source = s.getAnnotation().getProperty("SOURCE");
	        if(source != null)
	        {
	        	return (String)source;
	        }
        }catch(Exception e){}
        return null;
    }

    public FilterCacheSpec[] getFilterCacheSpecs()
    {
        return specs;
    }

}