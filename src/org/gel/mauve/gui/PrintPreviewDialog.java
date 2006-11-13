package org.gel.mauve.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;


public class PrintPreviewDialog extends JDialog
{
	private static final int HEIGHT = 570;
	private static final int WIDTH = 400;
	
	private PrintPreviewCanvas canvas;
	
	
	public PrintPreviewDialog(Printable p, PageFormat pf, int pages)
	{
		Book book = new Book();
		book.append(p, pf, pages);
		layoutUI(book);
	}
	
	public PrintPreviewDialog(Book b)
	{
		layoutUI(b);
	}

	public void layoutUI(Book b)
	{
		setSize(WIDTH, HEIGHT);
		setTitle("Mauve Print Preview");
		Container contentPane = getContentPane();
		canvas = new PrintPreviewCanvas(b);
		contentPane.add(canvas, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();

		JButton closeButton = new JButton("Close");
		buttonPanel.add(closeButton);
		closeButton.addActionListener(new
				ActionListener()
				{
					public void actionPerformed(ActionEvent evt)
					{
						setVisible(false);
					}
				});
		
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
	}
}
