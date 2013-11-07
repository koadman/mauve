package org.gel.mauve.assembly;

import java.io.File;


import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;
import org.gel.mauve.LCB;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.List;

import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.analysis.BrokenCDS;
import org.gel.mauve.analysis.CDSErrorExporter;
import org.gel.mauve.analysis.Gap;
import org.gel.mauve.analysis.PermutationExporter;
import org.gel.mauve.analysis.SNP;
import org.gel.mauve.analysis.SnpExporter;
import org.gel.mauve.contigs.ContigOrderer;
import org.gel.mauve.dcjx.Adjacency;
import org.gel.mauve.dcjx.Block;
import org.gel.mauve.dcjx.DCJ;
import org.gel.mauve.gui.AlignmentProcessListener;


public class AssemblyScorer implements AlignmentProcessListener {

	private ContigOrderer co;
	private File alnmtFile;
	private File outputDir;
	private boolean batch;
	private String basename;
	private XmfaViewerModel model; 
	private int[][] subs;
	private SNP[] snps;
	private Gap[] refGaps;
	private Gap[] assGaps; 
	private Chromosome[] extraCtgs;
	private Chromosome[] missingChroms;
	private BrokenCDS[] brokenCDS;
	private CDSErrorExporter cdsEE;
	private DCJ dcj;
	private boolean getBrokenCDS = true;
	
	private int numSharedBnds;
	
	private int numInterLcbBnds;
	
	/** extra adjacencies */
	private Vector<Adjacency> typeI;
	
	/** missing adjacencies */
	private Vector<Adjacency> typeII;
	private Vector<Chromosome> invCtgs;
	private Map<Chromosome,Integer> misAsm;
	private int numMisAssemblies;
	
	private int miscalled;
	private int uncalled;

	/** contig stats */
	private long maxContigLength = -1;
	private long contigN50 = -1;
	private long contigN90 = -1;
	private long minContigLength = -1;

	private static int A = 0;
	private static int C = 1;
	private static int T = 2;
	private static int G = 3;
	

	public AssemblyScorer(XmfaViewerModel model){
		this.model = model;
		loadInfo();
	}
	
	public AssemblyScorer(File alnmtFile, File outDir) {
		this.alnmtFile = alnmtFile;
		this.outputDir = outDir;
		basename = alnmtFile.getName();
		batch = false;
	}
	
	public AssemblyScorer(File alnmtFile, File outDir, String basename) {
		this(alnmtFile,outDir);
		this.basename = basename;
	}
	
	public AssemblyScorer(ContigOrderer co, File outDir) {
		this.co = co;
		this.outputDir = outDir;
		batch = false;
	}
	
	public AssemblyScorer(ContigOrderer co, File outDir, String basename) {
		this(co,outDir);
		this.basename = basename;
	}
	
	public void completeAlignment(int retcode){
		if (retcode == 0) {
			if (co != null){
				alnmtFile = co.getAlignmentFile();
				if (basename == null) {
					basename = alnmtFile.getName();
				}
			}
			try {
				this.model = new XmfaViewerModel(alnmtFile,null);
			} catch (IOException e) {
				System.err.println("Couldn't load alignment file " 
									+ alnmtFile.getAbsolutePath());
				e.printStackTrace();
			}
			if (model != null) {
				loadInfo();
				printInfo(this, outputDir, basename, batch);
			}
			
		} else { 
			System.err.println("Alignment failed with error code "+ retcode);
		}
			
	}
	
