package org.gel.mauve.ext.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Vector;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Genome;
import org.gel.mauve.gui.RearrangementPanel;
import org.gel.mauve.gui.sequence.FeaturePanel;
import org.gel.mauve.gui.sequence.SeqPanel;

public class GenomePanel extends SeqPanel {

	protected Vector <FeaturePanel> panels;
	
	public GenomePanel(BaseViewerModel model, Genome genome, RearrangementPanel rearrangementPanel)
    {
		super (model, genome, rearrangementPanel);
    }
    
	public Vector <FeaturePanel> getFeaturePanels () {
    	return panels;
    }
    
	protected void addFeatures (GridBagConstraints c) {
		if (panels != null) {
			GridBagLayout layout = (GridBagLayout) getLayout ();
			c.weighty = BOX_FEATURE_WEIGHT;
			for (int i = 0; i < panels.size(); i++) {
				add(panels.get (i));
				layout.setConstraints(panels.get(i), c);
			}
		}
	}

}
