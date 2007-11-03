package org.gel.mauve.format;

import javax.imageio.ImageIO;

public class TestMain {
	public static void main (String [] args) {
		String [] fmts = ImageIO.getReaderFormatNames ();
		for (int i = 0; i < fmts.length; i++) {
			System.out.println (fmts[i]);
		}
	}
}
