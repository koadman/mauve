package org.gel.mauve;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.event.EventListenerList;

import org.gel.mauve.contigs.DefaultContigHandler;

/**
 * @author pinfield
 * 
 * A combination of alignment data, in the list of genomes, and viewing data, in
 * the form of everything else. This doesn't include LCBs or anything
 * XMFA-specific.
 * 
 * This is a test.
 * 
 */
public class BaseViewerModel {
	// Genomes, stored in the order in which they are being viewed.
	protected Genome [] genomes;

	// Genomes, stored in the order in which they appear in the source file.
	private Genome [] sourceGenomes;

	// Current viewer mode.
	private ViewerMode mode = ViewerMode.NORMAL;

	// Listeners for changes to model, and event to use.
	protected EventListenerList listenerList = new EventListenerList ();

	protected ModelEvent modelEvent = new ModelEvent (this);

	// Length of longest genome.
	private long longestRange = -1;

	// Color scheme to be used.
	private ColorScheme colorScheme;

	// Set of matches.
	private Vector matchVector = new Vector ();

	// The list of boxes to highlight
	private LinkedList highBoxes = new LinkedList ();

	// The reference genome, for displaying reversals.
	private Genome referenceGenome;

	// The highlighted genome.
	private Genome highlightGenome;

	// The sequence coordinate for the current highlight.
	private long highlightCoordinate;

	// Whether to draw matches.
	private boolean drawMatches = true;

	// Whether to draw contig/chromosome boundary lines.
	private boolean drawChromosomeBoundaries = true;

	// Original source file for this model.
	private File src;

	// Original source url for this model, if applicable.
	private URL sourceURL = null;

	// The genome in which a range of sequence has been highlighted
	private Genome rangeHighlightGenome = null;

	// the left end of the highlight range
	private long rangeHighlightLeft = -1;

	// the right end of the highlight range
	private long rangeHighlightRight = -1;
	
	//changes coordinates to and from contig to pseudogene system
	protected DefaultContigHandler contig_handler;

	/**
	 * Create a model with the specified source. Note that this does not load
	 * the model, use
	 * {@link org.gel.mauve.ModelBuilder#buildModel(File,ModelProgressListener)}
	 * for reading files.
	 * 
	 * @param src
	 *            file from which data came
	 */
	public BaseViewerModel (File src) {
		this.src = src;
		contig_handler = new DefaultContigHandler (this);
	}

	/**
	 * Returns the file from which this model was derived.
	 * 
	 * @return source file
	 */
	public File getSrc () {
		return src;
	}

	/**
	 * Sets the URL from which the model was originally derived.
	 * 
	 * @param url
	 */
	public void setSourceURL (URL url) {
		this.sourceURL = url;
	}

	/**
	 * Returns the URL from which the model was originally derived.
	 * 
	 * @return source URL
	 */
	public URL getSourceURL () {
		return sourceURL;
	}

	/**
	 * Set the number of genomes to be added to this model. Only used at
	 * construction-time, since this will clear out preexisting genome data.
	 * 
	 * @param sequenceCount
	 *            the number of sequences to be displayed.
	 */
	public void setSequenceCount (int sequenceCount) {
		// NEWTODO Make the list of genomes simply a list
		genomes = new Genome [sequenceCount];
		sourceGenomes = new Genome [sequenceCount];
	}

	/**
	 * @return Returns the number of sequences being displayed.
	 */
	public int getSequenceCount () {
		return genomes.length;
	}

	/**
	 * Returns the Nth genome in the model, as indexed according to the viewing
	 * order. That is, suppose that the file contains the order A B C D, and the
	 * viewer shows A B D C. Then, getGenome(3) will return genome C.
	 * 
	 * @param viewIndex
	 * @return genome at viewIndex.
	 */
	public Genome getGenomeByViewingIndex (int viewIndex) {
		return genomes[viewIndex];
	}

	public Genome getGenomeBySourceIndex (int sourceIndex) {
		return sourceGenomes[sourceIndex];
	}

	/**
	 * Returns an Vector containing all the Genomes in this BaseViewerModel
	 * 
	 * @return Vector of genomes
	 */
	public Vector getGenomes () {
		Vector ret = new Vector ();
		for (int i = 0; i < genomes.length; i++)
			ret.add (genomes[i]);
		return ret;
	}

