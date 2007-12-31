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

import java.awt.Color;
import java.util.WeakHashMap;

import uk.ac.man.bioinf.analysis.consensus.ConsensusSequence;
import uk.ac.man.bioinf.analysis.misc.PercentageIDCalculator;
import uk.ac.man.bioinf.analysis.misc.PercentageIDCalculatorManager;
import uk.ac.man.bioinf.gui.color.ColorList;
import uk.ac.man.bioinf.gui.color.ColorMap;
import uk.ac.man.bioinf.sequence.Element;
import uk.ac.man.bioinf.sequence.alignment.GappedSequence;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;
import uk.ac.man.bioinf.sequence.alignment.event.AlignmentEvent;
import uk.ac.man.bioinf.sequence.alignment.event.AlignmentEventType;
import uk.ac.man.bioinf.sequence.alignment.event.AlignmentListener;
import uk.ac.man.bioinf.sequence.event.SequenceEvent;
import uk.ac.man.bioinf.sequence.event.SequenceListener;
import uk.ac.man.bioinf.sequence.geom.SequenceAlignmentPoint;


/**
 * ConsensusPercentageIDColorMap.java
 *
 *
 * Created: Fri Jun 23 17:57:45 2000
 *
 * @author Phillip Lord
 * @version $Id: ConsensusPercentageIDColorMap.java,v 1.6 2001/04/11 17:04:42 lord Exp $
 */

public class ConsensusPercentageIDColorMap 
  implements ColorMap, SequenceListener, AlignmentListener
{
  private ColorList colors = ColorList.getHotToColdColorList();
  private WeakHashMap calcMap;
  private SequenceAlignment alignment;
  private int[][] colorList;
  
  public ConsensusPercentageIDColorMap( SequenceAlignment alignment )
  {
    this.alignment = alignment; 
    calcMap = new WeakHashMap();
    alignment.addAlignmentListener( this );
    alignment.addSequenceListener ( this );
    recalc();
  }
  
  /**
   * Recalc everything. 
   */
  private void recalc()
  {
    calcMap.clear();
    
    colorList = new int[ alignment.getNumberSequences() ][];
    
    for( int i = 1; i < alignment.getNumberSequences() + 1; i++ ){
      ConsensusSequence cons = ((ConsensusSequence)alignment.getSequenceAt( i ) );
      PercentageIDCalculator calc = PercentageIDCalculatorManager.getDefaultInstance().getCalculator
	( cons.getConsensusCalculator().getSequenceAlignment() );
      calcMap.put( cons, calc );
      
      recalc( i, calc );
    }
  }

  private void recalc( int sequenceIndex, PercentageIDCalculator calc )
  {
    double[] var = calc.getVariance();
    int[] colorAtColumn = new int[ var.length ];
    
    for( int j = 0; j < var.length; j++ ){
	colorAtColumn[ j ] = (int)(var[ j ] * (colors.length() -1));
    }
    
    colorList[ sequenceIndex - 1 ] = colorAtColumn;
  }
  
  private void recalc( int sequenceIndex )
  {
    recalc
      ( sequenceIndex, 
	(PercentageIDCalculator)calcMap.get
	( alignment.getSequenceAt( sequenceIndex ) ) );
  }
  
  private void recalc( GappedSequence seq )
  {
    recalc
      ( alignment.getSequenceIndex( seq ), 
	(PercentageIDCalculator)calcMap.get( seq ) );
  }
  
  public Color getColorAt(SequenceAlignment sa, Element elem, SequenceAlignmentPoint point)
  {
    if( elem == null ) return null;
    return colors.getColorAt( colorList[ point.getY() - 1 ][ point.getX() - 1 ] );
  }

  public String getName()
  {
    return "ConsensusPercentageIDViewer";
  }

  public void changeOccurred( SequenceEvent event )
  {
    recalc( (GappedSequence)event.getSource() );
  }

  public void changeOccurred( AlignmentEvent event )
  {
    AlignmentEventType type = event.getType();
    
    if( type == AlignmentEventType.INSET_CHANGE ){
      recalc( event.getStart() );
    }
    else{
      // If its an insert or delete just recalc everything. In most
      // cases this should not actually require recalculating the
      // variances, just the final colors so it should not be too bad
      recalc();
    }
  }
} // ConsensusPercentageIDColorMap



/*
 * ChangeLog
 * $Log: ConsensusPercentageIDColorMap.java,v $
 * Revision 1.6  2001/04/11 17:04:42  lord
 * Added License agreements to all code
 *
 * Revision 1.5  2000/12/18 12:12:36  jns
 * o getting rid of system.out.println to avoid noisy output out of debug
 * mode
 *
 * Revision 1.4  2000/11/10 15:13:30  lord
 * Major rewrite, required because the PercentageIDCalculator has totally changed.
 *
 * Revision 1.3  2000/08/01 15:05:16  lord
 * Updated due to changes in PercentageIDCalculator class
 *
 * Revision 1.2  2000/08/01 12:47:05  jns
 * o removed references to BioInterface and BioObject.
 *
 * Revision 1.1  2000/06/27 15:56:36  lord
 * Initial checkin
 *
 */
