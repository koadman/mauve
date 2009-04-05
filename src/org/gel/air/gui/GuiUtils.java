package org.gel.air.gui;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class GuiUtils {
	
	public static void display (JComponent comp) {
		JFrame frame = new JFrame ();
		frame.getContentPane().add(comp);
		frame.pack();
		frame.setVisible(true);
	}

}
