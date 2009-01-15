package org.gel.mauve.operon;

import gr.zeus.ui.JConsole;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.ComponentFeature;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.StrandedFeature;
import org.gel.air.bioj.BioJavaUtils;
import org.gel.air.util.GroupUtils;
import org.gel.air.util.MathUtils;
import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Genome;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.analysis.AnalysisModule;
import org.gel.mauve.analysis.GuideTree;
import org.gel.mauve.analysis.ProcessBackboneFile;
import org.gel.mauve.analysis.Segment;
import org.gel.mauve.analysis.output.SegmentDataProcessor;
import org.gel.mauve.gui.MauveFrame;
import org.gel.mauve.module.MauveModule;
import org.gel.mauve.module.ModuleListener;

public class OperonHandler implements MauveConstants, ModuleListener {

	protected Hashtable <StrandedFeature, Operon> maps;
	protected Operon [] firsts;
	protected HashSet <Operon> not_fully_aligned;
	//misname and not yet used - non rna or gene features
	protected HashSet <Feature> [] non_aligned;
	protected BaseViewerModel model;
	public static final String RNA = "rna";
	public static final String GENE = "gene";
	public static final String CDS = "cds";
	//represents maximum distance between genes still considered within an operon
	protected int max_within = 75;
	protected Segment [] seg_starts;
	protected File operon_dir;
	//percent more than which is considered an operon or gene is conserved completely
	protected double complete = 95.0;
	//smallest percent considered significant
	protected double min_prct = 5.0;
	//minimum significant base pairs
	protected int min_bp; 
	
	public void startModule(MauveFrame frame) {
		initData (frame.getModel ());
		Hashtable data = new Hashtable ();
		data.put(FILE_STUB, operon_dir.getAbsolutePath());
		for (int i = 0; i < firsts.length; i++)
			findOperons (i, data);
		findOperonMultiplicity (frame);
		//buildOperonTree ();
	}
	
	protected void initData (BaseViewerModel mod) {
		model = mod;
		maps = new Hashtable <StrandedFeature, Operon> ();
		firsts = new Operon [model.getSequenceCount()];
		operon_dir = MauveHelperFunctions.getChildOfRootDir(model,
				OPERON_OUTPUT);
		operon_dir.mkdir();
	}
	
	protected void buildOperonTree () {
		GuideTree genomes = GuideTree.fromBaseModel(model);
		LinkedList <DefaultMutableTreeNode> parents = genomes.getBottomPairs();
		while (parents.size() > 0) {
			LinkedList <DefaultMutableTreeNode> new_rents = new LinkedList <
					DefaultMutableTreeNode> ();
			for (int i = 0; i < parents.size(); i++) {
				DefaultMutableTreeNode parent = genomes.getBottomPairs().get(i);
				for (int j = 0; j < 2; j++) {
					DefaultMutableTreeNode child = (DefaultMutableTreeNode) 
					parent.getChildAt(j);
					child.setUserObject (firsts [((Integer) child.getUserObject(
					)).intValue ()]);
				}
				parent.setUserObject(compareOperons ((DefaultMutableTreeNode) 
						parent.getChildAt(0), (DefaultMutableTreeNode)
						parent.getChildAt (1)));
				if (parent.getParent() != null && !new_rents.contains (
						parent.getParent()))
					new_rents.add((DefaultMutableTreeNode) parent.getParent ());
			}
			parents = new_rents;
		}
	}
		
	protected Operon compareOperons (DefaultMutableTreeNode one, 
			DefaultMutableTreeNode two) {
		HashSet <Operon> handled = new HashSet <Operon> ();
		HashSet <Operon> loose_ends = new HashSet <Operon> ();
		Operon op = (Operon) one.getUserObject();
		int ind1 = getSeqInd (op);
		int ind2 = getSeqInd ((Operon) two.getUserObject());
		Segment start_seg= seg_starts [ind1];
		Segment end_seg = start_seg;
		do {
			while (start_seg.getSegmentEnd(ind1) - min_bp < op.getStart())
				end_seg = start_seg.getNext (ind1);
			while (start_seg.getStart (ind1) < op.getEnd()) {
				if (MathUtils.percentContained(op.getStart(), op.getEnd (), 
						start_seg.getStart (ind1), start_seg.getEnd(ind1)) >= complete) {
					
				}
			}
		} while (op != (Operon) one.getUserObject());
		return null;
	}

