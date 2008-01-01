package org.gel.air.ja.msg.hub;

import java.net.*;
import java.io.*;
import java.util.*;

import org.gel.air.ja.msg.*;

public class ConnectedNode extends Thread {

	protected Socket sock;
	protected PrintStream out;
	protected BufferedReader in;
	protected String [] group_names;
	protected String current_msg;
	protected static LinkedList crients = new LinkedList ();

	protected ConnectedNode () {
	}//constructor

	public ConnectedNode (Socket sock) throws Exception {
		super ();
		this.sock = sock;
		try {
			in = new BufferedReader (new InputStreamReader (sock.getInputStream ()));
			out = new PrintStream (sock.getOutputStream ());
			if (!in.readLine ().equals (GlobalInit.CONNECT_STRING)) {
				out.println ("connections of this type not accepted");
				sock.close ();
				throw new Exception ("not a real connection");
			}
			out.println ("connection accepted");
			LinkedList list = new LinkedList ();
			String foo = null;
			while (!(foo = in.readLine ()).equals (GlobalInit.DONE))
				list.add (foo);
			Object [] wark = list.toArray ();
			group_names = new String [wark.length];
			for (int i = 0; i < wark.length; group_names [i] = (String) wark [i++]);
			Arrays.sort (group_names);
			start ();
		} catch (IOException e) {
			e.printStackTrace ();
			GlobalInit.error ("Stream I/O error thingy");
		}
		synchronized (crients) {
			crients.add (this);
		}
	}//method createNew

	public void addGroup (String group) {
		String [] new_groups = new String [group_names.length + 1];
		int i = Arrays.binarySearch (group_names, group);
		if (i >= 0)
			return;
		i = -(i + 1);
		System.arraycopy (group_names, 0, new_groups, 0, i);
		new_groups [i] = group;
		System.arraycopy (group_names, i, new_groups, i + 1, group_names.length - i);
		group_names = new_groups;
	}//method addGroup

	public void removeGroup (String group) {
		String [] new_groups = new String [group_names.length - 1];
		int i = Arrays.binarySearch (group_names, group);
		if (i < 0)
			return;
		System.arraycopy (group_names, 0, new_groups, 0, i);
		System.arraycopy (group_names, i + 1, new_groups, i, new_groups.length - i);
		group_names = new_groups;
	}//method removeGroup

	public void run () {
		while (GlobalInit.run)
			try {
				current_msg = in.readLine ();
				ConnectionThread.runInThread (this, current_msg);
			} catch (Exception e) {
				crients.remove (this);
				break;
			}
	}//method run

}//class Crient