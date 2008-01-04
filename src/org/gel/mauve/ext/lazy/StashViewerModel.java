package org.gel.mauve.ext.lazy;

import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;

import org.gel.mauve.Genome;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.MauveHelperFunctions;
import org.gel.mauve.ModelBuilder;
import org.gel.mauve.ModelProgressListener;
import org.gel.mauve.SimilarityIndex;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.ext.MauveInterfacer;
import org.gel.mauve.ext.MauveStoreConstants;

public class StashViewerModel extends XmfaViewerModel implements
		MauveStoreConstants {
	
	public StashViewerModel (File src, ModelProgressListener listener) 
			throws IOException {
		super (src, listener);
	}

	protected void init (ModelProgressListener listener, boolean reloading) 
			throws IOException {
		ModelBuilder.setUseDiskCache(false);
		super.init (listener, reloading);
	}
	
	protected void makeAlignment () {
		
	}
	
	protected void makeBBList () {
		
	}
	
    protected void makeSimilarityIndex () {
		File sim_dir = MauveHelperFunctions.getChildOfRootDir (this, 
				MauveConstants.SIMILARITY_OUTPUT);
		boolean sim_from_file = false;
		if (sim_dir.exists())
			sim_from_file = true;
		else
			sim_dir.mkdir();
		for (int seqI = 0; seqI < getSequenceCount (); seqI++)
		{
			if (sim_from_file) {
				MauveInterfacer.readSimilarity(this, seqI); 
			}
		}
    }
}
