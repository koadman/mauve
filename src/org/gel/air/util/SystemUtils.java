package org.gel.air.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;


public class SystemUtils {
	
	private static String ip;
	private static int current;
	
	static {
		try {
			ip = InetAddress.getLocalHost ().getHostAddress ();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public static Properties stringToProps (String s) {
		try {
			Properties props = new Properties ();
			ByteArrayInputStream in = new ByteArrayInputStream (s.getBytes ());
			props.clear ();
			props.load (in);
			in.close ();
			return props;
		}
		catch (Exception e) {
			e.printStackTrace ();
			return null;
		}
	}//method stringToProps

	/**
	  * formatString creates a properly formatted drag string from a properties object.
	  * @param props  The properties that should be included in the drag string.
	  * @return String  A correctly formatted drag string.
	**/
	public static String propsToString (Properties props) {
		/*Enumeration foo = props.propertyNames ();
		String drag = "";
		while (foo.hasMoreElements ()) {
			String name = (String) foo.nextElement ();
			drag += name + " = " + escapeBackslashes (props.getProperty (name)) + "\n";
		}
		return drag;*/
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream ();
			props.store (baos, "");
			baos.close ();
			return baos.toString ();
		}
		catch (Exception e) {
			e.printStackTrace ();
			return null;
		}
	}//method formatString

	public static String makeUniqueString () {
		synchronized (ip) {
			String temp = ip + System.currentTimeMillis () + "" + current;
			current++;
			while (temp.length () < 25)
				temp += "0";
			return temp;
		}
	}

}
