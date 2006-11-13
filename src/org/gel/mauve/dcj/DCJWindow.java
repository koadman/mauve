package org.gel.mauve.dcj;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.text.*;
import java.applet.Applet;


public class DCJWindow extends JWindow {

    TextArea input,output,log,ops;
	int fWIDTH=600;
	int fHEIGHT=300;
    Panel toptopPanel;
    CardLayout cards;
    String box;
    String pattern;
	String defaultInput=""+"1 -32 17 2 23 12 3 20 6 30 7 8 21 31 24 9 -10 -18 11 33 -28 19 14 34 13 25 4 22 -29 26 5 35 -15 -27 -16 -36 $,\n1 30 7 2 23 12 3 -32 6 8 21 31 9 -10 11 19 14 18 33 -13 -5 -22 -4 -25 -20 -36 17 -26 34 -16 -35 15 -24 -27 29 -28 $,\n1 30 7 2 23 12 3 -32 6 8 21 31 9 -10 11 19 14 18 33 28 -29 27 24 -15 35 16 -34 26 -17 36 20 25 4 22 5 13 $,\n1 25 2 23 17 12 3 20 6 15 30 27 31 18 -19 -9 -21 -8 -7 33 -28 10 11 32 -4 -24 -13 -34 -14 22 -29 26 5 35 -16 -36 $,\n1 25 2 23 17 12 3 20 6 15 30 27 31 18 -19 -9 -21 -8 -7 33 -28 10 11 32 -4 -24 -13 -34 -14 26 5 35 -22 -29 -16 -36 $,\n1 34 13 24 28 15 10 9 4 7 11 17 16 19 2 36 35 20 21 -29 -25 -27 -12 -30 -18 -14 -26 -6 -32 31 8 -33 -3 22 5 23 $,\n1 34 13 24 15 10 28 9 4 7 11 17 16 19 2 36 35 20 21 -29 -25 -27 -12 -30 -18 -14 26 -6 -32 -33 -3 31 8 22 5 23 $,\n1 17 2 12 -19 -9 -21 -8 -7 33 -32 -11 -10 28 -4 -25 -24 -13 -34 -14 -26 -16 -36 -35 -29 -20 -18 3 23 15 30 27 22 6 31 5 $,\n1 27 2 17 36 20 3 29 10 11 35 12 30 21 9 19 18 28 33 7 8 16 26 14 34 13 24 15 32 25 4 22 23 6 31 5 $,\n1 16 26 17 20 2 21 13 6 9 15 28 34 10 7 35 18 14 32 27 36 4 12 23 25 31 5 22 30 29 19 11 24 3 33 8 $,\n1 35 10 30 29 11 24 3 23 15 25 27 26 7 14 36 4 19 12 22 20 2 21 13 6 16 32 28 17 34 9 18 31 5 33 8";

	public static void startDCJ(String s){
		DCJWindow w=new DCJWindow();
		w.performDCJ(s);
	}

	private Vector parseInput(String s){
		StringTokenizer token=new StringTokenizer(s,",");
		StringTokenizer newtoken;
		Vector v=new Vector();
		String st;
		while(token.hasMoreTokens()){
			st=(token.nextToken().trim());
			if(st.length()>0)v.add(st);
		}
		return v;

	}//end parseInput
	
	public void performDCJ(String s){		
		build();
		String box2="";
		output.setText(box2);
		log.setText(box2);
		StringBuffer boxa=new StringBuffer(256);
		StringBuffer boxb=new StringBuffer();
		StringBuffer boxc=new StringBuffer();
		Vector v=parseInput(s.trim());
		DCJ d;
		int[][] distances=new int[v.size()][v.size()];
		for(int i=0;i<v.size();i++){distances[i][i]=0;}
		for(int i=0;i<v.size();i++){
			for(int j=i+1;j<v.size();j++){
				boxa.append(i+" to "+j+"\n"+(d=new DCJ(new StringTokenizer((String)v.elementAt(i),"$"),new StringTokenizer((String)v.elementAt(j),"$"))).getLog());
				distances[i][j]=d.getCount();
				distances[j][i]=d.getCount();
				boxc.append("\n"+i+" to "+j+"\n");
				boxc.append(d.getOpBuf());
			}//end for j
		}//end for i
		for(int i=0;i<v.size();i++){
			for(int j=0;j<v.size();j++){
				boxb.append(distances[i][j]+"	");
			}
			boxb.append("\n");
		}
		output.setText(boxb.toString());
		ops.setText(boxc.toString());
		log.setText(boxa.toString());
		
	}//end actionPerformed

