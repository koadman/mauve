package org.gel.mauve.gui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.gel.mauve.Genome;

public class GenomeCellRenderer {

	public static ListCellRenderer getListCellRenderer () {
		return new DefaultListCellRenderer () {

			public Component getListCellRendererComponent (JList list,
					Object val, int index, boolean sel, boolean focus) {
				if (val instanceof Genome)
					val = ((Genome) val).getDisplayName ();
				return super.getListCellRendererComponent (list, val, index,
						sel, focus);
			}

		};
	}
}
