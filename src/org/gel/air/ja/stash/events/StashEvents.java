package org.gel.air.ja.stash.events;


import org.xml.sax.*;
import org.apache.xerces.parsers.SAXParser;
import org.gel.air.ja.msg.*;
import org.gel.air.ja.stash.*;
import org.gel.air.util.SystemUtils;

import java.io.*;
import java.net.InetAddress;
import java.util.*;


public class StashEvents implements MessageHandler, StashConstants {

	protected AbstractStashEventManager events;
	protected StashLoader loader;
	protected Stash callback_map;

	protected static final Iterator EMPTY = new LinkedList ().iterator ();


	public StashEvents (AbstractStashEventManager ev, StashLoader l) {
		events = ev;
		loader = l;
		callback_map = new Stash ();
		ev.add ("update/...", this);
	}


	public void addCallbackTo (Stash hash, StashChangeListener callback) {
		LinkedList callbacks = (LinkedList) callback_map.get (hash);
		if (callbacks == null) {
			callbacks = new LinkedList ();
			callback_map.put (hash, callbacks);
		}
		callbacks.add (callback);
	}


	public void removeCallback (Stash hash, StashChangeListener callback) {
		LinkedList callbacks = (LinkedList) callback_map.get (hash);
		callbacks.remove (callback);
	}


	public void process (Message msg) {
		String dest = msg.getDest ();
		if (dest.startsWith ("update/"))
			update (msg, dest.substring (7, dest.length () - 1));
	}


	protected void update (Message msg, String dest) {
		try {
			int slash = dest.indexOf ('/');
			System.out.println ("dest: " + dest);
			Stash current = loader.getDefaults ().getHashtable (
					dest.substring (0, slash)).getHashtable (dest.substring
					(slash + 1,	dest.length ()));
			Properties props = SystemUtils.stringToProps (msg.getMessage ());
			//System.out.println ("got props");
			LinkedList callbacks = (LinkedList) callback_map.get (current);
			Iterator itty = props.keySet ().iterator ();
			while (itty.hasNext ()) {
				String key = (String) itty.next ();
				String value = props.getProperty (key);
				//System.out.println ("prop: " + key + " " + props.get (key));
				StringTokenizer toke = new StringTokenizer (key, Q, false);
				Stash ephemeral = current;
				while (toke.hasMoreTokens ()) {
					key = toke.nextToken ();
					//System.out.println ("foo: " + key);
					if (toke.hasMoreTokens ())
						ephemeral = ephemeral.getHashtable (key);
				}
				//System.out.println ("eph: " + (ephemeral == null));
				//System.out.println ("value: " + value);
				if (value.equals (REMOVE_VALUE))
					ephemeral.remove (key);
				else
					ephemeral.put (key, value);
			}
			itty = callbacks == null ? EMPTY : callbacks.iterator ();
			Stash changes = new Stash ();
			changes.putAll (props);
			changes.put (ID, current.get (ID));
			while (itty.hasNext ()) {
				StashChangeListener callback = (StashChangeListener) itty.next ();
				callback.dataChanged (changes);
			}
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

}