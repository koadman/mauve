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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import uk.ac.man.bioinf.apps.cinema.CinemaModule;
import uk.ac.man.bioinf.apps.cinema.CinemaProperties;
import uk.ac.man.bioinf.apps.cinema.consensus.CinemaConsensusIdentifier;
import uk.ac.man.bioinf.apps.cinema.core.CinemaActionProvider;
import uk.ac.man.bioinf.apps.cinema.core.CinemaCoreIdentifier;
import uk.ac.man.bioinf.apps.cinema.core.CinemaSequenceMenu;
import uk.ac.man.bioinf.apps.cinema.utils.CinemaUtilityIdentifier;
import uk.ac.man.bioinf.module.ModuleIdentifierList;
import uk.ac.man.bioinf.sequence.alignment.GappedSequence;
import uk.ac.man.bioinf.sequence.alignment.event.AlignmentEvent;
import uk.ac.man.bioinf.sequence.alignment.event.AlignmentEventType;
import uk.ac.man.bioinf.sequence.alignment.event.AlignmentListener;


/**
 * CinemaGroupModule.java
 *
 *
 * Created: Tue May 23 15:42:48 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaGroupModule.java,v 1.15 2001/04/11 17:04:42 lord Exp $
 */
public class CinemaGroupModule extends CinemaModule 
  implements ActionListener, CinemaActionProvider, PropertyChangeListener, AlignmentListener
{
  private JMenuItem createNewGroup, removeFromGroup;
  private JMenu deleteGroup, addToGroup;
  
  private CinemaGroupManager groups;
  private CinemaSequenceMenu seqMenu;
  private CinemaGroupFrame groupDialog;
  private CinemaAnchorManager anchor;
  

  public void load()
  {
    groups = new CinemaGroupManager( this );
    groups.addPropertyChangeListener( this );
    anchor = new CinemaAnchorManager( this );
  }
  
  public void start()
  {
    seqMenu = getMenuSystem();
    
    createNewGroup = new JMenuItem( "Create New Group" );
    createNewGroup.addActionListener( this );
    seqMenu.add( createNewGroup );
   
    deleteGroup = new JMenu( "Delete Group" );
    
    addToGroup = new JMenu( "Add to Group" );
    
    removeFromGroup = new JMenuItem( "Remove From Group" );
    removeFromGroup.addActionListener( this );
    
    regenerateGroupMenu( groups.getAllGroups() );
    seqMenu.getPopupMenu().addSeparator();
    addCinemaPropertyChangeListener( CinemaProperties.SEQ_ALIGN.toString(), this );
  }    
  
  public GappedSequence getLastSequence()
  {
    return getMenuSystem().getLastSequence();
  }
  
  public CinemaAnchorManager getAnchorManager()
  {
    return anchor;
  }
  
  public CinemaGroupManager getGroupManager()
  {
    return groups;    
  }
  
  private boolean groupMenusPresent = false;
  
  private void regenerateGroupMenu( CinemaGroup[] cinemaGroups )
  {
    if( cinemaGroups.length > 0 ){
      if( !groupMenusPresent ){
	int i;
	
	for( i = 0; i < seqMenu.getPopupMenu().getComponentCount(); i++ ){
	  if( seqMenu.getPopupMenu().getComponent( i ) == createNewGroup ) break;
	} //end for( i < seqMenu.getComponentCount() )
	
	i++;
	seqMenu.getPopupMenu().add( removeFromGroup, i );
	seqMenu.getPopupMenu().add( addToGroup, i );
	seqMenu.getPopupMenu().add( deleteGroup, i );
	groupMenusPresent = true;
      }
      
      deleteGroup.removeAll();
      addToGroup.removeAll();
      
      for( int i = 0; i< cinemaGroups.length; i++ ){
	// put the menu item for the delete group 
	JMenuItem delGroup = new JMenuItem( cinemaGroups[ i ].getTitle() );
	deleteGroup.add( delGroup );
	delGroup.addActionListener( new DeleteGroupActionListener( cinemaGroups[ i ] ) );
	
	// put the menu item for the add to group
	JMenuItem addGroup = new JMenuItem( cinemaGroups[ i ].getTitle() );
	addToGroup.add( addGroup );
	addGroup.addActionListener( new AddToGroup( cinemaGroups[ i ] ) );
      }
    }
    else{
      // there are no cinema groups, therefore remove the delete, add, and remove from group buttons.
      seqMenu.getPopupMenu().remove( removeFromGroup );
      seqMenu.getPopupMenu().remove( addToGroup ); 
      seqMenu.getPopupMenu().remove( deleteGroup );
      groupMenusPresent = false;
    }
  }
  
  class DeleteGroupActionListener implements ActionListener
  {
    private CinemaGroup group;
    
    DeleteGroupActionListener( CinemaGroup group )
    {
      this.group = group;
    }
    
    public void actionPerformed( ActionEvent event )
    {
      int retVal = JOptionPane.showConfirmDialog
	( null, "Do you really want to delete group \"" + group.getTitle() + "\"",
	  "Delete Group", JOptionPane.YES_NO_OPTION );
      
      if( retVal == JOptionPane.OK_OPTION ){
	GappedSequence[] seqs = group.getGappedSequences();
	
	for( int i = 0; i < seqs.length; i++ ){
	  clearSequenceTitleColor( seqs[ i ] );
	}
	groups.removeGroup( group );
      }
    }
  }
  
  class AddToGroup implements ActionListener
  {
    private CinemaGroup group;
    
    AddToGroup( CinemaGroup group )
    {
      this.group = group;
    }
    
    public void actionPerformed( ActionEvent event )
    {   
      GappedSequence seq = getLastSequence();
      group.addSequenceToGroup( seq );
      setSequenceTitleColor( seq, group.getColor() );
    }
  }
  
  private CinemaSequenceMenu menuSystem;
  public CinemaSequenceMenu getMenuSystem()
  {
    if( menuSystem == null ){
      menuSystem =  (CinemaSequenceMenu)getRequiredModule( CinemaCoreIdentifier.CINEMA_SEQUENCE_MENU );
    }
    return menuSystem;
  }
  
  public void actionPerformed( ActionEvent event )
  {
    Object source = event.getSource();
    
    if( source == createNewGroup ){
      // (PENDING:- PL) Need a dialog box which returns a name I think
      groups.createNewGroup();
    }
    
    if( source == removeFromGroup ){
      GappedSequence seq = getLastSequence();
      CinemaGroup group = groups.getGroupContaining( seq );
      if( group != null ){
	group.removeSequenceFromGroup( seq );
	clearSequenceTitleColor( seq );
      }
    }
  }
  
  public String getVersion()
  {
    return "$Id: CinemaGroupModule.java,v 1.15 2001/04/11 17:04:42 lord Exp $";
  }
  
  public ModuleIdentifierList getRequiredIdentifiers()
  {
    ModuleIdentifierList list = super.getRequiredIdentifiers();
    list.add( CinemaCoreIdentifier.CINEMA_SEQUENCE_MENU );
    list.add( CinemaUtilityIdentifier.CINEMA_SLAVE_VIEWER );
    list.add( CinemaConsensusIdentifier.CINEMA_CONSENSUS );
    list.add( CinemaUtilityIdentifier.CINEMA_MULTIPLE_CONSENSUS );
    return list;
  }
  
  // forwards groups events 
  public void propertyChange( PropertyChangeEvent event )
  {
    // (PENDING:- PL) This needs to be altered to switch a boolean,
    // which can regenerate the group menu when it is needed, rather
    // than automatically....
    if( event.getPropertyName().equals( CinemaGroupManager.groupsName ) ){
      // we need to ensure that we are listening only to current
      // groups. 
      
      // remove this as a listener       
      CinemaGroup[] oldGroups = (CinemaGroup[])event.getOldValue();
      for( int i = 0; i < oldGroups.length; i++ ){
	oldGroups[ i ].removeAlignmentListener( this );
      }
      
      CinemaGroup[] newGroups = groups.getAllGroups();
      for( int i = 0; i < newGroups.length; i++ ){
	newGroups[ i ].addAlignmentListener( this );
      }
      
      // and regenerate the group menu so that it is correct also. 
      regenerateGroupMenu( groups.getAllGroups() );
    }

    if( event.getPropertyName().equals( CinemaProperties.SEQ_ALIGN.toString() ) ){
      // remove all the groups as they are all going to be wrong.
      CinemaGroup[] allGroups = groups.getAllGroups();
      
      for( int i = 0; i < allGroups.length; i++ ){
	groups.removeGroup( allGroups[ i ] );
      }

      regenerateGroupMenu( groups.getAllGroups() );
    }
    
    if( event.getPropertyName().equals( "title" ) ){
      regenerateGroupMenu( groups.getAllGroups() );
    }
    
    if( event.getPropertyName().equals( CinemaGroupManager.groupsOrder ) ){
      regenerateGroupMenu( groups.getAllGroups() );
    }
  }
  
  public void changeOccurred( AlignmentEvent event )
  {
    // it will be a CinemaGroup that has caused this alignment event. 
    CinemaGroup group = (CinemaGroup)event.getSource();
    Color col = group.getColor();
    
    if( event.getType() == AlignmentEventType.INSERT ){
      for( int i = event.getStart(); i < event.getEnd() + 1; i++ ){
	setSequenceTitleColor( group.getSequenceAt( i ), col );
      }
    }

    if( event.getType() == AlignmentEventType.DELETE ){
      for( int i = event.getStart(); i < event.getEnd() + 1; i++ ){
	clearSequenceTitleColor( group.getSequenceAt( i ) );
      }
    }
  }
  
  public Action[] getActions()
  {
    Action[] retn = new Action[ 1 ];
    retn[ 0 ] = new AbstractAction( "Group Editor" ){
	public void actionPerformed( ActionEvent event )
	{
	  if( groupDialog == null ){
	    groupDialog = new CinemaGroupFrame( CinemaGroupModule.this );
	  }
	  groupDialog.setVisible( true );
	}
      };
    return retn;
  }
} // CinemaGroupModule



