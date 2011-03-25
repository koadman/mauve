package org.gel.mauve.remote;

import org.freedesktop.dbus.DBusInterface;

public interface MauveInterface extends DBusInterface {
	public void setGenomeOrder(int[] order);
	public void hackOrder();
	public void setDisplayCoordinate(int genome, long coordinate);
	public void setDisplayRange(int genome, long left, long right);
	public void setDisplayBlockAndColumn(int block, long left_column, long right_column);
	// returns block ID and column coordinate where the mouse is located
	public int getMouseBlock();
	public long getMouseColumn();
}
