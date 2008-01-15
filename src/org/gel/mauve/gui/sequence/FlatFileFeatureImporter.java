package org.gel.mauve.gui.sequence;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.biojava.bio.SimpleAnnotation;
import org.biojava.bio.gui.sequence.MultiLineRenderer;
import org.biojava.bio.gui.sequence.OverlayRendererWrapper;
import org.biojava.bio.gui.sequence.RectangularBeadRenderer;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.RangeLocation;
import org.biojava.utils.ChangeVetoException;
import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Chromosome;
import org.gel.mauve.FilterCacheSpec;
import org.gel.mauve.Genome;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.gui.GenomeCellRenderer;
import org.gel.mauve.gui.MauveFrame;

public class FlatFileFeatureImporter extends JFrame implements ActionListener,
		Comparator, FlatFileFeatureConstants {

	/**
	 * necessary class gui variable
	 */
	protected JFileChooser chooser;

	protected JComboBox genome_choices;

	/**
	 * Vector of required annotations for general flat file feature type-- can
	 * be added to for further specificity in subclasses
	 */
	public Vector req_fields;

	/**
	 * parent MauveFrame
	 */
	protected MauveFrame mauve;
	
	protected BaseViewerModel model;

	protected FeatureFilterer filterer;
	
	protected double [] offset;

	/**
	 * creates a FeatureImporter associated with the specified mauve frame
	 * 
	 * @param mauve_frame
	 */
	public FlatFileFeatureImporter (MauveFrame mauve_frame) {
		super ("Import Annotation File");
		mauve = mauve_frame;
		model = mauve.getModel ();
		offset = new double [model.getSequenceCount ()];
		for (int i = 0; i < offset.length; i++)
			offset [i] = MauveConstants.FEATURE_HEIGHT + 5;
		req_fields = new Vector ();
		req_fields.add (LABEL_STRING);
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
		genome_choices = new JComboBox (model.getGenomes ());
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
		for (int i = 0; i < FLAT_FEATURE_REQ_INFO.length; i++) {
			if (FLAT_FEATURE_REQ_INFO[i].equals (field))
				return i;
		}
		return -1;
	}

	/**
	 * Tries to load new annotations from a file
	 * 
	 * @param file
	 *            The file containing the new annotation information
	 * @param genome
	 *            The genome to which the annotations belong
	 */
	public void importAnnotationFile (File file, Genome genome) {
		try {
			BufferedReader in = new BufferedReader (new FileReader (file));
			StringTokenizer toke = new StringTokenizer (in.readLine ().trim (),
					"\t");
			String [] fields = new String [toke.countTokens ()];
			String [] vals = new String [fields.length];
			int [] req = new int [FLAT_FEATURE_REQ_INFO.length];
			String s = null;
			for (int i = 0; i < fields.length; i++) {
				s = toke.nextToken ();
				int ind = getIndexOfField (s);
				if (ind == -1)
					fields[i] = s;
				else
					req[ind] = i;
			}
			s = in.readLine ();
			StrandedFeature.Template template = new StrandedFeature.Template ();
			List list = new ArrayList (genome.getChromosomes ());
			String [] contig_list = new String [list.size ()];
			Collections.sort (list, this);
			for (int i = 0; i < list.size (); i++) {
				contig_list[i] = ((Chromosome) list.get (i)).getName ()
						.toLowerCase ();
			}
			filterer = FeatureFilterer.getFilterer (model);
			Set types = filterer.getFeatureTypes ();
			HashSet new_types = new HashSet ();
			HashSet old_types = new HashSet ();
			while (s != null) {
				toke = new StringTokenizer (s, "\t");
				if (toke.countTokens () != vals.length) {
					if (s.trim ().length () > 0)
						System.out.println ("did not process feature: " + s);
				}
				else {
					for (int i = 0; i < vals.length; i++)
						vals[i] = toke.nextToken ().trim ();
					int ind = Arrays.binarySearch (contig_list,
							vals[req[CONTIG]].toLowerCase ());
					int left = Integer.parseInt (vals[req[LEFT]]);
					int right = Integer.parseInt (vals[req[RIGHT]]);
					if (ind > -1) {
						int shift = (int) ((Chromosome) list.get (ind))
								.getStart () - 1;
						left += shift;
						right += shift;
					}
					if (vals[req[STRAND]].toLowerCase ().equals (FORWARD))
						template.strand = StrandedFeature.POSITIVE;
					else
						template.strand = StrandedFeature.NEGATIVE;
					template.location = new RangeLocation (left, right);
					template.type = vals[req[TYPE]];
					if (!types.contains (template.type)) {
						new_types.add (template.type);
						addFeatureType (template.type, filterer, genome);
					}
					else if (!new_types.contains (template.type))
						old_types.add (template.type);
					Hashtable anno = new Hashtable ();
					anno.put (LABEL_STRING, vals[req[LABEL]]);
					for (int i = 0; i < fields.length; i++) {
						if (fields[i] != null)
							anno.put (fields[i], vals[i]);
					}
					template.annotation = new SimpleAnnotation (anno);
					genome.getAnnotationSequence ().createFeature (template);
				}
				s = in.readLine ();
			}
			in.close ();
			System.out.println ("new! " + new_types + "   old " + old_types);
			//model.fireViewableRangeEvent ();
			//mauve.getRearrangementPanel ().setVisible (false);
			addedMoreOfTypes (old_types, filterer, genome);
			filterer.resetMultiRenderers ();
			//mauve.getRearrangementPanel ().setVisible (true);//.getNewPanel (genome.getViewIndex ()).feature.updateTransPanel ();
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}

	protected void addedMoreOfTypes (HashSet types, FeatureFilterer filterer, Genome genome) {
		Iterator itty = types.iterator ();
		int seq = genome.getSourceIndex ();
		while (itty.hasNext ()) {
			String type = (String) itty.next ();
			LinkedList vals = (LinkedList) filterer.filter_specs.get (type);
			Iterator itty2 = vals.iterator ();
			boolean resize = false;
			while (itty2.hasNext ()) {
				Object [] rends = (Object []) itty2.next ();
				MultiGenomeRectangularBeadRenderer rend = ((MultiGenomeRectangularBeadRenderer)
						((FilterCacheSpec) rends [FILTER_SPEC_INDEX]).getFeatureRenderer ());
				if (rend.getOffset (genome) == NO_OFFSET) {
					rend.setOffset (genome, offset [seq]);
					resize = true;
				}
				OverlayRendererWrapper over = (OverlayRendererWrapper) rends [OVERLAY_REND_INDEX];
				filterer.addOrRemove (over, false);
				filterer.addOrRemove (over, true);
				System.out.println ("there and back again");
			}
			if (resize) {
				offset [seq] += MauveConstants.FEATURE_HEIGHT;
				mauve.getRearrangementPanel ().getNewPanel (genome.getViewIndex ()).
						feature.resizeForMoreFeatures ();
			}
		}
	}	
	
	protected void addFeatureTypes (HashSet types, FeatureFilterer filterer, Genome genome) {
		Iterator itty = types.iterator ();
		while (itty.hasNext ()) {
			addFeatureType ((String) itty.next (), filterer, genome);
		}
	}
	
	protected void addFeatureType (String type, FeatureFilterer filterer, Genome genome) {
		MultiLineRenderer [] multis = new MultiLineRenderer [filterer.multis
		                                                     .size ()];
		filterer.multis.toArray (multis);
		//System.out.println ("another multi " + multis.length);
		int seq = genome.getSourceIndex ();
		try {
			filterer.addCheckBox (type);
			MultiGenomeRectangularBeadRenderer renderer = new MultiGenomeRectangularBeadRenderer (
					10.0, 10.0, Color.BLACK, Color.WHITE, new BasicStroke (), model);
			renderer.setOffset (genome, offset [seq]);
			renderer.setHeightScaling (false);
			FilterCacheSpec spec = new FilterCacheSpec (
					new FeatureFilter.And (new FeatureFilter.ByType (type),
							new FeatureFilter.StrandFilter (
									StrandedFeature.NEGATIVE)),
									new String [] {LABEL_STRING}, renderer);
			for (int i = 0; i < multis.length; i++) {
				filterer.addSpecForType (spec);
				FeaturePanel.makeRenderer (model, multis[i],
						spec);
			}
			renderer = new MultiGenomeRectangularBeadRenderer (10.0, 0, Color.BLACK,
					Color.white, new BasicStroke (), model);
			renderer.setOffset (genome, offset [seq]);
			offset [seq] += MauveConstants.FEATURE_HEIGHT;
			mauve.getRearrangementPanel ().getNewPanel (genome.getViewIndex ()).
					feature.resizeForMoreFeatures ();
			renderer.setHeightScaling (false);
			spec = new FilterCacheSpec (new FeatureFilter.And (
					new FeatureFilter.ByType (type),
					new FeatureFilter.StrandFilter (
							StrandedFeature.POSITIVE)),
							new String [] {LABEL_STRING}, renderer);
			for (int i = 0; i < multis.length; i++) {
				filterer.addSpecForType (spec);
				FeaturePanel.makeRenderer (model, multis[i],
						spec);
			}
		} catch (ChangeVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace ();
		}
	}

	/**
	 * called when a user decides to import a annotations from a flat file
	 */
	public void actionPerformed (ActionEvent e) {
		if (e.getActionCommand ().equals (JFileChooser.APPROVE_SELECTION)
				&& chooser.getSelectedFile () != null) {
			SwingUtilities.invokeLater (new Runnable () {
				public void run () {
					importAnnotationFile (chooser.getSelectedFile (),
							(Genome) genome_choices.getSelectedItem ());
				}
			});
		}
		setVisible (false);
	}

	public int compare (Object arg0, Object arg1) {
		return ((Chromosome) (arg0)).getName ().toLowerCase ().compareTo (
				((Chromosome) (arg1)).getName ().toLowerCase ());
	}

}
