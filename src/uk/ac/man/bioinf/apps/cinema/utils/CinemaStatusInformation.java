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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.ac.man.bioinf.apps.cinema.CinemaGuiModule;
import uk.ac.man.bioinf.gui.viewer.AlignmentSelectionModel;
import uk.ac.man.bioinf.gui.viewer.SequenceCursor;
import uk.ac.man.bioinf.gui.viewer.event.AlignmentSelectionEvent;
import uk.ac.man.bioinf.gui.viewer.event.AlignmentSelectionListener;
import uk.ac.man.bioinf.sequence.alignment.EmptySequenceAlignment;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;
import uk.ac.man.bioinf.sequence.geom.SequenceAlignmentPoint;
import uk.ac.man.bioinf.sequence.geom.SequenceAlignmentRectangle;
import uk.ac.man.bioinf.sequence.identifier.Identifier;
import uk.ac.man.bioinf.sequence.identifier.NoIdentifier;


/**
 * CinemaStatusInformation.java
 *
 * A utility class which provides what are hopefully useful status bar
 * information messages about what Cinema is currently showing.
 *
 * Created: Fri Jan 26 15:49:33 2001
 *
 * @author Phillip Lord
 * @version $Id: CinemaStatusInformation.java,v 1.3 2001/04/11 17:04:42 lord Exp $
 */

public class CinemaStatusInformation extends CinemaGuiModule implements PropertyChangeListener
{
  public void start()
  {
    
    getViewer().addPropertyChangeListener( this );
    
    addSelectionListener( null, getViewer().getSelectionModel() );
    addCursorListener( null, getViewer().getCursorModel() );
    printSequenceInfo();
    
  }

  public void propertyChange( PropertyChangeEvent event )
  {
    if( event.getPropertyName().equals( "sequenceAlignment" ) ){
      printSequenceInfo();
    }
    
    if( event.getPropertyName().equals( "alignmentSelectionModel" ) ){
      addSelectionListener( (AlignmentSelectionModel)event.getOldValue(), 
			     (AlignmentSelectionModel)event.getNewValue() );
    }

    if( event.getPropertyName().equals( "sequenceCursor" ) ){
      addCursorListener( (SequenceCursor)event.getOldValue(), 
			     (SequenceCursor)event.getNewValue() );
    }
  }
  
  private void printSequenceInfo()
  {
    SequenceAlignment seq = getViewer().getSequenceAlignment();
    
    if( !(seq instanceof EmptySequenceAlignment ) ){
      sendStatusMessage
	( "Loaded " + seq.getIdentifier().getTitle() + " from " + 
	  seq.getIdentifier().getSource().getTitle() );
      
      Identifier identifier;
      if( (identifier = seq.getIdentifier()) instanceof NoIdentifier ){
	setFrameTitle( identifier.getSource().getTitle() );
      }
      else{
	setFrameTitle( identifier.getTitle() );
      }
    }
  }
  
  private AlignmentSelectionListener selectionListener;
  
  private void addSelectionListener( AlignmentSelectionModel oldMod, AlignmentSelectionModel newMod )
  {
    if( selectionListener == null ){
      selectionListener = new AlignmentSelectionListener(){
	  public void valueChanged( AlignmentSelectionEvent event )
	  {
	    printSelectionInfo( event.getSelectionRectangle() );
	  }
	};
    }
    
    if( oldMod != null ) oldMod.removeAlignmentSelectionListener( selectionListener );
    if( newMod != null ) newMod.addAlignmentSelectionListener( selectionListener );
  }
  
  private void printSelectionInfo( SequenceAlignmentRectangle rect )
  {
    if( rect != null ){
      sendStatusMessage
	( "Selection: ( " + rect.getX() + " , " + rect.getY() + 
	  " - " + rect.getWidth() +  " x " + rect.getHeight() + " )" ) ;
    }
  }
  
  private ChangeListener cursorListener;
  
  private void addCursorListener( SequenceCursor oldMod, SequenceCursor newMod )
  {
    if( cursorListener == null ){
      cursorListener = new ChangeListener(){
	  public void stateChanged( ChangeEvent event )
	  {
	    printCursorInfo();
	  }
	};
    }
    
    if( oldMod != null ) oldMod.removeChangeListener( cursorListener );
    if( newMod != null ) newMod.addChangeListener( cursorListener );
  }
  
  private void printCursorInfo()
  {
    SequenceAlignmentPoint point = getViewer().getPoint();
    
    sendStatusMessage
      ( getSequenceAlignment().getIdentifier().getTitle() + " @ ( " + point.getX() + " , " + 
        point.getY() + " )"  );
  }
  
  public String getVersion()
  {
    return "$Id: CinemaStatusInformation.java,v 1.3 2001/04/11 17:04:42 lord Exp $";
  }
  
}// CinemaStatusInformation



/*
 * ChangeLog
 * $Log: CinemaStatusInformation.java,v $
 * Revision 1.3  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.2  2001/01/27 17:08:33  lord
 * Does not update title bar for EmptySequenceAlignment
 *
 * Revision 1.1  2001/01/27 16:56:27  lord
 * Initial checkin
 *
 */
