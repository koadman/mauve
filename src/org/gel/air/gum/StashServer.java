package org.gel.air.gum;

import java.io.File;

import org.gel.air.ja.msg.AbstractMessageManager;
import org.gel.air.ja.msg.GlobalInit;
import org.gel.air.ja.stash.StashXMLLoader;
import org.gel.air.ja.stash.events.XMLStashManager;

public class StashServer {

	public static void main (String [] args) throws Exception {
		AbstractMessageManager manager = AbstractMessageManager.createEvents (
				"127.0.0.1", GlobalInit.PORT);
		StashXMLLoader loader = new StashXMLLoader (args [0], manager);
		loader.loadDefaults(new File (args [0], args [1]));
		new XMLStashManager (manager, new File (args [0]), loader);
	}
	
}
