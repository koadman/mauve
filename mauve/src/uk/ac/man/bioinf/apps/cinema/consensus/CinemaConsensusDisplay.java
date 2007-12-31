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

package uk.ac.man.bioinf.apps.cinema.consensus; // Package name inserted by JPack
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;

import uk.ac.man.bioinf.analysis.consensus.ConsensusSequence;
import uk.ac.man.bioinf.apps.cinema.CinemaGuiModule;
import uk.ac.man.bioinf.apps.cinema.color.CinemaColorFactory;
import uk.ac.man.bioinf.apps.cinema.color.CinemaColorIdentifier;
import uk.ac.man.bioinf.apps.cinema.color.ColorMapMetaData;
import uk.ac.man.bioinf.apps.cinema.core.CinemaActionProvider;
import uk.ac.man.bioinf.apps.cinema.core.CinemaCoreIdentifier;
import uk.ac.man.bioinf.apps.cinema.core.CinemaSystemEvents;
import uk.ac.man.bioinf.apps.invoker.InvokerInternalQueue;
import uk.ac.man.bioinf.gui.viewer.CursorLessFastAlignmentViewerCellRenderer;
import uk.ac.man.bioinf.gui.viewer.JAlignmentViewer;
import uk.ac.man.bioinf.module.ModuleException;
import uk.ac.man.bioinf.module.ModuleIdentifierList;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;
import uk.ac.man.bioinf.sequence.alignment.SingleSequenceAlignment;


/**
 * CinemaConsensusDisplay.java
 *
 *
 * Created: Thu Nov  9 15:06:00 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaConsensusDisplay.java,v 1.10 2001/05/22 15:50:18 lord Exp $
 */

public class CinemaConsensusDisplay extends CinemaGuiModule implements CinemaActionProvider, PropertyChangeListener
{

  private JAlignmentViewer consensusViewer;

  public void load() throws ModuleException
  {
    consensusViewer = new JAlignmentViewer();
    consensusViewer.setReadonly(true);
    consensusViewer.setFastCellRenderer
      (new CursorLessFastAlignmentViewerCellRenderer(consensusViewer));
  }
  
  public void start()
  {
    getRulerPanel().add( consensusViewer, BorderLayout.NORTH );  
    
    syncConsensusSequence( getConsensusModule().getDefaultMetaData(), getSequenceAlignment() );
    
    syncConsensusColorMap();
    getViewer().addPropertyChangeListener( this );
  }

  private static InvokerInternalQueue queue;
  
  private ConsensusSequence cacheConsensus;
  private CinemaConsensusCalculatorMetaData lastMetaData;
  private void syncConsensusSequence( SequenceAlignment align )
  {
    syncConsensusSequence( lastMetaData, align );
  }
  
  private void syncConsensusSequence( CinemaConsensusCalculatorMetaData md, 
                                      SequenceAlignment align )
  {
    lastMetaData = md;
    if( queue == null ){
      queue = new InvokerInternalQueue
	( (CinemaSystemEvents)getRequiredModule( CinemaCoreIdentifier.CINEMA_SYSTEM_EVENTS ) );
    }
    
    ConsensusSequence seq = md.getInstance( align );
  
    consensusViewer.setSequenceAlignment
      ( new SingleSequenceAlignment( seq ) );
    
    if( cacheConsensus != null ){
      cacheConsensus.destroy();
    }
    
    cacheConsensus = seq;

    // jiggle the display a little bit. 
    // So I admit that this is a complete hack but it ensures that the
    // consensus viewer is showing the correct bit. Other wise it
    // shows the start of the sequence. 
    // Actually it also has the advantage of giving the user visual
    // feedback, reassuring them that something has honestly changed. 
    Point point = getScrollPane().getViewport().getViewPosition();
    point.setLocation( point.getX() + 1, point.getY() );
    getScrollPane().getViewport().setViewPosition( point );
    point.setLocation( point.getX() - 1, point.getY() );
    getScrollPane().getViewport().setViewPosition( point );
  }
  
  private void syncConsensusColorMap()
  {
    if( getContext().isModuleAvailable( CinemaColorIdentifier.CINEMA_COLOR_FACTORY ) ){
      try{
	CinemaColorFactory select = 
	  (CinemaColorFactory)getContext().getModule( CinemaColorIdentifier.CINEMA_COLOR_FACTORY );

	ColorMapMetaData md = select.getMetaData( getViewer().getColorMap() );
	
	// md might be null if the colour map did not itself come from
	// the CinemaColorSelector module. This normally will only
	// happen during development, but I might as well trap the
	// error. 
	if( md != null ){
	  consensusViewer.setColorMap
	  ( md.getConsensusInstance
	    ( (ConsensusSequence)((SingleSequenceAlignment)consensusViewer.
				  getSequenceAlignment()).getSingleSequence(),
	      consensusViewer ) );

	}
      }
      catch( ModuleException me ){
	// its okay to just ignore this
      }
    }
  }

