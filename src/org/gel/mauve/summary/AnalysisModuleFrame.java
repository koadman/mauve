package org.gel.mauve.summary;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.analysis.Segment;
import org.gel.mauve.contigs.ContigHandler;
import org.gel.mauve.contigs.DefaultContigHandler;
import org.gel.mauve.gui.Mauve;
import org.gel.mauve.gui.MauveFrame;
import org.gel.mauve.gui.sequence.FlatFileFeatureConstants;
import org.gel.mauve.summary.output.SegmentDataProcessor;

public class AnalysisModuleFrame extends MauveFrame implements FlatFileFeatureConstants,
		ContigHandler {
	
	protected DefaultContigHandler contig_handler;

	public AnalysisModuleFrame (Mauve mauve) {
		super (mauve);
		
	}
	
	public void setModel (BaseViewerModel model) {
		try {
			super.setModel (model);
			contig_handler = new DefaultContigHandler (model);
			final Hashtable args = new Hashtable ();
			File file = model.getSrc ();
			System.out.println ("name: " + file.getName ());
			int end = file.getName ().lastIndexOf ('.');
			if (end > -1)
				file = new File (file.getParentFile (), 
						file.getName ().substring (0, end));
			System.out.println ("name: " + file.getName ());
			args.put (ProcessBackboneFile.INPUT_FILE, file.toString ());
			args.put (MODEL, model);
			args.put (CONTIG_HANDLER, this);
			Iterator itty = MauveInterfacer.feat_files.iterator (); 
			while (itty.hasNext ()) {
				Object [] data = (Object []) itty.next (); 
				importer.importAnnotationFile ((File) data [0], 
						model.getGenomeBySourceIndex (((Integer) data [1]).intValue ()));
			}
			long [] lengths = new long [model.getSequenceCount ()];
			for (int i = 0; i < lengths.length; i++) {
				lengths [i] = model.getGenomeBySourceIndex (i).getLength ();
				System.out.println ("length: " + lengths [i]);
			}
			args.put (GENOME_LENGTHS, lengths);
			new Thread (new Runnable () {
				public void run () {
					ProcessBackboneFile.startProcessor ((String) args.get (
							ProcessBackboneFile.INPUT_FILE), args);
				}
			}).start ();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public long getContigCoord (int sequence, long loci) {
		return contig_handler.getContigCoord(sequence, loci);
	}
	
	public String getContigName (int sequence, long loci) {
		return contig_handler.getContigName(sequence, loci);
	}
	
	

	public long getPseudoCoord(int sequence, long loci, String contig) {
		return contig_handler.getPseudoCoord(sequence, loci, contig);
	}

	public void fixSegmentByContigs (int sequence, Segment segment) {
		Genome genome = model.getGenomeBySourceIndex (sequence);
		Chromosome one = genome.getChromosomeAt (segment.left [sequence]);
		Chromosome end = genome.getChromosomeAt (segment.right [sequence]);
		if (one != end && SegmentDataProcessor.multiplicityForGenome (
				sequence, model.getSequenceCount ()) != segment.multiplicityType ()) {
			System.out.println ("seg: " + segment);
			return;
		}
		//System.out.println ("original: " + segment);
		int part = 1;
		while (one != end) {
			Segment piece = new Segment (segment.left.length, true);
			piece.left [sequence] = segment.left [sequence];
			piece.right [sequence] = one.getEnd ();
			segment.left [sequence] = one.getEnd () + 1;
			piece.reverse [sequence] = segment.reverse [sequence];
			if (segment.prevs [sequence] != null)
				segment.prevs [sequence].nexts [sequence] = piece;
			piece.prevs [sequence] = segment.prevs [sequence];
			segment.prevs [sequence] = piece;
			piece.nexts [sequence] = segment;
			//System.out.println ("part" + part++ + ": " + piece);
			//System.out.println ("part" + part + ": " + segment);
			int ref = sequence + 1;
			/*while (ref != sequence) {
				if (segment.starts [ref] != 0) {
					piece.starts [ref] = segment.starts [ref];
					piece.lengths [ref] = 
				}
			}*/
			one = genome.getChromosomeAt (one.getEnd () + 1);
		}
	}

}
