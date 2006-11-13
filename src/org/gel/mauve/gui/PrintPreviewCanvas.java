package org.gel.mauve.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JPanel;

public class PrintPreviewCanvas extends JPanel
{
	private Book book;
	private int currentPage;
	
	public PrintPreviewCanvas(Book b)
	{
		book = b;
		currentPage = 0;
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		PageFormat pageFormat = book.getPageFormat(currentPage);
		
		double xoff;
		double yoff;
		double scale;
		
		double px = pageFormat.getWidth();
		double py = pageFormat.getHeight();
		double sx = getWidth() - 1;
		double sy = getHeight() - 1;
		
		if (px / py < sx / sy) // center horizontally
		{
			scale = sy / py;
			xoff = 0.5 * (sx - scale * px);
			yoff = 0;
		}
		else // Center vertically
		{
			scale = sx / px;
			xoff = 0;
			yoff = 0.5 * (sy - scale * py);
		}
		
		g2.translate((float) xoff, (float) yoff);
		g2.scale((float) scale, (float) scale);
		
		Rectangle2D page = new Rectangle2D.Double(0, 0, px, py);
		g2.setPaint(Color.WHITE);
		g2.fill(page);
		g2.setPaint(Color.BLACK);
		g2.draw(page);
		
		Printable printable = book.getPrintable(currentPage);
		try
		{
			printable.print(g2, pageFormat, currentPage);
		}
		catch (PrinterException exception)
		{
			g2.draw(new Line2D.Double(0, 0, px, py));
			g2.draw(new Line2D.Double(0, px, 0, py));
		}
			
	}

	public void flipPage(int by)
	{
		int newPage = currentPage + by;
		if (0 <= newPage && newPage < book.getNumberOfPages())
		{
			currentPage = newPage;
			repaint();
		}
	}
}