	private void computeAdjacencyErrors(){
		Adjacency[] ref = dcj.getAdjacencyGraph().getGenomeA();
		Adjacency[] ass = dcj.getAdjacencyGraph().getGenomeB();
		Comparator<Adjacency> comp = new Comparator<Adjacency>(){
			public int compare(Adjacency o1, Adjacency o2) {
				String s1 = new String(min(o1.getFirstBlockEnd(),o1.getSecondBlockEnd()));
				s1 = s1 + max(o1.getFirstBlockEnd(),o1.getSecondBlockEnd());
				String s2 = new String(min(o2.getFirstBlockEnd(),o2.getSecondBlockEnd()));
				s2 = s2 + max(o2.getFirstBlockEnd(),o2.getSecondBlockEnd());
				return s1.compareTo(s2);
			}
			private String min(String s1, String s2){
				if (s1.compareTo(s2) > 0)
					return s2;
				else 
					return s1;
			}
			private String max(String s1, String s2){
				if (s1.compareTo(s2) > 0)
					return s1;
				else 
					return s2;
			}
		};
		// false positives : adjacencies in assembly that aren't in the reference
		typeI = new Vector<Adjacency>();
		// false negatives : adjacencies in reference that aren't in the assembly
		typeII = new Vector<Adjacency>();
		
		TreeSet<Adjacency> refSet = new TreeSet<Adjacency>(comp);
		for (Adjacency a: ref) {
			refSet.add(a);
		}
		
	//	System.err.println("\nnum assembly adjacencies: " + ass.length);
		Vector<Adjacency> intersection = new Vector<Adjacency>(); 
		for (Adjacency a: ass){
			if (!refSet.contains(a)) 
				typeI.add(a);
			else
				intersection.add(a);
		}

	//	System.err.println("num typeI errors: " + typeI.size());
		
		TreeSet<Adjacency> assSet = new TreeSet<Adjacency>(comp);
		for (Adjacency a: ass) { assSet.add(a);}
		
	//	System.err.println("num ref adjacencies: " + ref.length);
		
		for (Adjacency a: ref) {
			if (!assSet.contains(a))
				typeII.add(a);
		}
	//	System.err.println("num typeII errors: " + typeII.size());
		
	/*	Iterator<Adjacency> it = intersection.iterator();
		while(it.hasNext()){
			System.err.println(it.next().toString());
		}*/
	}
	
	/**
	 * computes info. sorts gaps and snps
	 */
	private synchronized void loadInfo(){
		model.setReference(model.getGenomeBySourceIndex(0));
		
		System.out.print("Counting shared bounds between contigs/chromosomes and LCBs...");
		numSharedBnds = PermutationExporter.getSharedBoundaryCount(model);
		System.out.print("done!\n");
		
		System.out.print("Counting interLCB contig/chromosome boundaries...");
		numInterLcbBnds = PermutationExporter.countInterBlockBounds(model);
		System.out.print("done!\n");
		
		System.out.print("Computing signed permutations...");
		String[] perms = PermutationExporter.getPermStrings(model, true); 
		System.out.print("done!\n");
		
		//System.out.println("Permutations: ");
		//System.out.println(perms[0]);
		//System.out.println(perms[1]);
		
		System.out.print("Performing DCJ rearrangement analysis...");
		this.dcj = new DCJ(perms[0], perms[1]);
		System.out.print("done!\n");
		
		System.out.print("Computing adjacency errors...");
		computeAdjacencyErrors();
		System.out.print("done!\n");
		
		System.out.print("Getting SNPs...");
		this.snps = SnpExporter.getSNPs(model);
		System.out.print("done!\n");
		
		System.out.print("Counting base substitutions...");
		this.subs = AssemblyScorer.countSubstitutions(snps);
		summarizeBaseCalls();
		System.out.print("done!\n");
		
		System.out.print("Counting gaps...");
		Gap[][] tmp = SnpExporter.getGaps(model);
		System.out.print("done!\n");
		
		System.out.print("Counting extra contigs...");
		Chromosome[][] unique = SnpExporter.getUniqueChromosomes(model);
		System.out.print("done!\n");
		
		computeContigSizeStats();
		
		refGaps = tmp[0];
		assGaps = tmp[1];
		Arrays.sort(assGaps);
		Arrays.sort(refGaps);
		missingChroms = unique[0];
		extraCtgs = unique[1];
		System.out.flush();
		if (getBrokenCDS){
			computeBrokenCDS();
		}
	}
	
