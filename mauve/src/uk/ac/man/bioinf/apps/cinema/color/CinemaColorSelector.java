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
import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;

import uk.ac.man.bioinf.apps.cinema.CinemaGuiModule;
import uk.ac.man.bioinf.apps.cinema.core.CinemaFramedActionProvider;
import uk.ac.man.bioinf.apps.cinema.utils.CinemaAlignmentFrame;
import uk.ac.man.bioinf.gui.viewer.JAlignmentViewer;
import uk.ac.man.bioinf.module.ModuleIdentifierList;


/**
 * CinemaColorSelector.java
 *
 * Created: Sun May 28 22:14:13 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaColorSelector.java,v 1.8 2001/04/11 17:04:41 lord Exp $
 */

public class CinemaColorSelector extends CinemaGuiModule implements CinemaFramedActionProvider
{
  public void start()
  {
    setColorMap( getColorFactory().
                 getMetaData( AminoAcidColorMapMetaData.NAME ).
                 getInstance( getSequenceAlignment(), getViewer() ) );
  }

  public Action[] getActions( CinemaAlignmentFrame frame )
  {
    
    Action[] retn = new Action[ getColorFactory().getSize() ];
    
    // the action for the null color map. 
    retn[ 0 ] = new ClosureAbstractAction( frame, "Colour Map off", "null" );
    
    Iterator iter = getColorFactory().getColorMapIterator();
    int i = 1;
    
    while( iter.hasNext() ){
      ColorMapMetaData md = (ColorMapMetaData)iter.next();
      if( md != getColorFactory().getDefaultColorMapMetaData() ){
	retn[ i++ ] = new ClosureAbstractAction( frame, md.getColorMapName(), md.getColorMapName() );
      }
    }
        
    return retn;
  }

  class ClosureAbstractAction extends AbstractAction
  {
    private String colorMapName;
    private ColorMapMetaData md;
    private CinemaAlignmentFrame frame;
    
    ClosureAbstractAction( CinemaAlignmentFrame frame, String title, String colorMapName )
    {
      super( title );
      this.md = getColorFactory().getMetaData( colorMapName );
      this.frame = frame;
    }
    
    public void actionPerformed( ActionEvent event )
    {
      JAlignmentViewer viewer = frame.getViewer();
      
      viewer.setColorMap( md.getInstance( getSequenceAlignment(), viewer ) );
    }
  }

  public String getVersion()
  {
    return "$Id: CinemaColorSelector.java,v 1.8 2001/04/11 17:04:41 lord Exp $";
  }

  public CinemaColorFactory getColorFactory()
  {
    return ((CinemaColorFactory)getRequiredModule
            ( CinemaColorIdentifier.CINEMA_COLOR_FACTORY ) );
  }
  
  public ModuleIdentifierList getRequiredIdentifiers()
  {
    ModuleIdentifierList list = super.getRequiredIdentifiers();
    list.add( CinemaColorIdentifier.CINEMA_COLOR_FACTORY );
    return list;
  }
} // CinemaColorSelector


/*
 * ChangeLog
 * $Log: CinemaColorSelector.java,v $
 * Revision 1.8  2001/04/11 17:04:41  lord
 * Added License agreements to all code
 *
 * Revision 1.7  2001/02/19 16:55:08  lord
 * Have separated CinemaColorSelector into two. One factory for
 * ColorMaps, one display module.
 *
 * Revision 1.6  2000/12/05 15:10:03  lord
 * Updated due to change in MapMetaData interface.
 *
 * Revision 1.5  2000/10/19 17:37:44  lord
 * Import rationalisation.
 * Now extends CinemaFramedActionProvider. Not entirely happy with this,
 * probably needs better coding!
 *
 * Revision 1.4  2000/07/26 13:31:15  lord
 * Major rewrite, due to the addition of the ColorMapMetaData class. Now
 * loads in ColorMaps from config files
 *
 * Revision 1.3  2000/06/13 11:14:00  lord
 * Added new colour map
 *
 * Revision 1.2  2000/06/05 14:11:46  lord
 * Added version method
 *
 * Revision 1.1  2000/05/30 16:06:15  lord
 * Initial checkin
 *
 */
