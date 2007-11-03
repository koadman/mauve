package org.gel.mauve.dcj;

import java.util.*;
import java.io.*;

public class DCJ {

	/***************************************************************************
	 * Synteny Block object holds Vertices left,right, value of genomeA and
	 * value of genome B performs traversing along black lines to print state of
	 * genome A
	 **************************************************************************/
	class SB {
		private Vert left; // left vert

		private Vert right; // right vert

		private String identity;

		private int valueA; // int value for genome A

		private int valueB; // int value for genome B

		private boolean visited = false;

		// default constructor
		public SB () {
			left = null;
			right = null;
			identity = "";
		}// end SB constructor

		public SB (Vert l, Vert r, String i) {
			left = l;
			right = r;
			identity = i;
			l.setSB (this);
			r.setSB (this);
		}// end SB constructor

		public SB (Vert l, Vert r) {
			left = l;
			right = r;
			identity = "[" + l.getGene () + " " + r.getGene () + "]";
			l.setSB (this);
			r.setSB (this);
		}// end SB constructor

		// mutator methods
		public void setVisited (boolean b) {
			this.visited = b;
		}

		public void setLeft (Vert v) {
			this.left = v;
		}

		public void setRight (Vert v) {
			this.right = v;
		}

		public void setIdentity (String s) {
			this.identity = s;
		}

		public void turnValue (int s) {
			this.valueA = s;
			// {try{this.valueA=Integer.parseInt(s);}catch(Exception
			// e){System.out.println("format error\n"+e);System.exit(0);}
		}

		public void setValueA (int i) {
			this.valueA = i;
		}

		public void setValueB (int i) {
			this.valueB = i;
		}

		// //newly constructed vertices need to be set to point to this SB.
		public boolean setVertPointers () {
			if (this.getLeft () == null || this.getRight () == null) {
				return false;
			} else {
				this.getLeft ().setSB (this);
				this.getRight ().setSB (this);
			}
			return true;
		}

		// //retrieval methods
		public boolean wasVisited () {
			return this.visited;
		}

		public Vert getLeft () {
			return this.left;
		}

		public Vert getRight () {
			return this.right;
		}

		public String getIdentity () {
			return this.identity;
		}

		public int getValueA () {
			return this.valueA;
		}

		public int getValueB () {
			return this.valueB;
		}

		// //polarity of the SB, pos value head is right Vert, neg value head is
		// left Vert.
		public Vert getHeadA () {
			if (this.getValueA () < 0) {
				return this.getLeft ();
			} else {
				return this.getRight ();
			}
		}

		public Vert getTailA () {
			if (this.getValueA () < 0) {
				return this.getRight ();
			} else {
				return this.getLeft ();
			}
		}

		public Vert getHeadB () {
			// if(this.getValueB()<0){return this.getLeft();}else{return
			// this.getRight();}
			if (this.getValueB () == this.getValueA ()) {
				return this.getRight ();
			} else {
				return this.getLeft ();
			}
		}

		public Vert getTailB () {
			// if(this.getValueB()<0){return this.getRight();} else{return
			// this.getLeft();}
			if (this.getValueB () == this.getValueA ()) {
				return this.getLeft ();
			} else {
				return this.getRight ();
			}
		}

	}// end SB

	/***************************************************************************
	 * Class Vert for Vertices Every SB has a left and right Vert Vert contains
	 * Variables black and grey which point to other Verts
	 **************************************************************************/
	class Vert {
		private Vert black; // black line

		private Vert grey; // grey arc

		private SB sb; // point to parent SB

		private String gene; // name the Vert

		private String isCap; // name the cap ie. A1 A2 B1 B2...

		private String chromosome; // currently not used

		private boolean seenCap;// quick fix to know what caps were

		// visited for closing AA,AB, and BB paths
		// constructor
		public Vert () {
			black = null;
			grey = null;
			sb = null;
			gene = "";
			chromosome = "";
			isCap = "";
			seenCap = false;
		}// end Vert constructor

		// Vert constructor, doesn't assign SB
		public Vert (Vert b, Vert g, String s) {
			this.black = b;
			this.grey = g;
			this.gene = s;
			sb = null;
			chromosome = "";
			isCap = "";
			seenCap = false;
		}

		public Vert (String s) {
			this.black = null;
			this.grey = null;
			this.gene = s;
			sb = null;
			chromosome = "";
			isCap = "";
			seenCap = false;
		}

