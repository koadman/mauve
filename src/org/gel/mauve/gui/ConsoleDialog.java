package org.gel.mauve.gui;

import java.awt.Frame;
import java.awt.HeadlessException;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ConsoleDialog extends JDialog
{
    JTextArea text;

    public ConsoleDialog(Frame owner) throws HeadlessException
    {
        super(owner, "Mauve Console");

        initComponents();
    }

    private void initComponents()
    {
        setSize(400, 600);
        text = new JTextArea();
        JScrollPane sp = new JScrollPane(text);
        getContentPane().add(sp);
    }

    public void appendText(String s)
    {
        text.append(s);
        setVisible(true);
    }

}