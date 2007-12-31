package org.gel.mauve.tree;

import java.util.Iterator;

public class IntervalSequenceTree {
	IstNode root;

	/** < Root of the tree */
	IstNode leftmost;

	/** < Left most tree node, for begin() method */
	IstNode rightmost;

	/** < Right most tree node, for end() method */

	/**
	 * Returns the total length of intervals contained in this interval sequence
	 */
	long nodeCount () {
		return root == null ? 0 : root.getSubtreeSize ();
	}

	public Iterator insert (Key val, long point) {
		long [] iv_offset = new long [1];
		iv_offset[0] = point;
		IstNode ins_node = recursiveFind (iv_offset, root);
		IstNode new_node = new IstNode ();
		new_node.setKey ((Key) val.copy ());
		new_node.setLength (val.getLength ());
		new_node.setSubtreeSize (0);
		if (ins_node == null) {
			// end insert
			rightmost = new_node;
			if (root == null) {
				root = new_node;
				leftmost = new_node;
				return new IstIterator (this, new_node);
			}
			// find the shallowest right insertion point
			ins_node = decrement (null);
			// make a new parent node
			IstNode new_parent = new IstNode ();
			new_parent.setLeft (ins_node);
			new_parent.setRight (new_node);
			new_parent.setParent (ins_node.getParent ());
			if (new_parent.getParent () == null)
				root = new_parent;
			else
				new_parent.getParent ().setRight (new_parent);
			ins_node.setParent (new_parent);
			new_parent.setLength (ins_node.getLength ());

			// update lengths and subtree_sizes along the path to the root
			propogateChanges (new_parent, new_node.getLength (), 2);
			return new IstIterator (this, new_node);
		}

		// iv_offset is the distance into the node that the leaf should be split
		// 0 is a special case (left insert)
		if (iv_offset[0] == 0) {
			IstNode new_parent = new IstNode ();
			new_parent.setLeft (new_node);
			new_parent.setRight (ins_node);
			new_parent.setParent (ins_node.getParent ());
			if (new_parent.getParent ().getRight () == ins_node)
				new_parent.getParent ().setRight (new_parent);
			else
				new_parent.getParent ().setLeft (new_parent);
			new_parent.setLength (ins_node.getLength ());

			ins_node.setParent (new_parent);
			new_node.setParent (new_parent);

			if (point == 0)
				leftmost = new_node;
			// update lengths and subtree_sizes along the path to the root
			propogateChanges (new_parent, new_node.getLength (), 2);
		} else {
			// need to split a leaf node
			IstNode new_gp = new IstNode ();
			IstNode new_parent = new IstNode ();
			new_gp.setParent (ins_node.getParent ());
			new_gp.setRight (new_parent);
			new_gp.setLeft (new IstNode ());
			new_gp.getLeft ().setKey ((Key) ins_node.getKey ().copy ());
			new_gp.getLeft ().getKey ().cropEnd (
					ins_node.getLength () - iv_offset[0]);
			new_gp.getLeft ().setLength (
					new_gp.getLeft ().getKey ().getLength ());
			new_gp.getLeft ().setParent (new_gp);

			ins_node.getKey ().cropStart (iv_offset[0]);
			ins_node.setLength (ins_node.getKey ().getLength ());
			ins_node.setParent (new_parent);
			new_node.setParent (new_parent);
			new_parent.setLeft (new_node);
			new_parent.setRight (ins_node);
			new_parent.setParent (new_gp);
			new_parent
					.setLength (new_node.getLength () + ins_node.getLength ());
			new_parent.setSubtreeSize (2);

			new_gp.setLength (ins_node.getLength ()
					+ new_gp.getLeft ().getLength ());
			new_gp.setSubtreeSize (1);
			if (new_gp.getParent () == null) {
				root = new_gp;
				leftmost = new_gp.getLeft ();
				rightmost = ins_node;
			} else if (new_gp.getParent ().getRight () == ins_node)
				new_gp.getParent ().setRight (new_gp);
			else
				new_gp.getParent ().setLeft (new_gp);

			// update lengths and subtree_sizes along the path to the root
			new_gp.setSubtreeSize (-1);
			propogateChanges (new_gp, new_node.getLength (), 4);
		}
		return new IstIterator (this, new_node);
	}

