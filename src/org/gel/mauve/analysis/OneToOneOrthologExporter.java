package org.gel.mauve.analysis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.LocationTools;
import org.gel.mauve.Genome;
import org.gel.mauve.GenomeBuilder;
import org.gel.mauve.LCB;
import org.gel.mauve.SimilarityIndex;
import org.gel.mauve.XMFAAlignment;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.backbone.Backbone;
import org.gel.mauve.backbone.BackboneList;

class CDS implements Comparable
{
	public CDS( int l, int r, int s, String ltag){ left = l; right = r; strand = s; locus = ltag; };
	public int compareTo(Object o)
	{
		CDS c = (CDS)o;
		if(left < c.left)
			return -1;
		else if(left > c.left)
			return 1;
		if(right < c.right)
			return -1;
		else if(right > c.right)
			return 1;
		if(strand < c.strand)
			return -1;
		else if(strand > c.strand)
			return 1;
		return 0;
	}
	int left;
	int right;
	int strand;
	String locus;
};

public class OneToOneOrthologExporter {

	private static CDS[] getCDS(Iterator i)
	{
		Vector cds = new Vector();
		while(i.hasNext())
		{
			StrandedFeature f = (StrandedFeature)i.next();
			if(f.getType().equalsIgnoreCase(GenomeBuilder.MAUVE_AGGREGATE))
			{
				CDS[] d = getCDS(f.features());	// this is an aggregate, so recurse
				for(int dI = 0; dI < d.length; dI++)
					cds.add(d[dI]);
				continue;
			}
			if(!f.getType().equalsIgnoreCase("CDS"))
				continue;
			int l = f.getLocation().getMin();
			int r = f.getLocation().getMax();
			Object ltag = f.getAnnotation().getProperty("locus_tag");
			if(ltag==null) ltag = new String();
			if(l > r)
			{
				int t = l;
				l = r;
				r = t;
			}
			cds.add(new CDS(l, r, f.getStrand() == StrandedFeature.POSITIVE ? 1 : -1, ltag.toString()));
		}
		Collections.sort(cds);
		// remove dupes
		for(int j = 1; j < cds.size(); j++)
		{
			CDS cj = (CDS)cds.elementAt(j);
			CDS cjm1 = (CDS)cds.elementAt(j-1);
			if(cj.compareTo(cjm1) == 0 && cj.locus.equalsIgnoreCase(cjm1.locus))
			{
				cds.remove(j);
				j--;
			}
		}
		CDS[] c = new CDS[cds.size()];
		return (c = (CDS[])cds.toArray(c));
	}
	private static class Quad {
		public Quad(long[] l, long[] r)
		{ 
			left = new long[l.length];
			right = new long[r.length];
			System.arraycopy(l, 0, left, 0, l.length);
			System.arraycopy(r, 0, right, 0, r.length);
			for(int i = 0; i < l.length; i++)
			{
				left[i] = left[i] < 0 ? -left[i] : left[i];
				right[i] = right[i] < 0 ? -right[i] : right[i];
				if(left[i] > right[i])
				{
					long t = right[i];
					right[i] = left[i];
					left[i] = t;
				}
			}
		}
		long[] left;
		long[] right;
	}

	private static Quad[] getBackboneSegs(XmfaViewerModel model, Genome g_i, CDS cds)
	{
		XMFAAlignment xmfa = model.getXmfa();
		// extract the alignment of the region in question
		BackboneList bbl = model.getBackboneList();
		int cur = cds.left;
		int max = cds.right;
		boolean[] gap = new boolean[model.getSequenceCount()];
		long[] tmp = new long[model.getSequenceCount()];
		Vector bbsegs = new Vector();
		LCB[] lcbs = model.getFullLcbList();
		while(cur <= max)
		{
			Backbone bcur = bbl.getBackbone(g_i, cur);
			if(bcur == null)
			{	// check whether we've fallen outside backbone
				bcur = bbl.getNextBackbone(g_i, cur);
				cur = (int)(bcur.getLeftEnd(g_i));
				continue;
			}
			Quad q = new Quad(bcur.starts, bcur.ends);
			if(q.left[g_i.getSourceIndex()] < cds.left)
			{
				long[] leftPos = xmfa.getLCBAndColumn(g_i, cds.left);
				xmfa.getColumnCoordinates(model, (int)leftPos[0], leftPos[1], tmp, gap);
				// check that it's still in-range
				for(int ii = 0; ii < gap.length; ii++)
				{
					if(lcbs[bcur.getLcbIndex()].reverse[g_i.getSourceIndex()] == lcbs[bcur.getLcbIndex()].reverse[ii])
						q.left[ii] = tmp[ii];
					else
						q.right[ii] = tmp[ii];
					if(q.left[ii] < Math.abs(bcur.starts[ii]) || q.left[ii] > Math.abs(bcur.ends[ii]))
					{
						q.left[ii] = 0;
						q.right[ii] = 0;
					}
				}
			}
			if(q.right[g_i.getSourceIndex()] > cds.right)
			{
				long[] rightPos = xmfa.getLCBAndColumn(g_i, cds.right);
				xmfa.getColumnCoordinates(model, (int)rightPos[0], rightPos[1], tmp, gap);
				// check that it's still in-range
				for(int ii = 0; ii < gap.length; ii++)
				{
					if(lcbs[bcur.getLcbIndex()].reverse[g_i.getSourceIndex()] == lcbs[bcur.getLcbIndex()].reverse[ii])
						q.right[ii] = tmp[ii];
					else
						q.left[ii] = tmp[ii];
					if(q.right[ii] < Math.abs(bcur.starts[ii]) || q.right[ii] > Math.abs(bcur.ends[ii]) || q.left[ii] == 0)
					{
						q.left[ii] = 0;
						q.right[ii] = 0;
					}
				}
			}
			bbsegs.add(q);
			cur = (int)(q.right[g_i.getSourceIndex()]) + 1;
		}
		Quad[] bbs = new Quad[bbsegs.size()];
		bbs = (Quad[])bbsegs.toArray(bbs);
		return bbs;
	}

