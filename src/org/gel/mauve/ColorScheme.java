package org.gel.mauve;

/**
 * Interface for objects that can apply a color scheme to the elements of a
 * model.
 */
public interface ColorScheme {
	static final float MATCH_SAT = .8f;

	static final float MATCH_BRIGHT = .65f;

	// Put a gap in the spectrum around violet to make offsets on each end
	// distinguishable
	static final float SPECTRUM_GAP = .2f;

	static final float LCB_BRIGHT = .55f;

	/**
	 * @param model
	 * 
	 * Apply the color scheme to the elements of the model. At a minimum, the
	 * matches of the model must be assigned colors.
	 */
	void apply (BaseViewerModel model);
}