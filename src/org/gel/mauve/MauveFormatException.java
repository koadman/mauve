package org.gel.mauve;

public class MauveFormatException extends Exception
{
    public MauveFormatException(String msg)
    {
        super(msg);
    }

    public MauveFormatException(String msg, Exception e)
    {
        super(msg, e);
    }
}