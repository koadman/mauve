package org.gel.mauve.contigs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import org.biojava.bio.seq.ComponentFeature;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceTools;
import org.biojava.bio.seq.io.FastaFormat;
import org.biojava.bio.seq.io.SeqIOTools;
import org.biojava.bio.seq.io.StreamWriter;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.gel.air.bioj.ListSequenceIterator;
import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;
import org.gel.mauve.LcbViewerModel;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.MauveHelperFunctions;

public class FastAContigChangeWriter implements MauveConstants {
	
	protected Genome genome;
	protected Hashtable inverters;
	protected Sequence seq;
	protected PrintStream out;
	protected LinkedList ordered;
	protected Hashtable conflicts;
	protected Hashtable all_contigs;
	protected Hashtable nexts;
	protected PrintStream out2;
	protected FastaFormat format;
	protected StreamWriter writer;
	protected StreamWriter writer2;
	
	public FastAContigChangeWriter (ContigReorderer central) {
		genome = central.fix;
		inverters = central.inverters;
		ordered = central.ordered;
		conflicts = central.conflicts;
		nexts = central.nexts;
		File dir = central.directory;
		if (central instanceof ContigRenamer) {
			format = new ChangedFastaFormat (((ContigRenamer) central).names);
		}
		else
			format = new FastaFormat ();
		boolean print_extra = false;
		try {
			String file = MauveHelperFunctions.genomeNameToFasta (genome);
			out = new PrintStream (new FileOutputStream (new File (
					dir, file).getAbsolutePath ()));
			if (print_extra) {
				file = "extra_" +file;
				out2 = new PrintStream (new FileOutputStream (new File (
						dir, file).getAbsolutePath ()));
				writer2 = new StreamWriter (out2, format);
			}
			writer = new StreamWriter (out, format);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		try {
			all_contigs = getContigFeatures (genome);
			writeContigs (ordered.iterator (), false);
			System.out.println ("done with ordered.");
			ArrayList list = new ArrayList (conflicts.keySet ());
			Collections.sort (list);
			writeContigs (list.iterator (), true);
			System.out.println ("done with conflicted.");
			list = new ArrayList (all_contigs.keySet ());
			Collections.sort (list);
			writeContigs (list.iterator (), true, print_extra);
			System.out.println ("done with unordered.");
		} catch (Exception e) {
			e.printStackTrace ();
		}
		out.flush ();
		out.close ();
		if (print_extra) {
			out2.flush ();
			out2.close ();
		}
	}
	
	public void writeContigs (Iterator itty, boolean key) {
		writeContigs (itty, key, false);
	}
	public void writeContigs (Iterator itty, boolean key, boolean print_extra) {
		if (!itty.hasNext())
			return;
		Long start = new Long (-1);
		Object obj = null;
		ListSequenceIterator list = new ListSequenceIterator ();
		ListSequenceIterator list2 = new ListSequenceIterator ();
		long total = 0;
		try {
			while (true) {
				obj = nexts.remove(start);
				if (obj != null)
					System.out.println ("got from table: " + obj);
				if (obj == null && itty.hasNext())
					obj = itty.next();
				if (obj == null)
					break;
				if (obj instanceof Chromosome)
					start = new Long (((Chromosome) obj).getStart ());
				else
					start = (Long) obj;
				//System.out.println ("start: " + start);
				ComponentFeature feat = (ComponentFeature) all_contigs.remove (start);
				if (feat == null) {
					System.out.println ("null feature: " + start);
					continue;
				}
				Chromosome chrom = (Chromosome) inverters.get (start);
				Sequence seq = null;
				if (chrom != null) {
					seq = SequenceTools.reverseComplement (
							feat.getComponentSequence ());
				}
				else {
					seq = feat.getComponentSequence ();
					if (print_extra) {
						total += feat.getLocation ().getMax () - feat.getLocation ().getMin ();
						//SeqIOTools.writeFasta (out2, feat.getComponentSequence ());
						list2.add (feat.getComponentSequence ());
					}
				}
				//SeqIOTools.writeFasta (out, seq);
				list.add (seq);
			}
			System.out.println ("done wirh loop");
			writer.writeStream (list);
			if (out2 != null)
				writer2.writeStream (list2);
		} catch (IllegalAlphabetException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println ("unmatched length: " + total);
	}

/*System.out.println ("a: " + feat.getComponentSequence ().seqString ());
System.out.println ("b: " + two.seqString ());
ComponentFeature.Template template = (ComponentFeature.Template) 
feat.makeTemplate ();
template.componentSequence = two;
seq.removeFeature (feat);
seq.createFeature (template);*/
	
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