		// MUTATOR methods
		public void setBlack (Vert v) {
			this.black = v;
		}

		public void setGrey (Vert v) {
			this.grey = v;
		}

		public void setSB (SB s) {
			this.sb = s;
		}

		public void setGene (String s) {
			this.gene = s;
		}

		// name the chromosome the vertex belongs to
		public void setChromosome (String s) {
			this.chromosome = s;
		}

		// name the cap
		public void setIsCap (String b) {
			this.isCap = b;
		}

		// cap a vertex, true is A cap(black line), false is B cap (grey line)
		public void setSeen (boolean b) {
			this.seenCap = b;
		}

		// generates a new Vert object that has the profile of a cap.
		public Vert cap (boolean b) {
			Vert newcap;
			if (b) {
				newcap = new Vert ("A");
				newcap.setIsCap (newcap.getGene ());
				newcap.setSB (new SB ());
				newcap.setBlack (newcap);
				newcap.setGrey (newcap);
				this.joinBlack (newcap);
				newcap.setGene ("" + newcap.getGene () + (++counta));
			} else {
				newcap = new Vert ("B");
				newcap.setIsCap (newcap.getGene ());
				newcap.setSB (new SB ());
				newcap.setBlack (newcap);
				newcap.setGrey (newcap);
				this.joinGrey (newcap);
				newcap.setGene ("" + newcap.getGene () + (++countb));
			}
			return newcap;
		}

		// retrieval methods
		public String getGene () {
			return this.gene;
		}

		public Vert getBlack () {
			return this.black;
		}

		public Vert getGrey () {
			return this.grey;
		}

		public SB getSB () {
			return this.sb;
		}

		public String getCap () {
			return this.isCap;
		}

		public boolean isCap () {
			return ((this.getCap ()).length () > 0);
		}

		public String getChromosome () {
			return this.chromosome;
		}

		public boolean seen () {
			return this.seenCap;
		}

		// print methods
		// prints the main variables of Vert, try-catch for events where vars
		// have not been initialized.
		public void printMe () {
			buf.append ("Block: ");
			try {
				buf.append (this.getGene ());
			} catch (Exception e) {
			}
			buf.append ("	black:");

			try {
				buf.append (this.getBlack ().getGene ());
			} catch (Exception e) {
			}
			buf.append ("	Grey: ");

			try {
				buf.append (this.getGrey ().getGene ());
			} catch (Exception e) {
			}
			buf.append ("	SB: ");

			try {
				buf.append (this.getSB ().getIdentity ());
			} catch (Exception e) {
			}
			if (this.isCap ()) {
				buf.append ("	cap:" + this.getCap ());
			}
			buf.append ("\n");
		}

		// traverse capped genome with alternating black/grey paths (for closing
		// paths to generate HP cycle) and returns the end cap
		public Vert traversePath (boolean b) {
			if (this.isCap ())
				return this;
			if (b)
				return this.getBlack ().traversePath (false);
			return this.getGrey ().traversePath (true);
		}

		// overloaded method, same as previous but populates a vector
		public Vert traversePath (boolean b, Vector v) {
			// this.printMe();
			// populate greyVector (vector of grey lines)
			if (!b) {
				v.addElement (this);
			}
			if (this.isCap ())
				return this;
			if (b)
				return this.getBlack ().traversePath (false, v);
			return this.getGrey ().traversePath (true, v);
		}

		// this = a = cap, b is another cap. closepath forms a cycle, the method
		// depends on the Cap values (A&B A&A or B&B)
		public void closePath (Vert b) {
			// a and b are both B caps
			if ((this.getCap ().equals (b.getCap ()))
					&& (this.getCap ().equals ("B"))) {
				this.joinBlack (b);
			}
			// a and b are both A caps
			if ((this.getCap ().equals (b.getCap ()))
					&& (this.getCap ().equals ("A"))) {
				this.joinGrey (b);
			}
			// a!=b and a is an A cap (black line out)
			if (!(this.getCap ().equals (b.getCap ()))
					&& this.getCap ().equals ("A")) {
				this.setGrey (b.getGrey ());
				(b.getGrey ()).setGrey (this);
			}
			// a!=b and a is a B cap (grey line out) - is reverse of above code
			if (!(this.getCap ().equals (b.getCap ()))
					&& this.getCap ().equals ("B")) {
				b.setGrey (this.getGrey ());
				(this.getGrey ()).setGrey (b);
			}
		}// end closepath

