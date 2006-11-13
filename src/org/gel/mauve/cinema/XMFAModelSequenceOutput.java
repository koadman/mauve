package org.gel.mauve.cinema;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import uk.ac.man.bioinf.apps.cinema.CinemaGuiModule;
import uk.ac.man.bioinf.apps.cinema.core.CinemaActionProvider;
import uk.ac.man.bioinf.debug.Debug;
import uk.ac.man.bioinf.io.ParserException;
import uk.ac.man.bioinf.io.ParserExceptionHandler;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;

public class XMFAModelSequenceOutput extends CinemaGuiModule implements CinemaActionProvider, ParserExceptionHandler
{
    private XMFAAlignmentParser parser = new XMFAAlignmentParser();

    public String getVersion()
    {
        return "version 0";
    }

    public Action[] getActions()
    {
        Action[] actions = new Action[1];

        actions[0] = new AbstractAction("Save...")
        {
            public void actionPerformed(ActionEvent event)
            {
                saveAlignment(getSequenceAlignment());
            }
        };

        return actions;
    }

    public void saveAlignment(final SequenceAlignment sa)
    {
        final JDialog dialog = new JDialog(getAlignmentFrame(), "Saving and reloading", true);
        JPanel panel = new JPanel();
        panel.add(new JLabel("Saving alignment"));
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        //FIXME:  Why isn't this message appearing??!?!?!?!?
        dialog.getContentPane().add(new JLabel("Saving file and reloading viewer."));
        dialog.getContentPane().add(panel);
        dialog.setSize(200, 100);
        // The dialog needs to kick off the reload, so that the modality of the
        // dialog doesn't prevent the viewer from repainting itself.
        dialog.addWindowListener(new WindowAdapter()
        {
            public void windowOpened(WindowEvent evt)
            {
                XMFASource xmfaSource = (XMFASource) sa.getIdentifier().getSource();
                File tmp;
                try
                {
                    tmp = File.createTempFile("mauve", null);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
                saveToFile(sa, tmp);
                File src = xmfaSource.getModel().getSrc();
                try
                {
                    // Copy temporary file back to source.
                    copy(tmp, src);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
                // Delete temporary file.
                tmp.delete();
                // Tell the model to reload from file.
                xmfaSource.getModel().reload();

                dialog.setVisible(false);
                dialog.dispose();

            }

        });
        dialog.setVisible(true);

    }

    /**
     * @param sa
     * @param dest
     */
    private void saveToFile(SequenceAlignment sa, File dest)
    {
        XMFASource xmfaSource = (XMFASource) sa.getIdentifier().getSource();

        try
        {
            File src = xmfaSource.getModel().getSrc();

            // Copy source to file.
            copy(src, dest);

            // Use the "parser" to write to the temp file.
            FileWriter writer = new FileWriter(dest);
            parser.write(sa, writer, this);

            // flush the writer and close it
            writer.flush();
            writer.close();

            xmfaSource.setDirty(false);

        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(null, "<html>There was a problem with saving the results: <p>" + e.getMessage(), "Warning", JOptionPane.ERROR_MESSAGE);

            if (Debug.debug)
                Debug.both(this, "File Sequence Output: An IOException was found", e);
        }
    }

    private void copy(File src, File dest) throws IOException
    {
        // Copy file to tmp.
        BufferedReader r = new BufferedReader(new FileReader(src));
        BufferedWriter w = new BufferedWriter(new FileWriter(dest));

        String s = r.readLine();
        while (s != null)
        {
            w.write(s);
            w.newLine();
            s = r.readLine();
        }

        r.close();
        w.close();
    }

    public void handleException(ParserException e)
    {
        System.err.println(e);
    }

}
