package org.gel.mauve;

import java.util.EventObject;


public class ModelEvent extends EventObject
{
	static final long serialVersionUID = 234234;
    public ModelEvent(BaseViewerModel source)
    {
        super(source);
    }
}
