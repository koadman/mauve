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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Dictionary;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.border.MatteBorder;

import org.gel.mauve.cinema.EditorAlignmentButtonPanel;

import uk.ac.man.bioinf.gui.optionable.OptionableJFrame;
import uk.ac.man.bioinf.gui.viewer.InvertedScrollPaneLayout;
import uk.ac.man.bioinf.gui.viewer.JAlignmentButtonPanel;
import uk.ac.man.bioinf.gui.viewer.JAlignmentRuler;
import uk.ac.man.bioinf.gui.viewer.JAlignmentViewer;
import uk.ac.man.bioinf.gui.viewer.JChangedScrollPane;
import uk.ac.man.bioinf.gui.viewer.MultiplexerFastAlignmentViewerCellRenderer;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;


/**
 * CinemaAlignmentFrame.java
 *
 * This class provides a basic CinemaAlignmentFrame which consists of
 * a JScrollPane, with an JAlignmentViewer in the middle,
 * JAlignmentButton panel down the right hand side and a
 * JAlignmentRuler across the bottom. 
 *
 * Created: Tue Jun 20 15:01:39 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaAlignmentFrame.java,v 1.12 2001/04/11 17:04:42 lord Exp $
 */

public class CinemaAlignmentFrame extends OptionableJFrame implements PropertyChangeListener
{
  private JPanel mainPanel, rulerPanel, rowHeadersPanel;
  private JAlignmentViewer viewer;
  private JScrollPane scrollPane;
  private JAlignmentButtonPanel rowHeaders;
  private JAlignmentRuler ruler;
  private Dimension initialCellSize;
  
  public CinemaAlignmentFrame( String optionableName, String title, SequenceAlignment alignment )
  {
    this( optionableName, title );
    setSequenceAlignment( alignment );
  }
  
  public CinemaAlignmentFrame( String optionableName, String title )
  {
    this( optionableName, title, false );
  }
  
  public CinemaAlignmentFrame( String optionableName, String title, boolean delayOptionable )
  {
    super( optionableName, title, delayOptionable );
    setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE);
    
    // set up a main panel to hold the scroll pane
    mainPanel = new JPanel();
    mainPanel.setLayout( new BorderLayout() );
    
    // start up the main viewer
    viewer = new JAlignmentViewer();
    viewer.setFastCellRenderer
      ( new MultiplexerFastAlignmentViewerCellRenderer( viewer ) );
    viewer.addPropertyChangeListener( this );
    
    // need the main scroll pane. 
    scrollPane = new JChangedScrollPane
     ( new InvertedScrollPaneLayout(),
       viewer, 
       JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
    
    // enable the performance enhancements. 
    enableScrollPanePerformance( scrollPane.getViewport() );
    
    // this ensures that the default ruler fits the screen
    rulerPanel = new JPanel();
    rulerPanel.setLayout( new BorderLayout() );
    rulerPanel.setBorder( new MatteBorder( 5, 0, 5, 0, getBackground() ) );
    
    //  install the ruler. 
    ruler = new JAlignmentRuler();
    
    // install the column header 
    rowHeaders = new EditorAlignmentButtonPanel();
    
    // this bit ensures that the rowHeader takes up only its preferred
    // size, and it not resized bigger than that. BorderLayout always
    // grows the CENTER component to take up excess, even when there
    // is not CENTER component!
    rowHeadersPanel = new JPanel();
    rowHeadersPanel.setLayout( new BorderLayout() );
    rowHeadersPanel.add( rowHeaders, BorderLayout.NORTH );
    scrollPane.setRowHeaderView( rowHeadersPanel );
    rowHeaders.setBackground( viewer.getBackground() );
    
    
    scrollPane.setColumnHeaderView( rulerPanel );
    rulerPanel.add( ruler, BorderLayout.WEST );

    setJMenuBar( new JMenuBar() );
    
    getContentPane().add( mainPanel );
    mainPanel.add( scrollPane, BorderLayout.CENTER );

    if( initialCellSize != null ){
      int width = (int)initialCellSize.getWidth();
      int height = (int)initialCellSize.getHeight();
      
      viewer.setCellWidth ( width );
      viewer.setCellHeight( height );
    }
    
    rowHeaders.setFixedCellHeight( viewer.getCellHeight() );
    ruler.setPreferredWidthPerBase( viewer.getCellWidth() );
    invalidate();
  }

