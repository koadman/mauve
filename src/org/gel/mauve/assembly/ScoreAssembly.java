package org.gel.mauve.assembly;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import org.gel.mauve.Genome;
import org.gel.mauve.LCB;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.analysis.Gap;
import org.gel.mauve.analysis.PermutationExporter;
import org.gel.mauve.analysis.SNP;
import org.gel.mauve.analysis.SnpExporter;
import org.gel.mauve.dcjx.DCJ;

public class ScoreAssembly {
	
	private static int A = 0;
	
	private static int C = 1;
	
	private static int T = 2;
	
	private static int G = 3;
	
	private String refPath;
	
	private String assPath;
	
	private String xmfaPath;

	private String outDirPath;
	
	private XmfaViewerModel model; 
	
	private int[][] subs;
	
	private SNP[] snps;
	
	private Gap[] refGaps;
	
	private Gap[] assGaps; 
	
	private DCJ dcj;
	
	public static void main(String[] args){
		if (args.length != 2){
			System.err.println("Usage: java -cp Mauve.jar org.gel.mauve.assemlbly.ScoreAssembly <alignment> <working_directory>\n" +
					"  NOTE: all output will be stored in <working_directory>");
			System.exit(-1);
		}
		
		ScoreAssembly sa = null;
		String basename = args[0].substring(args[0].lastIndexOf("/")+1,args[0].lastIndexOf("."));
		File outDir = null;		
		try {
			//sa = new ScoreAssembly(args,true);
			XmfaViewerModel model = new XmfaViewerModel(new File(args[0]),null);
			
			sa = new ScoreAssembly(model);
			outDir = new File(args[1]);
		} catch (Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
		Arrays.sort(sa.assGaps);
		Arrays.sort(sa.refGaps);
		printInfo(sa,outDir,basename);
		
		/*  DCJ_distance	Num_Blocks	Num_SNPs	Num_Gaps_Reference	Num_Gaps_Assembly           */
		
		
		
		
		
	} 
	
	private static void printInfo(ScoreAssembly sa, File outDir, String baseName){
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
		
		gapOut.print("Sequence\tPosition\tLength\n");
		for (int i = 0; i < sa.refGaps.length; i++)
			gapOut.print(sa.refGaps[i].toString("reference")+"\n");
		for (int i = 0; i < sa.assGaps.length; i++)
			gapOut.print(sa.assGaps[i].toString("assembly")+"\n");
		gapOut.close();
		
		snpOut.print("SNP_Pattern\tReference_Location\tAssembly_Location\n");
		for (int i = 0; i < sa.snps.length; i++)
			snpOut.print(sa.snps[i].toString()+"\n");
		snpOut.close();
				
		sumOut.print("DCJ_distance\tNum_Blocks\tNum_SNPs" +
				"\tNum_Gaps_Reference\tNum_Gaps_Assembly\n");
		sumOut.println(sa.dcj.dcjDistance()+"\t"+sa.dcj.numBlocks()+"\t"+
				sa.snps.length+"\t"+sa.refGaps.length+"\t"+sa.assGaps.length);
		sumOut.close();
	}
	
	private void loadInfo(){
		model.setReference(model.getGenomeBySourceIndex(0));
		String[] perms = PermutationExporter.getPermStrings(model);
		this.dcj = new DCJ(perms[0], perms[1]);
		this.snps = SnpExporter.getSNPs(model, model.getXmfa());
		this.subs = countSubstitutions(snps);
		Gap[][] tmp = SnpExporter.getGaps(model, model.getXmfa());
		refGaps = tmp[0];
		assGaps = tmp[1];
	}
	
	public ScoreAssembly(String[] args, boolean reorder) throws IOException{
		File xmfaFile = runPMauveAlnmt(args[0],args[1],args[2],reorder);
		model = new XmfaViewerModel(xmfaFile, null);
		refPath = args[0];
		assPath = args[1];
		outDirPath = args[2]; 	
		loadInfo();
	}
	
	public ScoreAssembly(XmfaViewerModel model){
		this.model = model;
		loadInfo();
	}
	
	
	
	public int[][] getSubs(){
		return subs;
	}
	
	/**
	 * 
	 * @param refPath
	 * @param assPath
	 * @param reorder
	 * @return
	 */
	public static File runPMauveAlnmt(String refPath, String assPath, String outDirPath, boolean reorder){
		if (reorder){
			
		} else {
			File ref = new File(refPath);
			File ass = new File(assPath);
			File outDir = new File(outDirPath);
			
			if (!outDir.exists()){
				outDir.mkdir();
			} else if (!outDir.isDirectory()){
				System.err.println(outDirPath + " already exists as a file.");
				System.exit(-1);
			} 
		}
		return null;
	}
	
	/**
	 * Returns a 4x4 matrix of counts of substitution types between 
	 * genome <code>src_i</code> and <code>src_j</code>
	 * 
	 * <code>
	 *      A  C  T  G
	 *    A -
	 *    C    -
	 *    T       -
	 *    G          -
	 * </code>
	 * 
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
				System.err.println("Skipping ambiguity: ref = " +c_0 +" assembly = " + c_1 );
			}
		}
		return subs;
	}
	
	private static int getBaseIdx(char c) throws IllegalArgumentException {
		switch(c){
		  case 'a': return A; 
		  case 'A': return A;
		  case 'c': return C;
		  case 'C': return C;
		  case 't': return T;
		  case 'T': return T;
		  case 'g': return G;
	 	  case 'G': return G;
		  default:{ throw new IllegalArgumentException("char " + c);}
		}
	}
	
	
	
	
}