	private void computeContigSizeStats(){
		List<Chromosome> chromos = model.getGenomeBySourceIndex(1).getChromosomes();
		long[] sizer = new long[chromos.size()];
		int i=0;
		long sum = 0;
		for(Chromosome c : chromos){
			sizer[i++] = c.getLength();
			sum += c.getLength();
		}
		
		Arrays.sort(sizer);
		minContigLength = sizer[0];
		maxContigLength = sizer[sizer.length-1];
		long cur = 0;
		for(i=sizer.length-1; i>=0 && cur*2 < sum; i--){
			cur += sizer[i];
		}
		contigN50 = sizer[i+1];
		i = 0;
		for(; i>0 && cur < sum*0.9d;){
			cur += sizer[--i];
		}
		contigN90 = sizer[i];
	}
	
	
	public void summarizeBaseCalls(){
		int subsum = 0;
		for(int i=0; i<subs.length;i++){
			for(int j=0; j<subs.length;j++)
				subsum += subs[i][j];
		}
		this.miscalled = subsum;
		this.uncalled = snps.length - subsum;
	}

	private void computeBrokenCDS(){
		Iterator<Genome> it = model.getGenomes().iterator();
		boolean haveAnnotations = true;
		while(it.hasNext()){
			haveAnnotations = haveAnnotations &&
				(it.next().getAnnotationSequence() != null);
		}
		haveAnnotations = model.getGenomeBySourceIndex(0).getAnnotationSequence() != null;
		if (haveAnnotations){
			System.out.print("Getting broken CDS...");
			System.out.flush();
			this.cdsEE = new CDSErrorExporter(model, snps, assGaps, refGaps);
			try {
				brokenCDS = cdsEE.getBrokenCDS();
				System.out.print("done!\n");
			} catch (Exception e){
				System.err.println("\n\nfailed to compute broken CDS. Reason given below");
				System.err.println(e.getMessage());
				e.printStackTrace();
			} 
		}
	}
	
	public int getMiscalled() {
		return miscalled;
	}

	public void setMiscalled(int miscalled) {
		this.miscalled = miscalled;
	}

	public int getUncalled() {
		return uncalled;
	}

	public void setUncalled(int uncalled) {
		this.uncalled = uncalled;
	}

	public XmfaViewerModel getModel(){
		return this.model;
	}

	public DCJ getDCJ(){
		return dcj;
	}
	
	public Gap[] getReferenceGaps(){
		return refGaps;
	}
	
	public Gap[] getAssemblyGaps(){
		return assGaps;
	}
	
	public Chromosome[] getExtraContigs(){
		return extraCtgs;
	}
	
	public Chromosome[] getMissingChromosomes(){
		return missingChroms;
	}
	
	public SNP[] getSNPs(){
		return snps;
	}
	
	public int[][] getSubs(){
		return subs;
	}
	
	public boolean hasBrokenCDS(){
		if (brokenCDS == null) 
			return false;
		else {
			return brokenCDS.length > 0;
		}
	}
	
	public BrokenCDS[] getBrokenCDS(){
		return brokenCDS;
	}
	
	public int numBrokenCDS(){
		return cdsEE.numBrokenCDS();
	}
	
	public int numCompleteCDS(){
		return cdsEE.numCompleteCDS();
	}
	
	public int getSCJdist(){
		return dcj.scjDistance();
	}
	
	public int getDCJdist(){
		return dcj.dcjDistance();
	}
	
	public int getBPdist(){
		return dcj.bpDistance();
	}
	
	public int numBlocks(){
		return dcj.numBlocks();
	}

	public double typeIadjErr(){
		return ((double) typeI.size()) / 
			((double) dcj.getAdjacencyGraph()
						 .getGenomeB().length);
	}
	
