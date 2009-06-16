package org.gel.mauve.analysis;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.LocationTools;
import org.gel.mauve.Genome;
import org.gel.mauve.GenomeBuilder;
import org.gel.mauve.LCB;
import org.gel.mauve.MyConsole;
import org.gel.mauve.SimilarityIndex;
import org.gel.mauve.XMFAAlignment;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.backbone.Backbone;
import org.gel.mauve.backbone.BackboneList;
import org.gel.mauve.gui.ExportFrame;
import org.gel.mauve.gui.MauveRenderingHints;
import org.gel.mauve.gui.RearrangementPanel;

class Feature implements Comparable
{
	public Feature( int l, int r, int s, String ltag){ left = l; right = r; strand = s; locus = ltag; };
	public int compareTo(Object o)
	{
		Feature c = (Feature)o;
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

	private static Feature[] getFeature(Iterator i, String featureType)
	{
		Vector cds = new Vector();
		while(i.hasNext())
		{
			StrandedFeature f = (StrandedFeature)i.next();
			if(f.getType().equalsIgnoreCase(GenomeBuilder.MAUVE_AGGREGATE))
			{
				Feature[] d = getFeature(f.features(), featureType);	// this is an aggregate, so recurse
				for(int dI = 0; dI < d.length; dI++)
					cds.add(d[dI]);
				continue;
			}
			if(!f.getType().equalsIgnoreCase(featureType))
				continue;
			int l = f.getLocation().getMin();
			int r = f.getLocation().getMax();
			Object ltag = null;
			try{
				ltag = f.getAnnotation().getProperty("locus_tag");
			}catch(Exception e){
				try{
					ltag = f.getAnnotation().getProperty("gene");
				}catch(Exception e2){}				
			}
			if(ltag==null) ltag = new String();
			if(l > r)
			{
				int t = l;
				l = r;
				r = t;
			}
			cds.add(new Feature(l, r, f.getStrand() == StrandedFeature.POSITIVE ? 1 : -1, ltag.toString()));
		}
		Collections.sort(cds);
		// remove dupes
		for(int j = 1; j < cds.size(); j++)
		{
			Feature cj = (Feature)cds.elementAt(j);
			Feature cjm1 = (Feature)cds.elementAt(j-1);
			if(cj.compareTo(cjm1) == 0 && cj.locus.equalsIgnoreCase(cjm1.locus))
			{
				cds.remove(j);
				j--;
			}
		}
		Feature[] c = new Feature[cds.size()];
		return (c = (Feature[])cds.toArray(c));
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

	private static Quad[] getBackboneSegs(XmfaViewerModel model, Genome g_i, Feature cds)
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
				if(bcur == null)
					cur = max + 1;
				else
					cur = (int)(bcur.getLeftEnd(g_i));
				continue;
			}
			Quad q = new Quad(bcur.starts, bcur.ends);
			if(q.left[g_i.getSourceIndex()] < cds.left)
			{
				long[] leftPos = xmfa.getLCBAndColumn(g_i, cds.left);
				if(leftPos == null)
					System.err.println("Null leftpos!");
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
				if(rightPos == null)
					System.err.println("Null rightpos!");
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
	private static void addOverlappingCDS(XmfaViewerModel model, Feature[] cds, Quad bb, HashMap hm, int gI, int gJ)
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

	static public class OrthologExportParameters
	{
		// find all annotated orthologs that meet the following criteria:
		public float min_conserved_length = 0.7f;  /**< minimum length of alignment as a fraction of feature */
		public float max_conserved_length = 1.0f;  /**< max length of alignment coverage of feature */
		public float min_nucleotide_id = 0.6f;		/**< min nucleotide identity in aligned region */
		public float max_nucleotide_id = 1.0f;		/**< Max nucleotide identity over aligned region */
		public boolean predictUnannotated = false;	/**< Include aligned regions that are not annotated with features (not yet implemented) */
		public String featureType = "CDS";	/**< Type of features for which to find orthologs, e.g. CDS, tRNA, misc_RNA etc. */
		public File alignmentOutputFile = null;	/**< If non-null, specifies a file where an XMFA alignment of features should be stored */
	}
	public static void export( XmfaViewerModel model, BufferedWriter output, OrthologExportParameters oep ) throws IOException
	{
		float min_conserved_length = oep.min_conserved_length;
		float max_conserved_length = oep.max_conserved_length;  
		float min_nucleotide_id = oep.min_nucleotide_id;
		float max_nucleotide_id = oep.max_nucleotide_id;

		// for each annotated CDS in each genome, identify candidate 
		// ortholog CDSes in other genomes
		Vector allCds = new Vector();
		for(int gI = 0; gI < model.getSequenceCount(); gI++)
		{
			Genome g = model.getGenomeBySourceIndex(gI);
            Location loc = LocationTools.makeLocation(1, (int)g.getLength());
            Feature[] cdsi = new Feature[0];
            if(g.getAnnotationSequence() != null && loc != null )
            {
				FeatureHolder fh = g.getAnnotationSequence().filter(new FeatureFilter.OverlapsLocation(loc));
				cdsi = getFeature(fh.features(), oep.featureType);
				Arrays.sort(cdsi);
            }
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
			Feature[] cdsi = (Feature[])allCds.elementAt(gI);
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
					Feature[] cdsj = (Feature[])allCds.elementAt(gJ);
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
								Feature[] cdsi = (Feature[])allCds.elementAt(cur);
								Feature[] cdsj = (Feature[])allCds.elementAt(i);
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
				
		// write out the ortholog table
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
					Feature[] cdsi = (Feature[])allCds.elementAt(sI);
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
		output.flush();

		// make a list of CDS that have no orthologs, since
		// we will want to write these out too
		Vector found = new Vector();
		for(int sI = 0; sI < model.getSequenceCount(); sI++)
		{
			Feature[] cdsi = (Feature[])allCds.elementAt(sI);
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
			Feature[] cdsi = (Feature[])allCds.elementAt(sI);
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
		output.flush();
		
		// if requested to write a file of alignments then do that now
		BufferedWriter xmfaout = new BufferedWriter(new FileWriter(oep.alignmentOutputFile));
		for(int oI = 0; oI < orthologs.size(); oI++)
		{
			HashSet[] ortho = (HashSet[])orthologs.elementAt(oI);			
			// for each sequence, construct the maximal coordinate range then select
			// the maximal alignment column range among all sequences?
			long maxalnRange = -1;
			int maxalnSeq = -1;
			long maxalnLeft = -1;
			long maxalnRight = -1;
			long[] seqlefts = new long[ortho.length];
			long[] seqrights = new long[ortho.length];
			for(int sI = 0; sI < ortho.length; sI++)
			{
				long mincoord = 0;
				long maxcoord = 0;
				Iterator iter = ortho[sI].iterator();
				while(iter.hasNext())
				{
					int osi = ((Integer)iter.next()).intValue();
					Feature[] cdsi = (Feature[])allCds.elementAt(sI);
					if(mincoord==0||mincoord>cdsi[osi].left)	mincoord=cdsi[osi].left;
					if(maxcoord==0||maxcoord<cdsi[osi].right)	maxcoord=cdsi[osi].right;
				}
				seqlefts[sI]=mincoord;
				seqrights[sI]=maxcoord;
				Object[] aln = model.getXmfa().getRange(model.getGenomeBySourceIndex(sI), mincoord, maxcoord);
				if(((byte[])aln[sI]).length > maxalnRange)
				{
					maxalnSeq=sI;
					maxalnRange=((byte[])aln[sI]).length;
					maxalnLeft = mincoord;
					maxalnRight = maxcoord;
				}
			}
			Object[] aln = model.getXmfa().getRange(model.getGenomeBySourceIndex(maxalnSeq), maxalnLeft, maxalnRight);
			StringBuilder xmfaEntry = new StringBuilder();
			for(int sI = 0; sI < ortho.length; sI++)
			{
				xmfaEntry.append(">");
				xmfaEntry.append(sI);
				Iterator iter = ortho[sI].iterator();
				boolean first = true;
				while(iter.hasNext())
				{
					if(first)
					{
						xmfaEntry.append(":");
						xmfaEntry.append(seqlefts[sI]);
						xmfaEntry.append("-");
						xmfaEntry.append(seqrights[sI]);
						xmfaEntry.append(":");
						first = false;
					}
					else
						xmfaEntry.append(",");
					int osi = ((Integer)iter.next()).intValue();
					Feature[] cdsi = (Feature[])allCds.elementAt(sI);
					xmfaEntry.append(cdsi[osi].locus);
					xmfaEntry.append(" ");
					xmfaEntry.append(cdsi[osi].strand > 0 ? "+" : "-");
				}
				xmfaEntry.append("\n");
				xmfaEntry.append(format80(new String((byte[])aln[sI])));
			}
			xmfaEntry.append("=\n");
			xmfaout.write(xmfaEntry.toString());
		}
	}
	
	/* reformats a string to be 80 column width */
	private static String format80(String sequence)
	{
		StringBuilder sb = new StringBuilder();
		sequence.replaceAll("[\r\n]", "");
		for(int i=0; i<sequence.length(); i+=80)
		{
			int j = i+80 < sequence.length() ? i+80 : sequence.length();
			sb.append(sequence.substring(i,j));
			sb.append("\n");
		}
		return sb.toString();
	}

	static public class ExportFrame extends JFrame
	{
	    private final static DecimalFormat FORMAT = new DecimalFormat("###");
	    
	    private RearrangementPanel rrpanel;
	    private JTextField outputFile = new JTextField();
	    private JFileChooser fc = new JFileChooser();
	    private JComboBox featureTypeSelector = new JComboBox();

	    private JFormattedTextField minIdentityBox = new JFormattedTextField(FORMAT);
	    private JFormattedTextField maxIdentityBox = new JFormattedTextField(FORMAT);
	    private JFormattedTextField minCoverageBox = new JFormattedTextField(FORMAT);
	    private JFormattedTextField maxCoverageBox = new JFormattedTextField(FORMAT);
	    private JCheckBox writeAlignmentFileBox = new JCheckBox();
	    private JCheckBox visibleGenomesBox = new JCheckBox();

	    private OrthologExportParameters oep = new OrthologExportParameters();
	    
	    XmfaViewerModel model;

	    public ExportFrame(XmfaViewerModel model)
	    {
	    	this.model = model;
	        setSize(400,225);
	        
	        getContentPane().setLayout(new GridBagLayout());
	        GridBagConstraints c = new GridBagConstraints();

	        setTitle("Mauve Ortholog Export");

	        c.insets = new Insets(2,2,2,2);

	        // Format label.
	        c.gridx = 0;
	        c.gridy = 0;
	        c.gridwidth = 1;
	        c.anchor = GridBagConstraints.EAST;
	        c.fill = GridBagConstraints.NONE;
	        getContentPane().add(new JLabel("Features:"), c);
	        
	        // Format selector.
	        c.gridx = 1;
	        c.gridy = 0;
	        c.gridwidth = 2;
	        c.anchor = GridBagConstraints.WEST;
	        c.fill = GridBagConstraints.HORIZONTAL;
	        featureTypeSelector.addItem("CDS");
	        featureTypeSelector.addItem("gene");
	        featureTypeSelector.addItem("rRNA");
	        featureTypeSelector.addItem("misc_RNA");
	        featureTypeSelector.addItem("tRNA");
	        getContentPane().add(featureTypeSelector, c);

	        // Percent identity label.
	        c.gridx = 0;
	        c.gridy = 1;
	        c.gridwidth = 1;
	        c.weighty = 1;
	        c.anchor = GridBagConstraints.EAST;
	        c.fill = GridBagConstraints.NONE;
	        getContentPane().add(new JLabel("Identity:"), c);

	        // Image size boxes.
	        JPanel scalePanel = new JPanel();
	        scalePanel.setLayout(new GridBagLayout());
	        GridBagConstraints c2 = new GridBagConstraints();
	        
	        // Min ID
	        c2.gridx = 0;
	        c2.gridy = 0;
	        c2.anchor = GridBagConstraints.EAST;
	        scalePanel.add(new JLabel("Min:"), c2);
	        
	        // Min ID box
	        c2.gridx = 1;
	        c2.insets = new Insets(0,0,0,4);
	        c2.anchor = GridBagConstraints.WEST;
	        scalePanel.add(minIdentityBox,c2);
	        minIdentityBox.setValue(new Long(Math.round((oep.min_nucleotide_id*100.0))));
	        minIdentityBox.setColumns(3);

	        // Max ID label
	        c2.gridx = 2;
	        c2.anchor = GridBagConstraints.EAST;
	        c2.insets = new Insets(0,0,0,0);
	        scalePanel.add(new JLabel("Max:"),c2);

	        // Max ID box
	        c2.gridx = 3;
	        c2.anchor = GridBagConstraints.WEST;
	        scalePanel.add(maxIdentityBox,c2);
	        maxIdentityBox.setValue(new Long(Math.round((oep.max_nucleotide_id*100.0))));
	        maxIdentityBox.setColumns(3);

	        JPanel scalePanel2 = new JPanel();
	        scalePanel2.setLayout(new GridBagLayout());
	        c2 = new GridBagConstraints();

	        // Min Coverage
	        c2.gridx = 0;
	        c2.gridy = 0;
	        c2.anchor = GridBagConstraints.EAST;
	        scalePanel2.add(new JLabel("Min:"), c2);
	        
	        // Min Coverage box
	        c2.gridx = 1;
	        c2.insets = new Insets(0,0,0,4);
	        c2.anchor = GridBagConstraints.WEST;
	        scalePanel2.add(minCoverageBox,c2);
	        minCoverageBox.setValue(new Long(Math.round((oep.min_conserved_length*100.0))));
	        minCoverageBox.setColumns(3);

	        // Max Coverage label
	        c2.gridx = 2;
	        c2.anchor = GridBagConstraints.EAST;
	        c2.insets = new Insets(0,0,0,0);
	        scalePanel2.add(new JLabel("Max:"),c2);

	        // Max Coverage box
	        c2.gridx = 3;
	        c2.anchor = GridBagConstraints.WEST;
	        scalePanel2.add(maxCoverageBox,c2);
	        maxCoverageBox.setValue(new Long(Math.round((oep.max_conserved_length*100.0))));
	        maxCoverageBox.setColumns(3);

	        // Adding scale panel.
	        c.gridx = 1;
	        c.gridy = 1;
	        c.gridwidth = 2;
	        c.anchor = GridBagConstraints.WEST;
	        getContentPane().add(scalePanel, c);

	        
	        // Adding coverage label.
	        c.gridx = 0;
	        c.gridy = 2;
	        c.gridwidth = 1;
	        c.weighty = 1;
	        c.anchor = GridBagConstraints.EAST;
	        getContentPane().add(new JLabel("Coverage:"), c);

	        // Adding scale panel 2
	        c.gridx = 1;
	        c.gridy = 2;
	        c.gridwidth = 2;
	        c.anchor = GridBagConstraints.WEST;
	        getContentPane().add(scalePanel2, c);


	        // checkboxes.
	        c.gridx = 0;
	        c.gridy = 3;
	        c.gridwidth = 3;
	        c.weighty = 0;
	        c.anchor = GridBagConstraints.WEST;
	        c.fill = GridBagConstraints.NONE;

		    writeAlignmentFileBox.setText("Create ortholog alignment file");
		    writeAlignmentFileBox.setSelected(true);
	        getContentPane().add(writeAlignmentFileBox, c);

/*	        c.gridy = 4;
	        visibleGenomesBox.setText("Visible genomes only");
	        visibleGenomesBox.setToolTipText("Export orthologs only for non-collapsed genomes");
	        visibleGenomesBox.setSelected(false);
	        getContentPane().add(visibleGenomesBox, c);	        
*/        
	        // File label.
	        c.gridx = 0;
	        c.gridy = 5;
	        c.gridwidth = 1;
	        c.fill = GridBagConstraints.NONE;
	        c.anchor = GridBagConstraints.SOUTHEAST;
	        c.weighty = 0;
	        getContentPane().add(new JLabel("Output file:"), c);
	        
	        // File text box
	        c.gridx = 1;
	        c.gridy = 5;
	        c.gridwidth = 1;
	        c.weighty = 1;
	        c.fill = GridBagConstraints.HORIZONTAL;
	        c.anchor = GridBagConstraints.SOUTHWEST;
	        c.weightx = 1;
	        getContentPane().add(outputFile, c);
	        
	        // File browse button.
	        JButton fileButton = new JButton("Browse...");
	        fileButton.addActionListener(new ActionListener()
	                {

	                    public void actionPerformed(ActionEvent e)
	                    {
	                        int ret = fc.showDialog(ExportFrame.this, "Select");
	                        if (ret == JFileChooser.APPROVE_OPTION)
	                        {
	                            File f = fc.getSelectedFile();
	                            outputFile.setText(f.getAbsolutePath());
	                        }
	                    }
	                }
	        );
	        c.gridx = 2;
	        c.gridy = 5;
	        c.gridwidth = 1;
	        c.fill = GridBagConstraints.NONE;
	        c.anchor = GridBagConstraints.SOUTHWEST;
	        c.weightx = 0;
	        getContentPane().add(fileButton, c);
	        
	        // Export button.
	        JPanel buttonPanel = new JPanel();
	        
	        JButton exportButton = new JButton("Export");
	        exportButton.addActionListener(new ActionListener()
	                {
	        
	                    public void actionPerformed(ActionEvent e)
	                    {
	                        doExport();
	                    }
	            
	                }
	        );
	        
	        buttonPanel.add(exportButton);
	        
	        JButton cancelButton = new JButton("Cancel");
	        cancelButton.addActionListener(new ActionListener()
	                {
	        
	                    public void actionPerformed(ActionEvent e)
	                    {
	                        setVisible(false);
	                    }
	                }
	        );
	        
	        buttonPanel.add(cancelButton);

	        c.gridx = 0;
	        c.gridy = 6;
	        c.gridwidth = 2;
	        c.weighty = 0;
	        c.fill = GridBagConstraints.NONE;
	        c.anchor = GridBagConstraints.CENTER;

	        JLabel orthoLab = new JLabel("<html>Note: ortholog export may<br>be very time-consuming");
	        orthoLab.setForeground(Color.gray);
	        getContentPane().add(orthoLab, c);
	        
	        c.gridx = 2;
	        c.gridy = 6;
	        c.gridwidth = 2;
	        c.weighty = 0;
	        c.fill = GridBagConstraints.NONE;
	        c.anchor = GridBagConstraints.SOUTHEAST;

	        getContentPane().add(buttonPanel, c);
	        this.setVisible(true);
	    }
	    
	    private void doExport()
	    {
	    	File f = new File(outputFile.getText());
	        if (f.exists())
	        {
	            int result = JOptionPane.showConfirmDialog(this, "The file " + outputFile.getText() + " already exists.  Overwrite?", "File exists", JOptionPane.YES_NO_OPTION);
	            if (result == JOptionPane.NO_OPTION)
	            {
	                return;
	            }
	        }
	        oep.featureType = (String)featureTypeSelector.getSelectedItem();
	        oep.min_nucleotide_id = Float.parseFloat(this.minIdentityBox.getText()) / 100f;
	        oep.max_nucleotide_id = Float.parseFloat(this.maxIdentityBox.getText()) / 100f;
	        oep.min_conserved_length = Float.parseFloat(this.minCoverageBox.getText()) / 100f;
	        oep.max_conserved_length = Float.parseFloat(this.maxCoverageBox.getText()) / 100f;
	        String errorMessage = null;
	        if(oep.min_nucleotide_id>=oep.max_nucleotide_id)
	        	errorMessage = "Min. pairwise nucleotide identity must be less than max.";
	        if(oep.min_conserved_length>=oep.max_conserved_length)
	        	errorMessage = "Min. pairwise alignment coverage must be less than max.";
	        if(oep.min_nucleotide_id < 0 || oep.max_nucleotide_id > 1.0)
	        	errorMessage = "Pairwise nucleotide identity must be in the range of 0 to 100.";
	        if(oep.min_conserved_length < 0.51 || oep.max_conserved_length > 1.0)
	        	errorMessage = "Pairwise alignment coverage must be in the range of 51 to 100.";
	        if(errorMessage != null)
	        {
	        	JOptionPane.showMessageDialog(this, errorMessage, "Error",JOptionPane.ERROR_MESSAGE);
	        	return;
	        }
	        try{
		        if(this.writeAlignmentFileBox.isSelected())
		        	oep.alignmentOutputFile = new File(f.getCanonicalPath() + ".alignments");
	        	BufferedWriter bw = new BufferedWriter( new FileWriter(f));
		        OneToOneOrthologExporter.export(model, bw, oep);
		        setVisible(false);
	        }catch(IOException ioe){
	        	ioe.printStackTrace();
	        }
	    }
	}
}