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
import uk.ac.man.bioinf.apps.cinema.core.CinemaCoreIdentifier;
import uk.ac.man.bioinf.apps.cinema.core.CinemaSystemEvents;
import uk.ac.man.bioinf.apps.invoker.InvokerInternalQueue;
import uk.ac.man.bioinf.gui.color.ColorMap;
import uk.ac.man.bioinf.gui.color.PercentIDVarianceColorMap;
import uk.ac.man.bioinf.gui.color.ThreadedColorMap;
import uk.ac.man.bioinf.gui.viewer.JAlignmentViewer;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;


/**
 * ThreadedPercentageIDVarianceColorMapMetaData.java
 *
 *
 * Created: Fri Dec  1 16:54:24 2000
 *
 * @author Phillip Lord
 * @version $Id: ThreadedPercentageIDVarianceColorMapMetaData.java,v 1.3 2001/05/08 17:37:59 lord Exp $
 */

public class ThreadedPercentageIDVarianceColorMapMetaData implements ColorMapMetaData
{
  private static final String NAME = "Variance (% ID Background)";
  
  private CinemaModule module;
  
  public void setModule( CinemaModule module )
  {
    this.module = module;
  }
  
  public String getColorMapName()
  {
    return NAME;
  }
  
  public ColorMap getInstance( SequenceAlignment alignment, JAlignmentViewer viewer )
  {
    return getCreateInstance( alignment, viewer );
  }
  
  public ColorMap getConsensusInstance( ConsensusSequence sequence, JAlignmentViewer viewer )
  {
    return getCreateInstance( sequence.getConsensusCalculator().getSequenceAlignment(), viewer );
  }

  private ThreadedColorMap getCreateInstance( SequenceAlignment alignment, JAlignmentViewer viewer )
  {
    InvokerInternalQueue queue = new InvokerInternalQueue
      ( (CinemaSystemEvents)module.getRequiredModule( CinemaCoreIdentifier.CINEMA_SYSTEM_EVENTS ) );
    
    // we want to fire the fast events as quickly as possible. This
    // should allow coalescing of as many repaint requests as
    // possible. 
    //queue.setOneAtATime( false );
    
    // this code should win awards for the most wacky indentation. 
    return new ThreadedColorMap
      ( viewer,
	alignment, 
	new PercentIDVarianceColorMap
	  ( PercentageIDCalculatorManager.getDefaultInstance().getCalculator( alignment ) ),
	queue )
      {
	public String getName()
	{
	  return ThreadedPercentageIDVarianceColorMapMetaData.NAME;
	}
      };
  }
} // ThreadedPercentageIDVarianceColorMap



/*
 * ChangeLog
 * $Log: ThreadedPercentageIDVarianceColorMapMetaData.java,v $
 * Revision 1.3  2001/05/08 17:37:59  lord
 * New external name.
 *
 * Revision 1.2  2001/04/11 17:04:41  lord
 * Added License agreements to all code
 *
 * Revision 1.1  2000/12/05 15:09:42  lord
 * Initial checkin
 *
 */
