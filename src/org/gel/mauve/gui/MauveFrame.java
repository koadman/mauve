/** 
 * MauveFrame.java
 *
 * Title:			Mauve
 * Description:		Viewer for multiple genome alignments and annotation
 * @author			koadman
 * @version			
 */

package org.gel.mauve.gui;

import gr.zeus.ui.JConsole;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.prefs.BackingStoreException;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.BrowserLauncher;
import org.gel.mauve.LcbViewerModel;
import org.gel.mauve.ModelBuilder;
import org.gel.mauve.ModelProgressListener;
import org.gel.mauve.MyConsole;
import org.gel.mauve.SeqFeatureData;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.analysis.SnpExporter;
import org.gel.mauve.color.ColorMenu;
import org.gel.mauve.gui.dnd.FileDrop;
import org.gel.mauve.gui.sequence.FeatureFilterer;
import org.gel.mauve.gui.sequence.FlatFileFeatureImporter;

/**
 * The window frame for a Mauve application, containing a menu bar, a tool bar
 * and a RearrangementPanel to display genome alignments. 
 */
public class MauveFrame extends JFrame implements ActionListener, ModelProgressListener
{
    protected BaseViewerModel model;
    
    String documentation_url = "http://gel.ahabs.wisc.edu/mauve/mauve-user-guide";
    // member declarations
    JMenuBar jMenuBar1 = new JMenuBar();
    JMenuItem jMenuFileOpen = new JMenuItem();
    JMenuItem jMenuFileAlign = new JMenuItem();
    JMenuItem jMenuFileProgressiveAlign = new JMenuItem();
    JMenuItem jMenuFilePrint = new JMenuItem();
    JMenuItem jMenuFilePageSetup = new JMenuItem();
    JMenuItem jMenuFilePrintPreview = new JMenuItem();
    //JMenuItem jMenuFileImport = new JMenuItem();
    JMenuItem jMenuFileClose = new JMenuItem();
    JMenuItem jMenuFileQuit = new JMenuItem();

    JMenu jMenuHelp = new JMenu();
    JMenuItem jMenuHelpAbout = new JMenuItem();
    JMenuItem jMenuHelpConsole = new JMenuItem();
    JMenuItem jMenuHelpDocumentation = new JMenuItem();
    JMenuItem jMenuHelpClearCache = new JMenuItem();
    
    
    JMenu jMenuView = new JMenu ();

    ColorMenu jMenuViewColorScheme = new ColorMenu ();
    StyleMenu jMenuViewStyle = new StyleMenu ();
    ExportMenu jMenuFileExportMenu = new ExportMenu ();
    
    JMenu jMenuGoTo = new JMenu ();
    JMenuItem jMenuGoToSeqPos = new JMenuItem ();
    JMenuItem jMenuGoToFeatName = new JMenuItem ();
    JMenuItem jMenuGoToSearchFeatures = new JMenuItem ();
    
    JFileChooser fc;
    RearrangementPanel rrpanel;
    JToolBar toolbar;
    public LCBStatusBar status_bar;
    Mauve mauve;
    AlignFrame alignFrame;
    AlignFrame progressiveAlignFrame;
    JScrollPane scrollPane;
    SequenceNavigator navigator;
    //protected FlatFileFeatureImporter importer;
    
    private int progressSequenceCount;


