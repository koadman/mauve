package org.gel.mauve.contigs;

import java.util.Hashtable;
import java.util.Iterator;

import org.biojava.bio.seq.Feature;
import org.gel.mauve.Chromosome;
import org.gel.mauve.MauveHelperFunctions;

public class ContigRenamer extends ContigReorderer {
	
	public Hashtable names;
	
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
	 * removes number and underscore from beginning of name.
	 * 
	 * @param chrom
	 * @param format_length
	 * @return
	 */
	public String getContigName (Chromosome chrom, int format_length) {
		String add = chrom.getName ();
		return add.substring (format_length + 1, add.length ());
	}
	/**
	 * for renaming contigs to prepend their current order to their name.
	 * 
	 * @param chrom
	 * @param number
	 * @param format_length
	 * @return
	 */
	public String getContigName (Chromosome chrom, int number, int format_length) {
		String add = number++ + "_";
		while (add.length () <= format_length)
			add = "0" + add;
		add = add + chrom.getName ();
		names.put (chrom.getName (), add);
		return add;
	}

	/**
	 * @param args
	 */
	public static void main (String [] args) {
		new ContigRenamer ().init (args);
	}

}
