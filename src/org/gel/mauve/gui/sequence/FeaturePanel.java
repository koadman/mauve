package org.gel.mauve.gui.sequence;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.EventListener;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.event.EventListenerList;

import org.biojava.bio.gui.sequence.AbstractBeadRenderer;
import org.biojava.bio.gui.sequence.FeatureBlockSequenceRenderer;
import org.biojava.bio.gui.sequence.FilteringRenderer;
import org.biojava.bio.gui.sequence.MultiLineRenderer;
import org.biojava.bio.gui.sequence.OverlayRendererWrapper;
import org.biojava.bio.gui.sequence.RectangularBeadRenderer;
import org.biojava.bio.gui.sequence.SequenceRenderer;
import org.biojava.bio.gui.sequence.SequenceViewerEvent;
import org.biojava.bio.gui.sequence.SequenceViewerListener;
import org.biojava.bio.gui.sequence.SequenceViewerMotionListener;
import org.biojava.bio.gui.sequence.TranslatedSequencePanel;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.LocationTools;
import org.biojava.utils.ChangeVetoException;
import org.gel.mauve.BrowserLauncher;
import org.gel.mauve.DbXrefFactory;
import org.gel.mauve.FilterCacheSpec;
import org.gel.mauve.Genome;
import org.gel.mauve.GenomeBuilder;
import org.gel.mauve.MauveConstants;
import org.gel.mauve.ModelEvent;
import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.gui.MySymbolSequenceRenderer;
import org.gel.mauve.gui.QualifierPanel;

/**
 * @author Paul Infield-Harm, Aaron Darling (derived from FastBeadDemo by <a
 *         href="mailto:kdj@sanger.ac.uk">Keith James) </a>
 * 
 * A panel that shows features, duh!
 *  
 */
public class FeaturePanel extends AbstractSequencePanel
{
	public static final int DEFAULT_WIDTH = 10000;
	public static final int DEFAULT_HEIGHT = 40;
    public static final int MAX_FEATURE_DISPLAY_RANGE = 500000;
    private TranslatedSequencePanel trans;
    private Sequence seq;
    private final GenbankMenuItemBuilder gmib = new GenbankMenuItemBuilder();
    private final DbXrefMenuItemBuilder dmib = new DbXrefMenuItemBuilder();
    private final FeaturePopupMenuBuilder fpmb = new FeaturePopupMenuBuilder();
    
    public FeaturePanel(Genome genome, BaseViewerModel model)
    {
        super(model, genome);
        setLayout(new BorderLayout());
        init();
    }

    // This prevents the display of the translatedSequencePanel at the wrong
    // scale...
    public void setBounds(int arg0, int arg1, int arg2, int arg3)
    {
        super.setBounds(arg0, arg1, arg2, arg3);

        if (trans != null)
            adjustScaleAndTranslation();
    }

    private void clearTransPanel()
    {
        if (trans != null)
        {
            remove(trans);
            trans = null;
        }
    }

    private void init()
    {
        seq = getGenome().getAnnotationSequence();

        if (seq == null)
        {
            clearTransPanel();
            return;
        }

        if (trans != null)
        {
            remove(trans);
        }
        
        trans = new TranslatedSequencePanel();
        add(trans, BorderLayout.CENTER);

        try
        {
            trans.setSequence(seq);
            trans.setDirection(TranslatedSequencePanel.HORIZONTAL);

            trans.addSequenceViewerMotionListener(new ToolTipMotionListener());
            trans.addSequenceViewerListener(new ClickListener());

            MultiLineRenderer multi = new MultiLineRenderer();
            FilterCacheSpec[] specs = getGenome().getAnnotationFormat().getFilterCacheSpecs();
            FeatureFilterer filterer = FeatureFilterer.getFilterer (model);
            MySymbolSequenceRenderer my_symbol = new MySymbolSequenceRenderer();
            filterer.addMultiRenderer (multi, my_symbol);
            
            for (int i = 0; i < specs.length; i++)
            {
                FilterCacheSpec spec = specs[i];
                if (spec.getFeatureRenderer() != null)
                {
                	makeRenderer (model, multi, spec);
                }
            }
            
            multi.addRenderer(my_symbol);
            trans.setRenderer(multi);
            
            // set the size of this element
            Dimension my_size = new Dimension();
            my_size.height = DEFAULT_HEIGHT;
            my_size.width = DEFAULT_WIDTH;
            setNewSize (my_size);
        }
        catch (ChangeVetoException e)
        {
            JOptionPane.showMessageDialog(this, "Could not render pane", "Rendering error", JOptionPane.ERROR_MESSAGE);
            clearTransPanel();
        }
        
        // add menu item builders
        fpmb.addMenuItemBuilder(gmib);
        fpmb.addMenuItemBuilder(dmib);
    }
    
