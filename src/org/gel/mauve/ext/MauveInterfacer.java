package org.gel.mauve.ext;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;

import org.biojavax.bio.seq.RichSequence;
import org.gel.air.ja.stash.Stash;
import org.gel.air.ja.stash.StashLoader;
import org.gel.air.ja.stash.events.XMLUpdateEvents;
import org.gel.mauve.MauveAlignmentViewerModel;
import org.gel.mauve.ModelBuilder;
import org.gel.mauve.SimilarityIndex;
import org.gel.mauve.ext.lazy.StashViewerModel;
import org.gel.mauve.ext.lazy.StashedZHistogram;
import org.gel.mauve.gui.MauveFrame;
import org.gel.mauve.module.MauveModule;
import org.gel.mauve.module.ModuleListener;

public class MauveInterfacer implements ModuleListener, MauveStoreConstants {
	
	protected MauveModule mauve;
	protected static StashLoader loader;
	protected static String data_root_dir;
	protected Hashtable loaded_alignments;
	protected String alignment_id;
	
	public MauveInterfacer (String [] args) {
		mauve = new MauveModule (this);
		loaded_alignments = new Hashtable ();
		loader.loadAll(data_root_dir, "mauve_defaults.xml");
		Stash stash = loader.getStash("Alignment\\66.188.103.18711998221257340");
		System.out.println ("null? " + (stash == null));
		if (args.length > 0) {
			alignment_id = args [0];
			args = new String [0];
		}
		ModelBuilder.registerModel(StashViewerModel.factory);
		Hashtable src = new Hashtable ();
		src.put(ALIGNMENT, stash);
		src.put(LOADER, loader);
		mauve.init(src, StashViewerModel.factory.getUniqueName());
		//mauve.init(args [0]);
	}
	
	//doesn't work; doesn't finish writing; doesn't return; don't know why
	public void convertToINSD (MauveFrame frame) {
		try {
			RichSequence seq = RichSequence.Tools.enrich(
					frame.getModel().getGenomeBySourceIndex(0).getAnnotationSequence());
			/*SequenceIterator seq = RichSequence.IOTools.readINSDseqDNA(
					new BufferedReader(new FileReader(
							"c:\\mauvedata\\formattest\\"
									+ "sequences.gbc")), null);*/
			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream("c:\\out.gbk"));
			RichSequence.IOTools.writeGenbank(out, seq, seq.getNamespace());
			out.flush();
			out.close();
			System.out.println("done");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void readSimilarity (MauveAlignmentViewerModel model, 
			Stash genome, int i) {
		try {
			StashedZHistogram sim = new StashedZHistogram ((makeSimFile (genome, i)));
			model.setSim (model.getGenomeBySourceIndex(i), sim);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean writeSimilarity (MauveAlignmentViewerModel model, 
			Stash genome, int i) {
		return StashedZHistogram.outputHistogram ((SimilarityIndex) 
				model.getSim(model.getGenomeBySourceIndex(i)), makeSimFile (genome, i));
	}
	
	public static String makeSimFile (Stash aligned_genome, int genome) {
		return loader.getFileByID(aligned_genome.getString (ID) + ".sim").getAbsolutePath();
	}
	
	public static StashLoader getLoader () {
		return loader;
	}

	public void startModule(final MauveFrame frame) {
		//convertToINSD (frame);
		new Thread (new Runnable () {
			public void run () {
				//new AlignmentConverter ((XmfaViewerModel) frame.getModel(), loader);		
			}
		}).start ();
	}
	
	public static void makeDataDirs () {
		File dir = new File (data_root_dir);
		String [] subs = new String [] {GENOME_CLASS, ALIGNED_GENOME_CLASS, 
				ALIGNMENT_CLASS, FEATURE_INDEX_CLASS};
		for (int i = 0; i < subs.length; i++) {
			File sub = new File (dir, subs [i]);
			if (!sub.exists())
				sub.mkdir();
		}
	}

	public static String getStashDir () {
		return data_root_dir;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ModelBuilder.setUseDiskCache(false);
		data_root_dir = "c:\\mauve3data\\DataStore";
		makeDataDirs ();
		loader = new StashLoader (data_root_dir, new XMLUpdateEvents ());
		new MauveInterfacer (args);
	}

}
