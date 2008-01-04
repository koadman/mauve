package org.gel.mauve.ext;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;

import org.biojavax.bio.seq.RichSequence;
import org.gel.air.ja.stash.Stash;
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
	protected Hashtable loaded_alignments;
	
	public MauveInterfacer (String [] args) {
		mauve = new MauveModule (this);
		loaded_alignments = new Hashtable ();
		loader = new StashLoader (null, null);
		loader.loadAll(new File (data_root_dir, "mauve_defaults.xml"));
		if (args.length > 0)
			args [0] = loadAlignment (args [0]);
		Mauve.mainHook(args, mauve);
	}
	
	/**
	 * 
	 * @param align_id
	 * @return		A reference to the wrapper file it makes for loading in Mauve
	 */
	public String loadAlignment (String align_id) {
		try {
			Stash alignment = loader.getStash (align_id);
			File dir = File.createTempFile("a", "z");
			dir.delete();
			dir.mkdir();
			dir = new File (dir, alignment.getString(NAME));
			PrintStream out = new PrintStream (new FileOutputStream (dir));
			out.println (MAUVE_COMMENT_SYMBOL + FORMAT_VERSION + "\t-2");
			out.println (MAUVE_COMMENT_SYMBOL + ALIGNMENT_CLASS + "\t" + align_id);
			out.close();
			return dir.getAbsolutePath();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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
		data_root_dir = "c:\\mauve3data\\DataStore";
		makeDataDirs ();
		new MauveInterfacer (args);
	}

}