	/**
	 * makes renderers for displaying a type of feature
	 * 
	 * @param multi
	 *            the MultiLineRenderer that renders features for this panel
	 * @param spec
	 *            the FilterCacheSpec that describes the feature type
	 * @throws ChangeVetoException
	 */
	protected static void makeRenderer (BaseViewerModel model,
			MultiLineRenderer multi, FilterCacheSpec spec)
			throws ChangeVetoException {
		FeatureBlockSequenceRenderer fbr = new FeatureBlockSequenceRenderer ();
		fbr.setFeatureRenderer (spec.getFeatureRenderer ());
		fbr.setCollapsing (false);
		OverlayRendererWrapper over = new OverlayRendererWrapper (
				new FilteringRenderer (fbr, spec.getFilter (), true));
		FeatureFilterer.getFilterer (model).addOverlayRenderer (multi, over);
		multi.addRenderer (over);
	}
	
	public void resizeForMoreFeatures () {
		Dimension my_size = getSize ();
		my_size.height += MauveConstants.FEATURE_HEIGHT;
		setNewSize (my_size);
	}
	
	private void setNewSize (Dimension my_size) {
		setSize (my_size);
		setPreferredSize (my_size);
		setMaximumSize (my_size);
		setMinimumSize (my_size);
		trans.setSize (my_size);
		trans.setPreferredSize (my_size);
		trans.setMaximumSize (my_size);
		trans.setMinimumSize (my_size);
	}

    public FeaturePopupMenuBuilder getFeaturePopupMenuBuilder(){ return fpmb; }
    public DbXrefMenuItemBuilder getDbXrefMenuItemBuilder(){ return dmib; }
    public GenbankMenuItemBuilder getGenbankMenuItemBuilder(){ return gmib; }
    
    private SequenceRenderer barRenderer(String type, Color innerColor, double depth, StrandedFeature.Strand strand) throws ChangeVetoException
    {
    	FeatureFilter filter = new FeatureFilter.And(new FeatureFilter.ByType(type),new FeatureFilter.StrandFilter(strand));
        FeatureBlockSequenceRenderer fbr = new FeatureBlockSequenceRenderer();
        double offset = strand == StrandedFeature.POSITIVE ? 0 : 5;
        if(strand==StrandedFeature.NEGATIVE)
        	offset = 10;
        RectangularBeadRenderer renderer = new RectangularBeadRenderer(depth, offset, Color.BLACK, innerColor, new BasicStroke());
        renderer.setHeightScaling(false);
        fbr.setFeatureRenderer(renderer);
        fbr.setCollapsing(false);
        return new OverlayRendererWrapper(new FilteringRenderer(fbr, filter, true));
    }

    private void adjustScaleAndTranslation()
    {
        if (getSize().width != 0)
        {
            int width = this.getSize().width;
            double scale = (double) width / (double) getGenome().getViewLength();

            if (getGenome().getViewStart() >= seq.length() ||
                    getGenome().getViewLength() >= MAX_FEATURE_DISPLAY_RANGE)
            {
                // TranslatedSequencePanel can't handle being translated out of
                // visibility, and we want to limit the viewable range for 
            	// better performance
                trans.setVisible(false);
            }
            else
            {
                trans.setScale(scale);
                trans.setSymbolTranslation((int) getGenome().getViewStart());
                trans.setVisible(true);
            }
        }
    }

    
    private final class DbXrefMenuAction extends AbstractAction
	{
    	protected String url;
    	protected String db_name;
    	
    	DbXrefMenuAction( String url, String db_name, String feature_name ){
    		super("View " + feature_name + " in " + db_name);
    		this.url = url;
    		this.db_name = db_name;
    	}
    	public void actionPerformed( ActionEvent e ){
    		try{
    			BrowserLauncher.openURL(url);
    		}catch(IOException ioe){}
    	}
	}

