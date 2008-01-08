package org.gel.air.ja.stash;

import java.util.*;

import org.gel.air.ja.stash.events.*;


/**
 *  A Vector that can notify a listener when certain things happen.
 *  Currently, only the addElement, removeElement, removeElementAt and removeAllElements
 *  methods notify listeners.
**/

public class StashList extends Vector <Stash> implements StashConstants, 
StashChangeListener {

	private Vector listeners;
	private boolean enabled = true;

	/**
	 *  Create a new, empty vector.
	**/

	public StashList () {
		super ();
		listeners = new Vector ();
	}//constructor OpenVector

	/**
	 *  Add a vector listener.
	 *
	 *  @VectorListener what          the vector listener to add.
	**/

	public void addListener (StashListListener what) {
		listeners.add (what);
	}//method addVectorListener

	public void removeListener (StashListListener what) {
		listeners.remove (what);
	}//method addVectorListener


	// The rest of these methods call their corresponding methods in Vector,
	// and then notify their listeners.

	public void addElement (Stash what) {
		super.addElement (what);
		if (enabled) {
			int size = listeners.size ();
			for (int i = 0; i < size; i++)
				((StashListListener) listeners.elementAt (i)).elementAdded (this, what);
		}
	}//method addElement

	public boolean removeElement (Object what) {
		boolean found = false;
		super.removeElement (what);
		if (found) {
			int size = listeners.size ();
			if (enabled) {
				for (int i = 0; i < size; i++)
					((StashListListener) listeners.elementAt (i)).elementRemoved (this, what);
				return (true);
			}
		}
		return false;
	}//method removeElement

	public void removeElementAt (int index) {
		Object foo = elementAt (index);
		if (foo != null) {
			super.removeElementAt (index);
			if (enabled) {
				int size = listeners.size ();
				for (int i = 0; i < size; i++)
					((StashListListener) listeners.elementAt (i)).elementRemoved (this, foo);
			}
		}
	}//method removeElementAt

	/*public void setElementAt (Object what, int index) {
		Object foo = elementAt (index);
		if (foo != null) {
			super.setElementAt (what, index);
			if (enabled) {
				int size = listeners.size ();
				for (int i = 0; i < size; i++)
					((VectorListener) listeners.elementAt (i)).elementChangedAt (this, what, index);
			}
		}
	}*///method setElementAt



	public void setEnabled (boolean state) {
		enabled = state;
	}//method setEnabled

	public void dataChanged (Stash changes) {
		for (Iterator itty = changes.keySet ().iterator ();
				itty.hasNext ();) {
			String key = (String) itty.next ();
			if (key.indexOf ('/') < 0) {
				Object value = changes.get (key);
				if (REMOVE_VALUE.equals (value)) {
					for (int i = 0; i < size (); i++) {
						if (get (i).get (ID).equals (key)) {
							removeElementAt (i);
							break;
						}
					}
				}
				else
					addElement ((Stash) value);
			}
		}
	}//method dataChanged

}//class OpenVector