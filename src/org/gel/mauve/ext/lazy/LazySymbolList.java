package org.gel.mauve.ext.lazy;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.AbstractSymbolList;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;

public class LazySymbolList extends AbstractSymbolList {
	
	protected BufferedInputStream dna;
	protected FiniteAlphabet alphabet;
	protected long start;
	protected long end;
	protected int dna_length;
	
	protected static Hashtable <BufferedInputStream, Integer> open_streams = 
			new Hashtable <BufferedInputStream, Integer> ();
	//use rangeloadtracker for lazyloading from server as well as file
	
	public LazySymbolList (BufferedInputStream data, long start, long end, 
			int full_length) {
		this.start = start;
		this.end = end;
		dna = data;
		dna_length = full_length;
		alphabet = DNATools.getDNA();
		synchronized (dna) {
			if (open_streams.get(dna) == null) {
				open_streams.put(dna, 0);
			}
		}
	}

	public Alphabet getAlphabet() {
		return alphabet;
	}

	public int length() {
		return (int) (end - start + 1);
	}

	public Symbol symbolAt(int arg0) throws IndexOutOfBoundsException {
		int symbol = ((int) start) + arg0 - 2;
		if (symbol >= end - 1)
			throw new IndexOutOfBoundsException ("Symbol " + arg0 + " out of range.");
		else {
			synchronized (dna) {
				try {
					int pos = open_streams.get(dna);
					if (pos > symbol) {
						dna.reset();
						dna.mark(dna_length);
						pos = 0;
					}
					dna.skip(symbol - pos);
					open_streams.put(dna, symbol + 1);
					return DNATools.forSymbol((char) dna.read());
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		}
	}

}
