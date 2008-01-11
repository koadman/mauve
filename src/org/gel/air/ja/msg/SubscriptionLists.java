package org.gel.air.ja.msg;

import java.util.*;
import java.awt.Point;



/**
  *This class handles message distribution for both the client and server side.
  *
  *Wildcarded and non-wildcarded namespaces are stored separately within this class
  *for efficiency.  The matchSubjects method in the matcher object is only used for
  *matching two string where at least one contains a wildcard; otherwise, direct
  *string comparison is used.<p>
  *
  *Since there are situations in which the "same" message needs to be sent to
  *multiple places, it is slightly unclear when and how many times a MessageHandler
  *should receive a message.  Current behavior is as follows:<p>
  *
  *<ul>
  *<li>If there are two message handlers subscribed to the same namespace, they
  *will each receive identical messages.
  *<li>If a single message handler is subscribed twice to the same namespace, it
  *will only receive a message sent to that namespace once.
  *<li>A message sent to multiple namespaces (e.g., "foo" and "blah") where there is
  *a message handler subscribed to both, that message handler will receive two messages
  *with identical text but different namespaces.
  *<li>If a message is sent to a single namespace (e.g., "blah") and a message handler is subscribed
  *to two namespaces that match the message's namespace (e.g., "*ah" and "..."), it will
  *receive the message once, with the subject of the message set to "blah".
  *</ul><p>
**/
public class SubscriptionLists {

	/**
	  *Pointer to start of linked list of wildcard namespaces.
	**/
	protected Node first;

	/**
	  *Maps objects that wish to receive messages to a wrapper object
 	  *containing helper methods.
	**/
	protected Hashtable receiver_wrappers;

	/**
	  *Used for tracking whether a particular message has been received by
	  *a particular namespace yet.  Represents the current bit in a flag
	  *being looked at.
	**/
	protected int bit;

	/**
	  *Maps namespaces to wrapped receivers.
	**/
	protected Hashtable node_table;

	/**
	  *Maximum size of thread pool to use for distribution.
	**/
	protected int max_thread = 8;

	/**
	  *Number of threads currently being used.
	**/
	protected int cur_threads = 0;

	/**
	  *Contains all threads currently in the pool; i.e., all unused threads.
	**/
	protected LinkedList threads;

	/**
	  *The threads of this SubjectLists will continue handling messages
	  *while this value is true.
	**/
	protected boolean alive = true;

	/**
	  *Receiver wrappers that have a message and therefore need their flags reset
	**/
	protected LinkedList resetters;

	/**
	  *The object responsible for matching wildcarded namespaces.
	**/
	protected SubscriptionMatcher matcher;

	/**
	  *Tracks how many messages have been distributed.
	**/
	protected int msg_count;


	/**
	  *Creates a new SubjectLists using the specified matching class.  SubjectLists
	  *on the server and client side should use the same matching class.
	  *@param matcher  the object to use for matching
	**/
	public SubscriptionLists (SubscriptionMatcher matcher) {
		this.matcher = matcher;
		receiver_wrappers = new Hashtable ();
		threads = new LinkedList ();
		resetters = new LinkedList ();
		node_table = new Hashtable ();
	}//constructor

	/**
	  *Adds the specified object as a receiver for messages sent to the specified namespace.
	  *@param subject  the namespace to subscribe to
	  *@param interested_party  the receiver to subscribe; this should be an instance of
	  *either EventSSClient or MessageHandler (or their subclasses)
	**/
	public void addSubject (String subject, Object interested_party) {
		System.out.println (new Date () + " add: " + subject + " " + interested_party);
		ReceiverWrapper wrapper = (ReceiverWrapper) receiver_wrappers.get (interested_party);
		if (wrapper == null)
			wrapper = new ReceiverWrapper (interested_party);
		addSubscription (subject, wrapper);
	}

	/**
	  *Stops the specified receiver from getting further messages to the specified namespace.
	  *Note that any other receivers subscribed to this namespace will continue receiving
	  *messages, and the specified receiver will keep receiving messages from any other
	  *namespaces it is subscribed to.
	  *@param subject  the namespace to desubscribe
	  *@param disinterested_party  the receiver to remove; this should be either an
	  *EventSSClient or MessageHandler, or a subclass of one of them
	**/
	public void removeSubject (String subject, Object disinterested_party) {
		System.out.println (new Date () + " remove: " + subject + " " + disinterested_party);
		ReceiverWrapper wrapper = (ReceiverWrapper) receiver_wrappers.get (disinterested_party);
		if (wrapper == null)
			return;
		removeSubscription (subject, wrapper);
	}//method removeSubject

