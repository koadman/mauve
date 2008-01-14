package org.gel.mauve.analysis.output;

public abstract class AbstractIslandWriter extends AbstractMatchDataWriter {

	protected long multiplicity;

	public AbstractIslandWriter (String file, SegmentDataProcessor proc) {
		super (file, proc);
		printIslands ();
		doneWritingFile ();
	}

	public void printData () {
		//if (by_genome) {
			multiplicity = processor.multiplicityForGenome (seq_index);
		//}
		super.printData ();
	}

	abstract public void printIslands ();

}
