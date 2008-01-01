package org.gel.mauve.ext.lazy;

import java.util.Iterator;
import java.util.List;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.Feature.Template;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.Edit;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeListener;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;

public class LazyLoadSequence implements Sequence {

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getURN() {
		// TODO Auto-generated method stub
		return null;
	}

	public void edit(Edit edit) throws IndexOutOfBoundsException,
			IllegalAlphabetException, ChangeVetoException {
		// TODO Auto-generated method stub

	}

	public Alphabet getAlphabet() {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterator iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public int length() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String seqString() {
		// TODO Auto-generated method stub
		return null;
	}

	public SymbolList subList(int start, int end)
			throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public String subStr(int start, int end) throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public Symbol symbolAt(int index) throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	public List toList() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addChangeListener(ChangeListener cl) {
		// TODO Auto-generated method stub

	}

	public void addChangeListener(ChangeListener cl, ChangeType ct) {
		// TODO Auto-generated method stub

	}

	public boolean isUnchanging(ChangeType ct) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeChangeListener(ChangeListener cl) {
		// TODO Auto-generated method stub

	}

	public void removeChangeListener(ChangeListener cl, ChangeType ct) {
		// TODO Auto-generated method stub

	}

	public boolean containsFeature(Feature f) {
		// TODO Auto-generated method stub
		return false;
	}

	public int countFeatures() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Feature createFeature(Template ft) throws BioException,
			ChangeVetoException {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterator features() {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureHolder filter(FeatureFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureHolder filter(FeatureFilter fc, boolean recurse) {
		// TODO Auto-generated method stub
		return null;
	}

	public FeatureFilter getSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeFeature(Feature f) throws ChangeVetoException,
			BioException {
		// TODO Auto-generated method stub

	}

	public Annotation getAnnotation() {
		// TODO Auto-generated method stub
		return null;
	}

}