    static ImageIcon home_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/Home16.gif"));
    static ImageIcon left_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/Back16.gif"));
    static ImageIcon right_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/Forward16.gif"));
    static ImageIcon zoomin_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/ZoomIn16.gif"));
    static ImageIcon zoomout_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/ZoomOut16.gif"));
    static ImageIcon zoom_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/Zoom16.gif"));
    public static ImageIcon mauve_icon = new ImageIcon(MauveFrame.class.getResource("/images/mauve_icon.gif"));
    static ImageIcon find_feature_icon = new ImageIcon(MauveFrame.class.getResource("/images/searchBinoculars16.png"));
    static ImageIcon dcj_icon = new ImageIcon(MauveFrame.class.getResource("/images/Dcj16.gif"));
    static ImageIcon grimm_icon = new ImageIcon(MauveFrame.class.getResource("/images/Grimm16.gif"));
    static Cursor zoom_in_cursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(MauveFrame.class.getResource("/images/ZoomIn24.gif")).getImage(), new Point(8, 8), "zoomin");
    static Cursor zoom_out_cursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon(MauveFrame.class.getResource("/images/ZoomOut24.gif")).getImage(), new Point(8, 8), "zoomout");

    public MauveFrame(Mauve mauve)
    {
        this.mauve = mauve;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        fc = new JFileChooser();
        pack();
        initComponents();
        setVisible(true);
    }

    private void initComponents()
    {
        setTitle("Mauve " + mauve.getVersion() + " - Genome Alignment Visualization");
        setIconImage(mauve_icon.getImage());
        status_bar = new LCBStatusBar();
        getContentPane().add(status_bar, BorderLayout.SOUTH);
        setLocation(new java.awt.Point(0, 0));

        initMenus();

        setSize(new java.awt.Dimension(791, 500));

        // event handling
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent e)
            {
                thisWindowClosing(e);
            }
            public void windowClosed(java.awt.event.WindowEvent e)
            {
            	mauve.frameClosed();
            }
        });

        // Drag-and-drop handling.
        new FileDrop(getContentPane(), new FileDrop.Listener()
        {
            public void filesDropped(java.io.File[] files)
            {
                for (int i = 0; i < files.length; i++)
                {
                    mauve.loadFile(files[i]);
                }
            }
        });
    }

    /**
     *  
     */
    private void initMenus()
    {
        JMenu jMenuFile = new JMenu();
        jMenuFile.setToolTipText("Perform file related actions");
        jMenuFile.setVisible(true);
        jMenuFile.setText("File");
        jMenuFile.setMnemonic('F');
        jMenuFileOpen.setToolTipText("Open an existing alignment...");
        jMenuFileOpen.setVisible(true);
        jMenuFileOpen.setText("Open...");
        jMenuFileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        jMenuFileOpen.setMnemonic('O');
        
        jMenuFileAlign.setToolTipText("Align sequences...");
        jMenuFileAlign.setVisible(true);
        jMenuFileAlign.setText("Align...");
        jMenuFileAlign.setMnemonic('A');
        
        jMenuFileProgressiveAlign.setToolTipText("Align sequences with progressiveMauve (slower, more sensitive)...");
        jMenuFileProgressiveAlign.setVisible(true);
        jMenuFileProgressiveAlign.setText("Align with progressiveMauve...");
        jMenuFileProgressiveAlign.setMnemonic('M');

        jMenuFilePrint.setToolTipText("Print the current view...");
        jMenuFilePrint.setVisible(true);
        jMenuFilePrint.setEnabled(false);
        jMenuFilePrint.setText("Print");
        jMenuFilePrint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        jMenuFilePrint.setMnemonic('P');
        
        jMenuFilePageSetup.setToolTipText("Choose printer settings...");
        jMenuFilePageSetup.setVisible(true);
        jMenuFilePageSetup.setEnabled(false);
        jMenuFilePageSetup.setText("Page Setup...");
        
        jMenuFilePrintPreview.setToolTipText("Show a print preview...");
        jMenuFilePrintPreview.setVisible(true);
        jMenuFilePrintPreview.setEnabled(false);
        jMenuFilePrintPreview.setText("Print Preview");
        jMenuFilePrintPreview.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.SHIFT_MASK + ActionEvent.CTRL_MASK));
        
        jMenuFileExportMenu.setToolTipText("Export data in various formats");
        jMenuFileExportMenu.setVisible(true);
        jMenuFileExportMenu.setEnabled(true);
        jMenuFileExportMenu.setText("Export");
        jMenuFileExportMenu.setMnemonic('x');

        /*jMenuFileImport.setToolTipText("Import annotation file...");
        jMenuFileImport.setVisible(true);
        jMenuFileImport.setEnabled(false);
        jMenuFileImport.setText("Import Annotations");
        jMenuFileImport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        jMenuFileImport.setMnemonic('s');*/
        
        jMenuFileClose.setToolTipText("Close this alignment...");
        jMenuFileClose.setVisible(true);
        jMenuFileClose.setEnabled(false);
        jMenuFileClose.setText("Close");
        jMenuFileClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
        jMenuFileClose.setMnemonic('C');

        JSeparator jMenuFileSeparator1 = new JSeparator();
        jMenuFileSeparator1.setVisible(true);

        jMenuFileQuit.setToolTipText("Quit this application");
        jMenuFileQuit.setVisible(true);
        jMenuFileQuit.setText("Quit");
        jMenuFileQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        jMenuFileQuit.setMnemonic('Q');

        jMenuHelp.setToolTipText("Get help using this program");
        jMenuHelp.setVisible(true);
        jMenuHelp.setText("Help");
        jMenuHelp.setMnemonic('H');
        jMenuHelpAbout.setToolTipText("Display version and author information...");
        jMenuHelpAbout.setVisible(true);
        jMenuHelpAbout.setText("About Mauve...");
        jMenuHelpAbout.setMnemonic('A');

        jMenuHelpDocumentation.setToolTipText("View Mauve's online documentation...");
        jMenuHelpDocumentation.setVisible(true);
        jMenuHelpDocumentation.setText("Online documentation...");
        jMenuHelpDocumentation.setMnemonic('d');

        jMenuHelpConsole.setToolTipText("Shows the console where error messages and other information is reported");
        jMenuHelpConsole.setVisible(true);
        jMenuHelpConsole.setText("Show console");
        jMenuHelpConsole.setMnemonic('c');
        
        jMenuHelpClearCache.setToolTipText("Clear the on-disk cache of processed alignments");
        jMenuHelpClearCache.setVisible(true);
        jMenuHelpClearCache.setText("Clear alignment cache");
        jMenuHelpClearCache.setMnemonic('r');
        
        jMenuView.setToolTipText("Viewer controls");
        jMenuView.setVisible(true);
        jMenuView.setEnabled(false);
        jMenuView.setText("View");
        jMenuView.setMnemonic('V');

