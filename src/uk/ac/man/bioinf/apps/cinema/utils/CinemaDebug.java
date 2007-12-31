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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import uk.ac.man.bioinf.apps.invoker.Invoker;
import uk.ac.man.bioinf.debug.AbstractDebug;
import uk.ac.man.bioinf.debug.Debug;
import uk.ac.man.bioinf.debug.DebugInterface;
import uk.ac.man.bioinf.debug.DebugMultiplexer;
import uk.ac.man.bioinf.debug.DefaultDebug;
import uk.ac.man.bioinf.debug.DefaultFileDebug;
import uk.ac.man.bioinf.module.Module;
import uk.ac.man.bioinf.module.ModuleIdentifierList;


/**
 * CinemaDebug.java
 *
 * This class provides the Debugging architecture implementation for
 * Cinema. It provides a Console GUI, files logs and oh so much more...
 *
 * Eventually the initial state of this should result from the XML
 * initiation, but at the moment it just does lots of things at once. 
 *
 * Created: Sun May  7 18:36:44 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaDebug.java,v 1.4 2001/04/11 17:04:42 lord Exp $
 */

public class CinemaDebug extends Module implements ActionListener
{
  private DebugImpl dImpl = new DebugImpl();
  private DebugInterface standardOutInterface, fileOutInterface;
  
  private JFrame frame;
  private JTextArea area;
  private JMenuItem standardOutOn, standardOutOff, dumpToStandardOut;
  private JMenuItem fileOutOn, fileOutOff, dumpToFile;
  
  public void load()
  {
    Debug.message( this, "Cinema Debug: Load" );
    
    // do the gui. Eventually this should be done only if the config
    // information says to....
    frame = new JFrame( "Cinema Debug Console" );
    // basic console
    area = new JTextArea();
    JScrollPane scroll = new JScrollPane( area );
    frame.getContentPane().add( scroll );
    
    // menu bar
    JMenuBar mb = new JMenuBar();
    frame.setJMenuBar( mb );
    JMenu options = new JMenu( "Options" );
    mb.add( options );
    
    // standard out tracing
    ButtonGroup standardOut = new ButtonGroup();
    standardOutOn = new JRadioButtonMenuItem( "Trace to stout" );
    standardOutOn.addActionListener( this );
    standardOut.add( standardOutOn );
    options.add( standardOutOn );
    standardOutOff = new JRadioButtonMenuItem( "No trace to stout" );
    standardOutOff.addActionListener( this );
    standardOut.add( standardOutOff );
    standardOut.setSelected( standardOutOff.getModel(), true );
    options.add( standardOutOff );
    
    // standard out dump
    dumpToStandardOut = new JMenuItem( "Dump contents to stout" );
    dumpToStandardOut.addActionListener( this );
    options.add( dumpToStandardOut );
    options.addSeparator();
    
    // file dumping gui controls
    ButtonGroup fileOut = new ButtonGroup();
    fileOutOn = new JRadioButtonMenuItem( "Trace to file" );
    fileOutOn.addActionListener( this );
    fileOut.add( fileOutOn );
    options.add( fileOutOn );
    fileOutOff = new JRadioButtonMenuItem( "No trace to file" );
    fileOutOff.addActionListener( this );
    fileOut.add( fileOutOff );
    fileOut.setSelected( fileOutOff.getModel(), true );
    options.add( fileOutOff );
    
    // file out dump
    dumpToFile = new JMenuItem( "Dump contents to file" );
    dumpToFile.addActionListener( this );
    options.add( dumpToFile );
  }

  public void start()
  {
    Debug.message( this, "Cinema Debug: Start" );
    frame.setSize( 200, 400 );
    frame.setVisible( true );
    
    // install into the Debug architecture
    DebugMultiplexer.addDebugInstance
      ( dImpl );
    DebugMultiplexer.addDebugInstance
      ( standardOutInterface = new DefaultDebug() );
  }
  
