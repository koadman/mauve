package org.gel.mauve.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.gel.mauve.MyConsole;

/**
 * @author darling
 * 
 * Show a splash screen composed of an image and a message Code adapted from
 * SplashWindow3 at
 * http://www.javaworld.com/javaworld/javatips/jw-javatip104.html
 *  
 */
class SplashScreen extends JWindow
{
    public SplashScreen(String filename, String message, Frame f, int waitTime)
    {
        super(f);
        JPanel bg_panel = new JPanel();
        Border panel_border = BorderFactory.createLineBorder(Color.gray, 4);
        bg_panel.setBackground(Color.white);
        bg_panel.setBorder(panel_border);
        getContentPane().add(bg_panel);
        JLabel il = new JLabel(new ImageIcon(SplashScreen.class.getResource(filename)));
        JLabel tl = new JLabel(message, SwingConstants.CENTER);
        bg_panel.setLayout(new BorderLayout());
        bg_panel.add(il, BorderLayout.NORTH);
        bg_panel.add(tl, BorderLayout.SOUTH);
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = il.getPreferredSize();
        setLocation(screenSize.width / 2 - (labelSize.width / 2), screenSize.height / 2 - (labelSize.height / 2));

        addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                setVisible(false);
                dispose();
            }
        });
        final int pause = waitTime;
        final Runnable closerRunner = new Runnable()
        {
            public void run()
            {
                setVisible(false);
                dispose();
            }
        };

        Runnable waitRunner = new Runnable()
        {
            public void run()
            {
                try
                {
                    Thread.sleep(pause);
                }
                catch (InterruptedException e)
                {
                    MyConsole.err().print("Interrupted waiting for splash start.");
                    e.printStackTrace(MyConsole.err());
                }

                try
                {
                    SwingUtilities.invokeAndWait(closerRunner);
                }
                catch (InterruptedException e)
                {
                    MyConsole.err().print("Interrupted splash screen.");
                    e.printStackTrace(MyConsole.err());
                }
                catch (InvocationTargetException e)
                {
                    MyConsole.err().print("Error in splash screen invocation.");
                    e.printStackTrace(MyConsole.err());
                }
            }
        };
        setVisible(true);
        Thread splashThread = new Thread(waitRunner, "SplashThread");
        splashThread.start();
    }
}