	public double typeIIadjErr(){
		return ((double) typeII.size()) /
			((double) dcj.getAdjacencyGraph()
						 .getGenomeA().length);
	}
	
	public Map<Chromosome, Integer> getMisAssemblies(){
		return misAsm;
	}
	
	public Chromosome[] getInverted(){
		return invCtgs.toArray(new Chromosome[invCtgs.size()]);
	}
	
	public int numLCBs(){
		return (int) model.getLcbCount();
	}
	
	public int numContigs(){
		return model.getGenomeBySourceIndex(1).getChromosomes().size();
	}

	public long numReplicons(){
		return model.getGenomeBySourceIndex(0).getChromosomes().size();
	}
	
	
	public long numBasesAssembly(){
		return model.getGenomeBySourceIndex(1).getLength();
	}
	
	public long numBasesReference(){
		return model.getGenomeBySourceIndex(0).getLength();
	}
	
	public double percentMissedBases(){
		double totalBases = model.getGenomeBySourceIndex(0).getLength();
		double missedBases = 0;
		for(int i = 0; i < assGaps.length; i++){
			missedBases += assGaps[i].getLength();
		}
		return missedBases/totalBases;
	}
	
	public long totalMissedBases(){
		long missedBases = 0;
		for(int i = 0; i < assGaps.length; i++){
			missedBases += assGaps[i].getLength();
		}
		return missedBases;
	}
	
	public long getMaxContigLength() {
		return maxContigLength;
	}

	public long getContigN50() {
		return contigN50;
	}

	public long getContigN90() {
		return contigN90;
	}

	public long getMinContigLength() {
		return minContigLength;
	}

	/**
	 * 
	 * @return numExtraBases/totalNumBases
	 */
	public double percentExtraBases(){
		double totalBases = model.getGenomeBySourceIndex(1).getLength();
		double extraBases = 0;
		for (int i = 0; i < refGaps.length; i++){
			extraBases += refGaps[i].getLength();
		}
		return extraBases/totalBases;
	}
	
	public long totalExtraBases(){
		long extraBases = 0;
		for (int i = 0; i < refGaps.length; i++){
			extraBases += refGaps[i].getLength();
		}
		return extraBases;
	}
	
	public int getSharedBoundaryCount(){
		return numSharedBnds;
	}
	
	public int getInterLcbBoundaryCount(){
		return numInterLcbBnds;
	}
	