		// used in DCJ moves and joining caps to form cycles
		public void joinBlack (Vert b) {
			this.setBlack (b);
			b.setBlack (this);
		}

		// used in joining two B caps to form cycles
		public void joinGrey (Vert b) {
			this.setGrey (b);
			b.setGrey (this);
		}// end joinGrey

		// tests if the Vert exists as a 1-cycle. Which is characterized as a
		// black and grey line pointing to the same Gene
		public boolean isOneCycle () {
			return (this.grey == this.black);
		}

	}// end Vert

	// ////////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////////////////
	/***************************************************************************
	 * Class DCJ Loads the data and parses Creates and populates Vectors for SBs
	 * and grey lines in the process.
	 **************************************************************************/
	public int counta = 0;

	public int countb = 0;

	public int countDCJ = 0;

	public int bufsize = 256;

	public StringBuffer buf; // buffer holding log output

	public StringBuffer opBuf; // operation buffer

	public boolean suppress = false;

	String speciesA = "";

	String speciesB = "";

	Vector capVector;

	Vector sbVector;

	Vector greyVector;

	public DCJ () {
		this.capVector = new Vector ();
		this.sbVector = new Vector ();
		greyVector = new Vector ();
		buf = new StringBuffer (bufsize);
	}

	public DCJ (StringTokenizer A, StringTokenizer B) {
		buf = new StringBuffer (bufsize);
		opBuf = new StringBuffer ();
		this.capVector = new Vector ();
		this.sbVector = new Vector ();
		greyVector = new Vector ();
		loadGenome (A, B);
		this.printSBV ();
		this.cycleDCJ ();

	}

	public DCJ (StringTokenizer A, StringTokenizer B, String species1,
			String species2) {
		buf = new StringBuffer (bufsize);
		opBuf = new StringBuffer ();
		speciesA = species1;
		speciesB = species2;
		this.capVector = new Vector ();
		this.sbVector = new Vector ();
		greyVector = new Vector ();
		loadGenome (A, B);
		this.printSBV ();
		this.cycleDCJ ();

	}

	public void cycleDCJ () {
		Vert temp;
		if (!suppress)
			buf.append (greyVector.size () + " Grey lines\n");
		this.printSBBlack ();
		while (greyVector.size () > 0) {
			temp = (Vert) greyVector.firstElement ();
			if (this.DCJ (temp)) {
				if (!suppress) {
					this.printSBBlack ();
				}
			}
		}// end while
		if (suppress)
			buf.append (countDCJ + "	\n");
	}

	public String getLog () {
		return this.buf.toString ();
	}

	public String getOpBuf () {
		return this.opBuf.toString ();
	}

	// takes an sbArray which is the SB vector converted into an array
	// String s is genome B that is tokenized and parsed into an array
	// of integers
	// genome B values are empty and are set using intArray values.
	// grey arcs are they formed from head to tail
	// caps are then formed on both ends
	// run time is O(n) n=size of s.

	public int getCount () {
		return countDCJ;
	}

	// DCJ operation. 4 verts for v.DCJ(): v, v.black, v.grey, and v.black.grey
	public boolean DCJ (Vert v) {
		StringBuffer tempbuf = new StringBuffer ();
		// check for 1 cycle first, if it is, delete and move on
		// check because a previous call may have generated 2 1-cycles.
		if (v.isOneCycle ()) {
			// code for removing 1-cycle from Vector of lines, this 1-cycle is
			// formed from a previous call that was a 2-cycle forked into 2
			// 1-cycles, it is not counted as a DCJ call
			// remove(object) from a vector is O(n) time however since the
			// object is always the first object, it's constant.
			this.greyVector.remove (v);
		} else {
			// don't mess with the following orders
			// v.black -join- v.grey.black
			++countDCJ;
			if (!suppress) {
				tempbuf
						.append (countDCJ
								+ " JOIN: "
								+ v.getBlack ().getBlack ().getGene ()
								+ " to "
								+ v.getGrey ().getBlack ().getBlack ()
										.getGene () + "	");
			}
			v.getBlack ().joinBlack (v.getGrey ().getBlack ());
			// v -join- v.grey, this will always form a 1-cycle
			if (!suppress) {
				tempbuf.append ("JOIN: " + v.getBlack ().getGene () + " to "
						+ v.getGrey ().getBlack ().getGene () + "\n");
			}
			v.joinBlack (v.getGrey ());
			this.greyVector.remove (this); // remove The 1-cycle formed from
			// the DCJ
			buf.append (tempbuf.toString ());
			opBuf.append (tempbuf.toString ());
			return true;
		}
		buf.append (tempbuf.toString ());
		opBuf.append (tempbuf.toString ());
		return false;
	}// end DCJ

