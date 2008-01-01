package org.gel.mauve.contigs;

import java.util.Hashtable;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.io.FastaFormat;

public class ChangedFastaFormat extends FastaFormat {
	
	protected Hashtable names;
	
	public ChangedFastaFormat (Hashtable map) {
		names = map;
	}

	protected String describeSequence (Sequence seq) {
		String desc = super.describeSequence(seq);
		int ind = desc.indexOf (' ');
		if (ind > -1) {
			desc = desc.substring (0, ind);
		}
		System.out.println ("desc: " + desc);
		if (names.containsKey (desc))
			desc = (String) names.get (desc);
		return desc;
	}
	
	

}
