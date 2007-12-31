package org.gel.mauve.format;

import java.io.FileNotFoundException;

import junit.framework.TestCase;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.impl.SimpleSequence;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.AtomicSymbol;
import org.biojava.bio.symbol.DummySymbolList;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.SingletonAlphabet;


public class DelegatingSequenceTest extends TestCase
{
    static AtomicSymbol sym = AlphabetManager.createSymbol("X");
    static FiniteAlphabet a = new SingletonAlphabet(sym);
    
    MockFormat format;

    public void testInitiallyNoLoading()
    {
        Sequence s = testingDelegate(42);
        assertEquals(0, format.innerSequenceReadCount);
        s.symbolAt(13);
        assertEquals(1, format.innerSequenceReadCount);
    }
    
    public void testOneSymbolWorks()
    {
        Sequence s = testingDelegate(1);
        assertEquals(1, s.length());
        assertEquals(sym, s.symbolAt(1));
    }

    public void testZeroSymbolsWorks()
    {
        Sequence s = testingDelegate(0);
        assertEquals(0, s.length());
    }
    
    public void testHighOutsideThrowsException()
    {
        Sequence s = testingDelegate(1);
        try
        {
            s.symbolAt(2);
        }
        catch (IndexOutOfBoundsException e)
        {
            return;
        }
        fail();
    }
    
    public void testLowOutsideThrowsException()
    {
        Sequence s = testingDelegate(0);
        try
        {
            s.symbolAt(0);
        }
        catch (IndexOutOfBoundsException e)
        {
            return;
        }
        fail();
    }
    
    private Sequence testingDelegate(int length)
    {
        Sequence dummySequence = new SimpleSequence(new DummySymbolList(a, length), "someurn", "somename", Annotation.EMPTY_ANNOTATION);
        format = new MockFormat(dummySequence);
        try
        {
            return new DelegatingSequence(dummySequence, format, null, 0);
        }
        catch (FileNotFoundException e)
        {
            // Never should happen.
            throw new RuntimeException(e);
        }
    }
    
    
}
