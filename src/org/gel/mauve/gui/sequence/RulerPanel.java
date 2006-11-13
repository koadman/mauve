package org.gel.mauve.gui.sequence;

import java.awt.BorderLayout;

import org.biojava.bio.gui.sequence.RulerRenderer;
import org.biojava.bio.gui.sequence.TranslatedSequencePanel;
import org.biojava.bio.symbol.AbstractSymbolList;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.IntegerAlphabet;
import org.biojava.bio.symbol.Symbol;
import org.biojava.utils.ChangeVetoException;
import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Genome;
import org.gel.mauve.ModelEvent;

/**
 * 
 * Adapter class that allows use of Biojava ruler.
 *  
 */
public class RulerPanel extends AbstractSequencePanel
{
    private TranslatedSequencePanel sequencePanel;

    public RulerPanel(BaseViewerModel model, Genome genome)
    {
        super(model, genome);
        setLayout(new BorderLayout());
        sequencePanel = new TranslatedSequencePanel();
        
        final long rulerLength = getGenome().getLength();
        
        sequencePanel.setSequence(new AbstractSymbolList()
        {

            public Alphabet getAlphabet()
            {
                return IntegerAlphabet.INSTANCE;
            }

            public int length()
            {
                return (int) rulerLength;
            }

            public Symbol symbolAt(int index) throws IndexOutOfBoundsException
            {
                return IntegerAlphabet.INSTANCE.getSymbol(0);
            }

        });
        try
        {
            sequencePanel.setRenderer(new RulerRenderer());
        }
        catch (ChangeVetoException e)
        {
            // Totally unexpected.
            throw new RuntimeException(e);
        }
        add(sequencePanel, BorderLayout.CENTER);
        
        adjustScaleAndTranslation();
    }

    // This prevents the display of the translatedSequencePanel at the wrong scale...
    public void setBounds(int arg0, int arg1, int arg2, int arg3)
    {
        super.setBounds(arg0, arg1, arg2, arg3);

        adjustScaleAndTranslation();
    }
    
    
    /**
     * @param start
     * @param length
     */
    private void adjustScaleAndTranslation()
    {
        if (getWidth() != 0)
        {
            
            double scale = (double) getWidth() / (double) getGenome().getViewLength();
            if (getGenome().getViewStart() >= getGenome().getLength())
            {
                // TranslatedSequencePanel can't handle being translated out of
                // visibility.
                sequencePanel.setVisible(false);
            }
            else
            {
                sequencePanel.setScale(scale);
                sequencePanel.setSymbolTranslation((int) getGenome().getViewStart());
                sequencePanel.setVisible(true);
            }
        }
    }

    public void viewableRangeChanged(ModelEvent event)
    {
        adjustScaleAndTranslation();
    }
}