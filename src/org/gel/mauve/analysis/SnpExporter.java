package org.gel.mauve.analysis;

import java.io.BufferedWriter;
import java.io.IOException;

import org.gel.mauve.LCB;
import org.gel.mauve.XMFAAlignment;
import org.gel.mauve.XmfaViewerModel;

public class SnpExporter {
	static char[] revtab = initRevTab();
	static public char[] initRevTab(){
			revtab = new char[255];
	        revtab['a'] = 't';
	        revtab['A'] = 'T';
	        revtab['t'] = 'a';
	        revtab['T'] = 'A';
	        revtab['c'] = 'g';
	        revtab['C'] = 'G';
	        revtab['g'] = 'c';
	        revtab['G'] = 'C';
	        revtab['r'] = 'y';
	        revtab['R'] = 'Y';
	        revtab['y'] = 'r';
	        revtab['Y'] = 'R';
	        revtab['k'] = 'm';
	        revtab['K'] = 'M';
	        revtab['m'] = 'k';
	        revtab['M'] = 'K';
	        revtab['s']='s';
	        revtab['S']='S';
	        revtab['w']='w';
	        revtab['W']='W';
	        revtab['b'] = 'v';
	        revtab['B'] = 'V';
	        revtab['v'] = 'b';
	        revtab['V'] = 'B';
	        revtab['d'] = 'h';
	        revtab['D'] = 'H';
	        revtab['h'] = 'd';
	        revtab['H'] = 'D';
	        revtab['n']='n';
	        revtab['N']='N';
	        revtab['x']='x';
	        revtab['-']='-';
	        return revtab;
	}
	static public char revLookup(byte b)
	{
		return revtab[b];
	}
	static char[] lowertab = initLowerTab();
	static public char[] initLowerTab(){
			lowertab= new char[255];
			lowertab['a'] = 'a';
			lowertab['A'] = 'a';
			lowertab['t'] = 't';
			lowertab['T'] = 't';
			lowertab['c'] = 'c';
			lowertab['C'] = 'c';
			lowertab['g'] = 'g';
			lowertab['G'] = 'g';
			lowertab['r'] = 'r';
			lowertab['R'] = 'r';
			lowertab['y'] = 'y';
			lowertab['Y'] = 'y';
			lowertab['k'] = 'k';
			lowertab['K'] = 'k';
			lowertab['m'] = 'm';
			lowertab['M'] = 'm';
			lowertab['s']='s';
			lowertab['S']='s';
			lowertab['w']='w';
			lowertab['W']='w';
			lowertab['b'] = 'b';
			lowertab['B'] = 'b';
			lowertab['v'] = 'v';
			lowertab['V'] = 'v';
			lowertab['d'] = 'd';
			lowertab['D'] = 'd';
			lowertab['h'] = 'h';
			lowertab['H'] = 'h';
			lowertab['n']='n';
			lowertab['N']='n';
			lowertab['x']='x';
			lowertab['-']='-';
	        return lowertab;
	}
	static public char lowerLookup(byte b)
	{
		return lowertab[b];
	}
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
			output.write(Integer.valueOf(seqI+1).toString());
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
				char b = 0;			
				boolean poly = false;
				for( int seqI = 0; seqI < seq_count; seqI++ )
				{
					byte c = bytebuf[seqI][colI];
					if( c == '-' )
						continue;
					if( b == 0 )
						b = lowerLookup(c);
					if( b != lowerLookup(c) )
						poly = true;
				}
				if( b != 0)
					cc++;	// don't count all-gap columns
				if(!poly)
					continue;
				
				// if so, then write out the polymorphic site...
				// first determine which seq is reference
				long [] seq_offsets = new long[seq_count];
				boolean [] gap = new boolean[seq_count];
				xmfa.getColumnCoordinates (model, ivI, cc-1, seq_offsets, gap);
				int refseq = model.getReference().getSourceIndex();
				int lcbi = model.getLCBIndex(model.getGenomeBySourceIndex(refseq), seq_offsets[refseq]);
				// don't use refseq if it's not aligned at the polymorphic site
				while(lcbi >= xmfa.getSourceLcbList().length){
					refseq = (refseq + 1) % model.getSequenceCount();
					lcbi = model.getLCBIndex(model.getGenomeBySourceIndex(refseq), seq_offsets[refseq]);
				}
				boolean rev = xmfa.getSourceLcbList()[lcbi].getReverse(model.getReference());

				StringBuilder sb = new StringBuilder();
				for( int seqI = 0; seqI < seq_count; seqI++ )
				{
					if(rev)
						sb.append(revLookup(bytebuf[seqI][colI]));
					else
						sb.append((char)bytebuf[seqI][colI]);
				}
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
