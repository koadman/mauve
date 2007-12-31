package org.gel.mauve.analysis.output;

import java.io.File;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.gel.mauve.Match;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.analysis.Segment;
import org.gel.mauve.analysis.SegmentComparator;
import org.gel.mauve.contigs.ContigHandler;

public class SegmentDataProcessor extends Hashtable implements MauveConstants {

	protected int count;

	protected int backbone_min;

	protected int reference = -1;

	protected Segment [] firsts;

	protected Segment current;

	protected Vector backbone;
	
	protected long all_seq_multiplicity;

	public SegmentDataProcessor (Hashtable map) {
		super (map);
		init ();
		new IslandCoordinateWriter (this);
		new TroubleMatchWriter (this);
		IslandFeatureWriter.printIslandsAsFeatures (this);
		IslandGeneFeatureWriter.printIslandsAsFeatures (this);
		new OverviewFileWriter (this);
	}

	protected void init () {
		String file = (String) get (FILE_STUB);
		File temp = new File (file);
		File dir = new File (temp.getParentFile (), ANALYSIS_OUTPUT);
		if (!dir.exists ())
			dir.mkdir ();
		put (FILE_STUB, new File (dir, temp.getName ()).getAbsolutePath ());
		backbone = (Vector) get (BACKBONE);
		current = Segment.END;
		count = ((Segment) backbone.get (0)).starts.length;
		for (int i = 0; i < count; i++) {
			all_seq_multiplicity <<= 1;
			all_seq_multiplicity |= 1;
		}
		put (ALL_MULTIPLICITY, new Long (all_seq_multiplicity));
		put (REFERENCE, new Integer (getReferenceSequence ()));
		addDataLinks ();
		//compressBackboneSegments ();
		addUniques ();
		//findContigsInUniques ();
		assignIDs ();
	}
	
	protected void findContigsInUniques () {
		ContigHandler handler = (ContigHandler) get (CONTIG_HANDLER);
		if (handler == null)
			return;
		for (int i = 0; i < count; i++) {
			Segment segment = firsts [i];
			long mult = multiplicityForGenome (i);
			while (segment != Segment.END) {
				if (segment.multiplicityType () != all_seq_multiplicity) {
					handler.fixSegmentByContigs (i, segment);
				}
				segment = segment.nexts [i];
			}
		}
	}
	
	protected void assignIDs () {
		Segment segment = null;
		long all_mult = ((Long) get (ALL_MULTIPLICITY)).longValue ();
		int number = 1;
		for (int i = 0; i < count; i++) {
			long cur_mult = multiplicityForGenome (i);
			segment = firsts [i];
			do {
				if (segment.typed_id == null) {
					String id = segment.multiplicityType () == all_mult ? "b_" :
							segment.multiplicityType () == cur_mult ? "i_" : "b_i_";
					id += number++;
					segment.typed_id = id;
				}
				segment = segment.nexts [i];
			} while (segment != Segment.END);
		}
	}
	
	public long multiplicityForGenome (int index) {
		return multiplicityForGenome (index, count);
	}
	
	public static long multiplicityForGenome (int index, int count) {
		long multiplicity = 1;
		multiplicity <<= (count - index - 1);
		return multiplicity;
	}

	protected int getReferenceSequence () {
		if (reference == -1) {
			Iterator itty = backbone.iterator ();
			int [] wrong = new int [count];
			int remaining = count;
			while (remaining > 1 && itty.hasNext ()) {
				Segment seg = (Segment) itty.next ();
				for (int i = 0; i < count; i++) {
					if (seg.reverse[i] && wrong[i] == 0) {
						wrong[i] = -1;
						remaining--;
					}
				}
			}
			for (int i = 0; i < count; i++) {
				if (wrong[i] == 0)
					reference = i;
			}
		}
		return reference;
	}

	protected void addDataLinks () {
		firsts = new Segment [count];
		for (int i = 0; i < count; i++) {
			Collections.sort (backbone, new SegmentComparator (i));
			int size = backbone.size () - 1;
			for (int j = 0; j <= size; j++) {
				Segment seg = (Segment) backbone.get (j);
				if (j > 0
						&& ((Segment) backbone.get (j - 1)).starts[i] != Match.NO_MATCH) {
					seg.prevs[i] = (Segment) backbone.get (j - 1);
					if (firsts[i] == null) {
						firsts[i] = seg.prevs[i];
						firsts[i].prevs[i] = Segment.END;
					}
				}
				if (j < size && seg.starts[i] != Match.NO_MATCH)
					seg.nexts[i] = (Segment) backbone.get (j + 1);
			}
			((Segment) backbone.get (size)).nexts[i] = Segment.END;
		}
		put (FIRSTS, firsts);
	}

