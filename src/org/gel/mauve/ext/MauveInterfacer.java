package org.gel.mauve.ext;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.biojavax.bio.seq.RichSequence;
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

public class MauveInterfacer implements ModuleListener, MauveConstants {
	
	protected MauveModule mauve;
	
	public MauveInterfacer (String [] args) {
		mauve = new MauveModule (this);
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
				new AlignmentConverter ((XmfaViewerModel) frame.getModel());		
			}
		}).start ();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new MauveInterfacer (args);
	}

}
