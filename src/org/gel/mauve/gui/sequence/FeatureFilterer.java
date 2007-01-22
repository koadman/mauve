package org.gel.mauve.gui.sequence;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.biojava.bio.gui.sequence.FeatureBlockSequenceRenderer;
import org.biojava.bio.gui.sequence.FeatureRenderer;
import org.biojava.bio.gui.sequence.FilteringRenderer;
import org.biojava.bio.gui.sequence.MultiLineRenderer;
import org.biojava.bio.gui.sequence.OverlayRendererWrapper;
import org.biojava.utils.ChangeVetoException;
import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.FilterCacheSpec;
import org.gel.mauve.SeqFeatureData;
import org.gel.mauve.SupportedFormat;
import org.gel.mauve.gui.MySymbolSequenceRenderer;


/**
 * Allows a user to select which features should be viewable by feature type
 * 
 * @author rissman
 *
 */
public class FeatureFilterer extends JFrame implements ActionListener {
	
	/**
	 * model this filterer is associated with
	 */
	protected BaseViewerModel model;
	
	/**
	 * contains the overlay renderers associated with this filterer's BaseViewerModel
	 */
	protected LinkedList multis;
	
	/**
	 * maps MultiLineRenderers to associated OverlayRendererWrappers
	 */
	protected Hashtable multi_map;
	
	/**
	 * maps MultiLineRenderers to associated MySymbolSequenceRenderers
	 */
	protected Hashtable multi_to_symbol;
	
	/**
	 * maps a string representing feature type to a LinkedList of arrays containing
	 * a FilterCacheSpec containing a FeatureFilter associated with a given type and
	 * its associated OverlayRendererWrapper
	 */
	protected Hashtable filter_specs;
	
	/**
	 * holds already created filterers to help with factory method of creation
	 */
	protected static final Hashtable FILTERERS = new Hashtable ();
	
	/**
	 * array index for FilterCacheSpecs in filter_specs
	 */
	protected static final int FILTER_SPEC_INDEX = 0;
	
	/**
	 * array index for OverlayRendererWrappers in filter_specs
	 */
	protected static final int OVERLAY_REND_INDEX = 1;
	
	/**
	 * constructs new FeatureFilterer.  Is private to ensure only one
	 * instance is created per BaseViewerModel
	 * 
	 * @param mod		The model this filterer is associated with
	 */
	protected FeatureFilterer (BaseViewerModel mod) {
		super ("Feature Filter");
		model = mod;
		filter_specs = new Hashtable ();
		multis = new LinkedList ();
		multi_map = new Hashtable ();
		multi_to_symbol = new Hashtable ();
		initFeatureTypes ();
		initGUI ();
		FILTERERS.put(mod, this);
	}
	
	/**
	 * initializes gui components
	 */
	protected void initGUI () {
		Enumeration keys = filter_specs.keys ();
		JPanel pane = new JPanel (new GridLayout (filter_specs.size(), 1));
		while (keys.hasMoreElements()) {
			JCheckBox item = new JCheckBox ((String) keys.nextElement(), true);
			item.addActionListener (this);
			pane.add(item);
		}
		JPanel outer = new JPanel (new BorderLayout (5, 5));
		outer.add (new JScrollPane (pane));
		TitledBorder title = BorderFactory.createTitledBorder (
				"All checked types will be displayed"); 
		outer.setBorder (BorderFactory.createCompoundBorder (
				BorderFactory.createCompoundBorder (
				BorderFactory.createEmptyBorder (5, 8, 10, 8), title),
				BorderFactory.createEmptyBorder (5, 5, 5, 5)));
		Dimension size = title.getMinimumSize (outer);
		outer.setPreferredSize (new Dimension (size.width + 26,
				outer.getPreferredSize ().height));
		getContentPane ().add (outer);
		pack ();
	}
	
	/**
	 * initializes selection of possible renderable features
	 *
	 */
	protected void initFeatureTypes () {
		int count = model.getSequenceCount ();
		for (int i = 0; i < count; i++) {
			SupportedFormat format = model.getGenomeBySourceIndex(i).getAnnotationFormat();
			FilterCacheSpec [] specs = format.getFilterCacheSpecs();
			for (int j = 0; j < specs.length; j++) {
				//will eventually be mapped to whether these sorts are currently being displayed
				if (specs [j].getFeatureRenderer() != null) {
					String type = SeqFeatureData.getTypeFromFilterSpec (specs [j]);
					LinkedList vals = (LinkedList) filter_specs.get(type);
					if (vals == null) {
						vals = new LinkedList ();
						filter_specs.put (type, vals);
					}
					vals.add(new Object [] {specs [j], null});
				}
			}
		}
	}
	
