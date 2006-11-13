package org.gel.mauve.format;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;


public class RawFastaBridgeFilterReader extends FilterReader
{
    private final static String HEADER = ">raw\n";
    private int remainingHeader = HEADER.length();

    protected RawFastaBridgeFilterReader(Reader in)
    {
        super(in);
    }

    /* (non-Javadoc)
     * @see java.io.Reader#read()
     */
    public int read() throws IOException
    {
        if (remainingHeader > 0)
        {
            char c = HEADER.charAt(HEADER.length() - remainingHeader);
            remainingHeader--;
            return c;
        }
        else
        {
            return super.read();
        }
    }
    
    /* (non-Javadoc)
     * @see java.io.Reader#read(char[], int, int)
     */
    public int read(char[] cbuf, int off, int len) throws IOException
    {
        if (remainingHeader > 0)
        {
            int counter = 0;
            
            while (remainingHeader > 0)
            {
                cbuf[off] = (char) read();
                off++;
                len--;
                counter++;
            }
            return counter + super.read(cbuf, off, len);
        }
        else
        {
            return super.read(cbuf, off, len);
        }
    }
    
    
    /* (non-Javadoc)
     * @see java.io.Reader#skip(long)
     */
    public long skip(long n) throws IOException
    {
        if (remainingHeader > 0)
        {
            if (n <= remainingHeader)
            {
                remainingHeader = remainingHeader - (int) n;
                return n;
            }
            else
            {
                int oldRemaining = remainingHeader;
                remainingHeader = 0;
                n = n - oldRemaining;
                return super.skip(n) + oldRemaining; 
            }
            
        }
        else
        {
            return super.skip(n);
        }
    }
    
    /* (non-Javadoc)
     * @see java.io.Reader#reset()
     */
    public void reset() throws IOException
    {
        throw new IOException("Reset not supported.");
    }
    
    /* (non-Javadoc)
     * @see java.io.Reader#mark(int)
     */
    public void mark(int readAheadLimit) throws IOException
    {
        throw new IOException("Mark not supported");
    }

    /* (non-Javadoc)
     * @see java.io.Reader#markSupported()
     */
    public boolean markSupported()
    {
        return false;
    }
}