	/* 
	 * calculate GC content of missing bases
	 * in stretches up to 100nt.
	 * Useful for determining if we're suffering GC bias
	 */
	public static void calculateMissingGC(AssemblyScorer asmScore, File outDir, String basename){
		try{
			System.out.println("Printing GC contents of gaps < 100nt!");
			Gap[] gaps = asmScore.getAssemblyGaps();
			java.io.BufferedWriter bw = new BufferedWriter(new java.io.FileWriter(new File( outDir, basename + "_missing_gc.txt" )));
			java.io.BufferedWriter bw3 = new BufferedWriter(new java.io.FileWriter(new File( outDir, basename + "_background_gc_distribution.txt")));
			Random randy = new Random();
			for(int i=0; i<gaps.length; i++){
				if(gaps[i].getLength()>100)
					continue;
				long glen = gaps[i].getLength();
				glen = glen < 50 ? 50 : glen;
				if(gaps[i].getPosition()+glen/2 >=
					(int)asmScore.getModel().getGenomeBySourceIndex(gaps[i].getGenomeSrcIdx()).getLength())
				{
					glen = ((int)asmScore.getModel().getGenomeBySourceIndex(gaps[i].getGenomeSrcIdx()).getLength() - gaps[i].getPosition() - 3) / 2;
				}
				if(gaps[i].getPosition()-glen/2 < 1)
				{
					glen = gaps[i].getPosition() / 2;
				}

				long[] left = asmScore.getModel().getLCBAndColumn(gaps[i].getGenomeSrcIdx(), gaps[i].getPosition()-glen/2);
				long[] right = asmScore.getModel().getLCBAndColumn(gaps[i].getGenomeSrcIdx(), gaps[i].getPosition()+glen/2);
				if(left[0]!=right[0]){
					// gap spans LCB.  too hard for this hack.
					continue;
				}
				long ll = left[1] < right[1] ? left[1] : right[1];
				byte[] rawseq = asmScore.getModel().getXmfa().readRawSequence((int)left[0], 0, ll, Math.abs(right[1]-left[1])+1);
				double gc = countGC(rawseq);
				if(!Double.isNaN(gc))
				{
					bw.write((new Double(gc)).toString());
					bw.write("\n");
				}
				int upperbound = (int)asmScore.getModel().getGenomeBySourceIndex(gaps[i].getGenomeSrcIdx()).getLength() - (int)glen - 10000;
				upperbound = upperbound < 0 ? 0 : upperbound;
				int rpos = randy.nextInt(upperbound);
	
				// evil code copy!!
				left = asmScore.getModel().getLCBAndColumn(gaps[i].getGenomeSrcIdx(), rpos-glen/2);
				right = asmScore.getModel().getLCBAndColumn(gaps[i].getGenomeSrcIdx(), rpos+glen/2);
				if(left[0]!=right[0]){
					// gap spans LCB.  too hard for this hack.
					continue;
				}
				ll = left[1] < right[1] ? left[1] : right[1];
				rawseq = asmScore.getModel().getXmfa().readRawSequence((int)left[0], 0, ll, Math.abs(right[1]-left[1])+1);
				gc = countGC(rawseq);
				if(!Double.isNaN(gc))
				{
					bw3.write((new Double(gc)).toString());
					bw3.write("\n");
				}
			}
			bw.flush();
			bw.close();
			bw3.flush();
			bw3.close();
		}catch(IOException ioe){ioe.printStackTrace();};
	}

	static double countGC(byte[] rawseq){
		double gc = 0;
		double counts = 0;
		for(int j=0; j<rawseq.length; j++){
			if(rawseq[j]== 'G' || rawseq[j]== 'C' || 
					rawseq[j]== 'g' || rawseq[j]== 'c')
				gc++;
			else if(rawseq[j]=='-' || rawseq[j]=='\n')
				continue;
			counts++;
		}
		return gc /= counts;
	}

