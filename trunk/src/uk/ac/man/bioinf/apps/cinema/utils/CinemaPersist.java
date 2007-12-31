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
import uk.ac.man.bioinf.apps.cinema.CinemaModule;
import uk.ac.man.bioinf.apps.optionable.OptionHandler;
import uk.ac.man.bioinf.gui.optionable.OptionableJFrame;
import uk.ac.man.bioinf.gui.optionable.OptionableJSplitPane;


/**
 * CinemaPersist.java
 *
 * Provides the OptionHandler which classes need to implement persistance. 
 * Created: Tue Jan 30 15:31:24 2001
 *
 * @author Phillip Lord
 * @version $Id: CinemaPersist.java,v 1.3 2001/04/11 17:04:42 lord Exp $
 */

public abstract class CinemaPersist extends CinemaModule
{
  public void start()
  {
    super.start();
    
    // (PENDING:- PL) Need to put in support here for all the
    // libraries that automatically require this. 
    OptionableJFrame.setDefaultOptionHandler( getOptionHandler() );
    OptionableJSplitPane.setDefaultOptionHandler( getOptionHandler() );
  }
  
  public abstract OptionHandler getOptionHandler();
} // CinemaPersist



/*
 * ChangeLog
 * $Log: CinemaPersist.java,v $
 * Revision 1.3  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.2  2001/03/12 16:46:51  lord
 * Added support for more libraries.
 *
 * Revision 1.1  2001/01/31 17:51:40  lord
 * Initial checkin
 *
 */
