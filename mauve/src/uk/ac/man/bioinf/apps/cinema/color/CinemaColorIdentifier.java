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
import uk.ac.man.bioinf.module.AbstractEnumeratedModuleIdentifier;


/**
 * CinemaColorIdentifier.java
 *
 *
 * Created: Sun May 28 22:34:00 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaColorIdentifier.java,v 1.4 2001/04/11 17:04:41 lord Exp $
 */

public class CinemaColorIdentifier extends AbstractEnumeratedModuleIdentifier
{

  private CinemaColorIdentifier( String className, String toString )
  {
    super( className, toString );
  }
  
  public static final CinemaColorIdentifier CINEMA_COLOR_FACTORY
    = new CinemaColorIdentifier( "uk.ac.man.bioinf.apps.cinema.color.CinemaColorFactory",
                                 "Provides the factory for color maps within cinema" );
  public static final CinemaColorIdentifier CINEMA_COLOR_SELECTOR 
    = new CinemaColorIdentifier( "uk.ac.man.bioinf.apps.cinema.color.CinemaColorSelector",
				 "Provides the central selection system for colour map selection" );
} // CinemaColorIdentifier



/*
 * ChangeLog
 * $Log: CinemaColorIdentifier.java,v $
 * Revision 1.4  2001/04/11 17:04:41  lord
 * Added License agreements to all code
 *
 * Revision 1.3  2001/02/19 16:55:08  lord
 * Have separated CinemaColorSelector into two. One factory for
 * ColorMaps, one display module.
 *
 * Revision 1.2  2000/07/26 13:30:35  lord
 * Changed due to a spelling mistake in the super class name
 *
 * Revision 1.1  2000/05/30 16:06:15  lord
 * Initial checkin
 *
 */
