package org.gel.mauve.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.XmfaViewerModel;
import org.gel.mauve.analysis.OneToOneOrthologExporter;
import org.gel.mauve.analysis.PermutationExporter;
import org.gel.mauve.analysis.SnpExporter;

public class ExportMenu extends JMenu implements ActionListener {

    JMenuItem jMenuFileExportImage = new JMenuItem();
	JMenuItem jMenuFileExportSnps = new JMenuItem();
	JMenuItem jMenuFileExportSpas = new JMenuItem();
	JMenuItem jMenuFileExportOrthologs = new JMenuItem();
	JMenuItem jMenuFileExportPermutation = new JMenuItem();

	BaseViewerModel model;
	RearrangementPanel rrpanel;
    public ExportMenu()
    {
        jMenuFileExportImage.setToolTipText("Export graphics to file...");
        jMenuFileExportImage.setVisible(true);
        jMenuFileExportImage.setText("Export Image...");
        jMenuFileExportImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
        jMenuFileExportImage.setMnemonic('E');
        jMenuFileExportImage.setActionCommand("ExportImage");
        jMenuFileExportImage.addActionListener(this);

        jMenuFileExportSnps.setToolTipText("Export a tabular listing of polymorphic sites and their locations site");
    	jMenuFileExportSnps.setVisible(true);
    	jMenuFileExportSnps.setText("Export SNPs");
    	jMenuFileExportSnps.setMnemonic('p');
    	jMenuFileExportSnps.setActionCommand("ExportSNPs");
        jMenuFileExportSnps.addActionListener(this);

    	jMenuFileExportSpas.setToolTipText("Export a listing of segmental presence/absence patterns -- large indels");
    	jMenuFileExportSpas.setVisible(true);
    	jMenuFileExportSpas.setText("Export Islands");
    	jMenuFileExportSpas.setMnemonic('i');
    	jMenuFileExportSpas.setActionCommand("ExportIslands");
    	jMenuFileExportSpas.addActionListener(this);

    	jMenuFileExportOrthologs.setToolTipText("Export annotated 1-to-1 orthologs");
    	jMenuFileExportOrthologs.setVisible(true);
    	jMenuFileExportOrthologs.setText("Export Orthologs");
    	jMenuFileExportOrthologs.setMnemonic('i');
    	jMenuFileExportOrthologs.setActionCommand("ExportOrthologs");
    	jMenuFileExportOrthologs.addActionListener(this);

    	jMenuFileExportPermutation.setToolTipText("Export a signed gene-order permutation matrix");
    	jMenuFileExportPermutation.setVisible(true);
    	jMenuFileExportPermutation.setText("Export Permutation");
    	jMenuFileExportPermutation.setMnemonic('P');
    	jMenuFileExportPermutation.setActionCommand("ExportPermutation");
    	jMenuFileExportPermutation.addActionListener(this);

        jMenuFileExportImage.setEnabled(false);
		jMenuFileExportSnps.setEnabled(false);
		jMenuFileExportSpas.setEnabled(false);
		jMenuFileExportPermutation.setEnabled(false);
		jMenuFileExportOrthologs.setEnabled(false);
		
        add(jMenuFileExportImage);
        add(jMenuFileExportSnps);
//        add(jMenuFileExportSpas);
        add(jMenuFileExportPermutation);
        add(jMenuFileExportOrthologs);

    }
    
    public void setTarget(BaseViewerModel model, RearrangementPanel rrpanel)
    {
    	this.model = model;
    	this.rrpanel = rrpanel;
    	if(model instanceof XmfaViewerModel)
    	{
    		if(((XmfaViewerModel)model).getBackboneList()!=null)
    		{
    			jMenuFileExportOrthologs.setEnabled(true);
    			jMenuFileExportSpas.setEnabled(true);
    			jMenuFileExportSnps.setEnabled(true);
    		}
       		jMenuFileExportPermutation.setEnabled(true);
    	}else{
			jMenuFileExportSnps.setEnabled(false);
			jMenuFileExportSpas.setEnabled(false);
       		jMenuFileExportPermutation.setEnabled(false);
			jMenuFileExportOrthologs.setEnabled(false);
    	}
        jMenuFileExportImage.setEnabled(true);
    }
    
	public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("ExportImage"))
        {
            ExportFrame exportFrame = new ExportFrame(rrpanel);
            exportFrame.setVisible(true);
        }
        if (e.getActionCommand().equals("ExportOrthologs"))
        {
        	XmfaViewerModel xvm = (XmfaViewerModel)model;
        	OneToOneOrthologExporter.ExportFrame pef = new OneToOneOrthologExporter.ExportFrame(xvm);
        }
        if (e.getActionCommand().equals("ExportSNPs"))
        {
        	JFileChooser fc = new JFileChooser();
        	fc.setDialogTitle("Export SNP file to...");
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
            {
            	try{
            	BufferedWriter bw = new BufferedWriter( new FileWriter(fc.getSelectedFile()));
            	XmfaViewerModel xvm = (XmfaViewerModel)model;
    			SnpExporter.export(xvm, xvm.getXmfa(), bw);
    			bw.flush();
    			bw.close();
            	}catch(IOException ioe){ioe.printStackTrace();}
            }
        }
        if (e.getActionCommand().equals("ExportPermutation"))
        {
        	XmfaViewerModel xvm = (XmfaViewerModel)model;
        	PermutationExporter.ExportFrame pef = new PermutationExporter.ExportFrame(xvm);
        }
		
	}

}