	public void parseAndLoadGrey (String s, SB [] sbArray) {
		int [] intArray = this.toIntArray (s);
		int count = 0;
		// use intArray[] to manipulate existing objects in sbArray[] (join
		// grey)
		for (int i = 0; i < intArray.length - 1; i++) {
			sbArray[Math.abs (intArray[i]) - 1].setValueB (intArray[i]);
			sbArray[Math.abs (intArray[i + 1]) - 1].setValueB (intArray[i + 1]);
			sbArray[Math.abs (intArray[i]) - 1].getHeadB ().joinGrey (
					sbArray[Math.abs (intArray[i + 1]) - 1].getTailB ());
			// sbArray[Math.abs(intArray[i])-1].getRight().joinGrey(sbArray[Math.abs(intArray[i+1])-1].getLeft());

		}// end for
		capVector.add (sbArray[Math.abs (intArray[0]) - 1].getTailB ().cap (
				false));
		capVector.add (sbArray[Math.abs (intArray[intArray.length - 1]) - 1]
				.getHeadB ().cap (false));
	}

	// parses string s, adds a capped sb vector with black lines
	// connected to global vector, and populates the cap vector.
	// This method would have to be called k times where k is the number of
	// chromosomes. run time of O(n)
	public void parseAndLoadBlack (String s) {
		StringTokenizer token = new StringTokenizer (s);
		String tempInt; // for parsed strings of s
		SB tempSB; // temp SB
		Vector tempSbVector = new Vector (); // vector of SBs that're created
		while (token.hasMoreTokens ()) {
			tempInt = token.nextToken ().trim ();
			tempSB = new SB (new Vert (tempInt + "L"), new Vert (tempInt + "R"));
			tempSB.setVertPointers ();
			tempSB.turnValue (Integer.parseInt (tempInt));
			tempSbVector.add (tempSB);
			// System.out.print(tempInt+" ");
		}// end while
		for (int i = 0; i < tempSbVector.size () - 1; i++) {
			// CONNECT black lines
			((SB) tempSbVector.elementAt (i)).getRight ().joinBlack (
					((SB) tempSbVector.elementAt (i + 1)).getLeft ());
		}// end for
		// add A Caps to front and end of chromosome
		capVector.add ((((SB) tempSbVector.firstElement ()).getLeft ())
				.cap (true));
		capVector.add ((((SB) tempSbVector.lastElement ()).getRight ())
				.cap (true));

		this.sbVector.addAll (tempSbVector);
	}// end parseAndLoadBlack

	/***************************************************************************
	 * traverses the genome starting from caps from the cap vector checks if the
	 * caps have been visited, if visited it passes it otherwise it calls
	 * traversePath from class Vert. Then it closes the path from the start cap
	 * to the end cap. At this point the genome data structure is complete And
	 * lastly, it populates the grey vector by iterating through the SB Vector
	 * and adding all grey lines.
	 **************************************************************************/
	public void traverseAndClose () {
		Vector dump = new Vector (); // dump for visited verts
		Vector greybin = new Vector ();
		Vert start;
		Vert end;
		for (int i = 0; i < this.capVector.size (); i++) {
			start = (Vert) this.capVector.elementAt (i);
			if (!start.seen ()) {
				if (start.getCap ().equals ("A")) {
					// start.printMe();
					end = start.getBlack ().traversePath (false, greybin);
				} else { // is a B cap
					greybin.add (start.getGrey ());
					end = start.getGrey ().traversePath (true, greybin);
				}
				start.setSeen (true);
				end.setSeen (true);
				start.closePath (end);
				dump.add (start);
			}// end if
		}// end for
		// this.greyVector=greybin;
		SB tempSB;
		for (int i = 0; i < sbVector.size (); i++) {
			tempSB = (SB) sbVector.elementAt (i);
			greyVector.add (tempSB.getLeft ().getGrey ());
			greyVector.add (tempSB.getRight ().getGrey ());
		}

		// this.capVector=dump;
	}// end traverseAndClose

