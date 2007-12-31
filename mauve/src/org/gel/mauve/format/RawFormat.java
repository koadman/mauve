package org.gel.mauve.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.gel.mauve.MauveConstants;

class RawFormat extends FastaFormat {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gel.mauve.format.FastaFormat#openFile(java.io.File)
	 */
	protected BufferedReader openFile (File file) {
		try {
			return new BufferedReader (new RawFastaBridgeFilterReader (
					new FileReader (file)));
		} catch (FileNotFoundException e) {
			// This exception is not expected, because file is required to
			// exist.
			throw new RuntimeException (e);
		}
	}
	
	 public boolean isRich(){ return false; }
	 
	    public String getFormatName () {
	    	return MauveConstants.RAW_FORMAT;
	    }
}