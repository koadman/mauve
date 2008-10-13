package org.gel.mauve.module;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.gui.Mauve;
import org.gel.mauve.gui.MauveFrame;

public class MauveModuleFrame extends MauveFrame {
	
	MauveModule module;

	public MauveModuleFrame(Mauve mauve) {
		super(mauve);
		module = (MauveModule) mauve;
	}

	public void setModel(BaseViewerModel model) {
		super.setModel(model);
		new Thread ( new Runnable () {
			public void run () {
				module.mod_list.startModule(MauveModuleFrame.this);
			}
		}).start ();
	}
	
	

}
