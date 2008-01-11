package org.gel.air.ja.stash;

import java.util.*;

import org.gel.air.util.SystemUtils;

/**
 * Acts as a Hashtable representing an object stored in a Stash.  Has  special properties
 * for the name and id fields.  Expects all values to be stored as Strings or
 * Hashtables, and contains conversion methods ((throws exceptions if null, 
 * except for get (key)).  Stashes representing updates are constructed using the put
 * method, but persistent stashes should be modified through the StashManager interfaces.
 * 
 * @author Anna I Rissman/James Lowden
 *
 */
public class Stash extends Hashtable <Object, Object> implements 
		Comparable <Object>, StashConstants {
	
	protected static Stash defaults;
	
	public Stash (String class_type) {
		this (class_type, class_type + KEY_SEPARATOR + SystemUtils.makeUniqueString());
	}
	
	public Stash (String class_type, String id) {
		put (CLASS_TYPE_STRING, class_type);
		if (id == null)
			id = class_type + KEY_SEPARATOR + SystemUtils.makeUniqueString();
		put (ID, id);
	}
	
	public Stash () {
	}
	
	public Object put (Object key, Object val) {
		if (key instanceof String) {
			if (key.equals (ID)) {
				String s = (String) val;
				String ct = getString (CLASS_TYPE_STRING) + KEY_SEPARATOR;
				if (s.startsWith (ct))
					s = s.substring (ct.length ());
				if (s.indexOf ("\\") > -1 || s.indexOf("/") > -1)
					throw new Error ("Invalid ID--contains \\ or /");
			}
			else
				key = StashXMLLoader.makeKey ((String) key);		
		}
		else {
			if (key instanceof Number)
				key = key.toString();
			if (val instanceof Number)
				val = val.toString();
		}
		return super.put (key, val);
	}

	public String getString (Object key) {
		return (String) get (key);
	}

	public int getInt (Object key) {
		return Integer.parseInt ((String) get (key));
	}
	
	public long getLong (Object key) {
		return Long.parseLong((String) get (key));
	}
	public boolean getBool (Object key) {
		return ((String) get (key)).toLowerCase ().equals ("true");
	}

	/**
	 * will return null if Stash isn't loaded
	 * 
	 * @param key
	 * @return
	 */
	public Stash getHashtable (Object key) {
		Object value = super.get (StashXMLLoader.makeKey ((String) key));
		if (value instanceof String) {
			int index = ((String) value).indexOf (KEY_SEPARATOR);
			if (index < 0)
				value = defaults.getHashtable (value);
			else {
				String class_type = ((String) value).substring (0, index);
				String val = (String) value;
				value = defaults.getHashtable (class_type);
				if (value != null)
					value = ((Stash) value).getHashtable (val);
			}
		}
		return (Stash) value;
	}
		
	public Object get (Object key) {
		if (key instanceof Number)
			key = key.toString ();
		return super.get(key);
	}
	
	public String toString () {
		return getString (NAME);
	}

	public String descriptiveString () {
		return super.toString ();
	}

	public int compareTo (Object foo) {
		return getString (NAME).compareTo (((Stash) foo).getString (NAME));
	}

	public boolean equals (Object foo) {
		return getString (ID).equals (((Stash) foo).getString (ID));
	}

	public int hashCode () {
		return System.identityHashCode (this);
	}

	public Stash replicate () {
		Stash noo = new Stash ();
		Iterator keys = keySet ().iterator ();
		while (keys.hasNext ()) {
			String key = (String) keys.next ();
			Object value = get (key);
			if (!(value instanceof Stash) && !((String) value).equals(OPTIONAL))
				noo.put (key, value);
			else if (!key.startsWith (ITEM))
				noo.put (key, ((Stash) value).replicate ());
		}
		noo.put (ID, noo.getString (CLASS_TYPE_STRING) + KEY_SEPARATOR + SystemUtils.makeUniqueString ());
		return noo;
	}
	
	public static Stash getDefaults () {
		return defaults;
	}

}