package org.gel.mauve.format;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Iterator;
import java.util.Set;

import org.biojava.bio.Annotation;
import org.biojava.bio.gui.sequence.RectangularBeadRenderer;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.Note;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.taxa.NCBITaxon;
import org.biojavax.ontology.ComparableTerm;
import org.gel.mauve.FilterCacheSpec;
import org.gel.mauve.gui.navigation.AnnotationContainsFilter;
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

	public String getChromosomeName (Sequence s) {
		FeatureHolder fh = s.filter(new FeatureFilter.ByType("source"));
        String name = null;
        Annotation a = null;
        if (fh.countFeatures() != 0)
        {
            Feature f2 = (Feature) fh.features().next();
            a = f2.getAnnotation();
        }
        if (a != null) {
            if (a.containsProperty("chromosome"))
            {
                name = (String) a.getProperty("chromosome");
            }
            else if (a.containsProperty("biojavax:chromosome"))
            {
            	name = (String) a.getProperty("biojavax:chromosome");
            }
            else if (a.containsProperty("plasmid"))
            {
            	name = (String) a.getProperty("plasmid");
            }
            else if (a.containsProperty("biojavax:plasmid"))
            {
            	name = (String) a.getProperty("biojavax:plasmid");
            }
		}
        if (name == null || name.trim().equals("") && 
        		AnnotationContainsFilter.getKeyIgnoreCase ("definition",
				s.getAnnotation()) != null) {
			name = getChromNameFromDescription (s);
		}
		return name;
	}

	public String getChromNameFromDescription (Sequence seq) {
		String desc = ((String) AnnotationContainsFilter.getValueIgnoreCase (
				"definition", seq.getAnnotation ()));
        if(desc == null) {
            return null;
        } else {
            desc = desc.toLowerCase();
        }

        int ind = desc.indexOf ("contig");
		if (ind > -1) {
			ind = desc.lastIndexOf (" ", ind);
			if (ind < 0)
				ind = 0;
			int ind2 = desc.indexOf (" ", ind + 1);
			if (ind2 < 0)
				ind2 = desc.length ();
			desc = desc.substring (ind, ind2);
		}
		else if (desc.indexOf("chromosome") > 0)
			desc = "chromosome";
		else if (desc.indexOf("plasmid") > 0)
			desc = "plasmid";
		/*else {
			StringTokenizer toke = new StringTokenizer (getSequenceName (seq));
			while (toke.hasMoreTokens ()) {
				String [] pieces = desc.split (toke.nextToken ().toLowerCase ());
				desc = "";
				for (int i = 0; i < pieces.length; i++)
					desc += pieces[i].trim () + " ";
				desc = desc.trim ();
			}
		}*/
		return desc;
	}

    public String getSequenceName(Sequence s)
    {
        String name = "<html>";
    	if(s instanceof RichSequence)
    	{
    		NCBITaxon nt = ((RichSequence)s).getTaxon();
    		if(nt == null)
    			return "";	// no taxonomy info, so can't figure out a name
            name += "<i>" + nt.getDisplayName() + "</i> ";
    	}
        FeatureHolder fh = s.filter(new FeatureFilter.ByType("source"));
        if (fh.countFeatures() != 0)
        {
            Feature f2 = (Feature) fh.features().next();
            Annotation a = f2.getAnnotation();
            String add = null;

            if(f2 instanceof RichFeature)
            {
            	RichFeature f3 = (RichFeature)f2;
	            final Set noteSet = f3.getNoteSet();
	            final Iterator n = noteSet.iterator();
	            while(n.hasNext()) {
	                final Note note = (Note) n.next();
//	                if (note.getTerm().getName().equals("organelle")) 
//	                	return note.getValue().equals("mitochondrion");
	            }
            }

            ComparableTerm organismTerm = RichObjectFactory.getDefaultOntology().getOrCreateTerm("organism");
            ComparableTerm serovarTerm = RichObjectFactory.getDefaultOntology().getOrCreateTerm("serovar");
            ComparableTerm strainTerm = RichObjectFactory.getDefaultOntology().getOrCreateTerm("strain");
            ComparableTerm plasmidTerm = RichObjectFactory.getDefaultOntology().getOrCreateTerm("plasmid");
            if (a.containsProperty("organism"))
            {
                name += a.getProperty("organism") + " ";
            }else if (a.containsProperty(organismTerm))
            {
                name += a.getProperty(organismTerm) + " ";
            }
            if (a.containsProperty("serovar"))
            {
            	add = a.getProperty("serovar").toString ();
            	if (name.indexOf(add) == -1)
            		name += add + " ";
            }else if (a.containsProperty(serovarTerm))
            {
            	add = a.getProperty(serovarTerm).toString ();
            	if (name.indexOf(add) == -1)
            		name += add + " ";
            }
            if (a.containsProperty("strain"))
            {
            	add = a.getProperty("strain").toString ();
            	if (name.indexOf(add) == -1)
            		name += add + " ";
            }else if (a.containsProperty(strainTerm))
            {
            	add = a.getProperty(strainTerm).toString ();
            	if (name.indexOf(add) == -1)
            		name += add + " ";
            }
            if (a.containsProperty("plasmid"))
            {
            	add = a.getProperty("plasmid").toString ();
            	if (name.indexOf(add) == -1)
            		name += add + " ";
            }else if (a.containsProperty(plasmidTerm))
            {
            	add = a.getProperty(plasmidTerm).toString ();
            	if (name.indexOf(add) == -1)
            		name += "plasmid " + add + " ";
            }
            if(!name.equals("<html>") )
            {
            	return name.trim() + "</html>";
            }
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