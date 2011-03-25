package org.gel.mauve.format;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.SimpleAnnotation;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SimpleFeatureHolder;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.Feature.Template;
import org.biojava.bio.seq.impl.SimpleStrandedFeature;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.Edit;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.PackedSymbolListFactory;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.ontology.OntoTools;
import org.biojava.utils.ChangeListener;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.Changeable;
import org.biojava.utils.Unchangeable;
import org.biojavax.bio.seq.RichFeature;
import org.gel.mauve.FilterCacheSpec;
import org.gel.mauve.SupportedFormat;

class DelegatingSequence implements Sequence {
	protected File source;

	protected int sequenceIndex;

	protected Annotation annotation;

	protected Alphabet alphabet;

	protected int length;

	protected String name;

	protected String urn;

	protected int featureCount;

	protected Map filterCache = new HashMap ();

	protected SupportedFormat format;
	
	protected boolean circular;

	// Delegate changeable stuff to unchangeable implementation
	Changeable ch = new Unchangeable ();

	SymbolList packedList = null; // a packed symbol list

	public DelegatingSequence (Sequence s, SupportedFormat format, File source,
			int index) throws FileNotFoundException {
		format.validate (s, source, index);

		this.format = format;
		this.source = source;
		this.sequenceIndex = index;

		init (s);
	}

	protected void init (Sequence s) {
		PackedSymbolListFactory pslFactory = new PackedSymbolListFactory ();
		Symbol [] symArray = new Symbol [s.length ()];
		int symI = 0;
		for (Iterator symIter = s.iterator (); symIter.hasNext ();) {
			symArray[symI++] = (Symbol) symIter.next ();
		}
		try {
        	packedList = pslFactory.makeSymbolList(symArray, s.length(), s.getAlphabet());
        }catch(IllegalAlphabetException iae)
        {
			iae.printStackTrace ();
		}

		annotation = new SimpleAnnotation (s.getAnnotation ());
		alphabet = s.getAlphabet ();
		length = s.length ();
		name = format.getChromosomeName (s);
		//name = s.getName ();
		urn = s.getURN ();
		featureCount = s.countFeatures ();

		for (int i = 0; i < format.getFilterCacheSpecs ().length; i++) {
			addToCache (s, format.getFilterCacheSpecs ()[i]);
		}

		completeInit (s);
	}

	protected void completeInit (Sequence s) {
		// Added for subclass convenience.
	}

	// /////////////////////////////////////////////////////////////////
	// Begin Sequence-specific implementation
	public String getName () {
		return name;
	}

	public String getURN () {
		return urn;
	}

	// End Sequence-specific implementation
	// /////////////////////////////////////////////////////////////////

	// /////////////////////////////////////////////////////////////////
	// Begin ChangeListener implementation.
	/**
	 * @deprecated
	 */
//	@Deprecated
	public void addChangeListener (ChangeListener cl) {
		ch.addChangeListener (cl);
	}

	public void addChangeListener (ChangeListener cl, ChangeType ct) {
		ch.addChangeListener (cl, ct);
	}

	/**
	 * @deprecated
	 */
//	@Deprecated
	public void removeChangeListener (ChangeListener cl) {
		ch.removeChangeListener (cl);
	}

	public void removeChangeListener (ChangeListener cl, ChangeType ct) {
		ch.removeChangeListener (cl, ct);
	}

	public boolean isUnchanging (ChangeType ct) {
		return ch.isUnchanging (ct);
	}

	// End ChangeListener implementation.
	// /////////////////////////////////////////////////////////////////

	// /////////////////////////////////////////////////////////////////
	// Begin Annotatable implementation.
	public Annotation getAnnotation () {
		return annotation;
	}

	// End Annotatable implementation.
	// /////////////////////////////////////////////////////////////////

	// /////////////////////////////////////////////////////////////////
	// Begin SymbolList implementation.
	public Alphabet getAlphabet () {
		return packedList.getAlphabet ();
	}

