package org.gel.mauve.module;

import java.io.File;

import org.gel.mauve.ModelBuilder;
import org.gel.mauve.MyConsole;
import org.gel.mauve.gui.FrameLoader;
import org.gel.mauve.gui.Mauve;
import org.gel.mauve.gui.MauveFrame;

public class MauveModule extends Mauve {
	
	protected ModuleListener mod_list;
	
	public MauveModule (ModuleListener ml) {
		mod_list = ml;
	}
	
	public void init (final Object source, final String factory) {
		MyConsole.setUseSwing (true);
		MyConsole.showConsole ();
		javax.swing.SwingUtilities.invokeLater (new Runnable () {
			public void run () {
				init ();
				loadObject (source, factory);
			}
		});
	}
	/**
	 * Loads a stash into a rearrangement panel
	 */
	public void loadObject (Object source, String factory) {
		MauveFrame frame = getNewFrame ();
		Thread t = new Thread (new FrameLoader (frame, source, factory));
		t.start ();
	}

	protected MauveFrame makeNewFrame() {
		MauveModuleFrame frame = new MauveModuleFrame (this);
		frames.add (frame);
		return frame;
	}
	
	

}
