package org.gel.mauve;

import java.io.File;
import java.io.FileNotFoundException;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;

/**
 * A definition of a format supported for annotation/feature reading. All
 * formats must support delegation, that is, the smart caching of features and
 * such.
 */
public interface SupportedFormat
{
    /**
     * @param reader
     * @return
     * 
     * Create a sequenceIterator over the set of sequences within the file.
     *  
     */
    SequenceIterator readFile(File file);

    /**
     * @param s
     * @param file
     * @throws FileNotFoundException
     * 
     * Do whatever initial validation of sequence or file that is necessary
     *  
     */
    void validate(Sequence s, File file, int index) throws FileNotFoundException;

    /**
     * 
     * @param file
     * @param index
     * @return
     */
    Sequence readInnerSequence(File file, int index);

    /**
     * 
     * @param s
     * @param source
     * @param index
     * @return
     * @throws FileNotFoundException
     */
    SequenceIterator makeIterator(File file);

    /**
     * @param s
     * @param source
     * @param index -
     *            the ordinal of the sequence that should be delegated.
     * @return
     * @throws FileNotFoundException
     * 
     * Create a delegate based on the source file. The sequence s must
     * correspond to the contents of the file.
     */
    Sequence makeDelegate(Sequence s, File source, int index) throws FileNotFoundException;

    /**
     * @param s
     * @return
     * 
     * Return a human-readable name for the entire sequence.
     */
    String getSequenceName(Sequence s);

    /**
     * @param s
     * @return
     * 
     * Return a human-readable name for the subsequence.
     */
    String getChromosomeName(Sequence s);

    /**
     * True if this parsing files of this format results in biojavax RichSequence objects
     * @return
     */
    boolean isRich();

    FilterCacheSpec[] getFilterCacheSpecs();
}