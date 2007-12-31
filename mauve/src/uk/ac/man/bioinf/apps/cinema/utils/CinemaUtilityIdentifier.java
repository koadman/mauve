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
import uk.ac.man.bioinf.module.AbstractEnumeratedModuleIdentifier;



/**
 * CinemaUtilityIdentifier.java
 *
 *
 * Created: Thu May 25 14:27:34 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaUtilityIdentifier.java,v 1.14 2001/04/11 17:04:42 lord Exp $
 */

public class CinemaUtilityIdentifier extends AbstractEnumeratedModuleIdentifier
{

  private CinemaUtilityIdentifier( String className, String toString )
  {
    super( className, toString );
  }
  
  private CinemaUtilityIdentifier( String className, String toString, boolean isInterface )
  {
    super( className, toString, isInterface );
  }
  
  public static final CinemaUtilityIdentifier CINEMA_REGEXP =
    new CinemaUtilityIdentifier( "uk.ac.man.bioinf.apps.cinema.utils.CinemaRegexp", 
                                 "Provides regexp search facilities for Cinema" );
  public static final CinemaUtilityIdentifier CINEMA_STATUS =
    new CinemaUtilityIdentifier( "uk.ac.man.bioinf.apps.cinema.utils.CinemaStatusInformation",
				 "Provides the status bar information for cinema" );
  public static final CinemaUtilityIdentifier CINEMA_MULTIPLE_CONSENSUS =
    new CinemaUtilityIdentifier( "uk.ac.man.bioinf.apps.cinema.utils.CinemaMultipleConsensusViewer",
				 "Provides a viewer for many consensus sequences" );
  public static final CinemaUtilityIdentifier CINEMA_INVOKER =
    new CinemaUtilityIdentifier( "uk.ac.man.bioinf.apps.cinema.utils.CinemaInvoker", 
				 "Provides Invoker queues for Cinema" );
  public static final CinemaUtilityIdentifier CINEMA_SLAVE_VIEWER =
    new CinemaUtilityIdentifier( "uk.ac.man.bioinf.apps.cinema.utils.CinemaSlaveViewerModule", 
				 "Provides Slave viewers" );
  public static final CinemaUtilityIdentifier CINEMA_RESIZE_ELEMENTS =
    new CinemaUtilityIdentifier( "uk.ac.man.bioinf.apps.cinema.utils.CinemaResizeElements", 
				 "Resizes the element size in convienient gui" );
  public static final CinemaUtilityIdentifier CINEMA_MENU_BUILDER = 
    new CinemaUtilityIdentifier( "uk.ac.man.bioinf.apps.cinema.utils.CinemaMenuBuilder", 
				 "Builds a menu system from a config tree" );
  public static final CinemaUtilityIdentifier CINEMA_PERSIST =
    new CinemaUtilityIdentifier( "uk.ac.man.bioinf.apps.cinema.utils.CinemaPersist",
                                 "Provides persistance for cinema", 
                                 true );
  public static final CinemaUtilityIdentifier CINEMA_FILE_PERSIST =
    new CinemaUtilityIdentifier( "uk.ac.man.bioinf.apps.cinema.utils.CinemaFilePersist",
                                 "Provides persistance to file for cinema" );
} // CinemaUtilityIdentifier



/*
 * ChangeLog
 * $Log: CinemaUtilityIdentifier.java,v $
 * Revision 1.14  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.13  2001/03/12 16:47:25  lord
 * Removed hack identifiers
 *
 * Revision 1.12  2001/02/19 17:21:36  lord
 * Added regexp module
 *
 * Revision 1.11  2001/01/31 17:52:33  lord
 * Changes due to removal of InterfaceIdentifier
 * Added persistance identifiers
 *
 * Revision 1.10  2001/01/27 16:56:18  lord
 * Added new module
 *
 * Revision 1.9  2001/01/26 17:11:13  lord
 * Added Status info
 *
 * Revision 1.8  2000/11/08 18:23:55  lord
 * Fairly uninteresting changes
 *
 * Revision 1.7  2000/10/19 17:51:02  lord
 * Added more identifiers
 *
 * Revision 1.6  2000/09/15 17:33:12  lord
 * Removed debug console, now in shared.
 *
 * Revision 1.5  2000/08/01 17:19:57  lord
 * Added Resize module
 *
 * Revision 1.4  2000/07/26 13:27:58  lord
 * Changed due to a spelling mistake in the super class name
 *
 * Revision 1.3  2000/06/27 15:57:39  lord
 * Added more identifiers
 *
 * Revision 1.2  2000/06/05 14:17:53  lord
 * Added support for Slave viewer module
 *
 * Revision 1.1  2000/05/30 16:17:11  lord
 * Initial checkin
 *
 */
