package org.gel.mauve.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.gel.air.gui.GuiUtils;
import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.MauveHelperFunctions;

public class GuideTree {
	
	protected LinkedList <DefaultMutableTreeNode> bottom_pairs;
	protected DefaultMutableTreeNode root;
	
	protected GuideTree () {
		bottom_pairs = new LinkedList <DefaultMutableTreeNode> ();
	}
	public GuideTree (File file) {
		this ();
		readFile (file);
	}
	
	public LinkedList<DefaultMutableTreeNode> getBottomPairs() {
		return bottom_pairs;
	}
	public DefaultMutableTreeNode getRoot() {
		return root;
	}
	protected void readFile (File file) {
		StringTokenizer toke;
		try {
			BufferedReader in = new BufferedReader (new FileReader (file));
			toke = new StringTokenizer (in.readLine(), "(:),;", true);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		Stack tokens = new Stack <String> ();
		while (toke.hasMoreTokens()) {
			String token = toke.nextToken();
			System.out.print (token);
			if (token.equals(":"))
				toke.nextToken();
			else if (token.equals(",") || token.equals("("))
				;
			else if (token.startsWith("seq"))
				tokens.push(new DefaultMutableTreeNode (new Integer (
						token.substring(3))));
			else if (token.equals(")")) {
				boolean bottom = true;
				DefaultMutableTreeNode parent = new DefaultMutableTreeNode ();
				DefaultMutableTreeNode kid = (DefaultMutableTreeNode) tokens.pop();
				parent.add(kid);
				bottom = kid.getUserObject() != null;
				kid = (DefaultMutableTreeNode) tokens.pop();
				parent.add(kid);
				bottom = bottom && kid.getUserObject() != null;
				tokens.push(parent);
				if (bottom) {
					if (parent.getChildCount() > 1 &&
							parent.getChildAt(0).isLeaf())
						bottom_pairs.add(parent);
				}
			}
			else if (token.equals(";")) {
				root = (DefaultMutableTreeNode) tokens.pop();
				break;
			}
		}
		if (!tokens.empty ())
			System.out.println ("unbalanced stack");
		if (root == null)
			System.out.println ("no root");
	}
	
	public void display () {
		GuiUtils.display(new JScrollPane(new JTree (root)));
	}
	
	public static GuideTree fromBaseModel (BaseViewerModel model) {
		return new GuideTree (new File (MauveHelperFunctions.getRootDirectory(
				model), MauveHelperFunctions.getFileStub(model) + ".guide_tree"));
	}
	
	public static void main (String args []) {
		new GuideTree (new File (args [0])).display ();
	}

}
