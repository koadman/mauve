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

package uk.ac.man.bioinf.apps.cinema.core; // Package name inserted by JPack
import uk.ac.man.bioinf.apps.systemevents.SystemEventOption;
import uk.ac.man.bioinf.apps.systemevents.SystemEventProducer;
import uk.ac.man.bioinf.apps.systemevents.SystemEventSupport;
import uk.ac.man.bioinf.apps.systemevents.SystemListener;
import uk.ac.man.bioinf.apps.systemevents.SystemVetoException;
import uk.ac.man.bioinf.debug.Debug;
import uk.ac.man.bioinf.module.Module;


/**
 * CinemaSystemEvents.java
 *
 *
 * Created: Thu Apr 20 18:26:56 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaSystemEvents.java,v 1.4 2001/04/11 17:04:42 lord Exp $
 */

public class CinemaSystemEvents extends Module implements SystemEventProducer
{
  SystemEventSupport supp = new SystemEventSupport();
  
  public void load()
  {
  }
  
  public void start()
  {
  }
  
  public void addSystemEventListener( SystemListener listener )
  {
    supp.addSystemEventListener( listener );
  }
  
  public void removeSystemEventListener( SystemListener listener )
  {
    supp.removeSystemEventListener( listener );
  }

  public void fireSystemEvent( SystemEventOption option ) throws SystemVetoException
  {
    if( Debug.debug )
      Debug.message( this, "Firing system event " + option );
    try{
      supp.fireSystemEvent( option );
      if( Debug.debug )
	Debug.message( this, "System event run " );
    }
    catch( SystemVetoException sve ){
      if( Debug.debug )
	Debug.both( this, "System event exception ", sve );
      throw sve;
    }
  }
  
  public String getVersion()
  {
    return "$Id: CinemaSystemEvents.java,v 1.4 2001/04/11 17:04:42 lord Exp $";
  }
} // CinemaSystemEvents



/*
 * ChangeLog
 * $Log: CinemaSystemEvents.java,v $
 * Revision 1.4  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.3  2000/12/18 12:10:00  jns
 * o getting rid of system.out.println to avoid noisy output out of debug
 * mode
 *
 * Revision 1.2  2000/05/30 16:13:29  lord
 * Changes due to completion of module package
 * Import sorting
 *
 * Revision 1.1  2000/05/08 16:22:10  lord
 * Initial checkin
 *
 */
