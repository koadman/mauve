package org.gel.mauve;

import java.util.Arrays;
import java.util.Stack;
import java.util.Vector;

/**
 * Class implementing manipulation and analysis functions for LCBs
 */
public class LCBlist {

	public static final int ENDPOINT = -1;
	public static final int REMOVED = -2;

	public static boolean isNwayLcbList (LCB [] lcb_list, BaseViewerModel model) {
		int lcbI = 0;
		for (; lcbI < lcb_list.length; ++lcbI) {
			int gI = 0;
			for (; gI < model.getSequenceCount (); gI++) {
				Genome g = model.getGenomeBySourceIndex (gI);
				if (lcb_list[lcbI].getLeftEnd (g) == 0)
					break;
			}
			if (gI < model.getSequenceCount ())
				break;
		}
		return lcbI == lcb_list.length;
	}

	/**
	 * Links together adjacent LCBs in an array of LCBs. Links are stored in the
	 * left_adjacency[] and right_adjacency[] arrays as the index of the
	 * adjacent LCB in lcb_list when lcb_list is sorted on sequence 0. Redesign
	 * to be more intuitive. left_adjacency is always left, regardless of LCB
	 * orientation
	 */
	public static void computeLCBAdjacencies (LCB [] lcb_list,
			BaseViewerModel model) {
		if (lcb_list.length == 0)
			return; // there aren't any LCBs so there aren't any adjacencies!

		int lcbI;
		int seqI;
		for (lcbI = 0; lcbI < lcb_list.length; lcbI++) {
			lcb_list[lcbI].resetAdjacencies (model.getSequenceCount ());
			lcb_list[lcbI].id = lcbI;
		}

        for (seqI = 0; seqI < model.getSequenceCount(); seqI++)
        {
            Genome g = model.getGenomeBySourceIndex(seqI);
            
            LCBLeftComparator llc = new LCBLeftComparator(g);
            Arrays.sort(lcb_list, llc);
            int first = 0;
            for( ; first < lcb_list.length; first++ )
            	if(lcb_list[first].getLeftEnd(g) != 0)
            		break;
            int last = lcb_list.length;
            for( ; last > 0; last-- )
            	if(lcb_list[last-1].getLeftEnd(g) != 0)
            		break;

            for (lcbI = first + 1; lcbI + 1 < last; lcbI++)
            {
                lcb_list[lcbI].setLeftAdjacency(g, lcb_list[lcbI - 1].id);
                lcb_list[lcbI].setRightAdjacency(g, lcb_list[lcbI + 1].id);
            }
            if (lcbI == lcb_list.length)
                lcbI--; // need to decrement when there is only a single LCB

            // set first and last lcb adjacencies to ENDPOINT
            if(first < lcb_list.length)
            	lcb_list[first].setLeftAdjacency(g, ENDPOINT);
            if(last > 0)
            	lcb_list[last-1].setRightAdjacency(g, ENDPOINT);
            if (first < last-1)
            {
                lcb_list[first].setRightAdjacency(g, lcb_list[first+1].id);
                lcb_list[last-1].setLeftAdjacency(g, lcb_list[last-2].id);
            }
        }

		// sort back by ID
		LcbIdComparator lic = new LcbIdComparator ();
		Arrays.sort (lcb_list, lic);
	}

