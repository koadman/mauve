package org.gel.air.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class StatusBox extends JWindow {

	private final static int BORDER_SIZE = 23;

	private JLabel label;

	public StatusBox (String msg, Icon icon) {
		label = new JLabel (msg, icon, SwingConstants.CENTER);
		label.setIconTextGap (BORDER_SIZE);
		JComponent blah = (JComponent) getContentPane ();
		blah.setLayout (new BorderLayout (23, 23));
		blah.add (label);
		blah.setBorder (new CompoundBorder (
				new BevelBorder (BevelBorder.RAISED), new EmptyBorder (
						BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE)));
		pack ();
		Dimension scr = Toolkit.getDefaultToolkit ().getScreenSize ();
		Dimension s = getSize ();
		setLocation ((scr.width - s.width) >> 1, (scr.height - s.height) >> 1);
	}

	public void setVisible (boolean visible) {
		super.setVisible (visible);
		if (visible) {
			toFront ();
			paintAll (getGraphics ());
		}
	}

	public void setText (String msg) {
		label.setText (msg);
	}

}