	private final class GenbankMenuAction extends AbstractAction
	{
    	protected int seq_index;
    	
    	GenbankMenuAction( int seq_index ){
    		super("View GenBank annotation for features at " + seq_index);
    		this.seq_index = seq_index;
    	}

    	public void actionPerformed( ActionEvent e ){

            Location loc = LocationTools.makeLocation(seq_index, seq_index);
            System.err.println("Starting with " + seq.countFeatures() + " features");
            FeatureHolder fh = seq.filter(new FeatureFilter.And(new FeatureFilter.OverlapsLocation(loc), new FeatureFilter.Not(new FeatureFilter.ByType(GenomeBuilder.MAUVE_AGGREGATE))));
            System.err.println("Filtering leaves " + fh.countFeatures() + " features.");
            if (fh.countFeatures() == 0)
                return;
            
            JDialog dialog = new JDialog((JFrame) FeaturePanel.this.getTopLevelAncestor(), "Feature Detail", true);

            JTabbedPane tabs = new JTabbedPane();
            dialog.getContentPane().add(tabs, BorderLayout.CENTER);

            for (Iterator fi = fh.features(); fi.hasNext();)
            {
                Feature f = (Feature) fi.next();
                tabs.add(new QualifierPanel(f));
            }
            dialog.setSize(800, 800);
            dialog.pack();
            dialog.setVisible(true);
    	}
	}
	
	public interface FeatureMenuItemBuilder extends EventListener
	{
		public JMenuItem[] getItem(SequenceViewerEvent sve, Genome g, BaseViewerModel model);
	}
	