	/**
	 * @param targetMinWeight
	 * @param fullLCBList
	 * @param change_points -
	 *            minimum weights that, if applied, cause a change in the list
	 *            of LCBs.
	 * 
	 * Repeatedly removes the lowest weight LCB from the LCB list until all
	 * remaining LCBs meet the minimum weight criteria. Call filterLCBs() after
	 * calling this function to arrive at a final set of LCBs that meet the
	 * minimum weight.
	 */
	public static void greedyBreakpointElimination (long targetMinWeight,
			LCB [] fullLCBList, Vector change_points, LcbViewerModel model) {
		if (fullLCBList.length == 0)
			return;

		// repeatedly remove the low weight LCBs until the minimum weight
		// criteria is satisfied
		int lcbI = 0;
		long currentMinWeight = 0;
		long previousMinWeight = 0;
		int min_lcb = 0;
		int lcb_count = fullLCBList.length;

		while (currentMinWeight < targetMinWeight) {
			// Loop through all LCBs, looking for lowest-weight LCB that has not
			// either been eliminated, or that
			currentMinWeight = 0;
			for (lcbI = 0; lcbI < fullLCBList.length; lcbI++) {
				LCB lcb = fullLCBList[lcbI];
				if (lcb.id == lcbI && lcb.keep == false) {
					if (lcb.weight < currentMinWeight || currentMinWeight == 0) {
						currentMinWeight = lcb.weight;
						min_lcb = lcbI;
					}
				}
			}
			lcbI = min_lcb;

			// If we are saving change points, save it.
			if (change_points != null && previousMinWeight != currentMinWeight) {
				change_points.addElement (new Integer ((int) currentMinWeight));
			}

			// if only a single LCB remains, don't remove it
			// and if we've exceeded the target, don't remove it.
			if (lcb_count == 1 || currentMinWeight >= targetMinWeight) {
				break;
			}

			previousMinWeight = currentMinWeight;

			// remove this LCB
			fullLCBList[lcbI].id = REMOVED;

			// update adjacencies
			for (int seqI = 0; seqI < model.getSequenceCount (); seqI++) {
				Genome g = model.getGenomeBySourceIndex (seqI);

				int left_adj = fullLCBList[lcbI].getLeftAdjacency (g);
				int right_adj = fullLCBList[lcbI].getRightAdjacency (g);

				// Do a bunch of sanity-checking.
				if (left_adj == REMOVED || right_adj == REMOVED) {
					throw new Error ("Improper linking in LCB list.");
				}
				// Check that the left-adjacent LCB for the right-adjacent
				// neighbor
				// of the current LCB is the current LCB, and vice-versa.
				if (left_adj != ENDPOINT
						&& fullLCBList[left_adj].getRightAdjacency (g) != lcbI) {
					throw new Error ("Inconsistency in LCB list.");
				}
				if (right_adj != ENDPOINT
						&& fullLCBList[right_adj].getLeftAdjacency (g) != lcbI) {
					throw new Error ("Inconsistency in LCB list.");
				}
				if (right_adj >= fullLCBList.length) {
					throw new Error ("Right adjacency id outside valid range.");
				}

				// Update the doubly-linked list in both directions.
				if (left_adj != ENDPOINT) {
					fullLCBList[left_adj].setRightAdjacency (g, right_adj);
				}
				if (right_adj != ENDPOINT) {
					fullLCBList[right_adj].setLeftAdjacency (g, left_adj);
				}

			}
			// just deleted an lcb, drop the lcb count
			lcb_count--;

			// check for collapse
			for (int seqI = 0; seqI < model.getSequenceCount (); seqI++) {
				Genome g = model.getGenomeBySourceIndex (seqI);

				int left_adj = fullLCBList[lcbI].getLeftAdjacency (g);
				int right_adj = fullLCBList[lcbI].getRightAdjacency (g);
				if (right_adj == fullLCBList.length) {
					throw new RuntimeException ("Unexpected error.");
				}

				if (left_adj == ENDPOINT || right_adj == ENDPOINT) {
					continue; // can't collapse with a non-existant LCB!
				}

				// check whether this LCB has already been merged
				if (left_adj != fullLCBList[left_adj].id
						|| right_adj != fullLCBList[right_adj].id) {
					// because adjacency pointers are always updated to point to
					// the
					// representative entry of an LCB, the lcb_id and the array
					// index
					// should always be identical.
					throw new RuntimeException ("Improper Linking");
				}

				if (left_adj == REMOVED || right_adj == REMOVED) {
					throw new RuntimeException ("Improper Linking");
				}

				// check whether the two LCBs are adjacent in each sequence
				boolean orientation = !fullLCBList[left_adj].getReverse (g);
				int seqJ;
				for (seqJ = 0; seqJ < model.getSequenceCount (); seqJ++) {
					Genome g2 = model.getGenomeBySourceIndex (seqJ);

					boolean j_orientation = !fullLCBList[left_adj]
							.getReverse (g2);
					if (j_orientation == orientation
							&& fullLCBList[left_adj].getRightAdjacency (g2) != right_adj)
						break;
					if (j_orientation != orientation
							&& fullLCBList[left_adj].getLeftAdjacency (g2) != right_adj)
						break;
					// check that they are both in the same orientation
					if ((!fullLCBList[right_adj].getReverse (g2)) != j_orientation)
						break;
				}

				if (seqJ != model.getSequenceCount ())
					continue;

				// these two can be collapsed. repeatedly search the intervening
				// region

				fullLCBList[right_adj].id = left_adj;
				if (fullLCBList[right_adj].id == ENDPOINT
						|| fullLCBList[right_adj].id == REMOVED) {
					// TODO: Is there some sort of proactive validation that can
					// be done instead?
					throw new RuntimeException ("Corrupt LCB list.");
				}

				fullLCBList[left_adj].weight += fullLCBList[right_adj].weight;
				// unlink right_adj from the adjacency list and
				// update left and right ends of left_adj
				for (seqJ = 0; seqJ < model.getSequenceCount (); seqJ++) {
					Genome g2 = model.getGenomeBySourceIndex (seqJ);

					boolean j_orientation = !fullLCBList[left_adj]
							.getReverse (g2);
					int rr_adj = fullLCBList[right_adj].getRightAdjacency (g2);
					int rl_adj = fullLCBList[right_adj].getLeftAdjacency (g2);
					if (j_orientation == orientation) {
						fullLCBList[left_adj].setRightEnd (g2,
								fullLCBList[right_adj].getRightEnd (g2));
						fullLCBList[left_adj].setRightAdjacency (g2, rr_adj);
						if (rr_adj == fullLCBList.length) {
							// TODO: Is there some sort of proactive validation
							// that can be done instead?
							throw new RuntimeException ("Corrupt LCB list.");
						}
						if (rr_adj != ENDPOINT) {
							fullLCBList[rr_adj].setLeftAdjacency (g2, left_adj);
						}
					} else {
						fullLCBList[left_adj].setLeftEnd (g2,
								fullLCBList[right_adj].getLeftEnd (g2));
						fullLCBList[left_adj].setLeftAdjacency (g2, rl_adj);
						if (rl_adj == fullLCBList.length) {
							// TODO: Is there some sort of proactive validation
							// that can be done instead?
							throw new RuntimeException ("Corrupt LCB list.");
						}
						if (rl_adj != ENDPOINT)
							fullLCBList[rl_adj]
									.setRightAdjacency (g2, left_adj);
					}
					// update lcbI's adjacency links to point nowhere
					if (fullLCBList[lcbI].getLeftAdjacency (g2) == right_adj)
						fullLCBList[lcbI].setLeftAdjacency (g2, left_adj);
					if (fullLCBList[lcbI].getRightAdjacency (g2) == right_adj)
						fullLCBList[lcbI].setRightAdjacency (g2, left_adj);
				}

				// just collapsed an lcb, decrement lcb_count
				lcb_count--;
			}
		}

		if (change_points != null) {
			change_points.addElement (new Integer ((int) currentMinWeight));
		}

	}

