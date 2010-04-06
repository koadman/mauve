package org.gel.mauve.dcjx;

import java.awt.BorderLayout;


import gr.zeus.ui.JConsole;


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
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JWindow;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Genome;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.analysis.PermutationExporter;

public class dcjWindow extends JFrame {

//	private JTextArea matrix;
	
	private static final String NWAY_COMMAND = "N-Way";
	
	private static final String NWAY_DESC = "Distances based on N-Way LCBs";
	
	private static final String PWISE_COMMAND = "Pairwise";
	
	private static final String PWISE_DESC = "Distances based on Pairwise LCBs";
	
	private static final String NBLKS_COMMAND = "NoBlocks";
	
	private static final String NBLKS_DESC = "Number of blocks between each pair";
	
	private static final String temp = "Running...";
	
	private static final String error = "Error computing DCJ distances! Please report bug to atritt@ucdavis.edu";
	
	private TextArea nwayTA, pwiseTA, nblksTA;
	
	private int fWIDTH = 400;

	private int fHEIGHT = 400;
	
	private Panel toptopPanel;

	private CardLayout cards;

	private String box;
	
	private DCJ[][] nWayDist;
	
	private DCJ[][] pWiseDist;
	
	
//	private JTextArea log;
	// event listener
	
	public static void launchDCJ(BaseViewerModel model) {
		if (model instanceof XmfaViewerModel){
			dcjWindow win = new dcjWindow((XmfaViewerModel)model);
		} else {
			System.err.println("Can't compute DCJ distance without contig boundaries.");
		}
		
	}
	
	public dcjWindow (XmfaViewerModel model) {
		int numGenomes = model.getGenomes().size();
		if (numGenomes > 2){
			build(model);
			Vector<Genome> v = model.getGenomes();
			Genome[] genomes = v.toArray(new Genome[v.size()]);
			int numGen = genomes.length;
			nWayDist = new DCJ[numGen][numGen];
			pWiseDist = new DCJ[numGen][numGen];
			nwayTA.append(temp);
			pwiseTA.append(temp);
			nblksTA.append(temp);
			try {
				loadMatrices(model, nWayDist, pWiseDist);
				nwayTA.replaceRange("", 0, temp.length());
				pwiseTA.replaceRange("", 0, temp.length());
				nblksTA.replaceRange("", 0, temp.length());
				nwayTA.append("# DCJ distances based on N-way LCBs : ");
				nwayTA.append("no. of blocks = " + nWayDist[1][0].numBlocks()+" #\n\n");
				pwiseTA.append("# DCJ distances based on pairwise LCBs #\n\n");
				nblksTA.append("# Pairwise breakpoint distances #\n\n");
				printHeader(nblksTA,model);
				printHeader(pwiseTA,model);
				printHeader(nwayTA,model);
			//	PrintStream out = new PrintStream(textArea2OutputStream(nwayOut));
				nwayTA.append("\n");
				pwiseTA.append("\n");
				nblksTA.append("\n");
		
				PrintStream out = new PrintStream(textArea2OutputStream(nwayTA));
				printDist(out,nWayDist);
				out = new PrintStream(textArea2OutputStream(pwiseTA));
				printDist(out, pWiseDist);
				out = new PrintStream(textArea2OutputStream(nblksTA));
				printNBlks(out, pWiseDist);
			} catch (Exception e){
				nwayTA.replaceRange(error, 0, temp.length());
				pwiseTA.replaceRange(error, 0, temp.length());
				nblksTA.replaceRange(error, 0, temp.length());
				e.printStackTrace();
			}
		} else {
			build2Gen(model);
			nwayTA.append(temp);
			try {
				String[] perms = PermutationExporter.getPermStrings(model);
				DCJ dcj = new DCJ(perms[0], perms[1]);
				nwayTA.replaceRange("", 0, temp.length());
				nwayTA.append("# DCJ distance : ");
				nwayTA.append("no. of blocks = " + dcj.numBlocks()+" #\n\n");
				printHeader(nwayTA, model);
				nwayTA.append("\n0\n"+dcj.dcjDistance()+"\t0\n");
			} catch (Exception e){
				nwayTA.replaceRange(error, 0, temp.length());
				e.printStackTrace();
				
			}
		}
		
	}

