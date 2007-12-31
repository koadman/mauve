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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;

import uk.ac.man.bioinf.analysis.consensus.ConsensusSequence;
import uk.ac.man.bioinf.apps.cinema.consensus.CinemaConsensus;
import uk.ac.man.bioinf.apps.cinema.consensus.CinemaConsensusIdentifier;
import uk.ac.man.bioinf.apps.cinema.utils.CinemaMultipleConsensusViewer;
import uk.ac.man.bioinf.apps.cinema.utils.CinemaSlaveViewerModule;
import uk.ac.man.bioinf.apps.cinema.utils.CinemaUtilityIdentifier;
import uk.ac.man.bioinf.gui.misc.ChunkyListModel;
import uk.ac.man.bioinf.gui.misc.SwappingListBasicUI;
import uk.ac.man.bioinf.gui.optionable.OptionableJFrame;
import uk.ac.man.bioinf.gui.optionable.OptionableJSplitPane;
import uk.ac.man.bioinf.sequence.Sequence;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;
import uk.ac.man.bioinf.sequence.alignment.event.AlignmentEvent;
import uk.ac.man.bioinf.sequence.alignment.event.AlignmentListener;


/**

 * CinemaGroupFrame.java
 *
 *
 * Created: Fri Jun  2 19:19:00 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaGroupFrame.java,v 1.17 2001/07/10 13:40:18 lord Exp $

 */

