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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.ac.man.bioinf.apps.cinema.CinemaModule;
import uk.ac.man.bioinf.apps.cinema.core.CinemaFramedActionProvider;
import uk.ac.man.bioinf.gui.optionable.OptionableJFrame;
import uk.ac.man.bioinf.gui.viewer.JAlignmentViewer;


/**
 * CinemaResizeElements.java
 *
 *
 * Created: Wed Jul 26 14:40:14 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaResizeElements.java,v 1.9 2001/07/06 12:51:58 lord Exp $
 */

public class CinemaResizeElements extends CinemaModule 
  implements CinemaFramedActionProvider
{

  public Action[] getActions( CinemaAlignmentFrame frame )
  {
    return new Action[]{
      new FramedAbstractAction( frame, "Resize Elements" )
	};
  }
  
  class FramedAbstractAction extends AbstractAction
  {
    private CinemaAlignmentFrame frame;
    private ResizerGui gui;
    
    FramedAbstractAction( CinemaAlignmentFrame frame, String title )
    {
      super( title );
      this.frame = frame;
    }
    
    public void actionPerformed( ActionEvent event )
    {
      // if there is no instance of the re-sizer, create one
      if( gui == null ){
	gui = new ResizerGui( frame );
      }
      
      // set the gui visible, if it isn't already
      if (! gui.isVisible())
	gui.setVisible( true );

      // if the gui is minimized then pop it up
      if (gui.getState() == Frame.ICONIFIED)
	gui.setState(Frame.NORMAL);
      
      // bring the gui to the front
      gui.toFront();
    }
  }
  
  public String getVersion()
  {
    return "$Id: ";
  }
  
  // this is a nasty hack. I need to work out whether I am using a JVM
  // which is suffering from the bug described in bug parade 4246117,
  // which makes the GUI malfunction. This provides a work around. The
  // GUI is not as pretty as it should be but it works. 
  private static boolean isValueAdjustingBuggyJVM;
  static 
  {
    String vendor  = System.getProperty( "java.vendor" );
    String version = System.getProperty( "java.version" );
    
    if( vendor.equals( "Sun Microsystems Inc." ) &&
	version.startsWith( "1.2.2" ) ){
      isValueAdjustingBuggyJVM = true;
    }
  }
  
  class ResizerGui extends OptionableJFrame implements ChangeListener, ActionListener
  {
    private JSlider xSizeSlider;
    private JSlider ySizeSlider;
    private JPanel elementSizeComponent;
    private JCheckBox square, aspect;
    private double aspectRatio;
    
    private CinemaAlignmentFrame alignFrame;
    
    public ResizerGui( CinemaAlignmentFrame alignFrame )
    {
      super( "utils.resizer.gui." + alignFrame.getTitle(), 
             "Set Cell Size:- " + alignFrame.getTitle(), true );
      
      this.alignFrame = alignFrame;
      
      //This is the main container panel
      JPanel elementSizeChooserPanel = new JPanel();
      elementSizeChooserPanel.setLayout( new BorderLayout() );
      
      JAlignmentViewer view = alignFrame.getViewer();
      int elementWidth  = view.getCellWidth();
      int elementHeight = view.getCellHeight();
      
      
      //Heres the slider and label set up
      JPanel sliderAndLabelPanel = new JPanel();
      sliderAndLabelPanel.setLayout( new BoxLayout( sliderAndLabelPanel, BoxLayout.Y_AXIS ) );
      
      // controls the X axis
      JLabel sliderLabel = new JLabel( "Width" );
      xSizeSlider = new JSlider( 1, 85, elementWidth );
      xSizeSlider.addChangeListener( this );
      xSizeSlider.setPaintTicks( true );
      xSizeSlider.setPaintLabels( true );
      xSizeSlider.setMajorTickSpacing( 20 );
      xSizeSlider.setMinorTickSpacing( 4 );
      sliderAndLabelPanel.add( sliderLabel );
      sliderAndLabelPanel.add( xSizeSlider );
      
      // controls the Y axis
      sliderLabel = new JLabel( "Height" );
      ySizeSlider = new JSlider( 1, 85, elementHeight );
      ySizeSlider.addChangeListener( this );
      ySizeSlider.setPaintTicks( true );
      ySizeSlider.setPaintLabels( true );
      ySizeSlider.setMajorTickSpacing( 20 );
      ySizeSlider.setMinorTickSpacing( 4 );
      sliderAndLabelPanel.add( sliderLabel );
      sliderAndLabelPanel.add( ySizeSlider );
      
      JPanel buttonPanel = new JPanel();
      // keep square. Which switch this ON by default latter in the
      // constructor. 
      square = new JCheckBox( "Keep Square" );
      
      aspect = new JCheckBox( "Maintain aspect" );
      square.addActionListener( this );
      aspect.addActionListener( this );
      buttonPanel.add( square );
      buttonPanel.add( aspect );
      sliderAndLabelPanel.add( buttonPanel );
      
      
      //This is the panel that resizes to show the size of the thumb nails
      elementSizeComponent = new JPanel();
      elementSizeComponent.setBackground( Color.black );
      
      elementSizeComponent.setSize( elementWidth, elementHeight );
      elementSizeComponent.setLocation( new Point( 0, 0 ) );
      //This panel is invisible and it within this panel that the coloured
      //size panel changes
      JPanel outerElementPanel = new JPanel();
      //we want to specify the contents here specifically
      outerElementPanel.setLayout( null );
      outerElementPanel.setPreferredSize( new Dimension( 98, 140 ) );
      outerElementPanel.add( elementSizeComponent );
      
      elementSizeChooserPanel.add( sliderAndLabelPanel, BorderLayout.CENTER );
      elementSizeChooserPanel.add( outerElementPanel, BorderLayout.EAST );
      
      // make square selected by default
      setIsSquare( true );
      
      getContentPane().add( elementSizeChooserPanel );
      
      pack();
      forceOptionable();
      setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
    }
    
    public void actionPerformed( ActionEvent ae )
    {
      if( ae.getSource() == square ){
	setIsSquare( square.isSelected() );
      }
      
      if( ae.getSource() == aspect ){
        setMaintainAspect( aspect.isSelected() );
      }
    }
    
    // implementation of javax.swing.event.ChangeListener interface
    public void stateChanged( ChangeEvent event ) 
    {
      boolean xValueIsAdjusting = true;
      xValueIsAdjusting = xSizeSlider.getModel().getValueIsAdjusting();
      boolean yValueIsAdjusting = true;
      yValueIsAdjusting = ySizeSlider.getModel().getValueIsAdjusting();
      
      // there is a bug in Sun's 1.2.2 JVM's which means that JSlider
      // always returns true for this value, at least when the slider
      // is operated by the mouse. As this is quite a performance hit
      // I have decided to make a JVM specific bug fix in this case
      if( isValueAdjustingBuggyJVM ){
	xValueIsAdjusting = yValueIsAdjusting = false;
      }
      
      if( event.getSource() == xSizeSlider ){
	setElementXSize( xSizeSlider.getModel().getValue(),
                         xValueIsAdjusting );
      }
      
      if( event.getSource() == ySizeSlider ){
	setElementYSize( ySizeSlider.getModel().getValue(),
                         yValueIsAdjusting );
      }
    }

    private void setElementXSize( int value, boolean xValueIsAdjusting )
    {
      // if we are maintaining the aspect ratio then adjust ySlider
	if( aspect.isSelected() ){
	  elementSizeComponent.setSize( value, (int)(value * aspectRatio) );
	}
	else{
	  elementSizeComponent.setSize( value, elementSizeComponent.getHeight() );
	}
      
	// if we have finished adjusting then send the adjust the viewer
	if( !xValueIsAdjusting ){
	  if( aspect.isSelected() ){
	    alignFrame.getViewer().setCellHeight( (int)(value * aspectRatio) );
	  }
	  alignFrame.getViewer().setCellWidth( value );
	}
    }
    
    private void setElementYSize( int value, boolean yValueIsAdjusting )
    {
      elementSizeComponent.setSize( elementSizeComponent.getWidth(), value );
      
      if( !yValueIsAdjusting ){
        alignFrame.getViewer().setCellHeight( value );
      }
    }
    
    private void setMaintainAspect( boolean aspect )
    {
      this.aspect.setSelected( aspect );
      if( aspect ){
        ySizeSlider.setEnabled( false );
        aspectRatio = (double)xSizeSlider.getModel().getValue() 
          / (double)ySizeSlider.getModel().getValue();
      }
      else{
        ySizeSlider.setEnabled( true );
      }
    }

    private void setIsSquare( boolean square )
    {
      this.square.setSelected( square );
      if( square ){
        // to make a square, seq y equal to x and maintain the aspect ratio
        aspect.setSelected( true );
        aspect.setEnabled( false );
        ySizeSlider.setEnabled( false );
        aspectRatio = 1;
      }
      else{
        aspect.setEnabled( true );
        ySizeSlider.setEnabled( true );
      }
    }
    
    
    public Object getOptions()
    {
      HashMap options = (HashMap)super.getOptions();
      options.put( "isAspect", new Boolean( aspect.isSelected() ) );
      options.put( "isSquare", new Boolean( square.isSelected() ) );
      return options;
    }
    
    public void setOptions( Object opts )
    {
      if( opts == null ) return;
      
      try{
        HashMap options = (HashMap)opts;
        setMaintainAspect
          ( ((Boolean)options.get( "isAspect" )).booleanValue() );
        setIsSquare
          ( ((Boolean)options.get( "isSquare" )).booleanValue() );
      }
      catch( ClassCastException cce ){
        // ignore. Should only occur during development or after
        // version changes 
      }
    }
  }
  
  public static void main( String[] args )
  {
    CinemaResizeElements c = new CinemaResizeElements();
    c.start();
  } //end main method 
} // CinemaResizeElements



/*
 * ChangeLog
 * $Log: CinemaResizeElements.java,v $
 * Revision 1.9  2001/07/06 12:51:58  lord
 * Square by default
 *
 * Revision 1.8  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.7  2001/02/19 17:21:13  lord
 * Made optionable
 *
 * Revision 1.6  2000/12/05 15:54:37  lord
 * Import rationalisation
 *
 * Revision 1.5  2000/11/02 16:10:37  jns
 * o making sure that resize gui will appear each time it is called - it
 * wasn't previously - bug fixed
 *
 * Revision 1.4  2000/10/19 17:47:52  lord
 * Serious code shuffling. Most of the code is the same but its all
 * been moved around. In particularly the GUI code is all in an inner
 * class. This has the practical effect of allowing multiple instances of
 * the Resizer.
 *
 * Revision 1.3  2000/08/22 16:25:50  lord
 * Added some nasty hack code to work aroung JSlider bug
 * specific to Sun's 1.2.2 JVM's.
 *
 * Revision 1.2  2000/08/02 14:52:51  lord
 * Now provides menu item and responds to it correctly.
 *
 * Revision 1.1  2000/08/01 17:19:38  lord
 * Intial checkin
 *
 */
