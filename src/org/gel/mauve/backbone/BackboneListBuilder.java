package org.gel.mauve.backbone;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.gel.mauve.Genome;
import org.gel.mauve.XMFAAlignment;
import org.gel.mauve.XmfaViewerModel;

public class BackboneListBuilder {
	public static File getBbFile(XmfaViewerModel model, XMFAAlignment xmfa)
	{
        Properties meta = xmfa.metadata;
        // Find the backbone data
        String bb_fname;
        if (meta.containsKey("BackboneFile"))
        {
        	bb_fname = meta.getProperty("BackboneFile");
        }else{
        	return null;	// no backbone information
        }
        File src = new File(bb_fname);
        if (!src.canRead())
        {
    		bb_fname = model.getSrc().getParent() + File.separatorChar + src.getName();
        }
        src = new File(bb_fname);
        if (!src.canRead())
        {
        	return null;	// can't read the backbone file
        }        
        return src;
	}

	public static BackboneList build( XmfaViewerModel model, XMFAAlignment xmfa ) throws IOException
	{
		int sequenceCount = model.getSequenceCount();
		File src = getBbFile(model,xmfa);
		if(src == null)
			return null;
		Vector bbvect = new Vector();
		BufferedReader br = new BufferedReader( new java.io.FileReader(src));
		String cur_line;
		StringTokenizer stoke = null;
		while( br.ready() )
		{
			cur_line = br.readLine();
			stoke = new StringTokenizer(cur_line);
			String tok = stoke.nextToken();
			int lcb_id = Integer.parseInt(tok);
			tok = stoke.nextToken();
			long left_col = Long.parseLong(tok) - 1;	// left col starts at 1 in the file
			tok = stoke.nextToken();
			long length = Long.parseLong(tok);
			boolean seqs[] = new boolean[sequenceCount];
			for(int sI = 0; sI < sequenceCount; ++sI)
				seqs[sI] = false;
			while(stoke.hasMoreTokens())
			{
				tok = stoke.nextToken();
				int seq = Integer.parseInt(tok);
				seqs[seq] = true;
			}
			Backbone bb = new Backbone();
			bb.setLcbIndex(lcb_id);
			bb.setLeftColumn(left_col);
			bb.setLength(length);
			bb.setSeqs(seqs);
			long lends[] = xmfa.getColumnCoordinates(model,lcb_id,left_col);
			long rends[] = xmfa.getColumnCoordinates(model,lcb_id,left_col+length-1);
			for( int sI = 0; sI < seqs.length; ++sI )
			{
				if(!seqs[sI])
				{
					lends[sI] = 0;
					rends[sI] = 0;
				}else{
					if(rends[sI] < lends[sI])
					{
						long tmp = lends[sI];
						lends[sI] = rends[sI];
						rends[sI] = tmp;
					}
				}
			}
			bb.setLeftEnd(lends);
			bb.setRightEnd(rends);
			
			bbvect.addElement(bb);
		}
		Backbone[] bb_array = new Backbone[bbvect.size()];
		bb_array = (Backbone[])bbvect.toArray(bb_array);
		
		// now, for each genome create subarrays and sort on lcb id
		Vector seq_bb_vect = new Vector();
		for( int gI = 0; gI < model.getSequenceCount(); ++gI )
		{
			int bb_count = 0;
			for( int bbI = 0; bbI < bb_array.length; ++bbI)
			{
				if(bb_array[bbI].getSeqs()[gI])
					bb_count++;
			}
			Backbone[] seq_bb = new Backbone[bb_count];
			bb_count = 0;
			for( int bbI = 0; bbI < bb_array.length; ++bbI)
			{
				if(bb_array[bbI].getSeqs()[gI])
					seq_bb[bb_count++] = bb_array[bbI];
			}
			// now sort on LCB id
			Genome g = model.getGenomeBySourceIndex(gI);
			BbLeftEndComparator comp = new BbLeftEndComparator(g);
			Arrays.sort(seq_bb, comp);
			seq_bb_vect.addElement(seq_bb);
		}
		BackboneList bb_list = new BackboneList();
		bb_list.setXmfa(xmfa);
		bb_list.setBackbone(bb_array);
		bb_list.setSeqBackbone(seq_bb_vect);
		return bb_list;
	}
}
