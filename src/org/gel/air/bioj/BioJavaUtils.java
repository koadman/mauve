package org.gel.air.bioj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;

import org.biojava.bio.seq.ComponentFeature;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;
import org.gel.air.util.MathUtils;

public class BioJavaUtils {

	public static ArrayList getSortedStrandedFeatures (FeatureHolder annos) {
		Iterator <Feature> itty = annos.features();
		ArrayList <StrandedFeature> feats = new ArrayList <StrandedFeature> (); 
		while (itty.hasNext ()) {
			Feature feat = itty.next();
			if (feat instanceof ComponentFeature) {
				Iterator <Feature> itty2 = feat.features();
				while (itty2.hasNext ()) {
					Feature feat2 = itty2.next();
					if ( feat2 instanceof StrandedFeature)
						feats.add((StrandedFeature) feat2);
				}
			}
			else if ( feat instanceof StrandedFeature)
				feats.add((StrandedFeature) feat);
		}
		Collections.sort(feats, FEATURE_START_COMPARATOR);
		return feats;
	}
	
	public static int getLength (Feature feat) {
		return feat.getLocation().getMax() - feat.getLocation().getMin() + 1;
	}

	public static final Comparator FEATURE_START_COMPARATOR = new Comparator () {
		public int compare (Object a, Object b) {
			Location one = ((Feature) a).getLocation ();
			Location two = ((Feature) b).getLocation ();
			return MathUtils.compareByStartThenLength (one.getMin (), one.getMax (),
					two.getMin (), two.getMax ());
		}
	};
	
	public static final Comparator FEATURE_END_COMPARATOR = new Comparator () {
		public int compare (Object a, Object b) {
			Location one = ((Feature) a).getLocation ();
			Location two = ((Feature) b).getLocation ();
			return one.getMax() - two.getMax();
		}
	};
}
