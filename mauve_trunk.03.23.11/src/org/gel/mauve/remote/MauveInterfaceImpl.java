package org.gel.mauve.remote;

import org.gel.mauve.Genome;
import org.gel.mauve.XmfaViewerModel;

public class MauveInterfaceImpl implements MauveInterface {
	XmfaViewerModel model;
	MauveInterfaceImpl(XmfaViewerModel model){
		this.model = model;
	}

	public void setGenomeOrder(int[] order){
		model.reorderSequences(order);
	}
	public void hackOrder(){
		int ord[] = {13,21,10,16,28,22,23,15,20,17,0,5,27,25,26,24,14,1,2,4,18,9,19,7,3,11,12,6,8};
		model.reorderSequences(ord);
	}
	
	public void setDisplayCoordinate(int genome, long coordinate) {
		model.alignView(model.getGenomeBySourceIndex(genome), coordinate);
	}

	public void setDisplayRange(int genome, long left, long right) {
		Genome g = model.getGenomeBySourceIndex(genome);
		long len = (right-left) * 5;
		int zoom = (int) (100 * g.getViewLength () / (double) len);
		long center = left + ((right - left) / 2);
		model.zoomAndCenter (g, zoom, center);
	}

	public void setDisplayBlockAndColumn(int block, long left_column, long right_column)
	{
		System.err.println("lc " + left_column + " rc " + right_column);
		long[] seq_coords = new long[model.getSequenceCount()];
		boolean[] gap = new boolean[model.getSequenceCount()];
		long[] rseq_coords = new long[model.getSequenceCount()];
		boolean[] rgap = new boolean[model.getSequenceCount()];
		model.getColumnCoordinates(block, left_column, seq_coords, gap);
		model.getColumnCoordinates(block, right_column, rseq_coords, rgap);
		for(int gI=0; gI < gap.length; gI++){
			if(!gap[gI]){
				long left = seq_coords[gI] < rseq_coords[gI] ? seq_coords[gI] : rseq_coords[gI];
				long right = seq_coords[gI] > rseq_coords[gI] ? seq_coords[gI] : rseq_coords[gI];				
				Genome g = model.getGenomeBySourceIndex(gI);
				long len = (right-left) * 5;
				int zoom = (int) (100 * g.getViewLength () / (double) len);
				long center = left + ((right - left) / 2);
				System.err.println("zoom " + zoom + " center " + center);
				model.zoomAndCenter (g, 0, center);
				model.zoomAndCenter (g, zoom, center);
				model.alignView(g, center);
				break;
			}
		}
	}

	public boolean isRemote() {
		// TODO Auto-generated method stub
		return false;
	}

	public int getMouseBlock(){
		long coord = model.getHighlightCoordinate();
		Genome g = model.getHighlightGenome();
		long[] l = model.getLCBAndColumn(g, coord);
		return (int)l[0];
	}
	public long getMouseColumn(){
		long coord = model.getHighlightCoordinate();
		Genome g = model.getHighlightGenome();
		long[] l = model.getLCBAndColumn(g, coord);
		return l[1];
	}
}
