package org.gel.air.ja.stash;

public interface StashListListener {

	public void elementAdded (StashList source, Object what);
	public void elementRemoved (StashList source, Object what);

}//interface VectorListener