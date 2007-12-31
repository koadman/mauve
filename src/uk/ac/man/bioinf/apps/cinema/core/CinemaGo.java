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
import uk.ac.man.bioinf.apps.cinema.CinemaGuiModule;
import uk.ac.man.bioinf.apps.optionable.OptionableStateException;


/**
 * CinemaGo.java
 *
 * This class actually makes the Cinema gui visible. Effectively its a
 * dummy module, which I wrote so that I make the gui pop up when it
 * is fully formed rather than building it whilst its on
 * screen. Eventually I would like it to do some more things,
 * particularly provide a progress bar (although I do not know how I
 * am going to find out what is going on. An event scheme for the
 * ModuleFactory? How will I know how many modules are going to be loaded?)
 *
 * Created: Sat May 27 00:37:17 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaGo.java,v 1.7 2001/04/11 17:04:42 lord Exp $
 */

public class CinemaGo extends CinemaGuiModule
{
  public void start()
  {
    // this sets the size appropriately
    try{
      getAlignmentFrame().setOptions();
    }
    catch( OptionableStateException ose ){
      // we should be alright from this, unless its a programming
      // error!
      ose.printStackTrace();
    }
    catch( Exception exp ){
      exp.printStackTrace();
    }
    
    getFrame().setVisible( true );
  }

  public String getVersion()
  {
    return "$Id: CinemaGo.java,v 1.7 2001/04/11 17:04:42 lord Exp $";
  }
} // CinemaGo



/*
 * ChangeLog
 * $Log: CinemaGo.java,v $
 * Revision 1.7  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.6  2001/02/19 17:00:32  lord
 * Added optionability
 *
 * Revision 1.5  2001/01/31 17:41:42  lord
 * Increased robustness
 *
 * Revision 1.4  2001/01/27 17:04:02  lord
 * Removed automatic loading of alignment during load up
 *
 * Revision 1.3  2000/12/13 16:28:13  lord
 * Splash screen support
 *
 * Revision 1.2  2000/06/05 14:12:56  lord
 * Added version method
 *
 * Revision 1.1  2000/05/30 16:11:31  lord
 * Changes due to introduction of CinemaModule.
 * Added Status bar support
 *
 */
