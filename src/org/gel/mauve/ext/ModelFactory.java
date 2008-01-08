package org.gel.mauve.ext;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.ModelProgressListener;

public interface ModelFactory {

	public BaseViewerModel createModel (Object source, ModelProgressListener listener);
	
	public String getUniqueName ();
}
