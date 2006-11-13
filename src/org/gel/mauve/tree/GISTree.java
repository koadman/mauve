package org.gel.mauve.tree;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Records a mapping between gapped sequence coordinates and ungapped sequence
 * coordinates. Implemented using a splay tree for O(n log n) amortized time
 * complexity operations.
 */
public class GISTree implements Serializable
{
	static final long serialVersionUID = 1;

    public static long end = Long.MAX_VALUE;

    int rootIndex = TreeStore.NULL_REF;
    TreeStore ts = null;
    
    /** Create a new GISTree using the given TreeStore to store data */
    public GISTree(TreeStore ts)
    {
    	this.ts = ts;
    }

    /** returns the total length of the gapped sequence stored in the tree */
    public long length()
    {
        return (rootIndex == TreeStore.NULL_REF) ? 0 : ts.length[rootIndex];
    }

    // interval sequence specific:

    /** returns the length of ungapped sequence stored in the tree */
    public long sequenceLength()
    {
        return rootIndex == TreeStore.NULL_REF ? 0 : getSeqLength(rootIndex);
    }

    /** find the interval containing a position in the gapped sequence */
    public int find(long seq_point)
    {
        long[] position = new long[1];
        position[0] = seq_point;
        int index = recursiveFind(rootIndex, position);
        splay(index);
        return rootIndex;
    }

    /** find the interval containing a position in the ungapped sequence */
    public int find_seqindex(long seq_point)
    {
        long[] position = new long[1];
        position[0] = seq_point;
        int index = recursiveSeqFind(rootIndex, position);
        splay(index);
        return rootIndex;
    }

    public long getSequenceStart(int index)
    {
        splay(index);
        return getLeft(rootIndex) != TreeStore.NULL_REF ? getSeqLength(getLeft(rootIndex)) : 0;
    }

    public long getStart(int index)
    {
        splay(index);
        return getLeft(rootIndex) != TreeStore.NULL_REF ? getLength(getLeft(rootIndex)) : 0;
    }

    public int insert(Key val, long point)
    {
        long[] position = new long[1];
        position[0] = point;
        int newIndex = ts.createGistNode();

        setKey(newIndex, (Key) val.copy()); // newNode.setKey((Key)val.copy());
        setLength(newIndex, val.getLength()); //newNode.setLength(val.getLength());
        setSeqLength(newIndex, val.getSeqLength()); // newNode.setSeqLength(val.getSeqLength());

        // just insert new_node as root if the tree is empty
        if (rootIndex == TreeStore.NULL_REF)
        {
            rootIndex = newIndex;
            return rootIndex;
        }

        int insertionIndex = recursiveFind(rootIndex, position);

        // insert the new node below ins_node
        if (position[0] > 0 && position[0] < getKey(insertionIndex).getLength())
        {
            // trunc ins_node, do a right insert of new_node and the right part
            // of ins_node
            int rightIndex = ts.createGistNode();
            // Question: does inserting two nodes at once violate the splay
            // rules?
            // probably, but i'm not sure it really matters
            setRight(newIndex, rightIndex);
            setParent(rightIndex, newIndex);
            setKey(rightIndex, (Key) getKey(insertionIndex).copy());

            // crop the key
            getKey(rightIndex).cropStart(position[0]);
            setLength(rightIndex, val.getLength());
            setSeqLength(rightIndex, val.getSeqLength());
            // question: do I need to update new_node.length or will the splay
            // operations
            // take care of it?

            getKey(insertionIndex).cropEnd(getKey(insertionIndex).getLength() - position[0]);
            setSeqLength(insertionIndex, getKey(insertionIndex).getSeqLength());
            setLength(insertionIndex, getKey(insertionIndex).getLength());
            // now position[0] ought to be equal to ins_node.length,
            // so new_node should get inserted right below it
        }

        if (position[0] == 0)
        {
            // find the right-most child of the left subtree and insert there
            int currentIndex = getLeft(insertionIndex);
            if (currentIndex != TreeStore.NULL_REF)
            {
                while (getRight(currentIndex) != TreeStore.NULL_REF)
                {
                    currentIndex = getRight(currentIndex);
                }
                // insert to the right of cur_node
                setRight(currentIndex, newIndex);
                setParent(newIndex, currentIndex);
            }
            else
            {
                // insert to the left of ins_node
                setLeft(insertionIndex, newIndex);
                setParent(newIndex, insertionIndex);
            }
        }

        if (position[0] >= getKey(insertionIndex).getLength())
        {
            // find the left-most child of the right subtree and insert there
            int currentIndex = getRight(insertionIndex);
            if (currentIndex != TreeStore.NULL_REF)
            {
                while (getLeft(currentIndex) != TreeStore.NULL_REF)
                {
                    currentIndex = getLeft(currentIndex);
                }
                // insert to the left of cur_node
                setLeft(currentIndex, newIndex);
                setParent(newIndex, currentIndex);
            }
            else
            {
                // insert to the right of ins_node
                setRight(insertionIndex, newIndex);
                setParent(newIndex, insertionIndex);
            }
        }
        splay(newIndex);
        return newIndex;
    }

