package org.gel.air.ja.stash.events;

import java.util.Properties;

import org.gel.air.ja.msg.LocalMessageManager;
import org.gel.air.ja.msg.RemoteMessageManager;
import org.gel.air.ja.stash.Stash;
import org.gel.air.util.SystemUtils;

public class StashUpdateEvents extends LocalMessageManager {

	public StashUpdateEvents () {
		super ();
	}

	public void sendChanges (String class_type, String obj_id, Properties changes) {
		sendString (SystemUtils.propsToString (changes), "update/" + class_type + "/" + obj_id);
	}


	public void sendCreate (String class_type, Properties changes) {
		sendString (SystemUtils.propsToString (changes), "create/" + class_type);
	}

}