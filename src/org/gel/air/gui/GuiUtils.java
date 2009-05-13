package org.gel.air.gui;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class GuiUtils {
	
	public static void display (JComponent comp) {
		display (comp, "");
	}
	
	public static void display (JComponent comp, String title) {
		JFrame frame = new JFrame (title);
		frame.getContentPane().add(comp);
		frame.pack();
		frame.setVisible(true);
	}

}
