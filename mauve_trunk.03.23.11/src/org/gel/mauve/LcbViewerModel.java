package org.gel.mauve;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.gel.mauve.analysis.PermutationExporter;
import org.gel.mauve.color.LCBColorScheme;

/**
 * @author Paul Infield-Harm
 * 
 * Data model for viewer.
 */
public class LcbViewerModel extends BaseViewerModel {
	// The sequence coordinates of currently viewed LCB boundaries
	private LCB [] visibleLcbList = new LCB [0];

	// The sequence coordinates of the complete set of LCB boundaries
	private LCB [] fullLcbList = new LCB [0];

	// The list of deleted LCBs that should not be shown
	private LCB [] delLcbList = new LCB [0];

	private List originalMatchLcbs = new ArrayList ();

	// Tracks weight values which cause LCBs to drop out, each one of these
	// becomes a legal value on the LCB weight slider
	private Vector lcb_change_points;

	private long lcbCount = 0;

	private long minimumLCBWeight;

	private boolean drawLCBbounds = true;

	private boolean fillLCBboxes = false;

	private long highlightCoordinateRight = -1;

	private boolean nway_lcb_list = false;

	public LcbViewerModel (File src) {
		super (src);
	}

	public void updateHighlight (Genome g, long coordinate) {
		highlightCoordinateRight = coordinate;
		super.updateHighlight (g, coordinate);
	}

	public void updateHighlight (Genome g, long leftCoordinate,
			long rightCoordinate) {
		highlightCoordinateRight = rightCoordinate;
		super.updateHighlight (g, leftCoordinate);
	}

	/**
	 * @param lcbList
	 *            sequence coordinates of currently viewed LCB boundaries.
	 */
	public void setVisibleLcbList (LCB [] lcbList) {
		this.visibleLcbList = lcbList;
	}

	/**
	 * @return Returns the sequence coordinates of currently viewed LCB
	 *         boundaries.
	 */
	public LCB [] getVisibleLcbList () {
		return visibleLcbList;
	}

	public LCB getVisibleLcb (int index) {
		return visibleLcbList[index];
	}

	public void setVisibleLcb (int index, LCB lcb) {
		visibleLcbList[index] = lcb;
	}

	public int getVisibleLcbCount () {
		return visibleLcbList.length;
	}

	public boolean isNwayLcbList () {
		return nway_lcb_list;
	}

	public LCB getLeftmostVisibleLCB (Genome g) {
		for (int i = 0; i < visibleLcbList.length; i++) {
			if (visibleLcbList[i].getLeftAdjacency (g) < 0) {
				return visibleLcbList[i];
			}
		}
		return null;
	}

	public LCB getVisibleRightNeighbor (LCB lcb, Genome g, long pos) {
		int rightIndex = lcb.getRightAdjacency (g);
		while (rightIndex >= 0
				&& pos > visibleLcbList[rightIndex].getRightEnd (g)) {
			rightIndex = visibleLcbList[rightIndex].getRightAdjacency (g);
		}

		if (rightIndex < 0) {
			return null;
		} else {
			return visibleLcbList[rightIndex];
		}

	}

	public LCB getDeletedRightNeighbor (LCB lcb, Genome g, long pos) {
		int rightIndex = lcb.getRightAdjacency (g);
		while (rightIndex >= 0 && pos > delLcbList[rightIndex].getRightEnd (g)) {
			rightIndex = delLcbList[rightIndex].getRightAdjacency (g);
		}

		if (rightIndex < 0) {
			return null;
		} else {
			return delLcbList[rightIndex];
		}

	}

	public LCB getLeftmostDeletedLCB (Genome g) {
		for (int i = 0; i < delLcbList.length; i++) {
			if (delLcbList[i].getLeftAdjacency (g) < 0) {
				return delLcbList[i];
			}
		}
		return null;
	}

	/**
	 * @param fullLcbList
	 *            sequence coordinates of the complete set of LCB boundaries.
	 */
	void setFullLcbList (LCB [] fullLcbList) {
		this.fullLcbList = fullLcbList;
	}

	/**
	 * @return Returns the sequence coordinates of the complete set of LCB
	 *         boundaries.
	 */
	public LCB [] getFullLcbList () {
		return fullLcbList;
	}
	
