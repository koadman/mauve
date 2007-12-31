package org.gel.mauve.gui.editor;

import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.cinema.XMFAModelSequenceInput;
import org.gel.mauve.cinema.XMFASequenceModuleIdentifier;

import uk.ac.man.bioinf.apps.cinema.CinemaBootIdentifier;
import uk.ac.man.bioinf.apps.cinema.CinemaModuleFactoryInstance;
import uk.ac.man.bioinf.module.AbstractEnumeratedModuleIdentifier;
import uk.ac.man.bioinf.module.Module;
import uk.ac.man.bioinf.module.ModuleException;
import uk.ac.man.bioinf.module.ModuleFactoryInstance;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;

public class Editor {
	private ModuleFactoryInstance moduleFactory;

	private XmfaViewerModel model;

	public Editor (XmfaViewerModel model, int lcbIndex) {
		this.model = model;
		moduleFactory = new CinemaModuleFactoryInstance ();
		moduleFactory.addIdentifier (AbstractEnumeratedModuleIdentifier
				.getAllIdentifiers (CinemaBootIdentifier.class));
		try {
			Module module = moduleFactory
					.load (CinemaBootIdentifier.CINEMA_BOOT);
			XMFAModelSequenceInput input = (XMFAModelSequenceInput) module
					.getContext ().getModule (
							XMFASequenceModuleIdentifier.XMFA_MODEL_INPUT);
			SequenceAlignment sa = input.openAlignment (model, lcbIndex);
			input.setSequenceAlignment (sa);
		} catch (ModuleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace ();
		}
	}
}
