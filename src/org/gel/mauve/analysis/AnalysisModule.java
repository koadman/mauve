package org.gel.mauve.analysis;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.contigs.ContigHandler;
import org.gel.mauve.contigs.DefaultContigHandler;
import org.gel.mauve.gui.Mauve;
import org.gel.mauve.gui.MauveFrame;
import org.gel.mauve.gui.sequence.FlatFileFeatureConstants;

public class AnalysisModule implements FlatFileFeatureConstants,
		ContigHandler {
	
	protected DefaultContigHandler contig_handler;
	protected BaseViewerModel model;
	protected MauveFrame frame;

	public AnalysisModule (BaseViewerModel model) {
		this.model = model;
	}
	
	public AnalysisModule (MauveFrame fr) {
		this (fr.getModel ());
		frame = fr;
	}
	
	
	
	protected void doAnalysis () {
		try {
			final Hashtable args = getAnalysisArgs ();
			new Thread (new Runnable () {
				public void run () {
					ProcessBackboneFile.getProcessor ((String) args.get (
							ProcessBackboneFile.INPUT_FILE),
							args).printAllAnalysisData ();
				}
			}).start ();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Hashtable getAnalysisArgs () {
		contig_handler = new DefaultContigHandler (model);
		final Hashtable args = new Hashtable ();
		File file = model.getSrc ();
		args.put (ProcessBackboneFile.INPUT_FILE, file.toString ());
		args.put (MODEL, model);
		args.put (CONTIG_HANDLER, this);
		long [] lengths = new long [model.getSequenceCount ()];
		for (int i = 0; i < lengths.length; i++) {
			lengths [i] = model.getGenomeBySourceIndex (i).getLength ();
			System.out.println ("length: " + lengths [i]);
		}
		args.put (GENOME_LENGTHS, lengths);
		return args;
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
		Chromosome one = genome.getChromosomeAt (segment.starts [sequence]);
		Chromosome end = genome.getChromosomeAt (segment.ends [sequence]);
		if (one != end && MauveHelperFunctions.multiplicityForGenome (
				sequence, model.getSequenceCount ()) != segment.multiplicityType ()) {
			System.out.println ("seg: " + segment);
			return;
		}
		//System.out.println ("original: " + segment);
		int part = 1;
		while (one != end) {
			Segment piece = new Segment (segment.starts.length, true, true);
			piece.starts [sequence] = segment.starts [sequence];
			piece.ends [sequence] = one.getEnd ();
			segment.starts [sequence] = one.getEnd () + 1;
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
