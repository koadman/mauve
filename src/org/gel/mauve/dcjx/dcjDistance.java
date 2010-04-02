package org.gel.mauve.dcjx;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


/**
 * 
 * @author atritt
 * @deprecated 
 */
public class dcjDistance {

	public static void main(String [] args)
	{
		
		if (args.length != 1){
			System.err.println("Usage: java -jar DCJ.jar <perm_file>\n"+
					           "  where perm_file contains one line per genome");
			System.exit(-1);
		}
		
		Scanner in = null;
		try {
			in = new Scanner(new FileReader(args[0]));
			
		} catch (IOException e){
			e.printStackTrace();
			System.exit(-1);
		}
	
		String genomeX = in.nextLine().trim();
		String genomeY = in.nextLine().trim(); 

		// make some toys
	
		genomeX = "a,b,c,d,e,f,g,h*$";        
		genomeY = "a,c,-d,-g,-f,-e,h,b$";
		
		System.out.println(genomeX +"\n" +genomeY);
		
		System.out.flush();
		
//		Map<String,Integer> blockIdMap = new HashMap<String,Integer>();
//		DCJ.loadBlockIDMap(genomeX, blockIdMap);
//		DCJ.loadBlockIDMap(genomeY, blockIdMap);
		DCJ dcj = new DCJ(genomeX, genomeY);
//		Permutation x = new Permutation(genomeX, blockIdMap, "genomeX");
//		Permutation y = new Permutation(genomeY, blockIdMap, "genomeY");
//		System.out.println("Computing DCJ distance between " + x.getName() + " and " + y.getName());
//		x.printDesc(System.out);
//		y.printDesc(System.out);
		
		int N = dcj.numBlocks(); 
			//blockIdMap.size();
		System.out.println("Found N = " + N + " blocks");
		
/*	
		Adjacency[] adj = x.getAdjacencies();
		System.out.print("Adjacencies in X: {");
		for (int i = 0; i < adj.length; i++){
			System.out.print(adj[i].toString());
			if (i<adj.length-1)
				System.out.print(",");
			else 
				System.out.print("}\n");
		}
		adj = y.getAdjacencies();
		System.out.print("Adjacencies in Y: {");
		for (int i = 0; i < adj.length; i++){
			System.out.print(adj[i].toString());
			if (i<adj.length-1)
				System.out.print(",");
			else 
				System.out.print("}\n");
		}
*/
		System.out.print("Creating Adjacency graph...");
		AdjacencyGraph agXY = dcj.getAdjacencyGraph();
		System.out.println("done!");
		int C = agXY.numCycles();
		int I = agXY.numOddPaths();
		System.out.println("Found C = " + C + " cycles");
		System.out.println("Found I = " + I + " odd pathes");
		int dcjDist = N - (C + I/2);
		System.out.println("dcj(X,Y) = " + dcjDist);
		System.out.println("dcj(X,Y) = " + dcj.dcjDistance());
	}
	


	
}
