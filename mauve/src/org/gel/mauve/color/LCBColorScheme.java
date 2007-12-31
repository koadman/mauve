package org.gel.mauve.color;

import java.awt.Color;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.ColorScheme;
import org.gel.mauve.LCB;
import org.gel.mauve.LcbViewerModel;
import org.gel.mauve.Match;

/**
 * Color matches by their LCB number. This will also set the color and
 * match_color field for all LCBs.
 */
public class LCBColorScheme implements ColorScheme {
	private final static double BUMP_SIZE = 1d / 6d;

	private boolean lcbColorsComputed = false;

	public void apply (BaseViewerModel model) {
		LcbViewerModel lcbModel = (LcbViewerModel) model;

		if (!lcbColorsComputed) {
			computeLCBColors (lcbModel);
			lcbColorsComputed = true;
		}

		for (int i = 0; i < model.getMatchCount (); i++) {
			Match m = model.getMatch (i);
			if (m.lcb >= 0) {
				m.color = lcbModel.getVisibleLcb (m.lcb).match_color;
			}
		}
	}

	private void computeLCBColors (LcbViewerModel model) {
		double colorIncrement = 1d / (double) model.getLcbCount ();
		for (int i = 0; i < model.getFullLcbList ().length; i++) {
			LCB lcb = model.getFullLcbList ()[i];

			double wrapBump = ((double) i * BUMP_SIZE) / 1d;
			double colorVal = ((double) i * BUMP_SIZE) % 1d;

			// color the match based on its LCB number.
			double hue = wrapBump * colorIncrement + colorVal;
			Color color = Color.getHSBColor ((float) hue, MATCH_SAT,
					MATCH_BRIGHT);
			lcb.match_color = color;
			lcb.color = color;
		}
	}

	public String toString () {
		return "LCB";
	}

}