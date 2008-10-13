package org.gel.mauve.contigs;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;
import org.gel.mauve.LCB;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.MauveAlignmentViewerModel;
import org.gel.mauve.ZoomHistogram;
import org.gel.mauve.analysis.SegmentComparator;
import org.gel.mauve.backbone.Backbone;
import org.gel.mauve.backbone.BackboneList;

public class ContigGrouper implements MauveConstants {
	
	protected ContigReorderer central;
	protected int min_edge_bp;
	protected int min_contig_bp;
	protected double min_percent;
	protected double min_contig_ratio;
	protected Genome fix;
	protected ZoomHistogram how_similar;
	protected Hashtable groups;
	protected byte min_similarity;
	protected byte med_similarity;
	protected SegmentComparator compare;
	protected int genome_ind;
	protected BackboneList bb;
	
	
	public ContigGrouper (ContigReorderer orderer, int bp, double percent) {
		central = orderer;
		fix = central.fix;
		genome_ind = fix.getSourceIndex ();
		groups = new Hashtable ();
		MauveAlignmentViewerModel model = (MauveAlignmentViewerModel) central.model;
		how_similar = model.getSim (fix);
		bb = model.getBackboneList ();
		min_edge_bp = bp;
		min_contig_bp = 200;
		min_percent = percent;
		min_similarity = 0;
		med_similarity = 42;
		min_contig_ratio = .10;
		compare = new SegmentComparator (genome_ind, false);
	}
	
	public Chromosome getFirstOfLinkedContigs (LCB lcb, boolean left) {
		return getFirstOfLinkedContigs (lcb, left, null);
	}
	
	public Chromosome getFirstOfLinkedContigs (LCB lcb, boolean left, ContigGroup group) {
		Chromosome start = getEndChromosome (lcb, left);
		groups.put (lcb, group);
		Chromosome start2 = null;
		LCB prior = lcb;
		do {
			lcb = central.getAdjacentLCB(left, lcb);
			if (lcb == null)
				break;
			start2 = getEndChromosome (lcb, !left);
			if (start2 == start) {
				groups.put (start, this);
				start = getEndChromosome (lcb, left);
				prior = lcb;
			}
			else
				break;
		} while (true);
		if (group != null) {
			if (left) {
				group.start = start;
				group.first = prior;
			}
			else {
				group.end = start;
				group.last = prior;
			}
		}
		return start;
	}
	
	public boolean onSingleContig (LCB lcb) {
		return getEndChromosome (lcb, true) == getEndChromosome (lcb, false); 
	}
	
	public Chromosome getEndChromosome (LCB lcb, boolean left) {
		long pos = left ? lcb.getLeftEnd (fix) : lcb.getRightEnd (fix) - 1;
		Chromosome chrom = fix.getChromosomeAt (pos);
		if (chrom == fix.getChromosomeAt (left ? lcb.getRightEnd (fix) - 1 : 
			lcb.getLeftEnd (fix)) || isSolidlyOnContig (lcb, chrom, left, pos))
			return chrom;
		else {
			lcb = central.getAdjacentLCB (left, lcb);
			if (lcb != null) {
				pos = left ? lcb.getRightEnd (fix) - 1 : lcb.getLeftEnd (fix);
				Chromosome chrom2 = fix.getChromosomeAt (pos);
				if (chrom2 == chrom /*&& isSolidlyOnContig (lcb, chrom, !left, pos)*/) {
					pos = left ? chrom.getEnd () + 1 : chrom.getStart () - 1;
					chrom = fix.getChromosomeAt (pos);
				}
			}
			return chrom;
		}
	}
	
