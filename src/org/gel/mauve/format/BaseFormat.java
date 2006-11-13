package org.gel.mauve.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.gel.mauve.SupportedFormat;

abstract class BaseFormat implements SupportedFormat
{
    public void validate(Sequence s, File source, int index) throws FileNotFoundException
    {
        if (!source.exists())
            throw new FileNotFoundException("File " + source + " not found.");
    }

    public Sequence makeDelegate(Sequence s, File source, int index) throws FileNotFoundException
    {
        return new DelegatingSequence(s, this, source, index);
    }

    public Sequence readInnerSequence(File source, int index)
    {
        SequenceIterator iterator = readFile(source);
        try
        {
            for (int i = 0; i < index; i++)
            {
                iterator.nextSequence();
            }
            return iterator.nextSequence();
        }
        catch (NoSuchElementException e)
        {
            // This will only happen when we try to create a Delegating sequence
            // with a too-large index.
            throw new RuntimeException("Unexpected exception.", e);
        }
        catch (BioException e)
        {
            // TODO: Figure out how this should be handled elegantly.
            throw new Error("Unexpected exception.", e);
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
        return new SequenceIterator()
        {
            SequenceIterator inner = readFile(file);
            int index = -1;

            public boolean hasNext()
            {
                return inner.hasNext();
            }

            public Sequence nextSequence() throws NoSuchElementException, BioException
            {
                Sequence s = inner.nextSequence();
                index++;
                try
                {
                    return makeDelegate(s, file, index);
                }
                catch (FileNotFoundException e)
                {
                    // The file has already been verified above, so there must
                    // be a weird problem.
                    throw new Error("Index: " + index, e);
                }
            }
        };
    }

}