package org.gel.mauve.assembly;

import java.awt.Dimension;
import java.io.File;
import java.text.NumberFormat;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.OptionsBuilder;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.analysis.BrokenCDS;
import org.gel.mauve.analysis.Gap;
import org.gel.mauve.analysis.SNP;
import org.gel.mauve.contigs.ContigOrderer;
import org.gel.mauve.gui.AlignFrame;
import org.gel.mauve.gui.AlignWorker;
import org.gel.mauve.gui.AnalysisDisplayWindow;
import org.gel.mauve.gui.MauveFrame;

public class ScoreAssembly  {
	
	private static final String[] DEFAULT_ARGS = {"--skip-refinement",
													"--weight=200"};
	private static String SUM_CMD = "Summary";
	private static String SUM_DESC = "Summary of scoring assembly";
	private static String SNP_CMD = "SNPs";
	private static String SNP_DESC = "SNPs between reference and assembly";
	private static String GAP_CMD = "Gaps";
	private static String GAP_DESC = "Gaps in reference and assembly";
	private static String CDS_CMD = "Broken CDS";
	private static String CDS_DESC = "Broken CDS in assembly genome";
	
	private static HashMap<String,ScoreAssembly> modelMap = new HashMap<String, ScoreAssembly>();
	
	private static HashMap<String,ScoreAssembly> running = new HashMap<String, ScoreAssembly>();

	private static final String temp = "Running...";
	
	private static final String error = "Error computing DCJ distances! Please report bug to atritt@ucdavis.edu";
	
	private static int A = 0;
	
	private static int C = 1;
	
	private static int T = 2;
	
	private static int G = 3;
	
	private JTextArea sumTA;
	
	private XmfaViewerModel model;
	
	private int fWIDTH = 400;

	private int fHEIGHT = 510;
	
	private AssemblyScorer assScore;
	
	private AnalysisDisplayWindow win;
	
	private boolean getBrokenCDS;
	
	private boolean finished;
	
	private boolean cancel;
	