	static String subsToString(AssemblyScorer assScore){
		// A C T G
		StringBuilder sb = new StringBuilder();
		sb.append("\tA\tC\tT\tG\n");
		char[] ar = {'A','C','T','G'};
		int[][] subs = assScore.getSubs();
		for (int i = 0; i < subs.length; i++){
			sb.append(ar[i]);
			for (int j = 0; j < subs.length; j++){
				sb.append("\t"+(i==j?"-":subs[i][j]));
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Returns a 4x4 matrix of counts of substitution types between 
	 * genome <code>src_i</code> and <code>src_j</code>
	 * 
	 * <pre>
	 * <code>
	 *      A  C  T  G 
	 *    A -          
	 *    C    -       
	 *    T       -    
	 *    G          - 
	 * </code>
	 * </pre>
	 * @param snps
	 * @return a 4x4 matrix of substitution counts
	 */
	public static int[][] countSubstitutions(SNP[] snps){
		int[][] subs = new int[4][4];
		for (int k = 0; k < snps.length; k++){ 
			char c_0 = snps[k].getChar(0);
			char c_1 = snps[k].getChar(1);
			
			try {
				if (c_0 != c_1)
					subs[getBaseIdx(c_0)][getBaseIdx(c_1)]++;
			} catch (IllegalArgumentException e){
				//System.err.println("Skipping ambiguity: ref = " +c_0 +" assembly = " + c_1 );
			}
		}
		return subs;
	}

	static int getBaseIdx(char c) throws IllegalArgumentException {
		switch(c){
		  case 'a': return AssemblyScorer.A; 
		  case 'A': return AssemblyScorer.A;
		  case 'c': return AssemblyScorer.C;
		  case 'C': return AssemblyScorer.C;
		  case 't': return AssemblyScorer.T;
		  case 'T': return AssemblyScorer.T;
		  case 'g': return AssemblyScorer.G;
	 	  case 'G': return AssemblyScorer.G;
		  default:{ throw new IllegalArgumentException("char " + c);}
		}
	}

	public static final int MINIMUM_REPORTABLE_MISSING_LENGTH = 20;
	public static void printMissingRegions(AssemblyScorer asmScore, File outDir, String basename)
	{
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File( outDir, basename + "_missing_regions.txt" )));
			Gap[] gaps = asmScore.getReferenceGaps();
			for(int gI=0; gI < gaps.length; gI++){
				if(gaps[gI].getLength() < MINIMUM_REPORTABLE_MISSING_LENGTH)
					continue;
				byte[][] aln = asmScore.getModel().getSequenceRange(gaps[gI].getGenomeSrcIdx(), gaps[gI].getPosition(), gaps[gI].getPosition()+gaps[gI].getLength());
				StringBuilder sb = new StringBuilder();
				for(int i=0; i<aln[0].length; i++){
					if(aln[0][i]!='-' && aln[0][i]!='\n' && aln[0][i]!='\r')
						sb.append((char)aln[0][i]);
				}
				sb.append("\n");
				bw.write(">at_contig_" + gaps[gI].getContig() + "_pos_" + gaps[gI].getPositionInContig() + "_assembly_has_" + gaps[gI].getLength() + "_extra_bases\n");
				bw.write(sb.toString());
			}
			bw.flush();
		}catch(IOException ioe){ioe.printStackTrace();};
	}

	public static void printInfo(AssemblyScorer sa, File outDir, String baseName, boolean batch){
		PrintStream gapOut = null;
		PrintStream miscallOut = null;
		PrintStream uncallOut = null;
		PrintStream sumOut = null;
		PrintStream blockOut = null;
		PrintStream chromOut = null;
		if(!outDir.exists()){
			outDir.mkdir();
		}
		try {
			File gapFile = new File(outDir, baseName+"__gaps.txt");
			gapFile.createNewFile();
			gapOut = new PrintStream(gapFile);
			File miscallFile = new File(outDir, baseName+"__miscalls.txt");
			miscallFile.createNewFile();
			miscallOut = new PrintStream(miscallFile);
			File uncallFile = new File(outDir, baseName+"__uncalls.txt");
			uncallFile.createNewFile();
			uncallOut = new PrintStream(uncallFile);
			File sumFile = new File(outDir, baseName+"__sum.txt");
			sumFile.createNewFile();
			sumOut = new PrintStream(sumFile);
			File blockFile = new File(outDir,baseName+"__blocks.txt");
			blockFile.createNewFile();
			blockOut = new PrintStream(blockFile);
			File chromFile = new File(outDir,"chromosomes.txt");
			chromFile.createNewFile();
			chromOut = new PrintStream(chromFile);
		} catch (IOException e){
			e.printStackTrace();
			System.exit(-1);    
		}
		printInfo(sa,miscallOut,uncallOut,gapOut);
		printBlockInfo(blockOut,sa.model);
		if (batch){
		    sumOut.print(ScoreAssembly.getSumText(sa, false, true));	
		}else {
		    sumOut.print(ScoreAssembly.getSumText(sa, true, true));
		}
		AssemblyScorer.calculateMissingGC(sa, outDir, baseName);
		AssemblyScorer.printMissingRegions(sa, outDir, baseName);
		chromOut.print(getReferenceChromosomes(sa));
		
		blockOut.close();
		gapOut.close();
		miscallOut.close();
		uncallOut.close();
		sumOut.close();
		chromOut.close();
	}