	/**
	  *Stops the specified receiver from receiving any further messages and removes it
	  *from all data structures.
	  *@param disinterested_party  the receiver to remove
	**/
	public void removeParty (Object disinterested_party) {
		synchronized (receiver_wrappers) {
			ReceiverWrapper wrapper = (ReceiverWrapper) receiver_wrappers.get (disinterested_party);
			if (wrapper == null)
				return;
			wrapper.receiver = null;
		}
	}//method removeParty

	/**
	  *Will stop all distribution of messages.
	**/
	public void killThreads () {
		alive = false;
	}//method killThreads

	/**
	  *Adds the receiver wrapper for messages sent to the specified namespace to
	  *the proper data structures in SubjectLists
	  *@param subject  the namespace to subscribe to
	  *@param receiver  the receiver wrapper to subscribe
	**/
	protected void addSubscription (String subject, ReceiverWrapper receiver) {
		synchronized (receiver_wrappers) {
			subject = matcher.makeSubjectAbsolute (subject);
			if (matcher.hasWildcards (subject)) {
				Node current = first;
				while (current != null) {
					if (subject.equals (current.subject)) {
						current.receivers.add (receiver);
						return;
					}
					current = current.next;
				}
				current = new Node (subject);
				current.receivers.add (receiver);
				current.next = first;
				first = current;
			}
			else {
				Node node = (Node) node_table.get (subject);
				if (node == null) {
					node = new Node (subject);
					node_table.put (subject, node);
				}
				node.receivers.add (receiver);
			}
		}
	}

	/**
	  *Stops the specified receiver from getting further messages to the specified namespace
	  *and removes it from SubjectLists
	  *@param subject  the namespace to desubscribe
	  *@param receiver  the receiver wrapper to remove
	**/
	private void removeSubscription (String subject, ReceiverWrapper receiver) {
		synchronized (receiver_wrappers) {
			subject = matcher.makeSubjectAbsolute (subject);
			if (matcher.hasWildcards (subject)) {
				Node previous = null;
				Node current = first;
				while (current != null) {
					if (subject.equals (current.subject)) {
						current.receivers.remove (receiver);
						if (current.receivers.size () == 0) {
							if (previous == null)
								first = first.next;
							else
								previous.next = current.next;
						}
						return;
					}
					previous = current;
					current = current.next;
				}
			}
			else {
				Node node = (Node) node_table.get (subject);
				if (node != null) {
					node.receivers.remove (receiver);
					if (node.receivers.size () == 0) {
						node_table.remove (subject);
					}
				}
			}
		}
	}//method removeSubscription

	/**
	  *Distributes a message to all parties interested.
	  *@param msg  the message to distribute
	**/
	public void distribute (Message msg) {
		System.out.println (new Date () + " message dest = " + msg.getDest ());
		synchronized (receiver_wrappers) {
			msg_count++;
			if ((msg_count & 0xfff) == 0)
				System.out.println ("messages handled: " + msg_count);
			String subject = msg.getDest ();
			subject = matcher.makeSubjectAbsolute (subject);
			boolean wildcards = matcher.hasWildcards (subject);
			Node current = first;
			while (current != null) {
				if (matcher.matchSubjects (subject, current.subject))
					current.send (msg);
				current = current.next;
			}
			if (!wildcards) {
				Node node = (Node) node_table.get (subject);
				if (node != null) {
					node.send (msg);
				}
			}
			else {
				Iterator itty = node_table.values ().iterator ();
				while (itty.hasNext ()) {
					Node temp = (Node) itty.next ();
					if (matcher.matchSubjects (subject, temp.subject)) {
						temp.send (msg);
					}
				}
			}
			bit = (bit + 1) & 7;
			while (resetters.size () > 0)
				((ReceiverWrapper) resetters.remove (0)).flag = 0;
		}
	}//method distribute

	/**
	  *Parses the socket representation of a message, and takes appropriate
	  *action.  This includes internal messages, such as additions and removals
	  *of subscriptions.
	  *@param message  the socket representation of the message
	  *@param receiver  the client from which the message came
	**/
	public synchronized void handleSocketString (String message, Object receiver) {
		if (message.startsWith (GlobalInit.TWO)) {
			if (message.charAt (2) == 'a')
				addSubject (message.substring (6), receiver);
			else
				removeSubject (message.substring (9), receiver);
		}
		else {
			Iterator itty = createMessages (message);
			while (itty.hasNext ())
				distribute ((Message) itty.next ());
		}
	}//method handleSocketString


