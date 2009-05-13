package org.gel.mauve.operon;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.RangeLocation;
import org.gel.air.bioj.BioJavaUtils;
import org.gel.air.util.MathUtils;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.analysis.PhyloMultiplicity;
import org.gel.mauve.analysis.Segment;
import org.gel.mauve.operon.AncestralState.Difference;

public abstract class OperonDiff {
	
	public static String MULTIPLICITY_FEAT = "multiplicity";
	
	protected String feature;
	protected OperonHandler handler;
	protected PhyloOperon ent;
	protected Operon op;
	protected int seq;
	protected HashSet <Operon> comps;
	protected boolean same;
	
	public OperonDiff (String feat, PhyloOperon operon) {
		feature = feat;
		handler = operon.handler;
		ent = operon;
	}

	public String getFeature() {
		return feature;
	}

	public abstract AncestralState.Difference getLastDifference();

	/**
	 * should populate comps
	 */
	public abstract boolean isSame ();
	
	public boolean isSame(Operon one, int seq2) {
		if (!(one == op && seq2 == seq)) {
			comps = null;
			op = one;
			seq = seq2;
			same = isSame ();
		}
		return same;
	}
	
	public HashSet <Operon> getLastRelatedOperons () {
		return comps;
	}
	
	public HashSet <Operon> getRelatedOperons (Operon one, int seq2) {
		if (!(one == op && seq2 == seq))
			isSame (one, seq2);
		return comps;
	}
	
	
	public class OrthologDiff extends OperonDiff {
		
		public OrthologDiff (String feat, PhyloOperon handle) {
			super (feat, handle);
		}
		
		public boolean isSame() {
			Sequence seq2 = handler.model.getGenomeBySourceIndex(
					seq).getAnnotationSequence();
			
			return false;
		}
		
		public Difference getLastDifference() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public static class MultiplicityDiff extends OperonDiff implements 
			MauveConstants {
		
		protected double min_overlap;
		protected String difference;
		
		public MultiplicityDiff (String feat, PhyloOperon handle) {
			super (feat, handle);
			min_overlap = ((Double) handle.handler.proc.get(
					MINIMUM_PERCENT_CONTAINED)).doubleValue();
		}
		
		public boolean isSame () {
			PhyloMultiplicity p_mult = handler.op_mults [op.seq].get(op.getStart()); 
			long [] coords = p_mult.inMultiplicity (op.getStart (), op.getEnd(), 
					handler.model, seq, op.seq, min_overlap);
			if (op.getName().contains("lsrK") && op.seq == 4 && seq == 2)
				System.out.println ("coords: " + coords);
			if (coords != null) {
				long start = coords [0];
				long end = coords [1];
				Vector <StrandedFeature> seq2_ops = handler.bioj_ops [seq];
				int ind = Collections.binarySearch(seq2_ops, start, 
						BioJavaUtils.FEATURE_COMPARATOR);
				if (ind < 0)
					ind = -(ind + 1);
				if (ind >= seq2_ops.size ()) {
					difference = "Operon not in genome";
					return false;
				}
				if (start < seq2_ops.get(ind).getLocation().getMin() &&
						ind > 0 && seq2_ops.get(ind).getLocation (
						).getMax () > start) {
					ind--;
				}
				Location loci;
				do {
					loci = seq2_ops.get(ind).getLocation();
					if (MathUtils.percentContained(loci.getMin(), loci.getMax (), 
							start, end) > min_overlap) {
						if (MathUtils.percentContained(start, end, loci.getMin(), loci.getMax ()) >
								min_overlap) {
							comps = new HashSet ();
							comps.add (handler.op_lists [seq].get(ind));
							return true;
						}
					}
					ind++;
				} while (end > loci.getMax () && ind < seq2_ops.size() && 
						seq2_ops.get(ind).getLocation().getMin() < end);
				difference = "Aligned to part or none of other operon";
			}
			else if (p_mult.getLastMismatch() == PhyloMultiplicity.NOT_ALIGNED) {
				double percent = p_mult.getLastPercent();
				if (percent == 0)
					difference = "Operon not in genome";
				else
				{
					difference = "Only " + MathUtils.doubleToString(
							percent, 2) + "% of operon in genome";
				}
			}
			else
				difference = "Operon rearranged in other genome";
			return false;
		}
		
		
		public Difference getLastDifference() {
			return new Difference (difference);
		}
	
	} //class MultiplicityDiff
	
	

}
