package org.gel.air.ja.msg;

import java.net.Socket;
import java.io.IOException;

import org.gel.air.ja.msg.hub.*;
import org.gel.air.ja.threadpool.*;

/**
  *This class acts a server for messaging, receiving and passing
  *along messages to subscribed clients
  *
**/
public class SubscriptionServer extends Hub {

	/**
	  *Manages all subscriptions for one subscription distributor
	**/
	protected SubscriptionLists lists;

	/**
	  *new connections accepted in these threads
	**/
	protected Pool pool;

	/**
	  *Constructor
	**/
	public SubscriptionServer (int port, int max) {
		super (port, max);
		lists = new SubscriptionLists (createMatcher ());
		pool = new Pool (4);
	}

	/**
	  *Creates the matcher to use for deciding which namespaces should receive a message.
	  *Both receivers and senders should use a matcher that follows the same
	  *matching conventions.
	  *
	  *@return Matcher  the string matcher to use
	**/
	protected SubscriptionMatcher createMatcher () {
		return new WildcardHierarchyMatcher ();
	}

	/**
	  *Called for each new connection; don't call directly.
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
	}

	/**
	  *For testing.
	  *@param args  ignored
	**/
	public static void main (String [] args) {
		//new SubscriptionServer ();
	}

}