package org.gel.mauve.contigs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import org.gel.mauve.Chromosome;
import org.gel.mauve.LCB;
import org.gel.mauve.LcbViewerModel;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.MauveHelperFunctions;


public class ContigInverter implements MauveConstants {
	
	protected LcbViewerModel model;
	protected ContigReorderer central;
	protected double min_length_ratio;
	protected ContigGrouper grouper;
	protected HashSet groups;
	protected Hashtable befores;
	protected Hashtable afters;
	
	public ContigInverter (LcbViewerModel mod, ContigReorderer reorder) {
		init (mod, reorder);
		reorderContigs ();
	}
	
	public ContigInverter (LcbViewerModel mod, ContigReorderer reorder, String file) {
		System.out.println ("reading order file");
		init (mod, reorder);
		central.readOrdered (file);
		//invertContigs ();
		/*placeConflicts ();
		groups.clear ();
		matchEdges ();*/
	}
	
	protected void init (LcbViewerModel mod, ContigReorderer reorder) {
		model = mod;
		central = reorder;
		befores = new Hashtable ();
		afters = new Hashtable ();
		groups = new HashSet ();
		grouper = central.grouper;
	}
	
	public void placeUntouched () {
		long multiplicity = 0;
		for (int i = 0; i < model.getSequenceCount (); i++) {
			multiplicity <<= 1;
			if (i == central.fix.getSourceIndex () || i == central.ref.getSourceIndex ())
				multiplicity |= 1;
		}
		outer: for (int i = 0; i < central.lcbs.length; i++) {
			//System.out.println ("lcb: " + central.lcbs [i]);
			if (central.lcbs [i].multiplicityType () == multiplicity) {
				ContigGrouper.ContigGroup group = grouper.getContigGroup (central.lcbs [i]);
				/*Iterator itty = group.getLCBs ().iterator ();
				while (itty.hasNext ()) {
					if (((LCB) itty.next ()).multiplicityType () != multiplicity)
						continue outer;
				}*/
				Iterator itty = group.getNonEmpty ().iterator ();
				while (itty.hasNext ()) {
					if (central.ordered.contains (itty.next ()))
						continue outer;
				}
				//System.out.println ("past mult");
				int ind = 0;
				int ind2 = central.ordered.size () + 1;
				if (locationUnique (group, i)) {
					if (i > 0)
						ind = central.ordered.indexOf (grouper.getContigGroup (
								central.lcbs [i - 1]).end);
					if (i < central.lcbs.length - 1)
						ind2 =  central.ordered.indexOf (grouper.getContigGroup (
								central.lcbs [i + 1]).start);
					boolean ok = ind2 - ind == 1;
					addContigGroup (group, ok, ind2);
					if (group.isReversed ())
						central.addGroupToInverters (group);
				}
			}
		}
	}
	
	//method no longer necessary, does as ordering
	public void invertContigs () {
		LCB [] ordered_lcbs = central.fix_lcbs;
		for (int i = 0; i < ordered_lcbs.length; i++) {
			if (ordered_lcbs [i].getLeftEnd (central.ref) != 0 && 
					ordered_lcbs [i].getLeftEnd (central.fix) != 0) {
				ContigGrouper.ContigGroup group = grouper.getContigGroup (ordered_lcbs [i]);
				if (!groups.contains (group.toString ()) && group.isReversed ()) {
					if (group.start != group.end) {
						System.out.println ("LCB spans reversible contig");
						System.out.println ("chrom1: " + group.start);
						System.out.println ("chrom2: " + group.end);
					}
					central.addGroupToInverters (group);
					groups.add (group.toString ());
					while (i < ordered_lcbs.length && ordered_lcbs [i] != group.last)
						i++;
				}
			}
		}
	}
	
	public boolean contigReversed (Chromosome contig, LCB lcb) {
		ContigGrouper.ContigGroup group = grouper.getContigGroup (lcb);
		return group.isReversed ();
	}
	
