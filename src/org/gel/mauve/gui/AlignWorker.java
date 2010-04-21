package org.gel.mauve.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.gel.mauve.MyConsole;


public class AlignWorker extends SwingWorker
{
    String[] mauve_cmd;
    AlignmentProcessListener align_listener;
    int retcode = -1;
    private Process align_proc = null;
    private boolean killed = false;
    
    private PrintStream out;
    private PrintStream err;
    
    public AlignWorker (AlignmentProcessListener align_listener, String[] mauve_cmd)
    {
    	this(align_listener,mauve_cmd,true);
    }
    
    /**
     * 
     * @param align_listener
     * @param mauve_cmd
     * @param verbose true if redirect progressiveMauve output, false if suppress
     */
    public AlignWorker (AlignmentProcessListener align_listener, String[] mauve_cmd, boolean verbose){
    	this.mauve_cmd = mauve_cmd;
        this.align_listener = align_listener;
        if (verbose) {
        	this.out = MyConsole.out();
        	this.err = MyConsole.err();
        } else {
        	this.out = null;
        	this.err = null;
        }
    }
   
    public Object construct()
    {
        try
        {
            align_proc = Runtime.getRuntime().exec(mauve_cmd);

       //     OutStreamPrinter outP = new OutStreamPrinter(align_proc.getInputStream(), MyConsole.out());
        //    OutStreamPrinter errP = new OutStreamPrinter(align_proc.getErrorStream(), MyConsole.err());
            OutStreamPrinter outP = new OutStreamPrinter(align_proc.getInputStream(), out);
            OutStreamPrinter errP = new OutStreamPrinter(align_proc.getErrorStream(), err);
            
            errP.start();
            outP.start();
            
            try
            {
                retcode = align_proc.waitFor();
            }
            catch (InterruptedException e)
            {
            	if(!killed)
            		MyConsole.err().println("Interrupted.");
            }

        	if(!killed)
        	{
	            if (retcode == 0)
	            {
	                MyConsole.out().println("Completed without error.");
	            }
	            else
	            {
	                MyConsole.err().println("Exited with error code: " + retcode);
	            }
        	}
        }
        catch (IOException e)
        {
        	if(!killed)
        	{
	            MyConsole.err().println("Error running aligner.");
	            e.printStackTrace(MyConsole.err());
        	}
        }
        return new Integer(retcode);
    }
    
    public void finished()
    {
        align_listener.completeAlignment(retcode);
    }
    public void interrupt() {
    	killed = true;
    	align_proc.destroy();
    	align_proc = null;
    	super.interrupt();
    }
    public boolean getKilled()
    {
    	return killed;
    }
}


class OutStreamPrinter extends Thread
{
    InputStream in;
    PrintStream ps;
   
    OutStreamPrinter(InputStream in, PrintStream ps)
    {
        this.in = in;
        this.ps = ps;
    }
    
    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
            {
            	if(ps!=null)
            		ps.println(line);
            }
        } 
    	catch (IOException ioe)
    	{	
    	    ioe.printStackTrace(MyConsole.err());  
    	}
    }
}