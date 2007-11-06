package org.gel.mauve.color;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import org.gel.mauve.BaseViewerModel;
import org.gel.mauve.LcbViewerModel;
import org.gel.mauve.XmfaViewerModel;

public class ColorMenu extends JMenu implements ActionListener {

	BaseViewerModel model;
	
	JRadioButtonMenuItem bbLcbColor = new JRadioButtonMenuItem("LCB color");
	JRadioButtonMenuItem bbMultiplicityColor = new JRadioButtonMenuItem("Backbone color");
	JRadioButtonMenuItem LcbColor = new JRadioButtonMenuItem("LCB color");
	JRadioButtonMenuItem OffsetColor = new JRadioButtonMenuItem("Offset color");
	JRadioButtonMenuItem NormalizedOffsetColor = new JRadioButtonMenuItem("Normalized offset color");
	JRadioButtonMenuItem MultiplicityColor = new JRadioButtonMenuItem("Multiplicity color");
	JRadioButtonMenuItem MultiplicityTypeColor = new JRadioButtonMenuItem("Multiplicity type color");
	JRadioButtonMenuItem NormalizedMultiplicityTypeColor = new JRadioButtonMenuItem("Normalized multiplicity type color");

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
		bbLcbColor.addActionListener(this);
		bbMultiplicityColor.addActionListener(this);
		LcbColor.addActionListener(this);
		OffsetColor.addActionListener(this);
		NormalizedOffsetColor.addActionListener(this);
		MultiplicityColor.addActionListener(this);
		MultiplicityTypeColor.addActionListener(this);
		NormalizedMultiplicityTypeColor.addActionListener(this);
	}
	
	public void build(BaseViewerModel model)
	{
		bg = new ButtonGroup();
        bg.add(bbLcbColor);
        bg.add(bbMultiplicityColor);
        bg.add(LcbColor);
        bg.add(OffsetColor);
        bg.add(NormalizedOffsetColor);
        bg.add(MultiplicityColor);
        bg.add(MultiplicityTypeColor);
        bg.add(NormalizedMultiplicityTypeColor);

        this.removeAll();

        this.model = model;
        if (model instanceof XmfaViewerModel)
        {
        	boolean have_bb = ((XmfaViewerModel)model).getBackboneList() != null;
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
        		add(LcbColor);
        	add(OffsetColor);
        	add(NormalizedOffsetColor);
        	add(MultiplicityColor);
        	add(MultiplicityTypeColor);

    		if (model.getSequenceCount() < 62)
        		add(NormalizedMultiplicityTypeColor);

        	if(model instanceof LcbViewerModel)
        		LcbColor.setSelected(true);
        	else
        		OffsetColor.setSelected(true);
        }
		
	}

	public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == bbLcbColor)
            model.setColorScheme(backboneLcbColor);
        if (e.getSource() == bbMultiplicityColor)
            model.setColorScheme(backboneMultiplicityColor);
        if (e.getSource() == LcbColor)
            model.setColorScheme(lcbColorScheme);
        if (e.getSource() == OffsetColor)
            model.setColorScheme(offsetColorScheme);
        if (e.getSource() == NormalizedOffsetColor)
            model.setColorScheme(normalizedOffsetColorScheme);
        if (e.getSource() == MultiplicityColor)
            model.setColorScheme(multiplicityColorScheme);
        if (e.getSource() == MultiplicityTypeColor)
            model.setColorScheme(multiplicityTypeColorScheme);
        if (e.getSource() == NormalizedMultiplicityTypeColor)
            model.setColorScheme(normalizedMultiplicityTypeColorScheme);
    }
}
