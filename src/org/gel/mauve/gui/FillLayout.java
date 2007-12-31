package org.gel.mauve.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class FillLayout implements LayoutManager {

	public void addLayoutComponent (String name, Component comp) {
	}

	public void removeLayoutComponent (Component comp) {
	}

	public Dimension preferredLayoutSize (Container parent) {
		synchronized (parent.getTreeLock ()) {
			// Find the maximum preferred width and height.

			Insets insets = parent.getInsets ();
			int ncomponents = parent.getComponentCount ();

			int w = 0;
			int h = 0;

			for (int i = 0; i < ncomponents; i++) {
				Component comp = parent.getComponent (i);
				Dimension d = comp.getPreferredSize ();
				if (w < d.width) {
					w = d.width;
				}
				if (h < d.height) {
					h = d.height;
				}
			}
			return new Dimension (insets.left + insets.right + w, insets.top
					+ insets.bottom + h);
		}
	}

	public Dimension minimumLayoutSize (Container parent) {
		synchronized (parent.getTreeLock ()) {
			// Find the maximum preferred width and height.

			Insets insets = parent.getInsets ();
			int ncomponents = parent.getComponentCount ();

			int w = 0;
			int h = 0;

			for (int i = 0; i < ncomponents; i++) {
				Component comp = parent.getComponent (i);
				Dimension d = comp.getMinimumSize ();
				if (w > d.width) {
					w = d.width;
				}
				if (h < d.height) {
					h = d.height;
				}
			}
			return new Dimension (insets.left + insets.right + w, insets.top
					+ insets.bottom + h);
		}
	}

	public void layoutContainer (Container parent) {
		synchronized (parent.getTreeLock ()) {
			Insets insets = parent.getInsets ();
			int ncomponents = parent.getComponentCount ();

			if (ncomponents == 0) {
				return;
			}

			int w = parent.getWidth () - (insets.left + insets.right);
			int h = parent.getHeight () - (insets.top + insets.bottom);

			for (int i = 0; i < ncomponents; i++) {
				parent.getComponent (i).setBounds (insets.left, insets.top, w,
						h);
			}
		}
	}
}