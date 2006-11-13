package org.gel.mauve;

import java.util.EventListener;

public interface ModelListener extends EventListener
{
    public void colorChanged(ModelEvent event);
    public void weightChanged(ModelEvent event);
    public void drawingSettingsChanged(ModelEvent event);
    public void modeChanged(ModelEvent event);
    public void modelReloadStart(ModelEvent event);
    public void modelReloadEnd(ModelEvent event);
    public void viewableRangeChangeStart(ModelEvent event);
    public void viewableRangeChanged(ModelEvent event);
    public void viewableRangeChangeEnd(ModelEvent event);
    public void genomesReordered(ModelEvent event);
    public void referenceChanged(ModelEvent event);
}