	/**
	 * @param delLcbList
	 *            list of deleted LCBs that should not be shown.
	 */
	public void setDelLcbList (LCB [] delLcbList) {
		this.delLcbList = delLcbList;
	}

	/**
	 * @return Returns the list of deleted LCBs that should not be shown.
	 */
	public LCB [] getDelLcbList () {
		return delLcbList;
	}

	/**
	 * @param lcbCount
	 *            The lcbCount to set.
	 */
	public void setLcbCount (long lcbCount) {
		this.lcbCount = lcbCount;
	}

	/**
	 * @return Returns the lcbCount.
	 */
	public long getLcbCount () {
		return lcbCount;
	}

	public LCB getLCB (int match_start, int match_end) {
		int seqI;
		LCB lcb = new LCB (getSequenceCount ());

		if (match_start >= match_end)
			return null;

		// find left end of lcb in each sequence
		int matchI = match_start;
		long count = 0;
		for (; matchI < match_end; matchI++) {
			Match m = getMatch (matchI);
			for (seqI = 0; seqI < getSequenceCount (); seqI++) {
				Genome g = getGenomeByViewingIndex (seqI);

				if (lcb.getLeftEnd (g) == Match.NO_MATCH) {
					if (m.getStart (g) != Match.NO_MATCH) {
						if (!m.getReverse (g)) {
							lcb.setLeftEnd (g, m.getStart (g));
						} else {
							lcb
									.setLeftEnd (g, m.getStart (g)
											+ m.getLength (g));
						}
					}
				} else
					count++;
			}
			if (count == getSequenceCount ())
				break;
		}
		// if this LCB is only defined in a single sequence then bail out
		count = 0;
		for (seqI = 0; seqI < getSequenceCount (); seqI++) {
			Genome g = getGenomeBySourceIndex (seqI);

			if (lcb.getLeftEnd (g) != Match.NO_MATCH) {
				count++;
			}
		}
		if (count < 2) {
			return null;
		}

		// find end in each sequence
		count = 0;
		for (matchI = match_end; matchI > match_start; matchI--) {
			Match m = getMatch (matchI - 1);
			for (seqI = 0; seqI < getSequenceCount (); seqI++) {
				Genome g = getGenomeByViewingIndex (seqI);

				if (lcb.getRightEnd (g) == Match.NO_MATCH) {
					if (m.getStart (g) != Match.NO_MATCH) {
						if (!m.getReverse (g))
							lcb.setRightEnd (g, m.getStart (g)
									+ m.getLength (g));
						else
							lcb.setRightEnd (g, m.getStart (g));
					}
				} else
					count++;
			}
			if (count == getSequenceCount ())
				break;
		}
		Match m = getMatch (match_start);
		for (seqI = 0; seqI < getSequenceCount (); seqI++) {
			Genome g = getGenomeByViewingIndex (seqI);
			if (m.getReverse (g)) {
				long tmp = lcb.getLeftEnd (g);
				lcb.setLeftEnd (g, lcb.getRightEnd (g));
				lcb.setRightEnd (g, tmp);
			}
		}

		// set LCB weight
		lcb.weight = 0;
		Genome g0 = getGenomeByViewingIndex (0);
		for (matchI = match_start; matchI < match_end; matchI++) {
			m = getMatch (matchI);
			lcb.weight += m.getLength (g0);
		}

		// set keep flag to false
		lcb.keep = false;

		for (int i = 0; i < getSequenceCount (); i++) {
			Genome g = getGenomeBySourceIndex (i);
			lcb.setReverse (g, m.getReverse (g));
		}

		return lcb;
	}

	protected void referenceUpdated () {
		for (int lcbI = 0; lcbI < fullLcbList.length; lcbI++) {
			LCB lcb = fullLcbList[lcbI];
			lcb.setReference (getReference ());
		}

		for (int lcbI = 0; lcbI < visibleLcbList.length; lcbI++) {
			LCB lcb = visibleLcbList[lcbI];
			lcb.setReference (getReference ());
		}

		for (int lcbI = 0; lcbI < delLcbList.length; lcbI++) {
			LCB lcb = delLcbList[lcbI];
			lcb.setReference (getReference ());
		}

		/*
		 * for (int matchI = 0; matchI < getMatchCount(); matchI++) { Match m =
		 * getMatch(matchI); for (int genomeI = 0; genomeI < getSequenceCount();
		 * genomeI++) { Genome g = getGenomeBySourceIndex(genomeI); if( m.lcb !=
		 * LCBlist.ENDPOINT && m.lcb != LCBlist.REMOVED ) m.setReverse(g,
		 * fullLcbList[m.lcb].getReverse(g)); } }
		 */
	}

