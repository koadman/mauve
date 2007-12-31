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

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import uk.ac.man.bioinf.apps.cinema.resources.CinemaResources;
import uk.ac.man.bioinf.apps.xml.XMLParserFactory;
import uk.ac.man.bioinf.debug.Debug;

/**
 * CinemaXMLParserFactory.java
 * 
 * 
 * Created: Mon Sep 18 18:59:17 2000
 * 
 * @author Phillip Lord
 * @version $Id: CinemaXMLParserFactory.java,v 1.3 2001/04/11 17:04:41 lord Exp $
 */

public class CinemaXMLParserFactory implements XMLParserFactory
{

    public XMLReader getXMLParser()
    {
        try
        {

            XMLReader read = XMLReaderFactory
                    .createXMLReader("org.apache.xerces.parsers.SAXParser");
            read.setEntityResolver(new DefaultHandler()
            {

                public InputSource resolveEntity(String publicId, String systemId) throws SAXException
                {
                    if (systemId.equals("file:module.dtd"))
                    {
                        return new InputSource(CinemaResources.getModuleDTDAsResource());
                    }
                    InputSource is = null;
                    try{
                    	is = super.resolveEntity(publicId, systemId);
                    }catch(Exception e){
                    	e.printStackTrace();
                    }
                    return is;
                }
            });
            return read;
        } catch (Exception exp)
        {
            if (Debug.debug)
                Debug.throwable(this, exp);
        }
        return null;
    }
} // CinemaXMLParserFactory

/*
 * ChangeLog $Log: CinemaXMLParserFactory.java,v $ Revision 1.3 2001/04/11
 * 17:04:41 lord Added License agreements to all code
 * 
 * Revision 1.2 2000/12/18 12:09:23 jns o getting rid of system.out.println to
 * avoid noisy output out of debug mode
 * 
 * Revision 1.1 2000/09/25 16:35:34 lord Changes made so that the XMLParser used
 * is no longer hard coded but comes from a factory. This allows for instance
 * giving the parser a custom entity resolver.
 * 
 */
