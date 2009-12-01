package org.gel.mauve.remote;

import org.freedesktop.dbus.DBusConnection;
import org.gel.mauve.XmfaViewerModel;

public class MauveDisplayCommunicator {
    DBusConnection bus = null;
    public MauveDisplayCommunicator(XmfaViewerModel model) throws org.freedesktop.dbus.exceptions.DBusException {
    	
        bus = DBusConnection.getConnection(DBusConnection.SESSION);
        bus.requestBusName("org.gel.mauve.remote.MauveInterface");
        bus.exportObject("/MauveInterface", new MauveInterfaceImpl(model));
    }

}
