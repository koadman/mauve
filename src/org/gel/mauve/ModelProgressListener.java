package org.gel.mauve;


public interface ModelProgressListener
{
    void buildStart();
    void downloadStart();
    void alignmentStart();
    void alignmentEnd(int sequenceCount);
    void featureStart(int sequenceIndex);
    void done();
}
