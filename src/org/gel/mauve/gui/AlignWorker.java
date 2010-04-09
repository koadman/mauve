package org.gel.mauve.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.gel.mauve.MyConsole;


public class AlignWorker extends SwingWorker
{
    String[] mauve_cmd;
    AlignmentProcessListener align_frame;
    int retcode = -1;
    private Process align_proc = null;
    private boolean killed = false;
    
    public AlignWorker (AlignmentProcessListener align_frame, String[] mauve_cmd)
    {
        this.mauve_cmd = mauve_cmd;
        this.align_frame = align_frame;
    }
    
    public Object construct()
    {
        try
        {
            align_proc = Runtime.getRuntime().exec(mauve_cmd);

            OutStreamPrinter outP = new OutStreamPrinter(align_proc.getInputStream());
            ErrStreamPrinter errP = new ErrStreamPrinter(align_proc.getErrorStream());
            
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
        align_frame.completeAlignment(retcode);
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
   
    OutStreamPrinter(InputStream in)
    {
        this.in = in;
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
                MyConsole.out().println(line);
            }
        } 
    	catch (IOException ioe)
    	{	
    	    ioe.printStackTrace(MyConsole.err());  
    	}
    }
}

class ErrStreamPrinter extends Thread
{
    InputStream in;
    
     ErrStreamPrinter(InputStream in)
     {
         this.in = in;
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
                 MyConsole.err().println(line);
             }
         } 
     	catch (IOException ioe)
     	{	
     	    ioe.printStackTrace(MyConsole.err());  
     	}
     }
}