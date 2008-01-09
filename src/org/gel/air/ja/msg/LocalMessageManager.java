package org.gel.air.ja.msg;

import org.gel.air.ja.msg.*;

public class LocalMessageManager extends RemoteMessageManager {

	public LocalMessageManager () {
		super (null);
	}


	public void init (String host) {
		if (event_client == null)
			event_client = new LocalSubscriptionManager ();
	}


}