	public ScoreAssembly(XmfaViewerModel model, boolean getBrokenCDS){
		//init(model);
		this.model = model;
		this.getBrokenCDS = getBrokenCDS;
		this.finished = false;
		initWithJTables(model);
	}
	/*
	private void init(XmfaViewerModel model){
		win = new AnalysisDisplayWindow("Score Assembly - "+model.getSrc().getName(), fWIDTH, fHEIGHT);
		sumTA = win.addContentPanel(SUM_CMD, SUM_DESC, true);
		JTextArea snpTA = win.addContentPanel(SNP_CMD, SNP_DESC, false);
		JTextArea gapTA = win.addContentPanel(GAP_CMD, GAP_DESC, false);
		sumTA.append(temp);
		snpTA.append(temp);
		gapTA.append(temp);
		win.showWindow();
		//assScore = new AssemblyScorer(model);
		sumTA.replaceRange("", 0, temp.length());
		sumTA.setText(getSumText(assScore,true,false));
		snpTA.replaceRange("", 0, temp.length());
		gapTA.replaceRange("", 0, temp.length());
		setInfoText();
		sumTA.setCaretPosition(0);
		snpTA.setCaretPosition(0);
		gapTA.setCaretPosition(0);
	}
	*/
	private void initWithJTables(XmfaViewerModel model){
		win = new AnalysisDisplayWindow("Score Assembly - "+model.getSrc().getName(), fWIDTH, fHEIGHT);
		sumTA = win.addContentPanel(SUM_CMD, SUM_DESC, true);
		sumTA.append(temp);
		this.win.showWindow();
		running.put(this.model.getSrc().getAbsolutePath(), this);
		new Thread( new Runnable (){ 
			public void run(){
				try {
				assScore = new AssemblyScorer(ScoreAssembly.this.model,
										ScoreAssembly.this.getBrokenCDS);
				ScoreAssembly.this.finish();
				} catch (Exception e){
					ScoreAssembly.this.cancel();
					System.err.println("\nError\n");
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public void cancel(){
		cancel = true;
	}
	
	private synchronized void finish(){
		if (!cancel) {
			running.remove(this.model.getSrc().getAbsolutePath());
			modelMap.put(this.model.getSrc().getAbsolutePath(), this);
			sumTA.replaceRange("", 0, temp.length());
			sumTA.setText(getSumText(assScore,true,false));
			//addJTables();
			addTables();
			finished = true;
			win.showWindow();
			sumTA.setCaretPosition(0);
		}
	}
	
	private void addTables(){
		SNP[] snps = assScore.getSNPs();
		Object[] snpHeader = {"SNP_Pattern","Ref_Contig","Ref_PosInContig",
							"Ref_PosGenomeWide","Assembly_Contig",
							"Assembly_PosInContig","Assembly_PosGenomeWide"};
		
		DefaultTableModel snpData = win.addContentTable(SNP_CMD, SNP_DESC, false);
		win.showWindow();
		snpData.setColumnIdentifiers(snpHeader);
		for (int snpI = 0; snpI < snps.length; snpI++){
			snpData.addRow(snps[snpI].toString().split("\t"));
		}
		
		Gap[] refGaps = assScore.getReferenceGaps();
		Gap[] assGaps = assScore.getAssemblyGaps();
		int nGen = model.numGenomes();
		Object[] gapHeader = new Object[5+(nGen)*3];
		gapHeader[0] = "Sequence";
		gapHeader[1] = "Contig";
		gapHeader[2] = "Position_in_Contig";
		gapHeader[3] = "GenomeWide_Position";
		gapHeader[4] = "Length";
		int idx = 5;
		for (int j = 0; j < nGen; j++){
			gapHeader[idx++] = "sequence_"+j+"_pos"; 
			gapHeader[idx++] = "sequence_"+j+"_ctg";
			gapHeader[idx++] = "sequence_"+j+"_posInCtg";
		}
		DefaultTableModel gapData = win.addContentTable(GAP_CMD, GAP_DESC, false);
		gapData.setColumnIdentifiers(gapHeader);
		for (int gapI = 0; gapI < refGaps.length; gapI++)
			gapData.addRow(refGaps[gapI].toString().split("\t"));
		for (int gapI = 0; gapI < assGaps.length; gapI++)
			gapData.addRow(assGaps[gapI].toString().split("\t"));
		if (getBrokenCDS && assScore.hasBrokenCDS()){
			Object[] cdsHeader = {"CDS_ID","Substituted_Positions","Substitution","Stop_Codon_Positions","Original_Residue"};
			DefaultTableModel cdsData = win.addContentTable(CDS_CMD, CDS_DESC, false);
			BrokenCDS[] cds = assScore.getBrokenCDS();
			for (int cdsI = 0; cdsI < cds.length; cdsI++)
				cdsData.addRow(cds.toString().split("\t"));
		}
		
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
						assScore.getDCJdist()+"\t"+assScore.numBlocks()+"\t"+assScore.getSNPs().length+"\t"+
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
					"Number of Blocks:\t"+assScore.numBlocks()+"\n\n"+
					"Breakpoint Distance:\t"+assScore.getBPdist()+"\n\n"+
					"DCJ Distance:\t"+assScore.getDCJdist()+"\n\n"+
					"SCJ Distance:\t"+assScore.getSCJdist()+"\n\n"+
					"Type-I Adjacency Error Rate:\t"+nf.format(assScore.typeIadjErr())+"\n\n"+
					"Type-II Adjacency Error Rate:\t"+nf.format(assScore.typeIIadjErr())+"\n\n"+
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
						assScore.getDCJdist()+"\n"+
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
	
	public static void launchWindow(MauveFrame frame){
		BaseViewerModel model = frame.getModel();
		String key = model.getSrc().getAbsolutePath();
		if (modelMap.containsKey(key)){
		//	modelMap.get(key).win.showWindow();
			startNewSA(frame);
		} else if (model instanceof XmfaViewerModel) {
			startNewSA(frame);			
		} else {
			System.err.println("Can't score assembly unless I have an" +
							" XmfaViewerModel -- Please report this bug!");
		}	
	}
	
	private static void startNewSA(MauveFrame frame){
		interactive(frame);
	}

	public static void interactive(MauveFrame frame){
		XmfaViewerModel model = (XmfaViewerModel) frame.getModel();
		String key = model.getSrc().getAbsolutePath();
		if (running.containsKey(key)) {
			int ret_val = JOptionPane.showConfirmDialog(frame, "I'm currently"+
					"running the scoring pipeline for this assembly. Do you " +
					"want me to restart it?");
			if (ret_val == JOptionPane.YES_OPTION){
				running.get(key).cancel();
				running.remove(key).win.closeWindow();
				startNewSA(frame);
			} else if (ret_val == JOptionPane.NO_OPTION){
				running.get(key).win.showWindow();
			}
		} else {
			int ret_val = JOptionPane.showConfirmDialog(frame, "Do you want me " +
									"to locate broken CDS. This can take awhile");
			if (ret_val == JOptionPane.YES_OPTION) {
				running.put(key, 
						new ScoreAssembly(model,true));
			} else if (ret_val == JOptionPane.NO_OPTION) {
				running.put(key, 
						new ScoreAssembly(model,false));
			}
		}
	}
	
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
		boolean getBrokenCDS = false;
		if (line.hasOption("brokenCDS")){
			getBrokenCDS = true;
		}
		
		
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
			as = getAS(alnmtFile,getBrokenCDS);
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
					as = new AssemblyScorer(co, outDir, basename,getBrokenCDS);
				else 
					as = new AssemblyScorer(co, outDir,getBrokenCDS);
				co.addAlnmtListener(as);
			} else {
				if (basename == null) {
					basename = assPath.getName();
					basename = basename.substring(0,basename.lastIndexOf("."));
				}
				alnmtFile = new File(outDir, basename+".xmfa");
				as = new AssemblyScorer(alnmtFile, outDir, basename, getBrokenCDS);
				String[] cmd = makeAlnCmd(refFile,assPath, alnmtFile);
				System.out.println("Executing");
				AlignFrame.printCommand(cmd, System.out);
				AlignWorker worker = new AlignWorker(as, cmd);
				worker.start();
			}
			
		}
	}

	private static AssemblyScorer getAS(File alnmtFile, boolean getBrokenCDS){
		try {
			//sa = new ScoreAssembly(args,true);
			XmfaViewerModel model = new XmfaViewerModel(alnmtFile,null);
			return new AssemblyScorer(model, getBrokenCDS);
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
		ob.addArgument("file", "file containing alignment of assembly to " +
											"reference genome", "alignment",false);
		ob.addArgument("file", "file containing reference genome", "reference",false);
		ob.addArgument("file", "file containing assembly/draft genome to score",
																	"assembly",false);
		ob.addBoolean("brokenCDS", "compute broken CDS in assembly. This can take awhile.");
		return ob.getOptions();
	}
}