	public class FeaturePopupMenuBuilder
	{
	    protected EventListenerList builders = new EventListenerList();
		public void addMenuItemBuilder(FeatureMenuItemBuilder fmib)
		{
			builders.add(FeatureMenuItemBuilder.class, fmib);
		}
		public JPopupMenu build(SequenceViewerEvent sve, Genome g, BaseViewerModel model)
		{
			Object[] listeners = builders.getListenerList();
        	JPopupMenu leMenu = new JPopupMenu();
        	for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==FeatureMenuItemBuilder.class) {
            		JMenuItem[] items = ((FeatureMenuItemBuilder)listeners[i+1]).getItem(sve, g, model);
            		for(int j = 0; j < items.length; j++)
            			leMenu.add(items[j]);
                }
            }
			return leMenu;
		}		
		public void removeMenuItemBuilder(FeatureMenuItemBuilder fmib)
		{
			builders.remove(FeatureMenuItemBuilder.class, fmib);
		}
	}
	
	
	private class GenbankMenuItemBuilder implements FeatureMenuItemBuilder
	{
		public JMenuItem[] getItem(SequenceViewerEvent sve, Genome g, BaseViewerModel model)
		{
        	JMenuItem gbk_item = new JMenuItem();
        	gbk_item.setAction( new GenbankMenuAction(sve.getPos()) );
        	return new JMenuItem[]{gbk_item};
		}
	}
	private class DbXrefMenuItemBuilder implements FeatureMenuItemBuilder
	{
		public JMenuItem[] getItem(SequenceViewerEvent sve, Genome g, BaseViewerModel model)
		{
			Vector items = new Vector();
            Object t = sve.getTarget();

            if (t instanceof FeatureHolder)
            {
            	// we'll be popping up a menu with DB xref options
            	// for the user

            	FeatureHolder fh = (FeatureHolder) t;
                for (Iterator fi = fh.features(); fi.hasNext();)
                {
                    Feature f = (Feature) fi.next();

                    if (f.getAnnotation().containsProperty("db_xref"))
                    {
                    	String feature_name = f.getType() + " ";
                    	if( f.getAnnotation().containsProperty("gene") )
                    		feature_name += f.getAnnotation().getProperty("gene");
                    	else if( f.getAnnotation().containsProperty("locus_tag") )
                    		feature_name += f.getAnnotation().getProperty("locus_tag");
                    	else
                    		feature_name += f.getLocation();
                    	String db_xref = f.getAnnotation().getProperty("db_xref").toString();
                    	if( db_xref.charAt(0) == '[' )
                    		db_xref = db_xref.substring(1, db_xref.length() - 1 );

                    	// tokenize on ,
                    	StringTokenizer comma_tok = new StringTokenizer(db_xref, ",");
                    	// if no tokens then don't bother with a menu
                    	// just display the GenBank annotation...
                    	while(comma_tok.hasMoreTokens()){
                    		String cur_xref = comma_tok.nextToken();
                    		cur_xref = cur_xref.trim();
	                    	// for each db xref, try to get its URL
                    		try{
		                    	DbXrefFactory dxuf = DbXrefFactory.getInstance();
		                    	String db_url = dxuf.getDbURL(cur_xref);
		                    	String db_name = dxuf.getDbName(cur_xref);
		                    	JMenuItem xref_item = new JMenuItem();
		                    	xref_item.setAction( new DbXrefMenuAction(db_url,db_name,feature_name) );
		                    	items.add(xref_item);
                    		}catch(DbXrefFactory.UnknownDatabaseException ude)
							{
                    			System.err.println(ude.getMessage());
							}
                    	}
                    }
                }
           }
           JMenuItem[] items_array = new JMenuItem[items.size()];
           items.toArray(items_array);
           return items_array;
		}
	}

    private final class ClickListener implements SequenceViewerListener
    {
        public void mouseClicked(SequenceViewerEvent sve)
        {            
        	JPopupMenu jpm = fpmb.build(sve, getGenome(), model);
        	jpm.show(sve.getMouseEvent().getComponent(), sve.getMouseEvent().getX(), sve.getMouseEvent().getY());

        }

        public void mousePressed(SequenceViewerEvent sve)
        {
            // This space intentionally left blank.
        }

        public void mouseReleased(SequenceViewerEvent sve)
        {
            // This space intentionally left blank.
        }
    }

    private final class ToolTipMotionListener implements SequenceViewerMotionListener
    {
        public void mouseDragged(SequenceViewerEvent sve)
        {
            //This space intentionally left blank.
        }

        public void mouseMoved(SequenceViewerEvent sve)
        {
            Object t = sve.getTarget();

            if (t == null)
            {
                trans.setToolTipText(null);
            }
            else if (t instanceof FeatureHolder)
            {
            	if (sve.getSource() instanceof FeatureBlockSequenceRenderer) {
            		AbstractBeadRenderer renderer = (AbstractBeadRenderer) 
            				((FeatureBlockSequenceRenderer) sve.getSource()
            				).getFeatureRenderer();
            		int y = sve.getMouseEvent().getY();
            		//System.out.println ("y: " + y + "   start: " + renderer.getBeadDisplacement () + "   end: " + renderer.getBeadDepth());
            		if (y < renderer.getBeadDisplacement() ||
            				y > renderer.getBeadDepth() + renderer.getBeadDisplacement())
            			return;
            		
            	}
                FeatureHolder fh = (FeatureHolder) t;
                /*System.out.println ("source: " + ((FeatureBlockSequenceRenderer) sve.getSource()).getDepth (sve));
                System.out.println ("target: " + sve.getTarget());*/
                System.out.println ("me: " + sve.getMouseEvent ());
                StringBuffer msg = new StringBuffer("<HTML>");
                for (Iterator fi = fh.features(); fi.hasNext();)
                {
                    Feature f = (Feature) fi.next();
                    if(f.getAnnotation() == null)
                    	continue;
                    msg.append(f.getType() + " ");
                    msg.append(f.getLocation());
                    msg.append("<br>");

                    if (f.getAnnotation().containsProperty("gene"))
                    {
                    	msg.append(" <b>");
                    	msg.append(f.getAnnotation().getProperty("gene"));
                    	msg.append("</b>");
                    }
                    else if (f.getAnnotation().containsProperty("locus_tag"))
                    {
                    	msg.append(" <b>");
                    	msg.append(f.getAnnotation().getProperty("locus_tag"));
                    	msg.append("</b>");
                    }
                    	
                    if (f.getAnnotation().containsProperty("product"))
                    {
                        msg.append("   ");
                        msg.append(f.getAnnotation().getProperty("product"));
                    }

                    if (fi.hasNext())
                    {
                        msg.append("<BR>");
                    }
                }
                trans.setToolTipText(msg.toString());
            }
            else
            {
                trans.setToolTipText(null);
            }
        }
    }

    public void viewableRangeChanged(ModelEvent event)
    {
        if (trans != null)
        {
            adjustScaleAndTranslation();
        }
    }
    
    public void genomesReordered(ModelEvent event)
    {
        // Ignored
    }
}