//        jMenuViewColorScheme.setToolTipText("Color schemes for the similarity plot");
        jMenuViewColorScheme.setVisible(true);
        jMenuViewColorScheme.setEnabled(false);
        jMenuViewColorScheme.setText("Color Scheme");
        jMenuViewColorScheme.setMnemonic('C');

        jMenuViewStyle.setToolTipText("Drawing styles for the genome comparison");
        jMenuViewStyle.setVisible(true);
        jMenuViewStyle.setEnabled(false);
        jMenuViewStyle.setText("Style");
        jMenuViewStyle.setMnemonic('S');

//       jMenuGoTo.setToolTipText("Go to specific location in genome");
        jMenuGoTo.setVisible(true);
        jMenuGoTo.setEnabled(false);
        jMenuGoTo.setText("Go To");
        jMenuGoTo.setMnemonic('T');
        
        jMenuGoToSeqPos.setToolTipText("Go To Numerical Sequence Position");
        jMenuGoToSeqPos.setVisible(true);
        jMenuGoToSeqPos.setText("Sequence Position");
        jMenuGoToSeqPos.setMnemonic('e');
        
        jMenuGoToFeatName.setToolTipText("Find a feature by exact name");
        jMenuGoToFeatName.setVisible(true);
        jMenuGoToFeatName.setText("Feature Named. . .");
        jMenuGoToFeatName.setMnemonic('N');
        
        jMenuGoToSearchFeatures.setToolTipText("Find features with specified constraints");
        jMenuGoToSearchFeatures.setVisible(true);
        jMenuGoToSearchFeatures.setText("Find Features. . .");
        jMenuGoToSearchFeatures.setMnemonic('i');
        jMenuGoToSearchFeatures.setAccelerator(KeyStroke.getKeyStroke(
        		KeyEvent.VK_I, ActionEvent.CTRL_MASK));

        setJMenuBar(jMenuBar1);

        jMenuBar1.add(jMenuFile);
        jMenuFile.add(jMenuFileOpen);
        jMenuFile.add(jMenuFileAlign);
        jMenuFile.add(jMenuFileProgressiveAlign);
        jMenuFile.add(jMenuFilePrint);
        jMenuFile.add(jMenuFilePageSetup);
        jMenuFile.add(jMenuFilePrintPreview);
        //jMenuFile.add(jMenuFileImport);
        jMenuFile.add(jMenuFileExportMenu);
        jMenuFile.add(jMenuFileClose);
        jMenuFile.add(jMenuFileSeparator1);
        jMenuFile.add(jMenuFileQuit);

        jMenuBar1.add (jMenuView);
        jMenuView.add(jMenuViewColorScheme);
        jMenuView.add(jMenuViewStyle);
        jMenuView.add(jMenuGoTo);

