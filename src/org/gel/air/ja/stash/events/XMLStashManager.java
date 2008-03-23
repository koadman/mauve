package org.gel.air.ja.stash.events;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.Timer;

import org.gel.air.ja.msg.AbstractMessageManager;
import org.gel.air.ja.msg.Message;
import org.gel.air.ja.msg.MessageHandler;
import org.gel.air.ja.stash.Stash;
import org.gel.air.ja.stash.StashConstants;
import org.gel.air.ja.stash.StashXMLLoader;
import org.gel.air.util.IOUtils;
import org.gel.air.util.SystemUtils;


public class XMLStashManager implements MessageHandler, StashConstants, ActionListener {


	protected AbstractMessageManager events;
	protected File root_dir;
	protected StashXMLLoader loader;
	
	/**
	 * cache of support files
	 */
	protected Hashtable <String, Object[]> sup_file_cache;
	
	/**
	 * timer for removing streams from support file cache
	 */
	protected Timer timer;
	
	public static final int FILE_POS_IND = 0;
	public static final int STREAM_IND = 1;
	public static final int LAST_ACCESS_IND = 2;
	
	public static final int FIVE_MINS = 300000;
	
	public XMLStashManager (AbstractMessageManager ev, File root_dir, StashXMLLoader load) {
		events = ev;
		this.root_dir = root_dir;
		loader = load;
		sup_file_cache = new Hashtable ();
		ev.add ("stash/...", this);
		timer = new Timer (FIVE_MINS, this);
	}


	public void process (Message msg) {
		String dest = msg.getDest ();
		System.out.println ("destproc: " + dest);
		if (dest.startsWith (UPDATE_NS))
			update (msg, dest.substring (UPDATE_NS.length (), dest.length ()));
		else if (dest.startsWith (GET_OBJ_NS))
			get (msg, dest.substring (GET_OBJ_NS.length(), dest.length ()));
		else if (dest.startsWith (GET_FILE_NS)) {
			if (dest.startsWith (GET_FILE_LENGTH_NS))
				getFileLength (msg, dest.substring (GET_FILE_LENGTH_NS.length(), dest.length ()));
			else
				getFile (msg, dest.substring (GET_FILE_NS.length(), dest.length ()));
		}
		/*else if (dest.startsWith ("getAll/"))
			getAll (msg, dest.substring (7, dest.length ()));*/
	}


	protected void update (Message msg, String dest) {
		try {
			int slash = dest.indexOf ('/');
			String obj_id = dest.substring (slash + 1, dest.length () - 1);
			File file = loader.getFileForStash (obj_id);
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
				loader.stashChanged (current);
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}


	protected void get (Message msg, String dest) {
		try {
			dest = dest.substring (0, dest.lastIndexOf ("/"));
			File f = loader.getFileForStash (dest);
			System.out.println ("file: " + f);
			Stash stash = loader.getStash (dest);
			BufferedReader in = new BufferedReader (new FileReader (f));
			String s = null;
			StringBuffer buf = new StringBuffer ();
			while ((s = in.readLine ()) != null) {
				buf.append (s);
				buf.append ("\n");
			}
			in.close ();
			System.out.println ("sending");
			events.sendString (buf.toString (), msg.getMessage ());
		}
		catch (Exception e) {
			e.printStackTrace ();
			events.sendString ("", msg.getMessage ());
		}
	}

	protected void getFileLength (Message msg, String dest) {
		String ret = "";
		try {
			File f = new File (root_dir, dest);
			if (f.exists ())
				ret = f.length() + "";			
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
		events.sendString (ret, msg.getMessage ());
	}
	
	/**
	 * assumes start and end pos encoded in dest string start from 0 and
	 * end at file length - 1.  Range is start and end inclusive.
	 * 
	 * @param msg
	 * @param dest
	 */
	protected void getFile (Message msg, String dest) {
		String ret = "";
		try {
			int ind = dest.indexOf(" ");
			int start = Integer.parseInt(dest.substring (0, ind));
			dest = dest.substring (ind + 1);
			ind = dest.indexOf(" ");
			int end = Integer.parseInt(dest.substring (0, ind));
			dest = dest.substring (ind + 1);
			Object [] data = null;
			synchronized (sup_file_cache) {
				if (!sup_file_cache.containsKey (dest)) {
					data = new Object [3];
					sup_file_cache.put(dest,data);
				}
				else
					data = sup_file_cache.get (dest);
			}
			File file = new File (root_dir, dest);
			synchronized (data) {
				if (data [STREAM_IND] == null) {
					setInputStream (file, data);
				}
				BufferedInputStream in = (BufferedInputStream) data [STREAM_IND];
				long pos = (Long) data [FILE_POS_IND];
				if (start < pos) {
					try {
						in.reset();
						data [FILE_POS_IND] = 0L;
					} catch (RuntimeException e) {
						in.close();
						setInputStream (file, data);
						in = (BufferedInputStream) data [STREAM_IND];
					}
				}
				pos = (Long) data [FILE_POS_IND];
				if (start > pos)
					IOUtils.guaranteedSkip(in, start - pos);
				byte [] bytes = new byte [end - start + 1];
				IOUtils.guaranteedRead(in, bytes);
				ret = new String (bytes);
				if (ret.length() != bytes.length)
					System.out.println ("string sucks");
				data [FILE_POS_IND] = (long) (end + 1);
				data [LAST_ACCESS_IND] = System.currentTimeMillis ();
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
		events.sendString (ret, msg.getMessage ());
	}
	
	protected void setInputStream (File file, Object [] data) throws IOException {
		data [STREAM_IND] = new BufferedInputStream (
				new FileInputStream (file));
		data [FILE_POS_IND] = 0L;
		if (file.length() < Integer.MAX_VALUE)
			((BufferedInputStream) data [STREAM_IND]).mark((int) 
					file.length ());
		else
			((BufferedInputStream) data [STREAM_IND]).mark(
					Integer.MAX_VALUE);
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

	// it is assumed that this timer is the only thing removing
	// items from the cache; otherwise, vals might be null
	public void actionPerformed(ActionEvent e) {
		Iterator <String> itty = sup_file_cache.keySet ().iterator ();
		long time = System.currentTimeMillis ();
		while (itty.hasNext ()) {
			String file = itty.next ();
			Object [] vals = sup_file_cache.get (file);
			synchronized (vals) {
				if (time - ((Long) vals [LAST_ACCESS_IND]) > FIVE_MINS) {
					try {
						((BufferedInputStream) vals [STREAM_IND]).close ();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					itty.remove ();
				}
			}
		}
	}

	/*public static void main (String [] args) throws Exception {
		AbstractMessageManager ev = AbstractMessageManager.createEvents ();
		ev.add (InetAddress.getLocalHost ().getHostAddress () + ":" +
				EasyBunny.props.getProperty ("port") + "/...",
				new FileWriterEvents (ev, new File (args [0])));
	}*/

}