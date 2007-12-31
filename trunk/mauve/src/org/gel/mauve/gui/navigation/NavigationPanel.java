package org.gel.mauve.gui.navigation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.gel.mauve.MauveConstants;
import org.gel.mauve.gui.SequenceNavigator;

/**
 * A panel that allows a user to set up a constraint for searching features 
 * 
 * @author rissman
 * 
 */
public class NavigationPanel extends JPanel implements ActionListener,
		MauveConstants {

	/**
	 * Contains choices for annotation keys to search for.  Multiple values can be entered
	 * and should be comma-separated
	 */
	protected JComboBox nav_chooser;

	/**
	 * the value to search for.
	 */
	protected JTextField input;

	/**
	 * reference to the SequenceNavigator this panel is associated with
	 */
	protected SequenceNavigator navigator;

	/**
	 * If selected, this constraint should be an exact match
	 */
	protected JRadioButton equals;

	/**
	 * If selected, returned features should have annotations with values that
	 * contain the specified input
	 */
	protected JRadioButton contains;

	/**
	 * If this button is pressed, this panel is removed from the SequenceNavigator
	 */
	protected JButton remove;

	/**
	 * Strings representing button names and actions
	 */
	protected static final String EQUALS = "equals";

	protected static final String CONTAINS = "contains";

	/**
	 * vector of fields available to navigate by
	 */
	public Vector nav_methods;

	/**
	 * constructs a new NavigationPanel and adds it to the SequenceNavigator so
	 * a user may add an additional constraint
	 * 
	 * @param nav
	 *            The SequenceNavigator that will hold this panel
	 */
	public NavigationPanel (SequenceNavigator nav) {
		super ();
		navigator = nav;
		setNavigationChoices ();
		new BoxLayout (this, BoxLayout.X_AXIS);
		initGUI ();
	}

	/**
	 * initializes gui components
	 * 
	 */
	protected void initGUI () {
		nav_chooser = new JComboBox (nav_methods);
		nav_chooser.setEditable (true);
		Font usual = nav_chooser.getFont ();
		Font large = new Font (usual.getName (), usual.getStyle (), 12);
		Font small = new Font (usual.getName (), usual.getStyle (), 10);
		nav_chooser.setFont (large);
		nav_chooser.addActionListener (this);
		input = new JTextField (10);
		input.setFont (large);
		input.addKeyListener (navigator);
		JPanel radios = new JPanel (new BorderLayout ());
		equals = new JRadioButton (EQUALS);
		equals.setFont (small);
		equals.setActionCommand (EQUALS);
		ButtonGroup exact_match = new ButtonGroup ();
		exact_match.add (equals);
		radios.add (equals, BorderLayout.NORTH);
		contains = new JRadioButton (CONTAINS);
		contains.setFont (small);
		contains.setActionCommand (CONTAINS);
		exact_match.add (contains);
		contains.setSelected (true);
		radios.add (contains, BorderLayout.SOUTH);
		remove = new JButton ("Remove");
		remove.setFont (large);
		remove.addActionListener (this);
		int height = input.getPreferredSize ().height + 2;
		nav_chooser.setPreferredSize (new Dimension (nav_chooser
				.getPreferredSize ().width, height));
		remove.setPreferredSize (new Dimension (
				remove.getPreferredSize ().width, height));
		input.setPreferredSize (new Dimension (input.getPreferredSize ().width,
				height));
		equals.setPreferredSize (new Dimension (
				equals.getPreferredSize ().width, height - 4));
		contains.setPreferredSize (new Dimension (
				contains.getPreferredSize ().width, height - 4));
		add (nav_chooser);
		add (radios);
		add (input);
		add (remove);
		navigator.addNavigationPanel (this);
	}

	/**
	 * checks if the user has entered valid information to search by
	 * 
	 * @return True if the data entered is valid
	 */
	public boolean dataValid () {
		boolean valid = true;
		String field = (String) nav_chooser.getSelectedItem ();
		if (field == null || field.length () == 0) {
			nav_chooser.grabFocus ();
			valid = false;
		} else {
			String choice = input.getText ();
			if (choice == null || choice.length () == 0) {
				input.grabFocus ();
				valid = false;
			}
		}
		if (!valid)
			navigator.makeConstraintVisible (this);
		return valid;
	}

	/**
	 * returns the selections the user made using this navigation panel
	 * 
	 * @return String array length 3. The 0 position is field name, the 1 the
	 *         value the field should have, and 2 is a String value representing
	 *         a boolean--true if the user wants an exact match, false otherwise
	 */
	public String [] getSearchCriteria () {
		String [] data = new String [3];
		data[FIELD] = (String) nav_chooser.getSelectedItem ();
		data[VALUE] = input.getText ();
		data[EXACT] = Boolean.toString (equals.isSelected ());
		return data;
	}

	/**
	 * sets choices available to search by- includes all field names in all
	 * genomes and pre-made choice groupings
	 * 
	 */
	protected void setNavigationChoices () {
		nav_methods = new Vector ();
		nav_methods.add (NAME_GROUP);
		nav_methods.add (ID_GROUP);
		nav_methods.add (PRODUCT_GROUP);
		nav_methods.add (GO_GROUP);
		nav_methods.addAll (navigator.getGenomeKeys ());
	}

	/**
	 * responds when a user elects to remove this navigation panel
	 * 
	 * @param e
	 *            The action event representing the button being clicked
	 */
	public void actionPerformed (ActionEvent e) {
		if (e.getSource () == remove)
			navigator.removeNavigationPanel (this);
		else
			input.grabFocus ();
	}

}
