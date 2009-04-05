package org.gel.mauve.operon;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.biojava.bio.seq.Feature;
import org.gel.air.gui.GuiUtils;
import org.gel.mauve.Genome;
import org.gel.mauve.analysis.GuideTree;
import org.gel.mauve.gui.MauveFrame;
import org.gel.mauve.gui.SequenceNavigator;
import org.gel.mauve.operon.AncestralState.Difference;
import org.gel.mauve.operon.AncestralState.DifferentOperon;

/*
 * does not currently take multiple features into account
 */
public class OperonTree extends JTree implements TreeSelectionListener,
		ActionListener {

	protected PhyloOperon phylo;
	protected GuideTree guide_tree;
	protected SequenceNavigator navigator;
	protected DefaultMutableTreeNode root;
	
	protected JLabel location;
	protected JTextField search;
	protected String last_search;
	protected Vector <DefaultMutableTreeNode> search_results;
	protected int search_index;
	
	public OperonTree (MauveFrame frame, PhyloOperon ph, GuideTree tree) {
		phylo = ph;
		guide_tree = tree;
		navigator = frame.getPanel ().getNavigator ();
		setModel (new DefaultTreeModel (root = buildRootNode ()));
		addTreeSelectionListener (this);
	}
	
	protected DefaultMutableTreeNode buildRootNode () {
		DefaultMutableTreeNode root = guide_tree.getRoot(); 
		addChildren (root);
		return root;
	}
	
	protected HashSet <Integer> addChildren (DefaultMutableTreeNode node) {
		HashSet <Integer> seqs = new HashSet <Integer> (); 
		if (!node.isLeaf()) {
			HashSet <Integer> left = addChildren ((DefaultMutableTreeNode)
					node.getChildAt(0)); 
			seqs.addAll (left);
			HashSet <Integer> right = addChildren ((DefaultMutableTreeNode)
					node.getChildAt(1));
			seqs.addAll (right);
			AncestralState state = (AncestralState) node.getUserObject();
			state.seqs = seqs;
			populateSames (node, state, seqs);
			populateDiffs (node, left, right, "present but absent in one child", state.differences2);
			populateDiffs (node, left, right, "absent but present in one child", state.differences);
		}
		else {
			Operon op = (Operon) node.getUserObject();
			seqs.add(op.seq);
			node.setUserObject(new LeafSeqOps (op));
			populateLeaf (node, op);
		}
		return seqs;
	}
	
	protected void populateSames (DefaultMutableTreeNode node,
			AncestralState state, HashSet <Integer> seqs) {
		DefaultMutableTreeNode sames_node = new DefaultMutableTreeNode (
				state.sames.size () + " present operons");
		DefaultMutableTreeNode unclears_node = new DefaultMutableTreeNode (
				state.unclears.size () + " possibly present");
		node.add (sames_node);
		node.add (unclears_node);
		Iterator <Operon> itty = state.sames.keySet().iterator();
		while (itty.hasNext ()) {
			Operon op = itty.next ();
			DefaultMutableTreeNode op_node = new DefaultMutableTreeNode (
					new LeadOperon (op));
			if (state.unclears.containsKey(op)) {
				DefaultMutableTreeNode unclear_node = new DefaultMutableTreeNode (
						new UnclearOperon (op, op_node));
				unclears_node.add (unclear_node);
				populateSames (unclear_node, op, seqs);
			}
			else {
				sames_node.add (op_node);
				populateSames (op_node, op, seqs);
			}
		}
	}
	
	protected void populateDiffs (DefaultMutableTreeNode node,
			HashSet <Integer> lefts, HashSet <Integer> rights, String display,
			Hashtable <Operon, Hashtable <String, DifferentOperon>> differences) {
		DefaultMutableTreeNode diffs_node = new DefaultMutableTreeNode (
				differences.size () + " " + display);
		node.add (diffs_node);
		Iterator <Operon> itty = differences.keySet().iterator(); 
		while (itty.hasNext ()) {
			Operon op = itty.next();
			DefaultMutableTreeNode op_node = new DefaultMutableTreeNode (
					new LeadOperon (op));
			diffs_node.add(op_node);
			HashSet <Integer> current = rights.contains(op.seq)
					? rights : lefts;
			DefaultMutableTreeNode sub_same = new DefaultMutableTreeNode (
					"conserved operons");
			op_node.add (sub_same);
			if (phylo.sames.containsKey(op))
				populateSames (sub_same, op, current);
			
			current = current == rights ? lefts : rights;
			DifferentOperon diff_op = differences.get(op).values ().iterator().next ();
			DefaultMutableTreeNode diff_node = new DefaultMutableTreeNode (
					diff_op.diffs.values ().iterator().next ());
			op_node.add (diff_node);
			DefaultMutableTreeNode seq_list = new DefaultMutableTreeNode (
					current);
			diff_node.add(seq_list);
		}
	}
	
	protected void populateSames (DefaultMutableTreeNode op_node,
			Operon op, HashSet <Integer> seqs) {
		Iterator <Operon> others = phylo.sames.get(op).values (
				).iterator().next ().iterator ();
		HashSet <Integer> sames = new HashSet <Integer> ();
		while (others.hasNext()) {
			Operon other = others.next ();
			if (seqs.contains(other.seq)) {
				sames.add (other.seq);
			}
		}
		DefaultMutableTreeNode child_node = 
			new DefaultMutableTreeNode (sames);
		op_node.add (child_node);
	}
		
	protected void populateLeaf (DefaultMutableTreeNode node,
			Operon first) {
		Operon.OpIterator itty = new Operon.OpIterator (first);
		while (itty.hasNext()) {
			Operon op = itty.next ();
			DefaultMutableTreeNode op_node = new DefaultMutableTreeNode (op);
			node.add(op_node);
		}
	}
	
	public void display () {
		JPanel panel = new JPanel (new BorderLayout ());
		panel.add (new JScrollPane(this));
		search = new JTextField ();
		JButton button = new JButton ("Next");
		search.addActionListener (this);
		button.addActionListener(this);
		JPanel south = new JPanel (new BorderLayout());
		south.add(search);
		south.add(button, BorderLayout.EAST);
		panel.add(south, BorderLayout.SOUTH);
		location = new JLabel (" ");
		panel.add(location, BorderLayout.NORTH);
		search_results = new Vector <DefaultMutableTreeNode> ();
		GuiUtils.display(panel);
	}
	
	protected void setLocationLabel (Object [] path) {
		String genomes = null;
		String section = null;
		int i = path.length - 1;
		while (genomes == null) {
			Object obj = ((DefaultMutableTreeNode) path [i]).getUserObject();
			if (obj instanceof String)
				section = (String) obj;
			else if (obj instanceof AncestralState || obj instanceof LeafSeqOps)
				genomes = obj.toString();
			i--;
		}
		if (section != null)
			genomes = genomes + ": " + section;
		location.setText(genomes);
	}
	
	public void setSelectionPath (TreePath path) {
		TreePath old = getLeadSelectionPath ();
		if (old != null) {
			while (old.getPathCount() > 1) {
				if (path.isDescendant (old))
					break;
				collapsePath (old);
				old = old.getParentPath();
			}
		}
		super.setSelectionPath(path);
	}
	
	/**
	 * When the selection in the tree changes, recenters mauve frame gui on
	 * selected feature.
	 */
	public void valueChanged (TreeSelectionEvent event) {
		TreePath new_path = event.getNewLeadSelectionPath ();
		if (new_path != null) {
			Object [] path = new_path.getPath ();
			setLocationLabel (path);
			Object obj = ((DefaultMutableTreeNode) 
					path [path.length - 1]).getUserObject();
			Operon op = null;
			if (obj instanceof Operon)
				op = (Operon) obj;
			else if (obj instanceof LeadOperon)
				op = ((LeadOperon) obj).op;
			else if (obj instanceof UnclearOperon)
				op = ((UnclearOperon) obj).op;
			else if (obj instanceof LeafOperon)
				op = ((LeafOperon) obj).op;
			if (op != null) {
				if (phylo.handler.model.getGenomeBySourceIndex(op.seq) == null)
					System.out.println ("bad seq num: " + op.seq + " " + op.toString());
				navigator.displayFeature (op.getStart(), op.getEnd (),
						phylo.handler.model.getGenomeBySourceIndex(op.seq));
				String new_text = obj.toString ();
				if (!new_text.contains (last_search))
					search.setText(new_text);
			}
		}
	}


	public void actionPerformed(ActionEvent e) {
		String text = search.getText();
		text = text.trim ();
		if (text.length() != 0) {
			if (!text.equals (last_search)) {
				last_search = text;
				search_results.clear();
				Enumeration <DefaultMutableTreeNode> items = root.breadthFirstEnumeration();
				while (items.hasMoreElements()) {
					DefaultMutableTreeNode node = items.nextElement();
					Object obj = node.getUserObject();
					if (!(obj instanceof String) && obj.toString().contains(text))
						search_results.add(node);
				}
				search_index = 0;
			}
			else
				search_index = search_index == search_results.size () - 1 ?
						0 : search_index + 1;
			if (search_results.size () > 0) {
				final TreePath path = new TreePath (search_results.get(
						search_index).getPath());
				setSelectionPath (path);
				SwingUtilities.invokeLater (new Runnable () {
					public void run () {
						scrollPathToVisible (path);
					}
				});
			}
		}
	}
	
	
	public static class LeadOperon {
		public Operon op;
		
		public LeadOperon (Operon ope) {
			op = ope;
		}
		
		public String toString () {
			return op.getName ();
		}
	}
	

	public static class UnclearOperon {
		public DefaultMutableTreeNode same;
		public Operon op;
		
		public UnclearOperon (Operon ope, DefaultMutableTreeNode node) {
			op = ope;
			same = node;
		}
		
		public String toString () {
			return op.getName ();
		}
	}
	

	public static class LeafOperon {
		public Operon op;
		
		public LeafOperon (Operon ope) {
			op = ope;
		}
		
		public String toString () {
			return op.seq + "";
		}
	}

	public class LeafSeqOps {
		public Operon op;
		
		public LeafSeqOps (Operon ope) {
			op = ope;
		}
		
		public String toString () {
			return op.seq + ": " + phylo.handler.model.getGenomeBySourceIndex(
					op.seq).getDisplayName();
		}
	}

}
