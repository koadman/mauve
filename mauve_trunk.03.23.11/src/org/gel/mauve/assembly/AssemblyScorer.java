package org.gel.mauve.assembly;

import java.io.File;


import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;


import java.io.IOException;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
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
	
	/** extra adjacencies */
	private Vector<Adjacency> typeI;
	
	/** missing adjacencies */
	private Vector<Adjacency> typeII;
	private Vector<Chromosome> invCtgs;
	private Map<Chromosome,Integer> misAsm;
	private int numMisAssemblies;
	
	private int miscalled;
	private int uncalled;
	
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
		
		System.err.println("\nnum assembly adjacencies: " + ass.length);
		Vector<Adjacency> intersection = new Vector<Adjacency>(); 
		for (Adjacency a: ass){
			if (!refSet.contains(a)) 
				typeI.add(a);
			else
				intersection.add(a);
		}

		System.err.println("num typeI errors: " + typeI.size());
		
		TreeSet<Adjacency> assSet = new TreeSet<Adjacency>(comp);
		for (Adjacency a: ass) { assSet.add(a);}
		
		System.err.println("num ref adjacencies: " + ref.length);
		
		for (Adjacency a: ref) {
			if (!assSet.contains(a))
				typeII.add(a);
		}
		System.err.println("num typeII errors: " + typeII.size());
		
		Iterator<Adjacency> it = intersection.iterator();
		while(it.hasNext()){
			System.err.println(it.next().toString());
		}
	}
	
	/**
	 * computes info. sorts gaps and snps
	 */
	private synchronized void loadInfo(){
		model.setReference(model.getGenomeBySourceIndex(0));
		
		System.out.print("Computing signed permutations....");
		String[] perms = PermutationExporter.getPermStrings(model, true); 
		System.out.print("done!\n");
		
		System.out.println("Permutations: ");
		System.out.println(perms[0]);
		System.out.println(perms[1]);
		
		System.out.print("Performing DCJ rearrangement analysis...");
		this.dcj = new DCJ(perms[0], perms[1]);
		System.out.print("done!\n");
		
		System.out.print("Computing adjacency errors...");
		computeAdjacencyErrors();
		System.out.print("done!\n");
		
		System.out.print("Computing inversions and mis-assemblies...");
		computeInverted(perms[1]);
		System.out.print("done\n");
		
		System.out.print("Getting SNPs...");
		this.snps = SnpExporter.getSNPs(model);
		System.out.print("done!\n");
		
		System.out.print("Counting base substitutions...");
		this.subs = ScoreAssembly.countSubstitutions(snps);
		summarizeBaseCalls();
		System.out.print("done!\n");
		
		System.out.print("Counting gaps...");
		Gap[][] tmp = SnpExporter.getGaps(model);
		System.out.print("done!\n");
		
		System.out.print("Counting extra contigs...");
		Chromosome[][] unique = SnpExporter.getUniqueChromosomes(model);
		System.out.print("done!\n");
		
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
	
	private void computeInverted(String asm){
		invCtgs = new Vector<Chromosome>();
		Comparator<Chromosome> comp = new Comparator<Chromosome>(){
			public int compare(Chromosome c1, Chromosome c2){
				return c1.getName().compareTo(c2.getName());
			}
		};
		misAsm = new TreeMap<Chromosome,Integer>(comp);
		String[] ctg = asm.split("[ ]*[$][ ]*");
		List<Chromosome> tmp = model.getGenomeBySourceIndex(1).getChromosomes();
		Chromosome[] ctgs = tmp.toArray(new Chromosome[tmp.size()]);
		for (int i = 0; i < ctg.length;i++){
			String[] blocks = ctg[i].split("[ ]*[,][ ]*");
			boolean allInv =  blocks[0].startsWith("-");
			boolean first = true;
			for (int j = 1; j < blocks.length; j++){
				boolean prev = blocks[j-1].startsWith("-");
				boolean curr = blocks[j].startsWith("-");
				if (prev != curr) {
					numMisAssemblies++;
					if (first) {
						misAsm.put(ctgs[i], new Integer(1));
						first = false;
					} else {
						int x = misAsm.get(ctgs[i]).intValue()+1;
						misAsm.put(ctgs[i], x);
					}
				}
			}
			if (allInv){
				invCtgs.add(ctgs[i]);
			}
		}
		
		
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
	
	public static void printInfo(AssemblyScorer sa, File outDir, String baseName, boolean batch){
		PrintStream gapOut = null;
		PrintStream miscallOut = null;
		PrintStream uncallOut = null;
		PrintStream sumOut = null;
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
			
		} catch (IOException e){
			e.printStackTrace();
			System.exit(-1);    
		}
		printInfo(sa,miscallOut,uncallOut,gapOut);
		if (batch){
		    sumOut.print(ScoreAssembly.getSumText(sa, false, true));	
		}else {
		    sumOut.print(ScoreAssembly.getSumText(sa, true, true));
		}
		ScoreAssembly.calculateMissingGC(sa, outDir, baseName);
		
		gapOut.close();
		miscallOut.close();
		uncallOut.close();
		sumOut.close();
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
