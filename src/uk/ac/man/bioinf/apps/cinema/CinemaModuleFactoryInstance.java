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
import uk.ac.man.bioinf.apps.cinema.core.CinemaCoreView;
import uk.ac.man.bioinf.apps.cinema.core.CinemaModuleCoreIdentifier;
import uk.ac.man.bioinf.module.DefaultModuleFactoryInstance;
import uk.ac.man.bioinf.module.Module;
import uk.ac.man.bioinf.module.ModuleException;
import uk.ac.man.bioinf.module.ModuleIdentifier;


/**
 * CinemaModuleFactoryInstance.java
 *
 * This class adds some reporting functions to the superclass. 
 * Created: Wed Aug  2 13:47:22 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaModuleFactoryInstance.java,v 1.4 2001/04/11 17:04:41 lord Exp $
 */

public class CinemaModuleFactoryInstance extends DefaultModuleFactoryInstance
{
  private CinemaCoreView coreView;
  
  public Module load(ModuleIdentifier identifier) throws ModuleException
  {
    // we may not have started the core gui yet. 
    if( coreView != null && coreView.isStarted() ){
      coreView.sendStatusMessage( "Loading module:- " + identifier.getModuleName() + " ... " );
    }
    
    Module retn = super.load( identifier );
    
    // if this is the core gui that we have just started then store a
    // copy of it for later use. 
    if( identifier == CinemaModuleCoreIdentifier.CINEMA_CORE_VIEW ){
      coreView = (CinemaCoreView)retn;
    }

    if( coreView != null && coreView.isStarted() ){
      coreView.sendStatusMessage( "Loading module:- " + identifier.getModuleName() + " ... Done" );
    }
    
    return retn;
  }
  
} // CinemaModuleFactoryInstance



/*
 * ChangeLog
 * $Log: CinemaModuleFactoryInstance.java,v $
 * Revision 1.4  2001/04/11 17:04:41  lord
 * Added License agreements to all code
 *
 * Revision 1.3  2000/10/19 17:36:05  lord
 * Import rationalisation
 *
 * Revision 1.2  2000/09/11 16:23:04  lord
 * New status bar system. Wipes after 5 seconds of idle now.
 *
 * Revision 1.1  2000/08/02 14:53:37  lord
 * Initial checkin
 *
 */