public class CinemaGroupFrame extends OptionableJFrame 
  implements PropertyChangeListener, ListSelectionListener, ActionListener, AlignmentListener
{
  private JList groupsList;
  private CinemaGroupModule module;
  private ChunkyListModel groupsListModel, sequencesListModel;
  private JMenuItem viewGroupAlignment, editGroupName, addToConsensus, createNewGroup, 
    addAllAsGroup, anchor, unanchor, anchorAll, unanchorAll;
  
  public CinemaGroupFrame( CinemaGroupModule module )
  {
    super( "group.cinema.frame", "Cinema Sequence Groups" );
    this.module = module;
    
    module.getGroupManager().addPropertyChangeListener( this );

    Container contentPane = getContentPane();
    OptionableJSplitPane splitPane = new OptionableJSplitPane( "group.cinema.frame.split.pane",
                                                     JSplitPane.VERTICAL_SPLIT );
    contentPane.setLayout( new BorderLayout() );
    contentPane.add( splitPane, BorderLayout.CENTER );
    
    JPanel topPanel    = new JPanel();
    JPanel bottomPanel = new JPanel();
    
    // split the frame into two panels
    topPanel.setBorder( LineBorder.createGrayLineBorder() );
    bottomPanel.setBorder( LineBorder.createGrayLineBorder() );
    
    splitPane.setTopComponent( topPanel );
    splitPane.setBottomComponent( bottomPanel );
    
    
    // but a scroll pane in the top panel
    groupsList = new JList( groupsListModel = new ChunkyListModel() );
    groupsList.setCellRenderer( new GroupListCellRenderer() );
    groupsList.addListSelectionListener( this );
    groupsList.setUI( new InformativeSwappingListBasicUI( module ) );
    topPanel.setLayout( new BorderLayout() );
    JScrollPane groupListScrollPane = new JScrollPane( groupsList );
    groupListScrollPane.setBorder
      ( new TitledBorder( LineBorder.createGrayLineBorder(), "Groups" ) );
    topPanel.add( groupListScrollPane, BorderLayout.CENTER );
    


    JList groupMemberList = new JList( sequencesListModel = new ChunkyListModel() );
    groupMemberList.setCellRenderer( new GroupMemberCellRenderer() );
    bottomPanel.setLayout( new BorderLayout() );
    JScrollPane groupMembersListScrollPane = new JScrollPane( groupMemberList );
    groupMembersListScrollPane.setBorder
      ( new TitledBorder( LineBorder.createGrayLineBorder(), "Group Members" ) );
    bottomPanel.add( groupMembersListScrollPane, BorderLayout.CENTER );
    
    
    
    JMenuBar menuBar = new JMenuBar();
    setJMenuBar( menuBar );
    JMenu groups = new JMenu( "Group" );
    menuBar.add( groups );
    
    editGroupName = new JMenuItem( "Change Group Name" );
    editGroupName.addActionListener( this );
    groups.add( editGroupName );    
    
    createNewGroup = new JMenuItem( "Create New Group" );
    createNewGroup.addActionListener( this );
    groups.add( createNewGroup );
    
    addAllAsGroup = new JMenuItem( "Add all sequences as group" );
    addAllAsGroup.addActionListener( this );
    groups.add( addAllAsGroup );
    
    JMenu view = new JMenu( "View" );
    menuBar.add( view );
    
    viewGroupAlignment = new JMenuItem( "View Alignment" );
    viewGroupAlignment.addActionListener( this );
    view.add( viewGroupAlignment );
    
    addToConsensus = new JMenuItem( "Add to Consensus Viewer" );
    addToConsensus.addActionListener( this );
    view.add( addToConsensus );

    JMenu edit = new JMenu( "Edit" );
    menuBar.add( edit );
    
    
    anchor = new JMenuItem( "Anchor group" );
    anchor.addActionListener( this );
    edit.add( anchor );
    
    unanchor = new JMenuItem( "Unanchor group" );
    unanchor.addActionListener( this );
    edit.add( unanchor );
    
    anchorAll = new JMenuItem( "Anchor all" );
    anchorAll.addActionListener( this );
    edit.add( anchorAll );
    
    unanchorAll = new JMenuItem( "Unanchor all" );
    unanchorAll.addActionListener( this );
    edit.add( unanchorAll );
    
    buildGroupsList();

    splitPane.setDefaultDividerLocation( 150 );
    setDefaultSize( 400, 300 );
    setVisible( true );
  }

  /**
   * Builds the groups list into the Groups List JList
   */
  private void buildGroupsList()
  {
    groupsListModel.clear();
    groupsListModel.addAll( module.getGroupManager().getAllGroups() );
    if( groupsListModel.getSize() == 1 ){
      groupsList.setSelectedIndex( 0 );
    }
  }
  
  private CinemaGroup getSelectedGroup()
  {
    if( groupsList.getSelectedIndex() != - 1 ){
      return (CinemaGroup)groupsListModel.getElementAt( groupsList.getSelectedIndex() );
    }
    return null;
  }
  
  // Event listener interfaces....
  public void actionPerformed( ActionEvent event )
  {
    Object src = event.getSource();
    
    if( src == viewGroupAlignment ){
      CinemaGroup selected;
      
      if( (selected = getSelectedGroup()) != null ){
	CinemaSlaveViewerModule slaveViewerModule
	  = (CinemaSlaveViewerModule)module.getRequiredModule( CinemaUtilityIdentifier.CINEMA_SLAVE_VIEWER );
	slaveViewerModule.showSlaveAlignment
	  ( selected );
      }
    } 
    else if( src == editGroupName ){
      String newName = JOptionPane.showInputDialog( this, "Please enter the new group name", 
						    "New Name", JOptionPane.QUESTION_MESSAGE );
      if( newName != null ){
	getSelectedGroup().setTitle( newName );
      }
    } 
    else if( src == createNewGroup ){
      module.getGroupManager().createNewGroup();
    } 
    else if( src == addToConsensus ){
      CinemaGroup selected;
      
      if( (selected = getSelectedGroup()) != null ){

        System.out.println( module.getRequiredModule( CinemaConsensusIdentifier.CINEMA_CONSENSUS ) );
        System.out.println(  ((CinemaConsensus)module.getRequiredModule( CinemaConsensusIdentifier.CINEMA_CONSENSUS )).
                             getDefaultMetaData() );
        
        System.out.println( ((CinemaConsensus)module.getRequiredModule( CinemaConsensusIdentifier.CINEMA_CONSENSUS )).
                            getDefaultMetaData().getInstance( selected ) );
        

	ConsensusSequence cons =
	  ((CinemaConsensus)module.getRequiredModule( CinemaConsensusIdentifier.CINEMA_CONSENSUS )).
	  getDefaultMetaData().getInstance( selected );


	((CinemaMultipleConsensusViewer)module.getRequiredModule
         ( CinemaUtilityIdentifier.CINEMA_MULTIPLE_CONSENSUS ))
	  .addConsensusSequence( cons );
      }    
    }
    else if( src == addAllAsGroup ){
      SequenceAlignment align = module.getSequenceAlignment();
      CinemaGroup group = module.getGroupManager().createNewGroup( "All", getBackground() );
      
      for( int i = 1; i < (align.getNumberSequences() + 1) ; i ++ ){
	group.addSequenceToGroup( align.getSequenceAt( i ) );
      }
    }
    else if( src == anchor ){
      module.getAnchorManager().anchorGroup( getSelectedGroup() );
      repaint();
    }
    else if( src == unanchor ){
      module.getAnchorManager().unanchorGroup( getSelectedGroup() );
      repaint();
    }
    else if( src == anchorAll ){
      module.getAnchorManager().anchorAll();
      repaint();
    }
    else if( src == unanchorAll ){
      module.getAnchorManager().unanchorAll();
      repaint();
    }
  }
  
  private CinemaGroup lastSelectedGroup;
  
  public void valueChanged( ListSelectionEvent event )
  {
    // single selection so first is the same as the second
    event.getFirstIndex();
    
    CinemaGroup selectedGroup = getSelectedGroup();
    if( selectedGroup == null ) return;
    
    // ensure that we are only listening to the group that is
    // currently being displayed
    if( lastSelectedGroup != null ) 
      lastSelectedGroup.removeAlignmentListener( this );
    selectedGroup.addAlignmentListener( this );
    lastSelectedGroup = selectedGroup;
    
    displaySequenceGroup( selectedGroup );
  }
  
  private void displaySequenceGroup( CinemaGroup selectedGroup )
  {
    // display the newly selected sequence group. 
    sequencesListModel.clear();
    sequencesListModel.addAll
      ( selectedGroup.getSequences() );
  }
  
  public void changeOccurred( AlignmentEvent event )
  {
    displaySequenceGroup( lastSelectedGroup );
  }
  
  public void propertyChange( PropertyChangeEvent event )
  {
    if( event.getPropertyName().equals( CinemaGroupManager.groupsName ) ){
      // if the currently displayed group has been removed, then clear the sequences list. 
      CinemaGroup[] groups = (CinemaGroup[])event.getNewValue();
      boolean found = false;
      for( int i = 0; i < groups.length; i++ ){
	if( groups[ i ] == lastSelectedGroup ){
	  found = true;
	  i = groups.length;
	}
      }
      
      if( !found ) sequencesListModel.clear();
      
      // then rebuild the group list either way because we know that it has changed. 
      buildGroupsList();
    }
    
    if( event.getPropertyName().equals( "title" ) ){
      buildGroupsList();
    }
  }

  class GroupListCellRenderer extends DefaultListCellRenderer
  {
    public Component getListCellRendererComponent
      ( JList list, Object value, int index, 
	boolean isSelected, boolean cellHasFocus )
    {
      CinemaGroup group = (CinemaGroup)value;
      
      String title = ((module.getAnchorManager().isAnchored( group )) ? "Anc:" : "")
        + group.getTitle();
      
      return super.getListCellRendererComponent
	( list, title, index, isSelected, cellHasFocus );
    }
  }
  
  class GroupMemberCellRenderer extends DefaultListCellRenderer
  {
    public Component getListCellRendererComponent
      ( JList list, Object value, int index, 
	boolean isSelected, boolean cellHasFocus )
    {
      return super.getListCellRendererComponent
	( list, ((Sequence)value).getIdentifier().getTitle(), index, isSelected, cellHasFocus );
    }
  }

  // the SwappingListBasicUI allows the user to drag the list elements
  // around. This extension of it traps the swap events, and signals
  // the CinemaGroupManager that this is being done!
  class InformativeSwappingListBasicUI extends SwappingListBasicUI
  {
    private CinemaGroupManager manager;
    
    InformativeSwappingListBasicUI( CinemaGroupModule mod )
    {
      manager = mod.getGroupManager();
    }
    
    protected MouseInputListener createMouseInputListener()
    {
      return new InformativeSwappingListHandler();
    }
    
    public class InformativeSwappingListHandler extends SwappingMouseInputHandler
    {
      protected void swap( int max, int min )
      {
	
	// this code is copied directly from that super class, except
	// for the cast. As far as I can see there is no way of
	// inheriting this type information, and preventing codeyy
	// duplication. 
	
	// actually do the swap on the gui
	JList list = InformativeSwappingListBasicUI.this.getJList();
      
	ChunkyListModel model = (ChunkyListModel)list.getModel();
	
	// this lots just swaps the stuff over
	Object upper = model.remove( max );
	Object lower = model.getElementAt( min );
	model.insertElementAt( lower, max );
	model.remove( min );
	model.insertElementAt( upper, min );
	
	//inform the manager
	manager.swapGroupOrder( max, min );
      }
    }
  }
  
  public static void main( String[] args )
  {
    CinemaGroupFrame frame = new CinemaGroupFrame( null );
    frame.setVisible( true );
    frame.pack();
    frame.repaint();
  } //end main method 
  
} // CinemaGroupFrame



