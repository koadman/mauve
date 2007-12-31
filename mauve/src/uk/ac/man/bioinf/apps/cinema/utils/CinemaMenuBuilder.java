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
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import uk.ac.man.bioinf.apps.cinema.CinemaModule;
import uk.ac.man.bioinf.apps.cinema.core.CinemaActionProvider;
import uk.ac.man.bioinf.apps.cinema.core.CinemaFramedActionProvider;
import uk.ac.man.bioinf.apps.xml.ConfigNode;
import uk.ac.man.bioinf.debug.Debug;
import uk.ac.man.bioinf.module.ModuleException;


/**
 * CinemaMenuBuilder.java
 *
 * This class builds a menu system based upon the module configuration
 * information. 
 * The format of the config info should look something like this...
 *
 * <code>
 *  <pre>
 *    &lt;!-- The File Menu --&gt;
 *
 *    &lt;node&gt;
 *	 &lt;value&gt;File&lt;/value&gt;
 *	 &lt;node&gt;
 *	     &lt;!-- Provides the open alignment --&gt;
 *	     &lt;name&gt;SEQ_INPUT&lt;/name&gt;
 *	   &lt;/node&gt;
 *	   &lt;node&gt;
 *	     &lt;!-- Provides the save alignment --&gt;
 *	     &lt;name&gt;SEQ_OUTPUT&lt;/name&gt;
 *	   &lt;/node&gt;
 *	 &lt;/node&gt;
 *	 
 *	 &lt;!-- The Colour Selector --&gt;
 *	 &lt;node&gt;
 *	   &lt;value&gt;Colour Selector&lt;/value&gt;
 *	   &lt;node&gt;
 *	     &lt;name&gt;CINEMA_COLOR_SELECTOR&lt;/name&gt;
 *	   &lt;/node&gt;
 *	 &lt;/node&gt;
 *     &lt;/node&gt;
 *  </pre>
 * </code>
 *
 * Essentially a "Value" is translated as the title of a menu whilst
 * "name" is translated as a Module which will provide actions for
 * that menu. This makes arbitrarily deep menu systems to be
 * constructed. 
 *
 * Created: Fri Oct 13 15:26:26 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaMenuBuilder.java,v 1.5 2001/04/11 17:04:42 lord Exp $
 */

public class CinemaMenuBuilder extends CinemaModule
{
  /**
   * This is the build method. The ConfigNode should be the root nodes
   * which describe the menu system. 
   * @param frame this is optional and can be null. If it is present
   * then it will be passed to those menu items which require it. 
   * @param menuBar this is the JMenuBar to use. 
   * @param mainMenuItems this are there configuration nodes which
   * describe the menu system. 
   */
  public void buildMenu( CinemaAlignmentFrame frame, JMenuBar menuBar, ConfigNode[] mainMenuItems )
  { 
    //add the main menu items
    for( int i = 0; i < mainMenuItems.length; i++ ){
      JMenu menu = new JMenu( mainMenuItems[ i ].getStringData() );
      menuBar.add( menu );
      addToMenu( frame, menu, mainMenuItems[ i ] );
    }
  }

  private void addToMenu( CinemaAlignmentFrame frame, JMenu menu, ConfigNode node )
  {
    ConfigNode[] childNode = node.getChildNodes();
    for( int i = 0; i < childNode.length; i++ ){
      if( !childNode[ i ].isModuleIdentifier() ){
	// this is not a module. So it a value which indicates a
	// submenu. So make the menu, and add all children via a
	// recursive call
	JMenu childMenu = new JMenu( childNode[ i ].getStringData() );
	menu.add( childMenu );
	addToMenu( frame, childMenu, childNode[ i ] );
      }
      else{
	// we have a module. This module will implement
	// CinemaActionProvider, and we can get actions from this. 
	// (PENDING:- PL) I do not like this. The ConfigNode should be
	// able to return a Module directly, rather than a String
	try{
	  
          Object prov = getContext().getModule( childNode[ i ].getModuleData() );
	  
	  // fetch the actions, passing in the AlignmentFrame if necessary
	  Action[] actions;
	  // I don't like this code at all. Its seems a bit of a hack.
	  if( prov instanceof CinemaFramedActionProvider ){
	    actions = ((CinemaFramedActionProvider)prov).getActions( frame );
	  }
	  else{
	    actions = ((CinemaActionProvider)prov).getActions();
	  }
	  
	  // add them to the menu bar
	  for( int j = 0; j < actions.length; j++ ){
	    menu.add( actions[ j ] );
	  }
	}
        catch( ModuleException me ){
	  me.printStackTrace();
          
          if( Debug.debug )
	    Debug.throwable( this, me );
	}
      }
    }
  }

  public String getVersion()
  {
    return "$Id: CinemaMenuBuilder.java,v 1.5 2001/04/11 17:04:42 lord Exp $";
  }
} // CinemaMenuBuilder



/*
 * ChangeLog
 * $Log: CinemaMenuBuilder.java,v $
 * Revision 1.5  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.4  2001/01/31 18:06:22  lord
 * Improved exception handling.
 *
 * Revision 1.3  2000/12/18 12:12:22  jns
 * o getting rid of system.out.println to avoid noisy output out of debug
 * mode
 *
 * Revision 1.2  2000/12/05 15:54:37  lord
 * Import rationalisation
 *
 * Revision 1.1  2000/10/19 17:45:42  lord
 * Initial checkin. Most of this code came previously from CinemaMenuSystem
 *
 */
