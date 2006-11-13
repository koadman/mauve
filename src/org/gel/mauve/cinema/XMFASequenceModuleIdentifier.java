package org.gel.mauve.cinema;

import uk.ac.man.bioinf.module.AbstractEnumeratedModuleIdentifier;


public class XMFASequenceModuleIdentifier extends AbstractEnumeratedModuleIdentifier
{
    private XMFASequenceModuleIdentifier(String className, String toString)
    {
        super(className, toString);
    }

    public static final XMFASequenceModuleIdentifier XMFA_MODEL_INPUT = 
        new XMFASequenceModuleIdentifier( "org.gel.mauve.cinema.XMFAModelSequenceInput",
    						"Provides input from an XMFA model into cinema" );
    
    public static final XMFASequenceModuleIdentifier XMFA_MODEL_OUTPUT = 
        new XMFASequenceModuleIdentifier( "org.gel.mauve.cinema.XMFAModelSequenceOutput",
    						"Provides input from an XMFA model into cinema" );

    public static final XMFASequenceModuleIdentifier XMFA_COMMAND_LINE_PARSER = 
        new XMFASequenceModuleIdentifier( "org.gel.mauve.cinema.XMFACommandLineParser",
    						"Customized command-line parser." );

    public static final XMFASequenceModuleIdentifier XMFA_STATUS_INFORMATION = 
        new XMFASequenceModuleIdentifier( "org.gel.mauve.cinema.XMFAStatusInformation",
    						"Customized status bar updater." );

}
