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
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Genome;
import org.gel.mauve.LCB;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.analysis.Gap;
import org.gel.mauve.analysis.PermutationExporter;
import org.gel.mauve.analysis.SNP;
import org.gel.mauve.analysis.SnpExporter;
import org.gel.mauve.dcjx.DCJ;
import org.gel.mauve.gui.AnalysisDisplayWindow;


import gr.zeus.ui.JConsole;

public class ScoreAssembly {
	
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

	private int fHEIGHT = 400;
	
	private JPanel toptopPanel;

	private CardLayout cards;

	private String box;
	
	private AssemblyScorer assScore;

	private JFrame frame;
	
	private AnalysisDisplayWindow win;
	
	private XmfaViewerModel model;
	
	private static final String USAGE = 
		"Usage: java -cp Mauve.jar org.gel.mauve.assemlbly.ScoreAssembly [options]\n" +
		"  where [options] are:\n" +
		"\t--alignment <path>\n\t\tthe alignment of the assembly to the reference to score\n" +
//		"\t--reference <path>\n\t\tthe reference genome\n" +
//		"\t--assembly <path>\n\t\tthe assembly to score\n" +
		"\t--outDir <path>\n\t\tthe directory to store output in\n" +
		"\t\tIf this is not set, the current working directory is used.\n" +
		"\t--batch\n\t\ta flag to indicate running in batch mode.\n" +
		"\t\tIf you call this flag, summary info will be printed in tab-delimited\n" +
		"\t\tformat to standard output without any header text.\n" +
		"\t--help\n\t\tprint this text.\n";
	
	public static void main(String[] args){
		if (args.length == 0 || args[0].equalsIgnoreCase("--help") ){
			System.err.println(USAGE);
			System.exit(-1);
		}
		
		RunTimeParam rtp = null;
		try {
			rtp = new RunTimeParam(args);
		} catch (Exception e){
			if (e.getMessage().equals(RunTimeParam.DUAL_MODE)){
				System.err.println("You gave me an alignment along with a reference and/or assembly genome.\n" +
						"Do not use use the \"--reference\" and \"--assembly\" flags with the \"--alignment\" flag.");
				 
			} else if (e.getMessage().startsWith(RunTimeParam.UNREC_ARG)){
				System.err.println("Unrecognized argument: " + 
						e.getMessage().substring(RunTimeParam.UNREC_ARG.length()));

				System.err.println(USAGE);
			} else if (e.getMessage().equals(RunTimeParam.HELP)){
				System.err.println(USAGE);
			} else {
				e.printStackTrace();
			}
			System.exit(-1);
		}
		
		AssemblyScorer sa = null;
		String basename = rtp.basename;
		File outDir = rtp.outDir;		
		try {
			//sa = new ScoreAssembly(args,true);
			XmfaViewerModel model = new XmfaViewerModel(rtp.alnmtFile,null);
			sa = new AssemblyScorer(model);
		} catch (Exception e){
			e.printStackTrace();
			System.exit(-1);
		}

		
		AssemblyScorer.printInfo(sa,outDir,basename,rtp.batch);
		
	} 
	
	private static class RunTimeParam{
		static String UNREC_ARG = "unrecognized argument";
		/**
		 * If the user passes in both an alignment and
		 * the reference and/or assembly genomes.
		 */
		static String DUAL_MODE = "dual mode";
		static String HELP = "help";
		
		String basename;
		File outDir;
		boolean batch = false;
		
		boolean alnmtSet = false;
		String alnmtPath;
		File alnmtFile;
		
