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

package uk.ac.man.bioinf.apps.cinema.consensus; // Package name inserted by JPack
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import uk.ac.man.bioinf.analysis.consensus.ConsensusCalculator;
import uk.ac.man.bioinf.analysis.consensus.ConsensusSequence;
import uk.ac.man.bioinf.apps.cinema.CinemaModule;
import uk.ac.man.bioinf.apps.cinema.core.CinemaCoreIdentifier;
import uk.ac.man.bioinf.debug.Debug;
import uk.ac.man.bioinf.module.ModuleIdentifierList;


/*
 * CinemaConsensus.java
 *
 *
 * Created: Thu Jun 15 20:50:43 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaConsensus.java,v 1.13 2001/05/08 17:38:34 lord Exp $
 */

public class CinemaConsensus extends CinemaModule
{
  private HashMap metaDataHash = new HashMap();
  private CinemaConsensusCalculatorMetaData defaultMetaData;
  
  public void start()
  {
    // load the consensus calculators
    Properties configProperties = getConfigProperties();
    
    Iterator iter = configProperties.keySet().iterator();
    while( iter.hasNext() ){
      // declare this here so that we can access it in the catch block
      String className;
      
      try{
	className = (String)iter.next();
        Class metaDataClass = Class.forName( className );
	CinemaConsensusCalculatorMetaData instance = (CinemaConsensusCalculatorMetaData)
          metaDataClass.newInstance();
	instance.setModule( this );
        
        String value = configProperties.getProperty( className );
        if( (value != null) && (value.equals( "VAL:default" )) ){
          defaultMetaData = instance;
        }
        
	metaDataHash.put( instance.getConsensusCalculatorName(), instance );
      }
      catch( InstantiationException ie ){
	if( Debug.debug )
	  Debug.both( this, "Failed to instantiation class", ie );
      }
      catch( IllegalAccessException iae ){
	if( Debug.debug )
	  Debug.both( this, "Failed to access class", iae );
      }
      catch( ClassNotFoundException cnfe ){
	if( Debug.debug )
	  Debug.both( this, "Failed to find class", cnfe );
      }
    }
  }

  public Collection getAllMetaData()
  {
    return metaDataHash.values();
  }
  
  public CinemaConsensusCalculatorMetaData getDefaultMetaData()
  {
    return defaultMetaData;
  }
  
  public CinemaConsensusCalculatorMetaData getMetaData( ConsensusSequence sequence )
  {
    return getMetaData( sequence.getConsensusCalculator() );
  }
  
  public CinemaConsensusCalculatorMetaData getMetaData( ConsensusCalculator calc )
  {
    return getMetaData( calc.getCalculatorName() );
  }

  public CinemaConsensusCalculatorMetaData getMetaData( String name )
  {
    System.out.println( "Getting meta data for name " + name );
    return (CinemaConsensusCalculatorMetaData)metaDataHash.get( name );
  }
  
  public String getVersion()
  {
    return "$Id: CinemaConsensus.java,v 1.13 2001/05/08 17:38:34 lord Exp $";
  }  

  public ModuleIdentifierList getRequiredIdentifiers()
  {
    ModuleIdentifierList list = super.getRequiredIdentifiers();
    list.add( CinemaCoreIdentifier.CINEMA_SYSTEM_EVENTS );
    return list;
  }
} // CinemaConsensus



/*
 * ChangeLog
 * $Log: CinemaConsensus.java,v $
 * Revision 1.13  2001/05/08 17:38:34  lord
 * Now no longer requires consensus calculator and meta data to have
 * the same name. Does this by storing the current meta data.
 *
 * Revision 1.12  2001/04/11 17:04:41  lord
 * Added License agreements to all code
 *
 * Revision 1.11  2000/12/05 15:54:37  lord
 * Import rationalisation
 *
 * Revision 1.10  2000/11/13 18:17:45  jns
 * o removed an import statement left over from previous code - sorry
 * this is really trivial and pretty pointless
 *
 * Revision 1.9  2000/11/09 16:19:26  lord
 * Split into two. This now has nothing to do with the GUI display of the consensus's
 *
 * Revision 1.8  2000/11/08 18:31:34  lord
 * Lots of changes, mostly to allow threaded calculation.
 *
 * Revision 1.7  2000/10/31 15:52:20  lord
 * Put in a null check for ColorMaps
 *
 * Revision 1.6  2000/10/19 17:38:04  lord
 * Import rationalisation.
 *
 * Revision 1.5  2000/10/11 16:54:04  lord
 * Some debug code removed, and cosmetic changes.
 *
 * Revision 1.4  2000/09/27 16:21:42  jns
 * o reverted back to single fast cell renderer, because of the ability
 * to generate a multiplexer cell renderer that will render multiple cell
 * renderers at one time.
 *
 * Revision 1.3  2000/09/18 17:50:26  jns
 * o change of method name - setFastRenderers()
 *
 * Revision 1.2  2000/08/01 17:39:13  lord
 * Worked out how to create an array in one line
 *
 * Revision 1.1  2000/08/01 17:20:23  lord
 * Intial checkin
 *
 * Revision 1.2  2000/07/18 10:38:28  lord
 * Now uses cursor less renderer as the cursor does not have any function here
 *
 * Revision 1.1  2000/06/27 13:38:19  lord
 * Initial checkin
 *
 */
