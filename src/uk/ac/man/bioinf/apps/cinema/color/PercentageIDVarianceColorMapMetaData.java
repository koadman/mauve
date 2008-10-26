/* 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
*/
 
/* 
 * This software was written by Phillip Lord (p.lord@hgmp.mrc.ac.uk)
 * whilst at the University of Manchester as a Pfizer post-doctoral 
 * Research Fellow. 
 *
 * The initial code base is copyright by Pfizer, or the University
 * of Manchester. Modifications to the initial code base are copyright
 * of their respective authors, or their employers as appropriate. 
 * Authorship of the modifications may be determined from the ChangeLog
 * placed at the end of this file
 */

package uk.ac.man.bioinf.apps.cinema.color; // Package name inserted by JPack
import uk.ac.man.bioinf.analysis.consensus.ConsensusSequence;
import uk.ac.man.bioinf.analysis.misc.PercentageIDCalculatorManager;
import uk.ac.man.bioinf.apps.cinema.CinemaModule;
import uk.ac.man.bioinf.gui.color.ColorMap;
import uk.ac.man.bioinf.gui.color.PercentIDVarianceColorMap;
import uk.ac.man.bioinf.gui.viewer.JAlignmentViewer;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;


/**
 * PercentageIDVarianceColorMapMetaData.java
 *
 *
 * Created: Wed Jul 19 15:00:44 2000
 *
 * @author Phillip Lord
 * @version $Id: PercentageIDVarianceColorMapMetaData.java,v 1.6 2001/05/08 17:37:52 lord Exp $
 */

public class PercentageIDVarianceColorMapMetaData implements ColorMapMetaData
{

  public void setModule( CinemaModule module )
  {
    // we don't need this module
  }
  
  public String getColorMapName()
  {
    return "Variance (% ID)";
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
      ( PercentageIDCalculatorManager.getDefaultInstance().getCalculator( alignment ) );
  }
} // PercentageIDVarianceColorMapMetaData



/*
 * ChangeLog
 * $Log: PercentageIDVarianceColorMapMetaData.java,v $
 * Revision 1.6  2001/05/08 17:37:52  lord
 * New external name.
 *
 * Revision 1.5  2001/04/11 17:04:41  lord
 * Added License agreements to all code
 *
 * Revision 1.4  2000/12/05 15:54:37  lord
 * Import rationalisation
 *
 * Revision 1.3  2000/12/05 15:10:03  lord
 * Updated due to change in MapMetaData interface.
 *
 * Revision 1.2  2000/11/08 18:31:14  lord
 * Changes to reflect alternation in ConsensusSequence interface
 *
 * Revision 1.1  2000/07/26 13:29:59  lord
 * Initial checkin
 *
 */