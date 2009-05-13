package org.gel.air.bioj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.ComponentFeature;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;
import org.gel.air.util.MathUtils;

public class BioJavaUtils implements BioJavaConstants {

	public static Vector restrictedList (Vector <Feature> features, HashSet types) {
		return restrictedList (features.iterator(), types, false);
	}
	
	public static Vector restrictedList (Vector <Feature> features, HashSet types,
			boolean post_loaded) {
		return restrictedList (features.iterator(), types, post_loaded);
	}
	
	public static Vector restrictedList (Iterator <Feature> itty, HashSet types) {
		return restrictedList (itty, types, false);
	}
	public static Vector restrictedList (Iterator <Feature> itty, HashSet types,
			boolean post_loaded) {
		Vector keeps = new Vector <Feature> ();
		while (itty.hasNext()) {
			Feature feat = itty.next();
			boolean good = false;
			String type = feat.getType().toLowerCase();
			Iterator <String> keep = types.iterator();
			while (keep.hasNext()) {
				if (type.contains(keep.next().toLowerCase ()))
					good = true;
			}
			if (good && feat.getAnnotation() != null)
			if ((!post_loaded) || feat.getAnnotation().containsProperty("label"))
				keeps.add(feat);
		}
		System.out.println ("sl: " + keeps.size());
		return keeps;
	}
	
	public static Hashtable <String, StrandedFeature> getLoci (
			Vector <StrandedFeature> feats) {
		Hashtable <String, StrandedFeature> loci = new Hashtable <String, 
				StrandedFeature> ();
		Iterator <StrandedFeature> itty = feats.iterator();
		while (itty.hasNext()) {
			StrandedFeature feat = itty.next(); 
			if (feat.getAnnotation().containsProperty("locus_tag")) {
				loci.put((String) feat.getAnnotation().getProperty("locus_tag"), 
						feat);
			}
		}
		return loci;
	}

	public static Vector getSortedStrandedFeatures (FeatureHolder annos) {
		Iterator <Feature> itty = annos.features();
		Vector <StrandedFeature> feats = new Vector <StrandedFeature> (); 
		while (itty.hasNext ()) {
			Feature feat = itty.next();
			if (feat instanceof ComponentFeature) {
				Iterator <Feature> itty2 = feat.features();
				while (itty2.hasNext ()) {
					Feature feat2 = itty2.next();
					if ( feat2 instanceof StrandedFeature)
						feats.add((StrandedFeature) feat2);
				}
				//if only want chromosome, not plasmids, uncomment break
				//break;
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
	
	public static String getName (Feature feat) {
		Annotation note = feat.getAnnotation();
		if (note != null) {
			StringTokenizer toke = new StringTokenizer (LOC_NAME, "/");
			while (toke.hasMoreTokens()) {
				String key = toke.nextToken();
				if (note.containsProperty(key))
					return (String) note.getProperty(key);
			}
		}
		return null;
	}
	
	public static String getPrefix (String gene) {
		int ind = 0;
		while (ind < gene.length() && Character.isLowerCase(gene.charAt(ind)))
			ind++;
		return gene.substring(0, ind);
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

	/**
	 * A comparator for features.
	 */
	public static final Comparator FEATURE_COMPARATOR = new Comparator() {
		/**
		 * Returns -1 if Feature a is first in the sequence, 1 if b is first, and 0 if they are at the same place
		 * @param a The first object to compare
		 * @param b The second object to compare
		 */
		public int compare(Object a, Object b) {
			int one = a instanceof Feature ? ((Feature) a).getLocation()
					.getMin() : ((Number) a).intValue();
			int two = b instanceof Feature ? ((Feature) b).getLocation()
					.getMin() : ((Number) b).intValue();
			return (one == two) ? 0 : (one < two) ? -1 : 1;
		}
	};
}
