package org.gel.mauve.tree;

class IstNode
{
    private IstNode parent;
    private IstNode left;
    private IstNode right;
    private long subtreeSize;
    private long length;
    private Key key;

    void setParent(IstNode parent)
    {
        if (parent == this)
            throw new RuntimeException("Error:  attempt to set node's parent as itself.");

        this.parent = parent;
    }

    IstNode getParent()
    {
        return parent;
    }

    void setLeft(IstNode left)
    {
        this.left = left;
    }

    IstNode getLeft()
    {
        return left;
    }

    void setRight(IstNode right)
    {
        this.right = right;
    }

    IstNode getRight()
    {
        return right;
    }

    void setSubtreeSize(long subtreeSize)
    {
        this.subtreeSize = subtreeSize;
    }

    long getSubtreeSize()
    {
        return subtreeSize;
    }

    void setLength(long length)
    {
        this.length = length;
    }

    long getLength()
    {
        return length;
    }

    void setKey(Key key)
    {
        this.key = key;
    }

    Key getKey()
    {
        return key;
    }
};
