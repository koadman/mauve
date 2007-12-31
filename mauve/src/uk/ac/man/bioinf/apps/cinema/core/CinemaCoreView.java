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
import java.awt.Color;
import java.util.Dictionary;

import javax.swing.JLabel;

import uk.ac.man.bioinf.apps.cinema.CinemaGuiModule;
import uk.ac.man.bioinf.debug.Debug;
import uk.ac.man.bioinf.gui.color.ColorMap;
import uk.ac.man.bioinf.gui.viewer.AlignmentSelectionModel;
import uk.ac.man.bioinf.module.ModuleIdentifierList;
import uk.ac.man.bioinf.sequence.alignment.GappedSequence;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;
import uk.ac.man.bioinf.sequence.alignment.event.AlignmentEvent;
import uk.ac.man.bioinf.sequence.alignment.event.AlignmentEventProvider;
import uk.ac.man.bioinf.sequence.alignment.event.AlignmentListener;
import uk.ac.man.bioinf.sequence.alignment.event.AlignmentListenerSupport;
import uk.ac.man.bioinf.sequence.alignment.event.VetoableAlignmentListener;


/**
 * CinemaCoreView.java
 *
 * This class provides an easy interface to many of the display
 * functions of Cinema. (PENDING:- PL) In time this should actually be
 * turned into an interface. The idea is that all modules should use
 * this class rather than the CinemaCoreGui class, because there is
 * nothing here which refers to swing which means that we could
 * replace the gui implementation with an entirely different gui, and
 * still reuse many of the modules. 
 *
 * Created: Wed Apr 19 22:00:36 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaCoreView.java,v 1.13 2001/04/11 17:04:41 lord Exp $
 */

public class CinemaCoreView extends CinemaGuiModule implements AlignmentEventProvider, AlignmentListener
{  
  private AlignmentListenerSupport alignSupp = new AlignmentListenerSupport();
  
  public void start()
  {
    getViewer().getSequenceAlignment().addAlignmentListener( this );
  }
  
  public void destroy()
  {
    super.destroy();
    // call an interrupt. The thread should then shut itself down
    if( statusWiperThread != null ){
      statusWiperThread.interrupt();
    }
  }
  
  public void setSequenceAlignment( SequenceAlignment alignment )
  {
    getViewer().setSequenceAlignment( alignment );
    getRuler().setSequenceAlignment( alignment );
    getRowHeaders().setSequenceAlignment( alignment );
    offsetFirstLabel();
  }
  
  public SequenceAlignment getSequenceAlignment()
  {
    return getViewer().getSequenceAlignment();
  }
  
  public void setAlignmentSelectionModel( AlignmentSelectionModel model )
  {
    getViewer().setSelectionModel( model );
  }
  
  public AlignmentSelectionModel getAlignmentSelectionModel()
  {
    return getViewer().getSelectionModel();
  }
  
  public void setColorMap( ColorMap map )
  {
    getViewer().setColorMap( map );
  }
  
  public ColorMap getColorMap()
  {
    return getViewer().getColorMap();
  }

  public void setSequenceTitleColor( GappedSequence seq, Color colour )
  {
    getRowHeaders().setSequenceColor( seq, colour );
  }
  
  public void clearSequenceTitleColor( GappedSequence seq )
  {
    getRowHeaders().clearSequenceColor( seq );
  }
  
    
  // this stuff implements a simple status bar clearer. The status
  // messages stay up for a maximium of five seconds. Its implemented
  // by polling the time stamp every .5 seconds, although perhaps it
  // should be replaced with a "wait/notify" mechanism.
  /**
   * Send a message to the status bar. This message should be for
   * minor information only as it can get overrun at any stage. 
   *
   * @param message
   */
  public void sendStatusMessage( String message )
  {
    // start the wiper thread if it hasnt been already. 
    if( statusWiperThread == null ){
      statusWiperThread = new Thread(){
	  public void run()
	  {
	    while( !CinemaCoreView.this.isDestroyed() ){
	      try{
		Thread.sleep( 500 );
		if( (System.currentTimeMillis() - 
		     CinemaCoreView.this.getStatusUpdateTimestamp())
		    > 5000 ){
		  CinemaCoreView.this.setStatusUpdateTimestamp( System.currentTimeMillis() );
		  CinemaCoreView.this.clearStatusMessage();
		  
		}
	      }
	      catch( InterruptedException ie ){
		// safe to ignore this. 
	      }
	    }
	  }
	};
      statusWiperThread.start();
    }
    // update the time stamp and set the message
    setStatusUpdateTimestamp( System.currentTimeMillis() );
    setStatusMessage( message );
  }
	  
