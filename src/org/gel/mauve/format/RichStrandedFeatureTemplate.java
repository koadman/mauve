/**
 * 
 */
package org.gel.mauve.format;

import org.biojava.bio.seq.StrandedFeature;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichLocation;

class RichStrandedFeatureTemplate extends StrandedFeature.Template
{
	static final long serialVersionUID = 243583828;
	RichStrandedFeatureTemplate(RichFeature.Template t)
	{
		this.annotation = t.annotation;
		this.location = t.location;
		this.source = t.source;
		this.sourceTerm = t.sourceTerm;
		this.type = t.type;
		this.typeTerm = t.typeTerm;
		if( ((RichLocation)t.location).getStrand().intValue() == -1 )
			strand = StrandedFeature.NEGATIVE;
		else if( ((RichLocation)t.location).getStrand().intValue() == 1 )
			strand = StrandedFeature.POSITIVE;
		else
			strand = StrandedFeature.UNKNOWN;
	}
}