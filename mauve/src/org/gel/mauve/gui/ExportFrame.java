package org.gel.mauve.gui;

import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import org.gel.mauve.MyConsole;


public class ExportFrame extends JFrame
{
    private final static DecimalFormat FORMAT = new DecimalFormat("##########");
    
    private RearrangementPanel rrpanel;
    private JTextField outputFile = new JTextField();
    private JFileChooser fc = new JFileChooser();
    private JComboBox formatSelector = new JComboBox();

    private double scale = 1.0;
    private JFormattedTextField widthBox = new JFormattedTextField(FORMAT);
    private JFormattedTextField heightBox = new JFormattedTextField(FORMAT);
    private boolean scaleChanging = false;

    private float jpegQuality = 0.75f;
    private ButtonGroup qualityGroup = new ButtonGroup();
    
    public ExportFrame(RearrangementPanel rrpanel)
    {
        this.rrpanel = rrpanel;
        setSize(300,300);
        
        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        setTitle("Mauve Image Export");

        c.insets = new Insets(2,2,2,2);

        // Format label.
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        getContentPane().add(new JLabel("Format:"), c);
        
        // Format selector.
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        formatSelector.addItem("JPEG");
        formatSelector.addItem("PNG");
        formatSelector.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        String format = (String) formatSelector.getSelectedItem();
                        
                        if (format.equals("JPEG"))
                        {
                            Enumeration buttons = ExportFrame.this.qualityGroup.getElements();
                            while (buttons.hasMoreElements())
                            {
                                AbstractButton b = (AbstractButton) buttons.nextElement();
                                b.setEnabled(true);
                            }
                        }
                        else
                        {
                            Enumeration buttons = ExportFrame.this.qualityGroup.getElements();
                            while (buttons.hasMoreElements())
                            {
                                AbstractButton b = (AbstractButton) buttons.nextElement();
                                b.setEnabled(false);
                            }
                        }
                    }
                }
        );	
        getContentPane().add(formatSelector, c);

        // Image size label.
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        getContentPane().add(new JLabel("Image size:"), c);
        
        // Image size boxes.
        JPanel scalePanel = new JPanel();
        scalePanel.setLayout(new GridBagLayout());
        GridBagConstraints c2 = new GridBagConstraints();
        
        // Width label
        c2.gridx = 0;
        c2.gridy = 0;
        scalePanel.add(new JLabel("Width:"), c2);
        
        // Width box
        c2.gridx = 1;
        c2.weightx = 1;
        c2.insets = new Insets(0,0,0,4);
        c2.fill = GridBagConstraints.HORIZONTAL;
        scalePanel.add(widthBox,c2);
        widthBox.setValue(new Integer(rrpanel.getWidth()));
        widthBox.getDocument().addUndoableEditListener(new UndoableEditListener()
                {
                    public void undoableEditHappened(UndoableEditEvent evt)
                    {
                        try
                        {
                            Number n = FORMAT.parse(widthBox.getText());
                            scaleWidth(n.intValue());
                        }
                        catch (ParseException e)
                        {
                            // Invalid value.
                            return;
                        }
                    }
                }
        );

        // Height label
        c2.gridx = 2;
        c2.weightx = 0;
        c2.fill = GridBagConstraints.NONE;
        c2.insets = new Insets(0,0,0,0);
        scalePanel.add(new JLabel("Height:"),c2);

        // Height box
        c2.gridx = 3;
        c2.weightx = 1;
        c2.fill = GridBagConstraints.HORIZONTAL;
        scalePanel.add(heightBox,c2);
        heightBox.setEditable(false);
        heightBox.setValue(new Integer(rrpanel.getHeight()));
        
        // Adding scale panel.
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(scalePanel, c);

        // Quality label.
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.weighty = 0;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;

        getContentPane().add(new JLabel("Quality:"), c);
        
        // Quality options

        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        
        
        JRadioButton lowButton = new JRadioButton("Low", false);
        lowButton.addActionListener(new ActionListener()
                {

                    public void actionPerformed(ActionEvent e)
                    {
                        jpegQuality = 0.25f;
                    }
            
                }
        );
        JRadioButton mediumButton = new JRadioButton("Medium", false);
        mediumButton.addActionListener(new ActionListener()
                {

                    public void actionPerformed(ActionEvent e)
                    {
                        jpegQuality = 0.5f;
                    }
            
                }
        );
        JRadioButton highButton = new JRadioButton("High", true);
        highButton.addActionListener(new ActionListener()
                {

                    public void actionPerformed(ActionEvent e)
                    {
                        jpegQuality = 0.75f;
                    }
            
                }
        );
        JRadioButton maximumButton = new JRadioButton("Maximum", false);
        maximumButton.addActionListener(new ActionListener()
                {

                    public void actionPerformed(ActionEvent e)
                    {
                        jpegQuality = 1;
                    }
            
                }
        );

        qualityGroup.add(lowButton);
        qualityGroup.add(mediumButton);
        qualityGroup.add(highButton);
        qualityGroup.add(maximumButton);
        
        getContentPane().add(lowButton, c);
        c.gridy = 3;
        getContentPane().add(mediumButton,c);
        c.gridy = 4;
        getContentPane().add(highButton,c);
        c.gridy = 5;
        getContentPane().add(maximumButton,c);
        
        // File label.
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.SOUTHEAST;
        c.weighty = 0;
        getContentPane().add(new JLabel("Output file:"), c);
        
        // File text box
        c.gridx = 1;
        c.gridy = 6;
        c.gridwidth = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.SOUTHWEST;
        c.weightx = 1;
        getContentPane().add(outputFile, c);
        
        // File browse button.
        JButton fileButton = new JButton("Browse...");
        fileButton.addActionListener(new ActionListener()
                {

                    public void actionPerformed(ActionEvent e)
                    {
                        int ret = fc.showDialog(ExportFrame.this, "Select");
                        if (ret == JFileChooser.APPROVE_OPTION)
                        {
                            File f = fc.getSelectedFile();
                            outputFile.setText(f.getAbsolutePath());
                        }
                    }
                }
        );
        c.gridx = 2;
        c.gridy = 6;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.SOUTHWEST;
        c.weightx = 0;
        getContentPane().add(fileButton, c);
        
        // Export button.
        JPanel buttonPanel = new JPanel();
        
        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(new ActionListener()
                {
        
                    public void actionPerformed(ActionEvent e)
                    {
                        doExport();
                    }
            
                }
        );
        
        buttonPanel.add(exportButton);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener()
                {
        
                    public void actionPerformed(ActionEvent e)
                    {
                        setVisible(false);
                    }
                }
        );
        
        buttonPanel.add(cancelButton);
        
        c.gridx = 0;
        c.gridy = 7;
        c.gridwidth = 3;
        c.weighty = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.SOUTHEAST;

        getContentPane().add(buttonPanel, c);
    }
    
    private void doExport()
    {
        String format = (String) formatSelector.getSelectedItem();
    	File f = getFileWithExtension(outputFile.getText(), format);
        export(f, scale, format);
    }
    
    private File getFileWithExtension(String fileName, String format)
    {
    	String lastFour = "";
    	String lastFive = "";
    	if(fileName.length() >= 4)
    		lastFour = fileName.substring(fileName.length()-4);
    	if(fileName.length() >= 5)
    		lastFive = fileName.substring(fileName.length()-5);
    	if(format.equalsIgnoreCase("JPEG"))
    	{
    		if(!lastFive.equalsIgnoreCase(".jpeg") && 
    				!lastFour.equalsIgnoreCase(".jpg") )
    			fileName += ".jpg";
    	}else if(format.equalsIgnoreCase("PNG"))
    	{
    		if(	!lastFour.equalsIgnoreCase(".png") )
    			fileName += ".png";
    	}

    	return new File(fileName);
    }
    private void export(File outputFile, double scale, String formatName)
    {
        if (outputFile.exists())
        {
            int result = JOptionPane.showConfirmDialog(this, "The file " + outputFile + " already exists.  Overwrite?", "File exists", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION)
            {
                return;
            }
        }
        
        BufferedImage img = new BufferedImage((int) (rrpanel.getWidth() * scale), (int) (rrpanel.getHeight() * scale), BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2 = (Graphics2D) img.getGraphics();
        
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.scale(scale, scale);
        
        g2.setRenderingHint(MauveRenderingHints.KEY_SIMILARITY_DENSITY, new Double(1.0 / scale));
        rrpanel.paint(g2);

        
        
        if (formatName.equals("JPEG"))
        {
            try
            {
                writeJpeg(outputFile, img);
            }
            catch (IOException e)
            {
    			JOptionPane.showMessageDialog(this, "Error writing file.", "Error Writing File", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return;
            }        
        }
        else
        {
            try
            {
                ImageIO.write(img, formatName, outputFile);
            }
            catch (IOException e)
            {
    			JOptionPane.showMessageDialog(this, "The selected file could not be written.  Please choose another.", "Error Writing File", JOptionPane.ERROR_MESSAGE);
    			MyConsole.err().println(e);
    			return;
            }
        }
        
        setVisible(false);
    }
    
    
    /**
     * @param outputFile
     * @param img
     * @throws IOException
     */
    private void writeJpeg(File outputFile, BufferedImage img) throws IOException
    {
        Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
        if (!iter.hasNext())
        {
            throw new RuntimeException("Couldn't find jpeg image writer.");
        }
        
        ImageWriter writer = (ImageWriter)iter.next();

        // Prepare output file
        ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile);
        writer.setOutput(ios);

        // Set the compression quality
        JPEGImageWriteParam param = new JPEGImageWriteParam(Locale.getDefault());
        param.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT) ;
        param.setCompressionQuality(jpegQuality);

        // Write the image
        writer.write(null, new IIOImage(img, null, null), param);

        // Cleanup
        ios.flush();
        writer.dispose();
        ios.close();
    }

    private void scaleWidth(int width)
    {
        scale = width / (double) rrpanel.getWidth(); 
        heightBox.setValue(new Integer((int) (rrpanel.getHeight() * scale)));
    }
    
}
