package org.gel.mauve.format;

import java.io.File;

import java.io.FileNotFoundException;
import java.util.Set;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.Comment;
import org.biojavax.Namespace;
import org.biojavax.RankedCrossRef;
import org.biojavax.RankedDocRef;
import org.biojavax.RichAnnotation;
import org.biojavax.bio.BioEntryRelationship;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.taxa.NCBITaxon;
import org.gel.mauve.SupportedFormat;

public class RichDelegatingSequence extends DelegatingSequence implements
		RichSequence {
	
    public RichDelegatingSequence(Sequence s, SupportedFormat format, File source, int index) throws FileNotFoundException
    {
    	super(s,format,source,index);
    	richInit(s);
    }
    
    void richInit(Sequence s)
    {
    	RichSequence rs = (RichSequence)s;
    	featureSet = rs.getFeatureSet();
    	seqVersion = rs.getSeqVersion();
    	intSymList = rs.getInternalSymbolList();
    	accession = rs.getAccession();
    	comments = rs.getComments();
    	description = rs.getDescription();
    	division = rs.getDivision();
    	identifier = rs.getIdentifier();
    	bioname = rs.getName();
    	ns = rs.getNamespace();
    	rankedDocRefs = rs.getRankedDocRefs();
    	relationships = rs.getRelationships();
    	taxon = rs.getTaxon();
    	bioVersion = rs.getVersion();
    	noteSet = rs.getNoteSet();
    	rankedCrossRefs = rs.getRankedCrossRefs();
    	circular = rs.getCircular();
    }

    Set featureSet = null;
    Double seqVersion = null;
    SymbolList intSymList = null;
    
    //  RichSequence methods
	public Double getSeqVersion(){
		return seqVersion;
	}
	public void setSeqVersion(Double version) throws ChangeVetoException{
        throw new ChangeVetoException();
	}
	public Set getFeatureSet(){
		return featureSet;
	}
	public void setFeatureSet(Set s) throws ChangeVetoException{
        throw new ChangeVetoException();
	}
	public void setCircular(boolean circular) throws ChangeVetoException{
        throw new ChangeVetoException();
	}
	public boolean getCircular(){
		return circular;
	}
	public SymbolList getInternalSymbolList(){
		return intSymList;
	}
	

	// BioEntry methods
	public void addComment(Comment comment) throws ChangeVetoException {
        throw new ChangeVetoException();
	}
	public void addRankedDocRef(RankedDocRef docref) throws ChangeVetoException {        
		throw new ChangeVetoException();
	}
	public void addRelationship(BioEntryRelationship relation) throws ChangeVetoException {
		throw new ChangeVetoException();
	}
	String accession = null;
	Set comments = null;
	String description = null;
	String division = null;
	String identifier = null;
	String bioname = null;
	Namespace ns = null;
	Set rankedDocRefs = null;
	Set relationships = null;
	NCBITaxon taxon = null;
	int bioVersion = 0;

	public String getAccession(){return accession;}
	public Set getComments(){return comments;}
	public String getDescription(){return description;}
	public String getDivision(){return division;}
	public String getIdentifier(){return identifier;}
	public String getName(){return bioname;}
	public Namespace getNamespace(){return ns;}
	public Set getRankedDocRefs(){return rankedDocRefs;}
	public Set getRelationships(){return relationships;}
	public NCBITaxon getTaxon(){return taxon;}
	public int getVersion(){return bioVersion;}
	public void removeComment(Comment comment) throws ChangeVetoException {
		throw new ChangeVetoException();
	}
	public void removeRankedDocRef(RankedDocRef docref) throws ChangeVetoException {
		throw new ChangeVetoException();
	}
	public void removeRelationship(BioEntryRelationship relation) throws ChangeVetoException {
		throw new ChangeVetoException();
	}
	public void setDescription(String description) throws ChangeVetoException {
		throw new ChangeVetoException();
	}
	public void setDivision(String division) throws ChangeVetoException {
		throw new ChangeVetoException();
	}
	public void setIdentifier(String identifier) throws ChangeVetoException {
		throw new ChangeVetoException();
	}
	public void setTaxon(NCBITaxon taxon) throws ChangeVetoException {
		throw new ChangeVetoException();
	}

	// comparable methods
	public int compareTo(Object o){
		return -1;
	}
	
	// RichAnnotatable methods
	Set noteSet = null;
	public Set getNoteSet(){ return noteSet; }
	public void setNoteSet(Set notes) throws ChangeVetoException {
		throw new ChangeVetoException();
	}
	
	// RankedCrossRefable methods
	Set rankedCrossRefs = null;
	public Set getRankedCrossRefs()
    {
		return rankedCrossRefs;
    }
	public void setRankedCrossRefs(Set refs) throws ChangeVetoException
    {
		throw new ChangeVetoException();
    }
	public void addRankedCrossRef(RankedCrossRef crossref) throws ChangeVetoException
    {		
		throw new ChangeVetoException();
    }
	public void removeRankedCrossRef(RankedCrossRef crossref) throws ChangeVetoException
    {		
		throw new ChangeVetoException();
    }

	public RichAnnotation getRichAnnotation() {
		// TODO Auto-generated method stub
		return null;
	}
}
