package org.gel.mauve;

public class Chromosome
{
    private long start;
    private long end;
    private String name;
    boolean circular;

    public Chromosome(long start, long end, String name, boolean circular)
    {
        this.start = start;
        this.end = end;
        this.name = name;
        this.circular = circular;
    }

    public long getStart()
    {
        return start;
    }

    public long getEnd()
    {
        return end;
    }

    public String getName()
    {
        return name;
    }

    public long relativeLocation(long location)
    {
        return location - start + 1;
    }
    public boolean getCircular()
    {
    	return circular;
    }
}