	static class CdsOverlap
	{
		int length_i;
		int length_j;
		int left_i;
		int right_i;
	}
	private static void addOverlappingCDS(XmfaViewerModel model, CDS[] cds, Quad bb, HashMap hm, int gI, int gJ)
	{
		// this is really a awful search, need a stabbing query instead!
		for(int bs = 0; bs < cds.length; bs++)
		{
			if(!(bb.left[gJ] < cds[bs].right && bb.right[gJ] > cds[bs].left))
				continue;	// no overlap
			// this one overlaps.  compute pct identity
			long lefto = bb.left[gJ] > cds[bs].left ? bb.left[gJ] : cds[bs].left;
			long righto = bb.right[gJ] < cds[bs].right ? bb.right[gJ] : cds[bs].right;
			// get alignment columns
			long[] lblob = model.getXmfa().getLCBAndColumn(model.getGenomeBySourceIndex(gJ), lefto);
			long[] rblob = model.getXmfa().getLCBAndColumn(model.getGenomeBySourceIndex(gJ), righto);
			long[] loffsets = new long[model.getSequenceCount()];
			boolean[] lgaps = new boolean[model.getSequenceCount()];
			long[] roffsets = new long[model.getSequenceCount()];
			boolean[] rgaps = new boolean[model.getSequenceCount()];
			model.getXmfa().getColumnCoordinates(model, (int)lblob[0], lblob[1], loffsets, lgaps);
			model.getXmfa().getColumnCoordinates(model, (int)rblob[0], rblob[1], roffsets, rgaps);
			for(int i = 0; i < loffsets.length; i++)
			{
				if(loffsets[i] > roffsets[i])
				{
					long tmp = loffsets[i];
					loffsets[i] = roffsets[i];
					roffsets[i] = tmp;
				}
			}
			CdsOverlap co = new CdsOverlap();
			co.left_i = (int)(loffsets[gI] > bb.left[gI] ? loffsets[gI] : bb.left[gI]);	// deal with a rare off-by-one the bad way.
			co.right_i = (int)(roffsets[gI] < bb.right[gI] ? roffsets[gI] : bb.right[gI]);
			if(co.left_i > co.right_i)
				continue;	// weird.  no overlap.
			co.length_i = (int)(roffsets[gI] - loffsets[gI]);
			co.length_j = (int)(roffsets[gJ] - loffsets[gJ]);
			Object v = hm.get(new Integer(bs));
			if(v == null)
				v = new Vector();
			((Vector)v).add(co);
			hm.put(new Integer(bs), v);
		}
	}

