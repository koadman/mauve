package org.gel.mauve.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteControl extends Remote {
	public void setFocus (String alignID, String sequenceID, long start,
			long end, String auth_token, String contig) throws RemoteException;
}
