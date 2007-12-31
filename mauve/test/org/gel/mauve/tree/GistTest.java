package org.gel.mauve.tree;

import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;

import junit.framework.TestCase;

public class GistTest extends TestCase {
	
	
	//TODO: Actually run this test!
	void testGIST(GISTree[][] gis_tree, RandomAccessFile xmfa_file){
		// test the GIST to ensure it maintains proper element
		// ordering through a series of queries
		java.util.Random randy = new java.util.Random();
		int iter;
		for( int searchI = 0; searchI < 10000; searchI++ ){
			long search_index = 0;
			if( randy.nextInt() % 2 == 1 ){
				search_index = randy.nextInt() % gis_tree[0][0].length();
				iter = gis_tree[0][0].find( search_index );
			}else{
				search_index = randy.nextInt() % gis_tree[0][0].sequenceLength();
				iter = gis_tree[0][0].find_seqindex( search_index );
			}
		}
		
		// now that we've searched the tree many times, write the 
		// sequence back out for comparison
		int node_count = (int)nodeCount(gis_tree[0][0]);
		System.out.println("node count: " + node_count);
		System.out.println("seq length: " + gis_tree[0][0].sequenceLength() );
		System.out.println("total length: " + gis_tree[0][0].length() );
		int iters = 0;
		try{
		File my_outtie = new File( "gist_test.txt" );
		FileWriter out_writer = new FileWriter( my_outtie );
		iter = gis_tree[0][0].find(0);
		while( true ){
			FileKey fk = null;
			GapKey gk = null;
			iters++;
			try{
				fk = (FileKey)gis_tree[0][0].getKey(iter);
			}catch( ClassCastException cca ){
				// if we started in a gap the next one should be a FileKey
				try{
					gk = (GapKey)gis_tree[0][0].getKey(iter);
				}catch( ClassCastException ccb ){
					throw new RuntimeException(ccb);
				}
			}
			if( fk != null ){
				// write out the sequence
			    // TODO: What if FLength is too long?
				byte[] raw_buf = new byte[ (int)fk.getFLength() ];
				xmfa_file.seek( fk.getOffset() );
				xmfa_file.read( raw_buf, 0, (int)fk.getFLength() );
				int seqI = 0;
				// count the seq length minus newlines
				for( int rawI = 0; rawI < raw_buf.length; rawI++ )
					if( raw_buf[ rawI ] != '\r' && raw_buf[ rawI ] != '\n' )
						seqI++;
				// extract the sequence without newlines
				char[] seq_buf = new char[ seqI ];
				seqI = 0;
				for( int rawI = 0; rawI < raw_buf.length; rawI++ )
					if( raw_buf[ rawI ] != '\r' && raw_buf[ rawI ] != '\n' )
						seq_buf[ seqI++ ] = (char)raw_buf[ rawI ];
				// write the sequence without newlines
				out_writer.write( seq_buf );
			}
			if( gk != null ){
				// write out some gaps
				char[] gap_buf = new char[ (int)gk.getLength() ];
				for( int gapI = 0; gapI < gap_buf.length; gapI++ )
					gap_buf[ gapI ] = '-';
				out_writer.write( gap_buf );
			}

	        Object iterNext = gis_tree[0][0].increment(iter);
			if(iterNext == null)
				break;
		}
		out_writer.flush();
		out_writer.close();
		}catch( Exception e ){
			e.printStackTrace();
		}
		System.out.println( "iters: " + iters );
		node_count = (int)nodeCount(gis_tree[0][0]);
		System.out.println("node count: " + node_count);
		System.out.println("seq length: " + gis_tree[0][0].sequenceLength() );
		System.out.println("total length: " + gis_tree[0][0].length() );
	}

	/** counts the number of nodes in the subtree below x (recursive) */
	long countNodes( GistNode x ) {
		if( x == null )
			return 0;
		return countNodes( x.getLeft() ) + countNodes( x.getRight() ) + 1;
	}

	/**
	 * Returns number of nodes in this tree
	 */
	public long nodeCount(GISTree gist){
		
		return gist.rootIndex == TreeStore.NULL_REF ? 0 : countNodes(GistNode.loadNode(gist.rootIndex, gist));
	}

	/**
	 * validate that recorded subtree lengths and node lengths are consistent
	 */
	long checkNodeLengths( GistNode cur_node ){
		if( cur_node == null )
			return 0;

		long left_len = cur_node.getLeft() != null ? cur_node.getLeft().getLength() : 0;
		long right_len = cur_node.getRight() != null ? cur_node.getRight().getLength() : 0;
		// do left_len and right_len match the actual subtree lengths?
		if( left_len != checkNodeLengths( cur_node.getLeft() ) )
			System.err.println( "freakout\n" );
		if( right_len != checkNodeLengths( cur_node.getRight() ) )
			System.err.println( "freakout\n" );
		// do they all sum up to the correct value?
		if( left_len + right_len + cur_node.getKey().getLength() != cur_node.getLength() ){
			System.err.println( "freakout\n" );
			System.err.println(cur_node.getKey());
		}
		return cur_node.getLength();
	}

	/**
	 * validate that the recorded sequence length and actual sequence length
	 * below a node are consistent with each other
	 */
	long checkNodeSeqLengths( GistNode cur_node ){
		if( cur_node == null )
			return 0;

		long left_len = cur_node.getLeft() != null ? cur_node.getLeft().getSeqLength() : 0;
		long right_len = cur_node.getRight() != null ? cur_node.getRight().getSeqLength() : 0;
		// do left_len and right_len match the actual subtree lengths?
		if( left_len != checkNodeSeqLengths( cur_node.getLeft() ) )
			System.err.println( "freakout\n" );
		if( right_len != checkNodeSeqLengths( cur_node.getRight() ) )
			System.err.println( "freakout\n" );
		// do they all sum up to the correct value?
		if( left_len + right_len + cur_node.getKey().getSeqLength() != cur_node.getSeqLength() ){
			System.err.println( "freakout\n" );
			System.err.println(cur_node.getKey());
		}
		return cur_node.getSeqLength();
	}

	/** 
	 * validate the structure of the tree, e.g. parents and children 
	 * are linked correctly
	 */
	void checkTree( GistNode cur_node ){
		if( cur_node != null ){
			if( cur_node.getLeft() != null && !nodesEqual(cur_node.getLeft().getParent(), cur_node))
				System.err.println( "freakout\n" );
			if( cur_node.getRight() != null && !nodesEqual(cur_node.getRight().getParent(), cur_node))
				System.err.println( "freakout\n" );
			checkTree( cur_node.getLeft() );
			checkTree( cur_node.getRight() );
		}
	}
	
	/**
	 * check the tree for agreement between tree data structures and the
	 * stored intervals
	 */
	void checkTree(GISTree gist) {
		
		GistNode root = GistNode.loadNode(gist.rootIndex, gist);
		
		checkTree( root );
		checkNodeLengths( root );
		checkNodeSeqLengths( root );
	}
	
    
    private boolean nodesEqual(GistNode node1, GistNode node2)
    {
        if (node1 == null) return node2 == null;
        
        return node1.index == node2.index;
    }
	
}
