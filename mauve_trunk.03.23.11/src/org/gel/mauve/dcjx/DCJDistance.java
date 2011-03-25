package org.gel.mauve.dcjx;

import java.awt.CardLayout;
import java.awt.Panel;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JTextArea;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Genome;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.analysis.PermutationExporter;
import org.gel.mauve.gui.AnalysisDisplayWindow;

public class DCJDistance {

//	private JTextArea matrix;
	
	private static final String NWAY_COMMAND = "N-Way";
	private static final String NWAY_DESC = "Distances based on N-Way LCBs";
	private static final String PWISE_COMMAND = "Pairwise";
	private static final String PWISE_DESC = "Distances based on Pairwise LCBs";
	private static final String NBLKS_COMMAND = "NoBlocks";
	private static final String NBLKS_DESC = "Number of blocks between each pair";
	
	private static final String temp = "Running...";
	private static final String error = "Error computing DCJ distances! Please report bug to atritt@ucdavis.edu";
	
	private static HashMap<String,DCJDistance> modelMap;
	
	private JTextArea nwayTA, pwiseTA;
	
	private AnalysisDisplayWindow adw;
	
	private int fWIDTH = 400;

	private int fHEIGHT = 400;
	
	private DCJ[][] nWayDist;
	
	private DCJ[][] pWiseDist;
	
	private static DCJDistance curr;
	
	
//	private JTextArea log;
	// event listener
	
	public static void launchWindow(BaseViewerModel model) {
		if (modelMap == null)
			modelMap = new HashMap<String,DCJDistance>();
		String key = model.getSrc().getAbsolutePath();
		if (modelMap.containsKey(key)) {	
			modelMap.get(key).adw.showWindow();
		}else if (model instanceof XmfaViewerModel){
			modelMap.put(key, new DCJDistance((XmfaViewerModel)model));
		} else {
			System.err.println("Can't compute DCJ distance without contig boundaries.");
		}	
	}
	
	public DCJDistance (XmfaViewerModel model) {
		int numGenomes = model.getGenomes().size();
		// lets have some fun with interfaces
		DistanceFunction dcj = new DistanceFunction(){
			public int distance(DCJ dcj) { return dcj.dcjDistance();}
		};
		DistanceFunction scj = new DistanceFunction(){
			public int distance(DCJ dcj){ return dcj.scjDistance();}
		};
		DistanceFunction bp = new DistanceFunction(){
			public int distance(DCJ dcj){ return dcj.bpDistance();}
		};
		if (numGenomes > 2){
			adw = new AnalysisDisplayWindow("DCJ - "+model.getSrc().getName(), fWIDTH, fHEIGHT);
			nwayTA = adw.addContentPanel(NWAY_COMMAND, NWAY_DESC, true);
			pwiseTA = adw.addContentPanel(PWISE_COMMAND, PWISE_DESC, false);
			adw.showWindow();
			Vector<Genome> v = model.getGenomes();
			Genome[] genomes = v.toArray(new Genome[v.size()]);
			int numGen = genomes.length;
			nWayDist = new DCJ[numGen][numGen];
			pWiseDist = new DCJ[numGen][numGen];
			nwayTA.append(temp);
			pwiseTA.append(temp);
			try {
				loadMatrices(model, nWayDist, pWiseDist);
				nwayTA.replaceRange("", 0, temp.length());
				pwiseTA.replaceRange("", 0, temp.length());
				nwayTA.append("# Rearrangement distances based on N-way LCBs\n");
				nwayTA.append("# No. of blocks = " + nWayDist[1][0].numBlocks()+" #\n\n");
				pwiseTA.append("# Rearrangement distances based on pairwise LCBs #\n\n");
				printHeader(pwiseTA,model);
				printHeader(nwayTA,model);
			//	PrintStream out = new PrintStream(textArea2OutputStream(nwayOut));
				nwayTA.append("\n");
				pwiseTA.append("\n");
				PrintStream out = new PrintStream(textArea2OutputStream(nwayTA));
				out.print("# DCJ distance\n");
				printDistance(out, nWayDist, dcj);
				out.print("\n# SCJ distance\n");
				printDistance(out, nWayDist, scj);
				out.print("\n# Breakpoint distance\n");
				printDistance(out, nWayDist, bp);
				DistanceFunction nblks = new DistanceFunction(){
					public int distance(DCJ dcj){ return dcj.numBlocks();}
				};
				out = new PrintStream(textArea2OutputStream(pwiseTA));
				out.print("# Number of Blocks\n");
				printDistance(out, pWiseDist, nblks);
				out.print("\n# DCJ distance\n");
				printDistance(out, pWiseDist, dcj);
				out.print("\n# SCJ distance\n");
				printDistance(out, pWiseDist, scj);
				out.print("\n# Breakpoint distance\n");
				printDistance(out, pWiseDist, bp);
			} catch (Exception e){
				nwayTA.replaceRange(error, 0, temp.length());
				pwiseTA.replaceRange(error, 0, temp.length());
				e.printStackTrace();
			}
		} else {
			adw = new AnalysisDisplayWindow("DCJ - "+model.getSrc().getName(), fWIDTH, fHEIGHT);
			nwayTA = adw.addContentPanel("DCJ", NWAY_DESC, true);
			adw.showWindow();
			nwayTA.append(temp);
			try {
				String[] perms = PermutationExporter.getPermStrings(model,true);
				DCJ dcjDist = new DCJ(perms[0], perms[1]);
				nwayTA.replaceRange("", 0, temp.length());
				StringBuilder sb = new StringBuilder();
				sb.append("no. of blocks = " + dcjDist.numBlocks()+" #\n\n");
				sb.append("# DCJ distance\n");
				sb.append("0\n"+dcjDist.dcjDistance()+"\t0\n\n");
				sb.append("# SCJ distance\n");
				sb.append("0\n"+dcjDist.scjDistance()+"\t0\n\n");
				sb.append("# Breakpoint distance\n");
				sb.append("0\n"+dcjDist.bpDistance()+"\t0\n\n");
				nwayTA.setText(sb.toString());
			} catch (Exception e){
				nwayTA.replaceRange(error, 0, temp.length());
				System.err.println(error);
				e.printStackTrace();
				
			}
		}
		
	}

	
	private static void printNBlks(PrintStream out, DCJ[][] mat){
		for (int i = 0; i < mat.length; i++){
			for (int j = 0; j < i; j++){
				out.print(mat[i][j].numBlocks()+"\t");
			}
			out.print("0\n");
		}
	}
	
