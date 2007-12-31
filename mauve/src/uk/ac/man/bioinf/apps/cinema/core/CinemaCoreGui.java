/* 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

/* 
 * This software was written by Phillip Lord (p.lord@hgmp.mrc.ac.uk)
 * whilst at the University of Manchester as a Pfizer post-doctoral 
 * Research Fellow. 
 *
 * The initial code base is copyright by Pfizer, or the University
 * of Manchester. Modifications to the initial code base are copyright
 * of their respective authors, or their employers as appropriate. 
 * Authorship of the modifications may be determined from the ChangeLog
 * placed at the end of this file
 */

package uk.ac.man.bioinf.apps.cinema.core; // Package name inserted by JPack

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.gel.mauve.cinema.XMFAModelSequenceOutput;
import org.gel.mauve.cinema.XMFASequenceModuleIdentifier;
import org.gel.mauve.cinema.XMFASource;

import uk.ac.man.bioinf.apps.cinema.utils.CinemaAlignmentFrame;
import uk.ac.man.bioinf.apps.systemevents.SystemEvent;
import uk.ac.man.bioinf.apps.systemevents.SystemEventOption;
import uk.ac.man.bioinf.apps.systemevents.SystemListener;
import uk.ac.man.bioinf.apps.systemevents.SystemVetoException;
import uk.ac.man.bioinf.gui.viewer.JAlignmentButtonPanel;
import uk.ac.man.bioinf.gui.viewer.JAlignmentRuler;
import uk.ac.man.bioinf.gui.viewer.JAlignmentViewer;
import uk.ac.man.bioinf.module.Module;
import uk.ac.man.bioinf.module.ModuleException;
import uk.ac.man.bioinf.module.ModuleIdentifierList;
import uk.ac.man.bioinf.sequence.alignment.SequenceAlignment;

/**
 * CinemaCoreGui.java
 * 
 * This module defines the core Gui for cinema. It gives access to all of the
 * main components that it defines, and get/set methods for these. This is meant
 * to be a pretty low level module. Generally other modules should not use this
 * unless absolutely necessary. Rather the CinemaCoreView module should be used
 * instead.
 * 
 * At the moment set access has not been provided to any of the components. I
 * may change this if is needed.
 * 
 * Created: Wed Apr 19 16:58:08 2000
 * 
 * @author Phillip Lord
 * @version $Id: CinemaCoreGui.java,v 1.17 2001/05/08 17:39:31 lord Exp $
 */

public class CinemaCoreGui extends Module implements SystemListener, CinemaActionProvider
{
    private CinemaAlignmentFrame alignmentFrame;
    private JTextField statusBar;

    public void load() throws ModuleException
    {
        String title = "Mauve LCB Editor";

        alignmentFrame = new CinemaAlignmentFrame("cinema.core.main.frame", title, true);

        alignmentFrame.addWindowListener(new WindowListener()
        {
            public void windowClosing(WindowEvent e)
            {
                try
                {
                    getSystemEventModule().fireSystemEvent(SystemEventOption.SYSTEM_SHUTDOWN);
                }
                catch (SystemVetoException e1)
                {
                    return;
                }
                getContext().getModuleFactory().destroy();
            }

            public void windowActivated(WindowEvent e)
            {
            }

            public void windowClosed(WindowEvent e)
            {
            }

            public void windowDeactivated(WindowEvent e)
            {
            }

            public void windowDeiconified(WindowEvent e)
            {
            }

            public void windowIconified(WindowEvent e)
            {
            }

            public void windowOpened(WindowEvent e)
            {
            }
        });

        statusBar = new JTextField();
        statusBar.setEditable(false);
        statusBar.setBackground(alignmentFrame.getBackground());
        statusBar.setForeground(Color.black);

        alignmentFrame.getMainPanel().add(statusBar, BorderLayout.SOUTH);
    }

    public void start()
    {
        getSystemEventModule().addSystemEventListener(this);
        alignmentFrame.setDefaultSize(400, 400);
    }

    public void destroy()
    {
        super.destroy();
        // kill everything in sight which might prevent GC...
        alignmentFrame.setVisible(false);
        alignmentFrame.dispose();
        statusBar = null;
        alignmentFrame = null;
    }

    public void systemEventOccured(SystemEvent event) throws SystemVetoException
    {
        XMFASource xmfaSource = (XMFASource) alignmentFrame.getViewer().getSequenceAlignment().getIdentifier().getSource();
        
        if (xmfaSource.isDirty())
        {
            
            int response = JOptionPane.showConfirmDialog(alignmentFrame, "Would you like to save unsaved changes before closing?", "Save Prompt", JOptionPane.YES_NO_CANCEL_OPTION);
            if (response == JOptionPane.YES_OPTION)
            {
                XMFAModelSequenceOutput mod = getXmfaSequenceOutputModule();
                SequenceAlignment sa = alignmentFrame.getViewer().getSequenceAlignment();
                mod.saveAlignment(sa);
            }
            else if (response == JOptionPane.CANCEL_OPTION)
            {
                throw new SystemVetoException("Cancelled by user request", event);
            }
        }
    }

