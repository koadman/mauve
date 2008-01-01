package org.gel.mauve.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojavax.bio.BioEntry;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.gel.mauve.SupportedFormat;
import org.gel.mauve.ext.LoadedSequenceIteratorCache;

public abstract class BaseFormat implements SupportedFormat
{
	
	public SequenceIteratorCache iterator_cache;
	
	public BaseFormat () {
		iterator_cache = new LoadedSequenceIteratorCache ();
	}
    public void validate(Sequence s, File source, int index) throws FileNotFoundException
    {
        if (!source.exists())
            throw new FileNotFoundException("File " + source + " not found.");
    }

    public Sequence makeDelegate(Sequence s, File source, int index) throws FileNotFoundException
    {
    	if(s instanceof RichSequence)
    		return new RichDelegatingSequence(s, this, source, index);
        return new DelegatingSequence(s, this, source, index);
    }

    public Sequence readInnerSequence(File source, int index)
    {
    	try
        {
        	return iterator_cache.getSequence (
        			source, this, index);
        }
        catch (NoSuchElementException e)
        {
            // This will only happen when we try to create a Delegating sequence
            // with a too-large index.
            throw new RuntimeException("Unexpected exception.", e);
        }
    }

    /**
     * @param file
     *            A file, which is expected to exist.
     * @return
     * 
     * Produces an iterator that creates DelegateSequences while reading a
     * sequence file.
     */
    public SequenceIterator makeIterator(final File file)
    {
    	return iterator_cache.makeIterator (this, file);
    }

}