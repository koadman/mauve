package org.gel.mauve.tree;

import java.io.Serializable;


public class FileKey extends SequenceKey implements Cloneable, Serializable {
	static final long serialVersionUID = 2;
	
	private long f_offset;

	private long f_length = 0;
	
	public FileKey (long lend, long f_offset) {
		super (lend);
		this.f_offset = f_offset;
	}
	
	public long getOffset () {
		return f_offset;
	}

	public long getFLength () {
		return f_length;
	}

	public void setFLength (long f_length) {
		this.f_length = f_length;
	}
	
	public String toString () {
		String name = super.toString();
		name = name.substring(0, name.length() - 1);
		name += " bfo: " + f_offset + " len: " + f_length + "]";
		return name;
	}

}