package org.gel.air.ja.stash.events;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
  *abstract class for registering handler based on group being subscribed to
**/
//TODO move gui stuff to another class if useful or remove
//to keep different for lockheed reasons
public abstract class AbstractStashEventManager implements ActionListener {


	private JDialog messenger;
	private JTextArea message;




	public static AbstractStashEventManager createEvents (String host)  throws Exception {
		AbstractStashEventManager ev = null;
		int port = 0;
		ev = new RemoteStashEventManager (null);
		ev.init (host);
		ev.makeGUI ();
		return ev;
	}


	
	protected void makeGUI () {
		messenger = new JDialog ((JFrame) null, "Data Update");
		message = new JTextArea ();
		message.setBorder (null);
		JLabel colah = new JLabel ();
		message.setBackground (colah.getBackground ());
		message.setForeground (colah.getForeground ());
		message.setEditable (false);
		message.setLineWrap (true);
		message.setWrapStyleWord (true);
		messenger.getContentPane ().add (new JScrollPane (message));
		JPanel temp = new JPanel (new FlowLayout (FlowLayout.CENTER));
		JButton ok = new JButton ("OK");
		ok.addActionListener (this);
		temp.add (ok);
		messenger.getContentPane ().add (temp, BorderLayout.SOUTH);
		messenger.setSize (300, 200);
	}//makeGUI



	protected abstract void init (String host) throws Exception;


	/**
	  *Sets object to alert when message is received
	**/
	public abstract void setDefaultCallback (Object cb); 

	/**
	  *Returns the object alerted for a message that
	  *doesn't have a specific callback object registered
	  *@return  
	**/
	public abstract Object getDefaultCallback ();

	public void add (String group_name) {
		addDefault (group_name);
	}//method add

	public void remove (String group) {
		removeDefault (group);
	}//method remove

	public abstract void addDefault (String group_name);
	public abstract void removeDefault (String group_name);

	public abstract void remove (String group, Object cb);


	public abstract void add (String group, final Object cb);


	
	/**
	  *allows a message to be sent to the event server
	  *@param message The message to send
	**/
	public abstract void send (Object message);

	public abstract void sendString (String msg, String dest);

	public abstract Object getReply (Object msg, String listen_to);

	/**
	  *shows a message dialog alerting users that data has changed.
	  *@param blocked The container the modal dialog should block.
	  *@param message  The message that should be displayed.
	**/
	public void showUpdateDialog (String text) {
		if (messenger.isVisible ()) {
			message.append ("\n" + text);
			messenger.toFront ();
		}
		else {
			message.setText (text);
			messenger.setVisible (true);
		}
	}//method showUpdateDialog


	public void actionPerformed (ActionEvent e) {
		messenger.setVisible (false);
	}//method actionPerformed


}//class Events

