package org.gel.mauve;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JMenuItem;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceTools;
import org.biojava.bio.seq.impl.SimpleSequence;
import org.biojava.bio.seq.io.FastaFormat;
import org.biojava.bio.seq.io.StreamWriter;
import org.biojava.bio.symbol.Edit;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.SymbolList;
import org.gel.air.bioj.BioJavaUtils;
import org.gel.air.bioj.ListSequenceIterator;
import org.gel.mauve.contigs.ChangedFeatureWriter;
import org.gel.mauve.contigs.ContigFeatureWriter;
import org.gel.mauve.contigs.ContigReorderer;
import org.gel.mauve.contigs.ChangedFeatureWriter.FeatureReverser;
import org.gel.mauve.contigs.ContigReorderer.ReordererMauveFrame;
import org.gel.mauve.gui.Mauve;
import org.gel.mauve.gui.MauveFrame;
import org.gel.mauve.gui.MauvePanel;
import org.gel.mauve.gui.ProgressiveMauveAlignFrame;
import org.gel.mauve.module.MauveModule;
import org.gel.mauve.module.MauveModuleFrame;
import org.gel.mauve.module.ModuleListener;

public class AlignmentUnreverser implements ModuleListener, FeatureReverser, 
		MauveConstants, ActionListener {
	
	protected MauveAlignmentViewerModel model;
	protected File directory;
	protected File align_file;
	protected String [] seq_files;
	protected FastaFormat format;
	protected LCB [] lcbs;
	protected Hashtable <Feature, LCB> reverses;
	protected Hashtable args; 
	protected JMenuItem trigger;
	protected Mauve mauve;
	public static String REVERSE_DIR = "rev_fastas";
	
	public AlignmentUnreverser (MauveAlignmentViewerModel mod) {
		this ();
		model = mod;
	}
	
	public AlignmentUnreverser (MauvePanel mp, JMenuItem item, Mauve mv) {
		this ((MauveAlignmentViewerModel) mp.getModel());
		trigger = item;
		mauve = mv;
		initFromModel ();
		setMenuText ();
	}
	
	public AlignmentUnreverser () {
		format = new FastaFormat ();
		reverses = new Hashtable <Feature, LCB> ();
		args = new Hashtable ();
		args.put(ContigFeatureWriter.REVERSES, this);
	}
	
	public void startModule (MauveFrame frame) {
		BaseViewerModel mod = (MauveAlignmentViewerModel) frame.getModel();
		for (int i = 0; i < model.getSequenceCount(); i++) {
			Genome nome = mod.getGenomeBySourceIndex(i);
			String feature_file = new File (directory, 
					MauveHelperFunctions.getStrippedName(nome) + 
					FEATURE_EXT).getAbsolutePath();
			if (feature_file != null)
				frame.getPanel ().getFeatureImporter ().importAnnotationFile (new File (
						feature_file), nome);
		}
		if (!trigger.isEnabled()) {
			setMenuText ();
			trigger.setEnabled(true);
		}
	}
	
	public void setMenuText () {
		if (align_file.exists()) {
			trigger.setText("View Forward Alignment");
			trigger.setToolTipText("View alignment with all LCBs flipped to " +
					"forward direction");
		}
		else {
			trigger.setText("Make Forward Alignment");
			trigger.setToolTipText("Create new alignment with DNA in reverse" +
					" LCBs complemented");
		}
	}
	
	public void chooseDirectory () {
		
	}
	
	public void initFromModel () {
		File parent = MauveHelperFunctions.getRootDirectory(model);
		directory = new File (parent, REVERSE_DIR);
		seq_files = new String [model.getSequenceCount()];
		if (!directory.exists())
			directory.mkdir();
		align_file = new File (directory, addRevToName (model.getSrc().getName()));
	}
	
	public String addRevToName (String original_name) {
		StringBuffer name = new StringBuffer (original_name);
		int period = name.indexOf(".");
		if (period == -1)
			period = name.length();
		name.insert(period, "(rev)");
		return name.toString();
	}
	
	public void flipReversedLCBs () {
		lcbs = model.getFullLcbList();
		for (int i = 0; i < model.getSequenceCount(); i++)
			flipReversedLCBs (i);
	}
	
	/*public void flipReversedLCBs (int index) {
		try {
			Genome nome = model.getGenomeBySourceIndex(index);
			Sequence seq = nome.getAnnotationSequence();
			PrintStream ps = new PrintStream (new FileOutputStream (new File (
					directory, MauveHelperFunctions.genomeNameToFasta(
					nome)).getAbsolutePath ()));
			StreamWriter out = new StreamWriter (ps, format);
			LCBLeftComparator comp = new LCBLeftComparator (nome);
			Arrays.sort(lcbs, comp);
			int i = Arrays.binarySearch(lcbs, 1, comp);
			if (i < 0)
				i = -i - 1;
			int first = 0;
			StringBuffer rseq = new StringBuffer ();
			for (; i < lcbs.length; i++) {
				if (lcbs [i].getReverse(nome)) {
					int start = (int) lcbs [i].getStart(index);
					if (start != first)
						rseq.append(seq.subStr(first , start - 1));
					int end = (int) lcbs [i].getEnd(index);
					Sequence rev = SequenceTools.reverseComplement(
							SequenceTools.subSequence(seq, start, end));
					rseq.append(rev.seqString());
					first = end + 1;
				}
			}
			if (first != seq.length())
				rseq.append(seq.subStr(first, seq.length ()));
			ListSequenceIterator lsi = new ListSequenceIterator ();
			//lsi.add(SequenceTools.)
			ps.flush();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	public void flipReversedLCBs (int index) {
		try {
			Genome nome = model.getGenomeBySourceIndex(index);
			Sequence seq = nome.getAnnotationSequence();
			Iterator <Feature> feats = BioJavaUtils.getSortedStrandedFeatures(
					seq).iterator();
			Feature feat = null;
			if (feats.hasNext())
				feat = feats.next();
			seq_files [index] = new File (
					directory, MauveHelperFunctions.genomeNameToFasta(
							nome)).getAbsolutePath ();
			PrintStream ps = new PrintStream (new FileOutputStream (seq_files [index]));
			StreamWriter out = new StreamWriter (ps, format);
			LCBLeftComparator comp = new LCBLeftComparator (nome);
			Arrays.sort(lcbs, comp);
			SymbolList symbols = new SimpleSymbolList (seq);
			for (int i = 0; i < lcbs.length; i++) {
				int start = (int) lcbs [i].getStart(index);
				if (start > 0 && lcbs [i].getReverse(nome)) {
					int end = (int) lcbs [i].getEnd(index);
					SymbolList rev = SequenceTools.reverseComplement(
							SequenceTools.subSequence(seq, start, end));
					symbols.edit(new Edit (start, end - start + 1, rev));
					while (feat != null) {
						boolean half = false;
						Location loci = feat.getLocation();
						if (loci != null) {
							if (loci.getMin() < start) {
								if (loci.getMax() > start)
									half = true;
							}
							else if (loci.getMax() <= end)
								reverses.put(feat, lcbs [i]);
							else if (loci.getMin() <= end)
								half = true;
							else
								break;
							if (half)
								System.out.println ("half reversed feature: " +
										MauveHelperFunctions.getUniqueId(feat));
						}
						if (loci == null || loci.getMin () <= end) {
							if (feats.hasNext())
								feat = feats.next();
							else
								feat = null;
						}
					}
				}
			}
			ListSequenceIterator lsi = new ListSequenceIterator ();
			SimpleSequence rseq = new SimpleSequence (symbols, seq_files [index],
					seq.getName() + "(rev)", null);
			lsi.add(rseq);
			out.writeStream(lsi);
			ps.flush();
			ps.close();
			new ChangedFeatureWriter (new File (directory, 
					MauveHelperFunctions.getStrippedName(nome) + 
					FEATURE_EXT).getAbsolutePath(), args, 
					BioJavaUtils.getSortedStrandedFeatures(
							seq).iterator(), nome);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void makeAlignment () {
		trigger.setEnabled(false);
		flipReversedLCBs ();
		ProgressiveMauveAlignFrame frame = new ProgressiveMauveAlignFrame (
				new ForwardMauve (this, mauve));
		frame.initComponents();
		frame.setOutput(align_file.getAbsolutePath());
		frame.setSequences(seq_files);
		frame.setVisible(true);
	}
	
	public void loadAlignment () {
		new ForwardMauve (this, mauve).loadFile(align_file);
	}
	
	public boolean isReversed (Feature feat, Chromosome chrom, Genome genome) {
		return reverses.containsKey (feat);
	}
	
	public long reverseStart (Feature feat, Chromosome chrom, Genome genome) {
		return reverses.get(feat).getRightEnd(genome);
	}
	
	public long reverseEnd (Feature feat, Chromosome chrom, Genome genome) {
		return reverses.get(feat).getLeftEnd(genome);
	}
	
	public void actionPerformed (ActionEvent e) {
		if (trigger.getText().indexOf("Make") > -1) {
			makeAlignment ();
		}
		else
			loadAlignment ();
	}
	
	public static void main (String [] args) {
		Mauve.mainHook(args, new MauveModule (new AlignmentUnreverser ()));
	}
	
	public class ForwardMauve extends MauveModule {
		
		protected Mauve mauve;
		
		public ForwardMauve (ModuleListener list, Mauve root) {
			super (list);
			mauve = root;
			frames = mauve.getFrames ();
		}
		
		protected MauveFrame makeNewFrame () {
			MauveFrame frame = new MauveModuleFrame (mauve, this);
			frames.add (frame);
			return frame;
		}
	}

}
