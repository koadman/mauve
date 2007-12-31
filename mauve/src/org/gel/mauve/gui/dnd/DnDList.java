package org.gel.mauve.gui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import org.gel.mauve.MyConsole;

/**
 * An extension of {@link JList}that supports drag and drop to rearrange its
 * contents and to move objects in and out of the list. The objects in the list
 * will be passed either as a String by calling the object's <tt>toString()</tt>
 * object, or if your drag and drop target accepts the
 * {@link TransferableObject#DATA_FLAVOR}data flavor then the actual object
 * will be passed.
 * 
 * <p>
 * I'm releasing this code into the Public Domain. Enjoy.
 * </p>
 * <p>
 * <em>Original author: Robert Harder, rharder@usa.net</em>
 * </p>
 * 
 * @author Robert Harder
 * @author rharder@usa.net
 * @version 1.1
 */
public class DnDList extends JList implements DropTargetListener,
		DragSourceListener, DragGestureListener {

	private DropTarget dropTarget = null;

	private DragSource dragSource = null;

	private int sourceIndex = -1;

	private int dropIndex = -1;

	private Object sourceObject;

	/**
	 * Constructs a default {@link DnDList}using a {@link DefaultListModel}.
	 * 
	 * @since 1.1
	 */
	public DnDList () {
		super (new DefaultListModel ());
		initComponents ();
	} // end constructor

	/**
	 * Constructs a {@link DnDList}using the passed list model that must be
	 * extended from {@link DefaultListModel}.
	 * 
	 * @param model
	 *            The model to use
	 * @since 1.1
	 */
	public DnDList (DefaultListModel model) {
		super (model);
		initComponents ();
	} // end constructor

	/**
	 * Constructs a {@link DnDList}by filling in a {@link DefaultListModel}
	 * with the passed array of objects.
	 * 
	 * @param data
	 *            The data from which to construct a list
	 * @since 1.1
	 */
	public DnDList (Object [] data) {
		this ();
		((DefaultListModel) getModel ()).copyInto (data);
	} // end constructor

	/**
	 * Constructs a {@link DnDList}by filling in a {@link DefaultListModel}
	 * with the passed {@link Vector}of objects.
	 * 
	 * @param data
	 *            The data from which to construct a list
	 * @since 1.1
	 */
	public DnDList (Vector data) {
		this ();
		((DefaultListModel) getModel ()).copyInto (data.toArray ());
	} // end constructor

	private void initComponents () {
		dropTarget = new DropTarget (this, this);
		dragSource = new DragSource ();
		dragSource.createDefaultDragGestureRecognizer (this,
				DnDConstants.ACTION_MOVE, this);
	} // end initComponents
	
	public void setDropActive (boolean active) {
		dropTarget.setActive(active);
	}

	/* ******** D R A G G E S T U R E L I S T E N E R M E T H O D S ******** */

	public void dragGestureRecognized (DragGestureEvent event) {
		final Object selected = getSelectedValue ();
		if (selected != null) {
			sourceIndex = getSelectedIndex ();
			Transferable transfer = new TransferableObject (
					new TransferableObject.Fetcher () {
						/**
						 * This will be called when the transfer data is
						 * requested at the very end. At this point we can
						 * remove the object from its original place in the
						 * list.
						 */
						public Object getObject () {
							((DefaultListModel) getModel ())
									.remove (sourceIndex);
							return selected;
						} // end getObject
					}); // end fetcher

			// as the name suggests, starts the dragging
			dragSource.startDrag (event, DragSource.DefaultLinkDrop, transfer,
					this);
		} else {
			// System.out.println( "nothing was selected");
		}
	} // end dragGestureRecognized

	/* ******** D R A G S O U R C E L I S T E N E R M E T H O D S ******** */

	public void dragDropEnd (DragSourceDropEvent evt) {
	}

	public void dragEnter (DragSourceDragEvent evt) {
	}

	public void dragExit (DragSourceEvent evt) {
	}

	public void dragOver (DragSourceDragEvent evt) {
	}

	public void dropActionChanged (DragSourceDragEvent evt) {
	}

	/* ******** D R O P T A R G E T L I S T E N E R M E T H O D S ******** */

	public void dragEnter (DropTargetDragEvent evt) {
		evt.acceptDrag (DnDConstants.ACTION_MOVE);
	}

	public void dragExit (DropTargetEvent evt) {
	}

	public void dragOver (DropTargetDragEvent evt) {
	}

	public void dropActionChanged (DropTargetDragEvent evt) {
		evt.acceptDrag (DnDConstants.ACTION_MOVE);
	}

	public void drop (DropTargetDropEvent evt) {
		Transferable t = evt.getTransferable ();

		if (t.isDataFlavorSupported (DataFlavor.javaFileListFlavor)) {
			evt.acceptDrop (DnDConstants.ACTION_MOVE);
			Object obj;
			try {
				obj = t.getTransferData (DataFlavor.javaFileListFlavor);
			} catch (UnsupportedFlavorException e) {
				// Unexpected, because we just checked whether it is supported.
				throw new RuntimeException (e);
			} catch (IOException e) {
				MyConsole.err ().println (
						"Drag and drop data no longer available");
				e.printStackTrace (MyConsole.err ());
				evt.rejectDrop ();
				return;
			}

			List listobj = (List) obj;

			// See where in the list we dropped the element.
			int dropIndex = locationToIndex (evt.getLocation ());
			DefaultListModel model = (DefaultListModel) getModel ();

			Iterator iterator = listobj.iterator ();
			if (dropIndex < 0) {
				while (iterator.hasNext ()) {
					model.addElement (((File) iterator.next ()).getPath ());
				}
			}
			// Else is it moving down the list?
			else if (sourceIndex >= 0 && dropIndex > sourceIndex) {
				while (iterator.hasNext ()) {
					model.add (dropIndex - 1, ((File) iterator.next ())
							.getPath ());
				}
			} else {
				while (iterator.hasNext ()) {
					model.add (dropIndex, ((File) iterator.next ()).getPath ());
				}
			}
			evt.getDropTargetContext ().dropComplete (true);
		} else if (t.isDataFlavorSupported (DataFlavor.stringFlavor)) {
			evt.acceptDrop (DnDConstants.ACTION_MOVE);
			Object obj;
			try {
				obj = t.getTransferData (DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException e) {
				// Unexpected, because we just checked whether it is supported.
				throw new RuntimeException (e);
			} catch (IOException e) {
				MyConsole.err ().println (
						"Drag and drop data no longer available");
				e.printStackTrace (MyConsole.err ());
				evt.rejectDrop ();
				return;
			}

			// See where in the list we dropped the element.
			int dropIndex = locationToIndex (evt.getLocation ());
			DefaultListModel model = (DefaultListModel) getModel ();

			if (dropIndex < 0) {
				model.addElement (obj);
			}
			// Else is it moving down the list?
			else if (sourceIndex >= 0 && dropIndex > sourceIndex) {
				model.add (dropIndex - 1, obj);
			} else {
				model.add (dropIndex, obj);
			}

			evt.getDropTargetContext ().dropComplete (true);
		} else
		// Else we can't handle this
		{
			evt.rejectDrop ();
		}
	}
}