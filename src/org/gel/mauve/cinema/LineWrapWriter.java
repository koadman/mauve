package org.gel.mauve.cinema;

import java.io.BufferedWriter;
import java.io.IOException;

public class LineWrapWriter {
	private BufferedWriter w;

	private int len;

	private int pos = -1;

	public LineWrapWriter (BufferedWriter w, int len) {
		this.w = w;
		this.len = len;
	}

	public void write (char c) throws IOException {
		pos++;
		if (pos == len) {
			pos = 0;
			w.newLine ();
		}
		w.write (c);
	}

	public void write (char [] data) throws IOException {
		// TODO: Make faster, please.
		for (int i = 0; i < data.length; i++) {
			write (data[i]);
		}
	}

}
