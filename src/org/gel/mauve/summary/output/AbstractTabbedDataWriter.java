package org.gel.mauve.summary.output;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Used to provide base functionality to print files largely made up of rows of
 * tab- separated data. Provides loose structure for getting data based on row
 * and column numbers. The row and column information is used only when printing
 * data rows, as done through printDataRow (). All free-form data can be written
 * in between rows of formatted data. Row and column information can be reset at
 * the convenience of the implementing classes
 * 
 * @author Anna I Rissman
 * 
 */
public abstract class AbstractTabbedDataWriter {

	/**
	 * writer used to print
	 */
	protected PrintStream out;

	/**
	 * represents the file name (and path) the output stream writes to
	 */
	String file_name;

	/**
	 * Contains the header for each column. The headers can be printed on demand
	 * and are also used to determine what data from the match to display in
	 * each column. Array indeces match column indeces.
	 */
	protected String [] headers;

	/**
	 * acts as a buffer for a row of information
	 */
	protected String [] current_row;

	/**
	 * Is meant for sub-classes as an identifier to what information should be
	 * printed. Is incremented in printDataRow, but can be reset without harm by
	 * sub-classes.
	 */
	protected int row_number;

	/**
	 * Provides basic structure for outputting data from
	 * 
	 * @param mod
	 *            reference to mauve data
	 * @param file_name
	 *            The name of the file that should be output
	 */
	public AbstractTabbedDataWriter (String file, Hashtable args) {
		try {
			// TODO if was being official, should check if already exists, path
			// valid,
			// etc, and give appropriate messages
			file_name = file;
			out = new PrintStream (new BufferedOutputStream (
					new FileOutputStream (file_name)));
			initSubClassParticulars (args);
			printHeaderInfoForFile ();
		} catch (Exception e) {
			System.out.println ("Can't open file");
			e.printStackTrace ();
		}
	}

	// TODO write a constructor that takes in a bufferedwriter and a file,
	// in case it has already be created by something else.

	/**
	 * Prints a row of data to the output file, each field tab separated
	 * 
	 * @param data
	 *            Array of fields to print. Must be the same length as the
	 *            header array.
	 */
	public void printRow (String [] data) {
		try {
			for (int i = 0; i < data.length - 1; i++) {
				out.print (data[i]);
				out.print ("\t");
			}
			out.println (data[data.length - 1]);
		} catch (Exception e) {
			System.out.println ("couldn't print row: ");
			System.out.println (data);
		}
	}

	/**
	 * Prints the headers
	 */
	public void printHeaders () {
		if (headers != null && headers.length > 0)
			printRow (headers);
	}

	/**
	 * Gathers and prints a row of data
	 * 
	 */
	public void printDataRow () {
		if (shouldPrintRow (row_number)) {
			for (int i = 0; i < headers.length; i++) {
				current_row[i] = getData (i, row_number);
			}
			printRow (current_row);
		}
		row_number++;
	}

	public void printData () {
		do {
			printDataRow ();
		} while (moreRowsToPrint ());
	}

	/**
	 * sets how many columns there are and what their headers should be
	 * 
	 * @param columns
	 *            An array of strings representing column names
	 */
	public void setColumnHeaders (Vector columns) {
		headers = (String []) columns.toArray (new String [columns.size ()]);
		current_row = new String [headers.length];
	}

	public String [] getColumnHeaders () {
		return (String []) headers.clone ();
	}

	public void doneWritingFile () {
		System.out.println ("done: " + file_name);
		out.println ();
		out.flush ();
		out.close ();
	}

	/**
	 * Convenience method for inheriting classes to set variables necessary from
	 * the constructor.
	 * 
	 * @param args
	 *            Contains objects necessary to successfully initialize a
	 *            subclass. NOTE: can be null value
	 */
	protected void initSubClassParticulars (Hashtable args) {
		setColumnHeaders (setColumnHeaders ());
	}

	/**
	 * convenience method if there is data that should automatically be printed
	 * at the beginning of a file
	 * 
	 */
	public void printHeaderInfoForFile () {
	}

	/**
	 * Returns the data that should be printed in the specified row and column.
	 * It is up to sub-classes to track where to get the data from, as the
	 * format is not intended to be standardized.
	 * 
	 * @param column
	 *            The header representing which column this data is for
	 * @param row
	 *            An int that may be useful for identifying what data is
	 *            desired. It gets incremented every time printDataRow () is
	 *            called.
	 */
	abstract protected String getData (int column, int row);

	/**
	 * called from printData. Used to determine when to stop attempting to print
	 * rows.
	 * 
	 * @return True if there is more tabbed data to print, false otherwise
	 */
	abstract protected boolean moreRowsToPrint ();

	/**
	 * Gets the names of the columns
	 * 
	 */
	abstract protected Vector setColumnHeaders ();

	abstract protected boolean shouldPrintRow (int row);

}
