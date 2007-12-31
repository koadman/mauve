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
import java.awt.Color;

import uk.ac.man.bioinf.analysis.consensus.ConsensusSequence;
import uk.ac.man.bioinf.apps.cinema.CinemaModule;
import uk.ac.man.bioinf.gui.color.ColorMap;
import uk.ac.man.bioinf.gui.color.IndividualElementColorMap;
import uk.ac.man.bioinf.gui.viewer.JAlignmentViewer;
import uk.ac.man.bioinf.sequence.Element;
import uk.ac.man.bioinf.sequence.alignment.Gap;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;
import uk.ac.man.bioinf.sequence.types.AminoAcid;


/**
 * AminoAcidColorMapMetaData.java
 *
 *
 * Created: Wed Jul 19 14:31:39 2000
 *
 * @author Phillip Lord
 * @version $Id: AminoAcidColorMapMetaData.java,v 1.4 2001/07/06 11:44:50 lord Exp $
 */

public class AminoAcidColorMapMetaData implements ColorMapMetaData
{
  // we only need one!

  private static final IndividualElementColorMap map;
  public static final String NAME = "Amino Acid Color Scheme";
  
  static
  {
    // color mapping
    AminoAcid[] aa = AminoAcid.getAll();
    Color[] colors = new Color[aa.length + 1];
    for (int i = 0; i < aa.length; i++) {
      if (aa[i] == AminoAcid.GLYCINE)
	colors[i] = new Color(255, 170, 136);
      else if (aa[i] == AminoAcid.ALANINE)
	colors[i] = new Color(255, 255, 255);
      else if (aa[i] == AminoAcid.VALINE)
	colors[i] = new Color(255, 255, 255);
      else if (aa[i] == AminoAcid.LEUCINE)
	colors[i] = new Color(255, 255, 255);
      else if (aa[i] == AminoAcid.ISOLEUCINE)
	colors[i] = new Color(255, 255, 255);
      else if (aa[i] == AminoAcid.SERINE)
	colors[i] = new Color(136, 255, 136);
      else if (aa[i] == AminoAcid.CYSTEINE)
	colors[i] = new Color(255, 255, 136);
      else if (aa[i] == AminoAcid.THREONINE)
	colors[i] = new Color(136, 255, 136);
      else if (aa[i] == AminoAcid.METHIONINE)
	colors[i] = new Color(255, 255, 255);
      else if (aa[i] == AminoAcid.PHENYLALANINE)
	colors[i] = new Color(255, 136, 255);
      else if (aa[i] == AminoAcid.TYROSINE)
	colors[i] = new Color(255, 136, 255);
      else if (aa[i] == AminoAcid.TRYPTOPHAN)
	colors[i] = new Color(255, 136, 255);
      else if (aa[i] == AminoAcid.PROLINE)
	colors[i] = new Color(255, 170, 136);
      else if (aa[i] == AminoAcid.HISTIDINE)
	colors[i] = new Color(136, 255, 255);
      else if (aa[i] == AminoAcid.LYSINE)
	colors[i] = new Color(136, 255, 255);
      else if (aa[i] == AminoAcid.ARGININE)
	colors[i] = new Color(136, 255, 255);
      else if (aa[i] == AminoAcid.ASPARTICACID)
	colors[i] = new Color(255, 136, 136);
      else if (aa[i] == AminoAcid.GLUTAMICACID)
	colors[i] = new Color(255, 136, 136);
      else if (aa[i] == AminoAcid.ASPARAGINE)
	colors[i] = new Color(136, 255, 136);
      else if (aa[i] == AminoAcid.GLUTAMINE)
	colors[i] = new Color(136, 255, 136);
      else
	colors[i] = new Color(102, 102, 102);
    }
    colors[ aa.length ] = Color.white;
    
    
    Element[] elem = new Element[ aa.length + 1 ];
    System.arraycopy( aa, 0, elem, 0, aa.length );
    elem[ aa.length ] = Gap.gap;
    
    map = new IndividualElementColorMap( NAME, elem, colors);
  }
  
  public void setModule( CinemaModule module )
  {
    // nothing required
  }
  
  public String getColorMapName()
  {
    return NAME;
  }
    
  public ColorMap getInstance( SequenceAlignment alignment, JAlignmentViewer viewer )
  {
    return map;
  }
  
  public ColorMap getConsensusInstance( ConsensusSequence sequence, JAlignmentViewer viewer )
  {
    return map;
  }
} // AminoAcidColorMapMetaData



/*
 * ChangeLog
 * $Log: AminoAcidColorMapMetaData.java,v $
 * Revision 1.4  2001/07/06 11:44:50  lord
 * Cosmetic
 *
 * Revision 1.3  2001/04/11 17:04:41  lord
 * Added License agreements to all code
 *
 * Revision 1.2  2000/12/05 15:10:03  lord
 * Updated due to change in MapMetaData interface.
 *
 * Revision 1.1  2000/07/26 13:29:59  lord
 * Initial checkin
 *
 */