  public void actionPerformed( ActionEvent event )
  {
    if( event.getSource() == standardOutOff && standardOutInterface != null ){
      DebugMultiplexer.removeDebugInstance( standardOutInterface );
      standardOutInterface = null;
    }
    
    if( event.getSource() == standardOutOn && standardOutInterface == null ){
      DebugMultiplexer.addDebugInstance( standardOutInterface = new DefaultDebug() );
    }
    
    if( event.getSource() == dumpToStandardOut ){
      System.out.println( "\nCinema Debug Console: Dumping to standard out\n" );    
      System.out.println( area.getText() );                                         
      System.out.println( "Cinema Debug Console: Dump to standard out complete\n" );	
    }
    
    if( event.getSource() == fileOutOn ){
      File file = selectWriteFile();
      

      try{
	if( file != null ){
	  if( fileOutInterface != null ){
	    DebugMultiplexer.removeDebugInstance( fileOutInterface );
	  }
	  if( Debug.debug ){
	    Debug.message( this, "Installing debug tracing to " + file );
	  }
	  DebugMultiplexer.addDebugInstance
	    ( fileOutInterface = new DefaultFileDebug( file ) );
	}
      }
      catch( IOException io ){
	// (PENDING:- PL) Need to have something better here, once I
	// have generic method for informing the user of problems,
	// which should probably be through the CinemaCoreView
	if( Debug.debug ){
	  Debug.throwable( this, io );
	}
      }
    }
    
    if( event.getSource() == fileOutOff && fileOutInterface != null ){
      DebugMultiplexer.removeDebugInstance( fileOutInterface );
      fileOutInterface = null;
    }
    
    if( event.getSource() == dumpToFile ){
      File file = selectWriteFile();
      try{
	if( file != null ){
	  PrintWriter write = new PrintWriter( new FileWriter( file ) );
	  write.println( area.getText() );                                         
	  write.close();
	}
      }
      catch( IOException io ){
	if( Debug.debug ){
	  Debug.throwable( this, io );
	}
      }
    }
  }

  private File selectWriteFile()
  {
    JFileChooser fileChooser = new JFileChooser();
    int option = fileChooser.showSaveDialog( frame );
    File chosenFile = fileChooser.getSelectedFile();
    
    if( option == JFileChooser.APPROVE_OPTION && chosenFile != null ){
      return chosenFile;
    }
    return null;
  }
  
  public String getVersion()
  {
    return "$version: $";
  }
  
  private void message( Class cla, String message )
  {
    //defaultDeb.message( cla, message );
    if( SwingUtilities.isEventDispatchThread() ){
      area.append
	( formMessageString( message ) );
    }else{
      SwingUtilities.invokeLater
	( new Invoker( this, formMessageString( message ) )
	  {
	    public void doRun()
	    {
	      CinemaDebug.this.area.append( (String)getParameter() );
	    }
	  });
    }
  }
  
  private String formMessageString( String message )
  {
    return "Debug Message: " + message + "\n";
  }
  
  private void throwable( Class cla, Throwable th )
  {
    //defaultDeb.throwable( cla, th );
    if( SwingUtilities.isEventDispatchThread() ){
      area.append( formStackTraceString( th ) );
    }else{
      // (PENDING:- PL) Theres a difficulty here. This should really
      // be done with the CinemaInvoker module, because that will have
      // the ExceptionHandler set. The difficult here is that the
      // ExceptionHandler will probably go through this class, and so
      // we have the first problem with which one to load first....
      SwingUtilities.invokeLater
	( new Invoker( this, formStackTraceString( th ) )
	  {
	    public void doRun()
	    {
	      CinemaDebug.this.area.append( (String)getParameter() );
	    }
	  });
    }
  }

  private String formStackTraceString( Throwable th )
  {
    StringWriter str = new StringWriter();
    th.printStackTrace( new PrintWriter( str ) );
    return 
      "Debug Throwable: " + th +
      "Debug Stack: " + str.toString() + "\n";
  }
    
  public class DebugImpl extends AbstractDebug
  {
    public void message( Class cla, String message )
    {
      CinemaDebug.this.message( cla, message );
    }
    
    public void throwable( Class cla, Throwable th )
    {
      CinemaDebug.this.throwable( cla, th );
    }
  }
  
  public ModuleIdentifierList getRequiredIdentifiers()
  {
    ModuleIdentifierList list = super.getRequiredIdentifiers();
    list.add( CinemaUtilityIdentifier.CINEMA_INVOKER );
    return list;
  }
} // CinemaDebug



/*
 * ChangeLog
 * $Log: CinemaDebug.java,v $
 * Revision 1.4  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.3  2000/05/30 16:16:57  lord
 * Import sorting, and module package completion
 *
 * Revision 1.2  2000/05/15 16:23:17  lord
 * Reflecting changes in module method names
 *
 * Revision 1.1  2000/05/08 16:22:10  lord
 * Initial checkin
 *
 */
