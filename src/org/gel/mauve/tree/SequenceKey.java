package org.gel.mauve.tree;

import java.io.Serializable;

public class SequenceKey implements Key, Cloneable, Serializable {
	static final long serialVersionUID = 1;

	private long lend;

	private long length = 0;

	public SequenceKey (long lend) {
		this.lend = lend;
	}

	public long getLength () {
		return length;
	}

	public void incrementLength () {
		length++;
	}

	public long getSeqLength () {
		return length;
	}

	public void cropStart (long size) {
		length -= size;
		lend += size;
		if (length < 0)
			throw new ArrayIndexOutOfBoundsException ();
	}

	public void cropEnd (long size) {
		length -= size;
		if (length < 0)
			throw new ArrayIndexOutOfBoundsException ();
	}

	public Object copy () {
		try {
			return clone ();
		} catch (CloneNotSupportedException e) {
			throw new Error ("Very unexpected exception", e);
		}
	}

	@Override
	public String toString () {
		return "SequenceKey[seq_offset: " + lend + " seq_len: " + length + "]";
	}

}