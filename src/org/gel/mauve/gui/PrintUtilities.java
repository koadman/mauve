package org.gel.mauve.gui;

import java.awt.Component;

import javax.swing.RepaintManager;

/**
 * A couple of utilities.
 * 
 * 7/99 Marty Hall, http://www.apl.jhu.edu/~hall/java/ May be freely used or
 * adapted.
 */

public class PrintUtilities {

	/**
	 * The speed and quality of printing suffers dramatically if any of the
	 * containers have double buffering turned on. So this turns if off
	 * globally.
	 * 
	 * @see enableDoubleBuffering
	 */
	public static void disableDoubleBuffering (Component c) {
		RepaintManager currentManager = RepaintManager.currentManager (c);
		currentManager.setDoubleBufferingEnabled (false);
	}

	/** Re-enables double buffering globally. */

	public static void enableDoubleBuffering (Component c) {
		RepaintManager currentManager = RepaintManager.currentManager (c);
		currentManager.setDoubleBufferingEnabled (true);
	}
}