	protected void addUniques () {
		long [] sizes = (long []) get (GENOME_LENGTHS);
		for (int i = 0; i < count; i++) {
			Segment prev = firsts[i];
			if (prev.starts[i] != 1) {
				Segment seg = new Segment (count, true);
				seg.ends[i] = prev.starts[i] - 1;
				seg.starts[i] = 1;
				firsts[i] = seg;
				seg.nexts[i] = prev;
				prev.prevs[i] = seg;
			}
			Segment cur = prev.nexts[i];
			while (cur != firsts[i] && cur != Segment.END) {
				if (prev.ends[i] + 1 != cur.starts[i]) {
					if (prev.ends[i] == cur.starts[i]) {
						/*System.out.println ("bp in two segments");
						System.out.println ("segment: " + cur + " prev: "
								+ prev);*/;
					} else {
						Segment island = new Segment (count, true);
						island.starts[i] = prev.ends[i] + 1;
						island.ends[i] = cur.starts[i] - 1;
						prev.nexts[i] = island;
						island.prevs[i] = prev;
						island.nexts[i] = cur;
						cur.prevs[i] = island;
					}
				}
				prev = cur;
				if (sizes != null && cur.nexts [i] == Segment.END &&
						cur.ends [i] < sizes [i]) {
					Segment island = new Segment (count, true);
					island.starts[i] = cur.ends[i] + 1;
					island.ends[i] = sizes [i];
					cur.nexts[i] = island;
					island.prevs[i] = cur;
					island.nexts[i] = Segment.END;
				}
				cur = prev.nexts[i];
			}
		}
	}

	// it doesn't necessarily matter if segments length 1 are reversed or not.
	// this
	// algorithm assumes whatever wrote the .backbone file put it in the "best"
	// orientation
	//this algorithm isn't working 100%; is currently concatinating two with different multiplicities
	protected void compressBackboneSegments () {
		try {
			int ind = reference;
			int num_combined = 0;
			long dist_added = 0;
			do {
				current = firsts[ind].nexts[ind];
				while (current != Segment.END) {
					int j = ind;
					boolean ok = true;
					long this_dist = 0;
					do {
						Segment comp = (current.reverse[ind] == current.reverse[j]) ? current.prevs[j]
								: current.nexts[j];
						if (comp == Segment.END || comp == firsts[j])
							ok = false;
						if (comp != null
								&& comp.multiplicityType () != current
										.multiplicityType ())
							ok = false;
						if (ok && comp == current.prevs[ind]) {
							long dist = current.starts[j] - comp.ends[j] - 1;
							if (dist < 0
									|| dist > backbone_min
									|| dist > current.ends[j]
											- current.starts[j] + 1
									|| dist > comp.ends[j] - comp.starts[j]
											+ 1)
								ok = false;
							else
								this_dist += dist;
						} else if (comp != null)
							ok = false;
						j++;
						if (j == count)
							j = 0;
					} while (ok && j != ind);
					current = current.nexts[ind];
					if (ok) {
						dist_added += this_dist;
						num_combined++;
						current.prevs[ind].prevs[ind].append (current, false);
						current.remove (ind);
						if (backbone.contains (current))
							backbone.remove (current);
					}
				}
				if (++ind == count)
					ind = 0;
			} while (ind != reference);
		} catch (Exception e) {
			System.out.println ("Problems compressing backbone segments");
			e.printStackTrace ();
		}
	}

	protected Vector makeDefaultStartEndColumnHeaders () {
		int divisor = (get (CONTIG_HANDLER) == null || get (CONTIG_HANDLER) 
				instanceof AbstractMatchDataWriter) ? 2 : 3;
		Vector titles = new Vector ();
		int seq = -1;
		String tail = null;
		for (int i = 0; i < count * divisor; i++) {
			if (i % divisor == 0) {
				seq++;
				tail = "left";
			}
			else if (i % divisor == 1)
				tail = "right";
			else
				tail = "contig";
			titles.add ("seq" + seq + "_" + tail);
		}
		return titles;
	}

	protected List getSegmentsSortedBySeq (int sequence) {
		Collections.sort (backbone, new SegmentComparator (sequence));
		return backbone;
	}

}