    public void build() {
	JFrame frame=new JFrame("DCJ");
	frame.setSize(fWIDTH,fHEIGHT);
	JPanel content=new JPanel(new BorderLayout());
	frame.getContentPane().add(content,BorderLayout.CENTER);
	box="";
	setLayout(new BorderLayout());
///create top panel
	Panel topPanel=new Panel();

	topPanel.setLayout(new BorderLayout());
//top top panel with cards
	cards=new CardLayout();
	toptopPanel=new Panel();
	toptopPanel.setLayout(cards);
	topPanel.add(toptopPanel,BorderLayout.CENTER);
//top lower panel of buttons
	Panel toplowerPanel=new Panel();
	GridLayout butts=new GridLayout(1,0);
	butts.setHgap(50);
	toplowerPanel.setLayout(butts);
	topPanel.add(toplowerPanel,BorderLayout.SOUTH);
//make buttons
	Button Bout=new Button("matrix");
	Button Bop=new Button("operations");
	Button Blog=new Button("log");
	toplowerPanel.add(Bout);
	toplowerPanel.add(Bop);
	toplowerPanel.add(Blog);
//make listener
	ChangeCards cc=new ChangeCards();
//register buttons
	Bout.addActionListener(cc);
	Blog.addActionListener(cc);
	Bop.addActionListener(cc);
///add the top panel
      add(topPanel,BorderLayout.NORTH);
content.add(topPanel,BorderLayout.CENTER);

///Add output text to cards panel
      	output = new TextArea(box,25, 40);
	output.setEditable(false);
	output.setFont(new Font("monospaced",Font.ITALIC,12));
	toptopPanel.add("matrix",output);
	cards.show(toptopPanel,"matrix");
///Add DCJ Operations text to cards panel
      	ops = new TextArea(box,25, 40);
	ops.setEditable(false);
	ops.setFont(new Font("monospaced",Font.PLAIN,12));
	toptopPanel.add("operations",ops);
///Add log text area
	log=new TextArea("",25,40);
	log.setEditable(false);
	log.setFont(new Font("monospaced",Font.PLAIN,12));
	toptopPanel.add("log",log);
////////////////////////////////////////////////////////////////
///Create bottom panel
/****************************************************************/
	Panel textInputField=new Panel();
	Panel forTheButtons=new Panel();
	textInputField.setLayout(new BorderLayout());
	forTheButtons.setLayout(new GridLayout(1,0));
	add(textInputField);
///create submit button for bottom pane
	Button submitB=new Button("Submit");
	Button clear=new Button("Clear");
	forTheButtons.add(submitB);
	forTheButtons.add(clear);
	textInputField.add(forTheButtons, BorderLayout.SOUTH);
///Here's the text area for it.
	input=new TextArea();
	textInputField.add(input);
///add listener submitButton
	ClearData clearit=new ClearData();
	SubmitData submitButton=new SubmitData();
///register listener to button
	submitB.addActionListener(submitButton);
	clear.addActionListener(clearit);

	input.setText(defaultInput);
/******************************************************************/
	output.setText("");
	frame.setVisible(true);
    }
    private class ChangeCards implements ActionListener{
	public void actionPerformed(ActionEvent e){
		if (e.getActionCommand()=="matrix"){cards.show(toptopPanel,"matrix");}
		if (e.getActionCommand()=="log"){cards.show(toptopPanel,"log");}
		if (e.getActionCommand()=="operations"){cards.show(toptopPanel,"operations");}
	}//end actionPerformed
    }//end ChangeCards

    private class ClearData implements ActionListener{
	public void actionPerformed(ActionEvent e){
		input.setText("");
		output.setText("");
		log.setText("");
	}//end actionPerformed
    }//end clearData

    private class SubmitData implements ActionListener{

	private Vector parseInput(String s){
		StringTokenizer token=new StringTokenizer(s,",");
		StringTokenizer newtoken;
		Vector v=new Vector();
		String st;
		while(token.hasMoreTokens()){
			st=(token.nextToken().trim());
			if(st.length()>0)v.add(st);
		}
		return v;
		
		
	}//end parseInput
	
	public void actionPerformed(ActionEvent e){
		String box2="";
		output.setText(box2);
		log.setText(box2);
		StringBuffer boxa=new StringBuffer(256);
		StringBuffer boxb=new StringBuffer();
		StringBuffer boxc=new StringBuffer();
		Vector v=parseInput((input.getText()).trim());
		DCJ d;
		int[][] distances=new int[v.size()][v.size()];
		for(int i=0;i<v.size();i++){distances[i][i]=0;}
		for(int i=0;i<v.size();i++){
			for(int j=i+1;j<v.size();j++){
				boxa.append(i+" to "+j+"\n"+(d=new DCJ(new StringTokenizer((String)v.elementAt(i),"$"),new StringTokenizer((String)v.elementAt(j),"$"))).getLog());
				distances[i][j]=d.getCount();
				distances[j][i]=d.getCount();
				boxc.append("\n"+i+" to "+j+"\n");
				boxc.append(d.getOpBuf());
			}//end for j
		}//end for i
		for(int i=0;i<v.size();i++){
			for(int j=0;j<v.size();j++){
				boxb.append(distances[i][j]+"	");
			}
			boxb.append("\n");
		}
		output.setText(boxb.toString());
		ops.setText(boxc.toString());
		log.setText(boxa.toString());
		
		}//end actionPerformed

    }//end SubmitData


}