	public long [] getHighlightArray (Genome g) {
		long high_c = getHighlightCoordinate ();
		long high_cr = highlightCoordinateRight;
		Genome high_g = getHighlightGenome ();
		long [] no_highlight = new long [1];
		no_highlight[0] = Match.NO_MATCH;
		if (high_c == Match.NO_MATCH || high_g == null) {
			return no_highlight;
		}
		Vector sortedMatches = high_g.getSortedMatches ();
		int [] match_range = new int [2];
		getMatchRange (high_g, high_c, high_cr, match_range);
		if (match_range[1] == match_range[0])
			return no_highlight;
		long [] g_highlights = new long [match_range[1] - match_range[0]];
		int highI = 0;
		for (int matchI = match_range[0]; matchI < match_range[1]; matchI++) {
			Match m = ((Match) sortedMatches.get (matchI));
			if (m.getReverse (high_g) == m.getReverse (g))
				g_highlights[highI] = m.getStart (g)
						+ (high_c - m.getStart (high_g));
			else
				g_highlights[highI] = m.getStart (g) + m.getLength (g)
						- (high_c - m.getStart (high_g));
			highI++;
		}
		return g_highlights;
	}

	/**
	 * aligns the display to a particular position of a particular sequence.
	 * typically called by RRSequencePanel when the user clicks a part of the
	 * sequence.
	 */
	public void alignView (Genome g, long left, long right) {
		Vector sortedMatches = g.getSortedMatches ();
		int [] match_range = new int [2];
		getMatchRange (g, left, right, match_range);
		if (match_range[1] == match_range[0])
			return;
		// construct the coords array
		Match m = ((Match) sortedMatches.get (match_range[0]));
		long [] coords = new long [genomes.length];
		for (int gI = 0; gI < genomes.length; gI++) {
			Genome cur_g = getGenomeBySourceIndex (gI);
			if (m.getReverse (cur_g) == m.getReverse (g))
				coords[gI] = m.getStart (cur_g) + (right - m.getStart (g));
			else
				coords[gI] = m.getStart (cur_g) + m.getLength (cur_g)
						- (right - m.getStart (g));
		}
		alignView (coords, g);
	}

	public void alignView (Genome g, long coord) {
		alignView (g, coord - 1, coord + 1);
	}

	public void updateLCBweight (int min_weight, boolean temporary) {
		setMinLCBWeight (min_weight);

		// Temporarily make all LCBs visible.
		visibleLcbList = new LCB [fullLcbList.length];
		for (int lcbI = 0; lcbI < fullLcbList.length; lcbI++) {
			visibleLcbList[lcbI] = new LCB (fullLcbList[lcbI]);
		}

		// Eliminate LCBs with weight below min_weight.
		LCBlist.greedyBreakpointElimination (min_weight, visibleLcbList, null,
				this);

		if (temporary) {
			visibleLcbList = LCBlist.filterLCBs (visibleLcbList, this, null,
					false);
		} else {
			for (int matchI = 0; matchI < getMatchCount (); matchI++) {
				Match m = getMatch (matchI);
				m.lcb = ((Integer) originalMatchLcbs.get (matchI)).intValue ();
			}
			Vector del_vec = new Vector ();
			visibleLcbList = LCBlist.filterLCBs (visibleLcbList, this, del_vec,
					true);

			// TODO: This check indicates some misdesign of LCBlist.filterLCBs.
			if (!del_vec.isEmpty ()) {
				delLcbList = ((LCB []) del_vec.elementAt (0));
			}

			// Reapply the current color scheme.
			// TODO: This is a temporary fix, since it breaks some encapsulation
			// it deals with staleness problem when set of LCBs changes.
			if (getColorScheme () instanceof LCBColorScheme) {
				setColorScheme (new LCBColorScheme ());
			} else {
				getColorScheme ().apply (this);
			}
		}

		// Since the LCB lists have been replaced, we need to make sure that
		// the reference genome is correct.
		referenceUpdated ();

		fireWeightEvent ();

	}

