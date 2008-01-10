package org.gel.air.gum;

import org.gel.air.ja.msg.GlobalInit;
import org.gel.air.ja.msg.SubscriptionServer;

public class MessageRouter {

	public static void main (String [] args) {
		new SubscriptionServer (
				GlobalInit.PORT, GlobalInit.MAX_CONNECTIONS);
	}
	
}
