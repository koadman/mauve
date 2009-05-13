package org.gel.mauve.operon.pred;

import java.util.LinkedList;

import org.gel.mauve.operon.OperonConstants;
import org.gel.mauve.operon.pred.PredictionHandler.IGD;
import org.gel.mauve.operon.pred.PredictionHandler.OperonGene;

public interface IGDSource extends OperonConstants {

	public int getType (IGD igd);
	
	public LinkedList <OperonGene> getGenes ();
}
