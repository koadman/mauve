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
import uk.ac.man.bioinf.module.AbstractEnumeratedModuleIdentifier;



/**
 * CinemaCoreIdentifier.java
 *
 *
 * Created: Sun May  7 18:16:43 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaCoreIdentifier.java,v 1.6 2001/04/11 17:04:41 lord Exp $
 */

public class CinemaCoreIdentifier extends AbstractEnumeratedModuleIdentifier
{
  private CinemaCoreIdentifier( String className, String toString )
  {
    super( className, toString );
  }
  
  public static final CinemaCoreIdentifier CINEMA_GO
    = new CinemaCoreIdentifier( "uk.ac.man.bioinf.apps.cinema.core.CinemaGo",
				"Following the Loading this module makes Cinema Visible" );
  public static final CinemaCoreIdentifier CINEMA_SYSTEM_EVENTS
    = new CinemaCoreIdentifier( "uk.ac.man.bioinf.apps.cinema.core.CinemaSystemEvents",
				"Provides system event support for Cinema" );
  public static final CinemaCoreIdentifier CINEMA_MENU_SYSTEM
    = new CinemaCoreIdentifier( "uk.ac.man.bioinf.apps.cinema.core.CinemaMenuSystem", 
				"Provides a configurable menu system for cinema" );
  public static final CinemaCoreIdentifier CINEMA_SEQUENCE_MENU
    = new CinemaCoreIdentifier( "uk.ac.man.bioinf.apps.cinema.core.CinemaSequenceMenu",
				"Provides a menu system for the sequence buttons" );
  //  public static final CinemaCoreIdentifier CINEMA_CONSENSUS
  // = new CinemaCoreIdentifier( "uk.ac.man.bioinf.apps.cinema.core.CinemaConsensus",
  //"Provides the consensus tracker for cinema" );
} // CinemaCoreIdentifiers



/*
 * ChangeLog
 * $Log: CinemaCoreIdentifier.java,v $
 * Revision 1.6  2001/04/11 17:04:41  lord
 * Added License agreements to all code
 *
 * Revision 1.5  2000/07/26 13:27:58  lord
 * Changed due to a spelling mistake in the super class name
 *
 * Revision 1.4  2000/06/27 13:39:36  lord
 * Added Cinema Consensus identifier
 *
 * Revision 1.3  2000/05/30 16:10:10  lord
 * Removed core view and core gui to CinemaModuleCoreIdentifier
 * Added some more modules
 *
 * Revision 1.2  2000/05/24 15:35:54  lord
 * Added new method in ModuleIdentifier interface
 *
 * Revision 1.1  2000/05/15 16:21:13  lord
 * Initial checkin
 *
 */