	public long erase (long point, long length) {
		long [] iv_offset = new long [1];
		iv_offset[0] = point;

		IstNode ins_node = recursiveFind (iv_offset, root);

		// iv_offset is the distance into the node that the leaf should be split
		// 0 is a special case (left delete)
		long deleted_nodes = 0;
		while (length > 0) {
			if (ins_node == null) {
				// end delete? that's illegal
				return deleted_nodes;
			}
			if (iv_offset[0] == 0) {
				if (length >= ins_node.getLength ()) {
					// delete the whole thing
					length -= ins_node.getLength ();
					if (ins_node.getParent () == null) {
						// deleting the root
						root = null;
						leftmost = null;
						rightmost = null;
						return deleted_nodes + 1;
					}

					IstNode other_child = null, del_node = null;
					if (ins_node.getParent ().getLeft () == ins_node) {
						other_child = ins_node.getParent ().getRight ();
					} else if (ins_node.getParent ().getRight () == ins_node) {
						other_child = ins_node.getParent ().getLeft ();
					}
					del_node = ins_node;
					ins_node = increment (ins_node);

					// update tree structure
					IstNode tmp_parent = other_child.getParent ();
					IstNode tmp_gp = tmp_parent.getParent ();
					// java: do something about the = operator!!
					tmp_parent.setKey (other_child.getKey ());
					tmp_parent.setLeft (other_child.getLeft ());
					tmp_parent.setRight (other_child.getRight ());
					tmp_parent.setParent (other_child.getParent ());
					tmp_parent.setSubtreeSize (other_child.getSubtreeSize ());
					tmp_parent.setLength (other_child.getLength ());

					tmp_parent.setParent (tmp_gp);
					if (tmp_parent.getLeft () != null)
						tmp_parent.getLeft ().setParent (tmp_parent);
					if (tmp_parent.getRight () != null)
						tmp_parent.getRight ().setParent (tmp_parent);
					if (ins_node == other_child)
						ins_node = tmp_parent;

					// propogate deletion length thru root
					tmp_parent = tmp_parent.getParent ();
					// checkTree( root );
					propogateChanges (tmp_parent, -del_node.getLength (), -2);

					++deleted_nodes;
				} else {
					// crop from start
					ins_node.getKey ().cropStart (length);
					// checkTree( root );
					propogateChanges (ins_node, -length, 0);
					return deleted_nodes;
				}
			} else if (length >= ins_node.getLength () - iv_offset[0]) {
				// crop from end
				ins_node.getKey ().cropEnd (
						ins_node.getLength () - iv_offset[0]);
				length -= ins_node.getLength () - iv_offset[0];
				// checkTree( root );
				propogateChanges (ins_node,
						-(ins_node.getLength () - iv_offset[0]), 0);
				ins_node = increment (ins_node);
				iv_offset[0] = 0;
			} else {
				// delete from middle (nastee part)
				IstNode new_parent = new IstNode ();
				new_parent.setLeft (ins_node);
				new_parent.setLength (ins_node.getLength ());
				new_parent.setRight (new IstNode ());
				new_parent.getRight ()
						.setKey ((Key) ins_node.getKey ().copy ());
				new_parent.getRight ().setLength (
						ins_node.getLength () - length - iv_offset[0]);
				new_parent.getRight ().getKey ().cropStart (
						length + iv_offset[0]);
				new_parent.getLeft ().getKey ().cropEnd (
						ins_node.getLength () - iv_offset[0]);
				new_parent.getLeft ().setLength (iv_offset[0]);
				new_parent.setParent (ins_node.getParent ());
				if (new_parent.getParent () == null) {
					root = new_parent;
					rightmost = new_parent.getRight ();
				} else if (new_parent.getParent ().getLeft () == ins_node)
					new_parent.getParent ().setLeft (new_parent);
				else if (new_parent.getParent ().getRight () == ins_node)
					new_parent.getParent ().setRight (new_parent);

				ins_node.setParent (new_parent);
				new_parent.getRight ().setParent (new_parent);
				propogateChanges (new_parent, -length, 2);
				return deleted_nodes;
			}
		}
		return deleted_nodes;
	}

	/**
	 * propogates changes to node values up a tree
	 */
	void propogateChanges (IstNode cur_node, long length_diff, long subtree_diff) {
		while (cur_node != null) {
			cur_node.setLength (cur_node.getLength () + length_diff);
			cur_node.setSubtreeSize (cur_node.getSubtreeSize () + subtree_diff);
			cur_node = cur_node.getParent ();
		}
	}

	protected IstNode recursiveFind (long [] point, IstNode node) {
		if (node == null)
			return null;

		// return this node if it's a leaf
		if (node.getKey () != null)
			return node;
		// look for the next node to recurse to
		if (point[0] < node.getLength ()) {
			if (node.getLeft () != null) {
				if (point[0] < node.getLeft ().getLength ())
					return recursiveFind (point, node.getLeft ());
				point[0] -= node.getLeft ().getLength ();
			}
			return recursiveFind (point, node.getRight ());
		}
		point[0] -= node.getLength ();
		// out of range
		return null;
	}

	IstNode increment (IstNode x) {
		// find the least-ancestor with another child
		// and set x to that child
		while (x.getParent () != null) {
			if (x == x.getParent ().getLeft ()
					&& x.getParent ().getRight () != null) {
				x = x.getParent ().getRight ();
				break;
			} else
				x = x.getParent ();
		}
		// if there were no other children to the right then we're at the end
		if (x.getParent () == null) {
			x = null;
			return x;
		}

		// find the left-most leaf node below x
		while (x.getKey () == null) {
			if (x.getLeft () != null)
				x = x.getLeft ();
			else if (x.getRight () != null)
				x = x.getRight ();
		}
		return x;
	}

	IstNode decrement (IstNode x) {
		if (x != null) {
			// find the least-ancestor with another child to the left
			// and set x to that child
			while (x.getParent () != null) {
				if (x == x.getParent ().getRight ()
						&& x.getParent ().getLeft () != null) {
					x = x.getParent ().getLeft ();
					break;
				} else
					x = x.getParent ();
			}
			// if there was no other children to the left then we're at the end
			// raise hell! (cause an access violation)
			if (x.getParent () == null)
				x = null;
		} else {
			x = root;
		}

		// find the right-most leaf node below x
		while (x.getKey () == null) {
			if (x.getRight () != null)
				x = x.getRight ();
			else if (x.getLeft () != null)
				x = x.getLeft ();
		}
		return x;
	}
}
