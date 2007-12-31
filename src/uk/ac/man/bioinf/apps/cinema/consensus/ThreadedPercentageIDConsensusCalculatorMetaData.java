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

package uk.ac.man.bioinf.apps.cinema.consensus; // Package name inserted by JPack
import uk.ac.man.bioinf.analysis.consensus.ConsensusSequence;
import uk.ac.man.bioinf.analysis.consensus.ThreadedConsensusSequence;
import uk.ac.man.bioinf.apps.cinema.CinemaModule;
import uk.ac.man.bioinf.apps.cinema.core.CinemaCoreIdentifier;
import uk.ac.man.bioinf.apps.cinema.core.CinemaSystemEvents;
import uk.ac.man.bioinf.apps.invoker.InvokerInternalQueue;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;


/**
 * ThreadedPercentageIDConsensusCalculatorMetaData.java
 *
 *
 * Created: Wed Nov  8 15:01:52 2000
 *
 * @author Phillip Lord
 * @version $Id: ThreadedPercentageIDConsensusCalculatorMetaData.java,v 1.6 2001/05/08 17:39:16 lord Exp $
 */

public class ThreadedPercentageIDConsensusCalculatorMetaData 
  extends PercentageIDConsensusCalculatorMetaData
{
  private static final String NAME = "Consensus (% ID Background)";
  
  private CinemaModule module;
  
  public void setModule( CinemaModule module )
  {
    this.module = module;
  }
  
  public ConsensusSequence getInstance( SequenceAlignment alignment )
  {
    InvokerInternalQueue queue = new InvokerInternalQueue
      ( (CinemaSystemEvents)module.getRequiredModule( CinemaCoreIdentifier.CINEMA_SYSTEM_EVENTS ) );
    
    return new ThreadedConsensusSequence( getCalculatorInstance( alignment ), queue );
  }
  
  public String getConsensusCalculatorName()
  {
    return NAME;
  }
} // ThreadedPercentageIDConsensusCalculatorMetaData



/*
 * ChangeLog
 * $Log: ThreadedPercentageIDConsensusCalculatorMetaData.java,v $
 * Revision 1.6  2001/05/08 17:39:16  lord
 * New external names.
 *
 * Revision 1.5  2001/04/11 17:04:41  lord
 * Added License agreements to all code
 *
 * Revision 1.4  2000/12/05 15:54:37  lord
 * Import rationalisation
 *
 * Revision 1.3  2000/12/05 15:18:06  lord
 * Now has one InvokerInternalQueue per instance
 *
 * Revision 1.2  2000/11/09 16:20:41  lord
 * Removed debug output
 *
 * Revision 1.1  2000/11/08 18:33:02  lord
 * Initial checkin
 *
 */
