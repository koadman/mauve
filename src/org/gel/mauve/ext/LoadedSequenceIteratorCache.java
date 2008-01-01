package org.gel.mauve.ext;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojavax.bio.BioEntry;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.gel.mauve.format.BaseFormat;
import org.gel.mauve.format.SequenceIteratorCache;

/**
 *  Acts as a sequence cache iterator that caches all sequences instead of an
 *  iterator pointing to the last retrieved sequence.  Prevents frequent file reads
 *  and seems to be keeping memory lower than caching is in some cases.  Vastly improves
 *  speed for viewing Feature Detail panel and getting search results.
 *  
 *  Currently caches all sequence information by file path and has no explicitly
 *  defined size or time limit.  Because of this, it doesn't make use of the 
 *  DelegatingSequence or the thin features.  Depending on things, this should maybe be
 *  changed.
 *  
 * @author Aaron Darling, James Lowden and Anna I Rissman
 *
 */
public class LoadedSequenceIteratorCache extends SequenceIteratorCache {
	
	public static Hashtable <File, Sequence []> sequences = 
			new Hashtable <File, Sequence []> ();
	
	public Sequence getSequence (final File source, BaseFormat format, int index) {
		synchronized (source) {
			if (sequences.get(source) == null) {
				SequenceIterator itty = format.readFile(source);
				LinkedList <Sequence> temp = new LinkedList <Sequence> ();
				while (itty.hasNext()) {
					try {
						temp.add(itty.nextSequence());
					} catch (NoSuchElementException e) {
						e.printStackTrace();
					} catch (BioException e) {
						System.out.println ("Couldn't load " + index + "the sequence from " +
								source.getAbsolutePath());
						e.printStackTrace();
					}
				}
				Sequence [] seqs = new Sequence [temp.size()];
				temp.toArray(seqs);
				sequences.put (source, seqs);
			}
			return sequences.get (source) [index];
		}
	}
	
	
	public SequenceIterator makeIterator (final BaseFormat format, final File file) {
		getSequence (file, format, 0);
	   	if(format.isRich())
    	{
            return new RichSequenceIterator()
            {
                int index = -1;

                public boolean hasNext()
                {
                    return index < sequences.get(file).length - 1;
                }

                public Sequence nextSequence() throws NoSuchElementException, BioException
                {
                	Sequence s = sequences.get(file) [++index];
                   /** try
                    {
                        s = format.makeDelegate(s, file, index);
                    }
                    catch (FileNotFoundException e)
                    {
                        // The file has already been verified above, so there must
                        // be a weird problem.
                        throw new Error("Index: " + index, e);
                    }**/
                	return s;
                }
                public RichSequence nextRichSequence() throws NoSuchElementException, BioException
                {
                	return (RichSequence)nextSequence();
                }
                
                public BioEntry nextBioEntry() throws BioException
                {
                	return (BioEntry) nextSequence ();
                }
            };
    	}
        return new SequenceIterator()
        {
            SequenceIterator inner = format.readFile(file);
            int index = -1;

            public boolean hasNext()
            {
            	return index < sequences.get(file).length - 1;
            }

            public Sequence nextSequence() throws NoSuchElementException, BioException
            {
            	Sequence s = sequences.get(file) [++index];
                /** try
                 {
                     s = format.makeDelegate(s, file, index);
                 }
                 catch (FileNotFoundException e)
                 {
                     // The file has already been verified above, so there must
                     // be a weird problem.
                     throw new Error("Index: " + index, e);
                 }**/
             	return s;
            }
        };
	}

}
