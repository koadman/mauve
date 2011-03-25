package org.gel.mauve.gui.sequence;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import org.biojava.bio.gui.sequence.RectangularBeadRenderer;
import org.biojava.bio.gui.sequence.SequenceRenderContext;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.Sequence;
import org.biojava.utils.ChangeVetoException;
import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Genome;

public class MultiGenomeRectangularBeadRenderer extends RectangularBeadRenderer
		implements FlatFileFeatureConstants {
	
	protected Hashtable seq_to_offset;
	
	protected HashSet painted;

	protected BaseViewerModel model;
	
	public double last_offset = -1;
	
	private boolean listeners = true;
	
	public MultiGenomeRectangularBeadRenderer (double beadDepth, double beadDisplacement, Paint beadOutline,
				Paint beadFill, Stroke beadStroke, BaseViewerModel mod) {
		super (beadDepth, beadDisplacement, beadOutline, beadFill, beadStroke);
		model = mod;
		painted = new HashSet (model.getSequenceCount ());
		seq_to_offset = new Hashtable (model.getSequenceCount ());
	}

	public synchronized void renderBead (Graphics2D g, Feature feature, SequenceRenderContext context) {
		try {
			double offset = ((Double) seq_to_offset.get (feature.getSequence ())).doubleValue ();
			if  (offset != last_offset) {
				/*if (painted.contains (feature.getSequence ())) {
					listeners = false;
					painted.clear ();
				}
				else
					painted.add (feature.getSequence ());*/
				listeners = false;
				if (last_offset != -1)
					setBeadDisplacement (beadDisplacement - last_offset);
				//new Exception ().printStackTrace ();
				System.out.println ("last: " + last_offset + "\ndisp: " + beadDisplacement);
				last_offset = offset;
				System.out.println ("offset: "  + offset + "\ndisp: " + beadDisplacement);
				//Snew Exception ().printStackTrace ();
				setBeadDisplacement (beadDisplacement + offset);
				listeners = true;
			}
			super.renderBead (g, feature, context);
		} catch (ChangeVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setOffset (Genome genome, double offset) {
		seq_to_offset.put (genome.getAnnotationSequence (), new Double (offset));
	}
	
	public double getOffset (Genome genome) {
		Object ret = seq_to_offset.get (genome.getAnnotationSequence ());
		if (ret == null)
			return NO_OFFSET;
		return ((Double) ret).doubleValue ();
	}
	
	public boolean hasListeners () {
		System.out.println ("list: " + listeners);
		if (listeners)
			return super.hasListeners ();
		else
			return false;
	}

}
