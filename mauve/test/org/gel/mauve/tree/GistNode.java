package org.gel.mauve.tree;

public class GistNode {
	
    int index;
    GISTree t;
    
	private GistNode(int index, GISTree t)
	{
	    this.index = index;
	    this.t = t;
	}

    public GistNode getParent()
    {
        return loadNode(t.getParent(index), t);
    }

    public GistNode getLeft()
    {
        return loadNode(t.getLeft(index), t);
    }

    public GistNode getRight()
    {
        return loadNode(t.getRight(index), t);
    }
    
    public long getLength()
    {
        return t.getLength(index);
    }

    public long getSeqLength()
    {
        return t.getSeqLength(index);
    }

    public Key getKey()
    {
        return t.getKey(index);
    }

	public static GistNode loadNode(int index, GISTree t)
	{
	    if (index == TreeStore.NULL_REF)
	    {
	        return null;
	    }
	    else
	    {
	        return new GistNode(index, t);
	    }
	}
	
	
	
}