	/**
	 * Computes the set of remaining LCBs after greedy breakpoint elimination.
	 * ALWAYS call this function immediately after calling
	 * greedyBreakpointElimination() Note: this code assumes that matches and
	 * LCBs are sorted on their coordinates in the first genome sequence
	 */
	public static LCB [] filterLCBs (LCB [] interimList, BaseViewerModel model,
			Vector removed_lcbs, boolean updateIDsAndColors) {
		if (interimList.length == 0)
			return interimList;

		// The interimList arrives having been munged by
		// greedyBreakpointElimination.
		// The rest of the program expects that the lcb_id and array index of an
		// LCB
		// will be identical. The interimList instead provides a list with
		// lcb.id's that
		// are set to the id of another LCB with which it should be
		// consolidated.
		// Any LCB with an lcb.id that matches its array position is preserved;
		// any that
		// have an lcb.id of REMOVED will cause referring LCBs to also be
		// removed.
		for (int lcbIndex = 0; lcbIndex < interimList.length; lcbIndex++) {
			// search and update the union/find structure for the target
			LCB lcb = interimList[lcbIndex];
			int currentID = lcb.id;

			if (currentID != ENDPOINT && currentID != REMOVED
					&& interimList[currentID].id != currentID) {
				Stack visited = new Stack ();
				visited.push (new Integer (lcbIndex));
				while (currentID != ENDPOINT && currentID != REMOVED
						&& interimList[currentID].id != currentID) {
					visited.push (new Integer (currentID));
					currentID = interimList[currentID].id;
				}

				while (visited.size () > 0) {
					int index = ((Integer) visited.pop ()).intValue ();
					interimList[index].id = currentID;
				}
			}
		}

		// update lcb_ids and colors for the matches
		if (updateIDsAndColors) {
			for (int matchI = 0; matchI < model.getMatchCount (); matchI++) {
				Match m = model.getMatch (matchI);
				m.lcb = interimList[m.lcb].id;
				if (m.lcb >= 0) {
					m.color = interimList[m.lcb].match_color;
				}
			}
		}

		// leave only the remaining LCBs
		int remaining_count = 0;
		for (int lcbI = 0; lcbI < interimList.length; lcbI++) {
			if (interimList[lcbI].id == lcbI)
				remaining_count++;
		}
		LCB [] remainders = new LCB [remaining_count];
		remaining_count = 0;
		for (int lcbI = 0; lcbI < interimList.length; lcbI++) {
			if (interimList[lcbI].id == lcbI) {
				remainders[remaining_count] = interimList[lcbI];
				remaining_count++;
			}
		}

		// update the lcb adjacencies!!
		computeLCBAdjacencies (remainders, model);

		// if we're supposed to return the removed LCBs then go get them
		if (removed_lcbs != null) {
			// count the removed LCBs
			int removed_count = 0;
			for (int lcbI = 0; lcbI < interimList.length; lcbI++)
				if (interimList[lcbI].id == REMOVED)
					removed_count++;
			// go get the removed LCBs
			LCB [] removed = new LCB [removed_count];
			int removedI = 0;
			for (int lcbI = 0; lcbI < interimList.length; lcbI++) {
				if (interimList[lcbI].id == REMOVED)
					removed[removedI++] = interimList[lcbI];
			}
			computeLCBAdjacencies (removed, model);
			removed_lcbs.add (removed);
		}

		// Now we need to flatten the LCB references within matches.
		if (updateIDsAndColors) {
			int remainderIndex = -1;
			int currentMatchIndex = -1;
			for (int i = 0; i < model.getMatchCount (); i++) {
				Match m = model.getMatch (i);
				if (m.lcb != LCBlist.ENDPOINT && m.lcb != LCBlist.REMOVED) {
					if (m.lcb != currentMatchIndex) {
						remainderIndex++;
						currentMatchIndex = m.lcb;
					}
					m.lcb = remainders[remainderIndex].id;
				}
			}
		}

		return remainders;
	}

}