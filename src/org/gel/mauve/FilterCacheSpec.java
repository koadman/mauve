package org.gel.mauve;

import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.gui.sequence.FeatureRenderer;

public class FilterCacheSpec {
	public final static String ALL_ANNOTATIONS = "ALL_ANNOTATIONS_99";

	public FeatureFilter filter;

	public String [] annotations;

	private FeatureRenderer renderer;

	public FilterCacheSpec (FeatureFilter filter) {
		this.filter = filter;
	}

	public FilterCacheSpec (FeatureFilter filter, String [] annotations) {
		this.filter = filter;
		this.annotations = annotations;
	}

	public FilterCacheSpec (FeatureFilter filter, FeatureRenderer renderer) {
		this.filter = filter;
		this.renderer = renderer;
	}

	public FilterCacheSpec (FeatureFilter filter, String [] annotations,
			FeatureRenderer renderer) {
		this.filter = filter;
		this.annotations = annotations;
		this.renderer = renderer;
	}

	public FeatureFilter getFilter () {
		return filter;
	}

	public String [] getAnnotations () {
		return annotations;
	}

	public FeatureRenderer getFeatureRenderer () {
		return renderer;
	}

}