	/**
	 * Finds all LCBs that intersect the specified coordinate range in the
	 * current view
	 * 
	 * @param start_coord
	 *            The first coordinate of the intersection range
	 * @param end_coord
	 *            The last coordinate of the intersection range
	 * @param lcbs
	 *            An int array of the intersecting LCB indices
	 */
	public List getLCBRange (Genome g, long start_coord, long end_coord) {
		List list = new LinkedList ();
		for (int lcbI = 0; lcbI < getVisibleLcbCount (); lcbI++) {
			LCB lcb = getVisibleLcb (lcbI);
			long lend = lcb.getLeftEnd (g);
			long rend = lcb.getRightEnd (g);
			// check for intersection
			if (rend >= start_coord && lend <= end_coord)
				list.add (lcb);
		}
		return list;
	}

	public void setMinLCBWeight (long lcb_minimum_weight) {
		this.minimumLCBWeight = lcb_minimum_weight;
	}

	public long getMinLCBWeight () {
		return minimumLCBWeight;
	}

	public Vector getLcbChangePoints () {
		return lcb_change_points;
	}

	public void setLcbChangePoints (Vector v) {
		this.lcb_change_points = v;
	}

	public void sanityCheck () {
		for (int i = 0; i < getVisibleLcbCount (); i++) {
			LCB lcb = getVisibleLcb (i);

			if (lcb.id != LCBlist.REMOVED) {
				if (lcb.id != i) {
					throw new Error ("LCB with incorrect id.  Expected " + i
							+ " but was " + lcb.id);
				}

				for (int j = 0; j < getSequenceCount (); j++) {
					Genome g = getGenomeBySourceIndex (j);

					if (lcb.getLeftAdjacency (g) != LCBlist.ENDPOINT
							&& lcb.getLeftAdjacency (g) != LCBlist.REMOVED) {
						LCB left = getVisibleLcb (lcb.getLeftAdjacency (g));
						if (lcb.id != left.getRightAdjacency (g)) {
							throw new Error ("Right adjacency error.");
						}
					}

					if (lcb.getRightAdjacency (g) != LCBlist.ENDPOINT
							&& lcb.getRightAdjacency (g) != LCBlist.REMOVED) {
						LCB right = getVisibleLcb (lcb.getRightAdjacency (g));
						if (lcb.id != right.getLeftAdjacency (g)) {
							throw new Error ("Left adjacency error.");
						}
					}
				}
			}
		}

		for (int i = 0; i < getMatchCount (); i++) {
			Match m = getMatch (i);
			if (m.lcb >= getVisibleLcbCount ()) {
				throw new Error ("Match reference error.");
			}
		}
	}

	public void addMatch (Match m) {
		super.addMatch (m);
		originalMatchLcbs.add (new Integer (m.lcb));
	}

	public void setDrawLcbBounds (boolean value) {
		if (value != drawLCBbounds) {
			drawLCBbounds = value;
			fireDrawingSettingsEvent ();
		}
	}

	public boolean getDrawLcbBounds () {
		return drawLCBbounds;
	}

	public void setFillLcbBoxes (boolean value) {
		if (value != fillLCBboxes) {
			fillLCBboxes = value;
			fireDrawingSettingsEvent ();
		}
	}

	public boolean getFillLcbBoxes () {
		return fillLCBboxes;
	}

