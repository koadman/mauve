package org.gel.mauve.color;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.LcbViewerModel;
import org.gel.mauve.MauveAlignmentViewerModel;

public class ColorMenu extends JMenu implements ActionListener {

	BaseViewerModel model;
	
	JRadioButtonMenuItem bbLcbColor = new JRadioButtonMenuItem("LCB color");
	JRadioButtonMenuItem bbMultiplicityColor = new JRadioButtonMenuItem("Backbone color");
	JRadioButtonMenuItem lcbColor = new JRadioButtonMenuItem("LCB color");
	JRadioButtonMenuItem offsetColor = new JRadioButtonMenuItem("Offset color");
	JRadioButtonMenuItem normalizedOffsetColor = new JRadioButtonMenuItem("Normalized offset color");
	JRadioButtonMenuItem multiplicityColor = new JRadioButtonMenuItem("Multiplicity color");
	JRadioButtonMenuItem multiplicityTypeColor = new JRadioButtonMenuItem("Multiplicity type color");
	JRadioButtonMenuItem normalizedMultiplicityTypeColor = new JRadioButtonMenuItem("Normalized multiplicity type color");

    ButtonGroup bg = new ButtonGroup();

	BackboneLcbColor backboneLcbColor = new BackboneLcbColor();
	BackboneMultiplicityColor backboneMultiplicityColor = new BackboneMultiplicityColor();
	LCBColorScheme lcbColorScheme = new LCBColorScheme();
	MultiplicityColorScheme multiplicityColorScheme = new MultiplicityColorScheme();
	MultiplicityTypeColorScheme multiplicityTypeColorScheme = new MultiplicityTypeColorScheme();
	NormalizedMultiplicityTypeColorScheme normalizedMultiplicityTypeColorScheme = new NormalizedMultiplicityTypeColorScheme();
	NormalizedOffsetColorScheme normalizedOffsetColorScheme = new NormalizedOffsetColorScheme();
	OffsetColorScheme offsetColorScheme = new OffsetColorScheme();
	
	public ColorMenu()
	{
		bbLcbColor.setActionCommand(bbLcbColor.getText());
		bbMultiplicityColor.setActionCommand(bbMultiplicityColor.getText());
		lcbColor.setActionCommand(lcbColor.getText());
		offsetColor.setActionCommand(offsetColor.getText());
		normalizedOffsetColor.setActionCommand(normalizedOffsetColor.getText());
		multiplicityColor.setActionCommand(multiplicityColor.getText());
		multiplicityTypeColor.setActionCommand(multiplicityTypeColor.getText());
		normalizedMultiplicityTypeColor.setActionCommand(normalizedMultiplicityTypeColor.getText());
		bbLcbColor.addActionListener(this);
		bbMultiplicityColor.addActionListener(this);
		lcbColor.addActionListener(this);
		offsetColor.addActionListener(this);
		normalizedOffsetColor.addActionListener(this);
		multiplicityColor.addActionListener(this);
		multiplicityTypeColor.addActionListener(this);
		normalizedMultiplicityTypeColor.addActionListener(this);
	}
	
	public void build(BaseViewerModel model)
	{
		bg = new ButtonGroup();
        bg.add(bbLcbColor);
        bg.add(bbMultiplicityColor);
        bg.add(lcbColor);
        bg.add(offsetColor);
        bg.add(normalizedOffsetColor);
        bg.add(multiplicityColor);
        bg.add(multiplicityTypeColor);
        bg.add(normalizedMultiplicityTypeColor);

        this.removeAll();

        this.model = model;
        if (model instanceof MauveAlignmentViewerModel)
        {
        	boolean have_bb = ((MauveAlignmentViewerModel)model).getBackboneList() != null;
        	if(have_bb)
        	{
        		add(bbLcbColor);
        		add(bbMultiplicityColor);
        		bbLcbColor.setSelected(true);
        	}else{
        		setEnabled(false);
        		JRadioButtonMenuItem lcbColor = new JRadioButtonMenuItem("LCB color");
        		add(lcbColor);
        	}
        }
        else
        {
        	if(model instanceof LcbViewerModel)
        		add(lcbColor);
        	add(offsetColor);
        	add(normalizedOffsetColor);
        	add(multiplicityColor);
        	add(multiplicityTypeColor);

    		if (model.getSequenceCount() < 62)
        		add(normalizedMultiplicityTypeColor);

        	if(model instanceof LcbViewerModel)
        		lcbColor.setSelected(true);
        	else
        		offsetColor.setSelected(true);
        }
		
	}

	public void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand() == bbLcbColor.getText())
            model.setColorScheme(backboneLcbColor);
        if (e.getActionCommand() == bbMultiplicityColor.getText())
            model.setColorScheme(backboneMultiplicityColor);
        if (e.getActionCommand() == lcbColor.getText())
            model.setColorScheme(lcbColorScheme);
        if (e.getActionCommand() == offsetColor.getText())
            model.setColorScheme(offsetColorScheme);
        if (e.getActionCommand() == normalizedOffsetColor.getText())
            model.setColorScheme(normalizedOffsetColorScheme);
        if (e.getActionCommand() == multiplicityColor.getText())
            model.setColorScheme(multiplicityColorScheme);
        if (e.getActionCommand() == multiplicityTypeColor.getText())
            model.setColorScheme(multiplicityTypeColorScheme);
        if (e.getActionCommand() == normalizedMultiplicityTypeColor.getText())
            model.setColorScheme(normalizedMultiplicityTypeColorScheme);
    }
}