	/*
	 * assumes lcb partially overlaps one side of the chromosome.  Since left refers to whether
	 * end is the left or right side, the lcb would overlap the right side of chrom if left is
	 * true, otherwise, the opposite is true.
	 */
	protected boolean isSolidlyOnContig (LCB lcb, Chromosome chrom, boolean left, long end) {
		long amount = left ? chrom.getEnd () - end + 1 : end - chrom.getStart () + 1;
		byte similarity = -1;
		if (left)
			similarity = how_similar.getValueForRange (end, chrom.getEnd ());
		else
			similarity = how_similar.getValueForRange (chrom.getStart (), end);
		if (amount < min_contig_bp && amount > min_edge_bp && similarity > med_similarity)
			return true;
		if (similarity < min_similarity || amount < min_edge_bp || 
				(amount / (double) chrom.getLength () < 
				min_contig_ratio && amount / ((double) lcb.getLength (fix)) < min_percent)) {
			if (!(similarity < min_similarity)) {
				if (amount > min_edge_bp)
				System.out.println ("not enought overlap: " + chrom);
			}
			/*else if (amount > min_edge_bp)
				System.out.println ("bad similarity: " + similarity + " " + chrom + "\n" + lcb.getRightEnd (fix));*/
			return false;
		}
		return true;
	}
	
	public boolean isSolidlyOnContig (LCB lcb, Chromosome chrom, boolean left) {
		boolean ret = isSolidlyOnContig (lcb, chrom, !left, left ? lcb.getRightEnd (fix) - 1 : 
			lcb.getLeftEnd (fix));
		//System.out.println ("solid: " + lcb.getLeftEnd (fix) + " " + ret);
		//System.out.println ("chrom: " + chrom);
		return ret;
	}
	
	public boolean onlyLCBOnContig (LCB lcb, Chromosome chrom) {
		boolean alone;
		LCB prev = central.getAdjacentLCB (true, lcb);
		LCB next = central.getAdjacentLCB (false, lcb);
		if ((prev != null && getEndChromosome (prev, false) == chrom) || 
				(next != null && getEndChromosome (next, true) == chrom))
			alone = false;
		else
			alone = true;
		//System.out.println ("alone: " + lcb.getLeftEnd (fix) + " " + alone);
		//System.out.println ("chrom: " + chrom);
		return alone;
	}
	
	public ContigGroup getContigGroup (LCB lcb) {
		ContigGroup group = (ContigGroup) groups.get (lcb);
		if (group != null)
			return group;
		else
			return new ContigGroup (lcb);
	}
	
	public class ContigGroup {

		protected Chromosome start;
		protected Chromosome end;
		protected LCB first;
		protected LCB last;
		protected long weight;
		protected Boolean reversed;
		//protected int num_lcbs;


		public ContigGroup (LCB lcb) {
			getFirstOfLinkedContigs (lcb, true, this);
			getFirstOfLinkedContigs (lcb, false, this);
			LinkedList list = getNonEmpty ();
			if (list.size() == 0)
				System.out.println ("size 0: " + lcb.getLeftEnd(fix));
			start = (Chromosome) list.getFirst();
			end = (Chromosome) list.getLast();
			calculateWeight ();
		}
		
		public long getLength () {
			Iterator itty = getNonEmpty ().iterator ();
			long total = 0;
			while (itty.hasNext ())
				total += ((Chromosome) itty.next ()).getLength ();
			return total;
		}
		
		/*protected void setCount {
		  	LCB temp = first;
			do {
			num_lcbs++;
			temp = central.getAdjacent (false, temp);
		} while (temp != central.getAdjacent (false, last));
		}*/

		public boolean matchedToEdge (boolean left) {
			long lcb = left ? first.getLeftEnd(fix) : last.getRightEnd(fix);
			long chrom = left ? start.getStart() : end.getEnd();
			lcb = left ? lcb - chrom : chrom - lcb;
			//System.out.println ("matched: " + left + " lcb: " + lcb + " chrom: " + chrom);
			//System.out.println ("lcb: " + (left ? 
			//		first.getLeftEnd (fix) : last.getLeftEnd (fix)));
			if (lcb < min_edge_bp)
				return true;
			else
				return false;
		}
		
		public String toString () {
			return "chrom 1: " + start + "\nchrom 2: " + end + "\nlcb 1: " + first +
					"\nlcb 2: " + last;
		}
		
		public boolean isReversed () {
			if (reversed != null)
				return reversed.booleanValue ();
			else
				return central.isReversed (first) && central.isReversed (last);
		}
		
