package org.gel.mauve.analysis.output;

import org.gel.mauve.BaseViewerModel;

/**
 * outputs data from Match objects stored in Genome objects in Mauve in a format
 * compatible with the .backbone file to see if the two match. written 2/2/07 on
 * Mauve 2.0. We're not currently sure how matches as displayed and as in the
 * backbone file match up, which is the purpose for this class.
 * 
 * @author Anna I Rissman
 * 
 */
// not useful anymore; never fully necessary
abstract public class BackboneCompareFile {// extends AbstractMatchDataWriter {

	String file = "Match_Backbone_Compare_File.txt";

	public BackboneCompareFile (BaseViewerModel model) {
		// super (model, null);
	}

	protected void setColumnHeaders () {
		// OutputHelperFunctions.writeGenomesWithIndeces (model, out);
	}

	protected int getReferenceSequence () {
		// TODO Auto-generated method stub
		return 0;
	}

	protected int getSequenceCount () {
		// TODO Auto-generated method stub
		return 0;
	}

}
