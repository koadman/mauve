package org.gel.mauve.analysis.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.biojava.bio.seq.Sequence;
import org.biojavax.bio.seq.RichSequence;
import org.gel.mauve.gui.Mauve;
import org.gel.mauve.gui.MauveFrame;
import org.gel.mauve.module.MauveModule;
import org.gel.mauve.module.ModuleListener;

public class PartialFastaWriter implements ModuleListener {
	
	protected String [] args;
	
	public PartialFastaWriter(String[] args) {
		this.args = args;
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
				PrintStream out = new PrintStream (new FileOutputStream (fout));
				Sequence sequence = frame.getModel().getGenomeBySourceIndex(
						Integer.parseInt(args [ind++])).getAnnotationSequence();
				String name = sequence.getName ();
				if (name == null) {
					name = fout.getName ();
					int ind2 = name.lastIndexOf ('.');
					if (ind2 != -1)
						name = name.substring (0, ind2);
				}
				/*System.out.println ("seq: " + sequence.subList (Integer.parseInt(args [ind++]),
						Integer.parseInt(args [ind++])));*/
				RichSequence dna = RichSequence.Tools.createRichSequence (name, 
						sequence.subList(Integer.parseInt(args [ind++]),
						Integer.parseInt(args [ind++])));
				RichSequence.IOTools.writeFasta(out, dna, dna.getNamespace ());
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

	public static void main (String [] args) {
		PartialFastaWriter writer = new PartialFastaWriter (args);
		MauveModule.mainHook (args, new MauveModule (writer));
	}

}
