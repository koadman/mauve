package org.gel.mauve.gui;

import java.util.EventObject;

public class HintMessageEvent extends EventObject{
	static final long serialVersionUID = 2342334;
	String message;
	public HintMessageEvent( RearrangementPanel source, String message )
	{
		super(source);
		this.message = message;
	}
	String getMessage()
	{
		return message;
	}
}