	private static String getReferenceChromosomes(AssemblyScorer sa) {
		Genome refGenome = sa.getModel().getGenomeBySourceIndex(0);
		StringBuilder sb = new StringBuilder();
		for(int cI=0; cI<refGenome.getChromosomes().size(); cI++){
			Chromosome c = refGenome.getChromosomes().get(cI);
			sb.append(c.getName());
			sb.append("\t");
			sb.append(c.getStart() + 1);
			sb.append("\n");
		}
		return sb.toString();
	}
	
	private static void printBlockInfo(PrintStream out, XmfaViewerModel model){
		out.println("BlockId\tBlockLength\tRefLeft\tRefRight\tAsmLeft\tAsmRight");
		LCB[] lcbList = model.getSplitLcbList();
		int l = -1;
		int r = -1;
		Genome ref = model.getGenomeBySourceIndex(0);
		Genome asm = model.getGenomeBySourceIndex(1);
		for (LCB lcb: lcbList){
			out.print(lcb.id+"\t"+lcb.getAvgSegmentLength());
			if (lcb.getReverse(ref)){
				out.print("\t"+-1*lcb.getLeftEnd(ref));
				out.print("\t"+-1*lcb.getRightEnd(ref));
			} else {
				out.print("\t"+lcb.getLeftEnd(ref));
				out.print("\t"+lcb.getRightEnd(ref));
			}
			if (lcb.getReverse(asm)){
				out.print("\t"+-1*lcb.getLeftEnd(asm));
				out.print("\t"+-1*lcb.getRightEnd(asm));
			} else {
				out.print("\t"+lcb.getLeftEnd(asm));
				out.print("\t"+lcb.getRightEnd(asm));
			}
			out.println();
		}
	}
	
	/**
	 * Prints the SNP and gap data from this AssemblyScorer object out to the 
	 * respective <code>PrintStream</code>s. 
	 * <br>
	 * NOTE: <code>snpOut</code> and <code>gapOut</code> 
	 * can take null values
	 * </br>
	 * @param sa the AssemblyScorer to print info for
	 * @param snpOut stream to print SNP info to
	 * @param gapOut stream to print gap info to
	 */
	public static void printInfo(AssemblyScorer sa,  PrintStream miscallOut, PrintStream uncallOut, PrintStream gapOut){
		if (miscallOut!=null){
			StringBuilder sb = new StringBuilder();
			sb.append("SNP_Pattern\tRef_Contig\tRef_PosInContig\tRef_PosGenomeWide\tAssembly_Contig\tAssembly_PosInContig\tAssembly_PosGenomeWide\n");
			StringBuilder sb2 = new StringBuilder();
			sb2.append("SNP_Pattern\tRef_Contig\tRef_PosInContig\tRef_PosGenomeWide\tAssembly_Contig\tAssembly_PosInContig\tAssembly_PosGenomeWide\n");
			for (int i = 0; i < sa.snps.length; i++)
			{
				if(sa.snps[i].hasAmbiguities())
					sb.append(sa.snps[i].toString()+"\n");
				else
					sb2.append(sa.snps[i].toString()+"\n");
			}
			uncallOut.print(sb.toString());
			uncallOut.flush();
			miscallOut.print(sb2.toString());
			miscallOut.flush();
		}
		if (gapOut!=null){
			StringBuilder sb = new StringBuilder();
			sb.append("Sequence\tContig\tPosition_in_Contig\tGenomeWide_Position\tLength\tGenome0_GlobalPos\tGenome0_contig\tGenome0_LocalPos\tGenome1_GlobalPos\tGenome1_contig\tGenome1_LocalPos\n");
			for (int i = 0; i < sa.refGaps.length; i++)
				sb.append(sa.refGaps[i].toString("reference")+"\n");
			for (int i = 0; i < sa.assGaps.length; i++)
				sb.append(sa.assGaps[i].toString("assembly")+"\n");			
			gapOut.print(sb.toString());
			gapOut.flush();
		}
		
	}
}
