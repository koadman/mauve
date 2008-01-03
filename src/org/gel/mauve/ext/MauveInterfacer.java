package org.gel.mauve.ext;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.biojavax.bio.seq.RichSequence;
import org.gel.air.ja.stash.StashLoader;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.SimilarityIndex;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.ext.io.AlignmentConverter;
import org.gel.mauve.ext.lazy.StashedZHistogram;
import org.gel.mauve.gui.Mauve;
import org.gel.mauve.gui.MauveFrame;
import org.gel.mauve.module.MauveModule;
import org.gel.mauve.module.ModuleListener;

public class MauveInterfacer implements ModuleListener, MauveStoreConstants {
	
	protected MauveModule mauve;
	protected StashLoader loader;
	protected static String data_root_dir;
	
	public MauveInterfacer (String [] args) {
		mauve = new MauveModule (this);
		loader = new StashLoader (null, null);
		Mauve.mainHook(args, mauve);
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
	
	public static void readSimilarity (XmfaViewerModel model, int i) {
		try {
			StashedZHistogram sim = new StashedZHistogram ((makeSimFile (model, i)));
			model.setSim (model.getGenomeBySourceIndex(i), sim);
			System.out.println ("set sim: " + i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean writeSimilarity (XmfaViewerModel model, int i) {
		return StashedZHistogram.outputHistogram ((SimilarityIndex) 
				model.getSim(model.getGenomeBySourceIndex(i)), makeSimFile (model, i));
	}
	
	public static String makeSimFile (XmfaViewerModel model, int genome) {
		return new File (MauveHelperFunctions.getChildOfRootDir (model, 
				SIMILARITY_OUTPUT), genome + SIMILARITY_EXT).getAbsolutePath();
	}

	public void startModule(final MauveFrame frame) {
		//convertToINSD (frame);
		new Thread (new Runnable () {
			public void run () {
				//loader.loadAll(new File (data_root_dir, "mauve_defaults.xml"));
				new AlignmentConverter ((XmfaViewerModel) frame.getModel(), loader);		
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
		data_root_dir = "c:\\mauve3data\\DataStore";
		makeDataDirs ();
		new MauveInterfacer (args);
	}

}
