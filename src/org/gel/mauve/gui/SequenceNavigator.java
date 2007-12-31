package org.gel.mauve.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import org.gel.mauve.Genome;
import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.SeqFeatureData;
import org.gel.mauve.gui.navigation.NavigationPanel;
import org.gel.mauve.gui.navigation.SearchResultPanel;
import org.gel.mauve.gui.sequence.RRSequencePanel;
import org.gel.mauve.gui.sequence.SeqPanel;

import org.biojava.bio.seq.Feature;


/**
 * A gui that allows a user to search genome features by annotation.  Allows multiple
 * constraints.  Results are shown in a tree, and selecting the feature in the tree
 * scrolls to that position in the genome.  Results can be added to previous search
 * results, or previous results can be cleared.
 * 
 * @author rissman
 *
 */
public class SequenceNavigator extends JSplitPane implements ActionListener, 
		KeyListener, MauveConstants {

	/**
	 * gui components needed throughout the class
	 */
	protected Component parent_component;	/**< The parent component of the panel, if it disappears, so will the panel */
	protected RearrangementPanel rrpanel;	/**< The panel displaying data under navigation */
	protected BaseViewerModel data_model;	/**< The data model being displayed and navigated */
	/**
	 * parent frame containing this panel
	 */
	protected JFrame frame;
	
	/**
	 * list of genomes with searchable features
	 */
	protected JComboBox genomes;
	
	/**
	 * each NavPanel in the list represents a constraint on the current search
	 */
	protected LinkedList nav_panels;
	
	/**
	 * when pressed, a search is performed
	 */
	protected JButton search;
	protected JButton cancel;
	protected JButton add;
	
	/**
	 * when pressed, resets search to one constraint with no values entered
	 */
	protected JButton reset;
	
	/**
	 * panel that contains all NavPanels
	 */
	protected JPanel nav_panel_holder;
	
	/**
	 * panel that shows results
	 */
	protected SearchResultPanel result_pane;
	
	/**
	 * if checked, previous search results are cleared when a new search is performed
	 */
	protected JCheckBox clear;
	
	/**
	 * allows results to be scrolled through
	 */
	protected JScrollPane result_scroller;
	
	/**
	 * allows constraints to be scrolled through
	 */
	protected JScrollPane nav_scroll;
	protected LinkedList window_listeners;
	protected WindowAdapter adapt;
	
	/**
	 * if there is no frame containing the alignment, so when it is removed,
	 * this SequenceNavigator can be closed
	 */
	protected AncestorListener ancestListener;
	
	/**
	 * availabe genome sequences to search
	 */
	protected Vector genome_choices;
	
	/**
	 * sets maximum height programmatically given to the frame
	 */
	public static int MAX_HEIGHT = 400;
	
	/**
	 *sets maximum height and width programmatically given to the frame.
	 **/
	 public static int MAX_WIDTH = 850;

	
	/**
	 * initializes hashtable and hashset inherited from NavigationConstants
	 */
	static {
		READ_TO_ACTUAL.put(NAME_GROUP, LOC_NAME);
		READ_TO_ACTUAL.put(PRODUCT_GROUP, PRODUCT_NAME);
		READ_TO_ACTUAL.put(ID_GROUP, ID_NUMBER);
		READ_TO_ACTUAL.put(GO_GROUP, GO_FEATS);
		ANNOTATION_KEYS.add ("biovar");
		ANNOTATION_KEYS.add ("codon_start");
		ANNOTATION_KEYS.add ("db_xref");
		ANNOTATION_KEYS.add ("function");
		ANNOTATION_KEYS.add ("gene");
		ANNOTATION_KEYS.add ("insertion_seq");
		ANNOTATION_KEYS.add ("internal_data");
		ANNOTATION_KEYS.add ("locus_tag");
		ANNOTATION_KEYS.add ("mol_type");
		ANNOTATION_KEYS.add ("note");
		ANNOTATION_KEYS.add ("organism");
		ANNOTATION_KEYS.add ("product");
		ANNOTATION_KEYS.add ("protein_id");
		ANNOTATION_KEYS.add ("pseudo");
		ANNOTATION_KEYS.add ("strain");
		ANNOTATION_KEYS.add ("transl_except");
		ANNOTATION_KEYS.add ("transl_table");
		ANNOTATION_KEYS.add ("translation");
	}
	
	/**
	 * mechanism for only doing one gui operation at a time/locking
	 */
	private Boolean current_search = Boolean.FALSE;
	
	
	/**
	 * Creates new GenomeNavigator
	 * 
	 * @param frame the MauveFrame this navigator should be associated with
	 */
	public SequenceNavigator (MauveFrame frame) {
		super ();
		parent_component = frame.getRootPane();
		data_model = frame.getModel();
		nav_panels = new LinkedList ();
		rrpanel = frame.getRearrangementPanel ();
		initGUI ();
	}
	public SequenceNavigator (Component parent, RearrangementPanel rrpanel, BaseViewerModel dataModel) 
	{
		super ();
		parent_component = parent;
		data_model = dataModel;
		this.rrpanel = rrpanel;
		nav_panels = new LinkedList ();
		initGUI ();
	}
	
	/**
	 *Does initial gui initialization
	 *
	 */
	private void initGUI () {
		makeFrame ();
		//setOneTouchExpandable (true);
		setResizeWeight (0);
		JPanel left = new JPanel (new BorderLayout (BORDER, BORDER));
		//makes panel for choosing genome
		JPanel top1 = new JPanel ();
		top1.setLayout(new BoxLayout (top1, BoxLayout.X_AXIS));
		top1.add (new JLabel ("Choose Genome:"));
		genomes = new JComboBox ();
		loadGenomeList ();
		top1.add (genomes);
		left.add (top1, BorderLayout.NORTH);	
		left.setBorder (BorderFactory.createEmptyBorder(3, 3, 3, 3));
		JPanel middle = new JPanel (new BorderLayout ());
		nav_panel_holder = new JPanel ();
		middle.setBorder (BorderFactory.createTitledBorder(
				BorderFactory.createCompoundBorder (BorderFactory.createLineBorder (Color.black),
						BorderFactory.createEmptyBorder(BORDER, BORDER, 0, BORDER)), 
						"Find features with the following qualifying information:"));
		nav_panel_holder.setLayout (new BoxLayout (nav_panel_holder, BoxLayout.Y_AXIS));
		result_scroller = result_pane.getScrollPane ();
		nav_scroll = new JScrollPane (nav_panel_holder);
		new NavigationPanel (this);
		middle.add (nav_scroll, BorderLayout.CENTER);
		left.add (middle, BorderLayout.CENTER);
		makeBottomPanel (left);
		setLeftComponent (left);
		setRightComponent (result_scroller);
		frame.getContentPane ().add (this);
		Dimension preferred = middle.getPreferredSize ();
		preferred.width += 6;
		preferred.height += 6;
		left.setMinimumSize (new Dimension (preferred.width,
				left.getPreferredSize ().height));
		left.setMaximumSize(new Dimension (preferred.width, -1));
		frame.pack ();
		reloadGUI ();
		moveFromBehind ();
	}
	
	/**
	 * sets up the frame that contains this SequenceNavigation panel 
	 * and its subcomponents
	 *
	 */
	private void makeFrame () {
		window_listeners = new LinkedList ();
		frame = new JFrame ("Sequence Navigator");
		frame.setIconImage(MauveFrame.mauve_icon.getImage());
		((JPanel) frame.getContentPane ()).setBorder (
				BorderFactory.createEmptyBorder(10, 10, 10, 10));
		adapt = new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
				frame.setVisible (false);
			}
		};
		ancestListener = new AncestorListener(){
			public void ancestorMoved(AncestorEvent ae){}
			public void ancestorAdded(AncestorEvent ae){}
			public void ancestorRemoved(AncestorEvent ae){
				frame.setVisible(false);
			}
		};
		if(parent_component instanceof Frame)
			((Frame)parent_component).addWindowListener(adapt);
		else if(parent_component instanceof JComponent)
			((JComponent)parent_component).addAncestorListener(ancestListener);
		/*mauve_frame.addWindowListener(new WindowAdapter () {
			public void windowClosing (WindowEvent e) {
				frame.setVisible (false);
			}
		});*/
	}
	
	/**
	 * initializes bottom part of left side of gui
	 * 
	 * @param holder		The panel in which to put the bottom component
	 */
	private void makeBottomPanel (JPanel holder) {
		search = new JButton ("Search");
		cancel = new JButton ("Close");
		add = new JButton ("Add Constraint");
		reset = new JButton ("Reset Constraints");
		JPanel all = new JPanel (new BorderLayout ());
		clear = new JCheckBox ("Clear previous results when adding new");
		clear.setSelected(true);
		JPanel bottom = new JPanel ();
		bottom.add(add);
		bottom.add(reset);
		bottom.add (search);
		bottom.add (cancel);
		search.addActionListener (this);
		search.addKeyListener (this);
		cancel.addActionListener (this);
		add.addActionListener (this);
		reset.addActionListener (this);
		JPanel temp = new JPanel ();
		temp.add (clear);
		all.add(temp, BorderLayout.NORTH);
		all.add(bottom, BorderLayout.SOUTH);
		//setBorder (BorderFactory.createEmptyBorder (10, 10, 10, 10));
		holder.add (all, BorderLayout.SOUTH);
	}
	
	/**
	 * finds the best place on screen to place the frame-
	 * takes into account size of mauve frame
	 *
	 */
	//not sure how well this works in all cases, has done what I need so far
	protected void moveFromBehind () {
		Dimension needed = frame.getSize();
		int area = 0;
		Dimension total = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension taken = parent_component.getSize ();
		int extra = total.width - taken.width;
		int x = 0;
		int y = 0;
		if (extra >= needed.width)
			x = taken.width;
		else {
			area = extra * needed.height;
			extra = total.height - taken.height;
			if (extra >= needed.height)
				y = taken.height;
			else if (area > extra * needed.width)
				x = total.width - needed.width;
			else
				y = total.height - needed.height;
		}
		frame.setLocation(x, y);
	}
	
	/**
	 * loads list of genomes user is viewing and should be able to search
	 *
	 */
	public void loadGenomeList () {
		genome_choices = SeqFeatureData.userSelectableGenomes (data_model, true, true);
		genomes.setRenderer (GenomeCellRenderer.getListCellRenderer ());
		genomes.setModel (new DefaultComboBoxModel (genome_choices));
		result_pane = new SearchResultPanel (SeqFeatureData.userSelectableGenomes (
				data_model, false, false), this);
	}
	
	/**
	 * determines whether current results should be cleared
	 * 
	 * @return		True if they should, false otherwise
	 */
	public boolean shouldClear () {
		return clear.isSelected ();
	}
	
	/**
	 * adds a newly constructed navigation panel to the gui
	 */
	public void addNavigationPanel (NavigationPanel pane) {
		nav_panels.addFirst (pane);
		nav_panel_holder.add (pane);
		pane.setMaximumSize (pane.getPreferredSize());
		reloadGUI ();
			
	}
	
	/**
	 * removes unwanted navigation panel from gui
	 */
	public void removeNavigationPanel (NavigationPanel pane) {
		if (nav_panels.size() != 1) {
			nav_panels.remove(pane);
			nav_panel_holder.remove(pane);
			reloadGUI ();
		}
	}
	
	/**
	 * resets search so only one navigation panel is present
	 */
	public void reset () {
		Object [] panels = nav_panels.toArray();
		for (int i = 0; i < panels.length - 1; i++)
			removeNavigationPanel ((NavigationPanel) panels [i]);
	}
	
	/**
	 * Shows the navigator so a user may select a genome
	 * and a position to go to within the genome.
	 *
	 */
	public void showNavigator () {
		frame.setVisible (true);
	}
	
	
	/**
	 * action listener for buttons on this panel
	 * 
	 * @param e If the source of the action is the search button,
	 * 			checks for valid input and performs search
	 * 			If the source is the cancel button, hides the Navigator
	 * 			If the source is the add button, adds a panel for the use to
	 * 			enter a new constraint in
	 * 			If the source is reset, removes all the input panel except the first
	 */
	public void actionPerformed (final ActionEvent e) {
		if (e.getSource () == search) {
			Thread t = new Thread () {
				public void run () {
					synchronized (current_search) {
						if (current_search == Boolean.FALSE) {
							current_search = Boolean.TRUE;
						}
						else
							return;
					}
					try {
						doNavigation ();
					} catch (Exception e) {
						e.printStackTrace ();
					}
					current_search = Boolean.FALSE;
				}
			};
			//threads.add(t);
			t.start ();
		}
		if (current_search == Boolean.FALSE) {
			if (e.getSource () == add)
				new NavigationPanel (SequenceNavigator.this);
			else if (e.getSource() == cancel)
				frame.setVisible (false);
			else if (e.getSource () == reset)
				reset ();
		}
	}
	
	/**
	 * necessary to call for some reason whenever adding or removing
	 * NavigationPanels-- automatically called from addNavigationPanel and
	 * removeNavigationPanel
	 *
	 */
	public void reloadGUI () {
		Dimension size = frame.getSize ();
		expandIfNecessary (nav_scroll, size);
		expandIfNecessary (result_scroller, size);
		size.width = (size.width > MAX_WIDTH) ? MAX_WIDTH : size.width;
		size.height = (size.height > MAX_HEIGHT) ? MAX_HEIGHT : size.height;
		frame.setSize(size);
		revalidate ();
		repaint ();
	}
	
	
	/**
	 * Expands the frame holding the gui as much as necessary without
	 * allowing it to increase over the maximum programmatically 
	 * allowable size
	 * 
	 * @param pane			Holds the component that might need more size
	 * @param size			The size of the frame before the resize
	 */
	public static void expandIfNecessary (JScrollPane pane, Dimension size) {
		JViewport port = pane.getViewport ();
		Dimension preferred = port.getView ().getPreferredSize ();
		Dimension actual = port.getSize ();
		if (preferred.width > actual.width)
			size.width += preferred.width - actual.width;
		if (preferred.height > actual.height)
			size.height += preferred.height - actual.height;
	}

	/**
	 * dummy method - implemented as part of key listener interface
	 */
	public void keyPressed (KeyEvent e) {
	}
	
	/**
	 * dummy method - implemented as part of key listener interface
	 */
	public void keyReleased (KeyEvent e) {
	}
	
	/**
	 * converts enters typed into the input panels to 
	 * action events representing a user desire to perform search
	 */
	public void keyTyped (KeyEvent e) {
		if (e.getKeyChar () == '\n') {
			ActionEvent ae = new ActionEvent (search, ActionEvent.ACTION_PERFORMED, null);
			actionPerformed (ae);
		}
	}
	
	/**
	 * scrolls the gui to a specific part of the list of constraints
	 * 
	 * @param panel			The NavigationPanel that should be visible on screen
	 */
	public void makeConstraintVisible (NavigationPanel panel) {
		nav_scroll.getViewport ().scrollRectToVisible (panel.getBounds ());
	}

	/**
	 * Finds out if the data entered into the input panels is valid,
	 * and how many valid constraints are possible.
	 * Input is considered valid if there are no breaks in input;
	 * if all fields are filled in.  Will allow empty constraints at the bottom
	 * 
	 * @return			The number of valid constraints
	 */
	public int getValidCount () {
		int count = 0;
		boolean hole = false;
		Object [] items = nav_panels.toArray ();
		for (int i = items.length - 1; i >= 0; i--) {
			NavigationPanel pan = (NavigationPanel) items [i];
			if (pan.dataValid ()) {
				if (hole) {
					JOptionPane.showMessageDialog(frame, "Missing or invalid data.\n" +
							"Can't perform search.", "Navigation Error", JOptionPane.ERROR_MESSAGE);
					return 0;
				}
				else
					count++;
			}
			else
				hole = true;
		}
		return count;
	}
	
	
	/**
	 * A simple wizard style routine that allows a user to choose a numeric
	 * sequence position from a specific sequence to navigate to, and performs
	 * navigation
	 *
	 * @param parentComponent	The parent in the GUI heirarchy
	 * @param dataModel		A viewer data model containing sequence data of interest
	 * @param rrpanel		The rearrangement panel to adjust when the user selects a seq coordinate
	 */
	public static void goToSeqPos (Component parentComponent, BaseViewerModel dataModel, RearrangementPanel rrpanel) {
		Genome [] chosen = SeqFeatureData.userSelectedGenomes (parentComponent, 
				dataModel, false, false);
		if (chosen != null) {
			long pos = -1;
			do {
				try {
					String input = JOptionPane.showInputDialog (parentComponent, 
					"Enter sequence coordinate to jump to...");
					if (input != null)
						pos = Long.parseLong (input);
					else
						break;
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(parentComponent, "Invalid position entered",
							"Invalid Data", JOptionPane.ERROR_MESSAGE);
				}
			} while (pos == -1);
			if (pos != -1)
				goToPosition (pos, chosen [0], rrpanel);
		}
	}
	
	
	/**
	 * A wizard style simplified feature searcher that allows the user
	 * to find a feature by name and highlights the first feature
	 * in the mauve gui that matches the desired name
	 *
	 */
	public void goToFeatureByName () {
		Genome [] chosen = SeqFeatureData.userSelectedGenomes (
				parent_component, data_model, true, true);
		if (chosen != null) {
			String input = JOptionPane.showInputDialog (parent_component, 
			"Enter name of desired feature. . .");
			if (input != null) {
				String [][] data = new String [1][3];
				data [0][FIELD] = LOC_NAME;
				data [0][VALUE] = input;
				data [0][EXACT] = Boolean.toString(false);
				int count = 0;
				LinkedList first = null;
				LinkedList [] tree_data = SeqFeatureData.findFeatures (chosen, data);
				for (int i = 0; i < chosen.length; i++) {
					Object genome = tree_data [i].removeFirst ();
					SeqFeatureData.removeLocationDuplicates (tree_data [i]);
					count += tree_data [i].size();
					tree_data [i].addFirst (genome);
					if (count == 1) {
						first = tree_data [i];
					}
				}
				if (count == 1)
					displayFeature ((Feature) first.get(1),	(Genome) first.getFirst());
				else if (count == 0) {
					JOptionPane.showMessageDialog(parent_component, 
							"No Features were found with specified name.");
				}
				else {
					frame.setVisible(true);
					result_pane.displayFeatures(tree_data);
				}
			}
		}
		
	}
	
	/**
	 * converts user input to features to display
	 * and centers view on first matching feature
	 *
	 */
	protected void doNavigation () {
		int index = genomes.getSelectedIndex();
		int valid = getValidCount ();
		if (valid > 0) {
			result_pane.waitForResults ();
			String [][] criteria = new String [valid][3];
			for (int i = 0; i < criteria.length; i++)
				criteria [i] = ((NavigationPanel) nav_panels.get(
						nav_panels.size() - 1 - i)).getSearchCriteria ();
			showResultTree (SeqFeatureData.convertIndexToSequence (
					genome_choices, index), criteria);
		}
	}
	
	
	/**
	 * Performs the final narrowing down of features to those that match the
	 * given constraints, and displays a tree of matching features
	 * 
	 * @param nomes			The genomes the data is from
	 * @param data			A two dimensional array; contains the constraints to
	 * 						search by
	 */
	public void showResultTree (final Genome [] nomes, final String [][] data) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                result_pane.displayFeatures (SeqFeatureData.findFeatures (nomes, data));
            }
        });
	}

	/**
	 * Centers the view on a specific feature of a genome
	 * 
	 * @param feat			The feature to center on
	 * @param genome		The genome containing the feature
	 */	
	public void displayFeature (Feature feat, Genome genome) {
		try {
			adjustZoom(feat);
			goToPosition(SeqFeatureData.centerOfFeature(feat), genome, rrpanel);
			data_model.highlightRange(genome,
					feat.getLocation().getMin(), feat.getLocation().getMax());
		} catch (Exception e) {
			e.printStackTrace ();
		}		
	}

	/**
	 * Adjust zoom so desired feature is a decent size and is all viewable
	 * @param feat
	 */
	public void adjustZoom (Feature feat) {
		int length = feat.getLocation().getMax () - feat.getLocation ().getMin();
		long vis_length = ((Genome) genomes.getItemAt(
				genomes.getItemCount() == 1 ? 0 : 1)).getViewLength ();
		double percent = 0;
		double new_vis = 0;
		int count = 0;
		if (length < vis_length) {
			new_vis = length * 10;
			if (new_vis < vis_length) {
				percent = ((double) vis_length) / new_vis;
				percent *= 100;
			}
		}
		else {
			new_vis = length + 1/3*((double) length);
			percent = vis_length/new_vis * 100;
			while (percent < 1) {
				percent *= 2;
				count++;
			}
		}
		if (percent != 0) {
			data_model.zoomAndMove ((int) percent, 0);
			while (count > 0) {
				data_model.zoomAndMove (50, 0);
				count--;
			}
		}
	}
	
	/**
	 * Centers view on a specific sequence position
	 * 
	 * @param position 		the position to center on
	 * @param chosen   		the genome to find the position in
	 * @param mf			the mauve frame containing the p
	 */
	public static void goToPosition (long position, Genome chosen, RearrangementPanel rrpanel) {
		Object [] panels = rrpanel.newPanels.toArray ();
		for (int i = 0; i < panels.length; i++) {
			RRSequencePanel panel = (RRSequencePanel) ((SeqPanel) 
					panels [i]).getSequencePanel ();
			if (panel.isForGenome (chosen)) {
				panel.goTo (position);
				break;
			}
		}
	}
	
	/**
	 * Centers view on a specific sequence position
	 * 
	 * @param position 		the position to center on
	 * @param chosen   		the genome to find the position in
	 */
	public void goToPosition (long position, Genome chosen) {
		goToPosition (position, chosen, rrpanel);
	}
	
	/**
	 * Gets all the feature keys present in any of the sequences
	 * 
	 * @return		A vector of strings representing all the fields in
	 * 				all the currently viewed sequences
	 */
	public Vector getGenomeKeys () {
		Vector readable = new Vector ();
		Iterator itty = ANNOTATION_KEYS.iterator();
		while (itty.hasNext())
			readable.add(((String) itty.next ()).replace ('_', ' '));
		Collections.sort (readable);
		return readable;
	}
	
	public void dispose () {
		if(parent_component instanceof Frame)
			((Frame)parent_component).removeWindowListener (adapt);
		else if(parent_component instanceof JComponent)
			((JComponent)parent_component).removeAncestorListener(ancestListener);
		frame.dispose ();
	}
	
}
