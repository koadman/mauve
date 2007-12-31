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
import uk.ac.man.bioinf.apps.cinema.color.AminoAcidColorMapMetaData;
import uk.ac.man.bioinf.apps.cinema.color.CinemaColorFactory;
import uk.ac.man.bioinf.apps.cinema.color.CinemaColorIdentifier;
import uk.ac.man.bioinf.gui.color.ColorMap;
import uk.ac.man.bioinf.module.ModuleContext;
import uk.ac.man.bioinf.module.ModuleException;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;


/**
 * CinemaSlaveViewerModule.java
 *
 * Generates arbitrary numbers of Slave Viewers. At lot of the code
 * here is very similar to that in CinemaCoreGui, and it would
 * probably make sense to consolidate some of this into a single
 * place. 
 *
 * The point of a slave viewer is to show an alignment which is not
 * the main alignment. It will not produce events like the main
 * alignment, and will not control everything else. At the moment the
 * main reason for writing this is so that I can test out some other
 * data structures I have written but I think that this will be a
 * useful functionality in the end. 
 *
 * Created: Sun Jun  4 20:26:57 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaSlaveViewerModule.java,v 1.7 2001/04/11 17:04:42 lord Exp $
 */

public class CinemaSlaveViewerModule extends CinemaModule
{
  public void showSlaveAlignment( SequenceAlignment alignment )
  {
    CinemaAlignmentFrame frame = new CinemaAlignmentFrame
      ( "cinema.utils.slave.viewer", "Cinema Slave Viewer", alignment );
    ModuleContext context = getContext();
    
    if( context.isModuleAvailable( CinemaColorIdentifier.CINEMA_COLOR_FACTORY ) ){
      try{
        CinemaColorFactory fact = (CinemaColorFactory)context.getModule( CinemaColorIdentifier.CINEMA_COLOR_FACTORY );
        ColorMap map = fact.getMetaData( AminoAcidColorMapMetaData.NAME ).
          getInstance( getSequenceAlignment(), frame.getViewer() );
        frame.getViewer().setColorMap( map );
      }
      catch( NullPointerException npe ){
      }
      catch( ModuleException me ){
        // just ignore this, its not critical
      }
    }
    
    frame.setDefaultSize( 200, 200 );
    frame.setVisible( true );

  }
  
  //public ModuleIdentifierList get
  
  public String getVersion()
  {
    return "$Id: CinemaSlaveViewerModule.java,v 1.7 2001/04/11 17:04:42 lord Exp $";
  }
} // CinemaSlaveViewerModule



/*
 * ChangeLog
 * $Log: CinemaSlaveViewerModule.java,v $
 * Revision 1.7  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.6  2001/03/24 19:27:12  lord
 * Added standard colour scheme if the relevant modules are
 * available.
 *
 * Revision 1.5  2001/02/19 17:21:23  lord
 * Made optionable
 *
 * Revision 1.4  2000/12/05 15:54:37  lord
 * Import rationalisation
 *
 * Revision 1.3  2000/10/19 17:50:46  lord
 * Does not require CinemaSequenceMenu any more, as this is really only
 * useful for the main Cinema Viewer.
 *
 * Revision 1.2  2000/06/27 15:57:20  lord
 * Now uses the CinemaAlignmentFrame, thereby rationalising the code
 * in here, with that in the CinemaCoreGui
 *
 * Revision 1.1  2000/06/05 14:17:33  lord
 * Initial checkin
 *
 */
