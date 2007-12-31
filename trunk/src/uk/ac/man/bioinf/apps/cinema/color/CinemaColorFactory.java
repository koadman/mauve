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

package uk.ac.man.bioinf.apps.cinema.color; // Package name inserted by JPack
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import uk.ac.man.bioinf.apps.cinema.CinemaModule;
import uk.ac.man.bioinf.apps.cinema.core.CinemaCoreIdentifier;
import uk.ac.man.bioinf.debug.Debug;
import uk.ac.man.bioinf.gui.color.ColorMap;
import uk.ac.man.bioinf.module.ModuleIdentifierList;


/**
 * CinemaColorFactory.java
 *
 *
 * Created: Fri Feb  9 11:49:21 2001
 *
 * @author Phillip Lord
 * @version $Id: CinemaColorFactory.java,v 1.5 2002/03/08 14:49:27 lord Exp $
 */

public class CinemaColorFactory extends CinemaModule
{
  private NullColorMapMetaData nullColorMD = new NullColorMapMetaData();
  private HashMap metaDataHash = new HashMap();
  private boolean debug = true; 
  
  public void start()
  {
    // We always want to provide a no color map option, and this
    // provides it. 
    metaDataHash.put( nullColorMD.getColorMapName(), nullColorMD );
    
    // we need to pull the MetaData instances from the information
    // given in the config files. All of this stuff just instantiates
    // Object for each MetaData class and puts it into a hash for
    // latter. 
    Properties configProperties = getConfigProperties();
    
    Iterator iter = configProperties.keySet().iterator();
    while( iter.hasNext() ){
      // declare this here so that we can access it in the catch block
      String className;
      
      try{
	className = (String)iter.next();
	Class metaDataClass = Class.forName( className );
	ColorMapMetaData instance = (ColorMapMetaData)metaDataClass.newInstance();
	metaDataHash.put( instance.getColorMapName(), instance );
	instance.setModule( this );
      }
      catch( InstantiationException ie ){
	if( Debug.debug || debug )
	  Debug.both( this, "Failed to instantiation class", ie );
      }
      catch( IllegalAccessException iae ){
	if( Debug.debug || debug )
	  Debug.both( this, "Failed to access class", iae );
      }
      catch( ClassNotFoundException cnfe ){
	if( Debug.debug || debug )
	  Debug.both( this, "Failed to find class", cnfe );
      }
    }
  }
  
  public void addMetaData( ColorMapMetaData colorMap )
  {
  }
  
  /**
   * Returns a meta data object for the given ColorMap. 
   * @param map
   * @return
   */
  public ColorMapMetaData getMetaData( ColorMap map )
  {
    if( map == null ){
      return getMetaData( (String)null );
    }
    
    return getMetaData( map.getName() );
  }
  
  public ColorMapMetaData getMetaData( String name )
  { 
    if( name == null ){
      name = "null";
    }
    return (ColorMapMetaData)metaDataHash.get( name );
  }
  
  public Iterator getColorMapIterator()
  {
    return metaDataHash.values().iterator();
  }
  
  public int getSize()
  {
    return metaDataHash.size();
  }
  
  public ColorMapMetaData getDefaultColorMapMetaData()
  {
    return nullColorMD;
  }
  
  public String getVersion()
  {
    return "$Id: CinemaColorFactory.java,v 1.5 2002/03/08 14:49:27 lord Exp $";
  }
  
  public ModuleIdentifierList getRequiredIdentifiers()
  {
    ModuleIdentifierList list = super.getRequiredIdentifiers();
    list.add( CinemaCoreIdentifier.CINEMA_SYSTEM_EVENTS );
    return list;
  }
} // CinemaColorFactory



/*
 * ChangeLog
 * $Log: CinemaColorFactory.java,v $
 * Revision 1.5  2002/03/08 14:49:27  lord
 * Added local debug
 *
 * Revision 1.4  2001/05/08 17:37:38  lord
 * Cosmetic changes
 *
 * Revision 1.3  2001/04/11 17:04:41  lord
 * Added License agreements to all code
 *
 * Revision 1.2  2001/03/12 16:43:55  lord
 * Updated module requirements
 *
 * Revision 1.1  2001/02/19 16:55:08  lord
 * Have separated CinemaColorSelector into two. One factory for
 * ColorMaps, one display module.
 *
 */
