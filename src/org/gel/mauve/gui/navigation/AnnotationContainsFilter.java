package org.gel.mauve.gui.navigation;

import org.biojava.bio.AnnotationType;
import org.biojava.bio.CardinalityConstraint;
import org.biojava.bio.CollectionConstraint;
import org.biojava.bio.PropertyConstraint;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.OptimizableFilter;

public class AnnotationContainsFilter extends FeatureFilter.ByAnnotationType {

	protected String key;
	protected String value;
	protected boolean exact;
	
	public AnnotationContainsFilter (String key, String value, boolean exact) {
		this.key = key;
		this.value = value;
		this.exact = exact;

		AnnotationType.Impl type = new AnnotationType.Impl();
		type.setConstraint(
				key,
				new CollectionConstraint.Contains(
						new PropertyConstraint () {

							public boolean accept(Object value) {
								if (!(value instanceof String))
									value = value.toString ();
								boolean ret = ((String) value).toLowerCase ().indexOf (
										AnnotationContainsFilter.this.value) > -1;
								if (AnnotationContainsFilter.this.exact) {
									ret = ret && ((String) value).length () == 
										AnnotationContainsFilter.this.value.length ();
								}
								return ret;
							}

							public boolean subConstraintOf(PropertyConstraint subConstraint) {
								return false;
							}
						
						},
						CardinalityConstraint.ONE
				)
		);
		super.setType(type);
	}
	
}
