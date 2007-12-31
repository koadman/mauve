/**
 * Mauve.java
 * 
 * Title: Mauve Description: Viewer for multiple genome alignments and
 * annotation
 * 
 * @author Aaron Darling
 * @author Paul Infield-Harm
 * @version
 */

package org.gel.mauve.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.gel.mauve.ModelBuilder;
import org.gel.mauve.MyConsole;
import org.gel.mauve.remote.RemoteControlImpl;

public class Mauve {
	static Properties props = new Properties ();
	static String about_message = "";

	protected MauveFrame availableFrame;
	protected Vector frames; // List of open frames
	protected boolean check_updates = true;

	protected Mauve () {
	}

	// Main entry point
	static public void main (String [] args) {
		mainHook (args, new Mauve ());
	}
	
	public static void mainHook (String args [], final Mauve mv) {
		MyConsole.setUseSwing (true);
		MyConsole.showConsole ();
		ModelBuilder.setUseDiskCache (false);
		if (args.length >= 1) {
			final String filename = args[0];
			javax.swing.SwingUtilities.invokeLater (new Runnable () {
				public void run () {
					mv.init (filename);
				}
			});
		} else {
			javax.swing.SwingUtilities.invokeLater (new Runnable () {
				public void run () {
					mv.init ();
				}
			});
		}
	}

	public void init (String filename) {
		init ();
		if (filename.indexOf ("://") == -1) {
			loadFile (new File (filename));
		} else
			// this looks like a URL, try to load it
			try {
				loadURL (new URL (filename));
			} catch (MalformedURLException mue) {
				mue.printStackTrace ();
			}
    }
    
    private String release_version = "unknown";
    private String build_number = "0";
	