  public void propertyChange( PropertyChangeEvent pce )
  {
    if( pce.getPropertyName().equals( "cellHeight" ) ){
      
      int newValue = ((Integer)pce.getNewValue()).intValue();
      
      rowHeaders.setFixedCellHeight( newValue );
      rowHeaders.invalidate();      
      validate();
      repaint();
    }
    
    if( pce.getPropertyName().equals( "cellWidth" ) ){
      
      int newValue = ((Integer)pce.getNewValue()).intValue();
      
      ruler.invalidate();
      ruler.setPreferredWidthPerBase( newValue );
      validate();
      repaint();
    }
  }
  
  //getters and setters
  public void setSequenceAlignment( SequenceAlignment alignment )
  {
    viewer.setSequenceAlignment( alignment );
    ruler.setSequenceAlignment( alignment );
    rowHeaders.setSequenceAlignment( alignment );
    
    Dictionary dic = ruler.getLabelTable();
    JLabel oneLabel = (JLabel)dic.get( new Integer( 1 ) );
    oneLabel.setText( " 1" );
    ruler.setLabelTable( dic );
  }

  
  public JPanel getMainPanel()
  {
    return mainPanel;
  }
  
  public JAlignmentButtonPanel getRowHeaders()
  {
    return rowHeaders;
  }
  
  public JScrollPane getScrollPane()
  {
    return scrollPane;
  }
  
  public JPanel getRulerPanel()
  {
    return rulerPanel;
  }
  
  public JAlignmentViewer getViewer()
  {
    return viewer;
  }
  
  public JAlignmentRuler getRuler()
  {
    return ruler;
  }

  private void enableScrollPanePerformance( JViewport viewport )
  { 
    //    viewport.setBackingStoreEnabled( true );
    viewport.putClientProperty
      ( "EnableWindowBlot", Boolean.TRUE);
  }

  public void setOptions( Object opts )
  {
    super.setOptions( opts );
    
    try{
      HashMap options = (HashMap)opts;
      if( options == null ) return;
      
      Dimension dim = (Dimension)options.get( "cellSize" );
      
      // if we have completed instantiation set the size. 
      if( viewer != null && dim != null ){
        int width = (int)dim.getWidth();
        int height = (int)dim.getHeight();
        viewer.setCellWidth( (int)dim.getWidth() );
        viewer.setCellHeight( (int)dim.getHeight() );
        rowHeaders.setFixedCellHeight( height );
        ruler.setPreferredWidthPerBase( width );
      }
      // if we have not, then store it for later. 
      else{
        initialCellSize = dim;
      }
    }
    catch( ClassCastException cce ){
      // ignore. Should only occur during development or after version
      // changes. 
    }
  }
  
  public Object getOptions()
  {
    HashMap options = (HashMap)super.getOptions();
    if( viewer != null ){
      options.put( "cellSize", new Dimension( viewer.getCellWidth(),
                                              viewer.getCellHeight() ) );
    }
    return options;
  }
  
  public void dispose()
  {
    super.dispose();
    mainPanel = rulerPanel = rowHeadersPanel = null;
    viewer = null;
    scrollPane = null;
    rowHeaders = null;
    ruler = null;
  }

} // CinemaAlignmentFrame



/*
 * ChangeLog
 * $Log: CinemaAlignmentFrame.java,v $
 * Revision 1.12  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.11  2001/03/24 19:26:41  lord
 * Null check when getting options prevents crash if frame has been
 * killed by the user.
 *
 * Revision 1.10  2001/02/19 17:19:33  lord
 * Made optionable
 *
 * Revision 1.9  2000/12/05 15:54:37  lord
 * Import rationalisation
 *
 * Revision 1.8  2000/11/02 14:48:20  jns
 * o added border as a tempory messure to the ruler pane. This will
 * provide a bit of a gap between the JAV and the ruler stuff.
 *
 * Revision 1.7  2000/10/19 17:45:03  lord
 * Removed some nasty debug code
 *
 * Revision 1.6  2000/09/27 16:21:04  jns
 * o reverted back to single fast cell renderer, because of the ability
 * to generate a multiplexer cell renderer that will render multiple cell
 * renderers at one time.
 *
 * Revision 1.5  2000/09/18 17:54:00  jns
 * o made to use the new multiplexer renderer rather than the default
 *
 * Revision 1.4  2000/09/15 17:32:28  lord
 * Now destroyable.
 * Most of the stuff in this function was added paranoically when I was
 * trying to hunt a memory leak. It would probably GC correctly
 * anyway. Still they do not hurt.
 *
 * Revision 1.3  2000/08/01 17:10:37  lord
 * Now listens to height and width changes, and updates row headers and
 * ruler appropriately
 *
 * Revision 1.2  2000/07/18 11:06:42  lord
 * Import rationalisation
 *
 * Revision 1.1  2000/06/27 15:56:36  lord
 * Initial checkin
 *
 */
