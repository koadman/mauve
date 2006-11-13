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

package uk.ac.man.bioinf.apps.cinema.resources; // Package name inserted by JPack
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * CinemaResources.java
 *
 * The purpose of this class is to provide access to the resources
 * that cinema needs in a manner which is appropriate and simple both
 * during development time and after deployment. 
 *
 *
 * Created: Mon May 29 16:44:01 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaResources.java,v 1.9 2001/05/04 12:30:33 lord Exp $
 */

public class CinemaResources 
{

  public static URL getResource( String name )
  {
    try{
      if( fetchCinemaDirectoryString() != null ){
        return new File( fetchCinemaDirectoryString() + name ).toURL();
      }
      else{
        return CinemaResources.class.getResource( name );
      }
    }
    catch( MalformedURLException mfu ){
    }
    return null;
  }
  
  /*
   * Defines the way in which XML load names are translated into
   * actual XML files. The way this works at the moment is as
   * follows. 
   * 
   * First the user config directory is searched for a file with the
   * name of the parameter. This gives the user the ability to
   * override any of the config provided for by Cinema. The user
   * config directory probably needs to be configurable, but for the
   * moment I have hard coded it as ~/.cinema/config. To ensure that
   * this overriding does not occur by chance, all the config files
   * specified by Cinema itself will be named with the cinema-*
   * prefix. 
   *
   * After this the
   * cinema application config is searched. This happens in one of two
   * ways, which enable Cinema to access configuration either as a jar
   * file or during development. If the command line parameter
   * -Dcinema.dir is specified, then this is used as the
   * root. Alternatively if there is not specified an attempt is made
   * to retrieve the XML file as Class resource. 
   */
  private static File userConfig = new File( System.getProperty( "user.home" ) +
                                             System.getProperty( "file.separator" ) +
                                             ".cinema" +
                                             System.getProperty( "file.separator" ) +
					     "config" );
  
  private static File userExtensions = new File( System.getProperty( "user.home" ) +
                                                 System.getProperty( "file.separator" ) +
                                                 ".cinema" +
                                                 System.getProperty( "file.separator" ) +
                                                 "ext" );
  
  private static File userPersist = new File( System.getProperty( "user.home" ) +
                                              System.getProperty( "file.separator" ) +
                                              ".cinema" +
                                              System.getProperty( "file.separator" ) +
					      "persist" +
                                              System.getProperty( "file.separator" ) +
					      "persist.ser" );
 
  public static File getUserExtensions()
  {
    return userExtensions;
  }
  
  public static File getUserPersist()
  {
    userPersist.getParentFile().mkdirs();
    return userPersist;
  }
  
  public static InputStream resolveXMLLoadName( String loadName ) throws IOException
  {
    File loadFile = new File( userConfig, loadName );

    if( loadFile.exists() ){
      return new FileInputStream( loadFile );
    }
    
    if( fetchCinemaDirectoryString() != null ){
      return new FileInputStream( fetchCinemaDirectoryString() + loadName );
    }
    
    InputStream stream = CinemaResources.class.getResourceAsStream( loadName );
    
    if( stream == null ) throw new FileNotFoundException( "No load file found by the name " + loadName );
      
    return stream;
  }

  public static InputStream getModuleDTDAsResource()
  {
    return CinemaResources.class.getResourceAsStream( "module.dtd" );
  }
  
  public static String fetchModuleSystemIdentifier()
  {
    try{
      return new URL( "file", null, fetchCinemaDirectoryString() + "module.dtd" ).toString();
    }
    catch( MalformedURLException mue ){
      mue.printStackTrace();
    }
    return null;
  }
  
  public static String fetchCinemaDirectoryString()
  {
    if( System.getProperty( "cinema.dir" ) == null ){
      return null;
    }
    else{
      return System.getProperty( "cinema.dir" );
    }
  }
} // CinemaResources

/*
 * ChangeLog
 * $Log: CinemaResources.java,v $
 * Revision 1.9  2001/05/04 12:30:33  lord
 * Cosmetic changes
 *
 * Revision 1.8  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.7  2001/02/15 18:11:21  lord
 * Added user extensions directory
 *
 * Revision 1.6  2001/01/31 17:45:40  lord
 * Added user persist file.
 *
 * Revision 1.5  2001/01/26 17:06:32  lord
 * Changed XML name resolution so that I can load from the .cinema directory
 *
 * Revision 1.4  2000/12/18 12:10:30  jns
 * o getting rid of system.out.println to avoid noisy output out of debug
 * mode
 *
 * Revision 1.3  2000/12/13 16:30:42  lord
 * Added URL resource method
 *
 * Revision 1.2  2000/09/25 16:35:34  lord
 * Changes made so that the XMLParser used is no longer hard coded
 * but comes from a factory. This allows for instance giving the parser a
 * custom entity resolver.
 *
 * Revision 1.1  2000/08/03 16:39:54  lord
 * CinemaResources has been moved here from the .cinema package
 * because this way it can access resources directly through the
 * class getResource methods
 *
 * Revision 1.2  2000/06/27 13:38:01  lord
 * Cosmetic changes
 *
 * Revision 1.1  2000/05/30 16:05:54  lord
 * Initial checkin
 *
 */