	/**
	 * tracks MultiLineRenderer and their associated MySymbolSequenceRenderers used
	 * by the model associated with this FeatureFilterer
	 * 
	 * @param multi			a MultiLineRenderer
	 * @param my_symbol		MySymbolSequenceRenderer used by multi
	 */
	public void addMultiRenderer (MultiLineRenderer multi, MySymbolSequenceRenderer my_symbol) {
		multis.add (multi);
		multi_map.put (multi, new HashSet ());
		multi_to_symbol.put (multi, my_symbol);
	}
	
	/**
	 * if this OverlayRendererWrapper contains a FeatureRenderer associated with a
	 * FilterCacheSpec stored in filter_specs, associates this renderer with the
	 * appropriate type and FilterCacheSpec
	 * 
	 * @param multi		the MultiLineRenderer containing over
	 * @param over		the OverlayWrapperRenderer to associate
	 */
	public void addOverlayRenderer (MultiLineRenderer multi, OverlayRendererWrapper over) {
		try {
			FeatureRenderer rend = ((FeatureBlockSequenceRenderer) ((FilteringRenderer)
					over.getRenderer ()).getRenderer ()).getFeatureRenderer ();
			Iterator itty = filter_specs.values ().iterator ();
			outer: while (itty.hasNext ()) {
				Iterator it = ((LinkedList) itty.next ()).iterator ();
				while (it.hasNext ()) {
					Object [] val = (Object []) it.next ();
					if (((FilterCacheSpec) val
							[FILTER_SPEC_INDEX]).getFeatureRenderer ().equals (rend) && 
							val [OVERLAY_REND_INDEX] == null) {
						val [OVERLAY_REND_INDEX] = over;
						HashSet set = (HashSet) multi_map.get (multi);
						if (set == null)
							System.out.println ("multi not yet added: " + multi);
						else
							set.add (over);
						continue outer;
					}
				}
			}
		}
		catch (ClassCastException e) {
			e.printStackTrace ();
		}
	}
	
	/**
	 * called when a user chooses to filter features from the match panel pop-up menu
	 * 
	 * @param e		The action event triggering the method call
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource () instanceof JCheckBox) {
			try {
				JCheckBox box = (JCheckBox) e.getSource ();
				Iterator itty = ((LinkedList) filter_specs.get (box.getLabel ())).iterator ();
				while (itty.hasNext ()) {
					Object [] val = (Object []) itty.next ();
					OverlayRendererWrapper over = (OverlayRendererWrapper) val [OVERLAY_REND_INDEX];
					Iterator it = multis.iterator ();
					while (it.hasNext ()) {
						MultiLineRenderer multi = (MultiLineRenderer) it.next ();
						if (((HashSet) multi_map.get (multi)).contains (over)) {
							if (box.isSelected ())
								multi.addRenderer (over);
							else
								multi.removeRenderer (over);
						}
					}
				}
				if (box.isSelected ()) {
					itty = multis.iterator ();
					while (itty.hasNext ()) {
						MultiLineRenderer multi = (MultiLineRenderer) itty.next ();
						MySymbolSequenceRenderer rend = (MySymbolSequenceRenderer)
								multi_to_symbol.get (multi);
						multi.removeRenderer (rend);
						multi.addRenderer (rend);
					}
				}
			}
			catch (ChangeVetoException ex) {
				ex.printStackTrace ();
			}
		}
		else
			setVisible (true);
	}

	/**
	 * used in place of constructor to retrieve correct FeatureFilterer for genome
	 * 
	 * @param mod			The BaseViewerModel this filterer should be associated with
	 * @return				The desired FeatureFilterer
	 */
	public static FeatureFilterer getFilterer (BaseViewerModel mod) {
		FeatureFilterer filter = (FeatureFilterer) FILTERERS.get (mod);
		if (filter == null)
			filter = new FeatureFilterer (mod);
		return filter;
	}
	
	/**
	 * removes a filterer from the cache of filterers; should only be called
	 * when a BaseViewerModel will no longer be used
	 * 
	 * @param mod		The model that will no longer be used
	 */
	public static void removeFilterer (BaseViewerModel mod) {
		FILTERERS.remove(mod);
	}
	
}
