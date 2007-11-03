package org.gel.mauve.gui.navigation;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.symbol.Location;
import org.gel.mauve.Genome;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.SeqFeatureData;
import org.gel.mauve.gui.SequenceNavigator;

/**
 * A Gui component that shows the results of a given query separated by genome.
 * Each result represents a feature that matched the search constraints.  If a result
 * is selected, the Genome sequence is scrolled to that feature
 * 
 * @author rissman
 * 
 */
public class SearchResultPanel extends JPanel implements TreeModel,
		TreeCellRenderer, TreeSelectionListener, MauveConstants {

	/**
	 * Tree that contains search results (features)
	 */
	protected JTree tree;

	/**
	 * model that represents the data to be displayed
	 */
	protected DefaultTreeModel model;

	/**
	 * renders each value in the tree
	 */
	protected DefaultTreeCellRenderer renderer;

	/**
	 * reference to SequenceNavigator this panel is associated with
	 */
	protected SequenceNavigator navigator;

	/**
	 * reference to root node of tree
	 */
	protected DefaultMutableTreeNode root;

	/**
	 * allows results to be scrolled through
	 */
	protected JScrollPane scroller;

	/**
	 * represents display state - if there are no results, value is 0, 1 if
	 * there is 1 result, any number higher than 1 means multiple results
	 */
	protected int result_state;

	/**
	 * true when a search is in progress, false otherwise
	 */
	protected boolean searching;

	/**
	 * String representing no results
	 */
	public static final String NO_RESULTS = "No results to display. . .";

	/**
	 * represents genome with no found features
	 */
	public static final String MATCHLESS = "No features found. . .";
	
	/**
	 * represents tree while searching
	 */
	protected static final String SEARCHING = "Searching. . .";

	/**
	 * represents "dummy" root node of tree
	 */
	protected static final String ROOT = "Root";

	/**
	 * icon shown if a genome contained results
	 */
	public static final Icon PLUS_ICON = SignedIcon.getSignedIcon (true);

	/**
	 * icon shown if a genome did not contain results
	 */
	public static final Icon MINUS_ICON = SignedIcon.getSignedIcon (false);

	/**
	 * contains all the data the tree should display
	 */
	protected Hashtable genome_data;

	/**
	 * contains genomes mapped to their index
	 */
	protected Object [] genome_indexes;

	/**
	 * Constructs new SearchResultPanel
	 * 
	 * @param genomes
	 *            The genomes that the mauve frame displays
	 * @param nav
	 *            The SequenceNavigator it belongs to
	 */
	public SearchResultPanel (Vector genomes, SequenceNavigator nav) {
		super (new FlowLayout (FlowLayout.LEFT));
		System.out.println (getLayout ());
		navigator = nav;
		genome_data = new Hashtable ();
		genome_indexes = new Object [genomes.size ()];
		for (int i = 0; i < genomes.size (); i++) {
			genome_data.put (genomes.get (i), new LinkedList ());
			genome_indexes[i] = genomes.get (i);
		}
		initGUI ();
	}

	/**
	 * initializes gui components
	 * 
	 */
	protected void initGUI () {
		UIManager.put ("Tree.hash", new ColorUIResource (Color.BLACK));
		tree = new JTree ();
		tree.setRootVisible (false);
		tree.setBackground (navigator.getBackground ());
		tree.getSelectionModel ().setSelectionMode (
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		model = (DefaultTreeModel) tree.getModel ();
		renderer = (DefaultTreeCellRenderer) tree.getCellRenderer ();
		renderer.setBackgroundNonSelectionColor (navigator.getBackground ());
		renderer.setClosedIcon (PLUS_ICON);
		renderer.setOpenIcon (MINUS_ICON);
		renderer.setLeafIcon (null);
		tree.setCellRenderer (this);
		tree.setModel (this);
		tree.setEditable (false);
		tree.addTreeSelectionListener (this);
		tree.setToggleClickCount (1);
		root = new DefaultMutableTreeNode (ROOT);
		add (tree);
		scroller = new JScrollPane (this);
		scroller.getVerticalScrollBar ().setUnitIncrement (
				renderer.getPreferredSize ().height);
	}

	/**
	 * returns a reference to the ScrollPane that contains this result_panel
	 * 
	 * @return
	 */
	public JScrollPane getScrollPane () {
		return scroller;
	}

	/**
	 * displays the data passed into the method
	 * 
	 * @param data
	 *            An array of linked lists. Each list should start with a genome
	 *            object, followed by all the Features to display in that genome
	 */
	public void displayFeatures (Object [] data) {
		resetData (data);
		int prev_result = result_state;
		Object [] first = new Object [3];
		for (int i = 0; i < genome_indexes.length; i++) {
			LinkedList list = (LinkedList) genome_data.get (genome_indexes[i]);
			if (result_state == 0 && list.size () > 0) {
				first[2] = (Feature) list.get (0);
				first[1] = genome_indexes[i];
				first[0] = ROOT;
			}
			result_state += list.size ();
			if (result_state > 1)
				break;
		}
		searching = false;
		model.nodeStructureChanged (root);
		Object [] path = new Object [(result_state == 0) ? 1 : 2];
		path[0] = ROOT;
		if (result_state > 0) {
			for (int i = 0; i < getChildCount (ROOT); i++) {
				Object kid = getChild (ROOT, i);
				path[1] = kid;
				tree.expandPath (new TreePath (path));
			}
			if (prev_result == 0)
				tree.setSelectionPath (new TreePath (first));
		} else {
			tree.expandPath (new TreePath (path));
			try {
				Thread.currentThread ().wait (100);
			} catch (Exception e) {
				// ok probably. . .
			}
		}
		navigator.reloadGUI ();
	}

	/**
	 * sets display to reflect a search is being performed
	 *
	 */
	public void waitForResults () {
		searching = true;
				SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				model.nodeStructureChanged (root);
			}
		});
	}

	/**
	 * Resets the display to include the new data
	 * 
	 * @param data
	 *            The data that should be added. Each array index is a linked
	 *            list containing first the genome it is for, then all the
	 *            features to display for that sequence
	 */
	protected void resetData (Object [] data) {
		Enumeration keys = null;
		if (result_state != 0 && navigator.shouldClear ()) {
			keys = genome_data.keys ();
			while (keys.hasMoreElements ()) {
				LinkedList remove = (LinkedList) genome_data.get (keys
						.nextElement ());
				remove.clear ();
			}
			result_state = 0;
		}
		for (int i = 0; i < data.length; i++) {
			LinkedList new_data = (LinkedList) data[i];
			Object key = new_data.remove (0);
			LinkedList old = (LinkedList) genome_data.get (key);
			old.addAll (new_data);
			SeqFeatureData.removeLocationDuplicates (old);
		}
	}

	/**
	 * returns a string including the feature's name and location
	 * 
	 * @param feat
	 *            The feature whose information should be displayed
	 * @return The display string for this feature
	 */
	public static String getDisplayText (Feature feat) {
		String data = "";
		Annotation note = feat.getAnnotation ();
		StringTokenizer fields = SeqFeatureData.separateFields (LOC_NAME);
		while (fields.hasMoreTokens () && data.length () == 0) {
			String field = fields.nextToken ();
			if (note.containsProperty (field))
				data += (String) note.getProperty (field);
		}
		Location loci = feat.getLocation ();
		data += " (" + loci.getMin () + "-" + loci.getMax () + ")";
		data.trim ();
		return data;
	}

	/**
	 * When the selection in the tree changes, recenters mauve frame gui on
	 * selected feature.
	 */
	public void valueChanged (TreeSelectionEvent event) {
		if (event.getNewLeadSelectionPath () != null) {
			Object [] path = event.getNewLeadSelectionPath ().getPath ();
			if (path != null && path.length == 3 && path[2] != MATCHLESS)
				navigator.displayFeature ((Feature) path[2], (Genome) path[1]);
		}
	}

	/**
	 * facilitates painting of tree- converts feature to its name
	 */
	public Component getTreeCellRendererComponent (JTree tree, Object val,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean focus) {
		JLabel label = (JLabel) renderer.getTreeCellRendererComponent (tree,
				val, selected, expanded, leaf, row, focus);
		if (val instanceof Feature)
			label.setText (getDisplayText ((Feature) val));
		return label;
	}

	/**
	 * passes on listeners to real tree model
	 */
	public void addTreeModelListener (TreeModelListener listen) {
		model.addTreeModelListener (listen);
	}

	/**
	 * Finds the child of the given object
	 * 
	 * @param parent
	 *            The object whose child should be found
	 * @param index
	 *            The index of the desired child
	 * @return An object representing the child node
	 */
	public Object getChild (Object parent, int index) {
		if (parent == ROOT) {
			if (searching)
				return SEARCHING;
			else if (result_state == 0)
				return NO_RESULTS;
			else
				return genome_indexes[index];
		}
		LinkedList children = (LinkedList) genome_data.get (parent);
		if (children != null) {
			try {
				return children.get (index);
			} catch (IndexOutOfBoundsException e) {
				return MATCHLESS;
			}
		} else
			return null;

	}

	/**
	 * Returns the number of children the object has
	 * 
	 * @param parent
	 *            The object representing the parent
	 * @return The number of children associated with the parent
	 */
	public int getChildCount (Object parent) {
		if (parent == ROOT) {
			if (searching || result_state == 0)
				return 1;
			else
				return genome_data.size ();
		}
		LinkedList children = (LinkedList) genome_data.get (parent);
		if (children != null) {
			int size = ((LinkedList) genome_data.get (parent)).size ();
			if (size == 0)
				return 1;
			else
				return size;
		} else
			return 0;
	}

	/**
	 * Returns the index of the child
	 * 
	 * @param parent
	 *            The parent of the child in question
	 * @param child
	 *            The child whose index is desired
	 * @return The index of the child
	 */
	public int getIndexOfChild (Object parent, Object child) {
		if (parent == ROOT) {
			if (child == SEARCHING) {
				if (searching)
					return 0;
				else
					return -1;
			}
			if (child == NO_RESULTS) {
				if (result_state == 0)
					return 0;
				else
					return -1;
			} else {
				for (int i = 0; i < genome_indexes.length; i++) {
					if (genome_indexes[i].equals (child))
						return i;
				}
			}
		} else if (child == MATCHLESS)
			return 0;
		else if (genome_data.contains (parent)) {
			LinkedList kids = (LinkedList) genome_data.get (parent);
			return kids.indexOf (child);
		}
		return -1;
	}

	/**
	 * returns undisplayed root of the tree
	 * 
	 * @return An object representing the tree root
	 */
	public Object getRoot () {
		return ROOT;
	}

	/**
	 * Returns true if the object is a leaf
	 * 
	 * @param node
	 *            The object in question
	 * @return True if the object has no children, false otherwise
	 */
	public boolean isLeaf (Object node) {
		if (node != ROOT && !genome_data.containsKey (node))
			return true;
		else
			return false;
	}

	/**
	 * passes on listener removes to real tree model
	 */
	public void removeTreeModelListener (TreeModelListener list) {
		model.removeTreeModelListener (list);
	}

	/**
	 * Passes on events to real tree model
	 */
	public void valueForPathChanged (TreePath path, Object val) {
		model.valueForPathChanged (path, val);
	}

	/**
	 * represents an icon that shows whether a given node can be expanded (has
	 * children)
	 * 
	 * @author rissman
	 * 
	 */
	public static class SignedIcon implements Icon {

		/**
		 * whether this icon should show a plus or minus
		 */
		private boolean plus;

		/**
		 * class constants representing only two instances of this class
		 */
		private static SignedIcon PLUS = new SignedIcon (true);

		private static SignedIcon MINUS = new SignedIcon (false);

		/**
		 * constructs new SignedIcon
		 * 
		 * @param p
		 *            Whether it should show a plus or minus
		 */
		private SignedIcon (boolean p) {
			plus = p;
		}

		/**
		 * Entry point to get appropriate signed icon-controls how many are made
		 * 
		 * @param plus
		 *            True if the icon should show a plus sign, false if it
		 *            should show a minus sign
		 * @return
		 */
		public static SignedIcon getSignedIcon (boolean plus) {
			return plus ? PLUS : MINUS;
		}

		public int getIconHeight () {
			return 16;
		}

		public int getIconWidth () {
			return 16;
		}

		/**
		 * draws plus or minus sign
		 */
		public void paintIcon (Component comp, Graphics g, int x, int y) {
			g.drawRect (3, 3, 8, 8);
			g.drawLine (5, 7, 9, 7);
			if (plus)
				g.drawLine (7, 5, 7, 9);
		}

	}
}