	public int length () {
		return length;
	}

	public Symbol symbolAt (int index) throws IndexOutOfBoundsException {
		return packedList.symbolAt (index);
	}

	public List toList () {
		return packedList.toList ();
	}

	public Iterator iterator () {
		return packedList.iterator ();
	}

	public SymbolList subList (int start, int end)
			throws IndexOutOfBoundsException {
		return new DelegatingSublist (start, end);
	}

	public String seqString () {
		return packedList.seqString ();
	}

	public String subStr (int start, int end) throws IndexOutOfBoundsException {
		return packedList.subStr (start, end);
	}

	public void edit (Edit edit) throws IllegalAlphabetException,
			ChangeVetoException {
		throw new ChangeVetoException ();
	}

    public int countFeatures () {
		return featureCount;
	}

	public Iterator features () {
		Sequence s = format.readInnerSequence (source, sequenceIndex);
		return s.features ();
	}

	public FeatureHolder filter (FeatureFilter fc, boolean recurse) {
		FeatureHolder fh;
		// if this is of the form And(SomeFilter,OverlapsLocation)
		// used by TranslatedSequencePanels, see if we have
		// data corresponding to SomeFilter in one of the cache entries
		if (fc instanceof FeatureFilter.And
				&& ((FeatureFilter.And) fc).getChild2 () instanceof FeatureFilter.OverlapsLocation) {
			fh = getCachedFilterResults (((FeatureFilter.And) fc).getChild1 ());
		} else {
			fh = format.readInnerSequence (source, sequenceIndex);
		}
		return fh.filter (fc, recurse);
	}

	private FeatureHolder getCachedFilterResults (FeatureFilter ff) {
		if (!filterCache.containsKey (ff)) {
			Sequence s = format.readInnerSequence (source, sequenceIndex);
			addToCache (s, new FilterCacheSpec (ff));
		}

		return (FeatureHolder) filterCache.get (ff);
	}

	private void addToCache (Sequence s, FilterCacheSpec filterCacheSpec) {
		FeatureHolder results = s.filter (filterCacheSpec.filter, true);

		// Save them.
		SimpleFeatureHolder sfh = new SimpleFeatureHolder ();
		Iterator i = results.features ();
		while (i.hasNext ()) {
			Feature f = (Feature) i.next ();
			Feature sf = null;
			if (f instanceof StrandedFeature)
				sf = makeThinFeature ((StrandedFeature) f, filterCacheSpec);
			else
				System.err.println ("found one that ain't stranded");
			try {
				sfh.addFeature (sf);
			} catch (ChangeVetoException e) {
				throw new Error (e);
			}
		}
		filterCache.put (filterCacheSpec.filter, sfh);
	}
	
	protected SimpleStrandedFeature makeThinFeature (StrandedFeature f, FilterCacheSpec spec) {
		return makeThinFeature (this, f, spec);
	}

	/**
	 * @param f
	 * @return
	 * @throws ChangeVetoException
	 * 
	 * Make a slimmed-down copy of the feature for caching.
	 * 
	 */
	protected static SimpleStrandedFeature makeThinFeature (Sequence sequence, 
			StrandedFeature f, FilterCacheSpec spec) {
		Feature.Template template = f.makeTemplate ();
		if (f.getAnnotation () != null && spec.getAnnotations () != null
				&& spec.getAnnotations ().length > 0) {
			if (spec.getAnnotations ()[0]
					.equals (FilterCacheSpec.ALL_ANNOTATIONS)) {
				// Don't replace any annotations.
			} else {
				Annotation a = new SimpleAnnotation ();
				for (int i = 0; i < spec.getAnnotations ().length; i++) {
					String property = spec.getAnnotations ()[i];

					if (f.getAnnotation ().containsProperty (property)) {
						try {
							a.setProperty (property, f.getAnnotation ()
									.getProperty (property));
						} catch (ChangeVetoException e) {
							// We don't expect an exception here.
							throw new RuntimeException (e);
						}
					}
				}
				template.annotation = a;
			}
		} else {
			// Strip out everything except location and type
			template.annotation = Annotation.EMPTY_ANNOTATION;
		}
		template.source = null;
		template.sourceTerm = OntoTools.ANY;
        if(template instanceof RichFeature.Template)
        {
        	SimpleStrandedFeature sf = new SimpleStrandedFeature(sequence, sequence, 
        			new RichStrandedFeatureTemplate((RichFeature.Template)template));
        	return sf;
        }else{
        	SimpleStrandedFeature sf = new SimpleStrandedFeature(sequence, sequence,
        			(StrandedFeature.Template)template);
        	return sf;
        }
	}

