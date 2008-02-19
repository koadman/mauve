package org.gel.mauve.ext.lazy;

import org.biojava.bio.seq.impl.SimpleSequence;
import org.biojava.bio.symbol.SymbolList;

public class LazySequence extends SimpleSequence {

	public LazySequence (SymbolList list) {
		super (list, null, null, null);
	}
	

}
