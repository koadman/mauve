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

package uk.ac.man.bioinf.apps.cinema.group; // Package name inserted by JPack
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import uk.ac.man.bioinf.gui.color.ColorGenerator;
import uk.ac.man.bioinf.sequence.alignment.GappedSequence;


/**
 * CinemaGroupManager.java
 *
 *
 * Created: Mon May 22 17:36:04 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaGroupManager.java,v 1.9 2001/04/11 17:04:42 lord Exp $
 */

public class CinemaGroupManager
{
  public static final String groupsName = "groups";
  public static final String groupsOrder = "order";

  private CinemaGroupModule module;
  // store the groups
  private List groups = new LinkedList();
  private int nextName = 0;
  private CinemaGroup[] allCache;
  private boolean allCacheCurrent = false;
  private ColorGenerator colourGen = ColorGenerator.getLightColorGenerator();

  public CinemaGroupManager( CinemaGroupModule module )
  {
    this.module = module;
  }
  
  public CinemaGroup[] getAllGroups()
  {
    if( !allCacheCurrent ){
      Object[] retnObj = groups.toArray();
      allCache = new CinemaGroup[ retnObj.length ];
      System.arraycopy( retnObj, 0, allCache, 0, retnObj.length );
      allCacheCurrent = true;
    }
    return allCache;
  }
  
  /**
   * Returns group with name or null
   */
  public CinemaGroup getGroupByName( String name )
  {
    // perhaps need a hash table implementation in here somewhere
    Iterator iter = groups.iterator();
    
    while( iter.hasNext() ){
      CinemaGroup current;
      if( ( current = (CinemaGroup)iter.next()).getTitle().equals( name ) ){
	return current;
      }
    }
    
    return null;
  }
  
  
  public CinemaGroup createNewGroup( String name, Color colour )
  { 
    // store the old for the event change
    CinemaGroup[] old = getAllGroups();
    
    // create the group
    CinemaGroup retn = new CinemaGroup( this, module.getSequenceAlignment(), name );
    retn.setColor( colour );
    groups.add( retn );
    
    allCacheCurrent = false;

    module.getAnchorManager().anchorGroup( retn );
    firePropertyChange( groupsName, old, getAllGroups() );
    return retn;
  }
  
  public CinemaGroup createNewGroup( String name )
  {
    return createNewGroup( name, colourGen.next() );
  }
  
  public CinemaGroup createNewGroup()
  {
     return createNewGroup( "" + (nextName++) );
   }
  
  public CinemaGroup getGroupContaining( GappedSequence seq )
  {
     Iterator iter = groups.iterator();
     while( iter.hasNext() ){
       CinemaGroup retn = (CinemaGroup)iter.next(); 
       if( retn.containsSequence( seq ) ) return retn;
     }
     return null;
   }
  
  public void removeGroup( CinemaGroup group )
  {
    if( groups.contains( group ) ){
      CinemaGroup[] old = getAllGroups();
      groups.remove( group );
      allCacheCurrent = false;
      firePropertyChange( groupsName, old, getAllGroups() );
    }
  }
  
  public void swapGroupOrder( int a, int b )
  {
    CinemaGroup[] old = getAllGroups();
    
    int max = Math.max( a, b );
    int min = Math.min( a, b );

    // this lots just swaps the stuff over
    Object upper = groups.remove( max );
    Object lower = groups.get( min );
    groups.add( max, lower );
    groups.remove( min );
    groups.add( min, upper );
    allCacheCurrent = false;

    firePropertyChange( groupsOrder, old, getAllGroups() );
  }
  
  public void clearGroups()
  {
    CinemaGroup[] old = getAllGroups();
    groups.clear();
    firePropertyChange( groupsName, old, null );
  }
  
  public String getVersion()
  {
    return "$Id: CinemaGroupManager.java,v 1.9 2001/04/11 17:04:42 lord Exp $";
  }
  
  // property change support
  // do lazy instantiation. I don't really want to increase the over
  // head of this class
  private PropertyChangeSupport supp;
  public synchronized void addPropertyChangeListener( PropertyChangeListener listener )
  {
    if( supp == null ){
      supp = new PropertyChangeSupport( this );
    }
    supp.addPropertyChangeListener( listener );
  }

  public synchronized void removePropertyChangeListener( PropertyChangeListener listener ) 
  {
    if( supp == null ){
      supp = new PropertyChangeSupport( this );
    }
    supp.removePropertyChangeListener( listener );
  }
  
  public synchronized void addPropertyChangeListener( String propertyName, PropertyChangeListener listener )
  {
    if( supp == null ){
      supp = new PropertyChangeSupport( this );
    }
    supp.addPropertyChangeListener( propertyName, listener );
  }
  
  public synchronized void removePropertyChangeListener
    ( String propertyName, PropertyChangeListener listener) 
  {
    if( supp == null ){
      supp = new PropertyChangeSupport( this );
    }
    supp.addPropertyChangeListener( propertyName, listener );
  }
  
  protected synchronized void firePropertyChange( String propertyName, Object oldValue, Object newValue )
  {
    if( supp == null ){
      supp = new PropertyChangeSupport( this );
    }
    supp.firePropertyChange( propertyName, oldValue, newValue );
  }
  
  protected synchronized void firePropertyChange( String propertyName, int oldValue, int newValue )
  {
    if( supp == null ){
      supp = new PropertyChangeSupport( this );
    }
    supp.firePropertyChange( propertyName, oldValue, newValue );
  }
  
  protected synchronized void firePropertyChange( PropertyChangeEvent event )
  {
    if( supp == null ){
      supp = new PropertyChangeSupport( this );
    }
    supp.firePropertyChange( event );
  }
  
  public synchronized boolean hasListeners( String propertyName )
  {
    if( supp == null ){
      supp = new PropertyChangeSupport( this );
    }
    return supp.hasListeners( propertyName );
  }
} // CinemaGroupManager



/*
 * ChangeLog
 * $Log: CinemaGroupManager.java,v $
 * Revision 1.9  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.8  2001/03/12 16:44:57  lord
 * Added support for anchoring
 *
 * Revision 1.7  2000/10/19 17:43:17  lord
 * Lots of small changes, to extend the functionality.
 *
 * Revision 1.6  2000/10/11 15:41:36  lord
 * Support for sorting of groups
 *
 * Revision 1.5  2000/07/18 10:41:06  lord
 * Added some convenience methods
 *
 * Revision 1.4  2000/06/13 11:17:18  lord
 * Changes to reflect alterations in CinemaGroup
 *
 * Revision 1.3  2000/06/05 14:14:31  lord
 * Substantial rewrite of class
 *
 * Revision 1.2  2000/05/30 16:15:27  lord
 * Added getGroupContaining method
 *
 * Revision 1.1  2000/05/24 15:42:16  lord
 * Initial checkin
 *
 */
