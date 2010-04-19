package org.gel.mauve.contigs;

public class ContigMauveDataModel {

	
	
	private String reference;
	
	private String outputDir;
	
	private String draft;
	
	private String[] pMauve_cmd;
	/*
	public ContigMauveDataModel(File reference, File outputDir, HashMap<String,String> options, HashSet<String> flags){
		
	}
	
	
	public XmfaViewerModel getNextAlignment(){
		
	}
	
	public int getCurrentIteration(){
		
	}
	*/
	public void setRefPath(String file){
		reference = file;
	}
	
	public void setDraftPath(String file){
		draft = file;
	}
	
	
	
	
	
}