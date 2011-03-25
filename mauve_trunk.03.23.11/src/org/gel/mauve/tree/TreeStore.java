package org.gel.mauve.tree;

import java.io.Serializable;
import java.util.Arrays;

public class TreeStore implements Serializable {
	final static int NULL_REF = -1;

	private final static int INITIAL_SIZE = 10;

	private final static float SCALE_FACTOR = 1.1f;

	private int nodeCount = 0;

	public int [] parent = new int [INITIAL_SIZE];

	public int [] left = new int [INITIAL_SIZE];

	public int [] right = new int [INITIAL_SIZE];

	public long [] length = new long [INITIAL_SIZE];

	public long [] seqLength = new long [INITIAL_SIZE];

	public Key [] key = new Key [INITIAL_SIZE];

	public TreeStore () {
		Arrays.fill (parent, NULL_REF);
		Arrays.fill (left, NULL_REF);
		Arrays.fill (right, NULL_REF);
	}

	public int createGistNode () {
		if (nodeCount >= parent.length) {
			resizeArrays ();
		}
		int val = nodeCount;
		nodeCount++;
		return val;
	}

	private void resizeArrays () {
		int newSize = (int) (parent.length * SCALE_FACTOR);

		int [] new_parent = new int [newSize];
		Arrays.fill (new_parent, TreeStore.NULL_REF);
		System.arraycopy (parent, 0, new_parent, 0, parent.length);
		parent = new_parent;

		int [] new_left = new int [newSize];
		Arrays.fill (new_left, TreeStore.NULL_REF);
		System.arraycopy (left, 0, new_left, 0, left.length);
		left = new_left;

		int [] new_right = new int [newSize];
		Arrays.fill (new_right, TreeStore.NULL_REF);
		System.arraycopy (right, 0, new_right, 0, right.length);
		right = new_right;

		long [] new_length = new long [newSize];
		System.arraycopy (length, 0, new_length, 0, length.length);
		length = new_length;

		long [] new_seq_length = new long [newSize];
		System.arraycopy (seqLength, 0, new_seq_length, 0, seqLength.length);
		seqLength = new_seq_length;

		Key [] new_key = new Key [newSize];
		System.arraycopy (key, 0, new_key, 0, key.length);
		key = new_key;
	}

	public void pruneArrays () {
		int [] new_parent = new int [nodeCount];
		System.arraycopy (parent, 0, new_parent, 0, nodeCount);
		parent = new_parent;

		int [] new_left = new int [nodeCount];
		System.arraycopy (left, 0, new_left, 0, nodeCount);
		left = new_left;

		int [] new_right = new int [nodeCount];
		System.arraycopy (right, 0, new_right, 0, nodeCount);
		right = new_right;

		long [] new_length = new long [nodeCount];
		System.arraycopy (length, 0, new_length, 0, nodeCount);
		length = new_length;

		long [] new_seq_length = new long [nodeCount];
		System.arraycopy (seqLength, 0, new_seq_length, 0, nodeCount);
		seqLength = new_seq_length;

		Key [] new_key = new Key [nodeCount];
		System.arraycopy (key, 0, new_key, 0, nodeCount);
		key = new_key;
	}
}
