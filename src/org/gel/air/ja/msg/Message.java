package org.gel.air.ja.msg;

import java.util.*;


/**
  *This class represents a message, including its namespace and text
  *Note that ASCII character 172 is used as an internal delimeter and should
  *not be used in either namespaces or messages.<p>
  *
**/
public class Message {

	/**
	  *The message text.
	**/
	protected String mess;

	/**
	  *The namespace the message goes to.
	**/
	protected String subject;

	/**
	  *The message as it will be written to the socket.
	**/
	protected String socket;

	/**
	  *Creates a new message with the specified text and namespace.
	  *@param message  the message text
	  *@param namespace  the namespace the message should be sent to
	**/
	public Message (String message, String namespace) {
		mess = message;
		subject = namespace;
		StringBuffer buf = new StringBuffer (mess);
		replace (GlobalInit.THREE, GlobalInit.THREE + GlobalInit.ONE73, buf);
		replace ("\n", GlobalInit.THREE + "n", buf);
		replace ("\r", GlobalInit.THREE + "r", buf);
		System.out.println ("subject: " + subject);
		socket = subject + GlobalInit.ONE73 + buf.toString ();
	}

	/**
	  *Constructor for internal use only.  If this constructor is used,
	  *all instance variables must be set elsewhere.
	**/
	protected Message () {}

	/**
	  *Creates messages from a socket representation.  Since the socket representation
	  *can contain multiple namespaces, this breaks it down into separate messages for each.
	  *@param socket_str  the socket representation of a message with (possibly) multiple namespaces
	  *@return Iterator  an iterator of created messages
	**/
	protected static Iterator createMessages (String socket_str, final SubscriptionMatcher matcher) {
		System.out.println ("sock: " + socket_str.substring (0, socket_str.indexOf(GlobalInit.ONE73)));
		StringBuffer buf = new StringBuffer (socket_str);
		String temp = socket_str;
		replace (GlobalInit.THREE + "n", "\n", buf);
		replace (GlobalInit.THREE + "r", "\r", buf);
		replace (GlobalInit.THREE + GlobalInit.ONE73, GlobalInit.THREE, buf);
		socket_str = buf.toString ();
		int ind = socket_str.indexOf (GlobalInit.ONE);
		int start = 0;
		LinkedList to = new LinkedList ();
		int separator = socket_str.indexOf(GlobalInit.ONE73) + 1;
		while (ind > -1 && ind + 1 < separator && socket_str.charAt (ind + 1) != GlobalInit.ONE.charAt (0)) {
			to.add (socket_str.substring (start, ind));
			start = ind + 1;
			ind = socket_str.indexOf (GlobalInit.ONE, start);
		}
		to.add(socket_str.substring (start, separator - 1));
		System.out.println ("ns: " + to);
		final Message mess = new Message ();
		mess.mess = socket_str.substring (separator, socket_str.length ());
		final String text  = temp.substring (separator, temp.length ());
		final Iterator itty = to.iterator ();
		return new Iterator () {
			public boolean hasNext () {
				return itty.hasNext ();
			}

			public Object next () {
				mess.subject = matcher.makeSubjectAbsolute ((String) itty.next ());
				mess.socket = mess.subject + GlobalInit.ONE73 + text;
				return mess;
			}

			public void remove () {}
		};
	}

	/**
	  *Returns the message text.
	  *@return String  the message text
	**/
	public String getMessage () {
		return mess;
	}

	/**
	  *Returns the namespace the message was sent to.  Note that this
	  *may not be exactly the same as the namespace the message handler
	  *which received the message was subscribed to.
	  *@return String  the namespace
	**/
	public String getDest () {
		return subject;
	}

	/**
	  *This exists for backwards compatibility reasons; it does not actually
	  *do anything.
	  *@param pos  ignored
	**/
	public void setCurrent (int pos) {
	}

	/**
	  *This exists for backwards compatibility reasons; it returns the message
	  *text, but getMessage should be called instead.
	  *@return String  the message text 
	**/
	public String nextStr () {
		return mess;
	}

	/**
	  *Returns the socket representation of this message.
	  *@return String  the socket representation of the message
	**/
	protected String socketString () {
		return socket;
	}

	/**
	  *Replaces all occurences of one substring within a StringBuffer with another
	  *substring.
	  *@param have  the substring to look for
	  *@param want  the substring to replace it with
	  *@param buffy  the StringBuffer to search and replace within
	**/
	protected static void replace (String have, String want, StringBuffer buffy) {
		for (int i = 0; i < buffy.length (); i++) {
			int k = i;
			if (buffy.charAt (i) == have.charAt (0)) {
				int j = 1;
				while (++i < buffy.length () && j < have.length () && buffy.charAt (i) == have.charAt (j++));
				if (j == have.length () && buffy.charAt (i - 1) == have.charAt (j - 1)) {
					buffy.replace (i - have.length (), i, want);
					k += want.length () - 1;
				}
			}
			i = k;
		}
	}
}