package org.gel.mauve;

import java.util.ArrayList;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.LocationTools;
import org.biojavax.bio.seq.Position;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.SimplePosition;
import org.biojavax.bio.seq.SimpleRichLocation;

public class Genome {
	private long length;

	private BaseViewerModel model;

	private Sequence annotationSequence;

	private List<Chromosome> chromosomes = new ArrayList<Chromosome> ();

	private String displayName;

	private SupportedFormat format;

	private String id;

	private long viewStart;

	private long viewLength;

	private int viewIndex;

	private Vector sortedMatches;

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
	}

	public Sequence getAnnotationSequence () {
		return annotationSequence;
	}

	public SupportedFormat getAnnotationFormat () {
		return format;
	}
	
	/**
	 * Returns annotations overlapping the given position of this 
	 * genome
	 * 
	 * @param left left position 
	 * @param right right position 
	 * @param rev true of the query position lies on complementary strand, false otherwise
	 * 
	 * @return a FeatureHolder containing the features in this genome that overlap the given position.
	 */
	public FeatureHolder getAnnotationsAt(long left, long right, boolean rev){
		Position min = new SimplePosition((int) left);
		Position max = new SimplePosition((int) right);
		RichLocation loc = new SimpleRichLocation (min,max,0,rev?
								RichLocation.Strand.NEGATIVE_STRAND : 
								RichLocation.Strand.POSITIVE_STRAND);
		return annotationSequence.filter(new FeatureFilter.OverlapsLocation(loc));
	}

	public String getDisplayName () {
		return displayName;
	}

	public void setDisplayName (String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Returns an unmodifiable sorted list of the chromosomes comprising this genome.
	 * 
	 * @return an unmodifiable sorted list of chromosomes.
	 */
	public List<Chromosome> getChromosomes () {
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

	public String toString () {
		return getDisplayName ();
	}
	/**
	 * Returns true if the specified chromosome is circular.
	 * 
	 * @param chrI the chromosome of interest
	 * @return true if chromosome <code>chrI</code> is circular, false otherwise.
	 */
	public boolean isCircular(int chrI){
		return ((Chromosome) chromosomes.get(chrI)).circular;
	}
	
}