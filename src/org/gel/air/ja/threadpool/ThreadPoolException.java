package org.gel.air.ja.threadpool;

/**
  *Exception class for thread pool errors.
  *@author anna rissman/james lowden
**/

public class ThreadPoolException extends Exception {

	/**
	  *Error code for trying to assign a task to a currently working thread.
	**/
	public static final int BUSY_THREAD = 0;

	private static final String [] ERRORS = {"Busy Thread; assign your task through the performTask method of the Pool class"};

	/**
	  *The error code for this exception.
	**/
	protected int error_code;


	/**
	  *constructs an exception
	  *@param error_code the type of error to create.
	**/
	public ThreadPoolException (int error_code) {
		super (ERRORS [error_code]);
		this.error_code = error_code;
	}//constructor

	/**
	  *constructs an exception
	  *@param message The text of the error.
	  *@param error_code the type of error to create.
	**/
	public ThreadPoolException (String message, int error_code) {
		super (message);
		this.error_code = error_code;
	}//constructor


	/**
	  *returns the type of error this exception represents.
	  *@return the error code associated with this exception.
	**/
	public int getErrorCode () {
		return error_code;
	}//method getErrorCode

}//class ThreadPoolException