	public void reorderContigs () {
		int lcb_index = 0;
		for (; lcb_index < central.lcbs.length; lcb_index++) {
			if (central.lcbs [lcb_index].getLeftEnd(central.fix) != 0) {
				orderContigGroup (lcb_index);
			}
		}
		groups.clear ();
		placeConflicts ();
		groups.clear ();
		matchEdges ();
	}

	public void matchEdges () {
		System.out.println ("matching edges");
		HashSet l_matched = new HashSet ();
		HashSet r_matched = new HashSet ();
		ContigGrouper.ContigGroup group1 = null;
		ContigGrouper.ContigGroup group2 = null;
		boolean reversed1 = false;
		boolean reversed2 = false;
		boolean last = true;
		boolean first = true;
		for (int gen = 0; gen < central.ordered_genomes.length; gen++) {
			//System.out.println ("matching genome: " + gen);
			central.setReference(central.ordered_genomes [gen]);
			for (int i = 0; i < central.lcbs.length - 1; i++) {
				group1 = grouper.getContigGroup (central.lcbs [i]);
				//System.out.println ("match: " + group1.toString ());
				do {
					first = true;
					reversed1 = central.isReversed (last ? group1.last : group1.first);
					//System.out.println ("last: " + last + " rev1: " + reversed1);
					if (last != reversed1 && !(last ? r_matched.contains (group1.last) :
						l_matched.contains (group1.first)) && group1.matchedToEdge (reversed1)
						&& central.containsEndOfLCB (group1, reversed1)) {
						do {
							group2 = grouper.getContigGroup (central.lcbs [i + 1]);
							//System.out.println ("i: " + group2);
							reversed2 = central.isReversed (first ? group2.first : group2.last);
							//System.out.println ("first: " + first + " rev2: " + reversed2);
							if ((first == reversed2) || (first ? l_matched.contains (group2.first)
									: r_matched.contains (group2.last)))
								group2 = group1;
							if (group2.start != group1.start && group2.matchedToEdge(!reversed2) &&
									central.containsEndOfLCB (group2, !reversed2) &&
									central.lcbs [i] == (reversed1 ?  group1.first : group1.last) &&
									central.lcbs [i + 1] == (reversed2 ?  group2.last : group2.first) &&
									central.lcbs [i + 1].getLeftEnd(central.ref) - 
									central.lcbs [i].getRightEnd(central.ref) < 
									ContigReorderer.MAX_IGNORABLE_DIST * 2) {
								int ind = central.ordered.indexOf (reversed2 ? group2.end :
									group2.start);
								int ind2 = central.ordered.indexOf (group1.isReversed () ? 
										group1.start : group1.end);
								//System.out.println ("ind1: " + ind +" ind2:"+ ind2);
								if (ind != 0 && ind2 != 0) {
									if (last)
										r_matched.add (group1.last); 
									else
										l_matched.add (group1.first);
									if (first)
										l_matched.add (group2.first);
									else
										r_matched.add (group2.last);
									ind = ind - ind2;
									//System.out.println ("i: " + i);
									boolean after = group1.weight > group2.weight;
									if (after)
										group2.weight = group1.weight;
									else
										group1.weight = group2.weight;
									//System.out.println ("g1: " + group1.weight + " gr2: " + group2.weight);
									System.out.println ("new group: " + central.lcbs [i].getRightEnd(central.fix) + ", " +
											central.lcbs [i + 1].getLeftEnd(central.fix) + " " + after);
									System.out.println ("rev1: " + reversed1 + "rev2: " + reversed2);
									boolean leader = group1.getLength () > group2.getLength ();
									if (reversed1 != reversed2) {
										if (group1.isReversed () == group2.isReversed ()) {
											System.out.println ("one longer: " + leader);
											if (leader)
												setReversed (group2, !group2.isReversed ());
											else {
												setReversed (group1, !group1.isReversed ());
												reversed1 = !reversed1;
											}
										}
									}
									else if (group1.isReversed () != group2.isReversed ()) {
										boolean rev = leader && group1.isReversed () || !leader &&
										group2.isReversed ();
										ContigGrouper.ContigGroup change = leader ? group2 : group1;
										if (!rev) {
											//System.out.println ("not reversed");
											reversed1 = false;
										}
										setReversed (change, rev);
									}
									if (reversed1 && last || !reversed1 && !last) {
										//System.out.println ("reversed");
										ContigGrouper.ContigGroup temp = group1;
										group1 = group2;
										group2 = temp;
										after = !after;
										ind = -ind;
									}
									afters.put (group1.toString (), group2);
									befores.put (group2.toString (), group1);
									if (ind != 1) {
										ContigGrouper.ContigGroup move = (ContigGrouper.ContigGroup) 
										(after ? afters.get (group2.toString()) : 
											befores.get(group1.toString()));
										removeContigGroup (after ? group2 : group1);
										putNextTo (group1, group2, after);
										if (!after)
											group2 = group1;
										while (move != null) {
											System.out.println ("new code: " + move);
											putNextTo (after ? group2 : move, after ? move : group2,
													after);
											group2 = move;
											move = (ContigGrouper.ContigGroup) 
											(after ? afters.get (group2.toString()) : 
												befores.get(group2.toString()));
										}
									}
								}
							}
							first = !first;
						} while (!first);
					}
					last = !last;
				} while (!last);
			}
		}
	}
	
