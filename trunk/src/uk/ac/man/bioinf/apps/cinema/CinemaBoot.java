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

package uk.ac.man.bioinf.apps.cinema; // Package name inserted by JPack
import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.SAXParseException;

import uk.ac.man.bioinf.apps.cinema.resources.CinemaResources;
import uk.ac.man.bioinf.apps.xml.XMLBootModule;
import uk.ac.man.bioinf.module.ModuleException;


/**
 * CinemaBoot.java
 *
 *
 * Created: Mon May 29 17:58:12 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaBoot.java,v 1.8 2001/04/11 17:04:41 lord Exp $
 */

public class CinemaBoot extends XMLBootModule
{
  public void load() throws ModuleException
  {
    try{
      super.load();
    }
    catch( ModuleException me ){
      System.out.println( "Exception in Cinema Boot" );
      System.out.println( me );
      me.printStackTrace();
      
      Throwable thrw = me.getThrowable();
      
      // we need to test for a SAXException, as this will have
      // swallowed the original exception resulting in a useless
      // stack trace.
      if( thrw instanceof SAXParseException ){
        System.out.println( "Problem parsing config files: " + thrw.getMessage() );
        SAXParseException spe = (SAXParseException)thrw;
        System.out.println( "In " + spe.getSystemId() + " at " + spe.getLineNumber() + ":" + spe.getColumnNumber() );
      }
      else{
        me.getThrowable().printStackTrace();
      }
      throw me;
    }
  }
  
  public String fetchMainBootName()
  {
    return "cinema-main.xml";
  }
  
  public String fetchModuleSystemIdentifier()
  {
    return CinemaResources.fetchModuleSystemIdentifier();
  }
  
  public InputStream resolveXMLLoadName( String loadName ) throws IOException
  {
    return CinemaResources.resolveXMLLoadName( loadName );
  }

  protected String getXMLParserClass()
  {
    return CinemaXMLParserFactory.class.getName();
  }
} // CinemaBoot



/*
 * ChangeLog
 * $Log: CinemaBoot.java,v $
 * Revision 1.8  2001/04/11 17:04:41  lord
 * Added License agreements to all code
 *
 * Revision 1.7  2001/01/26 17:08:04  lord
 * Improved exception handling
 *
 * Revision 1.6  2001/01/15 18:49:58  lord
 * Improved exception handling
 *
 * Revision 1.5  2000/10/19 17:34:56  lord
 * Import rationalisation
 *
 * Revision 1.4  2000/09/25 16:35:34  lord
 * Changes made so that the XMLParser used is no longer hard coded
 * but comes from a factory. This allows for instance giving the parser a
 * custom entity resolver.
 *
 * Revision 1.3  2000/08/03 16:38:24  lord
 * Modifications to enable Cinema to run from jar file
 *
 * Revision 1.2  2000/07/18 10:37:08  lord
 * Import rationalisation
 *
 * Revision 1.1  2000/05/30 16:05:54  lord
 * Initial checkin
 *
 */
