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

import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;
import uk.ac.man.bioinf.sequence.group.GroupSequenceAlignment;
import uk.ac.man.bioinf.sequence.identifier.AbstractIdentifier;
import uk.ac.man.bioinf.sequence.identifier.Identifier;
import uk.ac.man.bioinf.sequence.identifier.NoSource;



/**
 * CinemaGroup.java
 *
 * This class is more or less entirely a wrapper around the
 * HashMap, and just switches all of the return types back to
 * something sane.
 * Created: Mon May 22 17:42:36 2000
 *
 * @author Phillip Lord
 * @version $Id: CinemaGroup.java,v 1.7 2001/04/11 17:04:42 lord Exp $
 */

public class CinemaGroup extends GroupSequenceAlignment
{
  private String title;
  private Color groupColor = Color.black;
  private CinemaGroupManager manager;
  private Identifier ident = new CinemaGroupAlignmentIdentifier();
  
  public CinemaGroup( CinemaGroupManager manager, SequenceAlignment alignment , String title )
  {
    super( alignment );
    this.manager = manager;
    this.title = title;
  }
  
  public String getTitle()
  {
    return title;
  }
  
  public void setTitle( String title )
  {
    String old = this.title;
    this.title = title;
        
    manager.firePropertyChange( new PropertyChangeEvent( this, "title", old, title ) );
  }
  
  public  Color getColor()
  {
    return groupColor;
  }
  
  public void setColor( Color groupColor )
  {
    Color old = groupColor;
    this.groupColor = groupColor;
    
    manager.firePropertyChange( new PropertyChangeEvent( this, "color", old, groupColor ) );
  }
  
  public Identifier getIdentifier()
  {
    return ident;
  }
  
  class CinemaGroupAlignmentIdentifier extends AbstractIdentifier
  {
    CinemaGroupAlignmentIdentifier()
    {
      super( new NoSource() );
    }
    
    public String getTitle()
    {
      return CinemaGroup.this.getTitle();
    }
  }
} // CinemaGroup



/*
 * ChangeLog
 * $Log: CinemaGroup.java,v $
 * Revision 1.7  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.6  2000/10/19 17:43:17  lord
 * Lots of small changes, to extend the functionality.
 *
 * Revision 1.5  2000/07/18 10:39:51  lord
 * Import rationalisation.
 *
 * Revision 1.4  2000/06/13 11:14:52  lord
 * Added some property change support
 *
 * Revision 1.3  2000/06/05 14:13:37  lord
 * Now extends SequenceGroup where most of the logic has gone to
 *
 * Revision 1.2  2000/05/30 16:14:28  lord
 * Added containsSequence method
 *
 * Revision 1.1  2000/05/24 15:42:16  lord
 * Initial checkin
 *
 */