	/*
	 * expects to be called from match edges - assumes will be in neither or only one
	 * of the two hashtables befores and afters
	 */
	private void setReversed (ContigGrouper.ContigGroup group, boolean reverse) {
		System.out.println ("setting: " + reverse + " was: " + group.isReversed () + " " +
				group.start.getName());
		if (group.isReversed () != reverse) {
			Hashtable from = null;
			Hashtable to = null;
			String key = group.toString ();
			if (befores.containsKey (key)) {
				from = befores;
				to = afters;
			}
			else if (afters.containsKey (key)) {
				from = afters;
				to = befores;
			}
			if (!reverse)
				central.removeGroupFromInverters (group);
			else
				central.addGroupToInverters (group);
			group.setReversed (reverse);
			if (from != null) {
				ContigGrouper.ContigGroup group2 = (ContigGrouper.ContigGroup) from.get (key);
				System.out.println ("accessing");
				to.put (key, group2);
				from.remove (key);
				key = group2.toString ();
				to.remove (key);
				setReversed (group2, !group2.isReversed ());
				from.put (key, group);
			}
		}
		else
			group.setReversed (reverse);
	}
	
	public void removeContigGroup (ContigGrouper.ContigGroup group) {
		Iterator itty = group.getNonEmpty ().iterator ();
		while (itty.hasNext ())
			central.ordered.remove (itty.next ());
	}
	
	public void placeConflicts () {
		System.out.println ("placing conflicts");
		int [] placements = new int [central.fix.getChromosomes ().size () + 1];
		LinkedList [] cur_lcbs = new LinkedList [placements.length];
		HashSet misplaced = new HashSet ();
		//System.out.println ("size: " + placements.length);
		int highest = 0;
		//System.out.println ("central: " + central.ordered);
		for (int i = 0; i < central.fix_lcbs.length; i++) {
			ContigGrouper.ContigGroup group = grouper.getContigGroup(central.fix_lcbs [i]);
			if (!groups.contains(group.toString ()) && !central.ordered.contains(group.start)) {
				Iterator itty = group.getLCBs().iterator();
				ContigGrouper.ContigGroup group2 = null;
				ContigGrouper.ContigGroup best_group = null;
				while (itty.hasNext ()) {
					LCB cur = (LCB) itty.next ();
					central.setReference(central.getClosestRelation (cur));
					int ind = Arrays.binarySearch(central.lcbs, cur, central.left_compare);
					int other = -1;
					while (other == -1 && ind > -1) {
						if (!misplaced.contains (central.lcbs [ind--])) {
							group2 = grouper.getContigGroup(central.lcbs [ind + 1]);
							other = central.ordered.indexOf(group2.isReversed() ? group2.end : 
								group2.start);
						}
					}
					placements [++other] += cur.weight;
					if (cur_lcbs [other] == null)
						cur_lcbs [other] = new LinkedList ();
					cur_lcbs [other].add (cur);
					misplaced.add (cur);
					if (placements [highest] < placements [other]) {
						highest = other;
						best_group = group2;
					}
				}
				Iterator placed = cur_lcbs [highest].iterator ();
				LCB best = null;
				while (placed.hasNext ()) {
					LCB cor = (LCB) placed.next();
					if (best == null || cor.weight > best.weight)
						best = cor;
				}
				//System.out.println ("not misplaced: " + best.getLeftEnd (central.fix));
				group.weight = best.weight;
				group.setReversed(central.isReversed(best));
				putNextTo (highest == 0 ? null : best_group, group, true);
				misplaced.remove (best);
				Arrays.fill(placements, 0);
				Arrays.fill (cur_lcbs, null);
				highest = 0;
			}
		}
	}
	
