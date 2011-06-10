package org.gel.mauve.assembly;

import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.biojava.bio.symbol.SymbolList;
import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Chromosome;
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
	private static String INV_CMD = "Inverted Contigs";
	private static String INV_DESC = "Incorrectly inverted contigs";
	private static String MIS_CMD = "Mis-assembled Contigs";
	private static String MIS_DESC = "Mis-assembled contigs with the total number of mis-assemblies in each";
	
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
	
	private int fWIDTH = 800;

	private int fHEIGHT = 510;
	
	private AssemblyScorer asmScore;
	
	private AnalysisDisplayWindow win;
		
	private boolean finished;
	
	private boolean cancel;
	
	public ScoreAssembly(XmfaViewerModel model){
		//init(model);
		this.model = model;
		this.finished = false;
		initWithJTables(model);
	}

	private void initWithJTables(XmfaViewerModel model){
		win = new AnalysisDisplayWindow("Score Assembly - "+model.getSrc().getName(), fWIDTH, fHEIGHT);
		sumTA = win.addContentPanel(SUM_CMD, SUM_DESC, true);
		sumTA.append(temp);
		this.win.showWindow();
		running.put(this.model.getSrc().getAbsolutePath(), this);
		new Thread( new Runnable (){ 
			public void run(){
				try {
				asmScore = new AssemblyScorer(ScoreAssembly.this.model);
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
			sumTA.setText(getSumText(asmScore,true,false));
			//addJTables();
			addTables();
			finished = true;
			win.showWindow();
			sumTA.setCaretPosition(0);
		}
	}
	
	private void addTables(){
		SNP[] snps = asmScore.getSNPs();
		Object[] snpHeader = {"SNP_Pattern","Ref_Contig","Ref_PosInContig",
							"Ref_PosGenomeWide","Assembly_Contig",
							"Assembly_PosInContig","Assembly_PosGenomeWide"};
		
		DefaultTableModel snpData = win.addContentTable(SNP_CMD, SNP_DESC, false);
		win.showWindow();
		snpData.setColumnIdentifiers(snpHeader);
		for (int snpI = 0; snpI < snps.length; snpI++){
			snpData.addRow(snps[snpI].toString().split("\t"));
		}
		
		Gap[] refGaps = asmScore.getReferenceGaps();
		Gap[] assGaps = asmScore.getAssemblyGaps();
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
		if (asmScore.hasBrokenCDS()){
			Object[] cdsHeader = 
				{"CDS_ID","Peptide_Length",
					"Perc_IncorrectBases", "Broken_Frame_Segments",
					"Gap_Segments","Substituted_Positions","Substitutions",
					"Stop_Codon_Positions","Original_Residue"};
			DefaultTableModel cdsData = win.addContentTable(CDS_CMD, CDS_DESC, false);
			cdsData.setColumnIdentifiers(cdsHeader);
			BrokenCDS[] cds = asmScore.getBrokenCDS();
			for (BrokenCDS bcds: cds)
				cdsData.addRow(bcds.toString().split("\t"));
		}
	}
	
	public static String getSumText(AssemblyScorer assScore, boolean header, boolean singleLine){
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(4);
		StringBuilder sb = new StringBuilder();
		if (singleLine){
			if (header) {
				sb.append("Name\tNumContigs\tNumRefReplicons\tNumAssemblyBases\tNumReferenceBases\tNumLCBs\t" +
						"DCJ_Distance\tNumDCJBlocks\tNumSNPs\tNumMisCalled\tNumUnCalled\tNumGapsRef\tNumGapsAssembly\t" +
						"TotalBasesMissed\tPercBasesMissed\tExtraBases\tPercExtraBases" + 
						"\tMissingChromosomes\tExtraContigs\tNumSharedBoundaries\tNumInterLcbBoundaries"+
						"\tBrokenCDS\tIntactCDS"+
						"\tAA\tAC\tAG\tAT\tCA\tCC\tCG\tCT\tGA\tGC\tGG\tGT\tTA\tTC\tTG\tTT\n");
			}
			sb.append(	assScore.getModel().getGenomeBySourceIndex(1).getDisplayName() + "\t" +
						assScore.numContigs()+"\t"+assScore.numReplicons()+"\t"+
						assScore.numBasesAssembly()+"\t"+assScore.numBasesReference()+"\t"+assScore.numLCBs()+"\t"+
						assScore.getDCJdist()+"\t"+assScore.numBlocks()+"\t"+assScore.getSNPs().length+"\t"+
						assScore.getMiscalled()+'\t'+assScore.getUncalled()+'\t'+
						assScore.getReferenceGaps().length+"\t"+assScore.getAssemblyGaps().length+"\t"+
					 	assScore.totalMissedBases()+"\t"+nf.format(assScore.percentMissedBases()*100)+"\t"+
					 	assScore.totalExtraBases()+"\t"+nf.format(assScore.percentExtraBases()*100) +"\t"+
					 	assScore.getMissingChromosomes().length+"\t"+assScore.getExtraContigs().length +"\t"+ 
					 	assScore.getSharedBoundaryCount()+"\t"+assScore.getInterLcbBoundaryCount());
			sb.append( "\t" + assScore.numBrokenCDS()+"\t"+assScore.numCompleteCDS());
			int[][] subs = assScore.getSubs();
			for(int i=0; i<subs.length; i++)
			{
				for(int j=0; j<subs[i].length; j++)
				{
					sb.append('\t');
					sb.append(subs[i][j]);
				}
			}
			sb.append("\n");
		} else {
			if (header) {
				sb.append(
					"Number of Contigs:\t"+assScore.numContigs()+"\n"+
					"Number reference replicons:\t" + assScore.numReplicons()+"\n"+
					"Number of assembly bases:\t" + assScore.numBasesAssembly()+"\n"+
					"Number of reference bases:\t" + assScore.numBasesReference()+"\n"+
					"Number of LCBs:\t" + assScore.numLCBs()+"\n"+
					"Number of Blocks:\t"+assScore.numBlocks()+"\n"+
					"Breakpoint Distance:\t"+assScore.getBPdist()+"\n"+
					"DCJ Distance:\t"+assScore.getDCJdist()+"\n"+
					"SCJ Distance:\t"+assScore.getSCJdist()+"\n"+
					"Number of Complete Coding Sequences:\t"+assScore.numCompleteCDS()+"\n"+
					"Number of Broken Coding Sequences:\t"+assScore.numBrokenCDS()+"\n"+					
					"Number of SNPs:\t"+assScore.getSNPs().length+"\n"+
					"Number of Gaps in Reference:\t"+assScore.getReferenceGaps().length+"\n"+
					"Number of Gaps in Assembly:\t"+assScore.getAssemblyGaps().length+"\n" +
					"Total bases missed in reference:\t" + assScore.totalMissedBases() +"\n"+
					"Percent bases missed:\t" + nf.format(assScore.percentMissedBases()*100)+" %\n"+
					"Total bases extra in assembly:\t" + assScore.totalExtraBases()+"\n" +
					"Percent bases extra:\t" + nf.format(assScore.percentExtraBases()*100)+ " %\n"+
					"Number of missing chromosomes:\t" + assScore.getMissingChromosomes().length +"\n"+
					"Number of extra contigs:\t"+assScore.getExtraContigs().length +"\n"+
					"Number of Shared Boundaries:\t"+assScore.getSharedBoundaryCount()+"\n"+
					"Number of Inter-LCB Boundaries:\t"+assScore.getInterLcbBoundaryCount()+"\n"+
					"Substitutions (Ref on Y, Assembly on X):\n"+subsToString(assScore)
				);
				
				
			} else { 
				sb.append(
						assScore.numContigs()+"\n"+
						assScore.numLCBs()+"\n"+
						assScore.getDCJdist()+"\n"+
						 assScore.numBlocks()+"\n"+
						 assScore.getSNPs().length+"\n"+
						 assScore.getReferenceGaps().length+"\n"+
						 assScore.getAssemblyGaps().length+"\n" +
						 assScore.totalMissedBases() +"\n"+
						 nf.format(assScore.percentMissedBases()*100)+"\n"+
						 assScore.totalExtraBases()+"\n" +
						 nf.format(assScore.percentExtraBases()*100)+ "\n"+
						 assScore.getMissingChromosomes() +"\n"+
						assScore.getExtraContigs()+"\n"+
						"Number of Shared Boundaries:\t"+assScore.getSharedBoundaryCount()+"\n"+
						"Number of Inter-LCB Boundaries:\t"+assScore.getInterLcbBoundaryCount()+"\n"+
						 subsToString(assScore));
			}
		}
		return sb.toString();
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
				glen = glen < 20 ? 20 : glen;
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
				int rpos = randy.nextInt((int)asmScore.getModel().getGenomeBySourceIndex(gaps[i].getGenomeSrcIdx()).getLength() - (int)glen);

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

	private static double countGC(byte[] rawseq){
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
				running.put(key, 
						new ScoreAssembly(model));
		}
	}
	
	public static void main(String[] args){
		CommandLine line = OptionsBuilder.getCmdLine(getOptions(), args);
		if (args.length == 0 || line == null || line.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(80,
					"java -cp Mauve.jar org.gel.mauve.assembly.ScoreAssembly [options]",
					"[options]", getOptions(), "report bugs to Aaron Darling <aarondarling@ucdavis.edu>");
			System.exit(-1);
		}
	
		File outDir =  null;
		if (line.hasOption("outputDir"))
			outDir = new File(line.getOptionValue("outputDir"));
		else 
			outDir = new File(System.getProperty("user.dir"));
		
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
				// AED: can't truncate to last . because the file may not have a dot in the name
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
				String[] reorderParams = new String[6];
				reorderParams[0] = "-output";
				reorderParams[1] = outDir.getAbsolutePath();
				reorderParams[2] = "-ref";
				reorderParams[3] = refFile.getAbsolutePath();
				reorderParams[4] = "-draft";
				reorderParams[5] = assPath.getAbsolutePath();
				ContigOrderer co = new ContigOrderer(reorderParams, null, false);
				if (basename != null)
					as = new AssemblyScorer(co, outDir, basename);
				else 
					as = new AssemblyScorer(co, outDir);
				co.addAlignmentProcessListener(as);
			} else {
				if (basename == null) {
					basename = assPath.getName();
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
		ob.addBoolean("reorder", "reorder contigs before scoring the assembly");
		ob.addArgument("string", "basename for output files", "basename",false);
		ob.addArgument("directory", "save output in <directory>. Default " +
									"is current directory.", "outputDir",true);
		ob.addArgument("file", "file containing alignment of assembly to " +
											"reference genome", "alignment",false);
		ob.addArgument("file", "file containing reference genome", "reference",false);
		ob.addArgument("file", "file containing assembly/draft genome to score",
																	"assembly",false);
		return ob.getOptions();
	}
}
