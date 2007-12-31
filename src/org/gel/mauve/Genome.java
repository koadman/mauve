package org.gel.mauve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.biojava.bio.seq.Sequence;

public class Genome {
	private long length;

	private BaseViewerModel model;

	private Sequence annotationSequence;

	private List chromosomes = new ArrayList ();

	private String displayName;

	private SupportedFormat format;

	private String id;

	private long viewStart;

	private long viewLength;

	private int viewIndex;

	private Vector sortedMatches;
	
	private String uri;

	private int sourceIndex;
	private boolean visible = true;
	public Genome (long length, BaseViewerModel model, int sourceIndex) {
		this.length = length;
		this.model = model;
		this.sourceIndex = sourceIndex;
	}

	public int getSourceIndex () {
		return sourceIndex;
	}

	public long getLength () {
		return length;
	}

	public void setAnnotationSequence (Sequence annotationSequence,
			SupportedFormat format) {
		this.format = format;
		this.annotationSequence = annotationSequence;
	}

	public BaseViewerModel getModel () {
		return model;
	}

	// list must be in order by position in sequence!
	public void setChromosomes (List list) {
		this.chromosomes = list;
		System.out.println ("chrom: " + list.size());
	}

	public Sequence getAnnotationSequence () {
		return annotationSequence;
	}

	public SupportedFormat getAnnotationFormat () {
		return format;
	}

	public String getDisplayName () {
		return displayName;
	}

	public void setDisplayName (String displayName) {
		this.displayName = displayName;
	}

	public List getChromosomes () {
		return Collections.unmodifiableList (chromosomes);
	}

	public Chromosome getChromosomeAt (long loc) {

		if (loc < 1 || loc > length)
			return null;

		// This could be done smarter, if there are many chromosomes, since the
		// list is sorted.
		Iterator i = chromosomes.iterator ();
		while (i.hasNext ()) {
			Chromosome c = (Chromosome) i.next ();
			if (c.getStart () <= loc && loc <= c.getEnd ()) {
				return c;
			}
		}
		return null;
	}

	public void setID (String id) {
		this.id = id;
	}

	public String getID () {
		return id;
	}

	public long getViewLength () {
		return viewLength;
	}

	public void setViewLength (long viewLength) {
		this.viewLength = viewLength;
	}

	public long getViewStart () {
		return viewStart;
	}

	public void setViewStart (long viewStart) {
		this.viewStart = viewStart;
	}

	public int getViewIndex () {
		return viewIndex;
	}

	// TODO: Fire an event?
	public void setViewIndex (int viewIndex) {
		this.viewIndex = viewIndex;

		// Clear cache of matches.
		sortedMatches = null;
	}

	public Vector getSortedMatches () {
		try {
			if (sortedMatches == null) {
				sortedMatches = model
						.sortedMatches (new MatchStartComparator (this));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sortedMatches;
	}
	
	public boolean getVisible(){
    	return visible;
    }
    public void setVisible(boolean v){
    	visible = v;
    }

	public String getURI() {
		return uri;
	}

	public void setURI(String uri) {
		this.uri = uri;
	}

	public String toString () {
		return getDisplayName ();
	}
}