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
import java.io.File;

import uk.ac.man.bioinf.apps.cinema.core.CinemaCoreIdentifier;
import uk.ac.man.bioinf.apps.cinema.core.CinemaSystemEvents;
import uk.ac.man.bioinf.apps.cinema.resources.CinemaResources;
import uk.ac.man.bioinf.apps.optionable.FileSaveableOptions;
import uk.ac.man.bioinf.apps.optionable.OptionHandler;
import uk.ac.man.bioinf.module.ModuleIdentifierList;


/**
 * CinemaFilePersist.java
 *
 *
 * Created: Tue Jan 30 15:53:21 2001
 *
 * @author Phillip Lord
 * @version $Id: CinemaFilePersist.java,v 1.4 2001/05/14 17:08:18 lord Exp $
 */

public class CinemaFilePersist extends CinemaPersist
{
  private OptionHandler optionHandler;
  
  public void start()
  {
    File userPersist = CinemaResources.getUserPersist();
    
    CinemaSystemEvents events = ((CinemaSystemEvents)getRequiredModule
				 ( CinemaCoreIdentifier.CINEMA_SYSTEM_EVENTS ));
    
    // (PENDING:- PL) This could do with improving. 
    CinemaFileOptionableExceptionHandler exp = new CinemaFileOptionableExceptionHandler();
    
    optionHandler = new FileSaveableOptions
      ( userPersist, events, exp );
    
    super.start();
  }

  public OptionHandler getOptionHandler()
  {
    return optionHandler;
  }

  public ModuleIdentifierList getRequiredIdentifiers()
  {
    ModuleIdentifierList list = super.getRequiredIdentifiers();
    list.add( CinemaCoreIdentifier.CINEMA_SYSTEM_EVENTS );
    return list;
  }

  public String getVersion()
  {
    return "$Id: CinemaFilePersist.java,v 1.4 2001/05/14 17:08:18 lord Exp $";
  }
} // CinemaFilePersist



/*
 * ChangeLog
 * $Log: CinemaFilePersist.java,v $
 * Revision 1.4  2001/05/14 17:08:18  lord
 * Added proper error handling to save and restore.
 *
 * Revision 1.3  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.2  2001/02/19 17:20:05  lord
 * Added super call
 *
 * Revision 1.1  2001/01/31 17:51:40  lord
 * Initial checkin
 *
 */
