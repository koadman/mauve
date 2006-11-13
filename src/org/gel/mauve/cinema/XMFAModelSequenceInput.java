package org.gel.mauve.cinema;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.gel.mauve.XmfaViewerModel;

import uk.ac.man.bioinf.apps.cinema.CinemaGuiModule;
import uk.ac.man.bioinf.io.ParserException;
import uk.ac.man.bioinf.io.ParserExceptionHandler;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;
import uk.ac.man.bioinf.sequence.identifier.SimpleIdentifier;


public class XMFAModelSequenceInput extends CinemaGuiModule implements ParserExceptionHandler
{
    private XMFAAlignmentParser parser = new XMFAAlignmentParser();
    
    public SequenceAlignment openAlignment(XmfaViewerModel model, int ordinal)
    {
        File file = model.getSrc(); 
        XMFASource source = new XMFASource(model, ordinal);
        SimpleIdentifier identifier = new SimpleIdentifier(source.getTitle(), source);
        try
        {
            SequenceAlignment sa = parser.parse(identifier, new FileReader(file), this);
            sa.addAlignmentListener(source);
            return sa;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public String getVersion()
    {
        return "version 0";
    }

    public void handleException(ParserException e)
    {
        System.err.println(e);
    }

}
