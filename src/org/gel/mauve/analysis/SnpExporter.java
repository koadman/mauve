package org.gel.mauve.analysis;

import java.io.BufferedWriter;
import java.io.IOException;

import org.gel.mauve.XMFAAlignment;
import org.gel.mauve.XmfaViewerModel;

public class SnpExporter {
	public static void export( XmfaViewerModel model, XMFAAlignment xmfa, BufferedWriter output ) throws IOException
	{
		int iv_count = (int)model.getLcbCount();
		int seq_count = model.getSequenceCount();

		// write the header
		// then extract and write out all snps
		output.write("SNP pattern");
		for( int seqI = 0; seqI < seq_count; seqI++ )
		{
			output.write("\tsequence_");
			output.write(seqI+1);
		}
		output.write("\n");
		
		for(int ivI = 0; ivI < iv_count; ivI++ )
		{
			int iv_length = (int)xmfa.getLcbLength(ivI);
			byte[][] bytebuf = new byte[seq_count][iv_length];
			for( int seqI = 0; seqI < seq_count; seqI++ )
			{
				byte[] tmp = xmfa.readRawSequence (ivI, seqI, 0, iv_length);
				tmp = XMFAAlignment.filterNewlines(tmp);
				System.arraycopy(tmp, 0, bytebuf[seqI], 0, iv_length);
			}
			int cc = 0;
			for(int colI = 0; colI < iv_length; colI++)
			{
				// first check whether the column contains a polymorphic site
				byte b = 0;			
				boolean poly = false;
				for( int seqI = 0; seqI < seq_count; seqI++ )
				{
					byte c = bytebuf[seqI][colI];
					if( c == '-' )
						continue;
					if( b == 0 )
						b = c;
					if( b != c )
						poly = true;
				}
				if( b != 0)
					cc++;	// don't count all-gap columns
				if(!poly)
					continue;
				
				// if so, then write out the polymorphic site
				StringBuilder sb = new StringBuilder();
				for( int seqI = 0; seqI < seq_count; seqI++ )
				{
					sb.append((char)bytebuf[seqI][colI]);
				}
				long [] seq_offsets = new long[seq_count];
				boolean [] gap = new boolean[seq_count];
				xmfa.getColumnCoordinates (model, ivI, cc-1, seq_offsets, gap);
				output.write(sb.toString());
				String zero = "0";
				for( int seqI = 0; seqI < seq_count; seqI++ )
				{
					output.write('\t');
					if(gap[seqI])
						output.write(zero);
					else
						output.write(Long.toString(seq_offsets[seqI]));
				}
				output.write("\n");
			}
		}
		output.flush();
	}
}
