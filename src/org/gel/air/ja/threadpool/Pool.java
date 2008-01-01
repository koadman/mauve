package org.gel.air.ja.threadpool;

import java.util.*;
import java.awt.event.*;
import javax.swing.Timer;

/**
  *This class represents a thread pool.  For applications that require an indeterminate number of worker threads,
  *the thread pool can delegate tasks to threads in the pool, encapsulating the creation of threads to ensure that
  *threads are being created and used efficiently.  Threads are told to perform tasks through a request method call
  *rather than in the run method itself.
  *@author anna rissman/james lowden
**/

public class Pool implements ActionListener {

	/**
	  *The threads currently in the pool (not currently performing tasks).
	**/
	protected LinkedList threads;
	/**
	  *Total number of threads currently made.
	**/
	protected int count;
	/**
	  *Maximum number of threads in this pool.
	**/
	protected int max;


	/**
	  *Amount of time a thread can remain unused before being destroyed
	**/
	protected int timeout;

	/**
	  *Tracks idle threads and removes them from the pool if necessary
	**/
	protected Timer idler;

	/**
	  *Default maximum number of threads in pool.
	**/
	public static final int DEFAULT_MAX = 10;


	/**
	  *Creates a thread pool with the maximum number of threads equal to the default maximum.  No threads are created
	  *until they need to be used or the user instructs.
	**/
	public Pool () {
		this (DEFAULT_MAX);
	}//constructor

	/**
	  *Creates a thread pool with the specified maximum number of threads.  No threads are created
	  *until they need to be used or the user instructs.
	  *@param max  the maximum number of threads that should be created.
	**/
	public Pool (int max) {
		this.max = max;
		threads = new LinkedList ();
	}//constructor

	/**
	  *Creates a thread pool with the specified maximum number of threads.  No threads are created
	  *until they need to be used or the user instructs.
	  *@param max  the maximum number of threads that should be created.
	  *@param start the number of threads that should be immediately created.
	**/
	public Pool (int max, int start) {
		this.max = max;
		threads = new LinkedList ();
		createThreads (start);
	}//constructor


	/**
	  *This method allows you to set a maximum number of threads to create.  By default, the max is 10.
	  *@param number The maximum number of threads that should be created.
	**/
	public void setMaxThreads (int number) {
		if (number > 0)
			max = number;
	}


	/**
	  *retrieves the current maximum numbers of threads that will be created.
	  *@return The maximum number of threads that could be in the pool.
	**/
	public int getMaxThreads () {
		return max;
	}


	/**
	  *returns the number of threads currently created in the pool.
	  *@return The number of instantiated threads in the pool.
	**/
	public int getNumThreads () {
		return count;
	}



	/**
	  *creates the specified number of threads unless the maximum size of the pool would be exceeded, in which case,
	  *all threads in the pool will be created.
	  *@param number The number of threads to create.
	**/
	public void createThreads (int number) {
		while (number-- >= 0 && count < max)
			addThread (new PooledThread ());
	}


	/**
	  *creates threads until the maximum allowable number of threads have been created.
	**/
	public void fillPool () {
		createThreads (max);
	}


	/**
	  *sets the amount of time a thread can sit in a pool before it is terminated.  By default, threads are never terminated.
	  *To set it to never terminate threads, call this method with -1.
	  *@param delay Amount of time to let thread sit or -1 to never destroy.
	**/
	public void setIdleTime (int millis) {
		if (millis > 0 || millis == -1) {
			timeout = millis;
			if (millis > 0) {
				if (idler == null)
					idler = new Timer (millis, this);
				else
					idler.setDelay (millis);
				idler.restart ();
			}
			else if (idler != null)
				idler.stop ();
		}
	}


	
	//for use by timer
	/**
	  *handles idle timeouts.  Should not be called by user.
	**/
	public void actionPerformed (ActionEvent e) {
		if (threads.size () > 0) {
			synchronized (threads) {
				if (System.currentTimeMillis () - ((PooledThread) threads.get (0)).add_time > timeout) {
					PooledThread remove = ((PooledThread) threads.remove (0));
					remove.going = false;
					remove.interrupt ();
				}
			}
		}
	}

	/**
	  *gets the amount of time a thread can be inactive before being destroyed. 
	  *@return number of milliseconds a thread can remain idle in the pool. A value of -1 indicates that threads will
	  *     never be destroyed
	**/
	public int getIdleTime () {
		return timeout;
	}

	/**
	  *This method should be called to request that a task be performed by a thread in the pool.
	  *@param task  the task to perform
	**/
	public void performInOtherThread (PooledTask task) {
		try {
			PooledThread thread = null;
			synchronized (threads) {
				if (threads.size () == 0) {
					if (count == max) {
						threads.wait ();
						thread = (PooledThread) threads.remove (0);
					}
					else
						thread = new PooledThread ();
				}
				else
					thread = (PooledThread) threads.remove (0);
			}
			thread.setTask (task);
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}//method performTask


	/**
	  *adds a thread to the pool.
	  *@param thread the thread to add.
	**/
	protected void addThread (PooledThread thread) {
		threads.add (thread);
		thread.add_time = System.currentTimeMillis ();
	}

	/**
	  *This class represents a thread in a thread pool.  It contains an infinite loop that only performs activity
	  *when a request has been made via the thread pool.
	**/

	protected class PooledThread extends Thread {

		/**
		  *The task this thread is currently working on.
		**/
		protected PooledTask task;
		/**
		  *True as long as this thread is alive.
		**/
		protected boolean going;

		/**
		  *Time at which this thread became idle and was re-added to the pool.
		**/
		protected long add_time;



		/**
		  *Creates a new thread to add to the pool, and starts its wait for a task to perform.
		**/
		public PooledThread () {
			going = true;
			count++;
			start ();
		}//constructor

		/**
		  *Waits to be given a task, then performs the task, and re-adds itself to the pool when done.
		**/
		public void run () {
			while (going) {
				try {
					synchronized (this) {
						if (task == null)
							wait ();
					}
					task.performTask ();
					task = null;
					synchronized (threads) {
						addThread (this);
						threads.notify ();
					}
				}
				catch (InterruptedException e) {
					e.printStackTrace ();
				}
			}
		}//method run

		/**
		  *Sets the task that this thread should perform.
		**/
		public void setTask (PooledTask task) throws ThreadPoolException {
			if (this.task == null) {
				this.task = task;
				synchronized (this) {
					notify ();
				}
			}
			else
				throw new ThreadPoolException (ThreadPoolException.BUSY_THREAD);
		}//method setTask

	}//class PooledThread

}//class Pool
			