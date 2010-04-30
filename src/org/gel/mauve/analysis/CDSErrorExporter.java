package org.gel.mauve.analysis;
 
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.symbol.AtomicSymbol;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.SymbolListFactory;
import org.gel.mauve.XmfaViewerModel;

/**
 * A class for extracting broken CDS features from a draft genome. 
 * 
 * @author atritt
 *
 */
public class CDSErrorExporter {

	/**
	 * A mapping of CDS ids to vectors containing 
	 * a list of all errors found in that CDS
	 */
	private HashMap<LiteWeightFeature,Vector<SNP>> snpErrors;
	
	private HashMap<LiteWeightFeature,Vector<Gap>> gapErrors;
	
	private XmfaViewerModel model;
	
	private HashMap<LiteWeightFeature,BrokenCDS> cds; 
	
	
	public CDSErrorExporter(XmfaViewerModel model, SNP[] snps, Gap[] assGaps){
		snpErrors = new HashMap<LiteWeightFeature,Vector<SNP>>();
		gapErrors = new HashMap<LiteWeightFeature,Vector<Gap>>();
		this.model = model;
		cds = new HashMap<LiteWeightFeature, BrokenCDS>();
		loadBrokenCDS(model, snps, assGaps);
		loadSnpErrors();
		loadGapErrors();
	}
	
	public BrokenCDS[] getBrokenCDS(){
		return cds.values().toArray(new BrokenCDS[cds.size()]);
	}
	
	/**
	 * 
	 * @param model
	 * @param snpsAr
	 * @param assGaps gaps in the assembly
	 * @return
	 */
	private void loadBrokenCDS(XmfaViewerModel model, SNP[] snpsAr, Gap[] assGaps){
		if (model.getGenomes().size() > 2)
			return;
		LiteWeightFeature[] cds = OneToOneOrthologExporter.getFeaturesByType(0,
				model.getGenomeBySourceIndex(0).getAnnotationSequence().features(), "CDS");
		SNP[] snps = new SNP[snpsAr.length];
		System.arraycopy(snpsAr, 0, snps, 0, snps.length);
		Arrays.sort(snps, SNP.getLoopingComparator(0));
		Arrays.sort(cds, LiteWeightFeature.getLoopingComparator());
		int snpI = 0;
		int cdsI = 0;
		while (cdsI < cds.length && snpI < snps.length){
			int comp = snps[snpI].relativePos(cds[cdsI]);
			if (comp > 0){
				cdsI++;
			} else if (comp < 0){
				snpI++;
			} else {// add this SNP and get the next one
				if (snpErrors.containsKey(cds[cdsI])) {
					snpErrors.get(cds[cdsI]).add(snps[snpI]);
				} else {
					Vector<SNP> v = new Vector<SNP>();
					snpErrors.put(cds[cdsI], v);
				}
				snpI++;
			}
		}
		
		Gap[] gaps = new Gap[assGaps.length];
		System.arraycopy(assGaps, 0, gaps, 0, gaps.length);
		Arrays.sort(gaps, Gap.getAlnmtPosComparator());
		int gapI = 0;
		cdsI = 0;
		while (cdsI < cds.length && gapI < gaps.length){
			int comp = gaps[gapI].relativePos(cds[cdsI]);
			if (comp > 0){
				cdsI++;
			} else if (comp < 0) {
				gapI++;
			} else {
				if (gapErrors.containsKey(cds[cdsI])){
					gapErrors.get(cds[cdsI]).add(gaps[gapI]);
				} else {
					Vector<Gap> v = new Vector<Gap>();
					gapErrors.put(cds[cdsI], v);
				}
				gapI++;
			}
		}
	}
	
	private void loadGapErrors(){
		Iterator<LiteWeightFeature> it = gapErrors.keySet().iterator();
		while (it.hasNext()) {
			LiteWeightFeature feat = it.next();
			byte[][] alnmt = model.getRange(0, feat.getLeft(), feat.getRight());
			
		}
	}
	