	public void initModelLCBs () {
		nway_lcb_list = LCBlist.isNwayLcbList (getFullLcbList (), this);
		LCBlist.computeLCBAdjacencies (getFullLcbList (), this);

		// find minimum weight
		long lcb_minimum_weight = -1;
		for (int lcbI = 0; lcbI < getFullLcbList ().length; lcbI++) {
			if (getFullLcbList ()[lcbI].weight < lcb_minimum_weight
					|| lcb_minimum_weight == -1)
				lcb_minimum_weight = getFullLcbList ()[lcbI].weight;
		}

		setVisibleLcbList (new LCB [getFullLcbList ().length]);

		// Copy all of the data in the LCB list, in order to determine
		// the change points without breaking everything else.
		LCB [] tmp_lcb_list = new LCB [getFullLcbList ().length];
		for (int lcbI = 0; lcbI < getFullLcbList ().length; lcbI++) {
			setVisibleLcb (lcbI, new LCB (getFullLcbList ()[lcbI]));
			tmp_lcb_list[lcbI] = new LCB (getFullLcbList ()[lcbI]);
		}
		Vector lcb_change_points = new Vector ();
		if (nway_lcb_list) {
			try {
				LCBlist.greedyBreakpointElimination (Long.MAX_VALUE,
						tmp_lcb_list, lcb_change_points, this);
				LCBlist.filterLCBs (tmp_lcb_list, this, null, false);
			} catch (Error e) {
				lcb_minimum_weight = 0;
				lcb_change_points.addElement (new Integer (0));
				lcb_change_points.addElement (new Integer (0));
			}
			setLcbChangePoints (lcb_change_points);
		}
		setMinLCBWeight (lcb_minimum_weight);
	}

