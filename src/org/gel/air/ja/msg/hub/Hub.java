package org.gel.air.ja.msg.hub;

import java.io.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

import org.gel.air.ja.msg.*;

public class Hub extends Thread{

	protected ServerSocket sock;
	protected static int seconds;

	public Hub (int port, int max) {
		try {
			sock = new ServerSocket (port, max);
			start ();
		} catch (Exception e) {
			GlobalInit.error ("Couldn't create socket");
			System.exit (0);
		}
	}//constructor


	public void sendString (String [] subjects, String message) {
		StringBuffer buffy = new StringBuffer (message);
		buffy.insert(0, subjects [0] + GlobalInit.ONE73);
		for (int i = 1; i < subjects.length; i++)
			buffy.insert (0, subjects [i] + GlobalInit.ONE);
		ConnectionThread.runInThread (this, buffy.toString ());
	}

	public void run () {
		while (GlobalInit.run) {
			try {
				Socket one_socket = sock.accept ();
				new ConnectedNode (one_socket);
				
			}
			catch (IOException e) {
				GlobalInit.error ("An I/O error has occurred");
			}
			catch (SecurityException e) {
				GlobalInit.error ("A security error has occurred with this connection");
			}
			catch (Exception e) {
				e.printStackTrace ();
			}
		}
	}//method run


	public void friendlyStop () {
		GlobalInit.run = false;
	}//method stop



	public static void main (String [] args) {
		/*final Hub snervy = new Hub ();
		seconds = 0;
		javax.swing.Timer time = new javax.swing.Timer (30000, new ActionListener () {
			public void actionPerformed (ActionEvent e) {
				seconds += 30;
				System.out.println (seconds + " seconds: " + ConnectionThread.number);
			}
		});
		time.start ();*/
	}//method main

}