	/**
	 * Set the genome at viewing position viewIndex to g. This will update the
	 * viewing index stored in the genome, as well. This also has the
	 * side-effect of resetting the view length for all genomes.
	 * 
	 * @param viewIndex
	 * @param g
	 */
	public void setGenome (int viewIndex, Genome g) {
		genomes[viewIndex] = g;
		g.setViewIndex (viewIndex);

		sourceGenomes[g.getSourceIndex ()] = g;

		// Update longest range.
		if (longestRange < g.getLength ()) {
			longestRange = g.getLength ();
		}

		// Set lengths.
		for (int i = 0; i < genomes.length; i++) {
			Genome genome = genomes[i];
			if (genome != null) {
				genome.setViewLength (longestRange);
			}
		}
	}

	/**
	 * Returns the number of Matches held by this model.
	 * 
	 * @return number of matches
	 */
	public int getMatchCount () {
		return matchVector.size ();
	}

	/**
	 * Return the specified Match.
	 * 
	 * @param index
	 * @return match
	 */
	public Match getMatch (int index) {
		return (Match) matchVector.elementAt (index);
	}

	/**
	 * 
	 * Get a copy of the list of matches, sorted according to the given
	 * comparator.
	 * 
	 * @param c
	 *            a Match comparator
	 * @return sorted list of matches
	 */
	public Vector sortedMatches (Comparator c) {
		Vector sorted = (Vector) matchVector.clone ();
		if (c != null)
			Collections.sort (sorted, c);
		return sorted;
	}

	/**
	 * Add a match to the list of matches.
	 * 
	 * @param m
	 *            match to add
	 */
	public void addMatch (Match m) {
		matchVector.add (m);
	}

	/**
	 * Set the color scheme to be applied in the viewer, and apply it. This will
	 * cause the firing of {@link ModelListener@colorChanged(ModelEvent)} for
	 * any registered listeners, if the scheme is different than the current
	 * one.
	 * 
	 * @param colorScheme
	 *            new color scheme
	 */
	public void setColorScheme (ColorScheme colorScheme) {
		if (!colorScheme.equals (this.colorScheme)) {
			this.colorScheme = colorScheme;
			colorScheme.apply (this);

			fireColorEvent ();
		}
	}

	/**
	 * Returns the currently-applied color scheme.
	 * 
	 * @return color scheme
	 */
	public ColorScheme getColorScheme () {
		return colorScheme;
	}

	/**
	 * Add a ModelListener to the list of listeners for the model.
	 * 
	 * @param l
	 *            listener to add
	 */
	public void addModelListener (ModelListener l) {
		listenerList.add (ModelListener.class, l);
	}

	/**
	 * Remove a ModelListener from the list of listeners for this model.
	 * 
	 * @param l
	 *            listener to remove
	 */
	public void removeModelListener (ModelListener l) {
		listenerList.remove (ModelListener.class, l);
	}

	/**
	 * Add a HighlightListener to the list of listeners for the model.
	 * 
	 * @param l
	 *            listener to add
	 */
	public void addHighlightListener (HighlightListener l) {
		listenerList.add (HighlightListener.class, l);
	}

	/**
	 * Remove a HighlightListener from the list of listeners for this model.
	 * 
	 * @param l
	 *            listener to remove
	 */
	public void removeHighlightListener (HighlightListener l) {
		listenerList.remove (HighlightListener.class, l);
	}

