package org.gel.mauve.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.gel.mauve.FilterCacheSpec;
import org.gel.mauve.SupportedFormat;


public class MockFormat implements SupportedFormat
{
    Sequence inner;
    int innerSequenceReadCount;
    
    public MockFormat(Sequence inner)
    {
        this.inner = inner;
    }
    
    public SequenceIterator readFile(File file)
    {
        return new SequenceIterator()
        {
            int counter = 0;

            public boolean hasNext()
            {
                return counter == 0;
            }

            public Sequence nextSequence() throws NoSuchElementException, BioException
            {
                if (counter == 0) 
                {
                    counter++;
                    return inner; 
                }
                else
                {
                    throw new NoSuchElementException();
                }
            }
            
        };
    }

    public void validate(Sequence s, File file, int index) throws FileNotFoundException
    {
        return;
    }

    public Sequence readInnerSequence(File file, int index)
    {
        innerSequenceReadCount++;
        
        if (index == 0)
        {
            return inner;
        }
        else
        {
            throw new IndexOutOfBoundsException("index: " + index);
        }
    }

    public SequenceIterator makeIterator(File file)
    {
        return new SequenceIterator()
        {
            int counter = 0;

            public boolean hasNext()
            {
                return counter == 0;
            }

            public Sequence nextSequence() throws NoSuchElementException, BioException
            {
                if (counter == 0) 
                {
                    counter++;
                    return inner; 
                }
                else
                {
                    throw new NoSuchElementException();
                }
            }
            
        };
    }

    public Sequence makeDelegate(Sequence s, File source, int index) throws FileNotFoundException
    {
        return new DelegatingSequence(inner, this, source, index);
    }

    public String getSequenceName(Sequence s)
    {
        return s.getName();
    }

    public String getChromosomeName(Sequence s)
    {
        return s.getName();
    }

    public FilterCacheSpec[] getFilterCacheSpecs()
    {
        return new FilterCacheSpec[0];
    }
}
