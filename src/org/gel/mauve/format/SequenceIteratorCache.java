package org.gel.mauve.format;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.Timer;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;

/**
 * Acts as a cache to allow for faster access to files with multiple contigs without
 * creating a permanent memory commitment.  Necessary due to biojava's interaction with
 * the BaseFormat class.
 * 
 * @author Anna I Rissman
 *
 */
public class SequenceIteratorCache {
	
	/**
	 * contains file names mapped to arrays of data required for cache
	 */
	protected static Hashtable cache = new Hashtable ();
	
	/**
	 * Integers that represent array indexes of different required information.
	 * Useful for accessing info in the arrays returned from the cache.
	 */
	public final static int ITERATOR_INDEX = 0;
	public final static int INDEX_INDEX = 1;
	
	/**
	 * Represents how much time will pass between attempts to clean up cache
	 */
	public final static int CLEAN_DELAY = 120000;
	/**
	 * Allows for clean up when a sequence iterator has no more sequences
	 *  left to iterate through
	 */
	public final static Timer CLEAN_UP_TIMER = new Timer (CLEAN_DELAY, null);
	
	/**
	 * Initializes the clean up timer by adding a listener and starting the timer
	 */
	static {
		CLEAN_UP_TIMER.addActionListener (new ActionListener () {
			public void actionPerformed (ActionEvent e) {
				cleanCache ();
			}
		});
		CLEAN_UP_TIMER.start();
	}
	
	/**
	 * Returns an iterator already iterated to the appropriate index.
	 * 
	 * @param format	The format the file to parse is in
	 * @param source	The file containing the information from which to
	 * 					 construct the iterator
	 * @param index		The index of the desired sequence.
	 * @return			A SequenceIterator that will give the desired Sequence with one
	 * 					 nextSequence () call.
	 */
	public static Sequence getSequence (BaseFormat format, final File source, int index) {
		Object [] array = null;
		if (cache.containsKey (source)) {
			array = (Object []) cache.get (source);
			if (((Integer) array [INDEX_INDEX]).intValue () > index)
				array = null;
		}
		if (array == null) {
			array = new Object [2];
			array [ITERATOR_INDEX] = format.readFile(source);
			array [INDEX_INDEX] = new Integer (0);
			cache.put (source, array);
		}
		try {
			for (int i = ((Integer) array [INDEX_INDEX]).intValue (); i < index; i++)
				((SequenceIterator) array [ITERATOR_INDEX]).nextSequence ();
			array [INDEX_INDEX] = new Integer (index + 1);
			return ((SequenceIterator) array [ITERATOR_INDEX]).nextSequence();
		}
		catch (Exception e) {
			e.printStackTrace ();
            throw new Error("Unexpected exception.", e);
		}
	}
	
	/**
	 * Looks through cache hashtable for any iterators that have no more sequences
	 * and therefore are no longer useful and removes them from the cache.
	 */
	public static void cleanCache () {
		synchronized (cache) {
			Iterator keys = cache.keySet ().iterator();
			while (keys.hasNext()) {
				Object key = keys.next();
				if (!((SequenceIterator) ((Object []) cache.get(key))
						[ITERATOR_INDEX]).hasNext()) {
					keys.remove();
					System.out.println ("removed! " + cache.get(key));
				}
			}
		}
	}
	
}