    /**
     * splay a node to the root of the tree
     */
    protected void splay(int index)
    {
        _splay(index);
        rootIndex = index;
    }

    /**
     * Convert a sequence coordinate to a column index
     */
    public long seqPosToColumn(long seq_index)
    {
        int l_iter = find_seqindex(seq_index);
        long fk_left_seq_off = getSequenceStart(l_iter);
        long fk_left_col = getStart(l_iter);
        fk_left_col += seq_index - fk_left_seq_off;
        return fk_left_col;
    }

    /**
     * Convert a column index to a sequence index, taking the nearest seq index
     * to the left if the column falls in a gap region
     */
    public long columnToSeqPos(long column)
    {
        int l_iter = find(column);
        long seq_off = getSequenceStart(l_iter);
        long left_col = getStart(l_iter);
        if (column != left_col && getKey(l_iter).getSeqLength() != 0)
            seq_off += column - left_col;
        return seq_off;
    }



    private void recalculateLengths(int index)
    {
        ts.length[index] = ts.key[index].getLength();
        ts.seqLength[index] = ts.key[index].getSeqLength();

        if (ts.right[index] != TreeStore.NULL_REF)
        {
            ts.length[index] += ts.length[ts.right[index]];
            ts.seqLength[index] += ts.seqLength[ts.right[index]];
            ts.parent[ts.right[index]] = index;
        }

        if (ts.left[index] != TreeStore.NULL_REF)
        {
            ts.length[index] += ts.length[ts.left[index]];
            ts.seqLength[index] += ts.seqLength[ts.left[index]];
            ts.parent[ts.left[index]] = index;
        }
    }

    /**
     * splay this node to the root of the tree
     */
    public void _splay(int index)
    {
        // splay operations and node naming convention taken from
        // http://www.cs.nyu.edu/algvis/java/SplayTree.html
        while (ts.parent[index] != TreeStore.NULL_REF)
        {
            int yIndex = ts.parent[index];
            int zIndex = (ts.parent[yIndex] != TreeStore.NULL_REF) ? ts.parent[yIndex] : yIndex;
            if (ts.left[ts.parent[index]] == index)
            {
                if (ts.parent[ts.parent[index]] == TreeStore.NULL_REF)
                {
                    ts.left[yIndex] = ts.right[index];
                    ts.right[index] = yIndex;
                }
                else if (ts.left[ts.parent[ts.parent[index]]] == ts.parent[index])
                {
                    // zig-zig
                    ts.left[zIndex] = ts.right[yIndex];
                    ts.left[yIndex] = ts.right[index];
                    ts.right[yIndex] = zIndex;
                    ts.right[index] = yIndex;
                }
                else
                {
                    // zag-zig
                    ts.right[zIndex] = ts.left[index];
                    ts.left[yIndex] = ts.right[index];
                    ts.right[index] = yIndex;
                    ts.left[index] = zIndex;
                }
            }
            else
            {
                if (ts.right[ts.parent[index]] != index)
                    throw new Error("Inconsistency error");

                // zagging
                if (ts.parent[ts.parent[index]] == TreeStore.NULL_REF)
                {
                    // zag
                    ts.right[yIndex] = ts.left[index];
                    ts.left[index] = yIndex;
                }
                else if (ts.left[ts.parent[ts.parent[index]]] == ts.parent[index])
                {
                    // zig-zag
                    ts.left[zIndex] = ts.right[index];
                    ts.right[yIndex] = ts.left[index];
                    ts.left[index] = yIndex;
                    ts.right[index] = zIndex;
                }
                else
                {
                    // zag-zag
                    ts.right[zIndex] = ts.left[yIndex];
                    ts.right[yIndex] = ts.left[index];
                    ts.left[yIndex] = zIndex;
                    ts.left[index] = yIndex;
                }
            }
            // update parents and lengths
            ts.parent[index] = ts.parent[zIndex];
            if (ts.parent[index] != TreeStore.NULL_REF)
            {
                if (ts.left[ts.parent[index]] == zIndex)
                    ts.left[ts.parent[index]] = index;
                else
                    ts.right[ts.parent[index]] = index;
            }
            recalculateLengths(zIndex);
            recalculateLengths(yIndex);
            recalculateLengths(index);
        }
    }

