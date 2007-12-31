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
import java.awt.Color;
import java.beans.PropertyChangeListener;

import uk.ac.man.bioinf.apps.cinema.core.CinemaCoreView;
import uk.ac.man.bioinf.apps.cinema.core.CinemaModuleCoreIdentifier;
import uk.ac.man.bioinf.apps.xml.XMLModule;
import uk.ac.man.bioinf.gui.color.ColorMap;
import uk.ac.man.bioinf.gui.viewer.AlignmentSelectionModel;
import uk.ac.man.bioinf.module.ModuleIdentifierList;
import uk.ac.man.bioinf.sequence.alignment.GappedSequence;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;
import uk.ac.man.bioinf.sequence.alignment.event.AlignmentEventProvider;
import uk.ac.man.bioinf.sequence.alignment.event.AlignmentListener;
import uk.ac.man.bioinf.sequence.alignment.event.VetoableAlignmentListener;


/**
 * CinemaModule.java
 *
 * Most modules for Cinema should extend this interface. It provides
 * access to most of the functions of Cinema without being to tied to
 * the main GUI components, which should insulate these modules from
 * any changes which are made to it. 
 * 
 * I intend to include a few other methods here. I think that the
 * CinemaModule should provide access to listeners for the cursor for
 * instance, as this is a concept relatively divorced from that of the
 * GUI.
 *
 * Created: Fri May 26 16:04:49 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaModule.java,v 1.7 2001/04/11 17:04:41 lord Exp $
 */

public abstract class CinemaModule extends XMLModule implements AlignmentEventProvider
{
  
  // These are the methods that define the state of the main window of
  // cinema. At the moment that there are not many of them, though I
  // expect that this will increase!
  public SequenceAlignment getSequenceAlignment()
  {
    return getView().getSequenceAlignment();
  }
  /**
   * Set the Sequence Alignment. Signals a property change event. 
   * @param seq
   */
  public void setSequenceAlignment( SequenceAlignment seq )
  {
    SequenceAlignment old = getSequenceAlignment();
    getView().setSequenceAlignment( seq );
    fireCinemaPropertyChange( CinemaProperties.SEQ_ALIGN.toString(), old, seq );
  }

  public ColorMap getColorMap()
  {
    return getView().getColorMap();
  }
  
  public void setColorMap( ColorMap map )
  {
    firePropertyChange( "colorMap", getColorMap(), map );
    getView().setColorMap( map );
  }

  public AlignmentSelectionModel getAlignmentSelectionModel()
  {
    return getView().getAlignmentSelectionModel();
  }
  
  public void setAlignmentSelectionModel( AlignmentSelectionModel model )
  {
    firePropertyChange( "alignmentSelectionModel", getAlignmentSelectionModel(), model );
    getView().setAlignmentSelectionModel( model );
  }
  
  /**
   * This sets the colour that is used to display the sequence
   * title. NOT the colour of the elements! This is usually done to
   * display some linkage between sequences of the same colour.
   * @param seq
   * @param colour
   */
  public void setSequenceTitleColor( GappedSequence seq, Color colour )
  {
    getView().setSequenceTitleColor( seq, colour );
  }
  
  public void clearSequenceTitleColor( GappedSequence seq )
  {
    getView().clearSequenceTitleColor( seq );
  }
  
  public void sendStatusMessage( String message )
  {
    getView().sendStatusMessage( message );
  }
  
  public void fireCinemaPropertyChange( String name, Object old, Object newV )
  {
    getView().firePropertyChange( name, old, newV );
  }
  
  public void addCinemaPropertyChangeListener( PropertyChangeListener listener )
  {
    getView().addPropertyChangeListener( listener );
  }
  
  public void removeCinemaPropertyChangeListener( PropertyChangeListener listener )
  {
    getView().removePropertyChangeListener( listener );
  }
  
  public void addCinemaPropertyChangeListener( String propertyName, PropertyChangeListener listener )
  {
    getView().addPropertyChangeListener( propertyName, listener );
  }
  
  public void removeCinemaPropertyChangeListener( String propertyName, PropertyChangeListener listener )
  {
    getView().removePropertyChangeListener( propertyName, listener );
  }
  
  // implementation of AlignmentEventProvider interface
  public void addAlignmentListener( AlignmentListener listener )
  {
    getView().addAlignmentListener( listener );
  }
  
  public void removeAlignmentListener( AlignmentListener listener )
  {
    getView().removeAlignmentListener( listener );
  }
  
  public void addVetoableAlignmentListener( VetoableAlignmentListener listener )
  {
    getView().addVetoableAlignmentListener( listener );
  }
  
  public void removeVetoableAlignmentListener( VetoableAlignmentListener listener )
  {
    getView().removeVetoableAlignmentListener( listener );
  }
  
  public void destroy()
  {
    super.destroy();
    view = null;
  }
  
  private CinemaCoreView view;
  private CinemaCoreView getView()
  {
    if( view == null ){
      view = (CinemaCoreView)getRequiredModule( CinemaModuleCoreIdentifier.CINEMA_CORE_VIEW );
    }
    return view;
  }
  
  public ModuleIdentifierList getRequiredIdentifiers()
  {
    ModuleIdentifierList list = super.getRequiredIdentifiers();
    list.add( CinemaModuleCoreIdentifier.CINEMA_CORE_VIEW );
    return list;
  }
} // CinemaModule



/*
 * ChangeLog
 * $Log: CinemaModule.java,v $
 * Revision 1.7  2001/04/11 17:04:41  lord
 * Added License agreements to all code
 *
 * Revision 1.6  2001/01/15 18:50:16  lord
 * Added access to alignment selection model
 *
 * Revision 1.5  2000/10/19 17:35:56  lord
 * Improved event handling
 *
 * Revision 1.4  2000/09/15 17:27:04  lord
 * Now destroyable
 *
 * Revision 1.3  2000/09/11 16:23:04  lord
 * New status bar system. Wipes after 5 seconds of idle now.
 *
 * Revision 1.2  2000/06/05 14:11:13  lord
 * Added CinemaPropertyChangeListener methods
 *
 * Revision 1.1  2000/05/30 16:05:54  lord
 * Initial checkin
 *
 */
