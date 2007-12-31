package org.gel.mauve.gui.sequence;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;

import javax.swing.JCheckBoxMenuItem;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Genome;
import org.gel.mauve.Match;
import org.gel.mauve.gui.RearrangementPanel;

/**
 * A popup menu showing the sequence coordinates covered by an ungapped
 * alignment
 */

class MatchPopupMenu extends PopupMenu implements ActionListener, ItemListener {
	LinkedList match_list;

	RearrangementPanel rrpanel;

	Genome g;

	BaseViewerModel model;

	int lcb_id;

	int remove_item = Integer.MAX_VALUE;

	int keep_item = Integer.MAX_VALUE;

	static final String remove_string = "Remove this LCB";

	static final String keep_string = "Keep this LCB";

	public MatchPopupMenu (RearrangementPanel rrpanel, BaseViewerModel model,
			Genome g) {
		this.rrpanel = rrpanel;
		this.g = g;
		match_list = new LinkedList ();
		addActionListener (this);
	}

	public void addLCBOptions (int lcb_id) {
		// remove_item = getItemCount();
		// add( new MenuItem( remove_string ) );
		keep_item = getItemCount ();
		JCheckBoxMenuItem keep_checkbox = new JCheckBoxMenuItem (keep_string);
		keep_checkbox.setState (false);
		keep_checkbox.addItemListener (this);
		// add( keep_checkbox );
	}

	public void addMatch (Match m) {
		add (new MenuItem ("Align display to " + m.toString ()));
		// add( new MenuItem( new String( "Show annotation between " ) +
		// m.starts[ sequence ] + new String(" and ") + (m.starts[ sequence ] +
		// m.lengths[ sequence ] - 1) ) );
		match_list.addLast (m);
	}

	public void actionPerformed (ActionEvent e) {
		String act_command = e.getActionCommand ();
		int itemI = 0;
		for (; itemI < getItemCount (); itemI++) {
			if (getItem (itemI).getActionCommand () == act_command)
				break;
		}
		if (itemI == remove_item) {
			// rrpanel.removeLCB( lcb_id );
		} else if (itemI == keep_item) {
			// rrpanel.keepLCB( lcb_id );
		} else {
			if (itemI > keep_item) {
				itemI -= 1;
			}
			int matchI = itemI;
			Match selected_match = (Match) match_list.get (matchI);
			model.alignView (selected_match, g);
		}
	}

	/** this gets called when the "Keep LCB" item is selected */
	public void itemStateChanged (ItemEvent e) {
		// rrpanel.toggleKeepLCB( lcb_id );
	}

}
