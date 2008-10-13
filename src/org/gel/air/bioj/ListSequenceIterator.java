package org.gel.air.bioj;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;

public class ListSequenceIterator extends LinkedList <Sequence> implements
		SequenceIterator {

	protected Iterator iterator;
	
	public boolean hasNext () {
		if (iterator == null)
			iterator = iterator ();
		boolean ret = iterator.hasNext ();
		if (!ret)
			iterator = null;
		return ret;
	}

	public Sequence nextSequence () throws NoSuchElementException, BioException {
		if (iterator == null)
			return null;
		return (Sequence) iterator.next ();
	}

}
