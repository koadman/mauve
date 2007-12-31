package org.gel.mauve.remote;

import java.applet.Applet;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class RemoteApplet extends Applet {
	public final static int REMOTE_PORT = 32179;

	RemoteControl obj = null;

	private void waitForObj (final String alignmentID, final String sequenceID,
			final long start, final long end, final String auth_token, final String contig) {
		Thread t = new Thread (new Runnable () {

			public void run () {
				int counter = 0;
				while (true) {
					try {
						Thread.sleep (1000);
						counter++;
						if (counter == 600) {
							System.err.println ("Giving up after ten minutes.");
							break;
						}

						try {
							initObj ();
							break;
						} catch (RemoteException e2) {
							// Ignored, we're waiting for the remote server to
							// become available.
						} catch (NotBoundException e2) {
							// Ignored, we're waiting for the remote server to
							// become available.
						} catch (Exception e2) {
							e2.printStackTrace ();
							return;
						}
					} catch (InterruptedException e1) {
						e1.printStackTrace ();
						return;
					}
				}

				if (obj != null) {
					Object retval = AccessController
							.doPrivileged (new PrivilegedAction () {

								public Object run () {
									try {
										obj.setFocus (alignmentID, sequenceID,
												start, end, auth_token, contig);
									} catch (RemoteException e) {
										return e;
									}
									return null;
								}
							});

					if (retval != null) {
						((RemoteException) retval).printStackTrace ();
					}

					repaint ();
				}

			}
		});
		t.start ();
	}
	
	public void goTo (final String alignmentID, final String sequenceID,
			final long start, final long end, final String auth_token) {
		goTo (alignmentID, sequenceID, start, end, auth_token, null);
	}

	public void goTo (final String alignmentID, final String sequenceID,
			final long start, final long end, final String auth_token, final String contig) {
		if (obj == null) {
			try {
				initObj ();
				Object retval = AccessController
						.doPrivileged (new PrivilegedAction () {

							public Object run () {
								try {
									obj.setFocus (alignmentID, sequenceID,
											start, end, auth_token, contig);
								} catch (RemoteException e) {
									return e;
								}
								return null;
							}
						});

				if (retval != null) {
					startApplication ();
					waitForObj (alignmentID, sequenceID, start, end, auth_token, contig);
				}
			} catch (AccessException e) {
				e.printStackTrace ();
			} catch (RemoteException e) {
				startApplication ();
				waitForObj (alignmentID, sequenceID, start, end, auth_token, contig);
			} catch (NotBoundException e) {
				startApplication ();
				waitForObj (alignmentID, sequenceID, start, end, auth_token, contig);
			}
		} else {
			Object retval = AccessController
					.doPrivileged (new PrivilegedAction () {

						public Object run () {
							try {
								obj.setFocus (alignmentID, sequenceID, start,
										end, auth_token, contig);
							} catch (RemoteException e) {
								return e;
							}
							return null;
						}
					});

			if (retval != null) {
				startApplication ();
				waitForObj (alignmentID, sequenceID, start, end, auth_token, contig);
			}
		}
	}

	private void initObj () throws AccessException, RemoteException,
			NotBoundException {
		Object retval = AccessController.doPrivileged (new PrivilegedAction () {
			public Object run () {
				Registry r;
				try {
					r = LocateRegistry.getRegistry (REMOTE_PORT);
					return r.lookup ("MauveRemote");
				} catch (AccessException e) {
					return e;
				} catch (RemoteException e) {
					return e;
				} catch (NotBoundException e) {
					return e;
				}
			}
		});

		if (retval instanceof AccessException)
			throw (AccessException) retval;
		if (retval instanceof RemoteException)
			throw (RemoteException) retval;
		if (retval instanceof NotBoundException)
			throw (NotBoundException) retval;
		if (retval instanceof RemoteControl)
			obj = (RemoteControl) retval;
	}

	private void startApplication () {
		Thread t = new Thread (new Runnable () {
			public void run () {
				try {
					// Try to JWS it.
					getAppletContext ().showDocument (
							new URL (getCodeBase () + "/mauve.jnlp"));
				} catch (MalformedURLException e1) {
					e1.printStackTrace ();
					return;
				}
			}
		});
		t.start ();
	}
}