    /**
     * returns the Key of the node immediately to the right of x, or null if x
     * is already the righ-most tree node
     */
    public Key increment(int index)
    {
        // if x has a right child, find its leftmost descendant
        if (ts.right[index] != TreeStore.NULL_REF)
        {
            int leftie = ts.right[index];
            while (ts.left[leftie] != TreeStore.NULL_REF)
                leftie = ts.left[leftie];
            return ts.key[leftie];
        }

        // look for the least ancestor where x was the left descendant
        while (ts.parent[index] != TreeStore.NULL_REF)
        {
            if (ts.left[ts.parent[index]] == index)
                return ts.key[ts.parent[index]];
            index = ts.parent[index];
        }

        // x is already the right-most tree value
        return null;
    }

    /**
     * find the node below cur_node containing a given position in the gapped
     * sequence, starting at the left-most position below cur_node
     * 
     * @param cur_node
     *            The tree node to use as the base for the search. usually the
     *            root
     * @param position
     *            The position to search for. A single element array. The value
     *            is modified to reflect the distance into the returned node
     *            where the requested position actually occurs. version 2 of
     *            recursiveFind -- don't build a potentially huge call stack!
     */
    public int recursiveFind(int index, long[] position)
    {
        while (index != TreeStore.NULL_REF)
        {
            long left_len = ts.left[index] != TreeStore.NULL_REF ? ts.length[ts.left[index]] : 0;
            if (ts.left[index] != TreeStore.NULL_REF && position[0] < ts.length[ts.left[index]])
            {
                index = ts.left[index];
                continue;
            }

            // it's not part of the left subtree, subtract off the left subtree
            // length
            position[0] -= left_len;
            if (ts.right[index] != TreeStore.NULL_REF && position[0] >= ts.key[index].getLength())
            {
                position[0] -= ts.key[index].getLength();
                index = ts.right[index];
                continue;
            }

            // return this node if nothing else can be done
            return index;
        }
        return TreeStore.NULL_REF;
    }

    /**
     * Find the node below cur_node containing a given position in the ungapped
     * sequence, starting at the left-most position below cur_node
     * 
     * @param cur_node
     *            The tree node to use as the base for the search. usually the
     *            root
     * @param position
     *            The position to search for. A single element array. The value
     *            is modified to reflect the distance into the returned node
     *            where the requested position actually occurs. version 2 of
     *            recursiveSeqFind -- don't build a potentially huge call stack!
     */
    public int recursiveSeqFind(int index, long[] position)
    {
        while (index != TreeStore.NULL_REF)
        {
            long left_len = ts.left[index] != TreeStore.NULL_REF ? ts.seqLength[ts.left[index]] : 0;
            if (ts.left[index] != TreeStore.NULL_REF && position[0] < ts.seqLength[ts.left[index]])
            {
                index = ts.left[index];
                continue;
            }

            // it's not part of the left subtree, subtract off the left subtree
            // length
            position[0] -= left_len;
            if (ts.right[index] != TreeStore.NULL_REF && position[0] >= ts.key[index].getSeqLength())
            {
                position[0] -= ts.key[index].getSeqLength();
                index = ts.right[index];
                continue;
            }

            // return this node if nothing else can be done
            return index;
        }
        return TreeStore.NULL_REF;
    }

    public Key getKey(int index)
    {
        return ts.key[index];
    }

    public void setKey(int index, Key k)
    {
        ts.key[index] = k;
    }

    public long getSeqLength(int index)
    {
        return ts.seqLength[index];
    }

    public void setSeqLength(int index, long l)
    {
        ts.seqLength[index] = l;
    }

    public long getLength(int index)
    {
        return ts.length[index];
    }

    public void setLength(int index, long l)
    {
        ts.length[index] = l;
    }

    public int getLeft(int index)
    {
        return ts.left[index];
    }

    public void setLeft(int index, int l)
    {
        ts.left[index] = l;
    }

    public int getRight(int index)
    {
        return ts.right[index];
    }

    public void setRight(int index, int r)
    {
        ts.right[index] = r;
    }

    public int getParent(int index)
    {
        return ts.parent[index];
    }

    public void setParent(int index, int p)
    {
        ts.parent[index] = p;
    }

}

