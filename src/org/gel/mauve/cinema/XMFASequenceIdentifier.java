package org.gel.mauve.cinema;

import uk.ac.man.bioinf.sequence.identifier.SimpleIdentifier;

public class XMFASequenceIdentifier extends SimpleIdentifier {
	private long [] range;

	public XMFASequenceIdentifier (String title, long [] range) {
		super (title);
		this.range = range;
	}

	public long [] getRange () {
		return range;
	}

}
