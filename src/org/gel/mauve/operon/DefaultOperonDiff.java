package org.gel.mauve.operon;

import org.biojava.bio.seq.Sequence;

public abstract class DefaultOperonDiff implements OperonDiff {
	
	protected String feature;
	protected OperonHandler handler;
	
	public DefaultOperonDiff (String feat, OperonHandler hand) {
		feature = feat;
		handler = hand;
	}

	public String getFeature() {
		return feature;
	}

	public abstract boolean isSame(Operon one, int seq2);
	
	
	public class OrthologDiff extends DefaultOperonDiff {
		
		public OrthologDiff (String feat, OperonHandler handle) {
			super (feat, handle);
		}
		
		public boolean isSame(Operon one, int seq2) {
			Sequence seq = handler.model.getGenomeBySourceIndex(
					seq2).getAnnotationSequence();
			
			return false;
		}
		
	}

}
