package org.gel.air.ja.threadpool;

/**
  *This interface encapsulates method calls that should be carried out by pooled threads.
  *@author anna rissman/james lowden
**/


public interface PooledTask {

	/**
	  *This method will be called by threads in the thread pool sometime after a request is made to the
	  *thread pool with this PooledTask object.  This method should be implemented to contain mostly other
	  *method calls rather than detailed code for truly object-oriented design.
	**/
	public void performTask ();

}//interface PooledTask