		public void setReversed (boolean reverse) {
			//System.out.println ("reversing: " + reverse + " " + toString ());
			reversed = reverse ? Boolean.TRUE : Boolean.FALSE;
		}
		
		public HashSet getLCBs () {
			LCB current = first;
			HashSet lcbs = new HashSet ();
			LCB past = central.getAdjacentLCB(false, last);
			do {
				lcbs.add (current);
				current = central.getAdjacentLCB (false, current);
			} while (current != past);
			return lcbs;
		}
		
		public void calculateWeight () {
			Iterator itty = getLCBs ().iterator ();
			while (itty.hasNext ())
				weight += ((LCB) itty.next ()).weight;
		}
		
		public LinkedList getNonEmpty () {
			LinkedList contigs = new LinkedList ();
			if (start == end) {
				contigs.add(start);
				return contigs;
			}
			Backbone seg = null;
			Chromosome current = start;
			LCB lcb = first;
			//System.out.println ("getting nonempty");
			do {
				//System.out.println ("grouping: " + current + " lcb: " + lcb.getLeftEnd (fix));
				long start = Math.max (lcb.getLeftEnd (fix), current.getStart ());
				if (lcb.getLeftEnd (fix) < current.getStart () && 
						lcb.getRightEnd (fix) < current.getEnd () && !isSolidlyOnContig (lcb,
								current, true) && !(lcb == last)) {
					start = lcb.getRightEnd (fix) + 1;
					lcb = central.getAdjacentLCB (false, lcb);
				}
				//System.out.println ("getting seg");
				seg = bb.getNextBackbone (fix, start);
				//System.out.println ("got seg");
				long total = 0;
				do {
					if (seg != null && current.getEnd() > seg.starts [genome_ind]) {
						long match_length = seg.getSegmentLength (genome_ind);
						long end = Math.min (current.getEnd (), seg.ends [genome_ind]);
						start = Math.max (seg.starts [genome_ind], current.getStart ());
						long length = end - start;
						if (((length / (double) current.getLength() > .51) ||
								(length / (double) match_length) > min_percent) && 
								how_similar.getValueForRange (start, end) > min_similarity)
							total += length;
						boolean add = false;
						if (total / (double) current.getLength () > min_contig_ratio) {
							add = true;
						}
						/*if (!add && seg.getRightEnd (fix) > current.getEnd ()) {
							if ((onlyLCBOnContig (lcb, current) || onSingleContig (lcb))
									&& total > min_contig_bp)
								add = true;
						}*/
						//System.out.println ("in middle: " + seg);
						if (!(seg.getRightEnd (fix) > current.getEnd ())) {
							seg = bb.getNextBackbone (fix, seg.ends [genome_ind] + 1);
							if (current.getName ().indexOf("34") > 0)
								System.out.println ("new seg: " + seg);
							if (seg == null || seg.getLeftEnd (fix) > lcb.getRightEnd (fix)) {
								/*if ((onlyLCBOnContig (lcb, current) || onSingleContig (lcb))
										&& total > min_contig_bp)
									add = true;*/
								boolean past = lcb == last;
								//System.out.println ("last: " + past);
								if (!past) {
									lcb = central.getAdjacentLCB (false, lcb);
									//System.out.println ("new lcb: " + (lcb == null ? "end" : lcb.getLeftEnd (fix) + ""));
								}
								if (past || lcb == null || (lcb.getRightEnd (fix) > current.getEnd () &&
										!isSolidlyOnContig (lcb, current, false)))
									seg = null;
							}
						}
						else
							seg = null;
						if (add) {
							//System.out.println ("adding: " + current);
							contigs.add (current);
							break;
						}
					}
				} while (seg != null && seg.starts [genome_ind] < current.getEnd ());
				current = fix.getChromosomeAt(current.getEnd() + 1);
			} while (current != null && current.getStart () < end.getEnd ());
			if (contigs.size() < 1)
				System.out.println ("group determined empty");
			return contigs;
		}
		
	}
	
}