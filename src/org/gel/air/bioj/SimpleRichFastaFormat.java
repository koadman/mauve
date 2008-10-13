package org.gel.air.bioj;

import java.io.IOException;

import org.biojava.bio.seq.Sequence;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.Namespace;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.io.FastaFormat;

public class SimpleRichFastaFormat extends FastaFormat {

    public void writeSequence(Sequence seq, Namespace ns) throws IOException {
        RichSequence rs;
        try {
            if (seq instanceof RichSequence) rs = (RichSequence)seq;
            else rs = RichSequence.Tools.enrich(seq);
        } catch (ChangeVetoException e) {
            IOException e2 = new IOException("Unable to enrich sequence");
            e2.initCause(e);
            throw e2;
        }
        
        this.getPrintStream().print(">");
        this.getPrintStream().print(rs.getName());
        this.getPrintStream().print(" ");
        String desc = rs.getDescription();
        if (desc!=null && !"".equals(desc)) this.getPrintStream().print(desc.replaceAll("\\n"," "));
        this.getPrintStream().println();
        
        int length = rs.length();
        
        for (int pos = 1; pos <= length; pos += this.getLineWidth()) {
            int end = Math.min(pos + this.getLineWidth() - 1, length);
            this.getPrintStream().println(rs.subStr(pos, end));
        }
    }
    
}
