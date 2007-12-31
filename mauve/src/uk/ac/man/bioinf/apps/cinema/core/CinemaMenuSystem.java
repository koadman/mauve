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
import javax.swing.JMenuBar;

import uk.ac.man.bioinf.apps.cinema.CinemaGuiModule;
import uk.ac.man.bioinf.apps.cinema.utils.CinemaMenuBuilder;
import uk.ac.man.bioinf.apps.cinema.utils.CinemaUtilityIdentifier;
import uk.ac.man.bioinf.apps.xml.ConfigNode;
import uk.ac.man.bioinf.module.ModuleIdentifierList;


/**
 * CinemaMenuSystem.java
 *
 *
 * Created: Tue May 16 14:24:27 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaMenuSystem.java,v 1.7 2001/05/08 17:39:44 lord Exp $
 */

public class CinemaMenuSystem extends CinemaGuiModule
{
  public void start()
  {
    JMenuBar menuBar = getJMenuBar();
    
    //add the main menu items
    ConfigNode[] mainMenuItems = getConfigTree().getChildNodes();
    CinemaMenuBuilder builder = (CinemaMenuBuilder)getRequiredModule( CinemaUtilityIdentifier.CINEMA_MENU_BUILDER );
    builder.buildMenu( getAlignmentFrame(), menuBar, mainMenuItems );
  }
  
  public String getVersion()
  {
    return "$Id: CinemaMenuSystem.java,v 1.7 2001/05/08 17:39:44 lord Exp $";
  }
  
  public ModuleIdentifierList getRequiredIdentifiers()
  {
    ModuleIdentifierList list = super.getRequiredIdentifiers();
    list.add( CinemaUtilityIdentifier.CINEMA_MENU_BUILDER );
    return list;
  }
  
} // CinemaMenuSystem



/*
 * ChangeLog
 * $Log: CinemaMenuSystem.java,v $
 * Revision 1.7  2001/05/08 17:39:44  lord
 * Cosmetic changes.
 *
 * Revision 1.6  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.5  2000/12/05 15:54:37  lord
 * Import rationalisation
 *
 * Revision 1.4  2000/10/19 17:41:08  lord
 * Most of the code from here has been moved out to the CinemaMenuBuilder
 *
 * Revision 1.3  2000/06/27 13:40:26  lord
 * Changed to reflect method name change in CoreGui
 *
 * Revision 1.2  2000/05/30 16:12:32  lord
 * Changes due to completion of module package.
 * Changes due to alterations in ConfigNode class producing
 * better type safety in this class.
 * Import sorting.
 *
 * Revision 1.1  2000/05/18 17:12:19  lord
 * Initial checkin
 *
 */
