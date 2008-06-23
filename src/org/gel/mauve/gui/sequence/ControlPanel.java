package org.gel.mauve.gui.sequence;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.Genome;
import org.gel.mauve.ModelEvent;
import org.gel.mauve.gui.MauveFrame;
import org.gel.mauve.gui.RearrangementPanel;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.border.AbstractBorder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;


public class ControlPanel extends AbstractSequencePanel implements org.gel.mauve.ModelListener {

    static ImageIcon up_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/Up16.gif"));
    static ImageIcon upRollover_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/UpRollover16.png"));
    static ImageIcon down_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/Down16.gif"));
    static ImageIcon downRollover_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/DownRollover16.png"));
    static ImageIcon aPlus_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/aPlus16.png"));
    static ImageIcon r_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/r16.png"));
    static ImageIcon plus_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/plus16.png"));
    static ImageIcon plusRollover_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/plusRollover16.png"));
    static ImageIcon minus_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/minus16.png"));
    static ImageIcon minusRollover_button_icon = new ImageIcon(MauveFrame.class.getResource("/images/minusRollover16.png"));
	JButton moveUpButton;
	JButton moveDownButton;
	JButton plusMinusButton;
	JToggleButton setReferenceButton;
	JButton loadFeaturesButton;
	final BaseViewerModel model;
	final Genome genome;
	final RearrangementPanel rrPanel;
	Color defaultColor;
    public ControlPanel(BaseViewerModel m, Genome g, RearrangementPanel rrpanel)
    {
    	super(m, g);
    	this.model = m;
    	this.genome = g;
    	this.rrPanel = rrpanel;
    	
    	model.addModelListener(this);        

    	Dimension buttonSize = new Dimension(16,16);

        moveUpButton = new JButton(up_button_icon);
        moveUpButton.setAction(new javax.swing.AbstractAction(){
    		public void actionPerformed(java.awt.event.ActionEvent ae)
    		{
	            int[] new_order = new int[model.getSequenceCount()];
	            for(int seqI = 0; seqI < model.getSequenceCount(); seqI++)
	            	new_order[seqI] = seqI;
	            new_order[genome.getViewIndex()] = new_order[genome.getViewIndex()-1];
	            new_order[genome.getViewIndex()-1] = genome.getViewIndex();
	            rrPanel.reorderSequences(new_order);
    		}
    	});
        moveUpButton.setIcon(up_button_icon);
        moveUpButton.setRolloverIcon(upRollover_button_icon);
        moveUpButton.setRolloverEnabled(true);
        moveUpButton.setBorderPainted(false);
    	moveUpButton.setToolTipText("Move this sequence up");
    	moveUpButton.setContentAreaFilled(false);
        moveUpButton.setEnabled(genome.getViewIndex() != 0);
        moveUpButton.setPreferredSize(buttonSize);
        moveUpButton.setSize(buttonSize);
        moveUpButton.setMaximumSize(buttonSize);
        moveUpButton.setBorder(BorderFactory.createEmptyBorder());
        

        plusMinusButton = new JButton();
        plusMinusButton.setAction(new javax.swing.AbstractAction(){
    		public void actionPerformed(java.awt.event.ActionEvent ae)
    		{
    			model.setVisible(genome, !genome.getVisible());
    		}
    	});
        plusMinusButton.setIcon(minus_button_icon);
        plusMinusButton.setRolloverIcon(minusRollover_button_icon);
        plusMinusButton.setRolloverEnabled(true);
        plusMinusButton.setBorderPainted(false);
    	plusMinusButton.setToolTipText("Hide this sequence from the display");
    	plusMinusButton.setContentAreaFilled(false);
        plusMinusButton.setPreferredSize(buttonSize);
        plusMinusButton.setSize(buttonSize);
        plusMinusButton.setMaximumSize(buttonSize);
        plusMinusButton.setBorder(BorderFactory.createEmptyBorder());
        
        setReferenceButton = new JToggleButton(r_button_icon);
    	setReferenceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
        		model.setReference(genome);
            }});
    	setReferenceButton.setToolTipText("Set reference genome");
    	setReferenceButton.setActionCommand("SetReference");
    	setReferenceButton.setBorderPainted(false);
    	setReferenceButton.setBackground(new java.awt.Color(255,255,255,0));