	public FeatureHolder filter (FeatureFilter fc) {
		FeatureHolder fh;
		// if this is of the form And(SomeFilter,OverlapsLocation)
		// used by TranslatedSequencePanels, see if we have
		// data corresponding to SomeFilter in one of the cache entries
		if (fc instanceof FeatureFilter.And
				&& ((FeatureFilter.And) fc).getChild2 () instanceof FeatureFilter.OverlapsLocation) {
			fh = getCachedFilterResults (((FeatureFilter.And) fc).getChild1 ());
		} else if (fc instanceof FeatureFilter.ByType) {
			fh = getCachedFilterResults (fc);
		} else {
			fh = format.readInnerSequence (source, sequenceIndex);
		}
		return fh.filter (fc);
	}

	public Feature createFeature (Template ft) throws BioException,
			ChangeVetoException {
		throw new ChangeVetoException ();
	}

	public void removeFeature (Feature f) throws ChangeVetoException,
			BioException {
		throw new ChangeVetoException ();
	}

	public boolean containsFeature (Feature f) {
		Sequence s = format.readInnerSequence (source, sequenceIndex);
		return s.containsFeature (f);
	}

	public FeatureFilter getSchema () {
		Sequence s = format.readInnerSequence (source, sequenceIndex);
		return s.getSchema ();
	}

	private class DelegatingSublist implements SymbolList {
		private int start;

		private int end;

		public DelegatingSublist (int start, int end) {
			this.start = start;
			this.end = end;
		}

		public Alphabet getAlphabet () {
			return alphabet;
		}

		public int length () {
			return end - start + 1;
		}

		public Symbol symbolAt (int index) throws IndexOutOfBoundsException {
			return DelegatingSequence.this.symbolAt (index + start - 1);
		}

		public List toList () {
			throw new Error ("Not implemented");
		}

		public Iterator iterator () {
			throw new Error ("Not implemented");
		}

		public SymbolList subList (int start, int end)
				throws IndexOutOfBoundsException {
			return new DelegatingSublist (this.start + start - 1, this.start
					+ end - 1);
		}

		public String seqString () {
			return packedList.subStr (start, end);
			//throw new Error ("Not implemented");
		}

		public String subStr (int start, int end)
				throws IndexOutOfBoundsException {
			throw new Error ("Not implemented");
		}

		public void edit (Edit edit) throws IndexOutOfBoundsException,
				IllegalAlphabetException, ChangeVetoException {
			throw new ChangeVetoException ();
		}

		/**
		 * @deprecated
		 */
//		@Deprecated
		public void addChangeListener (ChangeListener cl) {
			ch.addChangeListener (cl);
		}

		public void addChangeListener (ChangeListener cl, ChangeType ct) {
			ch.addChangeListener (cl, ct);
		}

		/**
		 * @deprecated
		 */
		public void removeChangeListener (ChangeListener cl) {
			ch.removeChangeListener (cl);
		}

		public void removeChangeListener (ChangeListener cl, ChangeType ct) {
			ch.removeChangeListener (cl, ct);
		}

		public boolean isUnchanging (ChangeType ct) {
			return true;
		}

	}

}