	/**
	 * Uses currently visible LCBs to launch a DCJ window
	 */
/*	public void launchDCJ () {
		String lcb_input = "";
s
		// first construct a matrix of chromosome lengths
		int max_chr_count = 0;
		for (int seqI = 0; seqI < getSequenceCount (); seqI++) {
			int cur_count = this.getGenomeByViewingIndex (seqI)
					.getChromosomes ().size ();
			max_chr_count = cur_count > max_chr_count ? cur_count
					: max_chr_count;
		}
		long [][] chr_lens = new long [getSequenceCount ()] [max_chr_count];
		for (int seqI = 0; seqI < getSequenceCount (); seqI++) {
			List chromo = this.getGenomeByViewingIndex (seqI).getChromosomes ();
			for (int chrI = 0; chrI < chromo.size (); chrI++) {
				chr_lens[seqI][chrI] = ((Chromosome) chromo.get (chrI))
						.getEnd ();
			}
		}

		for (int seqI = 0; seqI < getSequenceCount (); seqI++) {
			Genome g = this.getGenomeByViewingIndex (seqI);
			int leftmost_lcb = 0;
			for (; leftmost_lcb < visibleLcbList.length; leftmost_lcb++)
				if (visibleLcbList[leftmost_lcb].getLeftAdjacency (g) == LCBlist.ENDPOINT)
					break;

			int adjI = leftmost_lcb;
			int cur_chromosome = 0;
			while (adjI != LCBlist.ENDPOINT && adjI != LCBlist.REMOVED
					&& adjI < visibleLcbList.length) {
				if (visibleLcbList[adjI].getLeftEnd (g) > chr_lens[seqI][cur_chromosome]) {
					lcb_input += " $ ";
					cur_chromosome++;
				} else if (adjI != leftmost_lcb)
					lcb_input += " ";
				if (visibleLcbList[adjI].getReverse (g))
					lcb_input += "-";
				lcb_input += adjI + 1;
				adjI = visibleLcbList[adjI].getRightAdjacency (g);
			}
			if (seqI + 1 < getSequenceCount ())
				lcb_input += " $,\n";
		}
		System.err.print(lcb_input);
		if (this instanceof XmfaViewerModel){
			lcb_input = PermutationExporter.getPermStrings((XmfaViewerModel) this,  genomes);			
		}
		System.err.print(lcb_input);
		org.gel.mauve.dcj.DCJWindow.startDCJ (lcb_input);
	}
*/
	
	
	public void launchGrimmMGR () {
		String grimm_url = "http://nbcr.sdsc.edu/GRIMM/grimm.cgi?";
		String url_data = "ismult=1&ngenomewins=" + getSequenceCount ();
		url_data += "&signedperm=signed&action=run";
		String [] per_genome = new String [getSequenceCount ()];

		// first construct a matrix of chromosome lengths
		int max_chr_count = 0;
		for (int seqI = 0; seqI < getSequenceCount (); seqI++) {
			int cur_count = this.getGenomeByViewingIndex (seqI)
					.getChromosomes ().size ();
			max_chr_count = cur_count > max_chr_count ? cur_count
					: max_chr_count;
		}
		long [][] chr_lens = new long [getSequenceCount ()] [max_chr_count];
		for (int seqI = 0; seqI < getSequenceCount (); seqI++) {
			List chromo = this.getGenomeByViewingIndex (seqI).getChromosomes ();
			for (int chrI = 0; chrI < chromo.size (); chrI++) {
				chr_lens[seqI][chrI] = ((Chromosome) chromo.get (chrI))
						.getEnd ();
			}
		}

		boolean single_chromosome = true;
		boolean all_circular = true;
		for (int seqI = 0; seqI < getSequenceCount (); seqI++) {
			url_data += "&genome" + (seqI + 1) + "=";
			per_genome[seqI] = "";
			Genome g = this.getGenomeByViewingIndex (seqI);
			List chromo = g.getChromosomes ();
			int leftmost_lcb = 0;
			for (; leftmost_lcb < visibleLcbList.length; leftmost_lcb++)
				if (visibleLcbList[leftmost_lcb].getLeftAdjacency (g) == LCBlist.ENDPOINT)
					break;

			int adjI = leftmost_lcb;
			int cur_chromosome = 0;
			all_circular = all_circular
					&& ((Chromosome) chromo.get (cur_chromosome))
							.getCircular ();
			while (adjI != LCBlist.ENDPOINT && adjI != LCBlist.REMOVED
					&& adjI < visibleLcbList.length) {
				if (visibleLcbList[adjI].getLeftEnd (g) > chr_lens[seqI][cur_chromosome]) {
					url_data += "%20$%20";
					per_genome[seqI] += " $ ";
					cur_chromosome++;
					single_chromosome = false;
					all_circular = all_circular
							&& ((Chromosome) chromo.get (cur_chromosome))
									.getCircular ();
				} else if (adjI != leftmost_lcb) {
					url_data += "%20";
					per_genome[seqI] += " ";
				}
				if (visibleLcbList[adjI].getReverse (g)) {
					url_data += "-";
					per_genome[seqI] += "-";
				}
				url_data += adjI + 1;
				per_genome[seqI] += adjI + 1;
				adjI = visibleLcbList[adjI].getRightAdjacency (g);
			}
			url_data += "%20$%20";
			per_genome[seqI] += " $ ";
		}

		if (single_chromosome && all_circular)
			url_data += "&nchromosomes=circular";
		else
			url_data += "&nchromosomes=multichromosomal";

		System.out.println ("Launching GRIMM with URL:\n" + grimm_url
				+ url_data);
		if (url_data.length () < 2000) {
			try {
				BrowserLauncher.openURL (grimm_url + url_data);
			} catch (IOException ioe) {
				System.err.println ("Error launching GRIMM with URL...");
			}
		} else {
			try {
				File tmp_form = File.createTempFile ("grimm", ".htm");
				FileWriter fw = new FileWriter (tmp_form);
				BufferedWriter bw = new BufferedWriter (fw);
				bw.write ("<html><body>\n");
				bw.write ("Click below to run the GRIMM/MGR analysis!<br>\n");
				bw
						.write ("<form method=POST action=\"http://nbcr.sdsc.edu/GRIMM/grimm.cgi\">\n");
				bw
						.write ("<input type=\"hidden\" name=\"ismult\" value=\"1\">\n");
				bw
						.write ("<input type=\"hidden\" name=\"ngenomewins\" value=\""
								+ getSequenceCount () + "\">\n");
				for (int seqI = 0; seqI < getSequenceCount (); seqI++) {
					bw.write ("<input type=\"hidden\" name=\"genome"
							+ (seqI + 1) + "\" value=\"" + per_genome[seqI]
							+ "\">\n");
				}
				if (single_chromosome && all_circular)
					bw
							.write ("<input type=\"hidden\" name=\"nchromosomes\" value=\"circular\">\n");
				else
					bw
							.write ("<input type=\"hidden\" name=\"nchromosomes\" value=\"multichromosomal\">\n");
				bw
						.write ("<input type=\"hidden\" name=\"signedperm\" value=\"signed\">\n");
				bw
						.write ("<input type=\"submit\" name=\"action\" value=\"run\"");
				bw.write ("</form>\n</body>\n</html>");
				bw.close ();
				BrowserLauncher.openURL ("file://"
						+ tmp_form.getCanonicalPath ());
			} catch (IOException ioe) {
				ioe.printStackTrace ();
			}
		}
	}
}
