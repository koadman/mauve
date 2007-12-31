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

import java.awt.Color;

import uk.ac.man.bioinf.analysis.consensus.ConsensusSequence;
import uk.ac.man.bioinf.apps.cinema.CinemaModule;
import uk.ac.man.bioinf.gui.color.ColorMap;
import uk.ac.man.bioinf.gui.color.SingleColorMap;
import uk.ac.man.bioinf.gui.viewer.JAlignmentViewer;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;


/**
 * WhiteColorMapMetaData.java
 *
 *
 * Created: Fri Feb  9 13:12:26 2001
 *
 * @author Phillip Lord
 * @version $Id: WhiteColorMapMetaData.java,v 1.2 2001/04/11 17:04:41 lord Exp $
 */

public class WhiteColorMapMetaData implements ColorMapMetaData
{
  private SingleColorMap map = new SingleColorMap( Color.white, "white color map" );
  
  public void setModule( CinemaModule module )
  {
    // nothing needed. 
  }
  
  public String getColorMapName()
  {
    return map.getName();
  }
  
  public ColorMap getInstance( SequenceAlignment alignment, JAlignmentViewer viewer )
  {
    return map;
  }
  
  public ColorMap getConsensusInstance( ConsensusSequence cons, JAlignmentViewer viewer )
  {
    return map;
  }
} // WhiteColorMapMetaData



/*
 * ChangeLog
 * $Log: WhiteColorMapMetaData.java,v $
 * Revision 1.2  2001/04/11 17:04:41  lord
 * Added License agreements to all code
 *
 * Revision 1.1  2001/02/19 16:55:22  lord
 * Initial checkin
 *
 */