	public void putNextTo (ContigGrouper.ContigGroup one, ContigGrouper.ContigGroup two, boolean after) {
		int ind = -1;
		boolean ordered = true;
		if (one != null) {
			Chromosome prev = after ? (one.isReversed () ? one.start : one.end) :
				(two.isReversed () ? two.end : two.start);
			ind = central.ordered.indexOf (prev);
			if (ind == -1)
				ordered = false;
			if (after)
				ind++;
		}
		else
			ind++;
		//System.out.println ("next ind: " + ind);
		addContigGroup (after ? two : one, true, ind);
	}
	
	public void orderContigGroup (int lcb_index) {
		ContigGrouper.ContigGroup group = grouper.getContigGroup (central.lcbs [lcb_index]);
		if (!groups.contains (group.toString ())) {
			boolean ok = locationUnique (group, lcb_index);
			if (ok && !group.isReversed() && (group.first.getReverse (central.fix) ||
					group.last.getReverse(central.fix)) && group.last.getLeftEnd(
							central.ref) < group.first.getLeftEnd(central.ref))
				group.setReversed(true);
			addContigGroup (group, ok, central.ordered.size ());
		}
	}
	
	public void addContigGroup (ContigGrouper.ContigGroup group, boolean in_order, int index) {
		LinkedList contigs = group.getNonEmpty ();
		groups.add (group.toString ());
		boolean reversed = group.isReversed();
		if (reversed)
			central.addGroupToInverters (group);
		while (contigs.size () > 0) {
			Chromosome chrom = (Chromosome) (reversed ? contigs.removeLast () :
					contigs.removeFirst());
			if (in_order) {
				int ind2 = central.ordered.indexOf (chrom);
				if (ind2 > -1) {
					if (ind2 < index)
						index--;
					central.ordered.remove (ind2);
					//System.out.println ("ordered twice: " + ind2 + 
					//		" new " + index + " start: " + chrom);
					//System.out.println ("prev: " + (ind2 == 0 ? null : central.ordered.get (--ind2)));
				}
				//else
					//System.out.println ("ordering: " + chrom + " prev: " + 
					//		(index > 0 ? central.ordered.get (index - 1) : "first"));
				central.ordered.add(index++, chrom);
			}
			else
				MauveHelperFunctions.addChromByStart (central.conflicts, chrom);
		}
	}
	
	public boolean locationUnique (ContigGrouper.ContigGroup group, int lcb_index) {
		LCB current = group.first;
		boolean ok = true;
		HashSet grouped_lcbs = group.getLCBs ();
		int count = grouped_lcbs.size () - 1; 
		for (int i = count; i > 0 && lcb_index < central.lcbs.length - 1; i--) {
			if (!grouped_lcbs.remove (central.lcbs [++lcb_index])) {
				//System.out.println ("didn't find: " + lcbs [lcb_index].getLeftEnd (central.fix));
				ok = false;
			}
		}
		return ok;
		
	}
	

	
	
	

}
