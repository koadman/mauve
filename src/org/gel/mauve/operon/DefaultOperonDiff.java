package org.gel.mauve.operon;

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

public abstract class DefaultOperonDiff implements OperonDiff {
	
	protected String feature;
	protected OperonHandler handler;
	protected PhyloOperon ent;
	protected Operon op;
	protected int seq;
	protected HashSet <Operon> comps;
	
	public DefaultOperonDiff (String feat, PhyloOperon operon) {
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
		op = one;
		seq = seq2;
		return isSame ();
	}
	
	public HashSet getRelatedOperons (Operon one, int seq2) {
		if (!(one == op && seq2 == seq))
			isSame (one, seq2);
		return comps;
	}
	
	
	public class OrthologDiff extends DefaultOperonDiff {
		
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
	
	public class MultiplicityDiff extends DefaultOperonDiff implements 
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
			long seq2_mult = MauveHelperFunctions.multiplicityForGenome(seq, 
					handler.firsts.length);
			if (p_mult.inMultiplicity (op.getEnd() - op.getStart() + 1, 
					 seq, op.seq, min_overlap)) {
				long [] coords = handler.model.getAlignCoords (
						handler.model.getGenomeBySourceIndex(seq), op.getStart(),
						op.getEnd());
				
				Vector <StrandedFeature> seq2_ops = handler.bioj_ops [seq];
				int ind = -(Collections.binarySearch(seq2_ops, op.getStart(), 
						BioJavaUtils.FEATURE_COMPARATOR) + 1);
				if (op.getStart () < seq2_ops.get(ind).getLocation().getMin())
					ind--;
				Location loci;
				do {
					loci = seq2_ops.get(ind).getLocation();
					if (MathUtils.percentContained(op.getStart(), op.getEnd (), 
							loci.getMin(), loci.getMax ()) > min_overlap) {
						if (MathUtils.percentContained(loci.getMin(), loci.getMax (),
								op.getStart(), op.getEnd ()) > min_overlap) {
							comps.add (handler.op_lists [seq].get(ind));
							return true;
						}
					}
					ind++;
				} while (coords [1] > loci.getMax ());
				difference = "aligned to part or none of other operon";
			}
			else
				difference = "Less than " + min_overlap + " of operon in genome";
			return false;
		}
		
		
		public Difference getLastDifference() {
			return new Difference (difference);
		}
	
	} //class MultiplicityDiff
	
	

}
