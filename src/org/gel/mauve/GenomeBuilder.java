package org.gel.mauve;

import java.io.File;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.ComponentFeature;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.io.SimpleAssemblyBuilder;
import org.biojava.bio.symbol.RangeLocation;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.gel.mauve.format.FastaFormat;
import org.gel.mauve.format.FileFinder;
import org.gel.mauve.format.SupportedFormatFactory;

public class GenomeBuilder
{
    public static final String MAUVE_AGGREGATE = "MauveAggregation";

    private GenomeBuilder()
    {
        // Don't allow an object to be created.
    }

    public static Genome buildGenome(int sequenceIndex, XmfaViewerModel model)
    {
        int adjustedIndex = sequenceIndex + 1;

        long length = model.getXmfa().seq_length[sequenceIndex];
        String annotationFilename = model.getXmfa().getName(sequenceIndex);
        SupportedFormat annotationFormat = SupportedFormatFactory.guessFormatFromFilename(annotationFilename);
        int restrictedIndex = -1;

        Properties meta = model.getXmfa().metadata;
        // See if we are in a file with multisequences here
        if (meta.containsKey("Sequence" + adjustedIndex + "Entry"))
        {
            annotationFilename = meta.getProperty("Sequence" + adjustedIndex + "File");
            annotationFormat = SupportedFormatFactory.formatNameToFormat(meta.getProperty("Sequence" + adjustedIndex + "Format"));
            restrictedIndex = Integer.parseInt(meta.getProperty("Sequence" + adjustedIndex + "Entry"));
        }

        // See if there is a better file to use anyway,
        if (meta.containsKey("Annotation" + adjustedIndex + "File"))
        {
            annotationFilename = meta.getProperty("Annotation" + adjustedIndex + "File");
            annotationFormat = SupportedFormatFactory.formatNameToFormat(meta.getProperty("Annotation" + adjustedIndex + "Format"));
        }
        else if (meta.containsKey("Sequence" + adjustedIndex + "File"))
        {
            annotationFilename = meta.getProperty("Sequence" + adjustedIndex + "File");
            annotationFormat = SupportedFormatFactory.formatNameToFormat(meta.getProperty("Sequence" + adjustedIndex + "Format"));
        }

        return buildGenome(length, annotationFilename, annotationFormat, model, restrictedIndex, sequenceIndex);
    }

    public static Genome buildGenome(long length, String annotationFilename, BaseViewerModel model, int sequenceIndex)
    {
        return buildGenome(length, annotationFilename, SupportedFormatFactory.guessFormatFromFilename(annotationFilename), model, -1, sequenceIndex);
    }


    private static Genome buildGenome(long length, String annotationFilename, SupportedFormat annotationFormat, BaseViewerModel model, int restrictedIndex, int sequenceIndex)
    {    	
    	String fname = FileFinder.findFile(model, annotationFilename);
        File f = new File(fname);
        return buildGenome(length, f, annotationFormat, model, restrictedIndex, sequenceIndex);
    }

    // Left package-visible for testing.
    static Genome buildGenome(long length, File annotationFile, SupportedFormat annotationFormat, BaseViewerModel model, int restrictedIndex, int sequenceIndex)
    {
        Genome g = new Genome(length, model, sequenceIndex);

        // By default, the displayname is the annotation file name
        // and there is a single non-circular chromosome that is the length of the file.
        g.setDisplayName(annotationFile.getName());
        ArrayList chromo = new ArrayList();
        chromo.add(new Chromosome(1, length, "[1," + length + "]", false));
        g.setChromosomes(chromo);

        // Construct the sequence Assembly.
        SimpleAssemblyBuilder b = new SimpleAssemblyBuilder();
        ComponentFeature.Template cft = new ComponentFeature.Template();
        cft.type = MAUVE_AGGREGATE;
        cft.strand = StrandedFeature.POSITIVE;
        int start = 1;

        // We will also build chromosome list simultaneously.
        chromo = new ArrayList();

        if (!annotationFile.exists())
        {
            return g;
        }

        // Use the format to read the file; this most likely will create
        // an iterator of delegating sequences.
        SequenceIterator seqi = annotationFormat.makeIterator(annotationFile);

        if (!seqi.hasNext())
        {
            // No sequences to read, so again a crippled genome.
            return g;
        }

        int counter = 1;
        while (seqi.hasNext())
        {
            Sequence s;
            try
            {
            	if(seqi instanceof RichSequenceIterator)
                    s = ((RichSequenceIterator)seqi).nextRichSequence();
            	else
            		s = seqi.nextSequence();
            }
            catch (NoSuchElementException e)
            {
                // This should never happen, so make it loud.
                throw new RuntimeException(e);
            }
            catch (BioException e)
            {
                MyConsole.err().println("Error reading file.");
                e.printStackTrace(MyConsole.err());
                return g;
            }

            if (restrictedIndex == -1 || counter == restrictedIndex)
            {
                // capture the genome name from the first sequence, if it
                // exists.
                if (start == 1 && !(annotationFormat instanceof FastaFormat && restrictedIndex == -1 ) )
                {
                    String tmpName = annotationFormat.getSequenceName(s);
                    if (tmpName != null)
                    {
                        g.setDisplayName(tmpName);
                    }
                }

                cft.componentSequence = s;
                cft.location = new RangeLocation(start, start + s.length() - 1);
                cft.componentLocation = new RangeLocation(1, s.length());
                try
                {
                    b.addComponentSequence(cft);
                }
                catch (ChangeVetoException e)
                {
                    // An unexpected exception, since we are building this.
                    throw new RuntimeException(e);
                }
                catch (BioException e)
                {
                    // An unexpected exception, since we are building this.
                    throw new RuntimeException(e);
                }

                // Create chromosomes.
                // Default chromosome name to location of segment.
                String chromoName = annotationFormat.getChromosomeName(s);
                if (chromoName == null)
                {
                    chromoName = cft.location.toString();
                }
                boolean circular = s.getAnnotation().containsProperty("CIRCULAR");
                chromo.add(new Chromosome(cft.location.getMin(), cft.location.getMax(), chromoName, circular));
                start += s.length();
            }
            counter++;
        }

        Sequence annotationSequence;
        try
        {
            annotationSequence = b.makeSequence();
        }
        catch (BioException e)
        {
            // An unexpected exeception, since we are building this.
            throw new RuntimeException(e);
        }

        // A simple validation; if lengths are not the same, the GUI will
        // break.
        if (annotationSequence.length() != length)
        {
            g.setDisplayName(annotationFile.getName());
        }
        else
        {
            g.setAnnotationSequence(annotationSequence, annotationFormat);
            g.setChromosomes(chromo);
        }
        return g;
    }

}
