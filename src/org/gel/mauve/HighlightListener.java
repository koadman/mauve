package org.gel.mauve;

import java.util.EventListener;

public interface HighlightListener extends EventListener
{
    public void highlightChanged(ModelEvent evt);
}
