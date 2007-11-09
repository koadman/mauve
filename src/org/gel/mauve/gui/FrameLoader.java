package org.gel.mauve.gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.MauveFormatException;
import org.gel.mauve.ModelBuilder;

public class FrameLoader implements Runnable {
	private MauveFrame frame;

	private File file;

	private URL url;

	private String sequenceID;

	private long start;

	private long end;

	private String auth_token;
	
	private String contig;

	public FrameLoader (MauveFrame frame, URL url) {
		this.frame = frame;
		this.url = url;
	}

	public FrameLoader (MauveFrame frame, File file) {
		this.frame = frame;
		this.file = file;
	}

	public FrameLoader (MauveFrame frame, URL url, String sequenceID,
			long start, long end, String auth_token, String contig) {
		this.frame = frame;
		this.url = url;
		this.sequenceID = sequenceID;
		this.start = start;
		this.end = end;
		this.auth_token = auth_token;
		this.contig = contig;
		
	}

	public void run () {
		if (url != null) {
			loadURL ();
		} else {
			loadFile ();
		}
	}

	private void loadURL () {
		try {
			final BaseViewerModel model = ModelBuilder.buildModel (url,
					auth_token, frame);

			if (sequenceID != null) {
				model.setFocus (sequenceID, start, end, contig);
			}

			SwingUtilities.invokeLater (new Runnable () {
				public void run () {
					frame.setModel (model);
				}
			});
		} catch (IOException e) {
			// TODO A better error.
			e.printStackTrace ();
		} catch (MauveFormatException e) {
			// TODO A better error.
			e.printStackTrace ();
		}

	}

	private void loadFile () {
		try {
			final BaseViewerModel model = ModelBuilder.buildModel (file, frame);
			SwingUtilities.invokeLater (new Runnable () {
				public void run () {
					frame.setModel (model);
				}
			});
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "An error occurred when reading the alignment file.\n" + e.getMessage(), "An error occurred", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace ();
		} catch (MauveFormatException e) {
			JOptionPane.showMessageDialog(null, "An error occurred when reading the alignment file.\n" + e.getMessage(), "An error occurred", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace ();
		}
	}

}
