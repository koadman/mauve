package org.gel.mauve.gui;

import java.util.EventListener;

public interface AlignmentProcessListener extends EventListener {
    public void completeAlignment(int retcode);
}
