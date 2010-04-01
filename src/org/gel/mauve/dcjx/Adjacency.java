package org.gel.mauve.dcjx;

public class Adjacency{
	
	private String first;
	private String second;
	private boolean isTelo;
	
	private Adjacency e1;
	
	private Adjacency e2;
	
	private boolean visited;
	
	public Adjacency(String first, String second){
		this.first = first;
		this.second = second;
		isTelo = false;
		visited = false;
	}
	public Adjacency(String first){
		this.first = first;
		this.second = Constants.TELOMERE;
		isTelo = true;
		visited = false;
	}
	
	public String toString(){
	 	if (isTelo){
	 		return "{"+first+"}";
	 	} else {
	 		return "{"+first+","+second+"}";
	 	}
	}
	
	public boolean isTelo(){
		return isTelo;
	}
	
	public boolean wasVisited(){
		return visited;
	}
	
	public void resetVisited(){
		visited = false;
	}
	
	public void setVisited(){
		visited = true;
	}
	
	public Adjacency getE1(){
		return e1;
	}
	
	public Adjacency getE2(){
		return e2;
	}
	
	public static void addAdjacencyEdge(Adjacency a, Adjacency b){
	//	System.out.println("Linking " + a.toString() + " and " + b.toString());
		if (a.e1 == null){
			a.e1 = b;
			if (b.e1 == null){
				b.e1 = a;
			} else if (b.e2 == null) {
				b.e2 = a;
			} else {
				throw new IllegalArgumentException("this Adjacency is full");
			}
		} else if (a.e2 == null){
			a.e2 = b;
			if (b.e1 == null){
				b.e1 = a;
			} else if (b.e2 == null) {
				b.e2 = a;
			} else {
				throw new IllegalArgumentException("this Adjacency is full");
			}
		} else {
			throw new IllegalArgumentException("this Adjacency is full");
		}
		
	}
	
	public String getFirstBlock(){
		return first.substring(0,first.length()-2);
	}
	
	public String getFirstBlockEnd(){
		return first;
	}
	
	public String getSecondBlockEnd(){
		return second;
	}
	
	public String getSecondBlock(){
		return second.substring(0,second.length()-2);
	}
	
}