	// generates an Array of SB Vector of size 0 to Max Value of genome.
	public SB [] toSBArray () {
		SB [] sbArray;
		SB tempSB;
		int max = 0; // max value of genes for array size
		for (Enumeration e = this.sbVector.elements (); e.hasMoreElements ();) {
			tempSB = (SB) e.nextElement ();
			if (max < Math.abs (tempSB.getValueA ()))
				max = Math.abs (tempSB.getValueA ());
		}
		sbArray = new SB [max];// size of array
		// load SB array indexed on gene values
		for (Enumeration e = this.sbVector.elements (); e.hasMoreElements ();) {
			tempSB = (SB) e.nextElement ();
			sbArray[Math.abs (tempSB.getValueA ()) - 1] = tempSB;
		}
		return sbArray;
	}

	public int [] toIntArray (String s) {
		StringTokenizer token = new StringTokenizer (s);
		int intArray[] = new int [token.countTokens ()];
		int count = 0;
		while (token.hasMoreTokens ()) {
			intArray[count++] = (Integer.parseInt (token.nextToken ()));
		}// end while hasMoreTokens
		return intArray;
	}// end toSBArray

	public void loadGenome (StringTokenizer tokenA, StringTokenizer tokenB) {
		while (tokenA.hasMoreTokens ()) {
			try {
				this.parseAndLoadBlack ((tokenA.nextToken ()).trim ());
			} catch (Exception e) {
				System.out.println ("loadBlack error");
				System.out.println (e);
			}
		}// end while
		// this.printSBV();
		SB [] sbArray = this.toSBArray ();
		while (tokenB.hasMoreTokens ()) {
			try {
				this.parseAndLoadGrey ((tokenB.nextToken ()).trim (), sbArray);
			} catch (Exception e) {
				System.out.println ("load Grey error");
				System.out.println (e);
			}
		}// end while
		this.traverseAndClose ();
	}

	public Vert traverseBlack (Vert last, SB current) {
		try {
			current.setVisited (true);
			Vert next = current.getLeft ().getBlack ();
			if (last == current.getLeft ()) {
				next = current.getRight ().getBlack ();
				buf.append (current.getValueA () + " ");
			} else {
				buf.append ((current.getValueA () * -1) + " ");
			}
			if (!next.isCap () && !next.getSB ().wasVisited ()) {
				return traverseBlack (next, next.getSB ());
			}
			return next;
		} catch (Exception e) {
			return last;
		}
	}

	public void resetSbVisit () {
		SB tempSB;
		for (Enumeration e = this.sbVector.elements (); e.hasMoreElements ();) {
			tempSB = (SB) e.nextElement ();
			tempSB.setVisited (false);
		}
	}

	// prints state of SB vector
	public void printSBV () {
		SB tempSB;
		for (Enumeration e = this.sbVector.elements (); e.hasMoreElements ();) {
			tempSB = (SB) e.nextElement ();
			tempSB.getLeft ().printMe ();
			tempSB.getRight ().printMe ();

		}// end for
		/***********************************************************************
		 * uncomment to print the caps** Vert tempCap; for(Enumeration
		 * e=this.capVector.elements();e.hasMoreElements();){
		 * tempCap=(Vert)e.nextElement(); tempCap.printMe(); }//end for
		 **********************************************************************/
	}

	// calls traverseBlack of class SB and prints the SB Values.
	// currently buggy
	public void printSBBlack () {
		Vert v;
		for (int i = 0; i < this.capVector.size (); i++) {
			v = (Vert) this.capVector.elementAt (i);
			if (!v.getBlack ().getSB ().wasVisited ()
					&& !v.getBlack ().isCap ()) {
				buf.append ("[" + v.getGene () + " ");
				v = traverseBlack (v.getBlack (), v.getBlack ().getSB ());
				buf.append (v.getGene () + "]\n");
			}
		}
		SB tempSB;
		for (Enumeration e = this.sbVector.elements (); e.hasMoreElements ();) {
			tempSB = (SB) e.nextElement ();
			if (!tempSB.wasVisited ()) {
				buf.append ("[CI ");
				traverseBlack (tempSB.getLeft (), tempSB);
				buf.append ("]\n");
			}
		}
		this.resetSbVisit ();
	}// end printSBBlack

}// end class DCJ
