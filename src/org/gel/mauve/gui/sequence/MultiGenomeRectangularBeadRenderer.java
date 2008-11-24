package org.gel.mauve.gui.sequence;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import org.biojava.bio.gui.sequence.RectangularBeadRenderer;
import org.biojava.bio.gui.sequence.SequenceRenderContext;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.Sequence;
import org.biojava.utils.ChangeVetoException;
import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Genome;

public class MultiGenomeRectangularBeadRenderer extends RectangularBeadRenderer
		implements FlatFileFeatureConstants {
	
	protected Hashtable seq_to_offset;
	
	protected HashSet painted;

	protected BaseViewerModel model;
	
	public double last_offset = -1;
	
	private boolean listeners = true;
	
	////my code
	public static final int POSITIVE_STRAND = 0;
	public static final int NEGATIVE_STRAND = 1;
	private double beadDepth;
	private double beadDisplacement;
	private Paint beadOutline;
	private Paint beadFill;
	private Stroke beadStroke;
	
	//private RectangularBeadRenderer new_rbr;
	private Map genome_RecBeadRendererLists;//a genome to two lists of RectangleRenderer, one positive strand, one negative strand
	private Map recBeadRenderer_Genome;//a rectangularBeadRenderer to a genome
	////
	
	public MultiGenomeRectangularBeadRenderer (double beadDepth, double beadDisplacement, Paint beadOutline,
				Paint beadFill, Stroke beadStroke, BaseViewerModel mod) {
		super (beadDepth, beadDisplacement, beadOutline, beadFill, beadStroke);
		model = mod;
		painted = new HashSet (model.getSequenceCount ());
		seq_to_offset = new Hashtable (model.getSequenceCount ());
		
		////my code
		//can set up a method to set the rbr
		genome_RecBeadRendererLists = new Hashtable(model.getSequenceCount());
		recBeadRenderer_Genome = new Hashtable();
		for (int i = 0; i < model.getSequenceCount(); i++)
		{
			genome_RecBeadRendererLists.put(model.getGenomeBySourceIndex(i), new LinkedList [] {new LinkedList(), new LinkedList()});
			//could define a method to set each RectangularBeadRenderer
		}
		////
	}
	

	public synchronized void renderBead (Graphics2D g, Feature feature, SequenceRenderContext context) {
		try {
			double offset = ((Double) seq_to_offset.get (feature.getSequence ())).doubleValue ();
//			System.out.println(">>>>> offset = " + offset);
			//draw when offset == last_offset, notice that the last_offset will be changed
			//first time call, last_offset == -1, so no draw. Change last_offset to valid offset
			//second time call, offset == lastoffset, ignore the if{}, goto else{} and draw.
			if  (offset != last_offset) {
				/*if (painted.contains (feature.getSequence ())) {
					listeners = false;
					painted.clear ();
				}
				else
					painted.add (feature.getSequence ());*/
				listeners = false;
				if (last_offset != -1)
					setBeadDisplacement (beadDisplacement - last_offset);
				//new Exception ().printStackTrace ();
				System.out.println ("last: " + last_offset + "\ndisp: " + beadDisplacement);
				last_offset = offset;
				System.out.println ("offset: "  + offset + "\ndisp: " + beadDisplacement);
				//Snew Exception ().printStackTrace ();
				setBeadDisplacement (beadDisplacement + offset);
				listeners = true;
			}
			
			//change this to call its child renderer.
			super.renderBead (g, feature, context);
			
			////my code
//			double offset = ((Double) seq_to_offset.get (feature.getSequence ())).doubleValue ();
//			setBeadDisplacement(beadDisplacement + offset);
//			for (int i = 0; i < model.getSequenceCount(); i++)
//			{
				
				
//			}
			////
		} catch (ChangeVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	////my code
	/**
	 * 
	 */
	public Map getGenomeToRendererListsMap()
	{
		return genome_RecBeadRendererLists;
	}

	public Map getRendererToGenomeMap()
	{
		return recBeadRenderer_Genome;
	}
	
	public LinkedList[] getRendererListArray(Genome genome)
	{
		return (LinkedList[]) genome_RecBeadRendererLists.get(genome);
	}
	
	public RectangularBeadRenderer addRendererToStrandList(RectangularBeadRenderer new_renderer, Genome genome, int flagForStrand)
	{
		recBeadRenderer_Genome.put(new_renderer, genome);
		LinkedList[] listArray = getRendererListArray(genome);
		if (flagForStrand == POSITIVE_STRAND)
		{
			listArray[POSITIVE_STRAND].add(new_renderer);
			return new_renderer;
		}
		else
		{
			listArray[NEGATIVE_STRAND].add(new_renderer);
			return new_renderer;
		}
	}
	////
	
	//only get called for positive strand!! do the job for negative strand automatically!
	public void setOffset (Genome genome, double offset) {
		seq_to_offset.put (genome.getAnnotationSequence (), new Double (offset));
		
		////my code
/*		LinkedList[] rbrListArray = (LinkedList[]) genome_RecBeadRendererLists.get(genome);
		//set offsets of the last positive-strand rbr's to offset
		RectangularBeadRenderer tmp = (RectangularBeadRenderer) rbrListArray[POSITIVE_STRAND].getLast();
		tmp.setBeadDisplacement(tmp.getBeadDisplacement() + offset - tmp.getBeadDepth());
		//set offsets of the last negative-strand rbr's to offset + beadDepth
		tmp = (RectangularBeadRenderer) rbrListArray[NEGATIVE_STRAND].getLast();
		tmp.setBeadDisplacement(tmp.getBeadDisplacement() + offset);
*/		////
	}
	
	////my code
	//only get called for positive strand!! do the job for negative strand automatically!
	public void setHeightScaling(Genome genome, boolean bool)
	{
		LinkedList[] rbrListArray = (LinkedList[]) genome_RecBeadRendererLists.get(genome);
		((RectangularBeadRenderer) rbrListArray[POSITIVE_STRAND].getLast()).setHeightScaling(bool);
		((RectangularBeadRenderer) rbrListArray[NEGATIVE_STRAND].getLast()).setHeightScaling(bool);
	}
	////
	
	public double getOffset (Genome genome) {
		Object ret = seq_to_offset.get (genome.getAnnotationSequence ());
		if (ret == null)
			return NO_OFFSET;
		return ((Double) ret).doubleValue ();
	}
	
	public boolean hasListeners () {
		System.out.println ("list: " + listeners);
		if (listeners)
			return super.hasListeners ();
		else
			return false;
	}

}
