package org.gel.mauve.contigs;

import java.util.ArrayList;

public class ContigMauveDataModel {

	private ArrayList<String> sequences = new ArrayList<String>();
	public void addSequence(String s){
		sequences.add(s);
	}
}
