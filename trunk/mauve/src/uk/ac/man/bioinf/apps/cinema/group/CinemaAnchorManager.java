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

package uk.ac.man.bioinf.apps.cinema.group; // Package name inserted by JPack
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import uk.ac.man.bioinf.apps.cinema.CinemaProperties;
import uk.ac.man.bioinf.sequence.alignment.GappedSequence;
import uk.ac.man.bioinf.sequence.alignment.NoGapAtThisPositionException;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;
import uk.ac.man.bioinf.sequence.event.SequenceEvent;
import uk.ac.man.bioinf.sequence.event.SequenceEventType;
import uk.ac.man.bioinf.sequence.event.SequenceListener;


/**
 * CinemaAnchorManager.java
 * 
 * This group takes care of the anchoring groups, and adding and
 * removing appropriate listeners to the main alignment viewer, and
 * then adding and removing gaps.  
 *
 *
 * Created: Wed Feb 21 18:36:38 2001
 *
 * @author Phillip Lord
 * @version $Id: CinemaAnchorManager.java,v 1.3 2001/07/06 11:45:51 lord Exp $
 */

public class CinemaAnchorManager implements PropertyChangeListener
{
  
  // store the listeners in this list. 
  private Map listenerList = new HashMap();
  
  private CinemaGroupModule module;
  
  public CinemaAnchorManager( CinemaGroupModule module )
  {
    this.module = module;
    module.addPropertyChangeListener( CinemaProperties.SEQ_ALIGN.toString(), this );
  }
  
  public void propertyChange( PropertyChangeEvent event )
  {
    // we want to stop listening to all of the groups. 
    if( event.getPropertyName().equals( CinemaProperties.SEQ_ALIGN.toString() ) ){
      unanchorAll();
      listenerList.clear();
    }
  }
  
  public void anchorGroup( CinemaGroup group )
  {
    if( !listenerList.containsKey( group ) ){
      AnchorListener listener = new AnchorListener( group );
      listenerList.put( group, listener );
      group.addSequenceListener( listener );
    }
  }
  
  public void anchorAll()
  {
    CinemaGroup[] group = module.getGroupManager().getAllGroups();
    
    for( int i = 0; i < group.length; i++ ){
      anchorGroup( group[ i ] );
    }
  }
    
  public void unanchorGroup( CinemaGroup group )
  {
    System.out.println( "Unanchoring group " + group );
    if( listenerList.containsKey( group ) ){
      AnchorListener listener = (AnchorListener)listenerList.get( group );
      listenerList.remove( group );
      group.removeSequenceListener( listener );
    }
  }
  
  public void unanchorAll()
  {
    Iterator iter = listenerList.keySet().iterator();
    while( iter.hasNext() ){
      CinemaGroup group = (CinemaGroup)iter.next();
      AnchorListener listener = (AnchorListener)listenerList.get( group );
      group.removeSequenceListener( listener );
    }
    listenerList.clear();
  }
  
  public boolean isAnchored( CinemaGroup group )
  {
    return listenerList.containsKey( group );
  }

  class AnchorListener implements SequenceListener
  {
    private SequenceAlignment alignment;
    // this class actually causes change events to occur, but we want
    // to ignore them or we will get in to a nasty cycle. 
    private boolean internalChange = false;
    
    AnchorListener( SequenceAlignment alignment )
    {
      this.alignment = alignment;
      alignment.addSequenceListener( this );
    }
    
    public void changeOccurred( SequenceEvent event )
    {
      if( !internalChange ){
        // first we need to calculate the row
        if( event.getType() == SequenceEventType.GAPINSERT ){
          int row = alignment.getSequenceIndex( (GappedSequence)event.getSource() );
          
          int col = event.getStart() + 
            alignment.getInset( row  );
          int numberGaps = event.getLength();
          
          // now we need to disable this listener or we will get
          // confused when it adds gaps
          internalChange = true;
          for( int i = 1; i < alignment.getNumberSequences() + 1; i++ ){
            if( i != row ){
              try{
                GappedSequence seq = alignment.getSequenceAt( i );
                seq.insertGapAt( col - alignment.getInset( i ), numberGaps );
                
              }
              catch( Exception exp ){
                exp.printStackTrace(); 
              }
            }
          }
          internalChange = false;
        }else if( event.getType() == SequenceEventType.GAPDELETE ){
          int row = alignment.getSequenceIndex( (GappedSequence)event.getSource() );
          
          int col = event.getStart() + 
            alignment.getInset( row  );
          int numberGaps = event.getLength();
          
          internalChange = true;
          for( int i = 1; i < alignment.getNumberSequences() + 1; i++ ){
            if( i != row ){
              try{
                GappedSequence seq = alignment.getSequenceAt( i );
                seq.deleteGapAt( col - alignment.getInset( i ), numberGaps );
              }
              catch( NoGapAtThisPositionException ngatpe ){
                // so there is no gap at this position. So we can't
                // delete it. So don't
              }
              catch( Exception exp ){
                exp.printStackTrace(); 
              }
            }
          }
          internalChange = false;
        }
      }
    } 
  }
} // CinemaAnchorManager



/*
 * ChangeLog
 * $Log: CinemaAnchorManager.java,v $
 * Revision 1.3  2001/07/06 11:45:51  lord
 * Handling for no gap exceptions
 *
 * Revision 1.2  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.1  2001/03/12 16:44:19  lord
 * Initial checkin
 *
 */

