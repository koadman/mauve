package org.gel.mauve.contigs;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.gui.AlignFrame;

public class ContigMauveDataModel {
	
	private static String DIR_STUB = "alignment";
	
	private static String[] DEFAULT_ARGS = {"--skip-refinement","--weight=200"};
	
	private int count;
	
	private File reference;
	
	private File outputDir;
	
	private File draft;
	
	/**
	 * <code>pMauve_cmd[pMauve_cmd.length-3]</code> should contain the argument 
	 *         "--output=[output_file_path]"
	 */
	private String[] pMauve_cmd;
	
	public ContigMauveDataModel(File reference){
		this();
		this.reference = reference;
		pMauve_cmd[pMauve_cmd.length-2] = this.reference.getAbsolutePath();
	}
	
	public ContigMauveDataModel(){
		count = 1;
		pMauve_cmd = new String[4+DEFAULT_ARGS.length];
		pMauve_cmd[0] = AlignFrame.getBinaryPath("progressiveMauve");
		int j = 1;
		for (int i = 0; i < DEFAULT_ARGS.length; i++){
			pMauve_cmd[j] = DEFAULT_ARGS[i];
			j++;
		}
		/*
		pMauve_cmd[pMauve_cmd.length-3] = null;
		pMauve_cmd[pMauve_cmd.length-2] = null;
		pMauve_cmd[pMauve_cmd.length-1] = null;
		*/
	}
	
	public static String[] buildPMauveCmd(HashMap<String,String> options, HashSet<String> flags){
		String[] ret = new String[options.size()+flags.size()+3];
		ret[0] = AlignFrame.getBinaryPath("progressiveMauve");
		return ret;
	}
	
	public XmfaViewerModel getNextAlignment(){
		Process p = null;
		int retcode = -1;
		try {
			p = Runtime.getRuntime().exec(pMauve_cmd);
			retcode = p.waitFor();
		} catch (Exception e) {
			
			e.printStackTrace();
			System.err.print("\n"+"Failed to get the next alignment because the following progressiveMauve command failed.\n\t");
			for (int i = 0; i < pMauve_cmd.length; i++){
				System.err.print(pMauve_cmd[i]+" ");
			}
			System.err.println();
			
		} 
		String outFile = pMauve_cmd[pMauve_cmd.length-3].split("=")[1];
		XmfaViewerModel ret = null;
		try{
			ret = new XmfaViewerModel(new File(outFile), null);
		} catch (IOException e){
			e.printStackTrace();
			System.exit(-1);
		}
		return ret;
	}
	
	public XmfaViewerModel getNextAlignment(String output_file){
		pMauve_cmd[pMauve_cmd.length-3] = "--output="+output_file;
		return getNextAlignment();
	}
	
	public XmfaViewerModel getNextAlignment(File toReorder, File alnmtOutput){
		draft = new File(alnmtOutput.getAbsolutePath());
		pMauve_cmd[pMauve_cmd.length-3] = "--ouput="+alnmtOutput.getAbsolutePath();
		pMauve_cmd[pMauve_cmd.length-2] = draft.getAbsolutePath();
		return getNextAlignment();
	}
	
	public int getCurrentIteration(){
		return -1;
	}
	
	public void setOutputFile(String filePath){
		pMauve_cmd[pMauve_cmd.length-3] = "--ouput="+filePath;
	}
	
	public void setRefPath(String filePath){
		reference = new File(filePath);
		pMauve_cmd[pMauve_cmd.length-2] = reference.getAbsolutePath();
	}
	
	public void setDraftPath(String filePath){
		draft = new File(filePath);
		pMauve_cmd[pMauve_cmd.length-1] = draft.getAbsolutePath();
	}
}