		boolean refSet = false;
		String refPath;
		File refFile;
		boolean assSet = false;
		String assPath;
		File assFile;
		
		
		RunTimeParam(String[] args) throws Exception {
			int i = 0;
			while (i < args.length){
				if (args[i].equalsIgnoreCase("--alignment")){
					i++;
					alnmtSet = true;
					if (refSet || assSet)
						throw new Exception(DUAL_MODE);
					alnmtPath = args[i];
					i++;
				} else if (args[i].equalsIgnoreCase("--reference")){
					i++;
					refSet = true;
					if (alnmtSet)
						throw new Exception(DUAL_MODE);
					refPath = args[i];
					i++;
				} else if (args[i].equalsIgnoreCase("--assembly")){
					i++;
					assSet = true;
					if (alnmtSet)
						throw new Exception(DUAL_MODE);
					assPath = args[i];
					i++;
				} else if (args[i].equalsIgnoreCase("--outDir")){
					i++;
					outDir = new File(args[i]);
					if (!outDir.exists()){
						outDir.mkdir();
					}
					i++;
				} else if (args[i].equalsIgnoreCase("--batch")){
					batch = true;
					i++;
				} else if (args[i].equalsIgnoreCase("--help")){
					throw new Exception(HELP);
				} else {
					throw new Exception (UNREC_ARG+args[i]);
				}
			}
			
			if (outDir == null){
				outDir = new File(System.getProperty("user.dir"));
			}
			
			if (alnmtSet){
				basename = alnmtPath.substring(alnmtPath.lastIndexOf("/")+1,alnmtPath.lastIndexOf("."));
				alnmtFile = new File(alnmtPath);
			} else if (refSet && assSet){
				
			} else {
				System.err.println("Bad RunTimeParam.class");
			}
			
			
		}
		
	}
	

