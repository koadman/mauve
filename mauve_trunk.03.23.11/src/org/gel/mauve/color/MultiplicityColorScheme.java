package org.gel.mauve.color;

import java.awt.Color;
import java.util.Vector;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.ColorScheme;
import org.gel.mauve.Genome;
import org.gel.mauve.Match;

/**
 * Colors the matches based on their multiplicity.
 */
public class MultiplicityColorScheme implements ColorScheme {
	private Vector _matchColorTable;

	public void apply (BaseViewerModel model) {
		Vector matchColorTable = getMatchColorTable (model);

		for (int matchI = 0; matchI < model.getMatchCount (); matchI++) {
			Match cur_match = model.getMatch (matchI);

			// color the match based on its multiplicity.
			int color_count = 0;
			for (int seqI = 0; seqI < model.getSequenceCount (); seqI++) {
				Genome g = model.getGenomeByViewingIndex (seqI);
				if (cur_match.getStart (g) != Match.NO_MATCH) {
					color_count++;
				}
			}
			color_count -= 2; // assume that all matches have multiplicity >=
			// 2
			cur_match.color = (Color) matchColorTable.elementAt (color_count);

		}
	}

	private Vector getMatchColorTable (BaseViewerModel model) {
		if (_matchColorTable == null) {
			initColorTable (model);
		}

		return _matchColorTable;
	}

	private void initColorTable (BaseViewerModel model) {

		_matchColorTable = new Vector ();

		if (model.getSequenceCount () == 0) {
			throw new RuntimeException (
					"Model must have at least one sequence.");
		}

		// modulate the colors through the spectrum for different multiplicities
		// 
		// allow a fixed number of multiplicities for each modulation through
		// the spectrum
		int max_m = 3; // 5 for testing purposes.

		int cycles = (int) Math.ceil ((double) model.getSequenceCount ()
				/ (double) max_m);

		for (int seqI = 0; seqI < model.getSequenceCount (); seqI++) {
			float hue = ((float) seqI / (float) max_m) % 1;
			// add the cycle increment
			hue += ((1d / (double) max_m) / (double) cycles)
					* Math.floor ((double) seqI / (double) max_m);
			_matchColorTable.addElement (Color.getHSBColor (hue, MATCH_SAT,
					MATCH_BRIGHT));
		}
	}

	public String toString () {
		return "Multiplicity";
	}

}