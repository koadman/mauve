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
import org.biojava.bio.symbol.SymbolList;
import org.gel.air.util.IOUtils;
import org.gel.mauve.ext.MauveStoreConstants;

public class LazySymbolList extends AbstractSymbolList implements MauveStoreConstants {
	
	protected BufferedInputStream dna;
	protected FiniteAlphabet alphabet;
	protected long start;
	protected long end;
	
	protected static Hashtable <BufferedInputStream, Integer> open_streams = 
			new Hashtable <BufferedInputStream, Integer> ();
	//use rangeloadtracker for lazyloading from server as well as file
	
	public LazySymbolList (BufferedInputStream data, long start, long end) {
		this.start = start;
		this.end = end;
		dna = data;
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
		int symbol = arg0 - 1;
		if (symbol > end - 1 || symbol < 0)
			return alphabet.getGapSymbol();
		else {
			synchronized (dna) {
				try {
					int pos = open_streams.get(dna);
					if (pos > symbol) {
						dna.reset();
						dna.mark((int) end);
						pos = 0;
					}
					if (symbol != pos)
						IOUtils.guaranteedSkip(dna, symbol - pos);
					open_streams.put(dna, symbol + 1);
					char which = (char) dna.read();
					return DNATools.forSymbol(which);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		}
	}
	
	public SymbolList subList (int start, int end) {
		System.out.println ("sublist: " + start + ", " + end);
		//new Exception ().printStackTrace();
		return super.subList (start, end);
	}

}
