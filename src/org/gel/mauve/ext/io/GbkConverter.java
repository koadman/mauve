package org.gel.mauve.ext.io;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.biojava.bio.Annotatable;
import org.biojava.bio.seq.ComponentFeature;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.StrandedFeature.Strand;
import org.biojava.bio.seq.io.GenbankProcessor;
import org.biojava.bio.symbol.Location;
import org.gel.air.ja.stash.Stash;
import org.gel.air.ja.stash.StashLoader;
import org.gel.mauve.Genome;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.ext.MauveStoreConstants;

public class GbkConverter implements MauveStoreConstants {
	
	protected static final HashSet <String> SKIPPERS = new HashSet <String> ();
	
	static {
		SKIPPERS.add ("ORIGIN");
		SKIPPERS.add ("internal_data");
		SKIPPERS.add (GenbankProcessor.PROPERTY_GENBANK_ACCESSIONS);
	}
	
	protected Sequence seq;
	protected int genome_index;
	public final static String GENOME_DIRECTORY = "genome_data";
	protected Stash genome;
	protected Genome src;
	protected Stash feature_labels;
	protected String db_header;
	
	public GbkConverter (Genome gen, String db_head, StashLoader loader) {
		seq = gen.getAnnotationSequence();
		db_header = db_head;
		src = gen;
		genome_index = gen.getSourceIndex();
		genome = new Stash (GENOME_CLASS);
		convertToStash (loader);
	}
	
	protected void convertToStash (StashLoader loader) {
		Iterator <ComponentFeature> itty = seq.features();
		long time = System.currentTimeMillis();
		System.out.println ("time: " + (System.currentTimeMillis() - time));
		genome.put(URI, makeSequenceFile (seq));
		System.out.println ("time: " + (System.currentTimeMillis() - time));
		Stash c_labels = new Stash (LIST_CLASS, CONTIG_INDEX);
		genome.put(CONTIG_INDEX, c_labels);
		Stash features = new Stash (FEATURE_INDEX_CLASS);
		genome.put(FEATURE_INDEX, features.get (ID));
		Stash files = new Stash (LIST_CLASS, FEATURE_FILES);
		features.put(FEATURE_FILES, files);
		Stash file = new Stash (FEATURE_FILE_CLASS);
		file.put(URI, src.getURI());
		file.put(FORMAT, src.getAnnotationFormat().getFormatName());
		files.put(file.get(ID), file);
		feature_labels = new Stash (LIST_CLASS, FEATURE_LABELS);
		features.put(FEATURE_LABELS, feature_labels);		
		while (itty.hasNext()) {
			ComponentFeature feat = itty.next();
			FeatureHolder hold = feat.filter(new FeatureFilter.ByType ("source"));
			Iterator <Feature> itty2 = hold.features();
			Stash contig_label = new Stash (GENOME_LABEL_CLASS,
					itty2.hasNext () ?
					MauveHelperFunctions.getDBXrefID (itty2.next (), db_header)
					: null);
			c_labels.put(contig_label.get(ID), contig_label);
			contig_label.put(NAME, src.getChromosomeAt(feat.getLocation().getMin()).getName());
			addLocation (contig_label, feat, 0);
			makeContigStash (feat, contig_label);
		}
		loader.writeXMLFile(genome, "c:\\genome.xml");
		loader.writeXMLFile(features, "c:\\features.xml");
		//load.writeCompressedFile(features, "c:\\features.xxml");
		System.out.println ("all written");
	}

	protected void addAnnotationData (Stash object, Annotatable feat) {
		Map <String, Object>annos = feat.getAnnotation().asMap();
		Iterator <String> props = annos.keySet().iterator();
		while (props.hasNext()) {
			String key = props.next();
			if (SKIPPERS.contains (key))
				continue;
			Object val = annos.get(key);
			if (val instanceof String)
				object.put (key, val);
			else if (val instanceof Boolean || val instanceof Number)
				object.put (key, val);
			else if (val instanceof ArrayList){
				Stash list = new Stash (LIST_CLASS);
				object.put(key, list);
				Iterator <String> vals = ((ArrayList) val).iterator ();
				int count = 1;
				while (vals.hasNext())
					list.put((count++) + "", vals.next());
			}
			else
				System.out.println ("not expected: " + val.getClass() + "   " + key);
		}
	}
	protected void makeContigStash (ComponentFeature parent, Stash contig_label) {
		Iterator <Feature> itty = parent.features();
		int count = 0;
		while (itty.hasNext()) {
			count++;
			Feature feat = itty.next();
			if (feat.countFeatures() > 0)
				System.out.println ("has kids");
			/*System.out.println ("type: " + feat.getType() + "   " + feat.getTypeTerm());
			System.out.println ("source: " + feat.getSource() + "   " + feat.getSourceTerm());*/
			String type = feat.getType().toLowerCase();
			Stash feature = new Stash (GENOME_LABEL_CLASS,
					MauveHelperFunctions.getDBXrefID(feat, db_header));
			feature.put(FEATURE_TYPE, type);
			addLocation (feature, feat, contig_label.getLong(LEFT_STRING) - 1);
			feature_labels.put(feature.getString(ID), feature);
		}
	}
	
	/**
	 * offset is how much to add to the local positions to get, for eg,
	 * an absolute position.  A negative offset could do the reverse.
	 * An offset of 0 does nothing.
	 * 
	 * @param feature
	 * @param feat
	 * @param offset
	 */
	protected void addLocation (Stash feature, Feature feat, long offset) {
		if (feat instanceof StrandedFeature) {
			Strand strand = ((StrandedFeature) feat).getStrand();
			if (strand != null && !strand.equals(StrandedFeature.UNKNOWN))
				feature.put(STRAND_STRING, strand.equals(
						StrandedFeature.POSITIVE) ? FORWARD_SYMBOL : COMPLEMENT_SYMBOL);
		}
		Location loc = feat.getLocation();
		feature.put(LEFT_STRING, (loc.getMin() + offset) + "");
		feature.put(RIGHT_STRING, (loc.getMax() + offset) + "");
	}
	
	public String makeSequenceFile (Sequence sequence) {
		try {
			String file = "c:\\sequence.sba";
			FileOutputStream out = new FileOutputStream (file);
			Iterator <ComponentFeature> itty = seq.features();
			while (itty.hasNext()) {
				Location c = itty.next ().getLocation();
				out.write(sequence.subStr(c.getMin(), 
						c.getMax()).getBytes());
			}
			out.close();
			return file;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Stash getGenome () {
		return genome;
	}

}