//        jMenuBar1.add (jMenuGoTo);
        jMenuGoTo.add (jMenuGoToSeqPos);
        jMenuGoTo.add (jMenuGoToFeatName);
        jMenuGoTo.add (jMenuGoToSearchFeatures);
        
        jMenuBar1.add(jMenuHelp);
        jMenuHelp.add(jMenuHelpAbout);
        jMenuHelp.add(jMenuHelpDocumentation);
        jMenuHelp.add(jMenuHelpClearCache);
        jMenuHelp.add(jMenuHelpConsole);

        jMenuFile.setMinimumSize(jMenuFile.getSize());
        jMenuView.setMinimumSize(jMenuView.getSize());
        jMenuHelp.setMinimumSize(jMenuHelp.getSize());
        jMenuBar1.setVisible(true);

        jMenuFileOpen.addActionListener(this);
        jMenuFileAlign.addActionListener(this);
        jMenuFileProgressiveAlign.addActionListener(this);        
        jMenuFileClose.addActionListener(this);
        jMenuFilePrint.addActionListener(this);
        jMenuFilePageSetup.addActionListener(this);
        jMenuFilePrintPreview.addActionListener(this);
        //jMenuFileImport.addActionListener(this);
        jMenuFileQuit.addActionListener(this);
        jMenuHelpAbout.addActionListener(this);
        jMenuHelpDocumentation.addActionListener(this);
        jMenuHelpConsole.addActionListener(this);
        jMenuHelpClearCache.addActionListener(this);
        jMenuGoToSeqPos.addActionListener (this);
        jMenuGoToFeatName.addActionListener (this);
        jMenuGoToSearchFeatures.addActionListener (this);
        
        // set up key bindings
        jMenuFilePrint.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl P"), "Print");
        jMenuFileOpen.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl O"), "Open");
        jMenuFileClose.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl W"), "Close");
        jMenuFileQuit.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl Q"), "Quit");
        jMenuGoToSearchFeatures.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        		KeyStroke.getKeyStroke("ctrl I"), jMenuGoToSearchFeatures.getText ());
        jMenuFilePrint.getActionMap().put("Print", new GenericAction(this, "Print"));
        jMenuFilePageSetup.getActionMap().put("PageSetup", new GenericAction(this, "PageSetup"));
        jMenuFilePrintPreview.getActionMap().put("PrintPreview", new GenericAction(this, "PrintPreview"));
        //jMenuFileImport.getActionMap().put("Import", new GenericAction(this, "Import"));
        jMenuFileOpen.getActionMap().put("Open", new GenericAction(this, "Open"));
        jMenuFileClose.getActionMap().put("Close", new GenericAction(this, "Close"));
        jMenuFileQuit.getActionMap().put("Quit", new GenericAction(this, "Quit"));
        jMenuHelpAbout.getActionMap().put("About", new GenericAction(this, "About"));
        jMenuHelpDocumentation.getActionMap().put("Documentation", new GenericAction(this, "About"));
        jMenuHelpClearCache.getActionMap().put("ClearCache", new GenericAction(this, "ClearCache"));
        jMenuHelpConsole.getActionMap().put("Console", new GenericAction(this, "Console"));
        jMenuGoToSeqPos.getActionMap().put(jMenuGoToSeqPos.getText (), new GenericAction(
        		this, jMenuGoToSeqPos.getText ()));
        jMenuGoToFeatName.getActionMap().put(jMenuGoToFeatName.getText (), new GenericAction(
        		this, jMenuGoToFeatName.getText ()));
        jMenuGoToSearchFeatures.getActionMap().put(jMenuGoToSearchFeatures.getText (), 
        		new GenericAction(this, jMenuGoToSearchFeatures.getText ()));
    }

    class GenericAction extends AbstractAction
    {

        GenericAction(ActionListener al, String command)
        {
            super(command);
            this.al = al;
            putValue(ACTION_COMMAND_KEY, command);
        }

        ActionListener al;

        public void actionPerformed(ActionEvent e)
        {
            al.actionPerformed(e);
        }
    }

    private boolean mShown = false;

    public void actionPerformed(ActionEvent ae)
    {
        if (ae.getSource() instanceof JMenuItem)
        {
            JMenuItem source = (JMenuItem) (ae.getSource());
            if (source == jMenuFileQuit || ae.getActionCommand().equals("Quit"))
            {
                setVisible(false);
                dispose();
                System.exit(0);
            }
            if (source == jMenuFileOpen || ae.getActionCommand().equals("Open"))
            {
                doFileOpen();
            }
            if (source == jMenuFileClose || ae.getActionCommand().equals("Close"))
            {
                this.
                thisWindowClosing(null);
            }
            if (source == jMenuFileAlign)
            {
                doAlign();
            }
            if (source == jMenuFileProgressiveAlign)
            {
                doProgressiveAlign();
            }
            if (source == jMenuFilePrint || ae.getActionCommand().equals("Print"))
            {
                rrpanel.print();
            }
            if (source == jMenuFilePageSetup || ae.getActionCommand().equals("PageSetup"))
            {
                rrpanel.pageSetup();
            }
            if (source == jMenuFilePrintPreview || ae.getActionCommand().equals("PrintPreview"))
            {
            	if (rrpanel != null)
            	{
            		PrintPreviewDialog dialog = new PrintPreviewDialog(rrpanel, rrpanel.pageFormat, 1);
            		dialog.setVisible(true);
            	}
            }
            /*if (source == jMenuFileImport || ae.getActionCommand().equals("Import"))
            {
            	importer.setVisible (true);
            }*/            

            if (source == jMenuHelpAbout || ae.getActionCommand().equals("About"))
            {
                new SplashScreen("/images/mauve_logo.png", Mauve.about_message, this, 5000000);
            }
            if (source == jMenuHelpDocumentation || ae.getActionCommand().equals("Documentation"))
            {
        		try{
        			BrowserLauncher.openURL(documentation_url);
        		}catch(IOException ioe){}
            }
            if (source == jMenuHelpConsole || ae.getActionCommand().equals("Console"))
            {
            	JConsole console = JConsole.getConsole();
            	console.showConsole();
            }
            if (source == jMenuHelpClearCache || ae.getActionCommand().equals("ClearCache"))
            {
            	try{
            		ModelBuilder.clearDataCache();
            	}catch(BackingStoreException bse)
            	{
            		bse.printStackTrace();
            	}
            }
            if (source == jMenuGoToSearchFeatures || ae.getActionCommand().equals(
            		jMenuGoToSearchFeatures.getText ())) {
            	navigator.showNavigator ();
            }
            if (source == jMenuGoToSeqPos || ae.getActionCommand().equals(
            		jMenuGoToSeqPos.getText ())) {
            	SequenceNavigator.goToSeqPos (this, getModel(), rrpanel);
            }
            if (source == jMenuGoToFeatName || ae.getActionCommand().equals(
            		jMenuGoToFeatName.getText ())) {
            	navigator.goToFeatureByName ();
            }
        }
    }

    public void addNotify()
    {
        super.addNotify();

        if (mShown)
            return;

        // resize frame to account for menubar
        JMenuBar jMenuBar = getJMenuBar();
        if (jMenuBar != null)
        {
            int jMenuBarHeight = jMenuBar.getPreferredSize().height;
            Dimension dimension = getSize();
            dimension.height += jMenuBarHeight;
            setSize(dimension);
        }

        mShown = true;
    }

    /** Close the window when the close box is clicked */
    void thisWindowClosing(java.awt.event.WindowEvent e)
    {
    	if (model != null)
    		FeatureFilterer.removeFilterer (model);
        mauve.closeFrame(this);
        if (navigator != null) {
        	navigator.dispose ();
        	navigator = null;
        }
        /*importer.dispose ();
        importer = null;*/
        System.gc ();
    }
    
    /**
     * 
     */
    public void reset()
    {
        getContentPane().remove(scrollPane);
        rrpanel = null;
        jMenuBar1.remove(toolbar);
        toolbar = null;
        jMenuBar1.validate();
        jMenuBar1.repaint();
        getContentPane().repaint();
        setTitle("Mauve " + mauve.getVersion() + " - Genome Alignment Visualization");
        jMenuFilePrint.setEnabled(false);
        jMenuFilePageSetup.setEnabled(false);
        jMenuFilePrintPreview.setEnabled(false);
        //jMenuFileImport.setEnabled(false);
        jMenuFileExportMenu.setEnabled(false);
        jMenuGoTo.setEnabled(false);
        jMenuFileClose.setEnabled(false);
        model.removeHighlightListener(status_bar);
        model = null;
        status_bar.clear();
    }

    void loadError(String message)
    {
        MyConsole.err().println(message);
        setTitle("Mauve " + mauve.getVersion() + " - Genome Alignment Visualization");
        model = null;
    }
    
	/*public FlatFileFeatureImporter getFeatureImporter () {
		return importer;
	}*/

    public BaseViewerModel getModel()
    {
        return model;
    }
    
	public RearrangementPanel getRearrangementPanel () {
		return rrpanel;
	}
    
    public void setModel(BaseViewerModel model)
    {
        if (model == null)
        {
            this.model = null;
        }
        else
        {
            this.model = model;
 
            toolbar = new JToolBar();
            jMenuBar1.add(toolbar);
            
            rrpanel = new RearrangementPanel(toolbar);
            Dimension max_size = new Dimension( 10000, 10000 );
            rrpanel.setMaximumSize(max_size);

            scrollPane = new JScrollPane(rrpanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            getContentPane().add(scrollPane);
            scrollPane.getVerticalScrollBar().setUnitIncrement(25);

            model.addHighlightListener(status_bar);
            status_bar.setModel(model);
            rrpanel.addHintMessageListener(status_bar);
            
            rrpanel.init(model);
            validate();
            toFront();
            /*
             * go into visualization mode: set the title and enable the print
             * option
             */
            setTitle("Mauve " + mauve.getVersion() + " - " + model.getSrc().getName());
            jMenuFilePrint.setEnabled(true);
            jMenuFilePageSetup.setEnabled(true);
            jMenuFilePrintPreview.setEnabled(true);
            //jMenuFileImport.setEnabled(true);
            jMenuFileExportMenu.setEnabled(true);
            jMenuFileClose.setEnabled(true);
            jMenuGoTo.setEnabled(true);
            if (SeqFeatureData.userSelectableGenomes (model, false, true).size () > 0) {
            	navigator = new SequenceNavigator(this, rrpanel, model);
            	jMenuGoToFeatName.setEnabled (true);
            	jMenuGoToSearchFeatures.setEnabled (true);
            }
            else {
            	jMenuGoToFeatName.setEnabled (false);
            	jMenuGoToSearchFeatures.setEnabled (false);
            }

            jMenuView.setEnabled(true);
            jMenuViewColorScheme.setEnabled(true);
            jMenuViewColorScheme.build(model);
            jMenuViewStyle.setTarget(model, rrpanel);
            jMenuFileExportMenu.setTarget(model, rrpanel);
            //importer = new FlatFileFeatureImporter (this);
            toFront();
        }
    }
    
    /**
     * Pop up an AlignFrame to let the user select parameters for genome
     * alignment
     */
    //TODO: Move out to Mauve (or some other class).
    void doAlign()
    {
        if (alignFrame == null)
        {
            alignFrame = new MauveAlignFrame(mauve);
            alignFrame.initComponents();
       		// notify the alignment frame if the parent window is closed...
            addWindowListener(new java.awt.event.WindowAdapter()
            {
                public void windowClosed(java.awt.event.WindowEvent e)
                {
                	alignFrame.thisWindowClosed(e);
                }
            });
        }
        alignFrame.setVisible(true);
    }
    
    void doProgressiveAlign()
    {
        if (progressiveAlignFrame == null)
        {
        	progressiveAlignFrame = new ProgressiveMauveAlignFrame(mauve);
        	progressiveAlignFrame.initComponents();
        }
        progressiveAlignFrame.setVisible(true);
    }
    
    /** called when the user selects 'Open' from the file menu */
    //TODO: Move out to Mauve (or some other class).
    void doFileOpen()
    {
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File rr_file = fc.getSelectedFile();
            mauve.loadFile(rr_file);
        }
    }

    public void buildStart()
    {
        status_bar.setHint("Starting...");
    }

    public void downloadStart()
    {
        status_bar.setHint("Downloading file");
    }

    public void alignmentStart()
    {
        status_bar.setHint("Reading file");
    }

    public void alignmentEnd(int sequenceCount)
    {
        progressSequenceCount = sequenceCount;
    }

    public void featureStart(int sequenceIndex)
    {
        status_bar.setHint("Reading sequence " + (sequenceIndex + 1) + " of " + progressSequenceCount);
    }

    public void done()
    {
        status_bar.setHint("Done");
    }

}