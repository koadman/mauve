package org.gel.mauve.tree;

import java.io.Serializable;

public class FileKey implements Key, Cloneable, Serializable
{
	static final long serialVersionUID = 1;
    private long lend;
    private long length = 0;
    private long f_offset;
    private long f_length = 0;

    public FileKey(long lend, long f_offset)
    {
        this.lend = lend;
        this.f_offset = f_offset;
    }

    public long getLength()
    {
        return length;
    }

    public void incrementLength()
    {
        length++;
    }

    public long getSeqLength()
    {
        return length;
    }

    public long getOffset()
    {
        return f_offset;
    }

    public long getFLength()
    {
        return f_length;
    }

    public void setFLength(long f_length)
    {
        this.f_length = f_length;
    }

    public void cropStart(long size)
    {
        length -= size;
        lend += size;
        if (length < 0)
            throw new ArrayIndexOutOfBoundsException();
    }

    public void cropEnd(long size)
    {
        length -= size;
        if (length < 0)
            throw new ArrayIndexOutOfBoundsException();
    }

    public Object copy()
    {
        try
        {
            return clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new Error("Very unexpected exception", e);
        }
    }

    public String toString()
    {
        return "FileKey[seq_offset: " + lend + " seq_len: " + length + " bfo: " + f_offset + " len: " + f_length + "]";
    }

}