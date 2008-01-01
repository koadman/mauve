package org.gel.air.ja.msg;

import java.io.*;

public class LocalSubscriptionManager extends SubscriptionManager {

	public LocalSubscriptionManager () {
		try {
			lists = new SubscriptionLists (getMatcher ());
			PipedInputStream p_in = new PipedInputStream ();
			in = new BufferedReader (new InputStreamReader (p_in));
			out = new PrintStream (new PipedOutputStream (p_in));
			start ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}//constructor

}//class SameJVMRouter