/*
 * ChangeLog
 * $Log: CinemaGroupModule.java,v $
 * Revision 1.15  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.14  2001/03/12 16:45:04  lord
 * Added support for anchoring
 *
 * Revision 1.13  2000/11/10 15:12:32  lord
 * CinemaMultipleConsensus re-enabled
 *
 * Revision 1.12  2000/11/09 16:42:13  lord
 * Package update
 *
 * Revision 1.11  2000/10/26 12:42:49  jns
 * o added editing facilities to SA - this includes insertion/deletion of gaps,
 * addition/removal of sequences from an alignment. It involved resolving some
 * conflicts with the group stuff.
 *
 * Revision 1.10  2000/10/19 17:43:17  lord
 * Lots of small changes, to extend the functionality.
 *
 * Revision 1.9  2000/10/11 15:41:36  lord
 * Support for sorting of groups
 *
 * Revision 1.8  2000/10/03 17:12:58  lord
 * Confirm dialog on delete group added
 *
 * Revision 1.7  2000/08/01 17:09:15  lord
 * Now requires CinemaConsensus
 *
 * Revision 1.6  2000/07/18 11:00:34  lord
 * Updated listener and event handling. Now ceases to listen to an
 * Group once it has been removed from the Manager
 *
 * Revision 1.5  2000/06/27 15:54:35  lord
 * Now requires Consensus Module
 *
 * Revision 1.4  2000/06/13 11:17:41  lord
 * Now listens for changes in titles
 *
 * Revision 1.3  2000/06/05 14:17:15  lord
 * Now checks to see whether there are any groups before adding menu
 * items.
 * Implemented ActionProvider interface.
 * Added support for CinemaGroupFrame popup
 *
 * Revision 1.2  2000/05/30 16:16:20  lord
 * Mostly changes due to module architecture completion
 *
 * Revision 1.1  2000/05/24 15:42:16  lord
 * Initial checkin
 *
 */