	public ScoreAssembly(XmfaViewerModel model){
	//	build(model);
		this.model = model;
		win = new AnalysisDisplayWindow("Score Assembly - "+model.getSrc().getName(), fWIDTH, fHEIGHT);
		sumTA = win.addContentPanel(SUM_CMD, SUM_DESC, true);
		snpTA = win.addContentPanel(SNP_CMD, SNP_DESC, false);
		gapTA = win.addContentPanel(GAP_CMD, GAP_DESC, false);
		win.showWindow();

		sumTA.append(temp);
		snpTA.append(temp);
		gapTA.append(temp);
		assScore = new AssemblyScorer(model);
		sumTA.replaceRange("", 0, temp.length());
		setSumText(true,false);
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
	
	private void setSumText(boolean header, boolean singleLine){
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(4);
		StringBuilder sb = new StringBuilder();
		if (singleLine){
			if (header) {
				sb.append("DCJ_Distance\tNum_Blocks\tNum_SNPs\tNumGaps_Ref\tNumGaps_Assembly\t" +
						"TotalBasesMissed\tPercentBasesMissed\tExtraBases\tPercentExtraBases\n");
			}
			
			sb.append(assScore.getDCJDist()+"\t"+assScore.numBlocks()+"\t"+assScore.getSNPs().length+"\t"+
						assScore.getReferenceGaps().length+"\t"+assScore.getAssemblyGaps().length+"\t"+
					 	assScore.totalMissedBases()+"\t"+nf.format(assScore.percentMissedBases()*100)+"\t"+
					 	assScore.totalExtraBases()+"\t"+nf.format(assScore.percentExtraBases()*100)+"\n");
			
		} else {
			if (header) {
				sb.append("DCJ Distance:\t"+assScore.getDCJDist()+"\n\n"+
					 "Number of Blocks:\t"+assScore.numBlocks()+"\n\n"+
					 "Number of SNPs:\t"+assScore.getSNPs().length+"\n\n"+
					 "Number of Gaps in Reference:\t"+assScore.getReferenceGaps().length+"\n\n"+
					 "Number of Gaps in Assembly:\t"+assScore.getAssemblyGaps().length+"\n\n" +
					 "Total bases missed:\t" + assScore.totalMissedBases() +"\n\n"+
					 "Percent bases missed:\t" + nf.format(assScore.percentMissedBases()*100)+" %\n\n"+
					 "Total bases extra:\t" + assScore.totalExtraBases()+"\n\n" +
					 "Percent bases extra:\t" + nf.format(assScore.percentExtraBases()*100)+ " %\n\n"+
					 "Substitutions (Ref on Y, Assembly on X):\n"+subsToString());
				
				
			} else { 
				sb.append(assScore.getDCJDist()+"\n"+
						 assScore.numBlocks()+"\n"+
						 assScore.getSNPs().length+"\n"+
						 assScore.getReferenceGaps().length+"\n"+
						 assScore.getAssemblyGaps().length+"\n" +
						 assScore.totalMissedBases() +"\n"+
						 nf.format(assScore.percentMissedBases()*100)+"\n"+
						 assScore.totalExtraBases()+"\n" +
						 nf.format(assScore.percentExtraBases()*100)+ "\n"+
						 subsToString());
			}
		}
		
		sumTA.setText(sb.toString());
	}
	
	private String subsToString(){
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
	
	private void build (XmfaViewerModel model) {
		frame = new JFrame ("Assembly Score - " + model.getSrc().getName());
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int xPos = dim.width - fWIDTH;
		frame.setSize (fWIDTH, fHEIGHT);
		frame.setLocation(xPos, 0);
		
		JPanel content = new JPanel (new BorderLayout ());
		
		frame.getContentPane ().add (content, BorderLayout.CENTER);
		box = "";
		// /create top panel
		JPanel topPanel = new JPanel ();

		topPanel.setLayout (new BorderLayout ());
		// top top panel with cards
		cards = new CardLayout ();
		toptopPanel = new JPanel ();
		toptopPanel.setLayout (cards);
		topPanel.add (toptopPanel, BorderLayout.CENTER);
		
		// top lower panel of buttons
		JPanel toplowerPanel = new JPanel ();
		GridLayout butts = new GridLayout (1, 0);
		butts.setHgap (50);
		toplowerPanel.setLayout (butts);
		topPanel.add (toplowerPanel, BorderLayout.SOUTH);
		// make buttons
		JButton Bsum = new JButton (SUM_CMD);
		JButton Bsnps = new JButton (SNP_CMD);
		JButton Bgaps = new JButton (GAP_CMD);
		toplowerPanel.add (Bsum);
		toplowerPanel.add (Bsnps);
		toplowerPanel.add (Bgaps);
		
		// make listener
		ChangeCards cc = new ChangeCards ();
		// register buttons
		Bsum.addActionListener (cc);
		Bsnps.addActionListener (cc);
		Bgaps.addActionListener (cc);
		// /add the top panel
		
		
		content.add (topPanel, BorderLayout.CENTER);

		Font font = new Font ("monospaced", Font.PLAIN, 12);
		font.getSize2D();
		
		// /Add output text to cards panel
		sumTA = new JTextArea (box, 0, 0);
		sumTA.setEditable (false);
		sumTA.setFont (font);
		toptopPanel.add (SUM_DESC, sumTA);
		
		
		cards.show (toptopPanel, SUM_DESC);
		// /Add DCJ Operations text to cards panel
		snpTA = new JTextArea (box, 0, 0);
		snpTA.setEditable (false);
		snpTA.setFont (font);
		toptopPanel.add (SNP_DESC, snpTA);
		// /Add log text area
		gapTA = new JTextArea (box, 0, 0);
		gapTA.setEditable (false);
		gapTA.setFont (font);
		toptopPanel.add (GAP_DESC, gapTA);
		sumTA.setText ("");
		frame.setVisible (true);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(content);
		scrollPane.setSize(fWIDTH, fHEIGHT);
		scrollPane.setVisible(true);
		frame.add(scrollPane);
		frame.pack();
		
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
	
	private class ChangeCards implements ActionListener {
		public void actionPerformed (ActionEvent e) {
			if (e.getActionCommand () == SUM_CMD) {
				cards.show (toptopPanel, SUM_DESC);
			}
			if (e.getActionCommand () == SNP_CMD) {
				cards.show (toptopPanel, SNP_DESC);
			}
			if (e.getActionCommand () == GAP_CMD) {
				cards.show (toptopPanel, GAP_DESC);
			}
		}// end actionPerformed
	}// end ChangeCards

	

	 private static OutputStream ta2os(final TextArea t)
	    { return new OutputStream()
	       { TextArea ta = t;
	         public void write(int b) //minimum implementation of an OutputStream
	          { byte[] bs = new byte[1]; bs[0] = (byte) b;
	            ta.append(new String(bs));
	          }//write
	       };
	    }
	
	
}