//    	setReferenceButton.setContentAreaFilled(false);
    	setReferenceButton.setSelected(model.getReference() == genome);
    	setReferenceButton.setRolloverEnabled(true);
    	setReferenceButton.setSize(buttonSize);
    	setReferenceButton.setMaximumSize(buttonSize);
    	setReferenceButton.setBorder(BorderFactory.createEmptyBorder());

        loadFeaturesButton = new JButton(aPlus_button_icon);
        loadFeaturesButton.setRolloverEnabled(true);
        loadFeaturesButton.setToolTipText("Load sequence feature data");
        loadFeaturesButton.setContentAreaFilled(false);
        loadFeaturesButton.setBorderPainted(false);
        loadFeaturesButton.setActionCommand("LoadFeatures");
        loadFeaturesButton.setSize(buttonSize);
        loadFeaturesButton.setMaximumSize(buttonSize);
        loadFeaturesButton.setBorder(BorderFactory.createEmptyBorder());

        moveDownButton = new JButton();
    	moveDownButton.setAction(new javax.swing.AbstractAction(){
    		public void actionPerformed(java.awt.event.ActionEvent ae)
    		{
	            int[] new_order = new int[model.getSequenceCount()];
	            for(int seqI = 0; seqI < model.getSequenceCount(); seqI++)
	            	new_order[seqI] = seqI;
	            new_order[genome.getViewIndex()] = new_order[genome.getViewIndex()+1];
	            new_order[genome.getViewIndex()+1] = genome.getViewIndex();
	            rrPanel.reorderSequences(new_order);
    		}
    	});
    	moveDownButton.setIcon(down_button_icon);
    	moveDownButton.setRolloverIcon(downRollover_button_icon);
    	moveDownButton.setToolTipText("Move this sequence down");
    	moveDownButton.setActionCommand("MoveDown");
    	moveDownButton.setContentAreaFilled(false);
    	moveDownButton.setBorderPainted(false);
        moveDownButton.setRolloverEnabled(true);
        moveDownButton.setSize(buttonSize);
        moveDownButton.setMaximumSize(buttonSize);
        moveDownButton.setEnabled(genome.getViewIndex() != (model.getSequenceCount()-1));
        moveDownButton.setBorder(BorderFactory.createEmptyBorder());
        
        if(genome.getVisible())
        	doVisibleLayout();
        else
        	doInvisibleLayout();
        
        Dimension minSize = this.getSize();
        minSize.height = 14*4;	// 16 for each button
        minSize.width = 18;
        this.setMinimumSize(minSize);
    
        Dimension prefSize = this.getSize();
        prefSize.height = 20*4;	// 20 for each button
        prefSize.width = 18;
        this.setPreferredSize(prefSize);
        
        defaultColor = getBackground();
        if(genome.getViewIndex() % 2 == 0)
        	setBackground(java.awt.Color.WHITE);
    }
    
    private void doVisibleLayout()
    {
    	setLayout(null);
    	removeAll();
        GridBagLayout layoutManager = new GridBagLayout();
        setLayout(layoutManager);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.gridx = 0;
        c.insets = new Insets(0, 0, 0, 0);
        c.ipadx = 0;
        c.ipady = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.gridy = GridBagConstraints.RELATIVE;
    	
    	add(moveUpButton);
        layoutManager.setConstraints(moveUpButton, c);
    	add(plusMinusButton);
        layoutManager.setConstraints(plusMinusButton, c);
        add(setReferenceButton);
        layoutManager.setConstraints(setReferenceButton, c);
        add(loadFeaturesButton);
        layoutManager.setConstraints(loadFeaturesButton, c);
        add(moveDownButton);
        layoutManager.setConstraints(moveDownButton, c);

        plusMinusButton.setIcon(minus_button_icon);
        plusMinusButton.setRolloverIcon(minusRollover_button_icon);
    	plusMinusButton.setToolTipText("Hide this sequence from the display");
        invalidate();
        validate();
        revalidate();
    }
    
    private void doInvisibleLayout()
    {
    	setLayout(null);
    	removeAll();
    	GridBagLayout layoutManager = new GridBagLayout();
        setLayout(layoutManager);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.gridx = 0;
        c.insets = new Insets(0, 0, 0, 0);
        c.ipadx = 0;
        c.ipady = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.gridy = GridBagConstraints.RELATIVE;
    	
    	add(plusMinusButton);
        layoutManager.setConstraints(plusMinusButton, c);

        plusMinusButton.setIcon(plus_button_icon);
        plusMinusButton.setRolloverIcon(plusRollover_button_icon);
    	plusMinusButton.setToolTipText("Show this sequence in the display");
        invalidate();
        validate();
        revalidate();
    }

    public void genomesReordered(org.gel.mauve.ModelEvent me)
    {
        moveUpButton.setEnabled(genome.getViewIndex() != 0);
        moveDownButton.setEnabled(genome.getViewIndex() != (model.getSequenceCount()-1));
        if(genome.getViewIndex() % 2 == 0)
        	setBackground(java.awt.Color.WHITE);
        else
        	setBackground(defaultColor);
    }
    
    public void referenceChanged(org.gel.mauve.ModelEvent me)
    {
    	setReferenceButton.setSelected(model.getReference() == genome);
    }
    
    public void genomeVisibilityChanged(ModelEvent event) 
    {
    	if(genome.getVisible()==true)
    		doVisibleLayout();
    	else
    		doInvisibleLayout();

    }
}
