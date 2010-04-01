package org.gel.mauve.dcjx;

import java.awt.BorderLayout;

import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
	
	private TextArea nwayOut, pwiseOut, nblksOut;
	
	private int fWIDTH = 600;

	private int fHEIGHT = 400;
	
	private Panel toptopPanel;

	private CardLayout cards;

	private String box;

	private String pattern;
	
	
//	private JTextArea log;
	// event listener
	
	public static void launchDCJ(BaseViewerModel model){
		if (model instanceof XmfaViewerModel){
			dcjWindow win = new dcjWindow((XmfaViewerModel)model);
		//	win.loadMatrices((XmfaViewerModel)model);
		//	win.setSize(100, 100);
		//	win.setVisible(true);
		} else {
			System.err.println("Can't compute DCJ distance without contig boundaries.");
			dcjWindow win = new dcjWindow();
			win.nwayOut.append("Can't compute DCJ distances without contig boundaries.");
			win.pwiseOut.append("Can't compute DCJ distances without contig boundaries.");
			win.nblksOut.append("Can't compute DCJ distances without contig boundaries.");
			win.setVisible(true);
		}
		
	}
	
	public dcjWindow (XmfaViewerModel model){
		build();
		loadMatrices(model);	
	}
	
	public dcjWindow (){
		build();
	}

	
	protected void loadMatrices(XmfaViewerModel model){
		String temp = "Running...";
		nwayOut.append(temp);
		pwiseOut.append(temp);
		nblksOut.append(temp);
		
		Vector<Genome> v = model.getGenomes();
		Genome[] genomes = new Genome[v.size()];
		v.toArray(genomes);
		Genome[] pair = new Genome[2];
		String[] nWayPerms = PermutationExporter.getPermStrings(model, genomes);
		String[] pairPerm = null;
		DCJ[][] nWayDist = new DCJ[genomes.length][genomes.length];
		DCJ[][] pWiseDist = new DCJ[genomes.length][genomes.length];
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

		StringBuilder nwaySb  = new StringBuilder();
		StringBuilder pwiseSb = new StringBuilder();
		StringBuilder nblksSb = new StringBuilder();

		nwaySb.append("# DCJ distances based on N-way LCBs : ");
		nwaySb.append("no. of blocks = " + nWayDist[1][0].numBlocks()+" #\n");
		pwiseSb.append("# DCJ distances based on pairwise LCBs #\n");
		nblksSb.append("# Pairwise breakpoint distances #\n");
		nwaySb.append("\n");
		pwiseSb.append("\n");
		nblksSb.append("\n");
		for (int i = 0; i < genomes.length; i++){
			nwaySb.append("# "+(i+1)+": " + genomes[i].getDisplayName()+"\n");
			pwiseSb.append("# "+(i+1)+": " + genomes[i].getDisplayName()+"\n");
			nblksSb.append("# "+(i+1)+": " + genomes[i].getDisplayName()+"\n");
		}
		
		nwaySb.append("\n");
		pwiseSb.append("\n");
		nblksSb.append("\n");
		
		
		for (int i = 0; i < nWayDist.length; i++){
			for (int j = 0; j < i; j++){
				nwaySb.append(nWayDist[i][j].dcjDistance()+"\t");
				pwiseSb.append(pWiseDist[i][j].dcjDistance()+"\t");
				nblksSb.append(pWiseDist[i][j].numBlocks()+"\t");
			}
			nwaySb.append("0\n");
			pwiseSb.append("0\n");
			nblksSb.append("0\n");
		}
		

		nwayOut.replaceRange("", 0, temp.length());
		pwiseOut.replaceRange("", 0, temp.length());
		nblksOut.replaceRange("", 0, temp.length());
		
		
		nwayOut.append(nwaySb.toString());
		
		pwiseOut.append(pwiseSb.toString());
		
		nblksOut.append(nblksSb.toString());
		
		try {
			File file = new File("/Users/andrew/Desktop/DCJ_log.txt");
			if (!file.exists())
				file.createNewFile();
			PrintStream out = new PrintStream(file);
			out.append(nwaySb.toString());
			out.append("\n---------------------------------------------\n---------------------------------------------\n");
			out.close();
		} catch (Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
		
		
	}
	
	// add
	
	
	public void build () {
		JFrame frame = new JFrame ("DCJ");
		frame.setSize (fWIDTH, fHEIGHT);
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
		Button Bnway = new Button ("N-Way");
		Button Bpwise = new Button ("Pairwise");
		Button Bnblks = new Button ("No. Blocks");
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
		nwayOut = new TextArea (box, 25, 40);
		nwayOut.setEditable (false);
		nwayOut.setFont (new Font ("monospaced", Font.PLAIN, 12));
		toptopPanel.add ("Distances based on N-Way LCBs", nwayOut);
		cards.show (toptopPanel, "Distances based on N-Way LCBs");
		// /Add DCJ Operations text to cards panel
		pwiseOut = new TextArea (box, 25, 40);
		pwiseOut.setEditable (false);
		pwiseOut.setFont (new Font ("monospaced", Font.PLAIN, 12));
		toptopPanel.add ("Distances based on Pairwise LCBs", pwiseOut);
		// /Add log text area
		nblksOut = new TextArea (box, 25, 40);
		nblksOut.setEditable (false);
		nblksOut.setFont (new Font ("monospaced", Font.PLAIN, 12));
		toptopPanel.add ("Number of blocks between each pair", nblksOut);
		// //////////////////////////////////////////////////////////////
		// /Create bottom panel
		/** ************************************************************* */
/*		Panel textInputField = new Panel ();
		Panel forTheButtons = new Panel ();
		textInputField.setLayout (new BorderLayout ());
		forTheButtons.setLayout (new GridLayout (1, 0));
		add (textInputField);
		// /create submit button for bottom pane
		Button submitB = new Button ("Submit");
		Button clear = new Button ("Clear");
		forTheButtons.add (submitB);
		forTheButtons.add (clear);
		textInputField.add (forTheButtons, BorderLayout.SOUTH);
		// /Here's the text area for it.
		input = new TextArea ();
		textInputField.add (input);
		// /add listener submitButton
		ClearData clearit = new ClearData ();
		SubmitData submitButton = new SubmitData ();
		// /register listener to button
		submitB.addActionListener (submitButton);
		clear.addActionListener (clearit);

		input.setText (defaultInput);*/
		/** *************************************************************** */
		
		nwayOut.setText ("");
		frame.setVisible (true);
	}

	private class ChangeCards implements ActionListener {
		public void actionPerformed (ActionEvent e) {
			if (e.getActionCommand () == "N-Way") {
				cards.show (toptopPanel, "Distances based on N-Way LCBs");
			}
			if (e.getActionCommand () == "Pairwise") {
				cards.show (toptopPanel, "Distances based on Pairwise LCBs");
			}
			if (e.getActionCommand () == "No. Blocks") {
				cards.show (toptopPanel, "Number of blocks between each pair");
			}
		}// end actionPerformed
	}// end ChangeCards

	private class ClearData implements ActionListener {
		public void actionPerformed (ActionEvent e) {
			//input.setText ("");
			nwayOut.setText ("");
			pwiseOut.setText ("");
			nblksOut.setText ("");
		}// end actionPerformed
	}// end clearData
/*
	private class SubmitData implements ActionListener {

		private Vector parseInput (String s) {
			StringTokenizer token = new StringTokenizer (s, ",");
			StringTokenizer newtoken;
			Vector v = new Vector ();
			String st;
			while (token.hasMoreTokens ()) {
				st = (token.nextToken ().trim ());
				if (st.length () > 0)
					v.add (st);
			}
			return v;

		}// end parseInput

		public void actionPerformed (ActionEvent e) {
			String box2 = "";
			nwayOut.setText (box2);
			pwiseOut.setText (box2);
			blksOut.setText (box2);
			StringBuffer boxa = new StringBuffer (256);
			StringBuffer boxb = new StringBuffer ();
			StringBuffer boxc = new StringBuffer ();
			Vector v = parseInput ((input.getText ()).trim ());
			DCJ d;
			int [][] distances = new int [v.size ()] [v.size ()];
			for (int i = 0; i < v.size (); i++) {
				distances[i][i] = 0;
			}
			for (int i = 0; i < v.size (); i++) {
				for (int j = i + 1; j < v.size (); j++) {
					boxa.append (i
							+ " to "
							+ j
							+ "\n"
							+ (d = new DCJ (new StringTokenizer ((String) v
									.elementAt (i), "$"), new StringTokenizer (
									(String) v.elementAt (j), "$"))).getLog ());
					distances[i][j] = d.getCount ();
					distances[j][i] = d.getCount ();
					boxc.append ("\n" + i + " to " + j + "\n");
					boxc.append (d.getOpBuf ());
				}// end for j
			}// end for i
			for (int i = 0; i < v.size (); i++) {
				for (int j = 0; j < v.size (); j++) {
					boxb.append (distances[i][j] + "	");
				}
				boxb.append ("\n");
			}
			output.setText (boxb.toString ());
			ops.setText (boxc.toString ());
			log.setText (boxa.toString ());

		}// end actionPerformed

	}// end SubmitData */
		
}