	public int getSeqInd (Operon first) {
		int ind = -1;
		for (int i = 0; i < firsts.length; i++) {
			if (first == firsts [i]) {
				ind = i;
				break;
			}
		}
		return ind;
	}
	
	protected void findOperonMultiplicity (MauveFrame frame) {
		AnalysisModule anal = new AnalysisModule (frame);
		Hashtable args = anal.getAnalysisArgs ();
		non_aligned = new HashSet [firsts.length];
		SegmentDataProcessor proc =  ProcessBackboneFile.getProcessor (
				(String) args.get (ProcessBackboneFile.INPUT_FILE), args);
		OperonMultiplicityWriter.initializeVars (proc);
		proc.put(FILE_STUB, new File (operon_dir, MauveHelperFunctions.getFileStub (
				model)).getAbsolutePath());
		seg_starts = (Segment []) proc.get(FIRSTS);
		for (int i = 0; i < firsts.length; i++) {
			frame.getPanel ().getFeatureImporter ().importAnnotationFile (
					new File (operon_dir, "operons_" + i + ".tab"), 
					model.getGenomeBySourceIndex (i));
			proc.setGenomeIndex(i);
			proc.put(BACKBONE_MASK, new Object ());
			non_aligned [i] = new OperonMultiplicityWriter (
					proc).unclear_mults;
			proc.remove(BACKBONE_MASK);
			proc.put(UNCLEAR_MULTS, non_aligned [i]);
			non_aligned [i] = new OperonMultiplicityWriter (
					proc).unclear_mults;
			proc.remove(UNCLEAR_MULTS);
		}
	}

	protected void findOperons (int index, Hashtable data) {
		partition (BioJavaUtils.getSortedStrandedFeatures(model.getGenomeBySourceIndex(
				index).getAnnotationSequence()), index);
		firsts [index] = Operon.first;
		System.out.println ("operons: " + Operon.count);
		Operon.reset ();
		data.put(FIRST_OPERON, firsts [index]);
		String dir = (String) data.get(FILE_STUB);
		new OperonWriter (new File (dir, "operon_genes_" + index + ".tab").getAbsolutePath(),
				data);
		new OperonFeatureWriter (new File (dir, "operons_" + index + ".tab").getAbsolutePath(),
				data);
	}
	
	protected void partition (ArrayList <StrandedFeature> feats, int index) {
		for (int i = 0; i < feats.size(); i++) {
			StrandedFeature feat = feats.get (i);
			Annotation note = feat.getAnnotation();
			String type = feat.getType().toLowerCase();
			if (note != null && (type.indexOf(GENE) > -1 || type.indexOf(CDS)
					> -1) || type.indexOf(RNA) > -1) {
				if (Operon.last == null || !feat.getStrand ().equals(
						Operon.last.genes.getLast().getStrand()) || 
						(type.equals('r' + RNA) && 
						Operon.last.genes.getLast().getType().equals(GENE))) {
					new Operon ();
					Operon.last.addGene(feat, 0);
				}
				else {
					 int distance = feat.getLocation().getMin() - 1 -
						Operon.last.genes.getLast().getLocation().getMax();
					 if (distance > max_within)
						 new Operon ();
					 Operon.last.addGene(feat, distance);
					 maps.put(feat, Operon.last);
				}
			}
		}
		Operon.last.next = Operon.first;
		Operon.first.prev = Operon.last;
		if (Operon.first.genes.getFirst().getStrand().equals(
				Operon.last.genes.getLast().getStrand())) {
			long distance = model.getGenomeBySourceIndex(index).getLength() - 
					Operon.last.genes.getLast().getLocation().getMax();
			distance += Operon.first.genes.getFirst().getLocation().getMin() - 1;
			if (distance < max_within) {
				Operon.last.genes.addAll(Operon.first.genes);
				Operon.first.distances.remove(0);
				Operon.first.distances.addFirst((int) distance);
				Operon.last.distances.addAll(Operon.first.distances);
				Operon.first.next.prev = Operon.last;
				Operon.last.prev.next = Operon.first;
				Operon.last.next = Operon.first.next;
			}
			else {
				Operon.first.distances.removeFirst();
				Operon.first.distances.addFirst((int) distance);
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MauveModule mv = new MauveModule (new OperonHandler ());
		MauveModule.mainHook (args, mv);
	}

}