  public void propertyChange( PropertyChangeEvent event )
  {
    if( event.getPropertyName().equals( "colorMap" ) ){
      syncConsensusColorMap();
    }
    
    if( event.getPropertyName().equals( "cellWidth" ) ){
      consensusViewer.setCellWidth( ((Integer)event.getNewValue()).intValue() );
    }
    
    if( event.getPropertyName().equals( "cellHeight" ) ){
      consensusViewer.setCellHeight( ((Integer)event.getNewValue()).intValue() );
    }

    if( event.getPropertyName().equals( "sequenceAlignment" ) ){
      syncConsensusSequence( getSequenceAlignment() );
    }
  }
  

  public Action[] getActions()
  {
    Action[] retn = new Action[ getConsensusModule().getAllMetaData().size() + 1 ];
    
    retn[ 0 ] = new ClosureAbstractAction
      ( getConsensusModule().getDefaultMetaData(), "Default" );
    
    Iterator iter = getConsensusModule().getAllMetaData().iterator();
    
    int i = 1;
    while( iter.hasNext() ){
      CinemaConsensusCalculatorMetaData metad = (CinemaConsensusCalculatorMetaData)
        iter.next();
      
      retn[ i++ ] = new ClosureAbstractAction( metad );
      
    }
    return retn;
  }


  class ClosureAbstractAction extends AbstractAction
  {
    protected CinemaConsensusCalculatorMetaData md;
    ClosureAbstractAction( CinemaConsensusCalculatorMetaData md, String title )
    {
      super( title );
      this.md = md;
    }
    
    ClosureAbstractAction( CinemaConsensusCalculatorMetaData md )
    {
      super( md.getConsensusCalculatorName() );
      this.md = md;
    }
    
    public void actionPerformed( ActionEvent event )
    {
      CinemaConsensusDisplay.this.syncConsensusSequence
        ( md, CinemaConsensusDisplay.this.getSequenceAlignment() );
      CinemaConsensusDisplay.this.syncConsensusColorMap();
    }
  }

  public String getVersion()
  {
    return "$Id: CinemaConsensusDisplay.java,v 1.10 2001/05/22 15:50:18 lord Exp $";
  }
  
  public ModuleIdentifierList getRequiredIdentifiers()
  {
    ModuleIdentifierList list = super.getRequiredIdentifiers();
    list.add( CinemaConsensusIdentifier.CINEMA_CONSENSUS );
    list.add( CinemaCoreIdentifier.CINEMA_SYSTEM_EVENTS );
    return list;
  }
  
  private CinemaConsensus cons;
  public CinemaConsensus getConsensusModule()
  {
    if( cons == null ){
      cons = (CinemaConsensus)getRequiredModule( CinemaConsensusIdentifier.CINEMA_CONSENSUS );
    }
    return cons;
  }
} // CinemaConsensusDisplay



/*
 * ChangeLog
 * $Log: CinemaConsensusDisplay.java,v $
 * Revision 1.10  2001/05/22 15:50:18  lord
 * Now jiggles the display a bit when the consensus sequence calculator
 * is changed. Its a bit of a hack but it works.
 *
 * Revision 1.9  2001/05/08 17:38:59  lord
 * Accesses default meta data by new method
 *
 * Revision 1.8  2001/04/11 17:04:41  lord
 * Added License agreements to all code
 *
 * Revision 1.7  2001/02/19 16:55:45  lord
 * Modified due to changes in Color modules.
 *
 * Revision 1.6  2000/12/20 17:43:28  jns
 * o set the ConsensusDisplay to be readonly
 *
 * Revision 1.5  2000/12/12 13:02:47  jns
 * o changing renderer to employ the multiplexer - thus it keeps in step
 * with the main alignment
 *
 * Revision 1.4  2000/12/05 15:54:37  lord
 * Import rationalisation
 *
 * Revision 1.3  2000/12/05 15:10:57  lord
 * Mostly updated due to changes in ColorMap syncing.
 *
 * Revision 1.2  2000/11/13 18:18:28  jns
 * o removed the border because it looked a bit ugly, after I had altered
 * the BAVUI to take account of insets
 *
 * Revision 1.1  2000/11/09 16:17:37  lord
 * Initial checkin
 *
 */
