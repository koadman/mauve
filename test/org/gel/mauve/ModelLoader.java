package org.gel.mauve;

import java.io.File;
import java.io.IOException;


public class ModelLoader
{

    public static void main(String[] args) throws IOException, MauveFormatException
    {
        
        String filename;
        if (args.length == 1)
        {
            filename = args[0];
        }
        else if (args.length == 2)
        {
            filename = args[1];
        }
        else
        {
            throw new RuntimeException("Required filename");
        }

        BaseViewerModel model = ModelBuilder.buildModel(new File(filename), null);
        
        if (args.length > 1)
        {
            synchronized(model)
            {
                try
                {
                    model.wait();
                } 
                catch (InterruptedException e)
                {
                }
            }
            
        }
    }
}
