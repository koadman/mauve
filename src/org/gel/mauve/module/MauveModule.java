package org.gel.mauve.module;

import org.gel.mauve.gui.Mauve;
import org.gel.mauve.gui.MauveFrame;

public class MauveModule extends Mauve {
	
	protected ModuleListener mod_list;
	
	public MauveModule (ModuleListener ml) {
		mod_list = ml;
	}

	protected MauveFrame makeNewFrame() {
		MauveModuleFrame frame = new MauveModuleFrame (this);
		frames.add (frame);
		return frame;
	}
	
	

}
