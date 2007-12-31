package org.gel.air.ja.msg.node;

import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;

import org.gel.air.ja.msg.*;

public class ConnectedHub extends Thread {

	protected Socket sock;
	public String PROP_FILE_NAME = "node.props";
	protected BufferedReader in;
	protected PrintStream out;
	protected ActionListener listener;

	protected ConnectedHub () {
	}

	public ConnectedHub (String [] groups, ActionListener listener) {
		this.listener = listener;
		try {
			BufferedReader temp = new BufferedReader (new FileReader (PROP_FILE_NAME));
			String stuff = null;
			int port = -1;
			String server = null;
			while ((stuff = temp.readLine ()) != null) {
				if (stuff.startsWith ("port = "))
					port = Integer.parseInt (stuff.substring (7));
				else if (stuff.startsWith ("server = "))
					server = stuff.substring (9);
			}
			temp.close ();
			sock = new Socket (server, port);
			in = new BufferedReader (new InputStreamReader (sock.getInputStream ()));
			out = new PrintStream (sock.getOutputStream ());
			out.println (GlobalInit.CONNECT_STRING);
			if (in.readLine ().equals ("connection accepted")) {
				for (int i = 0; i < groups.length; i++)
					out.println (groups [i]);
				out.println (GlobalInit.DONE);
			}
			start ();
		}
		catch (UnknownHostException e) {
			GlobalInit.error ("The host specified in the properties file could not be contacted");
		}
		catch (IOException e) {
			GlobalInit.error ("An I/O error has occurred");
		}
		catch (SecurityException e) {
			GlobalInit.error ("A security exception has occurred");
		}
	}//constructor

	public ConnectedHub (String [] groups, ActionListener listener, int port, String server) {
		this.listener = listener;
		try {
			sock = new Socket (server, port);
			in = new BufferedReader (new InputStreamReader (sock.getInputStream ()));
			out = new PrintStream (sock.getOutputStream ());
			out.println (GlobalInit.CONNECT_STRING);
			System.out.println ("connect string sent");
			if (in.readLine ().equals ("connection accepted")) {
				System.out.println ("connection accepted");
				for (int i = 0; i < groups.length; i++)
					out.println (groups [i]);
				out.println (GlobalInit.DONE);
			}
			start ();
		}
		catch (UnknownHostException e) {
			GlobalInit.error ("The host specified in the properties file could not be contacted");
		}
		catch (IOException e) {
			e.printStackTrace ();
			GlobalInit.error ("An I/O error has occurred");
		}
		catch (SecurityException e) {
			GlobalInit.error ("A security exception has occurred");
		}
	}//constructor

	public void sendString (String group, String what) {
		out.println (group + GlobalInit.ONE + what);
	}//method sendString

	public void addGroup (String group) {
		out.println (GlobalInit.TWO + "add " + group);
	}//method addGroup

	public void removeGroup (String group) {
		out.println (GlobalInit.TWO + "remove " + group);
	}//method removeGroup

	public void run () {
		try {
			while (GlobalInit.run) {
				String msg = in.readLine ();
				int index = msg.lastIndexOf (GlobalInit.ONE);
				listener.actionPerformed (new ActionEvent (msg.substring (0, index),
						ActionEvent.ACTION_PERFORMED, msg.substring (index + 1)));
			}
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}//method run

}//class Crient