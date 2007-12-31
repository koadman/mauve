/* 
 * This software was written by Phillip Lord (p.lord@russet.org.uk)
 * whilst at the University of Manchester.
 *
 * The initial code base is copyright the University of Manchester. 
 * Modifications to the initial code base are copyright
 * of their respective authors, or their employers as appropriate. 
 * Authorship of the modifications may be determined from the ChangeLog
 * placed at the end of this file
 */

package uk.ac.man.bioinf.apps.cinema.color; 
import uk.ac.man.bioinf.analysis.consensus.ConsensusSequence;
import uk.ac.man.bioinf.analysis.misc.PercentageIDCalculatorManager;
import uk.ac.man.bioinf.apps.cinema.CinemaModule;
import uk.ac.man.bioinf.gui.color.ColorList;
import uk.ac.man.bioinf.gui.color.ColorMap;
import uk.ac.man.bioinf.gui.color.PercentIDVarianceColorMap;
import uk.ac.man.bioinf.gui.viewer.JAlignmentViewer;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;

// Package name inserted by Jde-Package


/**
 * GrayScaleVariance.java
 *
 *
 * Created: Fri Nov 30 13:58:29 2001
 *
 * @author Phillip Lord
 * @version $Id: GrayScaleVariance.java,v 1.1 2002/03/08 14:49:40 lord Exp $
 */

public class GrayScaleVariance implements ColorMapMetaData
{


  public void setModule( CinemaModule module )
  {
    // we don't need this module
  }
  
  public String getColorMapName()
  {
    return "GrayScale Variance (% ID)";
  }
  

  public ColorMap getInstance( SequenceAlignment alignment, JAlignmentViewer viewer )
  {
    return getCreateInstance( alignment );
  }
  
  public ColorMap getConsensusInstance( ConsensusSequence sequence, JAlignmentViewer viewer )
  {
    return getCreateInstance( sequence.getConsensusCalculator().getSequenceAlignment() );
  }

  private PercentIDVarianceColorMap getCreateInstance( SequenceAlignment alignment )
  {
    return new PercentIDVarianceColorMap
      ( PercentageIDCalculatorManager.getDefaultInstance().getCalculator( alignment ),
        ColorList.getGray50List() );
  }
} // GrayScaleVariance



/*
 * ChangeLog
 * $Log: GrayScaleVariance.java,v $
 * Revision 1.1  2002/03/08 14:49:40  lord
 * Initial checkin
 *
 */
