package org.gel.mauve;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.RandomAccessFile;
import java.util.Properties;

import org.gel.mauve.backbone.BackboneListBuilder;


/**
 * @author pinfield
 * 
 * A viewer model backed by an XMFA file. Models a global gapped sequence
 * alignment in an XMFA format file
 */
public class XmfaViewerModel extends MauveAlignmentViewerModel {

	protected IOException init_error;
	
	public XmfaViewerModel (File src, ModelProgressListener listener)
	throws IOException {
		super (src, listener);
	}

	protected void init(ModelProgressListener listener, boolean isReloading) throws IOException
	{
		super.init(listener, isReloading);
		if (init_error != null) {
			IOException e = init_error;
			init_error = null;
			throw e;
		}
	}
	
	public void setSequenceCount (int sequenceCount) {
		super.setSequenceCount (sequenceCount);
	}

	protected void makeAlignment () {
        try {
			RandomAccessFile inputFile = new RandomAccessFile(getSrc(), "r");
			
			// read XMFA from object cache if possible
			if(cache_instream != null){
			    try{
			    	xmfa = (XMFAAlignment)cache_instream.readObject();
			    	((XMFAAlignment) xmfa).setFile(inputFile);
			    }catch(ClassNotFoundException cnfe){
			    	// cache must be corrupt
			    	cache_instream = null;
			    }catch(ClassCastException cce){
			    	// cache must be corrupt
			    	cache_instream = null;
			    }catch(InvalidClassException ice){
			    	cache_instream = null;
			    }
			}
			// it didn't get read from the cache
			if(cache_instream == null)
			    xmfa = new XMFAAlignment(inputFile);
		} catch (FileNotFoundException e) {
			init_error = e;
		} catch (IOException e) {
			init_error = e;
		}
    }
    
    protected void makeBBList () {
    	try{
    		bb_list = BackboneListBuilder.build(this,(XMFAAlignment) xmfa);
    		if( bb_list != null )
			{
				// if the backbone is newer than the cache then clear the cache
				File bb_file = BackboneListBuilder.getBbFile(this,(XMFAAlignment) xmfa);
				if(	ModelBuilder.getUseDiskCache() && bb_file.lastModified() > cache_file.lastModified())
					cache_instream = null;
			}

    	}catch(IOException ioe)
    	{
    		bb_list = null;
    	}
    }
    
    protected void makeSimilarityIndex () {
		try {
			for (int seqI = 0; seqI < xmfa.seq_count; seqI++)
			{
			    Genome g = getGenomeBySourceIndex(seqI);
			    // read the SimilarityIndex from object cache if possible
			    if(cache_instream != null){
			        try{
			        	sim[seqI] = (SimilarityIndex)cache_instream.readObject();
			        }catch(ClassNotFoundException cnfe){
			        	// cache must be corrupt
			        	cache_instream = null;
			        }catch(ClassCastException cce){
			        	// cache must be corrupt
			        	cache_instream = null;
			        }catch(InvalidClassException ice){
			        	cache_instream = null;
			        }
			    }
			    // it didn't get read from the cache
			    if(cache_instream == null || sim [seqI] == null)
			    	sim[seqI] = new SimilarityIndex (g, xmfa, bb_list);
			}
		} catch (IOException e) {
			init_error = e;
		}
    }
    
	protected void referenceUpdated () {
		super.referenceUpdated ();
		xmfa.setReference (getReference ());
	}

/**
     * 
     * @return
     */
    public XMFAAlignment getXmfa()
    {
        return (XMFAAlignment) xmfa;
    }

    public File getBbFile() {
    	Properties meta = ((XMFAAlignment) xmfa).metadata;
    	// Find the backbone data
    	String bb_fname;
    	if (meta.containsKey ("BackboneFile")) {
    		bb_fname = meta.getProperty ("BackboneFile");
    	} else {
    		return null; // no backbone information
    	}
    	File src = new File (bb_fname);
    	if (!src.canRead ()) {
    		bb_fname = getSrc ().getParent () + File.separatorChar
    		+ src.getName ();
    	}
    	src = new File (bb_fname);
    	if (!src.canRead ()) {
    		return null; // can't read the backbone file
    	}
    	return src;
    }

	protected Genome makeGenome(int genome_ind) {
		return GenomeBuilder.buildGenome(genome_ind, this);
	}


}