/*
 * ChangeLog
 * $Log: CinemaGroupFrame.java,v $
 * Revision 1.17  2001/07/10 13:40:18  lord
 * Fixed multi alignment viewer bug caused due to method name change
 *
 * Revision 1.16  2001/07/06 11:47:17  lord
 * Ensure one group is always selected
 *
 * Revision 1.15  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.14  2001/03/12 16:44:40  lord
 * Added support for anchoring
 *
 * Revision 1.13  2001/02/19 17:00:55  lord
 * Added optionability
 *
 * Revision 1.12  2000/12/05 15:54:37  lord
 * Import rationalisation
 *
 * Revision 1.11  2000/11/08 18:33:20  lord
 * Changes to reflect alternation in ConsensusSequence interface
 *
 * Revision 1.10  2000/10/26 12:42:49  jns
 * o added editing facilities to SA - this includes insertion/deletion of gaps,
 * addition/removal of sequences from an alignment. It involved resolving some
 * conflicts with the group stuff.
 *
 * Revision 1.9  2000/10/19 17:43:17  lord
 * Lots of small changes, to extend the functionality.
 *
 * Revision 1.8  2000/10/12 11:33:29  lord
 * Fixed bug with getJList call
 *
 * Revision 1.7  2000/10/11 15:41:36  lord
 * Support for sorting of groups
 *
 * Revision 1.6  2000/10/03 17:24:41  lord
 * Support added for "createNewGroup"
 *
 * Revision 1.5  2000/08/01 17:08:53  lord
 * Now calls the new CinemaConsensus API
 *
 * Revision 1.4  2000/07/18 10:40:15  lord
 * Import rationalisation.
 * Changes due to removal of biointerface
 *
 * Revision 1.3  2000/06/27 13:45:19  lord
 * Added in multiple consensus viewer logic
 *
 * Revision 1.2  2000/06/13 11:16:49  lord
 * Added Edit Group Name and event handling needed for it.
 * Added slave alignment viewer
 *
 * Revision 1.1  2000/06/05 14:14:43  lord
 * Initial checkin
 *
 */




