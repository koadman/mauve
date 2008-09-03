package org.gel.mauve.ext.lazy;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.biojava.bio.AbstractAnnotation;
import org.biojava.bio.Annotation;
import org.gel.air.ja.stash.Stash;

public class LazyAnnotation extends AbstractAnnotation {
	
	public static HashSet NOT_ALLOWED;
	protected Stash annos;


	public LazyAnnotation(Stash map) {
		super(map);
		annos = map;
	}
	
	public boolean containsProperty (Object key) {
		return !NOT_ALLOWED.contains(key) && annos.containsKey(key);
	}

	protected Map getProperties() {
		return annos;
	}
	
	public Object getProperty (Object key) {
		Object ret = super.getProperty(key);
		if (ret != null && NOT_ALLOWED.contains(key))
			ret = null;
		return ret;
	}
	
	public Set keys () {
		Set ret = super.keys();
		ret.removeAll(NOT_ALLOWED);
		return ret;
	}

	protected boolean propertiesAllocated() {
		return true;
	}
	
	public void removeProperty (Object key) {
		if (!NOT_ALLOWED.contains(key))
			super.removeProperty(key);
	}
	
	public void setProperty (Object key, Object value) {
		if (!NOT_ALLOWED.contains(key))
			super.setProperty (key, value);
	}

}
