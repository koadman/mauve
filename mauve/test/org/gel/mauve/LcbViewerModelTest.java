package org.gel.mauve;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class LcbViewerModelTest extends TestCase {

	public void testWeightAdjustmentBug() throws IOException, MauveFormatException
	{
		XmfaViewerModel model1 = (XmfaViewerModel) ModelBuilder.buildModel(new File("testdata/small.alignment"), null);
		XmfaViewerModel model2 = (XmfaViewerModel) ModelBuilder.buildModel(new File("testdata/small.alignment"), null);
		
		// Change weight and reorder.
		model1.updateLCBweight(50, true);
		model1.reorderSequences(new int[] {1,0});
		
		// Reorder and change weight.
		model2.reorderSequences(new int[] {1,0});
		model2.updateLCBweight(50, true);
		
		Genome g0 = model1.getGenomeBySourceIndex(0);
		Genome g1 = model1.getGenomeBySourceIndex(1);
		
		Genome g0b = model2.getGenomeBySourceIndex(0);
		Genome g1b = model2.getGenomeBySourceIndex(1);
		
		// The order of operations should not matter.
		assertEquals(305, model1.getVisibleLcb(0).getLeftEnd(g0));
		assertEquals(566, model1.getVisibleLcb(0).getLeftEnd(g1));
		assertEquals(model1.getVisibleLcb(0).getLeftEnd(g0), model2.getVisibleLcb(0).getLeftEnd(g0b));
		assertEquals(model1.getVisibleLcb(0).getLeftEnd(g1), model2.getVisibleLcb(0).getLeftEnd(g1b));
	}
	
	public void testRearrangementEliminatesColorsInMumsFile() throws IOException, MauveFormatException
	{
		BaseViewerModel model = ModelBuilder.buildModel(new File("testdata/small.mums"), null);
		
		// Reordering sequences certainly shouldn't affect color!
		Match match = model.getMatch(0);
		Color oldColor = match.color;
		model.reorderSequences(new int[] {1,0});
		assertEquals(oldColor, match.color);
	}
	
	public void testUpdateLCBWeightForMauveFile() throws IOException, MauveFormatException
	{
		LcbViewerModel model = (LcbViewerModel) ModelBuilder.buildModel(new File("testdata/small.mauve"), null);
		model.sanityCheck();
		model.updateLCBweight(1389, false);
		model.sanityCheck();
	}

	

	
}
