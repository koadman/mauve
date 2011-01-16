package org.gel.mauve.contigs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.biojava.bio.seq.Feature;
import org.gel.mauve.Chromosome;
import org.gel.mauve.MauveHelperFunctions;

public class FeatureFixer extends ContigReorderer {
	
	public void readOrdered (String file) {
		try {
			Hashtable chroms = new Hashtable (fix.getChromosomes ().size ());
			BufferedReader in = new BufferedReader (new FileReader (file));
			String input = new String (in.readLine ());
			while (!input.trim ().equals (ContigFeatureWriter.ORDERED_CONTIGS))
				input = in.readLine ();
			input = in.readLine ();
			input = in.readLine ();
			while (input != null && !input.trim ().equals ("")) {
				StringTokenizer toke = new StringTokenizer (input);
				toke.nextToken ();
				String name = toke.nextToken ();
				System.out.println ("name: " + name);
				//ordered.add (chroms.get (name));
				chroms.put (name.substring (name.indexOf ("_") + 1, name.length ()), name);
				input = in.readLine ();
			}
			in.close ();
			System.out.println ("getting ordered");
			Iterator itty = fix.getChromosomes ().iterator ();
			while (itty.hasNext ()) {
				Chromosome chrom = (Chromosome) itty.next ();
				chrom.setName (((String) chroms.get (chrom.getName ())).trim ());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void process () {
		readOrdered (input_file);
		Iterator itty = fix.getChromosomes ().iterator ();
		int length = (fix.getChromosomes ().size () + "").length ();
		int number = 0;
		Hashtable contigs = new Hashtable ();
		while (itty.hasNext ()) {
			Chromosome chrom = (Chromosome) itty.next ();
			ordered.add (chrom);
			contigs.put (chrom.getName (), chrom);
		}
		try {
			BufferedReader in = new BufferedReader (new FileReader (feature_file));
			in.readLine ();
			String s = null; 
			while ((s = in.readLine ()) != null) {
				StringTokenizer toke = new StringTokenizer (s);
				s = toke.nextToken ();
				for (int i = 1; i <= 6; i++)
					toke.nextToken ();
				boolean flip = new Boolean (toke.nextToken ()).booleanValue ();
				System.out.println ("s: " + flip);
				if (flip)
					MauveHelperFunctions.addChromByStart (inverters, (Chromosome) contigs.get (s));
			}
			in.close ();
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}

	/**
	 * @param args
	 */
	public static void main (String [] args) {
		new FeatureFixer ().init (args);
	}

}
