package org.gel.mauve.gui.navigation;

import java.util.Locale;

import org.biojava.bio.Annotation;
import org.biojava.bio.AnnotationType;
import org.biojava.bio.CardinalityConstraint;
import org.biojava.bio.CollectionConstraint;
import org.biojava.bio.PropertyConstraint;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.OptimizableFilter;

/**
 * Allows features to be filtered based on  whether they contain an annotation of a 
 * particular type with an either exact or partial value match.  Filtering is case
 * insensitive
 * 
 * @author Anna I Rissman
 *
 */
public class AnnotationContainsFilter extends FeatureFilter.ByAnnotationType {

	/**
	 * The type of annotation searched for
	 */
	protected String key;

	/**
	 * The value the annotation should have
	 */
	protected String value;

	/**
	 * If exact is true, the annotation must exactly match the value given,
	 * otherwise features will be filtered on substring matches
	 */
	protected boolean exact;

	/**
	 * Creates a feature filter
	 * @param key		The type of annotation searched for
	 * @param value		The value the annotation should have
	 * @param exact		Whether the match is exact or not
	 */
	public AnnotationContainsFilter (String key, String value, boolean exact) {
		this.key = key;
		this.value = value;
		this.exact = exact;

		AnnotationType.Impl type = new AnnotationType.Impl ();
		type.setConstraint (key, new CollectionConstraint.Contains (
				new PropertyConstraint () {

					public boolean accept (Object value) {
						if (!(value instanceof String))
							value = value.toString ();
						if (AnnotationContainsFilter.this.exact
								&& ((String) value).length () != AnnotationContainsFilter.this.value
										.length ())
							return false;
						boolean ret = ((String) value).toLowerCase ().indexOf (
								AnnotationContainsFilter.this.value) > -1;
						if (AnnotationContainsFilter.this.exact) {
							ret = ret
									&& ((String) value).length () == AnnotationContainsFilter.this.value
											.length ();
						}
						return ret;
					}

					public boolean subConstraintOf (
							PropertyConstraint subConstraint) {
						return false;
					}

				}, CardinalityConstraint.ONE));
		super.setType (type);
	}

	/**
	 * Returns a string representing the key in the right case (upper, lower, or
	 * capitalized
	 * 
	 * @param key
	 *            The key the annotation should contain
	 * @param note
	 *            The annotation in question
	 * @return A string representing the key as it appears
	 */
	public static String getKeyIgnoreCase (String key, Annotation note) {
		key = key.toLowerCase ();
		if (note.containsProperty (key))
			return key;
		else if (note.containsProperty (key.toUpperCase ()))
			return key.toUpperCase ();
		key = key.toUpperCase ().charAt (0) + key.substring (1, key.length ());
		if (note.containsProperty (key))
			return key;
		else
			return null;
	}

	/**
	 * gets the value of an annotation with a specified key; is case insensitive
	 * 
	 * @param key	The key of the desired annotation
	 * @param note  The annotation to search
	 * @return
	 */
	public static Object getValueIgnoreCase (String key, Annotation note) {
		key = getKeyIgnoreCase (key, note);
		if (key != null)
			return note.getProperty (key);
		else
			return null;
	}

}
