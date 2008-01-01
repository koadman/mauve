package org.gel.mauve.ext.io;

import java.util.Hashtable;
import java.util.Vector;

import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.analysis.Segment;
import org.gel.mauve.analysis.output.AbstractTabbedDataWriter;
import org.gel.mauve.ext.MauveStoreConstants;

public class SegmentWriter extends AbstractTabbedDataWriter implements MauveStoreConstants {
	
	protected Segment [] segments;
	protected XmfaViewerModel model;
	
	protected SegmentWriter (String file, Hashtable args) {
		super (file, args);
		printData ();
		doneWritingFile ();
	}
	
	public static SegmentWriter getSegmentWriter (String file, XmfaViewerModel model) {
		Hashtable args = new Hashtable ();
		args.put(file, model);
		return new SegmentWriter (file, args);
	}
	
	@Override
	public void initSubClassParticulars (Hashtable args) {
		model = (XmfaViewerModel) args.get(file_name);
		segments = model.getFullLcbList(); 
		super.initSubClassParticulars(args);
	}

	@Override
	protected String getData(int column, int row) {
		int seq = column / 3;
		int which = column % 3;
		String val = null;
		switch (which) {
		case 0:
			val = segments [row].starts [seq] + "";
			break;
		case 1:
			val = segments [row].ends [seq] + "";
			break;
		case 2:
			val = segments [row].reverse [seq] ? REVERSE_SYMBOL : FORWARD_SYMBOL;
			break;
		}
		return val;
	}

	@Override
	protected boolean moreRowsToPrint() {
		return row_number < segments.length;
	}

	@Override
	protected Vector setColumnHeaders() {
		return null;
	}
	
	/**not set in this class, so will be empty
	*from superclass.  Overwritten to give things
	*right size.
	**/
	@Override
	public void setColumnHeaders (Vector titles) {
		if (titles == null) {
			headers = new String [model.getSequenceCount() * 3];
			current_row = new String [headers.length];
		}
		else
			super.setColumnHeaders (titles);
		
	}

	@Override
	protected boolean shouldPrintRow(int row) {
		return true;
	}

}
