package org.gel.mauve.tree;

import java.io.Serializable;

public class GapKey implements Key, Cloneable, Serializable
{
	static final long serialVersionUID = 1;
    public long length;

    public GapKey(long length)
    {
        this.length = length;
    }

    public long getLength()
    {
        return length;
    }

    public long getSeqLength()
    {
        return 0;
    }

    public void cropStart(long size)
    {
        length -= size;
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
        return "GapKey[length: " + length + "]";
    }
}