	public static void export( XmfaViewerModel model, BufferedWriter output ) throws IOException
	{
		// find all annotated orthologs that meet the following criteria:
		float min_conserved_length = 0.7f;
		float max_conserved_length = 1.0f;
		float min_nucleotide_id = 0.6f;
		float max_nucleotide_id = 1.0f;

		// for each annotated CDS in each genome, identify candidate 
		// ortholog CDSes in other genomes
		Vector allCds = new Vector();
		for(int gI = 0; gI < model.getSequenceCount(); gI++)
		{
			Genome g = model.getGenomeBySourceIndex(gI);
            Location loc = LocationTools.makeLocation(1, (int)g.getLength());
			FeatureHolder fh = g.getAnnotationSequence().filter(new FeatureFilter.OverlapsLocation(loc));
			CDS[] cdsi = getCDS(fh.features());
			Arrays.sort(cdsi);
			allCds.add(cdsi);
		}

		// orthoPairs will contain a pairwise mapping of orthologs among each pair of genomes
		// keys are CDS indexes and values are vectors of CDS indices
		HashMap[][] orthoPairs = new HashMap[model.getSequenceCount()][model.getSequenceCount()];
		for(int gI = 0; gI < model.getSequenceCount(); gI++)
			for(int gJ = 0; gJ < model.getSequenceCount(); gJ++)
				orthoPairs[gI][gJ] = new HashMap();
		for(int gI = 0; gI < model.getSequenceCount(); gI++)
		{
			Genome g_i = model.getGenomeBySourceIndex(gI);
			CDS[] cdsi = (CDS[])allCds.elementAt(gI);
			for(int cI = 0; cI < cdsi.length; cI++)
			{
				// extract the alignment of the region in question
				Quad[] bbs;
				bbs = getBackboneSegs(model, g_i, cdsi[cI]);
				Object[] aln = model.getXmfa().getRange(g_i, (long)cdsi[cI].left, (long)cdsi[cI].right);
				// for each of the other genomes, find the CDS that overlap the backbone segs and assess orthology
				for(int gJ = gI+1; gJ < model.getSequenceCount(); gJ++)
				{
					// for each of the bb segs, find overlapping cds
					HashMap hm = new HashMap();
					CDS[] cdsj = (CDS[])allCds.elementAt(gJ);
					if(cI==1116 && gJ == 3 && gI == 1)
						System.err.println("blah");
					for( int bbI = 0; bbI < bbs.length; bbI++)
						addOverlappingCDS(model, cdsj, bbs[bbI], hm, gI, gJ);

					// now for each overlapping CDS that meets the coverage threshold, 
					// compute the percent identity
					Iterator ki = hm.keySet().iterator();
					while(ki.hasNext())
					{
						Integer cdsid = (Integer)ki.next();
						Vector v = (Vector)hm.get(cdsid);
						int cov_i = 0;
						int cov_j = 0;
						int left_min = -1;
						int right_max = -1;
						Iterator viter = v.iterator();
						while(viter.hasNext())
						{
							
							CdsOverlap co = (CdsOverlap)viter.next();
							cov_i += co.length_i;
							cov_j += co.length_j;
							if(co.left_i < left_min || left_min == -1)
								left_min = co.left_i;
							if(co.right_i > right_max || right_max == -1)
								right_max = co.right_i;
						}
						int cons_i = cdsi[cI].right - cdsi[cI].left;
						if(cov_i < min_conserved_length * cons_i ||
								cov_i > max_conserved_length * cons_i)
							continue;	// not enough covered
						int cons_j = cdsj[cdsid.intValue()].right - cdsj[cdsid.intValue()].left;
						if(cov_j < min_conserved_length * cons_j ||
								cov_j > max_conserved_length * cons_j)
							continue;	// not enough covered
						
						// now compute percent id
						int cur = cdsi[cI].left;
						int col = 0;
						byte[] aln_gI = (byte[])aln[gI];
						byte[] aln_gJ = (byte[])aln[gJ];
						while(cur < left_min)
						{
							if(col == aln_gI.length)
							{
								System.err.println("bombers");
							}
							if(aln_gI[col] != '-' && aln_gI[col] != '\n' && aln_gI[col] != '\r')
								cur++;
							col++;
						}
						int id = 0;
						int tot = 0;
						while(cur <= right_max)
						{
							if(col == aln_gI.length)
							{
								System.err.println("bombers");
							}
							if(aln_gI[col] != '-' && aln_gI[col] != '\n' && aln_gI[col] != '\r')
							{
								cur++;
								if(col < aln_gJ.length && aln_gJ[col] != '-' && aln_gJ[col] != '\n' && aln_gJ[col] != '\r')
									tot++;
								if(col < aln_gJ.length && 
										SimilarityIndex.char_map[(char)aln_gI[col]] == SimilarityIndex.char_map[(char)aln_gJ[col]])
									id++;	// Use of char_map should fix upper/lowercase issues
							}
							col++;
						}
						if(id >= min_nucleotide_id * tot && id <= max_nucleotide_id * tot)
						{
							Integer kkk = new Integer(cI);
							Vector vvv = (Vector)orthoPairs[gI][gJ].get(kkk);
							if(vvv == null) vvv = new Vector();
							vvv.add(cdsid);
							orthoPairs[gI][gJ].put(kkk, vvv);
							vvv = (Vector)orthoPairs[gJ][gI].get(cdsid);
							if(vvv == null) vvv = new Vector();
							vvv.add(kkk);
							orthoPairs[gJ][gI].put(cdsid, vvv);
							if(cI > cdsi.length || cdsid.intValue() > cdsj.length)
								System.err.println("bug!!");
						}
					}
				}
			}
		}

		// now that all pairwise orthology relationships have been identified,
		// compute transitive homology
		Vector orthologs = new Vector();
		for(int gI = 0; gI < model.getSequenceCount(); gI++)
		{
			for(int gJ = 0; gJ < model.getSequenceCount(); gJ++)
			{
				if(gI == gJ)
					continue;
				while(orthoPairs[gI][gJ].size() > 0)
				{
					HashSet[] orthos = new HashSet[model.getSequenceCount()];
					for(int i = 0; i < orthos.length; i++)
						orthos[i] = new HashSet();
					Integer key = (Integer)orthoPairs[gI][gJ].keySet().iterator().next();
					Vector val = (Vector)orthoPairs[gI][gJ].get(key);
					orthos[gI].add(key);
					orthos[gJ].addAll(val);
					orthoPairs[gI][gJ].remove(key);
					Stack s = new Stack();
					s.push(new Integer(gI));
					s.push(new Integer(gJ));
					while(!s.isEmpty())
					{
						// perform a depth-first-search of the ortho network for transitive orthologs
						int cur = ((Integer)s.pop()).intValue();
						// collect orthos for each of the other sequences
						for(int i = 0; i < orthos.length; i++)
						{
							Iterator siter = orthos[cur].iterator();
							while(siter.hasNext())
							{
								Object skey = siter.next();
								Object v2 = orthoPairs[cur][i].get(skey);
								if(v2 == null)
									continue;
								Vector vv = (Vector)v2;
								orthoPairs[cur][i].remove(skey);
								CDS[] cdsi = (CDS[])allCds.elementAt(cur);
								CDS[] cdsj = (CDS[])allCds.elementAt(i);
								if(((Integer)skey).intValue() > cdsi.length )
									System.err.println("bug!!");
								for(int x = 0; x < vv.size(); x++)
									if(((Integer)vv.elementAt(x)).intValue() > cdsj.length )
										System.err.println("bug!!");
								int psize = orthos[i].size();
								orthos[i].addAll(vv);
								if( orthos[i].size() > psize )
									s.push(new Integer(i)); // found new orthologs, so continue transitive search
							}
						}
					}
					orthologs.add(orthos);
				}
			}
		}
		
		StringBuilder sb = new StringBuilder();
		for(int oI = 0; oI < orthologs.size(); oI++)
		{
			HashSet[] ortho = (HashSet[])orthologs.elementAt(oI);			
			boolean first = true;
			for(int sI = 0; sI < ortho.length; sI++)
			{
				Iterator iter = ortho[sI].iterator();
				while(iter.hasNext())
				{
					if(!first)	sb.append("\t");
					if(first)	first = !first;
					int osi = ((Integer)iter.next()).intValue();
					CDS[] cdsi = (CDS[])allCds.elementAt(sI);
					if(osi > cdsi.length )
						System.err.println("bug!!");
					sb.append(sI);
					sb.append(":");
					sb.append(cdsi[osi].locus);
					sb.append(":");
					sb.append(cdsi[osi].left);
					sb.append("-");
					sb.append(cdsi[osi].right);
				}
			}
			sb.append("\n");
		}
		output.write(sb.toString());

		// make a list of CDS that have no orthologs, since
		// we will want to write these out too
		Vector found = new Vector();
		for(int sI = 0; sI < model.getSequenceCount(); sI++)
		{
			CDS[] cdsi = (CDS[])allCds.elementAt(sI);
			found.add( new boolean[cdsi.length] );
		}
	
		for(int oI = 0; oI < orthologs.size(); oI++)
		{
			HashSet[] ortho = (HashSet[])orthologs.elementAt(oI);			
			for(int sI = 0; sI < ortho.length; sI++)
			{
				Iterator iter = ortho[sI].iterator();
				while(iter.hasNext())
				{
					int osi = ((Integer)iter.next()).intValue();
					boolean[] f = (boolean[])found.elementAt(sI);
					f[osi] = true;
				}
			}
		}
		// now write out the singletons (not found)
		sb = new StringBuilder();
		for(int sI = 0; sI < model.getSequenceCount(); sI++)
		{
			boolean[] f = (boolean[])found.elementAt(sI);
			CDS[] cdsi = (CDS[])allCds.elementAt(sI);
			for(int fI = 0; fI < f.length; fI++)
			{
				if(f[fI])
					continue;	// this one has orthologs, skip it
				sb.append(sI);
				sb.append(":");
				sb.append(cdsi[fI].locus);
				sb.append(":");
				sb.append(cdsi[fI].left);
				sb.append("-");
				sb.append(cdsi[fI].right);
				sb.append("\n");
			}
		}
		output.write(sb.toString());
	}
}
