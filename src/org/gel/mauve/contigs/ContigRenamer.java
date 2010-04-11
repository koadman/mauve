package org.gel.mauve.contigs;

import java.util.Hashtable;
import java.util.Iterator;

import org.biojava.bio.seq.Feature;
import org.gel.mauve.Chromosome;
import org.gel.mauve.MauveHelperFunctions;

/**
 * This class does nothing. 
 * 
 * @author andrew
 *
 */
@Deprecated
public class ContigRenamer {
/*	
	private Hashtable names;
	
	public void process () {
		names = new Hashtable ();
		Iterator itty = fix.getChromosomes ().iterator ();
		int length = (fix.getChromosomes ().size () + "").length ();
		int number = 0;
		while (itty.hasNext ()) {
			Chromosome chrom = (Chromosome) itty.next ();
			String add = getContigName (chrom);
			chrom.setName (add);
			names.put (chrom.getName (), add);
			ordered.add (chrom);
		}
	}
	
	public String getContigName (Chromosome chrom) {
		return chrom.getName ();
	}


	/**
	 * @param args
	
	public static void main (String [] args) {
		new ContigRenamer ().init (args);
	}
*/
}
