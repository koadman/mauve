package org.gel.air.ja.stash.events;

import org.gel.air.ja.msg.*;

public class LocalStashEventManager extends RemoteStashEventManager {

	public LocalStashEventManager () {
		super (null);
	}//constructor


	public void init (String host) {
		if (event_client == null)
			event_client = new LocalSubscriptionManager ();
	}//method init


}//class SameJVMEvents