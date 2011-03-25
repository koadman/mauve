package org.gel.mauve.histogram;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import org.gel.mauve.Genome;
import org.gel.mauve.XMFAAlignment;
import org.gel.mauve.XmfaViewerModel;

public class HistogramBuilder {
	private static class HistogramLine {
		int genomeId;
		int blockId;
		String tag;
		float[] data;
	}

	// reads a file with one or more histograms keyed to genomes
	// populates the genome attributes of the XMFA model
	public static void build(RandomAccessFile f, XmfaViewerModel model){
		if(f==null) return;
        try{
        	XMFAAlignment xmfa = model.getXmfa();
	        String line = null;
	        // Parse each line into a histogram line
	        Vector histlines = new Vector();
	        float maxval = -1;	// for normalization of the histogram
	        float minval = 1000000;	// for normalization of the histogram
	        byte[] data = new byte[1000000];
	        int start = data.length;
	        int state = 0;	// 0 read blockID, 1 read genomeID, 2 read tag, 3 read data
            HistogramLine hl = new HistogramLine();
            int pos = 0;
            int jj=0;
            while(pos < f.length()){
            	int remainder = data.length-start;
            	int toread = data.length-remainder;
            	toread = (int) (toread < f.length() - pos ? toread : f.length() - pos);
            	System.out.println("read " + pos + " / " + f.length());
            	System.arraycopy(data, start, data, 0, remainder);
    	        f.read(data,remainder,toread);
            	start = 0;
            	pos += toread;            	
				for(int cur = remainder; cur < toread+remainder; cur++)
				{
					switch(data[cur]){
					case '\n':	
						start = cur+1; 
						state=0;
						jj=0;
				        histlines.add(hl);
			            hl = new HistogramLine();
						break;
					case '\t':
						String s = new String(data, start, cur-start);
						if(state==0){
							hl.blockId = Integer.parseInt(s);	state++;
						}else if(state==1){
							hl.genomeId = Integer.parseInt(s);	state++;	hl.data = new float[(int)xmfa.getLcbLength(hl.blockId)];
						}else if(state==2){
							hl.tag = s;							state++;
						}else if(state==3){
							float floater = Float.parseFloat(s);
							maxval = floater > maxval ? floater : maxval;
							minval = floater < minval ? floater : minval;
							hl.data[jj++] = floater;
						}
						start = cur+1;
						break;
						default:	;
					}
				}
            }
            // get the last one
            if(jj>0){
                histlines.add(hl);
            }
			// now aggregate the different histogram lines for each genome
			for(int i=0; i < model.getSequenceCount(); i++){
				boolean foundData = false;
				Genome g = model.getGenomeBySourceIndex(i);
				Boolean gap = new Boolean(false);
				byte[] histvals = new byte[(int)g.getLength()];
				for(int j=0; j<histvals.length; j++){
					histvals[j]=-128;
				}
				float maxrenorm = -127;
				// scan each histogram line, looking for lines relevant to this genome
				for(int k=0; k<histlines.size();k++){
					hl = (HistogramLine)histlines.get(k);
					if(hl==null)	continue;
					if(hl.genomeId!=i)	continue;
					foundData = true;					
					float normval = maxval-minval;
					for(int j=0; j<hl.data.length; j++){
						long histpos = xmfa.getCoordinate(model, g, hl.blockId, j, gap);
						if(gap) continue;
						float renorm = ((hl.data[j]-minval) / (normval));
						renorm -= 0.5;
						renorm *= 255;
						if(histpos < histvals.length)
							histvals[(int)histpos] = renorm < -127 ? -128 : (byte)renorm;
						else
							System.err.println("hisvals.lengt " + histvals.length + " histpos " + histpos);
						maxrenorm = maxrenorm > renorm ? maxrenorm : renorm;
					}
					// free memory
					histlines.set(k, null);
				}
				// put the genome-wide histogram values into a ZoomHistogram
				if(foundData){
					ZoomHistogram zh = new ZoomHistogram(g);
					zh.setGenomeLevelData(histvals);
					model.addGenomeAttribute(g, zh);
				}
			}
        }catch(IOException ioe){
        	ioe.printStackTrace();
        }
	}
}
