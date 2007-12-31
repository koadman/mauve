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
import uk.ac.man.bioinf.apps.cinema.CinemaModule;
import uk.ac.man.bioinf.gui.color.ColorMap;
import uk.ac.man.bioinf.gui.viewer.JAlignmentViewer;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;


/**
 * ColorMapMetaData.java
 *
 * Many of the colour maps available need to be treated in different
 * ways. So for instance some colour maps can use a single shared
 * instance, others needs a new one for each alignment. It does not
 * really make sense to try and encapsulate this sort of knowledge
 * within the ColorMap class, but it is needed within Cinema...
 *
 * Created: Tue Jul 18 14:57:08 2000
 *
 * @author Phillip Lord
 * @version $Id: ColorMapMetaData.java,v 1.3 2001/04/11 17:04:41 lord Exp $
 */

public interface ColorMapMetaData 
{
 
  public void setModule( CinemaModule module );
  /**
   * This should return the string that ColorMap instances created via
   * this class, return as their name, via the getName method
   * @return the name
   */
  public String getColorMapName();
  
  /**
   * Returns a instance of this Color map for the given alignment
   * instance.   
   * @return the ColorMap
   */
  public ColorMap getInstance( SequenceAlignment alignment, JAlignmentViewer viewer );
  
  /**
   * Returns an instance of this type for this consensus sequence
   * @param sequence the consensus sequence 
   * @return the ColorMap instance
   */
  public ColorMap getConsensusInstance( ConsensusSequence sequence, JAlignmentViewer viewer );
    
} // ColorMapMetaData



/*
 * ChangeLog
 * $Log: ColorMapMetaData.java,v $
 * Revision 1.3  2001/04/11 17:04:41  lord
 * Added License agreements to all code
 *
 * Revision 1.2  2000/12/05 15:10:03  lord
 * Updated due to change in MapMetaData interface.
 *
 * Revision 1.1  2000/07/26 13:29:59  lord
 * Initial checkin
 *
 */