	private static void printDistance(PrintStream out, DCJ[][] mat, DistanceFunction func){
		for (int i = 0; i < mat.length; i++){
			for (int j = 0; j < i; j++){
				out.print(func.distance(mat[i][j])+"\t");
			}
			out.print("0\n");
		}
	}
	
	private static void printHeader(JTextArea nblksTA2, XmfaViewerModel model){
		Vector<Genome> v = model.getGenomes();
		Genome[] genomes = v.toArray(new Genome[v.size()]);
		for (int i = 0; i < genomes.length; i++){
			nblksTA2.append("# "+(i+1)+": " + genomes[i].getDisplayName()+"\n");
		}
	}
	
	private static void printHeader(StringBuilder sb, XmfaViewerModel model){
		Vector<Genome> v = model.getGenomes();
		Genome[] genomes = v.toArray(new Genome[v.size()]);
		for (int i = 0; i < genomes.length; i++){
			sb.append("# "+(i+1)+": " + genomes[i].getDisplayName()+"\n");
		}
	}
	
	private void loadMatrices(XmfaViewerModel model, DCJ[][] nway, DCJ[][] pwise){
		Vector<Genome> v = model.getGenomes();
		Genome[] genomes = new Genome[v.size()];
		v.toArray(genomes);
		Genome[] pair = new Genome[2];
		String[] nWayPerms = PermutationExporter.getPermStrings(model, genomes,true);
		String[] pairPerm = null;
		DCJ[][] nWayDist = nway;
		DCJ[][] pWiseDist = pwise;
		for (int i = 0; i < genomes.length; i++){
			for (int j = 0; j < i; j++){
				pair[0] = genomes[i];
				pair[1] = genomes[j];
				pairPerm = PermutationExporter.getPermStrings(model, pair,true);
				
				if (!Permutation.equalContents(pairPerm[0], pairPerm[1])){
					System.err.println("Unequal contents between genomes "+
							i + " and " + j+"\n" + pairPerm[0] +"\n"+pairPerm[1]);
				}		
				nWayDist[i][j] = new DCJ(nWayPerms[i],nWayPerms[j]);
				pWiseDist[i][j] = new DCJ(pairPerm[0],pairPerm[1]);
			}
		}		
	}
	
	// add
	


	OutputStream textArea2OutputStream(final JTextArea t)
	    { return new OutputStream()
	       { JTextArea ta = t;
	         public void write(int b) //minimum implementation of an OutputStream
	          { byte[] bs = new byte[1]; bs[0] = (byte) b;
	            ta.append(new String(bs));
	          }//write
	       };
	    }
	
	private interface DistanceFunction {
		public int distance(DCJ dcj);
	}

}
