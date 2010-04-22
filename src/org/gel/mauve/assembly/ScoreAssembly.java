package org.gel.mauve.assembly;

import java.awt.BorderLayout;

import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Genome;
import org.gel.mauve.LCB;
import org.gel.mauve.OptionsBuilder;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.analysis.Gap;
import org.gel.mauve.analysis.PermutationExporter;
import org.gel.mauve.analysis.SNP;
import org.gel.mauve.analysis.SnpExporter;
import org.gel.mauve.contigs.ContigOrderer;
import org.gel.mauve.dcjx.DCJ;
import org.gel.mauve.gui.AlignFrame;
import org.gel.mauve.gui.AlignWorker;
import org.gel.mauve.gui.AlignmentProcessListener;
import org.gel.mauve.gui.AnalysisDisplayWindow;


import gr.zeus.ui.JConsole;

public class ScoreAssembly  {
	
	private static final String[] DEFAULT_ARGS = {"--skip-refinement",
													"--weight=200"};
	private static String SUM_CMD = "Summary";
	private static String SUM_DESC = "Summary of scoring assembly";
	private static String SNP_CMD = "SNPs";
	private static String SNP_DESC = "SNPs between reference and assembly";
	private static String GAP_CMD = "Gaps";
	private static String GAP_DESC = "Gaps in reference and assembly";
	
	private static HashMap<String,ScoreAssembly> modelMap;
	
	private static int A = 0;
	
	private static int C = 1;
	
	private static int T = 2;
	
	private static int G = 3;
	
	private static final String temp = "Running...";
	
	private static final String error = "Error computing DCJ distances! Please report bug to atritt@ucdavis.edu";
	
	private JTextArea sumTA, snpTA, gapTA;
	
	private int fWIDTH = 400;

	private int fHEIGHT = 510;
	
	private AssemblyScorer assScore;
	
	private AnalysisDisplayWindow win;
	
	public static void main(String[] args){
		CommandLine line = OptionsBuilder.getCmdLine(getOptions(), args);
		if (args.length == 0 || line == null || line.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(80,
					"java -cp Mauve.jar org.gel.mauve.assembly.ScoreAssembly [options]",
					"[options]", getOptions(), "report bugs to me");
			System.exit(-1);
		}

		File outDir =  null;
		if (line.hasOption("outputDir"))
			outDir = new File(line.getOptionValue("outputDir"));
		else 
			new File(System.getProperty("user.dir"));
		
		boolean batch = false;
		if (line.hasOption("batch"))
			batch = true;
		String basename = null;
		if (line.hasOption("basename"))
			basename = line.getOptionValue("basename");
		
		
		File alnmtFile = null;
		AssemblyScorer as = null;
		
		if (line.hasOption("alignment")){ // if we don't need to align
			if ((line.hasOption("reference") || line.hasOption("assembly"))){
				System.err.println("You gave me an alignment along with a reference and/or assembly genome.\n" +
				"Do not use use the \"-reference\" and \"-assembly\" flags with the \"-alignment\" flag.");
				System.exit(-1);
			}
			alnmtFile = new File (line.getOptionValue("alignment"));
			if (basename == null) {
				basename = alnmtFile.getName();
				basename = basename.substring(0,basename.lastIndexOf("."));
			}
			as = getAS(alnmtFile);
			AssemblyScorer.printInfo(as,outDir,basename,batch);
		} else { // we need to do some sort of alignment
			
			if (!line.hasOption("reference")){
				System.err.println("Reference file not given.");
				System.exit(-1);
			} else if (!line.hasOption("assembly")){
				System.err.println("Assembly file not given.");
				System.exit(-1);
			}
			
			File refFile = new File(line.getOptionValue("reference"));
			File assPath = new File(line.getOptionValue("assembly"));
			
			if (line.hasOption("reorder")){ // we need to reorder first
				String reorderDir = line.getOptionValue("reorder");
				ContigOrderer co = new ContigOrderer(refFile, assPath,
												new File(reorderDir));
				if (basename != null)
					as = new AssemblyScorer(co, outDir, basename);
				else 
					as = new AssemblyScorer(co, outDir);
				co.addAlnmtListener(as);
			} else {
				if (basename == null) {
					basename = assPath.getName();
					basename = basename.substring(0,basename.lastIndexOf("."));
				}
				alnmtFile = new File(outDir, basename+".xmfa");
				as = new AssemblyScorer(alnmtFile, outDir, basename);
				String[] cmd = makeAlnCmd(refFile,assPath, alnmtFile);
				System.out.println("Executing");
				AlignFrame.printCommand(cmd, System.out);
				AlignWorker worker = new AlignWorker(as, cmd);
				worker.start();
			}
			
		}
	} 
	
