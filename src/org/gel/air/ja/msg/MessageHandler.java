package org.gel.air.ja.msg;



public interface MessageHandler {

	/**
	  *This method is called when a message this message handler is subscribed to
	  *is received.
	  *@param msg  the message to handle
	**/
	public void process (Message msg);

}//interface MessageHandler