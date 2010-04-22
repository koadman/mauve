package org.gel.mauve.assembly;

import java.io.File;


import java.io.IOException;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.Arrays;

import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.analysis.Gap;
import org.gel.mauve.analysis.PermutationExporter;
import org.gel.mauve.analysis.SNP;
import org.gel.mauve.analysis.SnpExporter;
import org.gel.mauve.dcjx.DCJ;
import org.gel.mauve.gui.AlignmentProcessListener;

public class AssemblyScorer implements AlignmentProcessListener {

	private File alnmtFile;
	
	private File outputDir;
	
	private boolean batch;
	
	private String basename;
	
	private XmfaViewerModel model; 
	
	private int[][] subs;
	
	private SNP[] snps;
	
	private Gap[] refGaps;
	
	private Gap[] assGaps; 
	
	private DCJ dcj;
	
	public AssemblyScorer(XmfaViewerModel model){
		this.model = model;
		loadInfo();
	}
	
	public AssemblyScorer(File alnmtFile, File outDir) {
		this.alnmtFile = alnmtFile;
		this.outputDir = outDir;
		basename = alnmtFile.getName();
		basename = basename.substring(0,basename.lastIndexOf("."));
		batch = false;
	}
	
	public void completeAlignment(int retcode){
		if (retcode == 0) {
			try {
				this.model = new XmfaViewerModel(alnmtFile,null);
			} catch (IOException e) {
				System.err.println("Couldn't load alignment file " 
									+ alnmtFile.getAbsolutePath());
				e.printStackTrace();
			}
			loadInfo();
			printInfo(this, outputDir, basename, batch);
		} else { 
			System.err.println("Alignment failed with error code "+ retcode);
		}
			
	}
	
	/**
	 * computes info. sorts gaps and snps
	 */
	private void loadInfo(){
		model.setReference(model.getGenomeBySourceIndex(0));
		
		System.out.print("Computing signed permutations....");
		String[] perms = PermutationExporter.getPermStrings(model, true); 
		System.out.print("done!\n");
		
		System.out.print("Performing DCJ rearrangement analysis...");
		this.dcj = new DCJ(perms[0], perms[1]);
		System.out.print("done!\n");
		
		System.out.print("Getting SNPs...");
		this.snps = SnpExporter.getSNPs(model, model.getXmfa());
		System.out.print("done!\n");
		
		System.out.print("Counting base substitutions...");
		this.subs = ScoreAssembly.countSubstitutions(snps);
		System.out.print("done!\n");
		
		System.out.print("Counting gaps...");
		Gap[][] tmp = SnpExporter.getGaps(model);
		System.out.print("done!\n");
		
		refGaps = tmp[0];
		assGaps = tmp[1];
		Arrays.sort(assGaps);
		Arrays.sort(refGaps);
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
	
	public SNP[] getSNPs(){
		return snps;
	}
	
	public int[][] getSubs(){
		return subs;
	}
	
	public int getDCJDist(){
		return dcj.dcjDistance();
	}
	
	public int numBlocks(){
		return dcj.numBlocks();
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
		PrintStream snpOut = null;
		PrintStream sumOut = null;
		try {
			File gapFile = new File(outDir, baseName+"__gaps.txt");
			gapFile.createNewFile();
			gapOut = new PrintStream(gapFile);
			File snpFile = new File(outDir, baseName+"__snps.txt");
			snpFile.createNewFile();
			snpOut = new PrintStream(snpFile);
			File sumFile = new File(outDir, baseName+"__sum.txt");
			sumFile.createNewFile();
			sumOut = new PrintStream(sumFile);
			
		} catch (IOException e){
			e.printStackTrace();
			System.exit(-1);    
		}
		printInfo(sa,snpOut,gapOut);
		if (batch)
			printSummary(sa,sumOut,false,true);
		else 
			printSummary(sa,sumOut,true,true);
		gapOut.close();
		snpOut.close();
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
	public static void printInfo(AssemblyScorer sa,  PrintStream snpOut, PrintStream gapOut){
		if (snpOut!=null){
			StringBuilder sb = new StringBuilder();
			sb.append("SNP_Pattern\tRef_Contig\tRef_PosInContig\tRef_PosGenomeWide\tAssembly_Contig\tAssembly_PosInContig\tAssembly_PosGenomeWide\n");
			for (int i = 0; i < sa.snps.length; i++)
				sb.append(sa.snps[i].toString()+"\n");
			snpOut.print(sb.toString());
			snpOut.flush();
		}
		if (gapOut!=null){
			StringBuilder sb = new StringBuilder();
			sb.append("Sequence\tContig\tPosition_in_Contig\tGenomeWide_Position\tLength\n");
			for (int i = 0; i < sa.refGaps.length; i++)
				sb.append(sa.refGaps[i].toString("reference")+"\n");
			for (int i = 0; i < sa.assGaps.length; i++)
				sb.append(sa.assGaps[i].toString("assembly")+"\n");
			gapOut.print(sb.toString());
			gapOut.flush();
		}
		
	}
	
	public static void printSummary(AssemblyScorer sa, PrintStream out, boolean header, boolean singleLine){
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(4);
		StringBuilder sb = new StringBuilder();
		if (out!=null){
			if (singleLine){
				if (header) {
					sb.append("DCJ_Distance\tNum_Blocks\tNum_SNPs\tNumGaps_Ref\tNumGaps_Assembly\t" +
							"TotalBasesMissed\tPercentBasesMissed\tExtraBases\tPercentExtraBases\n");
				}
				
				sb.append(sa.dcj.dcjDistance()+"\t"+sa.dcj.numBlocks()+"\t"+sa.snps.length+"\t"+
							sa.getReferenceGaps().length+"\t"+sa.getAssemblyGaps().length+"\t"+
						 	sa.totalMissedBases()+"\t"+nf.format(sa.percentMissedBases()*100)+"\t"+
						 	sa.totalExtraBases()+"\t"+nf.format(sa.percentExtraBases()*100)+"\n");
				
			} else {
				if (header)
					sb.append("DCJ Distance:\t"+sa.dcj.dcjDistance()+"\n"+
						 "Number of Blocks:\t"+sa.dcj.numBlocks()+"\n"+
						 "Number of SNPs:\t"+sa.snps.length+"\n"+
						 "Number of Gaps in Reference:\t"+sa.refGaps.length+"\n"+
						 "Number of Gaps in Assembly:\t"+sa.assGaps.length+"\n" +
						 "Total bases missed:\t" + sa.totalMissedBases() +"\n"+
						 "Percent bases missed:\t" + nf.format(sa.percentMissedBases()*100)+" %\n"+
						 "Total bases extra:\t" + sa.totalExtraBases()+"\n" +
						 "Percent bases extra:\t" + nf.format(sa.percentExtraBases()*100)+ " %\n");
				else 
					sb.append(sa.dcj.dcjDistance()+"\n"+
							 sa.dcj.numBlocks()+"\n"+
							 sa.snps.length+"\n"+
							 sa.refGaps.length+"\n"+
							 sa.assGaps.length+"\n" +
							 sa.totalMissedBases() +"\n"+
							 nf.format(sa.percentMissedBases()*100)+"\n"+
							 sa.totalExtraBases()+"\n" +
							 nf.format(sa.percentExtraBases()*100)+ "\n");
				
			}
		}
		out.print(sb.toString());
		out.flush();
	}
	
}
