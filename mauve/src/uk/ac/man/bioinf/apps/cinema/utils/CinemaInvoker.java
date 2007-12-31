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

package uk.ac.man.bioinf.apps.cinema.utils; // Package name inserted by JPack
import uk.ac.man.bioinf.apps.cinema.core.CinemaCoreIdentifier;
import uk.ac.man.bioinf.apps.cinema.core.CinemaSystemEvents;
import uk.ac.man.bioinf.apps.invoker.InvokerInternalQueue;
import uk.ac.man.bioinf.module.Module;
import uk.ac.man.bioinf.module.ModuleIdentifierList;


/**
 * CinemaInvoker.java
 *
 *
 * Created: Tue Apr 25 20:24:30 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaInvoker.java,v 1.4 2001/04/11 17:04:42 lord Exp $
 */

public class CinemaInvoker extends Module
{
  private InvokerInternalQueue qu;
  
  public void start()
  {
    qu = getNewInvokerInternalQueue();
  }
  
  public InvokerInternalQueue getInvokerInternalQueue()
  {
    return qu;
  }
  
  public InvokerInternalQueue getNewInvokerInternalQueue()
  {
    return new InvokerInternalQueue( getCinemaSystemEvents() );
  }

  private CinemaSystemEvents getCinemaSystemEvents()
  {
    return (CinemaSystemEvents)getRequiredModule( CinemaCoreIdentifier.CINEMA_SYSTEM_EVENTS );
  }

  public String getVersion()
  {
    return "$Id: CinemaInvoker.java,v 1.4 2001/04/11 17:04:42 lord Exp $";
  }
  
  public ModuleIdentifierList getRequiredIdentifiers()
  {
    ModuleIdentifierList list = super.getRequiredIdentifiers();
    list.add( CinemaCoreIdentifier.CINEMA_SYSTEM_EVENTS );
    return list;
  }
  
} // CinemaInvoker



/*
 * ChangeLog
 * $Log: CinemaInvoker.java,v $
 * Revision 1.4  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.3  2000/05/30 16:18:44  lord
 * Removed identifier to CinemaUtilityIdentifier. Import sorting.
 * Module package completion
 *
 * Revision 1.2  2000/05/24 15:37:39  lord
 * Updated for change in ModuleInterface interface
 *
 * Revision 1.1  2000/05/08 16:22:10  lord
 * Initial checkin
 *
 */




