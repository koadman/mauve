package org.gel.mauve;

import java.io.File;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;

import org.biojava.bio.AnnotationType;
import org.biojava.bio.seq.ComponentFeature;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.Location;
import org.gel.air.util.MathUtils;
import org.gel.mauve.analysis.Segment;
import org.gel.mauve.gui.sequence.FlatFileFeatureConstants;
import org.gel.mauve.summary.output.SegmentDataProcessor;

/**
 * contains file writing routines and variables useful to multiple classes.
 * 
 * @author Anna I Rissman
 * 
 */
public class MauveHelperFunctions implements FlatFileFeatureConstants {

	
	public static final Comparator FEATURE_COMPARATOR = new Comparator () {
		public int compare (Object a, Object b) {
			Location one = ((Feature) a).getLocation ();
			Location two = ((Feature) b).getLocation ();
			return MathUtils.compareByStartThenLength (one.getMin (), one.getMax (),
					two.getMin (), two.getMax ());
		}
	};
	
	/**
	 * Returns a genome name with ".fas" appended to it
	 * if it is not already there.
	 * 
	 * @param genome the genome whose name to convert
	 * @return <code>genome.getDisplayName() + .fas</code>
	 */
	public static String genomeNameToFasta (Genome genome) {
		String file = genome.getDisplayName ();
		if (file.toLowerCase ().indexOf (".fas") == -1) {
			if (!file.endsWith("."))
				file += ".";
			file += "fas";
		}
		return file;
	}
	
	public static File getRootDirectory (BaseViewerModel model) {
		return model.getSrc ().getParentFile ();
	}
	
	public static File getChildOfRootDir (BaseViewerModel model, String child) {
		return new File (getRootDirectory (model), child);
	}
	
	public static String getFileStub (BaseViewerModel model) {
		String name = model.getSrc().getName ();
		if (!getChildOfRootDir (model, name + ".backbone").exists())
			name = name.substring(0, name.indexOf(".alignment"));
		return name;
	}

		
	/**
	 * writes out genome names and associates each with a unique index
	 * 
	 * @param model
	 *            The model containing the genomes of interest
	 * @param out
	 *            The stream to output to
	 */
	public static void writeGenomesWithIndices (BaseViewerModel model,
			PrintStream out) {
		try {
			for (int i = 0; i < model.getSequenceCount (); i++) {
				out.println (model.getGenomeBySourceIndex (i).getDisplayName ()
						+ "--" + i);
			}
		} catch (Exception e) {
			System.out.println ("couldn't output genomes and indeces");
		}
	}
/*
	public static void printSegment (Segment segment,
			AbstractTabbedDataWriter writer) {
		String [] data = new String [segment.ends.length * 2];
		for (int i = 0, j = 0; i < segment.ends.length; i++, j++) {
			String start = segment.reverse[i] ? "-" : "";
			data[j++] = start + segment.starts[i];
			data[j] = start + segment.ends[i];
		}
		writer.printRow (data);
	}
*/
	public static String doubleToString (double number, int decimals) {
		DecimalFormat format = new DecimalFormat ();
		format.setMaximumFractionDigits (decimals);
		return format.format (number);
	}
	
	public static String getSeqPartOfFile (SegmentDataProcessor processor) {
		return "seq_" + processor.get (SEQUENCE_INDEX).toString ();
	}
	
	public static String getReadableMultiplicity (Segment segment) {
		return getReadableMultiplicity (segment.multiplicityType (), segment.left.length);
	}
	
	public static String getReadableMultiplicity (long multiplicity, int count) {
		String val = Long.toBinaryString (multiplicity).replace ('0', '.').replace ('1', '*');
		while (val.length () < count)
			val = "." + val;
		return val;
	}
	
	public static void addChromByStart (Hashtable table, Chromosome chrom) {
		table.put (new Long (chrom.getStart ()), chrom);
	}
	
	public static Chromosome getChromByStart (Hashtable table, Chromosome chrom) {
		return (Chromosome) table.get (new Long (chrom.getStart ()));
	}
	
	public static Feature getFeatByStart (Hashtable table, Chromosome chrom) {
		return (Feature) table.get (new Long (chrom.getStart ()));
	}
	
	public static Chromosome removeChromByStart (Hashtable table, Chromosome chrom) {
		return (Chromosome) table.remove (new Long (chrom.getStart ()));
	}
	
	public static Iterator getFeatures (BaseViewerModel model, int genome) {
		Sequence holder = (Sequence) model.getGenomeBySourceIndex (genome)
				.getAnnotationSequence ();
		FeatureHolder hold = holder.filter (new FeatureFilter.And (NULL_AVOIDER,
				new FeatureFilter.ByAnnotationType (
				AnnotationType.ANY)), true);
		return hold.features ();
	}


	/**
	 * returns asap dbxref if exists, then any other dbxref, then label or gene
	 * annotation if one exists.
	 * 
	 * @param feat
	 * @return
	 */
	public static String getUniqueId (Feature feat) {
		String val = getDBXrefID (feat, ASAP);
		if (val == null)
			val = getDBXrefID (feat, ERIC);
		if (val == null)
			val = getDBXrefID (feat, "");
		if (val == null) {
			if (feat.getAnnotation ().containsProperty (LABEL_STRING)) {
				val = (String) feat.getAnnotation ().getProperty (LABEL_STRING);
			}
			else if (feat.getAnnotation ().containsProperty ("gene")) {
				val = (String) feat.getAnnotation ().getProperty ("gene");
			}
			else if (feat.getAnnotation().containsProperty("locus_tag"))
				val = (String) feat.getAnnotation ().getProperty ("locus_tag");
		}
		return val;
	}
	
	public static String getTruncatedDBXrefID (Feature feat, String header) {
		String val = getDBXrefID (feat, header);
		return val.substring(val.indexOf(':') + 1);
	}
	
	
	public static String getDBXrefID (Feature feat, String header) {
		String id = null;
		if (!feat.getAnnotation().containsProperty(DB_XREF))
			return null;
		Object val = feat.getAnnotation ().getProperty (DB_XREF);
		if (val != null) {
			if (val instanceof Collection) {
				Collection ids = (Collection) val;
				Iterator itty = ids.iterator ();
				while (itty.hasNext ()) {
					id = (String) itty.next ();
					if (id.toLowerCase ().indexOf (header) > -1)
						return id;
				}
			}
			else if (val instanceof String) {
				id = (String) val;
				if (id.toLowerCase ().indexOf (header) > -1)
					return id;
			}
			else
				System.out.println ("class " + val.getClass ());
		}
		return null;
			}
	
	public static Collection getDBXrefCollection (Feature feat) {
		Object val = feat.getAnnotation ().getProperty (DB_XREF);
		if (val != null && val instanceof String) {
			ArrayList temp = new ArrayList ();
			temp.add(val);
			val = temp;
		}
		if (!(val instanceof Collection))
			return null;
		else
			return (Collection) val;
	}

	public static Hashtable getContigFeatures (Genome genome) {
		Sequence seq = genome.getAnnotationSequence();
		Iterator itty = seq.features();
		Hashtable all_contigs = new Hashtable (seq.countFeatures());
		while (itty.hasNext()) {
			Object obj = itty.next ();
			if (obj instanceof ComponentFeature) {
				ComponentFeature feat = (ComponentFeature) obj;
				all_contigs.put(new Long (feat.getLocation().getMin()), feat);
			}
		}
		return all_contigs;
	}

}
