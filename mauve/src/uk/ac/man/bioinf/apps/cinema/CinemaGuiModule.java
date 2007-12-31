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

package uk.ac.man.bioinf.apps.cinema; // Package name inserted by JPack
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import uk.ac.man.bioinf.apps.cinema.core.CinemaCoreGui;
import uk.ac.man.bioinf.apps.cinema.core.CinemaModuleCoreIdentifier;
import uk.ac.man.bioinf.apps.cinema.utils.CinemaAlignmentFrame;
import uk.ac.man.bioinf.gui.viewer.JAlignmentButtonPanel;
import uk.ac.man.bioinf.gui.viewer.JAlignmentRuler;
import uk.ac.man.bioinf.gui.viewer.JAlignmentViewer;
import uk.ac.man.bioinf.module.ModuleIdentifierList;


/**
 * CinemaGuiModule.java
 *
 *
 * Created: Fri May 26 21:04:15 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaGuiModule.java,v 1.8 2001/04/11 17:04:41 lord Exp $
 */

public abstract class CinemaGuiModule extends CinemaModule
{
  public void destroy()
  {
    super.destroy();
    gui = null;
  }
  
  // getters for the main cinema gui components. I may want to put
  // setters for these here eventually, Im not sure.
  public JFrame getFrame()
  {
    return getCore().getFrame();
  }
  
  private String mainTitleString;
  
  public void setFrameTitle( String title )
  {
    if( mainTitleString == null ){
      mainTitleString = getFrame().getTitle();
    }
    
    getFrame().setTitle( mainTitleString + ":- " + title );
  }
  
  public JAlignmentButtonPanel getRowHeaders()
  {
    return getCore().getRowHeaders();
  }
  
  public JMenuBar getJMenuBar()
  {
    return getCore().getJMenuBar();
  }
  
  public JScrollPane getScrollPane()
  {
    return getCore().getScrollPane();
  }
  
  public JPanel getRulerPanel()
  {
    return getCore().getRulerPanel();
  }
  
  public JAlignmentViewer getViewer()
  {
    return getCore().getViewer();
  }
  
  public JAlignmentRuler getRuler()
  {
    return getCore().getRuler();
  }
  
  public JTextField getStatusBar()
  {
    return getCore().getStatusBar();
  }

  public CinemaAlignmentFrame getAlignmentFrame()
  {
    return getCore().getAlignmentFrame();
  }
  
  private CinemaCoreGui gui;
  private CinemaCoreGui getCore()
  {
    if( gui == null ){
      gui = (CinemaCoreGui)getRequiredModule( CinemaModuleCoreIdentifier.CINEMA_CORE_GUI );
    }
    return gui;
  }
  
  public ModuleIdentifierList getRequiredIdentifiers()
  {
    ModuleIdentifierList list = super.getRequiredIdentifiers();
    list.add( CinemaModuleCoreIdentifier.CINEMA_CORE_GUI );
    return list;
  }
  
} // CinemaGuiModule



/*
 * ChangeLog
 * $Log: CinemaGuiModule.java,v $
 * Revision 1.8  2001/04/11 17:04:41  lord
 * Added License agreements to all code
 *
 * Revision 1.7  2000/10/19 17:35:44  lord
 * Added new method getAlignmentFrame
 *
 * Revision 1.6  2000/09/15 17:27:11  lord
 * Now destroyable
 *
 * Revision 1.5  2000/09/11 14:00:31  lord
 * Added support for setting frame title
 *
 * Revision 1.4  2000/06/27 13:37:38  lord
 * Changed method names to resolve naming conflict
 *
 * Revision 1.3  2000/06/05 14:10:53  lord
 * Removed version method again and made class abstract
 *
 * Revision 1.2  2000/06/05 14:09:49  lord
 * Added version method
 *
 * Revision 1.1  2000/05/30 16:05:54  lord
 * Initial checkin
 *
 */