	/**
	 * Invoke {@link HighlightListener.highlightChanged(ModelEvent)} on this
	 * model's collection of HighlightListeners.
	 */
	protected void fireHighlightEvent () {
		Object [] listeners = listenerList.getListenerList ();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == HighlightListener.class) {
				((HighlightListener) listeners[i + 1])
						.highlightChanged (modelEvent);
			}
		}
	}

	/**
	 * Invoke {@link ModelListener.colorChanged(ModelEvent)} on this model's
	 * collection of ModelListeners.
	 */
	protected void fireColorEvent () {
		Object [] listeners = listenerList.getListenerList ();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ModelListener.class) {
				((ModelListener) listeners[i + 1]).colorChanged (modelEvent);
			}
		}
	}

	/**
	 * Invoke {@link ModelListener.weightChanged(ModelEvent)} on this model's
	 * collection of ModelListeners.
	 */
	protected void fireWeightEvent () {
		Object [] listeners = listenerList.getListenerList ();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ModelListener.class) {
				((ModelListener) listeners[i + 1]).weightChanged (modelEvent);
			}
		}
	}

	/**
	 * Invoke {@link ModelListener.drawingSettingsChanged(ModelEvent)} on this
	 * model's collection of ModelListeners.
	 */
	protected void fireDrawingSettingsEvent () {
		Object [] listeners = listenerList.getListenerList ();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ModelListener.class) {
				((ModelListener) listeners[i + 1])
						.drawingSettingsChanged (modelEvent);
			}
		}
	}

	/**
	 * Invoke {@link ModelListener.modeChanged(ModelEvent)} on this model's
	 * collection of ModelListeners.
	 */
	protected void fireModeEvent () {
		Object [] listeners = listenerList.getListenerList ();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ModelListener.class) {
				((ModelListener) listeners[i + 1]).modeChanged (modelEvent);
			}
		}
	}

	/**
	 * Invoke{@link ModelListener.printingStart(ModelEvent)} on this
	 * model's collection of ModelListeners.
	 */
	public void firePrintingStartEvent() {
		Object [] listeners = listenerList.getListenerList ();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ModelListener.class) {
				((ModelListener) listeners[i + 1])
						.printingStart (modelEvent);
			}
		}
	}

	/**
	 * Invoke{@link ModelListener.printingEnd(ModelEvent)} on this
	 * model's collection of ModelListeners.
	 */
	public void firePrintingEndEvent() {
		Object [] listeners = listenerList.getListenerList ();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ModelListener.class) {
				((ModelListener) listeners[i + 1])
						.printingEnd (modelEvent);
			}
		}
	}

	/**
	 * Invoke {@link ModelListener.viewableRangeChanged(ModelEvent)} on this
	 * model's collection of ModelListeners.
	 */
	public void fireViewableRangeEvent () {
		Object [] listeners = listenerList.getListenerList ();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ModelListener.class) {
				((ModelListener) listeners[i + 1])
						.viewableRangeChanged (modelEvent);
			}
		}
	}

	/**
	 * Invoke {@link ModelListener.viewableRangeChangeStart(ModelEvent)} on this
	 * model's collection of ModelListeners.
	 */
	protected void fireViewableRangeStartEvent () {
		Object [] listeners = listenerList.getListenerList ();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ModelListener.class) {
				((ModelListener) listeners[i + 1])
						.viewableRangeChangeStart (modelEvent);
			}
		}
	}

	/**
	 * Invoke {@link ModelListener.viewableRangeChangeEnd(ModelEvent)} on this
	 * model's collection of ModelListeners.
	 */
	protected void fireViewableRangeEndEvent () {
		Object [] listeners = listenerList.getListenerList ();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ModelListener.class) {
				((ModelListener) listeners[i + 1])
						.viewableRangeChangeEnd (modelEvent);
			}
		}
	}

	/**
	 * Invoke {@link ModelListener.genomesReordered(ModelEvent)} on this model's
	 * collection of ModelListeners.
	 */
	protected void fireReorderGenomeEvent () {
		Object [] listeners = listenerList.getListenerList ();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ModelListener.class) {
				((ModelListener) listeners[i + 1])
						.genomesReordered (modelEvent);
			}
		}
	}

	/**
	 * Invoke {@link ModelListener.referenceChanged(ModelEvent)} on this model's
	 * collection of ModelListeners.
	 */
	protected void fireReferenceChangedEvent () {
		Object [] listeners = listenerList.getListenerList ();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ModelListener.class) {
				((ModelListener) listeners[i + 1])
						.referenceChanged (modelEvent);
			}
		}
	}

	/**
	 * Returns the current drawing mode for the application.
	 * 
	 * @return
	 */
	public ViewerMode getMode () {
		return mode;
	}

	/**
	 * Set the current drawing mode for the application. If this results in a
	 * change of mode, then {@link ModelListener#modeChanged(ModelEvent)} will
	 * be fired for all of the model's ModelListeners.
	 * 
	 * @param mode
	 */
	public void setMode (ViewerMode mode) {
		if (this.mode != mode) {
			this.mode = mode;
			fireModeEvent ();
		}
	}

	/**
	 * Reorder genomes according to given permutation array. For example, if the
	 * genomes are being viewed in the order A B C D, and the array is 1 0 3 2,
	 * then the new viewing order will be B A D C. The permutation array is
	 * applied to the genomes in viewing order, not source order.
	 * 
	 * This method will cause the firing of
	 * {@link ModelListener#genomesReordered(ModelEvent)} for all registered
	 * ModelListeners.
	 * 
	 * @param new_order
	 *            permutation array
	 */
	public void reorderSequences (int [] new_order) {
		// Reorder genomes
		Genome [] newGenomes = new Genome [genomes.length];
		for (int seqI = 0; seqI < genomes.length; seqI++) {
			newGenomes[seqI] = genomes[new_order[seqI]];
			newGenomes[seqI].setViewIndex (seqI);
		}
		genomes = newGenomes;

		fireReorderGenomeEvent ();
	}

	public void setReference (Genome g) {
		referenceGenome = g;
		referenceUpdated ();
		fireReferenceChangedEvent ();
	}

	public Genome getReference () {
		return referenceGenome;
	}

	protected void referenceUpdated () {
		// Hook for subclasses.
	}

    public void setVisible(Genome g, boolean visible)
    {
    	if(g.getVisible() != visible )
    	{
    		g.setVisible(visible);
    		fireGenomeVisibilityChangedEvent();
    	}
    }
    
    public void fireGenomeVisibilityChangedEvent()
    {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ModelListener.class) {
                ((ModelListener)listeners[i+1]).genomeVisibilityChanged(modelEvent);
            }
        }
    }

	private void correctMatchReversals () {
		// Correct the orientation due to reversals.
		for (int matchI = 0; matchI < getMatchCount (); matchI++) {
			Match m = getMatch (matchI);

			// Find the first genome with something to show for each match
			// and make that the reference orientation.
			for (int seqI = 0; seqI < genomes.length; seqI++) {
				Genome g = getGenomeByViewingIndex (seqI);

				if (m.getStart (g) != 0) {
					if (m.getReverse (g)) {
						for (int seqJ = seqI; seqJ < genomes.length; seqJ++) {
							Genome g2 = getGenomeByViewingIndex (seqJ);
							m.setReverse (g2, !m.getReverse (g2));
						}
					}
					break;
				}
			}
		}
	}

	/**
	 * Provides a range of indexes into the m_matches array which contain
	 * matches intersecting with the specified range of coordinates
	 * 
	 * @param start_coord
	 *            The first coordinate of the intersection range
	 * @param end_coord
	 *            The last coordinate of the intersection range
	 * @param match_range
	 *            An int array with 2 elements. The resulting range of
	 *            intersecting match indices will be returned as the first and
	 *            second elements in the array.
	 */
	public void getMatchRange (Genome g, long start_coord, long end_coord,
			int [] match_range) {
		Vector sortedMatches = g.getSortedMatches ();

		if (sortedMatches.size () == 0) {
			match_range[0] = -1;
			match_range[1] = -1;
			return;
		}

		MatchStartComparator matchComparator = new MatchStartComparator (g);
		Match f_match = new Match ((Match) sortedMatches.get (0));

		f_match.setStart (g, start_coord);
		if (f_match.getStart (g) < 1)
			f_match.setStart (g, 1);

		int match_startI = Collections.binarySearch (sortedMatches, f_match,
				matchComparator);
		match_startI = match_startI >= 0 ? match_startI : -match_startI - 1;

		f_match.setStart (g, end_coord + 1);
		int match_endI = Collections.binarySearch (sortedMatches, f_match,
				matchComparator);
		match_endI = match_endI >= 0 ? match_endI : -match_endI - 1;

		// now look backwards 1 match for an intersection
		if (match_startI > 0) {
			Match prev_match = (Match) sortedMatches.get (match_startI - 1);

			if (prev_match.getStart (g) + prev_match.getLength (g) > start_coord
					&& prev_match.getStart (g) != Match.NO_MATCH)
				match_startI--;
		}
		match_range[0] = match_startI;
		match_range[1] = match_endI;
	}

	/**
	 * Adds a highlighted match to the list of currently highlighted matches.
	 * This method does not highlight the match, it ensures that when another
	 * group of matches are highlighted, this match can be found on the list and
	 * cleared of highlighting.
	 */
	public void addMatchHighlight (Match highlighted_box) {
		ListIterator high_iter = highBoxes.listIterator ();
		while (high_iter.hasNext ()) {
			if (high_iter.next () == highlighted_box) {
				return;
			}
		}
		highlighted_box.highlighted = true;
		highBoxes.addLast (highlighted_box);

		fireHighlightEvent ();
	}

	public Match lastMatchHighlight () {
		if (highBoxes.isEmpty ())
			return null;

		return (Match) highBoxes.getLast ();

	}

	/**
	 * Unhighlights any boxes which are currently highlighted.
	 */
	public void clearMatchHighlights () {
		ListIterator high_iter = highBoxes.listIterator ();
		while (high_iter.hasNext ()) {
			Match next_box = (Match) high_iter.next ();
			next_box.highlighted = false;
		}
		highBoxes.clear ();

		fireHighlightEvent ();
	}

	public long getHighlightCoordinate () {
		return highlightCoordinate;
	}

	public Genome getHighlightGenome () {
		return highlightGenome;
	}

	public long getRangeHighlightLeft () {
		return rangeHighlightLeft;
	}

	public long getRangeHighlightRight () {
		return rangeHighlightRight;
	}

	public Genome getRangeHighlightGenome () {
		return rangeHighlightGenome;
	}

	public void updateHighlight (Genome g, long coordinate) {
		highlightGenome = g;
		highlightCoordinate = coordinate;
		fireHighlightEvent ();
	}

	public void setDrawMatches (boolean value) {
		if (value != drawMatches) {
			this.drawMatches = value;
			this.fireDrawingSettingsEvent ();
		}
	}

	public boolean getDrawMatches () {
		return drawMatches;
	}

	public void setDrawChromosomeBoundaries (boolean value) {
		if (value != drawChromosomeBoundaries) {
			this.drawChromosomeBoundaries = value;
			this.fireDrawingSettingsEvent ();
		}
	}

	public boolean getDrawChromosomeBoundaries () {
		return drawChromosomeBoundaries;
	}

	public void setFocus (String sequenceID, long start, long end, String contig) {
		Genome g = null;
		for (int i = 0; i < genomes.length; i++) {
			if (sequenceID.equals (genomes[i].getID ())) {
				g = genomes[i];
				break;
			}
		}
		if (g == null) {
			System.err
					.println ("Received focus request for nonexistent sequence id "
							+ sequenceID);
			return;
		}
		if (contig != null) {
			start = contig_handler.getPseudoCoord(g.getSourceIndex(), start, contig);
			end = contig_handler.getPseudoCoord(g.getSourceIndex(), end, contig);
		}
		long len = (end - start) * 5;
		int zoom = (int) (100 * g.getViewLength () / (double) len);
		long center = start + ((end - start) / 2);
		zoomAndCenter (g, zoom, center);
		// highlight this part of the display
		highlightRange (g, start, end);
	}

	/** ************ Sequence alignment ***************** */
	/**
	 * Align the display to a particular set of coordinates in each sequence,
	 * using one sequence to set the viewable width
	 */
	public void alignView (long [] align_coords, Genome selected) {
		fireViewableRangeStartEvent ();

		// adjust everything to the same zoom level as the selected sequence

		long start_offset = align_coords[selected.getSourceIndex ()]
				- selected.getViewStart ();
		for (int seqI = 0; seqI < getSequenceCount (); seqI++) {
			if (seqI != selected.getSourceIndex ()
					&& align_coords[seqI] != Match.NO_MATCH) {
				Genome g = getGenomeBySourceIndex (seqI);
				g.setViewStart (align_coords[seqI] - start_offset);
				g.setViewLength (selected.getViewLength ());
			}
		}
		fireViewableRangeEvent ();

		fireViewableRangeEndEvent ();
	}

	public void alignView (Match align_match, Genome g) {
		long [] align_coords = new long [getSequenceCount ()];

		for (int i = 0; i < getSequenceCount (); i++) {
			align_coords[i] = align_match.getStart (getGenomeBySourceIndex (i));
		}

		alignView (align_coords, g);
	}

	/**
	 * Shift and zoom the displayed sequence regions relative to their currently
	 * viewable coordinates.
	 * 
	 * @param zoom_percent
	 *            0 will zoom all the way out. 100 does nothing. values between
	 *            1 and 99 zoom out, values greater than 100 zoom in
	 * @param move_percent
	 *            negative values shift to the left, positive values shift to
	 *            the right. zero does nothing. A value of 100 would shift the
	 *            display one full viewing range to the right, such that the
	 *            rightmost coordinate previously viewable would now be at the
	 *            left edge of the display.
	 */
	public void zoomAndMove (int zoom_percent, int move_percent) {
		for (int i = 0; i < genomes.length; i++) {
			zoomAndMove (genomes[i], zoom_percent, move_percent);
		}
	}

	public void zoomAndMove (int zoom_percent, long offset) {
		for (int i = 0; i < genomes.length; i++) {
			zoomAndMove (genomes[i], zoom_percent, offset);
		}
	}

	/**
	 * Shift and zoom the displayed sequence regions relative to their currently
	 * viewable coordinates
	 * 
	 * @param zoom_percent
	 *            0 will zoom all the way out. 100 does nothing. values between
	 *            1 and 99 zoom out, values greater than 100 zoom in
	 * @param move_percent
	 *            negative values shift to the left, positive values shift to
	 *            the right. zero does nothing. A value of 100 would shift the
	 *            display one full viewing range to the right, such that the
	 *            rightmost coordinate previously viewable would now be at the
	 *            left edge of the display.
	 */
	public void zoomAndMove (Genome g, int zoom_percent, int move_percent) {

		if (move_percent != 0) {
			double move = ((double) g.getViewLength ()) * (move_percent / 100d);
			if (Math.abs (move) < 1) {
				move = (move < 0) ? -1 : 1;
			}
			zoomAndMove (g, zoom_percent, (long) move);
		} else {
			zoomAndMove (g, zoom_percent, 0L);
		}
	}

	private void zoomAndMove (Genome g, int zoom_percent, long offset) {
		fireViewableRangeStartEvent ();

		// move before zooming so that we end up on the right coordinates!
		if (offset != 0) {
			// calculate the new viewable coordinates.
			long new_view_start = g.getViewStart () + offset;

			// don't let the sequence get completely out of range
			if (new_view_start + g.getViewLength () < 1) {
				new_view_start = 1 - g.getViewLength ();
			} else if (new_view_start > g.getLength ()) {
				new_view_start = g.getLength ();
			}
			g.setViewStart (new_view_start);
		}

		if (zoom_percent == 0) {
			// zoom all the way out.
			g.setViewStart (1);
			g.setViewLength (longestRange);
		} else if (zoom_percent != 100) {
			// calculate the new viewable coordinates.
			long new_view_length = (long) ((100d * g.getViewLength ()) / zoom_percent);
			// don't let the view length get too big or small
			if (new_view_length < 5)
				new_view_length = 5;
			if (new_view_length > longestRange)
				new_view_length = longestRange;

			long new_view_start = g.getViewStart ()
					+ ((g.getViewLength () - new_view_length) / 2);

			// don't let the sequence get completely out of range
			if (new_view_start + new_view_length < 1)
				new_view_start = 1 - g.getViewLength ();
			else if (new_view_start > g.getLength ())
				new_view_start = g.getLength ();

			g.setViewStart (new_view_start);
			g.setViewLength (new_view_length);
		}

		fireViewableRangeEvent ();

		fireViewableRangeEndEvent ();
	}

	public void zoomAndCenter (Genome g, int zoom_percent, long coord) {
		long offset = coord - (g.getViewStart () + (g.getViewLength () / 2));
		zoomAndMove (zoom_percent, offset);
	}

	/** Apply translucent highlighting to a range of sequence */
	public void highlightRange (Genome g, long left_end, long right_end) {
		rangeHighlightGenome = g;
		rangeHighlightLeft = left_end;
		rangeHighlightRight = right_end;
		fireHighlightEvent ();
	}

}
