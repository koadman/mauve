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

package uk.ac.man.bioinf.apps.cinema.utils; // Package name inserted by Jde-Package
import java.io.IOException;

import javax.swing.JOptionPane;

import uk.ac.man.bioinf.apps.optionable.OptionableExceptionHandler;
import uk.ac.man.bioinf.apps.optionable.OptionableSourceNotFoundException;


/**
 * CinemaFileOptionableExceptionHandler.java
 *
 *
 * Created: Mon May 14 16:21:58 2001
 *
 * @author Phillip Lord
 * @version $Id: CinemaFileOptionableExceptionHandler.java,v 1.2 2001/05/15 12:24:15 lord Exp $
 */

public class CinemaFileOptionableExceptionHandler implements OptionableExceptionHandler
{
  public boolean handleLoadException( Exception exp )
  {
    Object[] options =
    { "Continue?", "Try Again?", "More Details?" };
    
    int retn = JOptionPane.showOptionDialog
      ( null, "<html>Warning:- There is some problem with restoring the settings. <p>" +
        " Do you want to ", "Load error",  JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, 
        null, options, options[ 0 ] );
    
    switch( retn )
      {
      case 0:
        return false;
      case 1:      
        return true;
      case 2:
        JOptionPane.showMessageDialog
          ( null, "<html>Error reported as:<p> " + exp.getMessage(), "Error", 
            JOptionPane.INFORMATION_MESSAGE );
        return handleLoadException( exp );
      default:
        // if this happens then something has gone badly wrong!
        return false;
      }
  }
  
  public boolean handleLoadException( IOException ioe )
  {
    return handleLoadException( (Exception)ioe );
  }
  
  public boolean handleLoadException( ClassNotFoundException exp )
  {
    // this should really only occur during development, or if the
    // system has been inappropriately installed. The best course of
    // action is just to report the error and move on. 
    Object[] options =
    { "Ok", "More details" };
    
    int retn = JOptionPane.showOptionDialog
      ( null, "Warning:- Settings can not be restored. Using default options", "Warning",
        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, 
        null, options, options[ 0 ] );
    
    if( retn == 1 ){
      JOptionPane.showMessageDialog
        ( null, "<html>Error reported as: <p>" + exp.getMessage(), "Error", 
          JOptionPane.INFORMATION_MESSAGE );
    }
    
    // don't bother trying again. 
    return false;
  }
  
  public boolean handleLoadException( OptionableSourceNotFoundException osnfe )
  {
    // we can just ignore this. It probably means this is the first
    // time that the system has been run. 
    return false;
  }
  
  public boolean handleSaveException( Exception exp )
  {
    Object[] options =
    { "Continue?", "Try Again?", "More Details?" };
    
    int retn = JOptionPane.showOptionDialog
      ( null, "<html>Warning:- There is some problem with saving the settings. <p>" +
        " Do you want to ", "Save error",  JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, 
        null, options, options[ 0 ] );
    
    switch( retn )
      {
      case 0:
        return false;
      case 1:      
        return true;
      case 2:
        JOptionPane.showMessageDialog
          ( null, "Error reported as: " + exp.getMessage(), "Error", 
            JOptionPane.INFORMATION_MESSAGE );
        return handleSaveException( exp );
      default:
        // if this happens then something has gone badly wrong!
        return false;
      }
  }
  
  public boolean handleSaveException( IOException iop )
  {
    return handleSaveException( (Exception)iop );
  }
  
} // CinemaFileOptionableExceptionHandler



/*
 * ChangeLog
 * $Log: CinemaFileOptionableExceptionHandler.java,v $
 * Revision 1.2  2001/05/15 12:24:15  lord
 * Added <html> tags to get the <p> tags to work properly.
 *
 * Revision 1.1  2001/05/14 17:08:18  lord
 * Added proper error handling to save and restore.
 *
 */
