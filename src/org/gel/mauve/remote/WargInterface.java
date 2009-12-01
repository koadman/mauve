package org.gel.mauve.remote;

import org.freedesktop.dbus.DBusInterface;

public interface WargInterface extends DBusInterface {
	int getViewingSite();
	void setViewingSite(int block, long site);
}