	public synchronized void init () {
		// On OS X the aqua look and feel is default, but we can't develop for
		// so
		// many different looks and feels. Set it to Metal.
		try {
			javax.swing.UIManager
					.setLookAndFeel ("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (InstantiationException e) {
		} catch (ClassNotFoundException e) {
		} catch (javax.swing.UnsupportedLookAndFeelException e) {
		} catch (IllegalAccessException e) {
		}

		if (!hasRequiredJVM ())
			return;
		if (!System.getProperties ().containsKey ("mauve.force.console")) {
			MyConsole.setUseSwing (true);
		}
		if (System.getProperties ().containsKey ("mauve.enable.remote")) {
			RemoteControlImpl.startRemote (this);
		}

		// read in the version properties file
		try {
			InputStream props_stream = MauveFrame.class
					.getResourceAsStream ("/version.properties");

			if (props_stream != null) {
				props.load (props_stream);
				release_version = props.getProperty ("release.version");
				build_number = props.getProperty ("build.number");
			}
		} catch (IOException ioe) {
			MyConsole.err ().println ("Couldn't read version.properties file.");
			ioe.printStackTrace (MyConsole.err ());
		}

        about_message = "<html><center>Mauve version " + release_version + " build " + build_number + " (c) 2003-2007 Aaron Darling, Paul Infield-Harm, and Anna Rissman<br>" + "<a href=\"http://gel.ahabs.wisc.edu\">http://gel.ahabs.wisc.edu/mauve</a><br>" + "<p>Mauve is free, open-source software.  See COPYING for details.</center></p><p>CITATION:<br>Mauve: Multiple Alignment of Conserved Genomic Sequence With Rearrangements.<br>Aaron C. E. Darling, Bob Mau, Frederick R. Blattner, Nicole T. Perna.<br><i>Genome Research</i> <b>14</b>(7):1394-1403</p><p>Mauve uses the Cinema-MX alignment editing modules written by Phillip Lord<br>DCJ rearrangement history inference contributed by Mike Tsai</p>";

        // check for updates in a separate thread so as not to
        // stall the GUI if the network is down
        javax.swing.SwingUtilities.invokeLater(new Runnable()
                {
                    public void run(){
                    	checkForUpdates();
                    }
                });

		frames = new Vector ();
		availableFrame = makeNewFrame ();
		new SplashScreen ("/images/mauve_logo.png", about_message, null, 3000);
		// mauve_splash.dispose();
		// mauve_splash.setVisible(false);
	}

	/**
	 * Checks the Mauve web server for a newer version of this software
	 */
	public void checkForUpdates () {
		if (!check_updates)
			return;
		try {
			String os_type = System.getProperty ("os.name");
			String build_date = props.getProperty ("build.timestamp");

			if (build_date == null) {
				MyConsole
						.err ()
						.println (
								"Couldn't check version, build.timestamp property not found.");
				return;
			}
			long local_version = Long.parseLong (build_date);

			// default to checking the linux version since it's
			// probably the least standardized OS currently
			String os_suffix = "linux";
			if (os_type.startsWith ("Windows"))
				os_suffix = "windows";
			else if (os_type.startsWith ("Mac"))
				os_suffix = "mac";
			URL hurl = new URL (
					"http://gel.ahabs.wisc.edu/mauve/downloads/latest."
							+ os_suffix);
			BufferedReader latest_in = new BufferedReader (
					new InputStreamReader (hurl.openStream ()));
			long latest_version = Long.parseLong (latest_in.readLine ());

			if (local_version < latest_version) {
				// check the OS type since linux can't do an auto-update
				if (!os_suffix.equals ("windows")) {
					JOptionPane
							.showMessageDialog (
									null,
									"<html>An updated version of Mauve is available.<br>See the Mauve web site at <a href=\"http://gel.ahabs.wisc.edu/mauve\">http://gel.ahabs.wisc.edu/mauve</a> for more details...",
									"Updated Mauve available",
									JOptionPane.INFORMATION_MESSAGE);
					return;
				}

                int dl_val = JOptionPane.showConfirmDialog(null, "An updated version of Mauve is available.\nWould you like to download and install it?", "Updated Mauve available", JOptionPane.YES_NO_OPTION);
                if (dl_val == 0)
                {
                    //
                    // download the installer to a temp file
                    //
                    MyConsole.out().println("Downloading installer...\n");
                    String dl_location = "http://gel.ahabs.wisc.edu/mauve/downloads/mauve_installer_";
                    dl_location += Long.toString(latest_version) + ".exe";
                    URL dlurl = new URL(dl_location);
                    InputStream is = dlurl.openStream();
                    File ins_file = File.createTempFile("mauve_ins", ".exe");
                    FileOutputStream output_file = new FileOutputStream(ins_file);
                    byte[] buf = new byte[1024 * 1024];
                    int read_chars = is.read(buf);
                    while (read_chars >= 0)
                    {
                        output_file.write(buf, 0, read_chars);
                        read_chars = is.read(buf);
                    }
                    // close the output file
                    output_file.close();
                    //
                    // run the installer
                    //
                    String[] ins_cmd = new String[1];
                    ins_cmd[0] = ins_file.getPath();
                    Runtime.getRuntime().exec(ins_cmd);
                    // delete the installer
                    ins_file.delete();
                    // exit this
                    System.exit(0);
                }
            }
        }
        catch (Exception e)
        {
            MyConsole.err().println("Error checking for updates.");
//            e.printStackTrace(MyConsole.err());
        }
    }

	private boolean hasRequiredJVM () {
		String jvm_version = System.getProperty ("java.version");
		int minor_version = Integer.parseInt (jvm_version.substring (2, 3));
		if (jvm_version.charAt (0) == '1') {
			if (minor_version < 4) {
				MyConsole
						.err ()
						.println (
								"Sorry, Mauve requires at least Java version 1.4 to operate correctly");
				return false;
			}
		}
		return true;
	}

	public synchronized void setFocus (String alignID, String sequenceID,
			long start, long end, String auth_token, String contig) {
		URL alignURL;
		try {
			alignURL = new URL (alignID);
		} catch (MalformedURLException e) {
			e.printStackTrace ();
			return;
		}

		Iterator it = frames.iterator ();
		while (it.hasNext ()) {
			MauveFrame frame = (MauveFrame) it.next ();
			if (frame.model != null
					&& alignURL.equals (frame.model.getSourceURL ())) {
				frame.model.setFocus (sequenceID, start, end, contig);
				frame.toFront ();
				return;
			}
		}

		// Load model and then zoom it.
		MauveFrame frame = getNewFrame ();
		Thread t = new Thread (new FrameLoader (frame, alignURL, sequenceID,
				start, end, auth_token, contig));
		t.start ();

	}

	/**
	 * Load a file into a RearrangementPanel. Create a new frame if this frame
	 * is already displaying a genome alignment
	 */
	public void loadFile (File rr_file) {
		MauveFrame frame = getNewFrame ();
		Thread t = new Thread (new FrameLoader (frame, rr_file));
		t.start ();
	}

	/**
	 * Load a file into a RearrangementPanel. Create a new frame if this frame
	 * is already displaying a genome alignment
	 */
	public void loadURL (URL url) {
		MauveFrame frame = getNewFrame ();
		Thread t = new Thread (new FrameLoader (frame, url));
		t.start ();
	}

	synchronized public void closeFrame (MauveFrame frame) {
		if (frames.size () > 1 || frame.rrpanel == null) {
			frame.setVisible (false);
			frames.remove (frame);
			frame.dispose ();
		} else {
			frame.reset ();
			availableFrame = frame;
		}
	}
	    synchronized public void frameClosed()
    {
	    if (frames.size() == 0)
	    {
	    	// invoke after the current thread has notified all listeners
	    	SwingUtilities.invokeLater(new Runnable()
                {
                    public void run(){
            	        System.exit(0); // no more frames left. exit.
                    }
                });
	    }
    }

	synchronized protected MauveFrame getNewFrame () {
		MauveFrame frame;
		if (availableFrame != null) {
			frame = availableFrame;
			availableFrame = null;
		} else {
			frame = makeNewFrame ();
		}
		return frame;
	}
	
	protected MauveFrame makeNewFrame () {
		MauveFrame frame = new MauveFrame (this);
		frames.add (frame);
		return frame;
	}
	 /**
     *  The string with the version number
     * @return The version number
     */
    public String getVersion(){
    	return release_version;
    }
}