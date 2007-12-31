package org.gel.mauve;

public class Chromosome {
	private long start;

	private long end;

	private String name;

	boolean circular;

	public Chromosome (long start, long end, String name, boolean circular) {
		this.start = start;
		this.end = end;
		this.name = name.trim ();
		this.circular = circular;
	}

	public long getStart () {
		return start;
	}

	public long getEnd () {
		return end;
	}
	
	public long getLength () {
		return end - start + 1;
	}

	public String getName () {
		return name;
	}
	
	public void setName (String n) {
		name = n;
	}

	public long relativeLocation (long location) {
		return location - start + 1;
	}

	public boolean getCircular () {
		return circular;
	}
	
	public String toString () {
		return getName () + " (" + getStart () + "," + getEnd () + ")";
	}
	
	public boolean equals (Object comp) {
		Chromosome chrom = (Chromosome) comp;
		boolean ret = getName ().equals(chrom.getName ());
		return ret;
	}
}