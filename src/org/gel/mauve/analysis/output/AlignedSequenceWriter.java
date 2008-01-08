package org.gel.mauve.analysis.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Vector;

import org.gel.mauve.MauveAlignmentViewerModel;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.analysis.ProcessBackboneFile;
import org.gel.mauve.analysis.Segment;
import org.gel.mauve.analysis.SegmentComparator;
import org.gel.mauve.backbone.BackboneListBuilder;
import org.gel.mauve.gui.MauveFrame;
import org.gel.mauve.module.MauveModule;
import org.gel.mauve.module.ModuleListener;

/**
 * writes out fastas for each genome containing only those sections that aligned to
 * a part of the specified genome.
 * 
 * @author rissman
 *
 */
public class AlignedSequenceWriter implements ModuleListener {
	
	/**
	 * index of the genome written sequence must align to
	 */
	protected int genome_ind;
	
	/**
	 * reference to model that describes alignment
	 */
	MauveAlignmentViewerModel model;
	
	/**
	 * the directory to output the fastas to
	 */
	String directory;
	
	Vector backbone;
	/**
	 * sets class variables and starts write process
	 * @param args
	 */
	public AlignedSequenceWriter (String [] args) {
		genome_ind = Integer.parseInt(args [1]);
		directory = MauveHelperFunctions.getChildOfRootDir (model, 
				model.getGenomeBySourceIndex(genome_ind).getDisplayName()
				+ "_aligned_fastas").getAbsolutePath();
	}

	/**
	 * called when MauveFrame and model are instantiated, starts write process.
	 */
	public void startModule(MauveFrame frame) {
		model = (MauveAlignmentViewerModel) frame.getModel ();
		backbone = new ProcessBackboneFile (BackboneListBuilder.getBbFile(model, 
				model.getAlignment()).getAbsolutePath()).getBackboneSegments();
		pareContigs ();
		for (int i = 0; i < model.getSequenceCount(); i++) {
			SegmentComparator comp = new SegmentComparator (i);
			Collections.sort(backbone, comp);
			writeFasta (i);
		}

	}
	
	/**
	 * removes the part of backbone segments that are too close to the end of a contig in one
	 * of the genomes
	 *
	 */
	protected void pareContigs () {
		for (int g = 0; g < model.getSequenceCount (); g++) {
			for (int i = 0; i < backbone.size (); i++) {
				Segment seg = (Segment) backbone.get(i);
				if (useSegment (seg, g)) {
					
				}
			}
		}
	}
	
	public boolean useSegment (Segment seg, int index) {
		return seg.starts [index] != 0 && seg.starts [genome_ind] != 0;
	}
	
	/**
	 * writes a Fasta for a genome including only those segments aligned to a part of
	 * the genome specified by class variable genome_ind.
	 * 
	 * @param index			the index of the genome whose sequence should be printed
	 */
	public void writeFasta (int index) {
		try {
			PrintStream out = new PrintStream(new FileOutputStream(new File(
					directory, model.getGenomeBySourceIndex(index)
							.getDisplayName() + "_cut.fas")));
			for (int i = 0; i < backbone.size (); i++) {
				Segment segment = (Segment) backbone.get (i);
				if (useSegment (segment, index)) {
					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	/**
	 * First arg should be name of alignment file, second the genome of interest
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		new MauveModule (new AlignedSequenceWriter (args)).init (args [0]);
	}

}
