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
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import uk.ac.man.bioinf.analysis.consensus.ConsensusSequence;
import uk.ac.man.bioinf.apps.cinema.CinemaModule;
import uk.ac.man.bioinf.apps.cinema.CinemaProperties;
import uk.ac.man.bioinf.apps.xml.ConfigNode;
import uk.ac.man.bioinf.debug.Debug;
import uk.ac.man.bioinf.gui.viewer.ButtonViewerPopupMenu;
import uk.ac.man.bioinf.module.ModuleIdentifierList;
import uk.ac.man.bioinf.sequence.alignment.DefaultSequenceAlignment;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;
import uk.ac.man.bioinf.sequence.alignment.event.AlignmentVetoException;
import uk.ac.man.bioinf.sequence.types.ProteinSequenceType;


/**
 * CinemaMultipleConsensusViewer.java
 *
 *
 * Created: Fri Jun 23 12:09:24 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaMultipleConsensusViewer.java,v 1.7 2001/04/11 17:04:42 lord Exp $
 */

public class CinemaMultipleConsensusViewer extends CinemaModule 
  implements PropertyChangeListener, ActionListener
{
  private CinemaAlignmentFrame alignmentFrame;
  private SequenceAlignment alignmentGroup;
  private JMenuItem clearElements, removeSequence;
  private ButtonViewerPopupMenu buttonPopup;

  public void load()
  {
    // (PENDING:- PL) This sequence type should not be hard coded in here!
    alignmentGroup = new DefaultSequenceAlignment( ProteinSequenceType.getInstance() );
    alignmentFrame = new CinemaAlignmentFrame( "cinema.utils.multiple.consensus",
                                               "Cinema Multiple Consensus Viewer", alignmentGroup );
    
    ConsensusPercentageIDColorMap colorMap = new ConsensusPercentageIDColorMap( alignmentGroup );
    buttonPopup = new ButtonViewerPopupMenu( alignmentFrame.getRowHeaders() );
      
    removeSequence = new JMenuItem( "Remove Sequence" );
    removeSequence.addActionListener( this );
    buttonPopup.add( removeSequence );
    
    alignmentFrame.getViewer().setColorMap( colorMap );
    alignmentFrame.setDefaultSize( 300, 300 );
  }
  
  public void start()
  {
    JMenuBar menuBar = alignmentFrame.getJMenuBar();
    ConfigNode[] mainMenuItems = getConfigTree().getChildNodes();
    CinemaMenuBuilder builder = (CinemaMenuBuilder)getRequiredModule
      ( CinemaUtilityIdentifier.CINEMA_MENU_BUILDER );
    builder.buildMenu( alignmentFrame, menuBar, mainMenuItems );

    addCinemaPropertyChangeListener( CinemaProperties.SEQ_ALIGN.toString(), 
				     this );
  }
  
  public void addConsensusSequence( ConsensusSequence sequence )
  {
    alignmentFrame.setVisible( true );
    try {
      alignmentGroup.addSequence( sequence, 0 );
    } catch (AlignmentVetoException e) {
      /* (PENDING: JNS) 25.10.00 going to do what PL did and just
       * ignore this exception for the mo. It will need dealing with.
       */
      Debug.both(this, "Cinema Multiple Consensus Viewer: " + 
		 "Cannot add consensus sequence because of a veto", e);
    }
  }

  public void removeConsensusSequence( ConsensusSequence sequence )
  {
    try {
      alignmentGroup.removeSequence( alignmentGroup.getSequenceIndex(sequence) );
    } catch (AlignmentVetoException e) {
      /* (PENDING: JNS) 25.10.00 going to do what PL did and just
       * ignore this exception for the mo. It will need dealing with.
       */
      Debug.both(this, "Cinema Multiple Consensus Viewer: " + 
		 "Cannot remove consensus sequence because of a veto", e);
    }
  }
  
  public void destroy()
  {
    alignmentFrame.dispose();
  }
  
  public void actionPerformed( ActionEvent event )
  {
    if( event.getSource() == removeSequence ){
      try {
	alignmentGroup.removeSequence
	  ( alignmentGroup.getSequenceIndex(buttonPopup.getSelectedSequence()) );
      } catch (AlignmentVetoException e) {
	/* (PENDING: JNS) 25.10.00 going to do what PL did and just
	 * ignore this exception for the mo. It will need dealing with.
	 */
	Debug.both(this, "Cinema Multiple Consensus Viewer: " + 
		   "Cannot add consensus sequence", e);
      }
    }
  }
  
  public void propertyChange( PropertyChangeEvent evt )
  {
    if( evt.getPropertyName().equals( CinemaProperties.SEQ_ALIGN.toString() ) ){
      // remove all sequences from the group
      for (int i = 0; i < alignmentGroup.getNumberSequences(); i++)
	try {
	  alignmentGroup.removeSequence(i);
	} catch (AlignmentVetoException e) {
	  /* (PENDING: JNS) 25.10.00 going to do what PL did and just
	   * ignore this exception for the mo. It will need dealing with.
	   */
	  Debug.both(this, "Cinema Multiple Consensus Viewer: " + 
		     "Cannot add consensus sequence", e);
	}
    }
  }
  
  public ModuleIdentifierList getRequiredIdentifiers()
  {
    ModuleIdentifierList list = super.getRequiredIdentifiers();
    list.add( CinemaUtilityIdentifier.CINEMA_MENU_BUILDER );
    return list;
  }
  
  public String getVersion()
  {
    return "$Id: CinemaMultipleConsensusViewer.java,v 1.7 2001/04/11 17:04:42 lord Exp $";
  }
} // CinemaMultipleConsensusViewer



/*
 * ChangeLog
 * $Log: CinemaMultipleConsensusViewer.java,v $
 * Revision 1.7  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.6  2001/02/19 17:20:22  lord
 * Made optionable
 *
 * Revision 1.5  2000/12/05 15:54:37  lord
 * Import rationalisation
 *
 * Revision 1.4  2000/10/26 12:42:49  jns
 * o added editing facilities to SA - this includes insertion/deletion of gaps,
 * addition/removal of sequences from an alignment. It involved resolving some
 * conflicts with the group stuff.
 *
 * Revision 1.3  2000/10/19 17:46:09  lord
 * Some event handling. Remove sequence button added.
 *
 * Revision 1.2  2000/09/15 17:32:55  lord
 * Now destroyable
 *
 * Revision 1.1  2000/06/27 15:56:36  lord
 * Initial checkin
 *
 */
