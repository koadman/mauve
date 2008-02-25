package org.gel.air.ja.msg;

import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;

import org.gel.air.ja.msg.hub.ConnectionThread;

public class GlobalInit {

	/*public static Properties props;
	public static final String PROP_FILE_NAME = "hub.props";*/
	public static int PORT = 1337;
	public static int MAX_CONNECTIONS = 100;
	public static boolean run = true;
	public static final String ONE = "" + (char) 172;
	public static final String TWO = ONE + ONE;
	public static final String THREE = TWO + ONE;
	public static final String CONNECT_STRING = "|capture_meklajd83247923dsd38d|";
	
	/*static {
		try {
			FileInputStream in = new FileInputStream (PROP_FILE_NAME);
			props = new Properties ();
			props.load (in);
			ConnectionThread.pool = new ConnectionThread [Integer.parseInt (props.getProperty ("pool_size"))];
			for (int i = 0; i < ConnectionThread.pool.length; i++)
				ConnectionThread.pool [i] = new ConnectionThread ();
			in.close ();
		} catch (IOException e) {
			error ("Properties file unreadable");
			System.exit (0);
		}
	}*/

	public static final String DONE = "|release_me18e9jdflle9492|";

	public static void error (String msg) {
		JOptionPane.showMessageDialog (null, msg, "Error", JOptionPane.ERROR_MESSAGE);
	}

}