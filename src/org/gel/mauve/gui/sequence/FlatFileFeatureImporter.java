package org.gel.mauve.gui.sequence;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.biojava.bio.SimpleAnnotation;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.RangeLocation;
import org.gel.mauve.Chromosome;
import org.gel.mauve.Genome;
import org.gel.mauve.gui.GenomeCellRenderer;
import org.gel.mauve.gui.MauveFrame;

public class FlatFileFeatureImporter extends JFrame implements ActionListener, Comparator {

	/**
	 * necessary class gui variable
	 */
	protected JFileChooser chooser;
	protected JComboBox genome_choices;
	
	/**
	 * strings representing required information for general flat file feature type
	 */
	public final static String TYPE_STRING = "type";
	public final static String LABEL_STRING = "label";
	public final static String CONTIG_STRING = "contig";
	public final static String STRAND_STRING = "strand";
	public final static String LEFT_STRING = "left_end";
	public final static String RIGHT_STRING = "right_end";
	
	public final static String FORWARD = "forward";
	
	/**
	 * integers that programmatically represent the required information in the file
	 */
	public final static int TYPE = 0;
	public final static int LABEL = 1;
	public final static int CONTIG = 2;
	public final static int STRAND = 3;
	public final static int LEFT = 4;
	public final static int RIGHT = 5;
	
	/**
	 * Array that converts String to numeric representation of required fields in flat file
	 */
	public final static String [] REQ_INFO = {TYPE_STRING, LABEL_STRING, CONTIG_STRING,
				STRAND_STRING, LEFT_STRING, RIGHT_STRING};
	
	/**
	 * Vector of required annotations for general flat file feature type-- can be added to
	 * for further specificity in subclasses
	 */
	public Vector req_fields;
	
	/**
	 * parent MauveFrame
	 */
	protected MauveFrame mauve;
	
	/**
	 * creates a FeatureImporter associated with the specified mauve frame
	 * 
	 * @param mauve_frame
	 */
	public FlatFileFeatureImporter (MauveFrame mauve_frame) {
		super ("Import Annotation File");
		req_fields = new Vector ();
		req_fields.add (LABEL_STRING);
		mauve = mauve_frame;
		initGUI ();
	}
	
	/**
	 * initializes gui components
	 *
	 */
	protected void initGUI () {
		JPanel pane = new JPanel (new BorderLayout (0, 0));
		chooser = new JFileChooser ();
		chooser.setApproveButtonText ("Import");
		chooser.setApproveButtonToolTipText ("Import file");
		chooser.addActionListener (this);
		pane.add (chooser);
		genome_choices = new JComboBox (mauve.getModel ().getGenomes ());
		genome_choices.setRenderer (GenomeCellRenderer.getListCellRenderer ());
		JPanel north = new JPanel (new FlowLayout (FlowLayout.LEFT, 0, 0));
		north.add (new JLabel ("Genome to which annotations belong:  "));
		north.add (genome_choices);
		north.setBorder (chooser.getBorder ());
		pane.add (north, BorderLayout.NORTH);
		getContentPane ().add (pane);
		pack ();
	}
	
	public static int getIndexOfField (String field) {
		for (int i = 0; i < REQ_INFO.length; i++) {
			if (REQ_INFO [i].equals(field))
				return i;
		}
		return -1;
	}
	
	/**
	 * Tries to load new annotations from a file
	 * 
	 * @param file			The file containing the new annotation information
	 * @param genome		The genome to which the annotations belong
	 */
	public void importAnnotationFile (File file, Genome genome) {
		try {
			BufferedReader in = new BufferedReader (new FileReader (file));
			StringTokenizer toke = new StringTokenizer (in.readLine().trim(), "\t");
			String [] fields = new String [toke.countTokens ()];
			String [] vals = new String [fields.length];
			int [] req = new int [REQ_INFO.length];
			String s = null;
			for (int i = 0; i < fields.length; i++) {
				s = toke.nextToken();
				int ind = getIndexOfField (s);
				if (ind == -1)
					fields [i] = s;
				else
					req [ind] = i;
			}
			s = in.readLine ();
			StrandedFeature.Template template = new StrandedFeature.Template ();
			List list = new ArrayList (genome.getChromosomes ());
			String [] contig_list = new String [list.size ()];
			Collections.sort (list, this);
			for (int i = 0; i < list.size (); i++)
				contig_list [i] = ((Chromosome) list.get (i)).getName ().toLowerCase ();
			while (s != null) {
				toke = new StringTokenizer (s, "\t");
				if (toke.countTokens () != vals.length)
					System.out.println ("did not process feature: " + s);
				else {
					for (int i = 0; i < vals.length; i++)
						vals [i] = toke.nextToken ();
					int ind = Arrays.binarySearch (contig_list, vals [req [CONTIG]].toLowerCase ());
					int left = Integer.parseInt (vals [req [LEFT]]);
					int right = Integer.parseInt (vals [req [RIGHT]]);
					if (ind > -1) {
						int shift = (int) ((Chromosome) list.get (ind)).getStart ();
						left += shift;
						right += shift;
					}
					if (vals [req [STRAND]].toLowerCase ().equals (FORWARD))
						template.strand = StrandedFeature.POSITIVE;
					else
						template.strand = StrandedFeature.NEGATIVE;
					template.location = new RangeLocation (left, right);
					template.type = vals [req [TYPE]];
					Hashtable anno = new Hashtable ();
					anno.put (LABEL_STRING, vals [req [LABEL]]);
					for (int i = 0; i < fields.length; i++) {
						if (fields [i] != null)
							anno.put (fields [i], vals [i]);
					}
					template.annotation = new SimpleAnnotation (anno);
					genome.getAnnotationSequence ().createFeature (template);
				}
				s = in.readLine ();
			}
			in.close ();
			/*
			 * 	        renderer = new RectangularBeadRenderer(10.0, 10.0, Color.BLACK, Color.ORANGE, new BasicStroke());
            renderer.setHeightScaling(false);
	        specs[8] = new FilterCacheSpec(new FeatureFilter.And(new FeatureFilter.ByType("operon"),new FeatureFilter.StrandFilter(StrandedFeature.NEGATIVE)), new String[] { "note", "asap_featureid" }, renderer);

	        renderer = new RectangularBeadRenderer(10.0, 10.0, Color.BLACK, Color.ORANGE, new BasicStroke());
            renderer.setHeightScaling(false);
	        specs[9] = new FilterCacheSpec(new FeatureFilter.And(new FeatureFilter.ByType("operon"),new FeatureFilter.StrandFilter(StrandedFeature.POSITIVE)), new String[] {  "note", "asap_featureid" }, renderer);

			 */
			System.out.println ("we think it's there");
		}
		catch (Exception e) {
			e.printStackTrace ();
		}
	}
	
	/**
	 * called when a user decides to import a annotations from a flat file
	 */
	public void actionPerformed (ActionEvent e) {
		if (e.getActionCommand ().equals (JFileChooser.APPROVE_SELECTION) && 
				chooser.getSelectedFile () != null)
			importAnnotationFile (chooser.getSelectedFile (),
					(Genome) genome_choices.getSelectedItem ());
		setVisible (false);
	}

	public int compare (Object arg0, Object arg1) {
		return ((Chromosome) (arg0)).getName ().toLowerCase ().compareTo (((Chromosome) (arg1)).getName ().toLowerCase ());
	}

}
