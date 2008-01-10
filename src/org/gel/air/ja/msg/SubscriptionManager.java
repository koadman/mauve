package org.gel.air.ja.msg;

import java.util.Hashtable;
import java.net.InetAddress;

import org.gel.air.ja.msg.node.ConnectedHub;


/**
  *Client-side class representing a connection to the event server.  This is the entry point into
  *adding and removing subscriptions, as well as sending messages.  This class is a factory and
  *should not be instantiated directly; this prevents duplicate connections from being created.
  *Use the create method to get an appropriate instance.<p>
  *
  *The client uses namespaces and message handlers to control which message are received from
  *the server and how they are handled.  Messages are distributed on a publish-subscribe basis;
  *namespaces dictate which messages are received (you may subscribe to as many namespaces as you
  *want.)  Each namespace that message are received from has at least one message handler
  *associated with it; this dictates what code is executed when a message is received.  The same
  *message handler may be used for multiple namespaces.<p>
  *
  *For example, the following calls would result in MessageHandler foo receiving messages
  *published to both the "arthur" and "dent" namespaces:<p>
  *
  *event_cs_client.addGroup ("arthur", foo);
  *event_cs_client.addGroup ("dent", foo);
**/
public class SubscriptionManager extends ConnectedHub {

	/**
	  *Keeps track of where messages should be delivered.
	**/
	protected SubscriptionLists lists;

	/**
	  *Hashtable of client connections; used to prevent connecting twice to same host from same VM.
	**/
	protected static Hashtable clients = new Hashtable ();

	protected int port;

	protected String host;

	protected SubscriptionManager () {
	}

	/**
	  *Creates a new client connection to the specified server on the specified port.  Takes in
	  *a list of initial namespaces and a message handler to receive message from those spaces.
	  *If more than one message handler is desired for the namespaces, use the addGroup method
	  *instead of passing the namespaces and message handler here.
	  *@param groups  initial namespaces to receive messages from
	  *@param general  message handler to receive messages from initial namespaces
	  *@param port  the port number to connect to
	  *@param server  the host name (or the IP address) of the server to connect to
	**/
	protected SubscriptionManager (String [] groups, MessageHandler general, int port, String server) {
		super (groups, null, port, server);
		try {
			this.port = port;
			host = InetAddress.getByName (server).getHostAddress ();
			lists = new SubscriptionLists (getMatcher ());
			for (int i = 0; i < groups.length; i++)
				lists.addSubject (groups [i], general);
			clients.put (server + ":" + port, this);
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}//constructor

	/**
	  *Creates the matcher to use for deciding if wildcarded subject namespaces match, i.e.,
	  *messages sent to one should be received by the other.  If this method is overridden,
	  *it and the corresponding method in EventServer should be overridden to return the
	  *same type of object.
	  *@return Matcher  the string matcher to use
	**/
	protected SubscriptionMatcher getMatcher () {
		return new WildcardHierarchyMatcher ();
	}//method getMatcher


	/**
	  *Returns an appropriate client connection to the specified server on the specified port.  Takes in
	  *a list of initial namespaces and a message handler to receive message from those spaces.
	  *If more than one message handler is desired for the namespaces, use the addGroup method
	  *instead of passing the namespaces and message handler here.
	  *@param groups  initial namespaces to receive messages from
	  *@param general  message handler to receive messages from initial namespaces
	  *@param port  the port number to connect to
	  *@param server  the host name (or the IP address) of the server to connect to
	  *@return EventCSClient  the properly configured client connection
	**/
	public static SubscriptionManager create (String [] groups, MessageHandler general, int port, String server) {
		SubscriptionManager client = (SubscriptionManager) clients.get (server + ":" + port);
		if (client == null)
			return new SubscriptionManager (groups, general, port, server);
		else {
			for (int i = 0; i < groups.length; i++)
				client.addGroup (groups [i], general);
			return client;
		}
	}//method create


	/**
	  *Creates an appropriate client connection to the specified server on the specified port.
	  *Does not receive messages from any namespace until addGroup is called.
	  *@param port  the port number to connect to
	  *@param server  the host name (or the IP address) of the server to connect to
	**/
	protected SubscriptionManager (int port, String server) {
		this (new String [0], null, port, server);
	}//constructor

	/**
	  *Returns an appropriate client connection to the specified server on the specified port.
	  *Does not receive messages from any namespace until addGroup is called.
	  *@param port  the port number to connect to
	  *@param server  the host name (or the IP address) of the server to connect to
	  *@return EventCSClient  the properly configured client connection
	**/
	public static SubscriptionManager create (String server, int port) {
		return create (new String [0], null, port, server);
	}//method create


	/**
	  *Reads messages from the socket.  Should only be called internally.
	**/
	public void run () {
		try {
			while (GlobalInit.run) {
				String msg = in.readLine ();
				System.out.println ("mess: " + msg);
				lists.handleSocketString (msg, null);
			}
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}//method run


	/**
	  *Subscribes to client to specified namespace and sends messages to specified handler.
	  *@param group  the namespace to subscribe to
	  *@param handler  the message handler to receive messages from the specified namespace
	**/
	public void addGroup (String group, MessageHandler handler) {
		//super.addGroup (group);
		lists.addSubject (group, handler);	
	}//method addGroup

	/**
	  *Stops the specified message handler from receiving further messages from the specified
	  *namespace.  Note that if other message handlers are currently receiving messages
	  *from the group, they will continue to do so.
	  *@param group  the namespace to stop receiving messages from
	  *@param handler  the messages handler that is to stop receiving messages from the specified namespace
	**/
	public void removeGroup (String group, MessageHandler handler) {
		//super.removeGroup (group);
		lists.removeSubject (group, handler);
	}//method removeGroup

	/**
	  *Sends a string to a specified namespace.
	  *@param group  the namespace to send to
	  *@param msg  the text to send
	**/
	public void send (String group, String msg) {
		sendString (group, msg);
		System.out.println ("NO GOOD");
	}

	/**
	  *Sends a message.  The namespace to send to and the message text
	  *are encapsulated within the message.
	  *@param m  the message to send
	**/
	public void send (Message m) {
		synchronized (out) {
			out.println (m.socketString ());
			out.flush ();
		}
	}

	public String getHost () {
		return host;
	}

	public int getPort () {
		return port;
	}

}//class EventCSClient