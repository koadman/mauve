package org.gel.mauve.ext.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.RangeLocation;
import org.gel.air.ja.msg.AbstractMessageManager;
import org.gel.air.ja.msg.Message;
import org.gel.air.ja.msg.RemoteMessageManager;
import org.gel.air.ja.stash.StashConstants;
import org.gel.air.util.SystemUtils;

public class LoadManager implements StashConstants {

	protected Hashtable <String, Location> loaded;
	protected RemoteMessageManager events;
	protected String root_dir;
	
	public LoadManager (RemoteMessageManager ev, String root) {
		loaded = new Hashtable ();
		events = ev;
		root_dir = root;
	}
	
	public void loadFileSection (String file, int start, int end) {
		if (!loaded.containsKey(file)) {
			loaded.put(file, RangeLocation.empty);
			String respond = SystemUtils.makeUniqueString ();
			Message send = new Message (respond, 
					GET_FILE_LENGTH_NS + file);
			Message ret = (Message) events.getReply(send, respond);
			send = new Message (respond, GET_FILE_NS + 0 + " " + (Long.parseLong(
					ret.getMessage()) - 1) + " " + file);
			ret = (Message) events.getReply (send, respond);
			try {
				BufferedOutputStream out = new BufferedOutputStream (new FileOutputStream (
						new File (root_dir, file)));
				out.write(ret.getMessage().getBytes());
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
}
