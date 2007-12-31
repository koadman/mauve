package org.gel.mauve.gui;

import java.util.EventListener;

public interface HintMessageListener extends EventListener {
	public void messageChanged(HintMessageEvent hme);
}
