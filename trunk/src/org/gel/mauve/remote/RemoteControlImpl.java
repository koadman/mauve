package org.gel.mauve.remote;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.gel.mauve.gui.Mauve;

public class RemoteControlImpl extends UnicastRemoteObject implements
		RemoteControl {
	Mauve app;

	public RemoteControlImpl (Mauve app) throws RemoteException {
		super ();
		this.app = app;
	}

	public void setFocus (String alignID, String sequenceID, long start,
			long end, String auth_token, String contig) throws RemoteException {
		app.setFocus (alignID, sequenceID, start, end, auth_token, contig);
	}

	public static void startRemote (Mauve app) {
		try {
			Registry r = LocateRegistry
					.createRegistry (RemoteApplet.REMOTE_PORT);
			RemoteControlImpl obj = new RemoteControlImpl (app);
			r.rebind ("MauveRemote", obj);

		} catch (Exception e) {
			System.err.println ("Error creating registry and binding object.");
			e.printStackTrace ();
			return;
		}
	}

}