	/**
	  *Creates messages from a socket representation of a string.  Override this
	  *method if you wish to extend the message class so that it returns messages
	  *of your extended class.
	  *@param message  the socket representation
	  *@return Iterator  iterator containing messages
	**/
	public Iterator createMessages (String message) {
		return Message.createMessages (message, matcher);
	}//method createMessages


	/**
	  *Wraps an object (either an EventSSClient or a MessageHandler) interested in
	  *receiving messages.
	**/
	protected class ReceiverWrapper {

		private byte flag;
		private Object receiver;

		/**
		  *Creates a wrapper for the specified receiver, and adds it
		  *to the hashtable mapping receivers to wrappers.
		  *@param receiver  an object interested in receiving messages.
		**/
		protected ReceiverWrapper (Object receiver) {
			this.receiver = receiver;
			receiver_wrappers.put (receiver, this);
		}//constructor

		/**
		  *Passes a message to the wrapped object to be handled; if the object is
		  *a MessageHandler, it ensures that the MessageHandler's process gets called
		  *in a pooled thread so as not to block the receiving thread.
		  *@param msg  the message to pass along
		**/
		protected void send (Message msg) {
			try {
				if (receiver instanceof MessageHandler) {
					SendThread thread = null;
					synchronized (threads) {
						if (threads.size () == 0) {
							if (cur_threads == max_thread) {
								threads.wait ();
								thread = (SendThread) threads.remove (0);
							}
							else
								thread = new SendThread ();
						}
						else
							thread = (SendThread) threads.remove (0);
					}
					thread.setMessage (msg, (MessageHandler) receiver);
				}
				else {
					synchronized (receiver) {
						((ConnectedNodeManager) receiver).send (msg.socket);
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace ();
			}
		}//method send

	}//class ReceiverWrapper

	/**
	  *Wraps a namespace and its list of receivers.
	**/
	protected class Node {

		private String subject;
		private LinkedList receivers;
		private Node next;

		/**
		  *Creates a new node wrapping the specified namespace with an
		  *initially empty list of receivers.
		  *@param subject  the namespace to wrap
		**/
		protected Node (String subject) {
			this.subject = subject;
			receivers = new LinkedList ();
		}//constructor

		/**
		  *Distributes a message to all parties subscribed to the namespace
		  *wrapped by this Node.
		  *@param msg  the message to distribute
		**/
		protected void send (Message msg) {
			for (ListIterator iterator = receivers.listIterator (0); iterator.hasNext ();) {
				ReceiverWrapper wrapper = (ReceiverWrapper) iterator.next ();
				if (wrapper.receiver == null)
					iterator.remove ();
				else if (((wrapper.flag >> bit) & 1) == 0) {
					wrapper.send (msg);
					wrapper.flag = (byte) (1 << bit);
					resetters.add (wrapper);
				}
			}
			if (receivers.size () == 0) {
				removeSubscription (subject, null);
			}
		}//method send

		/**
		  *Returns a pretty string representation of this node
		  *@return String  a string containing the namespace and list of receivers
		**/
		public String toString () {
			return "subject: " + subject + "  receivers: " + receivers;
		}//method toString

	}//class Node


	/**
	  *Pooled thread class used for passing messages to MessageHandlers.
	**/
	protected class SendThread extends Thread {

		private Message message;
		private MessageHandler receiver;

		/**
		  *Creates and starts a new thread.
		**/
		public SendThread () {
			super ();
			cur_threads++;
			start ();
		}//constructor


		/**
		  *Shouldn't be called manually; waits until there's a message to handle,
		  *passes it along to the MessageHandler it goes to, and waits some more.
		**/
		public void run () {
			while (alive) {
				try {
					synchronized (this) {
						if (message == null)
							wait ();
					}
					receiver.process (message);
				}
				catch (Exception e) {
					e.printStackTrace ();
				}
				message = null;
				synchronized (threads) {
					threads.add (this);
					threads.notify ();
				}
			}
		}//method run

		/**
		  *Sets the next message to handle, and the MessageHandler that should
		  *receive it.  Alerts itself (the thread) that a message should be processed.
		  *@param message  the message to handle
		  *@param receiver  the MessageHandler that will process it
		**/
		public void setMessage (Message message, MessageHandler receiver) {
			this.receiver = receiver;
			this.message = message;
			synchronized (this) {
				notify ();
			}
		}//method setMessage

	}//class SendThread


}//class SubjectLists