package org.gel.mauve;

import gr.zeus.ui.JConsole;

import java.io.PrintStream;

public class MyConsole
{
    private static boolean useSwing = false;
    private static JConsole console;
    
    public static void setUseSwing(boolean b)
    {
        if (b && !useSwing)
        {
            console = JConsole.getConsole();
            console.setTitle("Mauve Console");
            console.setSize(400,400);
            console.startConsole();
        }
        else if (!b && useSwing)
        {
            console.stopConsole();
            console = null;
        }
        
        useSwing = b;
    }
    public static void showConsole()
    {
        if (useSwing)
        {
            console.showConsole();
        }
    }
    public static PrintStream err()
    {
        if (useSwing)
        {
            console.showConsole();
        }
        return System.err;
    }

    public static PrintStream out()
    {
        return System.out;
    }
}