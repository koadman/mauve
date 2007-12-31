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
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.RESyntaxException;

import uk.ac.man.bioinf.analysis.regexp.SequenceCharacterIterator;
import uk.ac.man.bioinf.apps.cinema.CinemaModule;
import uk.ac.man.bioinf.apps.cinema.core.CinemaFramedActionProvider;
import uk.ac.man.bioinf.gui.viewer.JAlignmentViewer;
import uk.ac.man.bioinf.sequence.alignment.GappedSequence;
import uk.ac.man.bioinf.sequence.geom.SequenceAlignmentPoint;


/**
 * CinemaRegexp.java
 *
 * Provides support for various regexp searching facilities. 
 *
 * Created: Fri Feb  9 11:38:52 2001
 *
 * @author Phillip Lord
 * @version $Id: CinemaRegexp.java,v 1.4 2001/05/22 16:03:01 lord Exp $
 */

public class CinemaRegexp extends CinemaModule
  implements CinemaFramedActionProvider
{
  private RE regexp = new RE();
  private RECompiler compiler = new RECompiler();
  
  private void searchForward( String regex, CinemaAlignmentFrame frame )
  {
    try{
      // first get the sequence at point
      JAlignmentViewer viewer = frame.getViewer();
      SequenceAlignmentPoint point = viewer.getPoint();
      
      GappedSequence seq = viewer.getSequenceAlignment().
        getSequenceAt( point.getY() );
      
      // now construct the regexp that we need. 
      regexp.setProgram( compiler.compile( regex ) );
      
      SequenceCharacterIterator iter = new SequenceCharacterIterator( seq );
      
      int startPos = point.getX() - viewer.getSequenceAlignment().getInset( point.getY() ) + 1;
      startPos = ((startPos < 0 ) ? startPos = 0: seq.getUngappedPositionOf( startPos ) );
      
      if( regexp.match( iter, startPos ) ){
        
        SequenceAlignmentPoint startPoint = new SequenceAlignmentPoint( point );
        SequenceAlignmentPoint endPoint = new SequenceAlignmentPoint( point );
        
        startPoint.setX( seq.getGappedPositionOf( regexp.getParenStart( 0 ) + 1 ) + 
                         viewer.getSequenceAlignment().getInset( point.getY() ) );
        
        endPoint.setX( ( seq.getGappedPositionOf( regexp.getParenEnd( 0 ) + 1 ) + 
                         viewer.getSequenceAlignment().getInset( point.getY() )  ) );
        
        viewer.ensureSequencePointIsVisible( endPoint );
        viewer.ensureSequencePointIsVisible( startPoint );
        viewer.moveCursorToSequencePoint( startPoint );
      }
      else{
        JOptionPane.showMessageDialog(null, "The search term could not be found", "Not Found",
                                      JOptionPane.ERROR_MESSAGE); 
      }
    }
    catch( RESyntaxException rese ){
      JOptionPane.showMessageDialog(null, "The regular expression you entered is invalid","Invalid regexp", 
                                    JOptionPane.ERROR_MESSAGE); 
    }
  }

  public Action[] getActions( CinemaAlignmentFrame frame )
  {
    Action[] retn = new Action[ 1 ];
    retn[ 0 ] = new FramedAbstractAction( frame, "Search Forward" ){
        private String lastRegexp;
        
        public void actionPerformed( ActionEvent event )
        {
          String search = (String)JOptionPane.showInputDialog
            ( getAlignmentFrame(), "Regexp Search Forward", "Re-search forward",
              JOptionPane.QUESTION_MESSAGE, null, null, lastRegexp );
          lastRegexp = search;
          
          searchForward( search, getAlignmentFrame() );
        }
      };
    return retn;
  }
  
  abstract class FramedAbstractAction extends AbstractAction
  {
    private CinemaAlignmentFrame frame;
    
    FramedAbstractAction( CinemaAlignmentFrame frame, String title )
    {
      super( title );
      this.frame = frame;
    }
    
    CinemaAlignmentFrame getAlignmentFrame()
    {
      return frame;
    }
  }
  
  public String getVersion()
  {
    return "$Id: CinemaRegexp.java,v 1.4 2001/05/22 16:03:01 lord Exp $";
  }
  
} // CinemaRegexp



/*
 * ChangeLog
 * $Log: CinemaRegexp.java,v $
 * Revision 1.4  2001/05/22 16:03:01  lord
 * Lots of numbers and off by one errors have been fixed.
 *
 * Revision 1.3  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.2  2001/03/12 16:47:07  lord
 * Removed debug
 *
 * Revision 1.1  2001/02/19 17:20:53  lord
 * Initial checkin
 *
 */
