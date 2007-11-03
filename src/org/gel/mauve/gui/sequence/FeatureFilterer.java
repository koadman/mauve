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
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
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
	 * contains the overlay renderers associated with this filterer's
	 * BaseViewerModel
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
	 * maps a string representing feature type to a LinkedList of arrays
	 * containing a FilterCacheSpec containing a FeatureFilter associated with a
	 * given type and its associated OverlayRendererWrapper
	 */
	protected Hashtable filter_specs;

	/**
	 * reference to panel that hold checkboxes; needed to add new types
	 */
	protected JPanel checks;

	protected JScrollPane scroll;
	JPanel outer;

	/**
	 * holds already created filterers to help with factory method of creation
	 */
	protected static final Hashtable FILTERERS = new Hashtable ();

	/**
	 * constructs new FeatureFilterer. Is private to ensure only one instance is
	 * created per BaseViewerModel
	 * 
	 * @param mod
	 *            The model this filterer is associated with
	 */
	protected FeatureFilterer (BaseViewerModel mod) {
		super ("Feature Filterer");
		model = mod;
		filter_specs = new Hashtable ();
		multis = new LinkedList ();
		multi_map = new Hashtable ();
		multi_to_symbol = new Hashtable ();
		initFeatureTypes ();
		initGUI ();
		FILTERERS.put (mod, this);
	}

	/**
	 * initializes gui components
	 */
	protected void initGUI () {
		Enumeration keys = filter_specs.keys ();
		checks = new JPanel (new GridLayout (0, 1));
		scroll = new JScrollPane (checks);
		scroll.setMaximumSize (new Dimension (220, 500));
		while (keys.hasMoreElements ())
			addCheckBox ((String) keys.nextElement ());
		getContentPane ().setLayout (new BorderLayout (5, 5));
		outer = (JPanel) getContentPane ();//new JPanel ();
		outer.add (scroll);
		TitledBorder title = BorderFactory
				.createTitledBorder ("All checked types will be displayed");
		outer.setBorder (BorderFactory.createCompoundBorder (BorderFactory
				.createCompoundBorder (BorderFactory.createEmptyBorder (5, 8,
						10, 8), title), BorderFactory.createEmptyBorder (5, 5,
				5, 5)));
		Dimension size = title.getMinimumSize (outer);
		outer.setPreferredSize (new Dimension (size.width + 26, outer
				.getPreferredSize ().height));
		//getContentPane ().add (outer);
		pack ();
	}

	protected void addCheckBox (final String type) {
		SwingUtilities.invokeLater (new Runnable () {
			public void run () {
				boolean vis = false;
				if (isVisible ()) {
					vis = true;
					setVisible (false);
				}
				JCheckBox item = new JCheckBox (type, true);
				item.addActionListener (FeatureFilterer.this);
				checks.add (item);
				Dimension size = getSize ();
				size.height = Math.min (size.height
						+ item.getPreferredSize ().height, 500);
				setSize (size);
				if (vis)
					setVisible (true);
			}
		});		
	}

	/**
	 * initializes selection of possible renderable features
	 * 
	 */
	protected void initFeatureTypes () {
		int count = model.getSequenceCount ();
		for (int i = 0; i < count; i++) {
			SupportedFormat format = model.getGenomeBySourceIndex (i)
					.getAnnotationFormat ();
			FilterCacheSpec [] specs = format.getFilterCacheSpecs ();
			for (int j = 0; j < specs.length; j++) {
				// will eventually be mapped to whether these sorts are
				// currently being displayed
				if (specs[j].getFeatureRenderer () != null) {
					addSpecForType (specs[j]);
				}
			}
		}
	}

	protected void addSpecForType (FilterCacheSpec spec) {
		String type = SeqFeatureData.getTypeFromFilterSpec (spec);
		LinkedList vals = (LinkedList) filter_specs.get (type);
		if (vals == null) {
			vals = new LinkedList ();
			filter_specs.put (type, vals);
		}
		vals.add (new Object [] {spec, null});
	}

	/**
	 * tracks MultiLineRenderer and their associated MySymbolSequenceRenderers
	 * used by the model associated with this FeatureFilterer
	 * 
	 * @param multi
	 *            a MultiLineRenderer
	 * @param my_symbol
	 *            MySymbolSequenceRenderer used by multi
	 */
	public void addMultiRenderer (MultiLineRenderer multi,
			MySymbolSequenceRenderer my_symbol) {
		multis.add (multi);
		multi_map.put (multi, new HashSet ());
		multi_to_symbol.put (multi, my_symbol);
	}

	/**
	 * if this OverlayRendererWrapper contains a FeatureRenderer associated with
	 * a FilterCacheSpec stored in filter_specs, associates this renderer with
	 * the appropriate type and FilterCacheSpec
	 * 
	 * @param multi
	 *            the MultiLineRenderer containing over
	 * @param over
	 *            the OverlayWrapperRenderer to associate
	 */
	public void addOverlayRenderer (MultiLineRenderer multi,
			OverlayRendererWrapper over) {
		try {
			FeatureRenderer rend = ((FeatureBlockSequenceRenderer) ((FilteringRenderer) over
					.getRenderer ()).getRenderer ()).getFeatureRenderer ();
			Iterator itty = filter_specs.values ().iterator ();
			outer: while (itty.hasNext ()) {
				Iterator it = ((LinkedList) itty.next ()).iterator ();
				while (it.hasNext ()) {
					Object [] val = (Object []) it.next ();
					if (((FilterCacheSpec) val[FlatFileFeatureConstants.FILTER_SPEC_INDEX])
							.getFeatureRenderer ().equals (rend)) {
						if (val[FlatFileFeatureConstants.OVERLAY_REND_INDEX] == null) {
							val[FlatFileFeatureConstants.OVERLAY_REND_INDEX] = over;
							HashSet set = (HashSet) multi_map.get (multi);
							if (set == null)
								System.out
								.println ("multi not yet added: " + multi);
							else
								set.add (over);
							continue outer;
						}
					}
				}
			}
		} catch (ClassCastException e) {
			e.printStackTrace ();
		}
	}

	/**
	 * called when a user chooses to filter features from the match panel pop-up
	 * menu
	 * 
	 * @param e
	 *            The action event triggering the method call
	 */
	public void actionPerformed (ActionEvent e) {
		if (e.getSource () instanceof JCheckBox) {
			JCheckBox box = (JCheckBox) e.getSource ();
			Iterator itty = ((LinkedList) filter_specs
					.get (box.getLabel ())).iterator ();
			while (itty.hasNext ()) {
				Object [] val = (Object []) itty.next ();
				OverlayRendererWrapper over = (OverlayRendererWrapper) val[FlatFileFeatureConstants.OVERLAY_REND_INDEX];
				System.out.println ("overlay: " + over);
				addOrRemove (over, box.isSelected ());
			}
			if (box.isSelected ()) {
				resetMultiRenderers ();
			}
		} else
			setVisible (true);
	}

	public void addOrRemove (OverlayRendererWrapper over, boolean add) {
		Iterator it = multis.iterator ();
		while (it.hasNext ()) {
			MultiLineRenderer multi = (MultiLineRenderer) it.next ();
			System.out.println ("playing with multi: " + multi);
			if (((HashSet) multi_map.get (multi)).contains (over)) {
				System.out.println ("more than playing");
				try {
					if (add)
						multi.addRenderer (over);
					else
						multi.removeRenderer (over);
				} catch (ChangeVetoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	protected void resetMultiRenderers () {
		Iterator itty = multis.iterator ();
		while (itty.hasNext ()) {
			try {
				MultiLineRenderer multi = (MultiLineRenderer) itty.next ();
				MySymbolSequenceRenderer rend = (MySymbolSequenceRenderer) multi_to_symbol
						.get (multi);
				multi.removeRenderer (rend);
				multi.addRenderer (rend);
			} catch (ChangeVetoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace ();
			}
		}
	}

	public Set getFeatureTypes () {
		return filter_specs.keySet ();
	}

	/**
	 * used in place of constructor to retrieve correct FeatureFilterer for
	 * genome
	 * 
	 * @param mod
	 *            The BaseViewerModel this filterer should be associated with
	 * @return The desired FeatureFilterer
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
	 * @param mod
	 *            The model that will no longer be used
	 */
	public static void removeFilterer (BaseViewerModel mod) {
		FILTERERS.remove (mod);
	}

}
