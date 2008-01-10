package org.gel.air.ja.msg;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.gel.air.ja.msg.*;

import java.util.Hashtable;


/**
  *can be used to hook up to an RTServer and specify the subject space to subscribe to.
**/

public class RemoteMessageManager extends AbstractMessageManager implements MessageHandler {


	public MessageHandler receiver;
	public static SubscriptionManager event_client;

	//for subclass
	protected RemoteMessageManager (MessageHandler starter) {
		if (starter == null)
			starter = this;
		receiver = starter;
		init (null, 0);

	}
	
	protected RemoteMessageManager () {
		
	}

	public void init (String host, int port) {
		try {
			if (event_client == null)
				event_client = SubscriptionManager.create (host, port);
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}//method init


	/**
	  *Sets the default callback
	  *@param cb  the callback to use as the default
	**/
	public void setDefaultCallback (Object cb) {
		receiver = (MessageHandler) cb;
	}//method setDefaultCallback

	/**
	  *Returns the default callback
	  *@return  the default process callback
	**/
	public Object getDefaultCallback () {
		return receiver;
	}//method getDefaultCallback



	/**
	  *Allows the subject subscribed to to be changed.
	  *@param group_name The new subject to subscribe to.
	**/
	public void addDefault (String group_name) {
		add (group_name, receiver);
	}//method add

	public void removeDefault (String group) {
		remove (group, receiver);
	}//method remove


	public void remove (String group, Object cb) {
		try {
			event_client.removeGroup (group, (MessageHandler) cb);
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}//method remove



	public void add (String group, final Object cb) {
		try {
			event_client.addGroup (group, (MessageHandler) cb);
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}//add		


	
	/**
	  *allows a message to be sent to the event server
	  *@param message The message to send
	**/
	public void send (Object message) {
		event_client.send ((Message) message);
	}//send

	public void sendString (String msg, String dest) {
		send (new Message (msg, dest));
	}//method sendString

	public Object getReply (Object msg, String listen_to) {
		try {
			final Object [] ret_val = new Object [1];
			MessageHandler handler = new MessageHandler () {
				public void process (Message msg) {
					synchronized (ret_val) {
						ret_val [0] = msg;
						ret_val.notify ();
					}
				}
			};
			add (listen_to, handler);
			synchronized (ret_val) {
				send (msg);
				ret_val.wait ();
			}
			remove (listen_to, handler);
			return ret_val [0];
		}
		catch (Exception e) {
			e.printStackTrace ();
			return null;
		}
	}


	/**
	  *a default process routine that prints the contents of a message.
	  *@param message The message received.
	  *@param vars Parameters specific to the message.
	**/
	//I have never had a use for the vars parameter, but it must be there to receive messages
	//properly
	public void process (Message message) {
		try {
			//prints the message.  setCurrent (0) is necessary to get access to the whole message
			message.setCurrent (0);
			System.out.println (message.nextStr () + "\n");
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}//method process



}//class Events

