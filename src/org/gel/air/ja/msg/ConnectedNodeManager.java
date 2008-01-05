package org.gel.air.ja.msg;

import java.net.Socket;
import java.io.*;

import org.gel.air.ja.msg.*;
import org.gel.air.ja.msg.hub.*;

/**
  *This class is instantiated for each client side
  *connections
**/
public class ConnectedNodeManager extends ConnectedNode {

	/**
	  *Reference to server's message distributor.
	**/
	protected SubscriptionLists lists;

	/**
	  *Creates a new server-side handler for a client connection and
	  *begins processing messages.
	  *@param sock  the socket used to communicate with the client machine
	  *@param lists  reference to server's message distributor.
	**/
	public ConnectedNodeManager (Socket sock, SubscriptionLists lists) throws Exception {
		super ();
		this.lists = lists;
		this.sock = sock;
		try {
			in = new BufferedReader (new InputStreamReader (sock.getInputStream ()));
			out = new PrintStream (sock.getOutputStream ());
			sock.setSoTimeout (30000);
			if (!in.readLine ().equals (GlobalInit.CONNECT_STRING)) {
				out.println ("connections of this type not accepted");
				sock.close ();
				throw new Exception ("not a real connection");
			}
			sock.setSoTimeout (0);
			out.println ("connection accepted");
			String foo = null;
			while (!(foo = in.readLine ()).equals (GlobalInit.DONE))
				lists.addSubject (foo, this);
			start ();
		} catch (IOException e) {
			e.printStackTrace ();
		}
	}//constructor

	/**
	  *Reads messages from the socket and passes them into the distribution mechanism.
	**/
	public void run () {
		while (GlobalInit.run) {
			try {
				current_msg = in.readLine ();
				lists.handleSocketString (current_msg, this);
			} catch (Exception e) {
				lists.removeParty (this);
				break;
			}
		}
	}//method run

	/**
	  *Writes a string to the socket.
	  *@param msg  the string to write
	**/
	public void send (String msg) {
		try {
			out.println (msg);
		}
		catch (Exception e) {
			e.printStackTrace ();
			lists.removeParty (this);
		}
	}//method send

}//class EventSSClient