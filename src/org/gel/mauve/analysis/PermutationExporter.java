package org.gel.mauve.analysis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;
import org.gel.mauve.LCB;
import org.gel.mauve.LCBlist;
import org.gel.mauve.XMFAAlignment;
import org.gel.mauve.XmfaViewerModel;

public class PermutationExporter {
	public static Vector[] computeSignedPermutation(XmfaViewerModel model)
	{
		int seq_count = model.getSequenceCount();
		LCB[] visibleLcbList = model.getVisibleLcbList();
		Vector[] signed_perms = new Vector[seq_count];

		for (int seqI = 0; seqI < seq_count; seqI++)
			signed_perms[seqI] = new Vector();

		// first construct a matrix of chromosome lengths
		int max_chr_count = 0;
		for (int seqI = 0; seqI < seq_count; seqI++) {
			int cur_count = model.getGenomeByViewingIndex (seqI)
					.getChromosomes ().size ();
			max_chr_count = cur_count > max_chr_count ? cur_count
					: max_chr_count;
		}
		long [][] chr_lens = new long [seq_count] [max_chr_count];
		for (int seqI = 0; seqI < seq_count; seqI++) {
			List chromo = model.getGenomeByViewingIndex (seqI).getChromosomes ();
			for (int chrI = 0; chrI < chromo.size (); chrI++) {
				chr_lens[seqI][chrI] = ((Chromosome) chromo.get (chrI))
						.getEnd ();
			}
		}

		boolean single_chromosome = true;
		boolean all_circular = true;
		for (int seqI = 0; seqI < seq_count; seqI++) {
			Genome g = model.getGenomeByViewingIndex (seqI);
			List chromo = g.getChromosomes ();
			int leftmost_lcb = 0;
			for (; leftmost_lcb < visibleLcbList.length; leftmost_lcb++)
				if (visibleLcbList[leftmost_lcb].getLeftAdjacency (g) == LCBlist.ENDPOINT)
					break;

			int adjI = leftmost_lcb;
			int cur_chromosome = 0;
			all_circular = all_circular
					&& ((Chromosome) chromo.get (cur_chromosome))
							.getCircular ();

			signed_perms[seqI].add(new Vector());	// initialize a new chromosome
			Vector cur_chromo = (Vector)signed_perms[seqI].lastElement();
			while (adjI != LCBlist.ENDPOINT && adjI != LCBlist.REMOVED
					&& adjI < visibleLcbList.length) {
				while (visibleLcbList[adjI].getLeftEnd (g) > chr_lens[seqI][cur_chromosome]) {
					signed_perms[seqI].add(new Vector());
					cur_chromo = (Vector)signed_perms[seqI].lastElement();
					cur_chromosome++;
					single_chromosome = false;
					all_circular = all_circular
							&& ((Chromosome) chromo.get (cur_chromosome))
									.getCircular ();
				} 
				if (visibleLcbList[adjI].getReverse (g)) {
					cur_chromo.add(new Integer(-(adjI + 1)));
				}else{
					cur_chromo.add(new Integer(adjI + 1));
				}
				adjI = visibleLcbList[adjI].getRightAdjacency (g);
			}
		}
		return signed_perms;
	}
	public static void export( XmfaViewerModel model, BufferedWriter output ) throws IOException
	{
		Vector[] perms = computeSignedPermutation(model);
		for(int i = 0; i < perms.length; i++)
		{
			for(int j = 0; j < perms[i].size(); j++)
			{
				Vector cur = (Vector)perms[i].elementAt(j);
				for(int k = 0; k < cur.size(); k++)
				{
					if(k>0)
						output.write(",");
					output.write(cur.elementAt(k).toString());
				}
				output.write(" $ ");
			}
			output.write("\n");
		}
		output.flush();
	}
}