	private static AssemblyScorer getAS(File alnmtFile){
		try {
			//sa = new ScoreAssembly(args,true);
			XmfaViewerModel model = new XmfaViewerModel(alnmtFile,null);
			return new AssemblyScorer(model);
		} catch (Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}
	
	private static String[] makeAlnCmd(File seq1, File seq2, File output){
		String[] ret = new String[6 + DEFAULT_ARGS.length];
		int j = 0;
		ret[j++] = AlignFrame.getBinaryPath("progressiveMauve");
		for (int i = 0; i < DEFAULT_ARGS.length; i++)
			ret[j++] = DEFAULT_ARGS[i];
		ret[j++] = "--output="+output.getAbsolutePath();
		ret[j++] = "--backbone-output=" + output.getAbsolutePath()+".backbone";
		ret[j++] = "--output-guide-tree=" + output.getAbsolutePath()+".guide_tree";
		ret[j++] = seq1.getAbsolutePath();
		ret[j++] = seq2.getAbsolutePath();
		return ret;
	}
	
	@SuppressWarnings("static-access")
	private static Options getOptions(){
		Options ret = new Options();
		OptionsBuilder ob = new OptionsBuilder();
		ob.addBoolean("help", "print this message");
		ob.addBoolean("batch", "run in batch mode i.e. print summary output " +
											"on one line to standard output");
		ob.addArgument("string", "basename for output files", "basename",false);
		ob.addArgument("directory", "reorder contigs before scoring " +
						"assembly and store output in <directory>", "reorder",false);
		ob.addArgument("directory", "save output in <directory>. Default " +
									"is current directory.", "outputDir",false);
		Option alnOpt = ob.addArgument("file", "file containing alignment of assembly to " +
											"reference genome", "alignment",false);
		Option refOpt = ob.addArgument("file", "file containing reference genome", "reference",false);
		Option assOpt = ob.addArgument("file", "file containing assembly/draft genome to score",
																	"assembly",false);
	//	ob.addMutExclOptions(refOpt, alnOpt);
	//	ob.addMutExclOptions(assOpt, alnOpt);
		
		return ob.getOptions();
	}
	
	

	public ScoreAssembly(XmfaViewerModel model){
		win = new AnalysisDisplayWindow("Score Assembly - "+model.getSrc().getName(), fWIDTH, fHEIGHT);
		sumTA = win.addContentPanel(SUM_CMD, SUM_DESC, true);
		snpTA = win.addContentPanel(SNP_CMD, SNP_DESC, false);
		gapTA = win.addContentPanel(GAP_CMD, GAP_DESC, false);
		sumTA.append(temp);
		snpTA.append(temp);
		gapTA.append(temp);
		win.showWindow();
		assScore = new AssemblyScorer(model);
		sumTA.replaceRange("", 0, temp.length());
		sumTA.setText(getSumText(assScore,true,false));
		snpTA.replaceRange("", 0, temp.length());
		gapTA.replaceRange("", 0, temp.length());
		setInfoText();
		sumTA.setCaretPosition(0);
		snpTA.setCaretPosition(0);
		gapTA.setCaretPosition(0);
		
	}

	private void setInfoText(){
		StringBuilder sb = new StringBuilder();
		sb.append("SNP_Pattern\tRef_Contig\tRef_PosInContig\tRef_PosGenomeWide\tAssembly_Contig\tAssembly_PosInContig\tAssembly_PosGenomeWide\n");
		SNP[] snps = assScore.getSNPs();
		for (int i = 0; i < snps.length; i++)
			sb.append(snps[i].toString()+"\n");
		
		snpTA.setText(sb.toString());
		
		sb = new StringBuilder();
		sb.append("Sequence\tContig\tPosition_in_Contig\tGenomeWide_Position\tLength\n");
		Gap[] gaps = assScore.getReferenceGaps();
		for (int i = 0; i < gaps.length; i++)
			sb.append(gaps[i].toString("reference")+"\n");
		gaps = assScore.getAssemblyGaps();
		for (int i = 0; i < gaps.length; i++)
			sb.append(gaps[i].toString("assembly")+"\n");
		gapTA.setText(sb.toString());
		
	}
	
	public static String getSumText(AssemblyScorer assScore, boolean header, boolean singleLine){
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(4);
		StringBuilder sb = new StringBuilder();
		if (singleLine){
			if (header) {
				sb.append("NumContigs\tNumRefReplicons\tNumAssemblyBases\tNumReferenceBases\tNumLCBs\t" +
						"DCJ_Distance\tNumDCJBlocks\tNumSNPs\tNumGapsRef\tNumGapsAssembly\t" +
						"TotalBasesMissed\tPercBasesMissed\tExtraBases\tPercExtraBases\n");
			}
			
			sb.append(	assScore.numContigs()+"\t"+assScore.numReplicons()+"\t"+
						assScore.numBasesAssembly()+"\t"+assScore.numBasesReference()+"\t"+assScore.numLCBs()+"\t"+
						assScore.getDCJDist()+"\t"+assScore.numBlocks()+"\t"+assScore.getSNPs().length+"\t"+
						assScore.getReferenceGaps().length+"\t"+assScore.getAssemblyGaps().length+"\t"+
					 	assScore.totalMissedBases()+"\t"+nf.format(assScore.percentMissedBases()*100)+"\t"+
					 	assScore.totalExtraBases()+"\t"+nf.format(assScore.percentExtraBases()*100)+"\n");
			
		} else {
			if (header) {
				sb.append(
					"Number of Contigs:\t"+assScore.numContigs()+"\n\n"+
					"Number reference replicons:\t" + assScore.numReplicons()+"\n\n"+
					"Number of assembly bases:\t" + assScore.numBasesAssembly()+"\n\n"+
					"Number of reference bases:\t" + assScore.numBasesReference()+"\n\n"+
					"Number of LCBs:\t" + assScore.numLCBs()+"\n\n"+
					"Number of DCJ Blocks:\t"+assScore.numBlocks()+"\n\n"+
					"DCJ Distance:\t"+assScore.getDCJDist()+"\n\n"+
					"Number of SNPs:\t"+assScore.getSNPs().length+"\n\n"+
					"Number of Gaps in Reference:\t"+assScore.getReferenceGaps().length+"\n\n"+
					"Number of Gaps in Assembly:\t"+assScore.getAssemblyGaps().length+"\n\n" +
					"Total bases missed in reference:\t" + assScore.totalMissedBases() +"\n\n"+
					"Percent bases missed:\t" + nf.format(assScore.percentMissedBases()*100)+" %\n\n"+
					"Total bases extra in assembly:\t" + assScore.totalExtraBases()+"\n\n" +
					"Percent bases extra:\t" + nf.format(assScore.percentExtraBases()*100)+ " %\n\n"+
					"Substitutions (Ref on Y, Assembly on X):\n"+subsToString(assScore)
				);
				
				
			} else { 
				sb.append(
						assScore.numContigs()+"\n\n"+
						assScore.numLCBs()+"\n\n"+
						assScore.getDCJDist()+"\n"+
						 assScore.numBlocks()+"\n"+
						 assScore.getSNPs().length+"\n"+
						 assScore.getReferenceGaps().length+"\n"+
						 assScore.getAssemblyGaps().length+"\n" +
						 assScore.totalMissedBases() +"\n"+
						 nf.format(assScore.percentMissedBases()*100)+"\n"+
						 assScore.totalExtraBases()+"\n" +
						 nf.format(assScore.percentExtraBases()*100)+ "\n"+
						 subsToString(assScore));
			}
		}
		
		return sb.toString();
	}
	
	private static String subsToString(AssemblyScorer assScore){
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
	 * 
	 * @param refPath
	 * @param assPath
	 * @param reorder
	 * @return
	 */
	private static File runPMauveAlnmt(String refPath, String assPath, String outDirPath, boolean reorder){
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
	 * <p>
	 * <code>
	 *      A  C  T  G <br />
	 *    A -          <br />
	 *    C    -       <br />
	 *    T       -    <br />
	 *    G          - <br />
	 * </code>
	 * </p>
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
	
	public static void launchWindow(BaseViewerModel model){
		if (modelMap == null) 
			modelMap = new HashMap<String,ScoreAssembly>();
		String key = model.getSrc().getAbsolutePath();
		if (modelMap.containsKey(key)){
			modelMap.get(key).win.showWindow();
		} else if (model instanceof XmfaViewerModel) {
			modelMap.put(key, new ScoreAssembly((XmfaViewerModel)model));
		} else {
			System.err.println("Can't score assembly -- Please report this bug!");
		}	
	}
}
