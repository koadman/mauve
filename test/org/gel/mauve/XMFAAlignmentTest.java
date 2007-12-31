package org.gel.mauve;

import java.io.IOException;


public class XMFAAlignmentTest
{
	/**
	 * Checks the XMFA file to ensure that every nucleotide in the 
	 * relevant sequences are represented exactly once
	 * @return true if the parsed XMFA is valid
	 */
	boolean validateXMFA(XMFAAlignment xmfa) throws IOException {
	    //FIXME: Return this test to usability.
	    return false;
//		boolean valid = true;
//		LCB[] m_lcb_list = new LCB[ xmfa.intervals.length ];
//		for( int ivI = 0; ivI < xmfa.intervals.length; ivI++ ){
//			LCB lcb = new LCB();
//			lcb.left_end = new long[ xmfa.intervals[ ivI ].starts.length ];
//			lcb.right_end = new long[ xmfa.intervals[ ivI ].starts.length ];
//			lcb.reverse = new boolean[ xmfa.intervals[ ivI ].starts.length ];
//			System.arraycopy( xmfa.intervals[ ivI ].starts, 0, lcb.left_end, 0, lcb.left_end.length );
//			System.arraycopy( xmfa.intervals[ ivI ].lengths, 0, lcb.right_end, 0, lcb.right_end.length );
//			m_lcb_list[ ivI ] = lcb;
//		}
//
//		for( int seqI = 0; seqI < xmfa.seq_count; seqI++ ){
//			// sort the lcb list on the current sequence
//			LcbStartComparator lcb_comp = xmfa.new LcbStartComparator( seqI );
//			Arrays.sort( m_lcb_list, lcb_comp );
//			long cur_coord = 0;
//			for( int lcbI = 0; lcbI < m_lcb_list.length; lcbI++ ){
//				if( m_lcb_list[ lcbI ].left_end[ seqI ] == 0 )
//					continue;	// this LCB isn't defined for this seq, skip it
//				if( cur_coord != m_lcb_list[ lcbI ].left_end[ seqI ] - 1 ){
//					System.out.println( "Missing " + cur_coord + " from seq " + seqI );
//					System.out.println( "m_lcb_list[ lcbI ].left_end[ seqI ] " + m_lcb_list[ lcbI ].left_end[ seqI ] );
//					System.out.println();
//					valid = false;
//				}
//				cur_coord = m_lcb_list[ lcbI ].right_end[ seqI ];
//			}
//		}
//		
//// special test case:
//		int debug_val = 3277;
//		long lento = xmfa.gis_tree[ debug_val ][ 1 ].length();
//		
//		xmfa.readRawSequence( debug_val, 1, 0, lento );
//
//
//		//
//		// read each lcb and verify that it has the correct number of characters
//		//
//		for( int ivI = 0; ivI < xmfa.intervals.length; ivI++ ){
//			for( int seqI = 0; seqI < xmfa.seq_count; seqI++ ){
//				if( ivI == debug_val )
//					System.out.print("");
//				long seq_len = xmfa.intervals[ ivI ].lengths[ seqI ] - xmfa.intervals[ ivI ].starts[ seqI ] + 1;
//				if( xmfa.intervals[ ivI ].lengths[ seqI ] == 0 && xmfa.intervals[ ivI ].starts[ seqI ] == 0 )
//					seq_len = 0;
//				long len = xmfa.gis_tree[ ivI ][ seqI ].length();
//				if( seq_len != xmfa.gis_tree[ ivI ][ seqI ].sequenceLength() ){
//					System.out.println( "Error: conflicting interval and gis_tree sequence length records" );
//					System.out.println( "At interval: " + ivI + " sequence: " + seqI + " iv seqlen: " + 
//								seq_len + " gis_tree len: " + xmfa.gis_tree[ ivI ][ seqI ].sequenceLength() );
//					System.out.println( "left end: " + xmfa.intervals[ ivI ].starts[ seqI ] + " right end: " +
//					        xmfa.intervals[ ivI ].lengths[ seqI ] );
//					valid = false;
//				}
//				byte[] buf = (byte[])xmfa.readRawSequence( ivI, seqI, 0, len );
//				long char_count = 0;
//				for( int bufI = 0; bufI < buf.length; bufI++ )
//					if( buf[ bufI ] != '\r' && buf[ bufI ] != '\n' && buf[ bufI ] != '-' )
//						char_count++;
//				if( char_count != seq_len ){
//					System.out.println( "Error in interval " + ivI + " seq: " + seqI +
//						"\nExpected " + seq_len + " chars but read " + char_count );
//					System.out.println( "left end: " + xmfa.intervals[ ivI ].starts[ seqI ] + " right end: " +
//					        xmfa.intervals[ ivI ].lengths[ seqI ] );
//					valid = false;
//				}
//			}
//		}
//		
//		if( valid )
//			System.out.println( "XMFA validated\n" );
//		return valid;
	}
	
	


}
