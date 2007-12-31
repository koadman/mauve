package org.gel.mauve.ext.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.symbol.SymbolList;
import org.biojavax.Namespace;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.BioEntry;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.io.RichStreamWriter;
import org.gel.air.util.MathUtils;
import org.gel.mauve.bioj.SimpleRichFastaFormat;
import org.gel.mauve.gui.Mauve;
import org.gel.mauve.gui.MauveFrame;
import org.gel.mauve.module.MauveModule;
import org.gel.mauve.module.ModuleListener;

public class PartialFastaWriter implements ModuleListener, RichSequenceIterator {
	

	protected String [] args;
	protected Sequence sequence;
	protected Random random;
	protected PrintStream out;
	protected int av_num_contigs = 300;
	protected int avg_bp;
	//
	protected int min_contig_bp = 200;
	//this is actually 1 standard deviation from avg_bp
	protected int max_sd_bp = 100000;
	protected int lower_deviation;
	protected int upper_deviation;
	protected double up_low_cutoff;
	protected int contig_num;
	public static final String NAME_BASE = "Contig";
	protected Hashtable <Integer, RichSequence> contigs;
	protected long total_bps;
	protected Iterator <Integer> keys;
	protected Namespace namespace;
	public static final int UP_LOW_RANGE = 10000;
	
	public PartialFastaWriter(String[] args) {
		this.args = args;
		random = new Random ();
		contig_num = 1;
		contigs = new Hashtable <Integer, RichSequence> (av_num_contigs);
	}

	public void startModule(MauveFrame frame) {
		int ind = 1;
		File fout = null;
		while (ind < args.length) {
			try {
				fout = new File (args [ind++]);
				if (!fout.exists()) {
					fout.getParentFile ().mkdirs ();
					fout.createNewFile();
				}
				out = new PrintStream (new FileOutputStream (fout));
				sequence = frame.getModel().getGenomeBySourceIndex(
						Integer.parseInt(args [ind++])).getAnnotationSequence();
				if (sequence instanceof RichSequence)
					namespace = ((RichSequence) sequence).getNamespace ();
				total_bps = sequence.length ();
				writeFastaContigs ();
				out.flush ();
				out.close ();
			}
			catch (IOException i) {
				System.out.println ("file: " + fout.getAbsolutePath ());
				i.printStackTrace ();
			}
			catch (Exception e) {
				e.printStackTrace();
				//ind -= (ind - 1) % 4;
			}
		}
	}
	
	protected void writeFastaContigs () {
		/*System.out.println ("seq: " + sequence.subList (Integer.parseInt(args [ind++]),
		Integer.parseInt(args [ind++])));*/
		try {
			setUpRanges ();
			boolean done = false;
			int start = 1;
			while (!done) {
				int length = getNextLength ();
				System.out.println ("len: " + length);
				//to increase range; avg. contig size is hemmed by 0 on one side, but has no
				//upper limit
				if (length < 200) {
					System.out.println ("bad length: " + length);
					length = 200;
				}
				if (start + length - 1 > total_bps - 200) {
					length = (int) (total_bps - start) + 1;
					done = true;
				}
				SymbolList seq = sequence.subList(start, start + length - 1);
				int key = random.nextInt();
				while (contigs.get(key) != null)
					key = random.nextInt();
				contigs.put(key, RichSequence.Tools.createRichSequence (
						NAME_BASE + contig_num++, seq));
				start = start + length;
				
			}
			System.out.println ("num_contigs: " + contig_num);
			ArrayList <Integer> list = new ArrayList <Integer> (contigs.keySet());
			Collections.sort (list);
			keys = list.iterator ();
			if (namespace != null)
				RichSequence.IOTools.writeFasta(out, this, namespace);
			else {
				RichStreamWriter writer = new RichStreamWriter (out, 
						new SimpleRichFastaFormat ());
				writer.writeStream(this, namespace);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*sequence.subList(Integer.parseInt(args [ind++]),
		Integer.parseInt(args [ind++])));*/
		/*RichStreamWriter writer = new RichStreamWriter (out,
				new SimpleRichFastaFormat  ());
		SequenceIterator itty = new RichSequence.IOTools.SingleRichSeqIterator (dna);
		writer.writeStream(itty, dna.getNamespace());*/
	}
	
	protected void setUpRanges () {
		avg_bp = sequence.length () / av_num_contigs;
		lower_deviation = avg_bp / 2;
		System.out.println ("lower_dev: " + lower_deviation);
		upper_deviation = max_sd_bp - avg_bp;
		System.out.println ("upper_dev: " + upper_deviation);
		int upper_contigs = (int) ((total_bps - .25*av_num_contigs * lower_deviation) /
				(upper_deviation + avg_bp - .25 * lower_deviation));
		double prct_upper = ((double) upper_contigs) / av_num_contigs;
		up_low_cutoff = UP_LOW_RANGE * prct_upper;
		System.out.println ("cut_off: " + up_low_cutoff);
	}
	
	protected int getNextLength () {
		int dev = 0;
		if (random.nextInt(UP_LOW_RANGE) >= up_low_cutoff) {
			dev = -(avg_bp-Math.abs(MathUtils.gaussianAsRange(random, 
					lower_deviation)));
			if (dev > 0)
				dev = avg_bp - dev;
		}
		else
			dev = Math.abs(MathUtils.gaussianAsRange(random, upper_deviation));
		System.out.println ("deviance: " + dev);
		return avg_bp + dev;
	}
	
	/*
	 * was in constructor, but not necessary now; fields are from constructor
	public String getNameFromSeqOrFile () {
		String name = sequence.getName ();
		if (name == null) {
			name = fout.getName ();
			int ind2 = name.lastIndexOf ('.');
			if (ind2 != -1)
				name = name.substring (0, ind2);
		}
	}*/


	public RichSequence nextRichSequence() throws NoSuchElementException,
	BioException {
		return contigs.get(keys.next());
	}

	public Sequence nextSequence() throws NoSuchElementException, BioException {
		return nextRichSequence ();
	}

	public boolean hasNext() {
		return keys.hasNext();
	}

	public BioEntry nextBioEntry() throws NoSuchElementException, BioException {
		return nextRichSequence ();
	}

	public static void main (String [] args) {
		PartialFastaWriter writer = new PartialFastaWriter (args);
		MauveModule.mainHook (args, new MauveModule (writer));
	}

}
