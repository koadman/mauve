package org.gel.air.ja.stash.events;


import org.xml.sax.*;
import org.apache.xerces.parsers.SAXParser;
import org.gel.air.ja.msg.*;
import org.gel.air.ja.stash.*;
import org.gel.air.util.SystemUtils;

import java.io.*;
import java.net.InetAddress;
import java.util.*;


public class XMLStashManager implements MessageHandler, StashConstants {

	protected AbstractMessageManager events;
	protected File root_dir;
	protected StashXMLLoader loader;


	public XMLStashManager (AbstractMessageManager ev, File root_dir, StashXMLLoader load) {
		events = ev;
		this.root_dir = root_dir;
		loader = load;
		ev.add ("update/...", this);
	}


	public void process (Message msg) {
		String dest = msg.getDest ();
		System.out.println ("dest: " + dest);
		if (dest.startsWith ("update/"))
			update (msg, dest.substring (7, dest.length ()));
		else if (dest.startsWith ("get/"))
			get (msg, dest.substring (4, dest.length ()));
		else if (dest.startsWith ("getAll/"))
			getAll (msg, dest.substring (7, dest.length ()));
	}


	protected void update (Message msg, String dest) {
		PrintStream out = null;
		try {
			int slash = dest.indexOf ('/');
			String obj_id = dest.substring (slash + 1, dest.length () - 1);
			File file = new File (root_dir, obj_id);
			String file_name = file.getAbsolutePath ();
			String cl = dest.substring (0, slash);
			if (file.exists ())
				loader.readInto (loader.getDefaults ().getHashtable (cl), file_name);
			else
				loader.getDefaults ().getHashtable (cl).put (obj_id, new Stash ());
			Stash current = loader.getDefaults ().getHashtable (cl
					).getHashtable (StashXMLLoader.makeKey (obj_id));
			synchronized (current) {
				Properties props = SystemUtils.stringToProps (msg.getMessage ());
				Iterator itty = props.keySet ().iterator ();
				while (itty.hasNext ()) {
					String object_path = (String) itty.next ();
					String value = props.getProperty (object_path);
					Stash temp = current;
					while (object_path.indexOf (Q) > -1) {
						int ind = object_path.indexOf (Q);
						if (ind == -1)
							ind = object_path.length ();
						String key = object_path.substring (0, ind);
						Object temp2 = temp.get (key);
						if (temp2 == null) {
							if (object_path.indexOf (Q) > -1) {
								temp2 = new Stash ();
								((Hashtable) temp2).put (ID, key);
								temp.put (key, temp2);
							}
							else
								break;
						}
						else if (!(temp2 instanceof Hashtable))
							break;
						temp = (Stash) temp2;
						object_path = object_path.substring (ind + 1, object_path.length ());
					}
					if (value.equals (REMOVE_VALUE))
						temp.remove (object_path);
					else
						temp.put (object_path, value);
				}
				out = new PrintStream (new BufferedOutputStream (
						new FileOutputStream (file_name)));
				loader.writeXMLFile (current, out);
				out.close ();
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
			try {
				out.close ();
			}
			catch (Exception f) {
				f.printStackTrace ();
			}
		}
	}


	protected void get (Message msg, String dest) {
		try {
			File f = new File (root_dir, dest);
			BufferedReader in = new BufferedReader (new FileReader (f));
			String s = null;
			StringBuffer buf = new StringBuffer ();
			while ((s = in.readLine ()) != null) {
				buf.append (s);
				buf.append ("\n");
			}
			in.close ();
			events.sendString (buf.toString (), msg.getMessage ());
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}


	protected void getAll (Message msg, String dest) {
		try {
			File [] files = new File (root_dir, dest).listFiles ();
			StringBuffer buf = new StringBuffer ();
			for (int i = 0; i < files.length; i++) {
				if (!files [i].isDirectory ()) {
					buf.append (files [i].getName ());
					buf.append ("\n");
				}
			}
			events.sendString (buf.toString (), msg.getMessage ());
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}
		
	/*public static void main (String [] args) throws Exception {
		Events ev = Events.createEvents (EasyBunny.props.getProperty ("server"));
		ev.add (InetAddress.getLocalHost ().getHostAddress () + ":" +
				EasyBunny.props.getProperty ("port") + "/...",
				new FileWriterEvents (ev, new File (args [0])));
	}*/

}