  private long getStatusUpdateTimestamp()
  {
    // need to sync this as the time stamp is a long and access is
    // not guarenteed to be atomic. 
    synchronized( statusWiperThread ){
      return lastStatusUpdateTimestamp;
    }
  }
  
  private void setStatusUpdateTimestamp( long newTimeStamp )
  {
    synchronized( statusWiperThread ){
      lastStatusUpdateTimestamp = newTimeStamp;
    }
  }
  
  // this is the thread that wipes the status bar clean. 
  private Thread statusWiperThread;
  // the time stamp for the access. 
  private long lastStatusUpdateTimestamp;
  
  private void clearStatusMessage()
  {
    if( !getStatusBar().getText().equals( "" ) ){
      setStatusMessage( "" );
    }
  }
  
  private void setStatusMessage( String message )
  {
    if( Debug.debug ){
      Debug.message( this, "Status Message: " + message );
    }
    getStatusBar().setText( message );
    getStatusBar().repaint();
    getStatusBar().validate();
  }
  
  private void offsetFirstLabel()
  {
    // off set the 1 label so it fits on screen 
    Dictionary dic = getRuler().getLabelTable();
    JLabel oneLabel = (JLabel)dic.get( new Integer( 1 ) );
    oneLabel.setText( " 1" );
    getRuler().setLabelTable( dic );
  }
  
  // implementation of AlignmentListener
  public void changeOccurred( AlignmentEvent event )
  {
    alignSupp.fireAlignmentEvent( event );
  }
  
  // implementation of AlignmentEventProvider interface
  public void addAlignmentListener( AlignmentListener listener )
  {
    alignSupp.addAlignmentListener( listener );
  }
  
  public void removeAlignmentListener( AlignmentListener listener )
  {
    alignSupp.removeAlignmentListener( listener );
  }
  
  public void addVetoableAlignmentListener( VetoableAlignmentListener listener )
  {
    alignSupp.addVetoableAlignmentListener( listener );
  }
  
  public void removeVetoableAlignmentListener( VetoableAlignmentListener listener )
  {
    alignSupp.removeVetoableAlignmentListener( listener );
  }
  
  public ModuleIdentifierList getRequiredIdentifiers()
  {
    ModuleIdentifierList list = super.getRequiredIdentifiers();
    list.add( CinemaModuleCoreIdentifier.CINEMA_CORE_GUI );
    return list;
  }

  public String getVersion()
  {
    return "$Id: CinemaCoreView.java,v 1.13 2001/04/11 17:04:41 lord Exp $";
  }
} // CinemaCoreView




/*
 * ChangeLog
 * $Log: CinemaCoreView.java,v $
 * Revision 1.13  2001/04/11 17:04:41  lord
 * Added License agreements to all code
 *
 * Revision 1.12  2001/01/27 16:53:14  lord
 * cosmetic
 *
 * Revision 1.11  2001/01/15 18:50:30  lord
 * Added access to alignment selection model
 *
 * Revision 1.10  2000/12/05 15:54:37  lord
 * Import rationalisation
 *
 * Revision 1.9  2000/10/19 17:38:47  lord
 * Moved all event handling upto CinemaModule
 *
 * Revision 1.8  2000/09/15 17:29:16  lord
 * Now destroyable
 *
 * Revision 1.7  2000/09/11 16:23:04  lord
 * New status bar system. Wipes after 5 seconds of idle now.
 *
 * Revision 1.6  2000/06/05 14:12:16  lord
 * Property change support on setAlignment method
 *
 * Revision 1.5  2000/05/30 16:11:06  lord
 * Changes due to introduction of CinemaModule.
 * Added Status bar support
 *
 * Revision 1.4  2000/05/24 15:36:22  lord
 * Sorted imports.
 * Added accessor function
 *
 * Revision 1.3  2000/05/18 17:12:34  lord
 * Support for row headers
 *
 * Revision 1.2  2000/05/15 16:21:45  lord
 * Added a load of getter setter methods
 *
 */
