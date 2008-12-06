package org.gel.air.bioj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;
import org.gel.air.util.GroupUtils;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.analysis.output.AbstractTabbedDataWriter;
import org.gel.mauve.gui.Mauve;
import org.gel.mauve.gui.MauveFrame;
import org.gel.mauve.module.MauveModule;
import org.gel.mauve.module.ModuleListener;

public class BadFeatureFinder extends AbstractTabbedDataWriter
		implements MauveConstants {
	
	protected ArrayList <StrandedFeature> feats;
	protected ArrayList <StrandedFeature> feats2;
	protected Hashtable <String, double []> [] blast;
	protected HashSet <String> interpro;
	protected double min_blast_id;
	protected double min_blast_length;
	protected double max_ok_overlap_prct;
	protected int max_ok_overlap_bp;
	protected int max_bad_length;
	protected String db_head;
	protected LinkedHashMap <StrandedFeature, WeightedFeature> bad_feats;
	protected Iterator <WeightedFeature> printer;
	protected WeightedFeature current;
	
	
	
	public BadFeatureFinder (Sequence annos, String blast_dir, String interpro_file,
			String file) {
		super (file, null);
		init (annos, blast_dir, interpro_file);
	}
	
	public void init (Sequence annos, String blast_dir, String interpro_file) {
		FeatureHolder hold = annos.filter(new FeatureFilter.Or (
				new FeatureFilter.ByType ("gene"), new FeatureFilter.ByType ("CDS")));
		feats = BioJavaUtils.getSortedStrandedFeatures(hold);
		System.out.println (feats.size());
		feats2 = new ArrayList (feats);
		Collections.sort(feats2, BioJavaUtils.FEATURE_END_COMPARATOR);
		interpro = new HashSet <String> ();
		min_blast_id = 40.0;
		min_blast_length = 60.0;
		max_bad_length = Integer.MAX_VALUE;
		max_ok_overlap_bp = Integer.MAX_VALUE;
		max_ok_overlap_prct = 10.0;
		db_head = ASAP;
		bad_feats = new LinkedHashMap <StrandedFeature, WeightedFeature> ();
		parseInterpro (interpro_file);
		parseBlast (blast_dir);
		findBadFeatures ();
		printBadFeatures ();
	}
	
	/**
	 * tries to find the dbxref head that matches the ids in the passed in hashtable
	 */
	protected void getDbHead () {
		
	}
	
	public void findBadFeatures () {
		StrandedFeature feat = null;
		for (int i = 0; i < feats.size(); i++) {
			feat = feats.get (i);
			int length = BioJavaUtils.getLength(feat);
			String dbxref = MauveHelperFunctions.getTruncatedDBXrefID(feat, db_head); 
			boolean ipro = dbxref != null && interpro.contains (dbxref);
			int blast_mult = getBlastMultiplicity (dbxref);
			//if (blast_mult == 0)
				//System.out.println ("bad");
			if (length <= max_bad_length && !ipro && blast_mult == 0) {
				//System.out.println ("overlap");
				double overlap = findOverlap (i);
				if (overlap > max_ok_overlap_bp)
					overlap = length;
				overlap = (overlap /length) * 100;
				if (overlap > max_ok_overlap_prct) {
					WeightedFeature wf = new WeightedFeature (feat, length, 
							overlap, ipro, blast_mult);
					bad_feats.put(feat, wf);
				}
			}
		}
	}
	
	public int getBlastMultiplicity (String dbxref) {
		int multiplicity = 0;
		for (int i = 0; i < blast.length; i++) {
			multiplicity <<= 1;
			if (blast [i].containsKey(dbxref))
				multiplicity++;
		}
		//System.out.println (multiplicity);
		return multiplicity;
	}
	
	public int indToInd2 (int index) {
		StrandedFeature feat = feats.get(index);
		int i = index;
		do {
			if (feats2.get (i) == feat)
				return i;
			i++;
		} while (i < feats.size() && feats2.get(i).getLocation().getMax() <= 
				feat.getLocation ().getMax());
		i = index - 1;
		do {
			if (feats2.get (i) == feat)
				return i;
			i--;
		} while (i > -1 && feats2.get(i).getLocation().getMax() >= 
				feat.getLocation ().getMax());
		System.out.println ("error");
		return -1;
	}
	
	public double findOverlap (int index) {
		int l_end = 0, r_start = 0, r_end, temp;
		Location loci = null;
		int j = indToInd2 (index) - 1;
		loci = feats.get(index).getLocation();
		r_end = loci.getMax();
		while (j > 0) {
			temp = feats.get(j--).getLocation().getMax();
			if (temp > loci.getMin()) {
				l_end = Math.max(l_end, temp);
			}
			else
				break;
		}
		j = index + 1;
		while (j < feats.size()) {
			temp = feats.get(j++).getLocation().getMin();
			if (temp < loci.getMax ()) {
				if (j - 2 == index)
					r_start = (temp <= l_end ? l_end + 1 : temp);
				r_end = Math.max(r_end, Math.min (feats.get(j++).getLocation().getMax(),
						loci.getMax ()));
			}
			else 
				break;
		}
		int overlap = (r_start > 0 ? r_end - r_start + 1 : 0);
		if (l_end > 0)
			overlap += l_end - loci.getMin() + 1;
		return overlap;
	}
	
	/**expects a directory of blast files.  each should have the genome in
	 * question as the query, and should have 28 columns:  asap id in 5th, percent
	 * identity in the 25th, and percent query length in the 26th.
	 * 
	 * If sort order 1 (col 1) always has best result first, could probably 
	 * ignore other sort orders
	**/
	public void parseBlast (String file_dir) {
		File dir = new File (file_dir);
		File [] files = dir.listFiles();
		if (files == null)
			blast = new Hashtable [0];
		else {
			Arrays.sort(files);
			blast = new Hashtable [files.length];
		}
		for (int i = 0; i < blast.length; i++) {
			try {
				blast [i] = new Hashtable <String, double []> ();
				BufferedReader in = new BufferedReader (new FileReader (files [i]));
				String s = in.readLine();
				s = in.readLine();
				double id = 0;
				double len = 0;
				while (s != null) {
					StringTokenizer toke = new StringTokenizer (s, "\t");
					if (toke.countTokens() == 27) {
						for (int j = 1; j < 27; j++) {
							if (j == 5)
								s = toke.nextToken();
							else if (j == 24) {
								id = Double.parseDouble(toke.nextToken());
								if (id < min_blast_id)
									break;
							}
							else if (j == 26) {
								len = Double.parseDouble(toke.nextToken());
								if (len >= min_blast_length)
									blast [i].put(s, new double [] {id, len});
							}
							else
								toke.nextToken();
						}
					}
					s = in.readLine();
				}
			} catch (Exception e) {
				System.out.println ("error");
				e.printStackTrace();
			}
		}
	}
	
	/**expects asap id as the part of first field between the last underscore
	 * and the last +.
	 **/ 
	public void parseInterpro (String file) {
		try {
			BufferedReader in = new BufferedReader (new FileReader (file));
			String s = in.readLine();
			while (s != null) {
				s = s.substring (0, s.indexOf('\t'));
				s = s.substring(s.lastIndexOf('_') + 1, s.indexOf('+'));
				interpro.add (s);
				s = in.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void printBadFeatures () {
		printer = bad_feats.values().iterator();
		if (printer.hasNext())
			printData ();
		else
			System.out.println ("no bad features");
		doneWritingFile ();
	}
	
	protected String getData(int column, int row) {
		switch (column) {
			case 0:
				return MauveHelperFunctions.getUniqueId(current.feat);
			case 1:
				return current.blast + "";
			case 2:
				return current.length + "";
			case 3:
				return current.overlap + "";
		}
		return null;
	}

	protected boolean moreRowsToPrint() {
		return printer.hasNext();
	}

	protected Vector setColumnHeaders() {
		Vector <String> heads = new Vector (4);
		heads.add("ID");
		heads.add("Blast");
		heads.add("length");
		heads.add("overlap");
		return heads;
	}

	protected boolean shouldPrintRow(int row) {
		current = printer.next();
		return true;
	}

	public class WeightedFeature {
		
		protected StrandedFeature feat;
		protected int length;
		protected double overlap;
		protected boolean ipro;
		protected int blast;
		
		
		public WeightedFeature (StrandedFeature f, int l, double o, 
				boolean i, int b) {
			feat = f;
			length = l;
			overlap = o;
			ipro = i;
			blast = b;
		}
		
		public boolean isBad () {
			return length < max_bad_length || !ipro || overlap > max_ok_overlap_prct ||
					blast == 0;
		}
		
	}
	
	public static void main (final String [] args) {
		MauveModule mv = new MauveModule (new ModuleListener () {
			public void startModule (MauveFrame mauve) {
				int seq = Integer.parseInt (args [3]);
				new BadFeatureFinder (mauve.getModel ().getGenomeBySourceIndex (
						seq).getAnnotationSequence (),
				args [1], args [2], MauveHelperFunctions.getRootDirectory(
						mauve.getModel()) + "bad_feats_seq" + seq + ".tab");
			}
		});
		Mauve.mainHook (args, mv);
	}
}
