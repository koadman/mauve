package org.gel.mauve.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;
import org.gel.mauve.LCB;
import org.gel.mauve.XMFAAlignment;
import org.gel.mauve.XmfaViewerModel;

public class SnpExporter {
	public static char[] revtab = initRevTab();
	static public char[] initRevTab(){
			revtab = new char[255];
			for(byte i=0; i<127; i++){
					revtab[i]=(char)i;
			}
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
			String seq = "sequence_"+Integer.valueOf(seqI+1).toString()+"_";
			output.write("\t"+seq+"Contig\t"+seq+"PosInContg\t"+seq+"GenWidePos");
			output.write(Integer.valueOf(seqI+1).toString());
		}
		output.write("\n");
		
		SNP[] snps = getSNPs(model, xmfa);
		for (int i = 0; i < snps.length; i++){
			output.write(snps[i].toString()+"\n");
		}
        output.flush(); // don't forget to flush!
	}
	
	public static void exportGaps(XmfaViewerModel model, XMFAAlignment xmfa, BufferedWriter output) throws IOException {
		// Genome  Contig   Position_in_Contig   GenomeWide_Position   Length
		Gap[][] gaps = getGaps(model);
		output.write("Genome\tContig\tPosition_in_Contig\tGenomeWide_Position\tLength\n");
		for (Gap[] ar: gaps)
			for (Gap g: ar)
				output.write(g.toString()+"\n");
        output.flush(); // don't forget to flush!
	}
	
	public static SNP[] getSNPs(XmfaViewerModel model){
		return getSNPs(model, model.getXmfa());
	}
	
	public static SNP[] getSNPs( XmfaViewerModel model, XMFAAlignment xmfa){
		return getSNPs(model,xmfa,xmfa.getSourceLcbList());
	}
	
	public static SNP[] getSNPs( XmfaViewerModel model, XMFAAlignment xmfa, LCB[] lcbs){
	//	int iv_count = (int)model.getLcbCount();
		int seq_count = model.getSequenceCount();
		Vector<SNP> snps = new Vector<SNP>();
		
		for(int i = 0;i  < lcbs.length; i++ )
		{
			int ivI = lcbs[i].id;
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
				Genome g = model.getReference();
				int refseq = g.getSourceIndex();
				int lcbi = model.getLCBIndex(model.getGenomeBySourceIndex(refseq), seq_offsets[refseq]);
				// don't use refseq if it's not aligned at the polymorphic site
				while(lcbi >= xmfa.getSourceLcbList().length){
					refseq = (refseq + 1) % model.getSequenceCount();
					lcbi = model.getLCBIndex(model.getGenomeBySourceIndex(refseq), seq_offsets[refseq]);
				}
				boolean rev = xmfa.getSourceLcbList()[lcbi].getReverse(model.getReference());
				SNP tmp = new SNP(model);
				StringBuilder sb = new StringBuilder();
				for( int seqI = 0; seqI < seq_count; seqI++ )
				{
					char c = '-';
					if(rev)
						c = revLookup(bytebuf[seqI][colI]);
					else
						c = (char)bytebuf[seqI][colI];

					long pos = 0;
					if(!gap[seqI])
						pos = seq_offsets[seqI];
					tmp.addTaxa(seqI, c, pos);
				}
				snps.add(tmp);
			}
		}
		return snps.toArray(new SNP[snps.size()]);
	}
	
	public static SNP[] getLocalSNPs( XmfaViewerModel model, XMFAAlignment xmfa, LCB lcb){
		LCB[] tmp = new LCB[1];
		tmp[0] = lcb;
		return getSNPs(model,xmfa,tmp);
	}
	
	private static int countGapLen(byte[] seq, int gapStart){
		int len = gapStart;
		while(seq[len] == '-')
			len++;
		return len-gapStart;
	}
	
	public static Chromosome[][] getUniqueChromosomes(XmfaViewerModel model){
		Chromosome[][] ret = new Chromosome[model.getGenomes().size()][];
		for (int i = 0; i < ret.length; i++){
			ret[i] = getUniqueChromosomes(model,model.getGenomeBySourceIndex(i));
		}
		return ret;
	}
	
	public static Chromosome[] getUniqueChromosomes(XmfaViewerModel model, Genome g){
		return getUniqueChromosomes(model, model.getXmfa(), model.getGenomes().toArray(new Genome[model.getGenomes().size()]),g);
	}
	
	public static Chromosome[] getUniqueChromosomes(XmfaViewerModel model, XMFAAlignment xmfa, Genome[] set, Genome g){
		if (g.getSourceIndex()==1) {
			System.out.println(g.getDisplayName());
		}
		Pattern p = Pattern.compile("Scaffold5.12[56][0-9]");
		List<Chromosome> chroms = g.getChromosomes();
		Iterator<Chromosome> it = chroms.iterator();
		Vector<Chromosome> ret = new Vector<Chromosome>();
		while(it.hasNext()){
			Chromosome curr = it.next();
		//	if (p.matcher(curr.getName()).matches()){
		//		System.out.println(curr.getName());
		//	} 
			boolean unique = true;
			long i = curr.getStart();
			long[] coords = new long[model.getGenomes().size()];
			boolean[] gap = new boolean[model.getGenomes().size()];
			for (; i <= curr.getEnd() && unique; i++){
				long[] loc = model.getLCBAndColumn(g, i);
				int lcbId = (int)loc[0];
				model.getColumnCoordinates(lcbId, loc[1], coords, gap);
				unique = unique && isUnique(gap,set,g.getSourceIndex());
			}
			if (gap[1])
				System.out.print("");
			if (unique) 
				ret.add(curr);
		}
		return ret.toArray(new Chromosome[ret.size()]);
	}
	
	private static boolean isUnique(boolean[] gap, Genome[] set, int g){
		if (gap[g]) 
			return false;
		boolean allGap = true;
		for (int i = 0; i < set.length && allGap; i++){
			if (set[i].getSourceIndex() == g) 
				continue;
			allGap = allGap && gap[set[i].getSourceIndex()];
		}
		return allGap;
	}
	
	@SuppressWarnings("unchecked")
	public static Gap[][] getGaps(XmfaViewerModel model){
		XMFAAlignment xmfa = model.getXmfa();
		int iv_count = (int)model.getLcbCount();
		int seq_count = model.getSequenceCount();

		Vector[] gaps = new Vector[seq_count];
		 for (int i = 0; i < gaps.length; i++)
			gaps[i] = new Vector();
		
		LCB[] lcbs = xmfa.getSourceLcbList();
		LCB[] tmpLCBs = new LCB[lcbs.length];
		System.arraycopy(lcbs, 0, tmpLCBs, 0, lcbs.length);
		
		
		Genome[] genomes = (Genome[]) model.getGenomes().toArray(new Genome[seq_count]); 
		
		for (int genI = 0; genI < genomes.length; genI++){
			Genome g = genomes[genI];

			// First we need to get intra-LCB gaps
			for (int lcbI = 0; lcbI < lcbs.length; lcbI++){
				
				int lcbLen = (int) xmfa.getLcbLength(lcbI);
				byte[] tmp = xmfa.readRawSequence(lcbI, genI, 0, lcbLen);
				tmp = XMFAAlignment.filterNewlines(tmp);
				long[] seq_offset = new long[genomes.length];
				boolean[] gap = new boolean[genomes.length];
				
				int colI = 0;
				while (colI < tmp.length){
					if (tmp[colI]=='-'){
						long start = colI;
						// this will move colI to the position immediately following the gap
						while(tmp[colI]=='-'){
							if (colI == tmp.length-1) {
								colI++;
								break;
							} else 
								colI++;
						}
						long end = colI-1;
						long len = end - start + 1;
						if (lcbs[lcbI].getReverse(g))
							xmfa.getColumnCoordinates(model, lcbI, end+1, seq_offset, gap);
						else
							xmfa.getColumnCoordinates(model, lcbI, start-1, seq_offset, gap);
						
						gaps[genI].add(new Gap(genI,lcbI,seq_offset[genI],len, model));	
					} else	
						colI++;
				}	
			}
			
			// Now we need to get inter-LCB gaps
			
			// sort based on left-end positions
			Arrays.sort(tmpLCBs, Segment.getGenPositionComparator(genI));
			
			for (int lcbI = 0; lcbI < tmpLCBs.length-1; lcbI++){
				LCB lcb1 = tmpLCBs[lcbI];
				LCB lcb2 = tmpLCBs[lcbI+1];
				long dist = lcb2.getLeftEnd(genomes[genI])-lcb1.getRightEnd(genomes[genI])-1;
				if (dist > 0){ // there's gaps in other genomes
					
					for (int genJ = 0; genJ < genomes.length; genJ++){
						if (genJ == genI) 
							continue; 
						else {
							gaps[genJ].add(new Gap(genJ, -1,
									lcb1.getRightEnd(genomes[genJ]), dist, model));
						}
					}
				}
			}
			
			
			
		}
		
		Gap[][] ret = new Gap[gaps.length][];
		for (int i = 0; i < ret.length; i++){
			mergeAdjacentGaps(gaps[i]);
			ret[i] = (Gap[]) gaps[i].toArray(new Gap[gaps[i].size()]);
		}
		return ret;
	}
	
	/**
	 * Merges gaps that begin at the same position.
	 * If two Gaps begin at the same position,
	 * they are merged to create a new Gap object,
	 * removed, and replaced with the resulting new Gap.
	 * 
	 * @param v list of gaps to merge (if necessary)
	 */
	private static void mergeAdjacentGaps(Vector<Gap> v){
		// sort Gaps by their position in alignment
		Collections.sort(v, Gap.getAlnmtPosComparator());
		
		int gapI = 0;
		while (gapI < v.size()-1){
			if (v.get(gapI).getPosition() == v.get(gapI+1).getPosition()){
		//		System.err.print("\nMerging gap of size " + v.get(gapI).getLength() +
		//				" with a gap of size " + v.get(gapI+1).getLength() + ". ");
				
				Gap merge = Gap.mergeGaps(v.get(gapI), v.get(gapI+1));
		//		System.err.println("Created gap of size " + merge.getLength());
				v.remove(gapI);
				v.remove(gapI);
				v.insertElementAt(merge, gapI);
			} else {
				gapI++;
			}
		}
	}
	
	/**
	 * Returns a 4x4 matrix of counts of substitution types between 
	 * genome <code>src_i</code> and <code>src_j</code>
	 * 
	 * <code>
	 *      A  C  T  G
	 *    A -
	 *    C    -
	 *    T       -
	 *    G          -
	 * </code>
	 * 
	 * @param model <code>XmfaViewerModel</code> holding alignment data
	 * @param src_i source index of a genome 
	 * @param src_j source index of a genome 
	 * @return a 4x4 matrix of substitution counts
	 */
	public static int[][] countSubstitutions(XmfaViewerModel model, int src_i, int src_j){
		int[][] subs = new int[4][4];
		
		SNP[] snps = getSNPs(model, model.getXmfa(), model.getXmfa().getSourceLcbList());
		
		for (int k = 0; k < snps.length; k++){ 
			char c_i = snps[k].getChar(src_i);
			char c_j = snps[k].getChar(src_j);
			int b_i = getBaseIdx(c_i);
			int b_j = getBaseIdx(c_j);
			if (b_i != b_j && b_i > 0 && b_j > 0)
				subs[b_i][b_j]++;
		}
		return subs;
	}
	
	private static int getBaseIdx(char c){
		switch(c){
		  case 'a': return 0; 
		  case 'A': return 0;
		  case 'c': return 1;
		  case 'C': return 1;
		  case 't': return 2;
		  case 'T': return 2;
		  case 'g': return 3;
	 	  case 'G': return 3;
		  default: return -1;
		}
	}
	
	public static void main(String[] args) {
		Options opts = new Options();
		opts.addOption( OptionBuilder
			.withArgName("alignment file")
			.hasArg()
			.withDescription("A required parameter specifying an XMFA format file generated by progressiveMauve")
			.isRequired()
			.create('f')
			);

		opts.addOption( OptionBuilder
				.withArgName("output file name")
				.hasArg()
				.withDescription("The name of the output file for the homologs")
				.isRequired()
				.create('o')
				);

		CommandLineParser parser = new GnuParser();
		CommandLine line=null;
		try{
			line = parser.parse( opts, args );
		}catch(org.apache.commons.cli.ParseException pe){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( 
					"SnpExporter -f <XMFA alignment input> -o <SNP output>", opts);
			
			throw new RuntimeException("There was an error parsing your command-line options.  Please check them and try again.");
		}
		String alnmtFilePath = line.getOptionValue('f');
		String outputFilePath = line.getOptionValue('o');
		
		try {
			System.out.print("Loading alignment file...");
			XmfaViewerModel model = new XmfaViewerModel(new File(alnmtFilePath), null);
			System.out.println("Exporting SNPs...");
			model.setReference(model.getGenomeBySourceIndex(0));
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath));
			export(model, model.getXmfa(), bw);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
	}

}