	private static void printDist(PrintStream out, DCJ[][] mat){
		for (int i = 0; i < mat.length; i++){
			for (int j = 0; j < i; j++){
				out.print(mat[i][j].dcjDistance()+"\t");
			}
			out.print("0\n");
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
	
	private static void printHeader(TextArea out, XmfaViewerModel model){
		Vector<Genome> v = model.getGenomes();
		Genome[] genomes = v.toArray(new Genome[v.size()]);
		for (int i = 0; i < genomes.length; i++){
			out.append("# "+(i+1)+": " + genomes[i].getDisplayName()+"\n");
		}
	}
	
	private void loadMatrices(XmfaViewerModel model, DCJ[][] nway, DCJ[][] pwise){
		Vector<Genome> v = model.getGenomes();
		Genome[] genomes = new Genome[v.size()];
		v.toArray(genomes);
		Genome[] pair = new Genome[2];
		String[] nWayPerms = PermutationExporter.getPermStrings(model, genomes);
		String[] pairPerm = null;
		DCJ[][] nWayDist = nway;
		DCJ[][] pWiseDist = pwise;
		for (int i = 0; i < genomes.length; i++){
			for (int j = 0; j < i; j++){
				pair[0] = genomes[i];
				pair[1] = genomes[j];
				pairPerm = PermutationExporter.getPermStrings(model, pair);
				
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
	
	
	private void build (XmfaViewerModel model) {
		//JConsole jcons = new  JConsole
		JFrame frame = new JFrame ("DCJ - " + model.getSrc().getName());
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int xPos = dim.width - fWIDTH;
		frame.setSize (fWIDTH, fHEIGHT);
		frame.setLocation(xPos, 0);
		JPanel content = new JPanel (new BorderLayout ());
		frame.getContentPane ().add (content, BorderLayout.CENTER);
		box = "";
		setLayout (new BorderLayout ());
		// /create top panel
		Panel topPanel = new Panel ();

		topPanel.setLayout (new BorderLayout ());
		// top top panel with cards
		cards = new CardLayout ();
		toptopPanel = new Panel ();
		toptopPanel.setLayout (cards);
		topPanel.add (toptopPanel, BorderLayout.CENTER);
		// top lower panel of buttons
		Panel toplowerPanel = new Panel ();
		GridLayout butts = new GridLayout (1, 0);
		butts.setHgap (50);
		toplowerPanel.setLayout (butts);
		topPanel.add (toplowerPanel, BorderLayout.SOUTH);
		// make buttons
		Button Bnway = new Button (NWAY_COMMAND);
		Button Bpwise = new Button (PWISE_COMMAND);
		Button Bnblks = new Button (NBLKS_COMMAND);
		toplowerPanel.add (Bnway);
		toplowerPanel.add (Bpwise);
		toplowerPanel.add (Bnblks);
		// make listener
		ChangeCards cc = new ChangeCards ();
		// register buttons
		Bnway.addActionListener (cc);
		Bpwise.addActionListener (cc);
		Bnblks.addActionListener (cc);
		// /add the top panel
		add (topPanel, BorderLayout.NORTH);
		content.add (topPanel, BorderLayout.CENTER);

		// /Add output text to cards panel
		nwayTA = new TextArea (box, 25, 40);
		nwayTA.setEditable (false);
		nwayTA.setFont (new Font ("monospaced", Font.PLAIN, 12));
		toptopPanel.add (NWAY_DESC, nwayTA);
		cards.show (toptopPanel, NWAY_DESC);
		// /Add DCJ Operations text to cards panel
		pwiseTA = new TextArea (box, 25, 40);
		pwiseTA.setEditable (false);
		pwiseTA.setFont (new Font ("monospaced", Font.PLAIN, 12));
		toptopPanel.add (PWISE_DESC, pwiseTA);
		// /Add log text area
		nblksTA = new TextArea (box, 25, 40);
		nblksTA.setEditable (false);
		nblksTA.setFont (new Font ("monospaced", Font.PLAIN, 12));
		toptopPanel.add (NBLKS_DESC, nblksTA);
		nwayTA.setText ("");
		frame.setVisible (true);
	}
	
	private void build2Gen (XmfaViewerModel model) {
		JFrame frame = new JFrame ("DCJ - " + model.getSrc().getName());
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int xPos = dim.width - fWIDTH;
		frame.setSize (fWIDTH, fHEIGHT);
		frame.setLocation(xPos, 0);
		JPanel content = new JPanel (new BorderLayout ());
		frame.getContentPane ().add (content, BorderLayout.CENTER);
		box = "";
		setLayout (new BorderLayout ());
		// /create top panel
		Panel topPanel = new Panel ();

		topPanel.setLayout (new BorderLayout ());
		// top top panel with cards
		cards = new CardLayout ();
		toptopPanel = new Panel ();
		toptopPanel.setLayout (cards);
		topPanel.add (toptopPanel, BorderLayout.CENTER);
		// /add the top panel
		add (topPanel, BorderLayout.NORTH);
		content.add (topPanel, BorderLayout.CENTER);

		// /Add output text to cards panel
		nwayTA = new TextArea (box, 25, 40);
		nwayTA.setEditable (false);
		nwayTA.setFont (new Font ("monospaced", Font.PLAIN, 12));
		toptopPanel.add (NWAY_DESC, nwayTA);
		cards.show (toptopPanel, NWAY_DESC);
		nwayTA.setText ("");
		frame.setVisible (true);
	}

	private class ChangeCards implements ActionListener {
		public void actionPerformed (ActionEvent e) {
			if (e.getActionCommand().equalsIgnoreCase(NWAY_COMMAND)) {
				cards.show (toptopPanel, NWAY_DESC);
			}
			if (e.getActionCommand().equalsIgnoreCase(PWISE_COMMAND)) {
				cards.show (toptopPanel, PWISE_DESC);
			}
			if (e.getActionCommand().equalsIgnoreCase(NBLKS_COMMAND)) {
				cards.show (toptopPanel, NBLKS_DESC);
			}
		}// end actionPerformed
	}// end ChangeCards

	

	 public OutputStream textArea2OutputStream(final TextArea t)
	    { return new OutputStream()
	       { TextArea ta = t;
	         public void write(int b) //minimum implementation of an OutputStream
	          { byte[] bs = new byte[1]; bs[0] = (byte) b;
	            ta.append(new String(bs));
	          }//write
	       };
	    }
		
}
