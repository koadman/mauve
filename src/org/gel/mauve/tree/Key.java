package org.gel.mauve.tree;

/**
 * Key specifies the interface allowed for intervals that can be stored in an
 * interval sequence tree.
 */
interface Key
{
    public long getLength();

    public long getSeqLength();

    public void cropStart(long size);

    public void cropEnd(long size);

    public Object copy();
}

