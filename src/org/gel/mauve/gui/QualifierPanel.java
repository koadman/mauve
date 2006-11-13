package org.gel.mauve.gui;

import java.awt.Font;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Feature;

public class QualifierPanel extends Box
{
    private final static int WRAP_COL = 60;

    public QualifierPanel(Feature f)
    {
        super(BoxLayout.Y_AXIS);
        setName("<html>" + f.getType() + "<br>" + f.getLocation() + "</html>");

        StringBuffer msg = new StringBuffer();
        msg.append("<html>");

        Annotation a = f.getAnnotation();

        if (a != null)
        {
            msg.append("<table border='1'>");
            Iterator i = a.keys().iterator();
            while (i.hasNext())
            {
                Object key = i.next();
                msg.append("<tr><td valign='top'><b>");
                msg.append(key.toString());
                msg.append("</b></td><td valign='top'>");
                msg.append(wrapLong(a.getProperty(key).toString()));
                msg.append("</td></tr>");
            }
            msg.append("</table>");
        }

        msg.append("</html>");
        JLabel label = new JLabel(msg.toString());
        label.setFont(new Font(label.getFont().getName(), Font.PLAIN, label.getFont().getSize()));
        add(label);
    }

    private String wrapLong(String in)
    {
        if (in == null)
            return null;

        if (in.length() < WRAP_COL)
            return in;

        StringBuffer s = new StringBuffer();

        int i = 0;
        for (; i < in.length() - WRAP_COL; i += WRAP_COL)
        {
            s.append(in.subSequence(i, i + WRAP_COL));
            s.append("<br>");
        }

        if (i < in.length())
        {
            s.append(in.subSequence(i, in.length()));
        }

        return s.toString();
    }
}