	//public byte[] subArray(byte[] in, )
	
	public boolean sameAA(byte[] s1, byte[] s2){
		String str1 = new String(s1);
		String str2 = new String(s2);
		boolean same = false;
		try {
			Symbol sym1 = RNATools.translate(
							DNATools.toRNA(
							DNATools.createDNA(str1))).symbolAt(0);
			Symbol sym2 = RNATools.translate(
							DNATools.toRNA(
							DNATools.createDNA(str2))).symbolAt(0);
			same = sym1.equals(sym2);
		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAlphabetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalSymbolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return same;
	}
	
	
	
	private void loadSnpErrors(){
		Iterator<LiteWeightFeature> it = snpErrors.keySet().iterator();
		while(it.hasNext()){
			LiteWeightFeature feat = it.next();
			if (gapErrors.containsKey(feat)){
				continue;
			} else {
				char[] refSeq = model.getSequence(feat.getLeft(), feat.getRight(), 0);
				long[] leftLCB = model.getLCBAndColumn(0, feat.getLeft());
				long[] rightLCB = model.getLCBAndColumn(0,feat.getRight());
				long[] left_pos = new long[model.getGenomes().size()];
				boolean[] gap = new boolean[leftLCB.length];
				long[] right_pos = new long[model.getGenomes().size()];
				model.getColumnCoordinates((int)leftLCB[0], leftLCB[1], left_pos, gap);
				model.getColumnCoordinates((int)rightLCB[0], rightLCB[1], right_pos, gap);
				if (left_pos[1] == 0 || right_pos[1] == 0)
					continue;
				else {
					char[] assSeq = model.getSequence(left_pos[1], right_pos[1], 1);
					computeSubstitutions(feat,refSeq,assSeq);
				}
			}
		}
	}
	
	private void computeSubstitutions(LiteWeightFeature feat, char[] refSeq, char[] assSeq){
		if (refSeq.length != assSeq.length){
			throw new IllegalArgumentException("Sequences must be the same length"); 
		}
		try {
			char[] tmp = RNATools.translate(
					  	 DNATools.toRNA(
					  	 DNATools.createDNA(new String(refSeq))))
					  	 .toString()
					  	 .toCharArray();
			refSeq = tmp;
		} catch (IllegalSymbolException e) {
			System.err.println(e.getMessage());
			System.err.println("Bad Symbol in the reference sequence: \n >>" + new String(assSeq)+"<<");
			e.printStackTrace();
		} catch (IllegalAlphabetException e) {
			e.printStackTrace();
		}
		try {
			char[] tmp = RNATools.translate(
					 DNATools.toRNA(
					 DNATools.createDNA(new String(assSeq))))
					 .toString()
					 .toCharArray();
			assSeq = tmp;
		} catch (IllegalSymbolException e) {
			System.err.println(e.getMessage());
			System.err.println("Bad Symbol in the assembly sequence: \n >>" + new String(assSeq)+"<<");
			e.printStackTrace();
		} catch (IllegalAlphabetException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < assSeq.length; i++){
			if (assSeq[i] != refSeq[i]) {
				if (assSeq[i] == '*') {
					if (cds.containsKey(feat)){
						cds.get(feat).addPrmtrStop(i+1, refSeq[i]);
					} else {
						BrokenCDS tmp = new BrokenCDS(feat);
						tmp.addPrmtrStop(i+1, refSeq[i]);
						cds.put(feat, tmp);
					}
				} else {
					if (cds.containsKey(feat)){
						cds.get(feat).addSubstitution(i+1, refSeq[i], assSeq[i]);
					} else {
						BrokenCDS tmp = new BrokenCDS(feat);
						tmp.addSubstitution(i+1, refSeq[i], assSeq[i]);
						cds.put(feat, tmp);
					}
				}
			}                           
		}
	}
}
