package org.gel.mauve.recombination;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.gel.mauve.Genome;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.histogram.ZoomHistogram;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/*
 * Class to read in recombination data from a run of the Weak ARG and build a data model for it
 */
public class WeakArgModelBuilder {
	public static WeakArgDataModel buildModel(File f, XmfaViewerModel xmfa) throws FileNotFoundException, IOException, SAXException
	{
		WeakArgDataModel model = null;
		
		// open the file and create an input stream
		// assume it is bzip2 compressed
		FileInputStream fis = new FileInputStream(f);
		// bzip reader expects the first two bytes skipped
		int B = fis.read(); 
		int z = fis.read();	
		if(B==66&&z==90){
			model=readWargXmlBzip2(fis, xmfa);
			// save to a cache file
			File outfile = new File(f.getAbsolutePath() + ".mauvedata");
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outfile));
			oos.writeObject(model);
			oos.close();
		}else{
			model=readStoredData(f);
		}

		// add the data to the viewer model
		for(int i=0; i < xmfa.getSequenceCount(); i++){
			xmfa.addGenomeAttribute(xmfa.getGenomeBySourceIndex(i), model.incoming[i]);
		}
		
		return model;
	}

	protected static WeakArgDataModel readWargXmlBzip2(FileInputStream fis, XmfaViewerModel xmfa) throws FileNotFoundException, IOException, SAXException
	{
		WeakArgDataModel model = new WeakArgDataModel(xmfa);
		BufferedInputStream bis = new BufferedInputStream(fis);
		CBZip2InputStream input = new CBZip2InputStream(bis);
		
		// now parse the XML
		XMLReader xmlreader = null;
		xmlreader = XMLReaderFactory.createXMLReader();
		WeakArgXmlHandler handler = new WeakArgXmlHandler(model, xmfa);
		xmlreader.setContentHandler(handler);		
		xmlreader.parse(new InputSource(input));
		
		handler.summarize();
		// now take the summaries from the xml handler and create objects
		for(int i=0; i < xmfa.getSequenceCount(); i++){
			ZoomHistogram zh = new ZoomHistogram(xmfa.getGenomeBySourceIndex(i));
			zh.setGenomeLevelData(toByteArray(handler.inEdgeTally[i]));
			model.incoming[i] = zh;

			ZoomHistogram zhout = new ZoomHistogram(xmfa.getGenomeBySourceIndex(i));
			zhout.setGenomeLevelData(toByteArray(handler.outEdgeTally[i]));
			model.outgoing[i] = zhout;
		}
		xmfa.setDrawAttributes(true);
		return model;
	}
	
	private static byte[] toByteArray(int[] data)
	{
		byte[] output=new byte[data.length];
		for(int i=0; i<data.length; i++)
			output[i]=(byte)data[i];
		return output;
	}
	
	protected static WeakArgDataModel readStoredData(File f) throws IOException
	{
		WeakArgDataModel model;
    	ObjectInputStream cache_instream = new ObjectInputStream(new FileInputStream(f));
    	try{
    		model = (WeakArgDataModel)cache_instream.readObject();
    	}catch(ClassNotFoundException cnfe)
    	{
    		throw new RuntimeException("Error reading stored data " + f);
    	}
    	return model;
	}

	/**
	 * Class to handle XML parse events from weak arg xml
	 * @author koadman
	 */
	static class WeakArgXmlHandler implements ContentHandler
	{
		WeakArgDataModel model;
		XmfaViewerModel xmfa;
		String curElement;
		String curNameMap;
		String curBlocks;
		
		int start, end, eFrom, eTo;
		double aFrom, aTo;
		int curblock = -1;

		// temporary storage for parsing
		int[][] inEdgeTally;
		int[][] outEdgeTally;		
		byte[][] regionalColors;
		long[] seq_coords;
		boolean[] gap;
		
		int iterations;

		
		
		WeakArgXmlHandler(WeakArgDataModel model, XmfaViewerModel xmfa)
		{
			this.model = model;
			this.xmfa = xmfa;
			inEdgeTally = new int[2*xmfa.getSequenceCount()-1][];
			outEdgeTally = new int[2*xmfa.getSequenceCount()-1][];
			Vector<Genome> genomes = xmfa.getGenomes();
			for(int i=0; i<xmfa.getSequenceCount(); i++){
				inEdgeTally[i] = new int[(int)(genomes.elementAt(i).getLength())];
				outEdgeTally[i] = new int[(int)(genomes.elementAt(i).getLength())];
			}
			seq_coords = new long[xmfa.getSequenceCount()];
			gap = new boolean[xmfa.getSequenceCount()];
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if(curElement==null)	return;
			try{
			if(curElement.equals("start")){				
				this.start = Integer.parseInt(new String(ch, start, length));
			}else 
			if(curElement.equals("end")){
				end = Integer.parseInt(new String(ch, start, length));
			}else 
			if(curElement.equals("efrom")){
				eFrom = Integer.parseInt(new String(ch, start, length));
			}else 
			if(curElement.equals("eto")){
				eTo = Integer.parseInt(new String(ch, start, length));
			}else 
			if(curElement.equals("afrom")){
				aFrom = Double.parseDouble(new String(ch, start, length));
			}else 
			if(curElement.equals("ato")){
				aTo = Double.parseDouble(new String(ch, start, length));
			}else 
			if(curElement.equals("Tree")){
				if(model.treeString==null)
					model.treeString = new String(ch, start, length);
				else{
//					if(!model.treeString.equals(new String(ch))){
//						throw new RuntimeException("Can not understand weak ARG data with non-constant tree");
//					}
				}
			}else if(curElement.equals("Blocks")){
				curBlocks = new String(ch, start, length);
			}else if(curElement.equals("nameMap")){
				curNameMap = new String(ch, start, length);
			}
			}catch(NumberFormatException nfe){
				System.err.println("Number format exception!");
			}
			curElement = null;
		}
		
		public void endElement(String uri, String localName, String qName) throws SAXException 
		{
			if(qName.equals("recedge"))
			{
				// finished a recedge.  record it.
				if(eTo<xmfa.getSequenceCount()){
					recordRecEdge(inEdgeTally, eTo);
				}

				if(eFrom<xmfa.getSequenceCount()){
					recordRecEdge(outEdgeTally, eFrom);
				}
			}
		}

		private void recordRecEdge(int[][] tally, int genome)
		{
			xmfa.getColumnCoordinates(curblock, start, seq_coords, gap);
			int s = (int)seq_coords[genome];
			xmfa.getColumnCoordinates(curblock, end, seq_coords, gap);
			int e = (int)seq_coords[genome];
			s = xmfa.getGenomeBySourceIndex(genome).getLength() < s ? (int)(xmfa.getGenomeBySourceIndex(genome).getLength()) : s;
			e = xmfa.getGenomeBySourceIndex(genome).getLength() < e ? (int)(xmfa.getGenomeBySourceIndex(genome).getLength()) : e;
			if(s>e){
				int tmp = s;
				s = e;
				e = tmp;
			}
			for(int i=s; i<e; i++){
				tally[genome][i]++;
			}
		}

		public void endDocument() throws SAXException {}
		public void endPrefixMapping(String prefix) throws SAXException {}
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
		public void processingInstruction(String target, String data) throws SAXException {}
		public void setDocumentLocator(Locator locator) {}
		public void skippedEntity(String name) throws SAXException {}
		public void startDocument() throws SAXException {}
		public void startPrefixMapping(String prefix, String uri) throws SAXException {}

		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException 
		{
			curElement = qName;
			if(curElement.equals("Blocks")){
				curblock++;
			}else if(curElement.equals("Iteration")){
				iterations++;
			}
		}

		/*
		 * normalizes a posterior sample to a probability distribution
		 */
		public void normalize(int[][] data){
			float norm = iterations / curblock;
			for(int i=0; i<data.length; i++)
			{
				if(data[i]==null)
					continue;
				for(int j=0; j<data[i].length; j++)
				{
					float f = data[i][j];
					f /= norm;
					f -= 0.5;
					f *= 255;
					data[i][j] = f < -127 ? -128 : (byte)f;
				}
			}
		}

		public void summarize(){
			normalize(inEdgeTally);
			normalize(outEdgeTally);
		}

	}
}
