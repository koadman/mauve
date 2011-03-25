package org.gel.mauve.recombination;

import java.io.Serializable;

import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.histogram.ZoomHistogram;

public class WeakArgDataModel implements Serializable
{
	String treeString;
	ZoomHistogram[] incoming;
	ZoomHistogram[] outgoing;

	WeakArgDataModel( XmfaViewerModel xmfa ){
		incoming = new ZoomHistogram[xmfa.getSequenceCount()*2 - 1];
		outgoing = new ZoomHistogram[xmfa.getSequenceCount()*2 - 1];
	}
}
