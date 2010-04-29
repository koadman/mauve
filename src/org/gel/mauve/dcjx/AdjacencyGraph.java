package org.gel.mauve.dcjx;

public class AdjacencyGraph {
	
	private Adjacency[] adjA;
	
//	private boolean[] visitA;
	
	private Adjacency[] adjB;
	
	private int numCycles;
	
	private int numOddPaths;
	
	private int numLen2Cycles;
	
	private int numLen1Paths;
	
	private int numPathsGreaterThan2;
	
//	private boolean[] visitB;
	
	public AdjacencyGraph(Permutation A, Permutation B){
		adjA = A.getAdjacencies();
		adjB = B.getAdjacencies();
//		visitA = new boolean[adjA.length];
//		visitB = new boolean[adjB.length];
		FastAccessTable fatA = A.getFAT(); // this may be useful later
		FastAccessTable fatB = B.getFAT();
		
		for (int i = 0; i < adjA.length; i++){
			try {
				
				if (adjA[i].isTelo()){
					Adjacency.addAdjacencyEdge(adjA[i],fatB.getAdjacency(adjA[i].getFirstBlockEnd()));
				} else {
					Adjacency.addAdjacencyEdge(adjA[i],fatB.getAdjacency(adjA[i].getFirstBlockEnd()));
					Adjacency.addAdjacencyEdge(adjA[i],fatB.getAdjacency(adjA[i].getSecondBlockEnd()));
				}
			
			} catch (NullPointerException e ){
				String msg = e.getMessage();
				msg = msg + " : null at adjA["+i+"]";
				System.err.println(msg);
				throw e;
			}
		}
		numCycles = countCycles();
		numOddPaths = countOddPaths();
		numLen2Cycles = countLen2Cycles();
		numLen1Paths = countSingleEdgePaths();
		numPathsGreaterThan2 = countPathsGreaterThan2(); 
		
	}
	
	/**
	 * Returns the number of paths in the adjacency
	 * graph with an odd length
	 * 
	 * @return the number of paths with an odd length
	 */
	public int numOddPaths(){
		return numOddPaths;
	}
	
	/**
	 * Returns the number of cycles in the adjacency graph
	 * 
	 * @return the number of cycles in the adjacency graph
	 */
	public int numCycles(){
		return numCycles;
	}
	
	/**
	 * Returns the number of cycles in the adjacency
	 * graph of length 2
	 * 
	 * @return the number of cycles of length 2
	 */
	public int numLen2Cycles(){
		return numLen2Cycles;
	}
	
	/**
	 * Returns the number of paths in the adjacency
	 * graph of length 1
	 * 
	 * @return the number of paths of length 1
	 */
	public int numLen1Paths(){
		return numLen1Paths;
	}
	
	/**
	 * Returns the number of paths in the adjacency 
	 * graph of length 2 or more
	 * 
	 * @return the number of paths of length 2 or more
	 */
	public int numPaths2(){
		return numPathsGreaterThan2;
	}
	
	private int countLen2Cycles(){
		int numCyc = 0;	
		for (int i = 0; i < adjA.length; i++){
			if (adjA[i].wasVisited()){
				continue;
			} else {
				if (isCycle(adjA[i])){
					resetVisited(adjA[i]);
					if (countEdges(adjA[i]) == 2)
						numCyc++;
				}
			}
		}
		resetVisitedAll();
		return numCyc;
	}
	
	private int countCycles(){
		int numCyc = 0;	
		for (int i = 0; i < adjA.length; i++){
			if (adjA[i].wasVisited()){
				continue;
			} else {
				if (isCycle(adjA[i])){
					numCyc++;
				}
			}
		}
		resetVisitedAll();
		return numCyc;
	}
	
	private int countOddPaths(){
		int numOddPath = 0;
		for (int i = 0; i < adjA.length; i++){
			if (adjA[i].wasVisited()) {
				continue;
			} else {
				if (countEdges(adjA[i]) % 2 == 1)
					numOddPath++;
			}
		}
		resetVisitedAll();
		return numOddPath;
	}
	
	private int countSingleEdgePaths(){
		int numSingleEdgePath = 0;
		for (int i = 0; i < adjA.length; i++){
			if (adjA[i].wasVisited()) {
				continue;
			} else {
				if (countEdges(adjA[i]) == 1)
					numSingleEdgePath++;
			}
		}
		resetVisitedAll();
		return numSingleEdgePath;
	}
	
	private int countPathsGreaterThan2(){
		int numPathGreaterThan2 = 0;
		for (int i = 0; i < adjA.length; i++){
			if (adjA[i].wasVisited()) {
				continue;
			} else {
				if (!isCycle(adjA[i])){
					resetVisited(adjA[i]);
					if (countEdges(adjA[i]) >= 2)
						numPathGreaterThan2++;
				} 
			}
		}
		resetVisitedAll();
		return numPathGreaterThan2;
	}
	
	private int countEdges(Adjacency a){
		if (a.wasVisited()){  // we've found a cycle 
			return 0;         
		}
		if (a.isTelo()){
			a.setVisited();
			if (a.getE1().wasVisited()){ // the only vertex directly connected was visited, so we're at the end of a path
				return 0;
			} else { // this is the first vertex we've visited in traversing this connected component
				return 1 + countEdges(a.getE1());
			}
		
		} else { // not at a telomere 
			a.setVisited();
			if (!(a.getE1().wasVisited() || a.getE2().wasVisited())) { // started at internal node
				// check both. we might not come back here. Don't know if this is a path or a cycle
				return 2 + countEdges(a.getE1()) + countEdges(a.getE2());
			} else if (a.getE1().wasVisited() && !a.getE2().wasVisited()) {   // make sure we don't go back to
				return 1 + countEdges(a.getE2());                             // the vertex we just came from
			} else if (!a.getE1().wasVisited() && a.getE2().wasVisited()) {   // make sure we don't go back to
				return 1 + countEdges(a.getE1());                             // the vertex we just came from
			} else { // we've found a cycle. Stop the recursion
				return 0;
			}
		}
	}
	
	private boolean isCycle(Adjacency a){
		if (a.wasVisited()){
			return true;
		} else if (a.isTelo()){
			a.setVisited();
			return false;
	//	} else if (!(a.getE1().wasVisited() || a.getE2().wasVisited())){
	//		a.setVisited();
	//		return 
		} else {
			a.setVisited();
			return isCycle(a.getE1()) && isCycle(a.getE2());
		}
	}
	
	private void resetVisitedAll(){
		for (int i = 0; i < adjA.length; i++){
			adjA[i].resetVisited();	
		} 
		for (int i = 0; i < adjB.length; i++){
			adjB[i].resetVisited();
		}
	}
	
	private void resetVisited(Adjacency a){
		if (!a.wasVisited()){
			a.resetVisited();
			resetVisited(a.getE1());
			if (!a.isTelo()){
				resetVisited(a.getE2());
			}
		} else
			return;
	}
	
	
	

}
