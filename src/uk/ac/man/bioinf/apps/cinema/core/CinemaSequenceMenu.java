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
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import uk.ac.man.bioinf.apps.cinema.CinemaGuiModule;
import uk.ac.man.bioinf.gui.viewer.ButtonViewerPopupMenu;
import uk.ac.man.bioinf.sequence.alignment.GappedSequence;


/**
 * CinemaSequenceMenu.java
 *
 *
 * Created: Mon May 22 14:04:57 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaSequenceMenu.java,v 1.8 2001/04/11 17:04:42 lord Exp $
 */

public class CinemaSequenceMenu extends CinemaGuiModule 
{
  private JLabel titleLabel;
  private GappedSequence currentSequence;
  private ButtonViewerPopupMenu popupMenu;
  
  public void start()
  {
    popupMenu = new ButtonViewerPopupMenu( getRowHeaders() );
    popupMenu.add( titleLabel = new JLabel() );
  }
  
  public JPopupMenu getPopupMenu()
  {
    return popupMenu;
  }
  
  public void add( JMenuItem menu )
  {
    popupMenu.add( menu );
  }
  
  public GappedSequence getLastSequence()
  {
    return popupMenu.getSelectedSequence();
  }
  
  public String getVersion()
  {
    return "$Id: CinemaSequenceMenu.java,v 1.8 2001/04/11 17:04:42 lord Exp $";
  }
} // CinemaSequenceMenu


/*
 * ChangeLog
 * $Log: CinemaSequenceMenu.java,v $
 * Revision 1.8  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.7  2000/12/05 15:54:37  lord
 * Import rationalisation
 *
 * Revision 1.6  2000/10/19 17:42:07  lord
 * Moved most of the code out to ButtonViewerPopupMenu
 *
 * Revision 1.5  2000/07/18 10:39:38  lord
 * Import rationalisation.
 * Changes due to biointerface removal
 *
 * Revision 1.4  2000/06/27 13:41:23  lord
 * Changed popup positioning logic, so that it now works!
 *
 * Revision 1.3  2000/06/05 14:13:13  lord
 * Accessor method for popup menu
 *
 * Revision 1.2  2000/05/30 16:13:13  lord
 * Changes due to completion of module package
 *
 * Revision 1.1  2000/05/24 15:36:50  lord
 * Initial checkin
 *
 */
