package org.gel.mauve;

import java.util.EventObject;


public class ModelEvent extends EventObject
{
    public ModelEvent(BaseViewerModel source)
    {
        super(source);
    }
}
