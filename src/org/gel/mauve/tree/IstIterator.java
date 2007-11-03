package org.gel.mauve.tree;

import java.util.Iterator;

class IstIterator implements Iterator {
	IstNode node;

	IntervalSequenceTree ist;

	IstIterator (IntervalSequenceTree ist, IstNode node) {
		this.ist = ist;
		this.node = node;
	}

	// Returns true if the iteration has more elements.
	public boolean hasNext () {
		return ist.increment (node) != null;
	}

	// Returns the next element in the iteration.
	public Object next () {
		node = ist.increment (node);
		return node.getKey ();
	}

	// Removes from the underlying collection the last element returned by the
	// iterator (optional operation).
	public void remove () {

	}
}