package org.gel.mauve.color;

import java.awt.Color;
import java.util.Arrays;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.ColorScheme;
import org.gel.mauve.LCB;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.backbone.Backbone;
import org.gel.mauve.backbone.BackboneList;

public class BackboneLcbColor implements ColorScheme {
	public void apply (BaseViewerModel model) {
		if (!(model instanceof XmfaViewerModel))
			return;
		XmfaViewerModel xmfa = (XmfaViewerModel) model;
		BackboneList bb_list = xmfa.getBackboneList ();
		if (bb_list == null)
			return;

		Backbone [] all_bb_array = bb_list.getBackboneArray ();
		LCB [] lcbs = xmfa.getFullLcbList ();
		for (int bbI = 0; bbI < all_bb_array.length; ++bbI) {
			Color c = lcbs[all_bb_array[bbI].getLcbIndex ()].color;
			all_bb_array[bbI].setColor (c);
		}
	}

	public String toString () {
		return "LCB color";
	}

}
