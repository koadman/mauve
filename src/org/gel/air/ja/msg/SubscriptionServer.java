package org.gel.air.ja.msg;

import java.net.Socket;
import java.io.IOException;

import org.gel.air.ja.msg.hub.*;
import org.gel.air.ja.threadpool.*;

/**
  *This class acts a server for publish-subscribe messaging.  It manages client connections,
  *receives messages from them, and sends them to interested clients.<p>
  *
  *@author the twins (twins@isx.com)
**/
public class SubscriptionServer extends Hub {

	/**
	  *Manages message distribution to various and sundry clients.
	**/
	protected SubscriptionLists lists;

	/**
	  *thread pool for accepting new connections
	**/
	protected Pool pool;

	/**
	  *Creates a new event server.  The port the server runs on is
	  *specified in the file hub.props.  The server is
	  *automatically started.
	**/
	public SubscriptionServer () {
		lists = new SubscriptionLists (getMatcher ());
		pool = new Pool (4);
	}//constructor

	/**
	  *Creates the matcher to use for deciding if wildcarded subject namespaces match, i.e.,
	  *messages sent to one should be received by the other.  If this method is overridden,
	  *all clients connecting to this server should be using the same class of object
	  *as this method returns.
	  *@return Matcher  the string matcher to use
	**/
	protected SubscriptionMatcher getMatcher () {
		return new WildcardHierarchyMatcher ();
	}//method getMatcher

	/**
	  *Accepts and handles new connections to the event server.  Should not
	  *be called directly.
	**/
	public void run () {
		while (GlobalInit.run) {
			try {
				final Socket socket = sock.accept ();
				pool.performInOtherThread (new PooledTask () {
					public void performTask () {
						try {
							new ConnectedNodeManager (socket, lists);
						}
						catch (Exception e) {
							e.printStackTrace ();
						}
					}
				});	
			}
			catch (IOException e) {
				GlobalInit.error ("An I/O error has occurred");
			}
			catch (SecurityException e) {
				GlobalInit.error ("A security error has occurred with this connection");
			}
		}
	}//method run

	/**
	  *Creates a new event server.
	  *@param args  ignored
	**/
	public static void main (String [] args) {
		new SubscriptionServer ();
	}

}//class EventServer