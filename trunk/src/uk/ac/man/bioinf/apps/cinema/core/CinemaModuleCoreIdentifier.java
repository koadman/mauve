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
 * CinemaModuleCoreIdentifer.java
 *
 *
 * Created: Fri May 26 21:39:17 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaModuleCoreIdentifier.java,v 1.3 2001/04/11 17:04:42 lord Exp $
 */

public class CinemaModuleCoreIdentifier extends AbstractEnumeratedModuleIdentifier
{

  private CinemaModuleCoreIdentifier( String className, String toString )
  {
    super( className, toString );
  }
  
  public static final CinemaModuleCoreIdentifier CINEMA_CORE_GUI
    = new CinemaModuleCoreIdentifier( "uk.ac.man.bioinf.apps.cinema.core.CinemaCoreGui", 
				      "Main GUI frame for cinema" );
  public static final CinemaModuleCoreIdentifier CINEMA_CORE_VIEW
    = new CinemaModuleCoreIdentifier( "uk.ac.man.bioinf.apps.cinema.core.CinemaCoreView",
				      "Main view for cinema to provide non Swing specific interface for other modules" );
} // CinemaModuleCoreIdentifer



/*
 * ChangeLog
 * $Log: CinemaModuleCoreIdentifier.java,v $
 * Revision 1.3  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.2  2000/07/26 13:27:58  lord
 * Changed due to a spelling mistake in the super class name
 *
 * Revision 1.1  2000/05/30 16:12:47  lord
 * Initial checkin
 *
 */
