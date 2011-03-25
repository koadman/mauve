package org.gel.mauve.remote;
import org.freedesktop.dbus.DBusConnection;
import org.gel.mauve.Genome;
import org.gel.mauve.HighlightListener;
import org.gel.mauve.ModelEvent;
import org.gel.mauve.XmfaViewerModel;

public class WargDisplayCommunicator implements HighlightListener {
    DBusConnection bus = null;
    WargInterface warg;
	public WargDisplayCommunicator(XmfaViewerModel model) {
		try{
        bus = DBusConnection.getConnection(DBusConnection.SESSION);
        warg = bus.getRemoteObject("org.gel.mauve.remote.WargInterface", "/weakarg", WargInterface.class);
        model.addHighlightListener(this);
		}catch(org.freedesktop.dbus.exceptions.DBusException dbe){}
//        bus.disconnect();
	}

	public void highlightChanged(ModelEvent me){
		XmfaViewerModel model = (XmfaViewerModel)me.getSource();
		long coord = model.getHighlightCoordinate();
		Genome g = model.getHighlightGenome();
		long[] l = model.getLCBAndColumn(g, coord);
		try{
		warg.setViewingSite((int)l[0],l[1]);
		}catch(Exception e){
			// the connection may have failed/broken/etc.  deregister from listening to save cpu
	        model.removeHighlightListener(this);
		}
	}
}