    public int systemListenerPriority()
    {
        return VETO_DUE_TO_USER_REQUEST;
    }

    //getters and setters
    public JFrame getFrame()
    {
        return alignmentFrame;
    }

    public JAlignmentButtonPanel getRowHeaders()
    {
        return alignmentFrame.getRowHeaders();
    }

    public JMenuBar getJMenuBar()
    {
        return alignmentFrame.getJMenuBar();
    }

    public JScrollPane getScrollPane()
    {
        return alignmentFrame.getScrollPane();
    }

    public JPanel getRulerPanel()
    {
        return alignmentFrame.getRulerPanel();
    }

    public JAlignmentViewer getViewer()
    {
        return alignmentFrame.getViewer();
    }

    public JAlignmentRuler getRuler()
    {
        return alignmentFrame.getRuler();
    }

    public JTextField getStatusBar()
    {
        return statusBar;
    }

    public CinemaAlignmentFrame getAlignmentFrame()
    {
        return alignmentFrame;
    }

    public Action[] getActions()
    {
        Action[] actions = new Action[1];

        alignmentFrame.getViewer().getSequenceAlignment();

        actions[0] = new AbstractAction("Close")
        {

            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    getSystemEventModule().fireSystemEvent(SystemEventOption.SYSTEM_SHUTDOWN);
                }
                catch (SystemVetoException e1)
                {
                    return;
                }
                getContext().getModuleFactory().destroy();
            }
        };

        return actions;
    }

    private CinemaSystemEvents getSystemEventModule()
    {
        return (CinemaSystemEvents) getRequiredModule(CinemaCoreIdentifier.CINEMA_SYSTEM_EVENTS);
    }

    private XMFAModelSequenceOutput getXmfaSequenceOutputModule()
    {
        return (XMFAModelSequenceOutput) getRequiredModule(XMFASequenceModuleIdentifier.XMFA_MODEL_OUTPUT);
    }

    public ModuleIdentifierList getRequiredIdentifiers()
    {
        ModuleIdentifierList list = super.getRequiredIdentifiers();
        list.add(CinemaCoreIdentifier.CINEMA_SYSTEM_EVENTS);
        list.add(XMFASequenceModuleIdentifier.XMFA_MODEL_OUTPUT);
        return list;
    }

    public String getVersion()
    {
        return "$Id: CinemaCoreGui.java,v 1.17 2001/05/08 17:39:31 lord Exp $";
    }

} // CinemaCoreGui

/*
 * ChangeLog $Log: CinemaCoreGui.java,v $ Revision 1.17 2001/05/08 17:39:31 lord
 * Removed spam debug statements.
 * 
 * Revision 1.16 2001/04/11 17:04:41 lord Added License agreements to all code
 * 
 * Revision 1.15 2001/02/19 16:56:16 lord Added optionability
 * 
 * Revision 1.14 2001/01/31 17:41:20 lord Made a little more robust. Essentially
 * if the nifty shared shut down thing fails, I just have a single Exit button.
 * 
 * Revision 1.13 2000/12/13 16:27:56 lord Removed debug statement
 * 
 * Revision 1.12 2000/12/05 15:54:37 lord Import rationalisation
 * 
 * Revision 1.11 2000/10/19 17:38:23 lord Access to CinemaAlignmentFrame
 * provided
 * 
 * Revision 1.10 2000/09/15 17:28:54 lord Now destroyable. Delegates Close, and
 * Exit functionality to a module shared amoung all instances
 * 
 * Revision 1.9 2000/08/02 14:54:20 lord Removed test status bar message
 * 
 * Revision 1.8 2000/07/18 10:38:53 lord Import rationalisation
 * 
 * Revision 1.7 2000/06/27 13:39:15 lord Now uses CinemaAlignmentFrame, so much
 * of the code has been moved to that class
 * 
 * Revision 1.6 2000/05/30 16:09:01 lord Rationalised imports. Added status bar.
 * Changes due to completion of the module package Now also ActionProvider
 * 
 * Revision 1.5 2000/05/24 15:35:17 lord Sorted imports Added row headers code
 * 
 * Revision 1.4 2000/05/18 17:12:52 lord Support for row headers
 * 
 * Revision 1.3 2000/05/15 16:21:02 lord fixed close down bug
 *  
 */
