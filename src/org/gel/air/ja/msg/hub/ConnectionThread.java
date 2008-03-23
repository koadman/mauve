package org.gel.air.ja.msg.hub;

import java.util.*;

import org.gel.air.ja.msg.GlobalInit;

public class ConnectionThread extends Thread {

	Object progenitor;
	public static ConnectionThread [] pool;
	static int current = 0;
	String msg;
	static int number;

	public void run () {
			while (true)
				if (progenitor != null) {
					synchronized (progenitor) {
						received ();
					}
					progenitor = null;
				} else
					Thread.currentThread ().yield ();
	}//method run

	private void received () {
		number++;
		if (progenitor instanceof ConnectedNode)
			if (msg.startsWith (GlobalInit.TWO + "add ")) {
				((ConnectedNode) progenitor).addGroup (msg.substring (5));
				return;
			 } else if (msg.startsWith (GlobalInit.TWO + "remove ")) {
				((ConnectedNode) progenitor).removeGroup (msg.substring (8));
				return;
			}

		LinkedList <String> groups = new LinkedList ();
		int i = 0;
		int j = 0;
		int separator = msg.indexOf (GlobalInit.ONE73);
		while (i != -1 && i < separator) {
			i = msg.indexOf (GlobalInit.ONE, j);
			if (i == -1)
				i = separator;
			if (i > 0)
				groups.add (msg.substring (j, i));
			j = i + 1;
		}
		String [] groupz = (String []) groups.toArray ();
		Arrays.sort (groupz);
		int k = 0;
//		System.out.print ("NEW MESSAGE RECEIVED . . . SENDING:  ");
		synchronized (ConnectedNode.crients) {
			Iterator foo = ConnectedNode.crients.iterator ();
			while (foo.hasNext ()) {
				ConnectedNode crient = (ConnectedNode) foo.next ();
				Iterator it = groups.iterator ();
				for (i = 0, j = 0; i < groupz.length && j < crient.group_names.length;) {
					k = crient.group_names [j].compareTo (groupz [i]);
					if (k == 0) {
						synchronized (crient.out) {
							crient.out.println (msg);
						}
//						System.out.println (msg);
						break;
					} else if (k < 0)
						j++;
					else
						i++;
				}
			}
		}
	}//method received

	public static void runInThread (Object progenitor, String msg) {
		while (pool [current].progenitor != null)
			current = (current + 1) % pool.length;
		pool [current].progenitor = progenitor;
		pool [current].msg = msg;
		if (!pool [current].isAlive ())
			pool [current].start ();
	}//method getThread

}//class NetThread