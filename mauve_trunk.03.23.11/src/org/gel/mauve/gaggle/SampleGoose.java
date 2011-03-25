package org.gel.mauve.gaggle;
// SampleGoose.java
//------------------------------------------------------------------------------
// $Revision: 503 $   
// $Date: 2005/04/03 19:15:04 $
//-------------------------------------------------------------------------------------
/*
 * Copyright (C) 2006 by Institute for Systems Biology,
 * Seattle, Washington, USA.  All rights reserved.
 *
 * This source code is distributed under the GNU Lesser
 * General Public License, the text of which is available at:
 *   http://www.gnu.org/copyleft/lesser.html
 */

// todo - this is the canonical goose - remove warnings


import java.rmi.*;

import org.systemsbiology.gaggle.core.datatypes.DataMatrix;
import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.Goose;
import org.systemsbiology.gaggle.core.datatypes.*;
import org.systemsbiology.gaggle.core.datatypes.Interaction;
import org.systemsbiology.gaggle.util.MiscUtil;
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;
import org.systemsbiology.gaggle.geese.common.GooseShutdownHook;
import org.systemsbiology.gaggle.geese.common.GaggleConnectionListener;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * A goose useful for testing and diagnostics and learning how to write a goose
 * that uses the new (2007-04) API. 
 */
public class SampleGoose extends JFrame implements Goose, GaggleConnectionListener,
        WindowListener {
    String myGaggleName = "Sample";
    String[] activeGooseNames;
    Boss boss;
    RmiGaggleConnector connector = new RmiGaggleConnector(this);
    protected JScrollPane scrollPane;
    protected JTextArea textArea;
    JComboBox gooseChooser;
    String targetGoose = "Boss";

    JButton connectButton;
    JButton disconnectButton;

    //-------------------------------------------------------------------------------------
    public SampleGoose() {
        super("Sample");
        addWindowListener(this);
        new GooseShutdownHook(connector);



        try {
            connectToGaggle();
        }
        catch (Exception ex0) {
            System.err.println("SampleGoose failed to connect to gaggle: " + ex0.getMessage());
        }
        add(createGui());

        connector.addListener(this);

        setSize(500, 500);
        MiscUtil.placeInCenter(this);
        setVisible(true);

    }

    //-------------------------------------------------------------------------------------
    JPanel createGui() {
        ToolTipManager.sharedInstance().setInitialDelay(0);
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        JPanel controlPanel = new JPanel();
        JToolBar toolbar = new JToolBar();
        controlPanel.add(toolbar);
        toolbar.setFloatable(false);

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        MiscUtil.setApplicationIcon(this);



        gooseChooser = new JComboBox(new String[]{"Boss"});
        gooseChooser.setPrototypeDisplayValue("a very very long goose name");
        gooseChooser.setToolTipText("Specify goose for broadcast");
        toolbar.add(gooseChooser);


        gooseChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                int gooseChooserIndex = cb.getSelectedIndex();
                System.out.println("choose goose index: " + gooseChooserIndex);
                targetGoose = (String) cb.getSelectedItem();
                System.out.println("target: " + targetGoose);
            }
        });

        JButton showGooseButton = new JButton("S");
        JButton hideGooseButton = new JButton("H");

        // broadcast small & simple versions of each data type
        JButton broadcastListButton = new JButton("B");
        JButton broadcastMatrixButton = new JButton("M");
        JButton broadcastNetworkButton = new JButton("N");
        JButton broadcastHashButton = new JButton("T");
        JButton broadcastClusterButton = new JButton("C");

        showGooseButton.setToolTipText("Show selected goose");
        hideGooseButton.setToolTipText("Hide selected goose");
        broadcastListButton.setToolTipText("Broadcast sample name list");
        broadcastMatrixButton.setToolTipText("Broadcast sample matrix");
        broadcastNetworkButton.setToolTipText("Broadcast sample network");
        broadcastHashButton.setToolTipText("Broadcast sample Tuple (lists of data and metadata)");
        broadcastClusterButton.setToolTipText("Broadcast cluster: selected row and column names");

        showGooseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    boss.show(targetGoose);
                }
                catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        });

        hideGooseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    boss.hide(targetGoose);
                }
                catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        });

        broadcastListButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    broadcastSampleList();
                }
                catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        });

        broadcastMatrixButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                broadcastSampleMatrix();
            }
        });

        broadcastNetworkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    broadcastSampleNetwork();
                }
                catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        });

        broadcastHashButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    broadcastSampleTuple();
                }
                catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        });

        broadcastClusterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    broadcastSampleCluster();
                }
                catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        });

        toolbar.add(showGooseButton);
        toolbar.add(hideGooseButton);
        toolbar.add(broadcastListButton);
        toolbar.add(broadcastMatrixButton);
        toolbar.add(broadcastNetworkButton);
        toolbar.add(broadcastHashButton);
        toolbar.add(broadcastClusterButton);

        JPanel searchPanel = new JPanel();
        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        mainPanel.setBorder(createBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        connectButton = new JButton("Connect");
        connectButton.setToolTipText("Connect to Boss");
        connectButton.setEnabled(!connector.isConnected());
        connectButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    connector.connectToGaggle();
                    connectButton.setEnabled(false);
                    disconnectButton.setEnabled(true);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SampleGoose.this,
                            "Couldn't connect to boss, is the boss running?");
                    e.printStackTrace();
                }
            }
        });


        disconnectButton = new JButton("Disconnect");
        disconnectButton.setToolTipText("Disconnect from Boss");
        disconnectButton.setEnabled(connector.isConnected());
        disconnectButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                connector.disconnectFromGaggle(false);
                connectButton.setEnabled(true);
                disconnectButton.setEnabled(false);
            }
        });


        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");
            }
        });


        JButton exitButton = new JButton("Quit");
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doExit();
            }
        });

        buttonPanel.add(connectButton);
        buttonPanel.add(disconnectButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exitButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        MiscUtil.updateGooseChooser(gooseChooser, myGaggleName, activeGooseNames);

        return mainPanel;

    } // createGui


    //-----------------------------------------------------------------------------------
    private Border createBorder() {
        int right = 10;
        int left = 10;
        int top = 10;
        int bottom = 10;
        return new EmptyBorder(top, left, bottom, right);
    }

    //-------------------------------------------------------------------------------
    public void connectToGaggle() {
        try {
            connector.connectToGaggle();
        }
        catch (Exception ex0) {
            System.err.println("failed to connect to gaggle: " + ex0.getMessage());
            ex0.printStackTrace();
        }
        boss = connector.getBoss();
    }

    //----------------------------------------------------------------------------------------
    public void handleNameList(String source, Namelist nameList) {
        StringBuffer sb = new StringBuffer();
        sb.append(" >>> handleNameList, name = " + nameList.getName() +
                ", length " + nameList.getNames().length + ", source = " + source + "\n");
        sb.append("  species: " + nameList.getSpecies() + "\n");

        int max = 5;
        if (nameList.getNames().length < max)
            max = nameList.getNames().length;
        for (int i = 0; i < max; i++)
            sb.append("   " + nameList.getNames()[i] + "\n");

        sb.append("\n\n");
        textArea.append(sb.toString());
        textArea.setCaretPosition(textArea.getText().length());
    }

    //----------------------------------------------------------------------------------------
    public void handleMatrix(String source, DataMatrix matrix) {
        StringBuffer sb = new StringBuffer();
        sb.append(" >>> handleMatrix: " + matrix.getRowCount() + " x " + matrix.getColumnCount() + "\n");
        sb.append("  species: " + matrix.getSpecies() + "\n");
        sb.append("matrix name = " + matrix.getName() + ", source = " + source + "\n");
        sb.append("\n\n");
        textArea.append(sb.toString());
        textArea.setCaretPosition(textArea.getText().length());

    }
//----------------------------------------------------------------------------------------

    public void handleTuple(String source, GaggleTuple gaggleTuple) { // todo - extract this to common code for other diag/sample geese
        // todo  - figure out a better way to show a small subset of a gaggleTuple
        // (right now we just show the whole thing)
        System.out.println("in SampleGoose.handleTuple()");
        StringBuilder sb = new StringBuilder();
        sb.append(" >>> handleTuple: " + gaggleTuple.getName() + "\n");
        sb.append("  species: " + gaggleTuple.getSpecies() + "\n");
        sb.append("  source: " + source + "\n\n");

        int datalength = (gaggleTuple.getData().getSingleList().size() > 5) ? 5
                : gaggleTuple.getData().getSingleList().size();
        
        int metadatalength = (gaggleTuple.getMetadata().getSingleList().size() > 5) ? 5
                : gaggleTuple.getMetadata().getSingleList().size();

        sb.append("First few rows of metadata: \n");
        sb.append(gaggleTuple.getMetadata().toString());
        sb.append("\n");

        /*
        if (gaggleTuple.getMetadata().getSingleList().size() == 0) {
            sb.append(gaggleTuple.getMetadata().toString());
            sb.append("\n");
        } else {
            String[] msegs = gaggleTuple.getMetadata().toString().split("\n");
            for (int i = 0; i < metadatalength; i++) {
                sb.append(msegs[i]);
                sb.append("\n");
            }

        }
        */


        sb.append("\n\nFirst few rows of data: \n");
        sb.append(gaggleTuple.getData().toString());
        sb.append("\n");
        /*
        if (gaggleTuple.getData().getSingleList().size() == 0) {
            sb.append(gaggleTuple.getData().toString());
            sb.append("\n");
        } else {
            String[] dsegs = gaggleTuple.getData().toString().split("\n");
            for (int i = 0; i < datalength; i++) {
                //Tuple tuple = (Tuple)gaggleTuple.getData().getSingleAt(i).getValue();
                //sb.append(tuple.toString());
                sb.append(dsegs[i]);
                sb.append("\n");
            }
            
        }
        */

        sb.append("\n\n");
        textArea.append(sb.toString());
        textArea.setCaretPosition(textArea.getText().length());
    }

    //----------------------------------------------------------------------------------------
    public void handleCluster(
            String source, Cluster cluster) {
        StringBuffer sb = new StringBuffer();
        sb.append(" >>> handleCluster: name =" + cluster.getName() + "\n");
        sb.append("  species: " + cluster.getSpecies() + "\n");
        sb.append("  source: " + source + "\n");
        sb.append("  rows: " + cluster.getRowNames().length + "\n");
        sb.append("  cols: " + cluster.getColumnNames().length + "\n");

        sb.append("\n\n");
        textArea.append(sb.toString());
        textArea.setCaretPosition(textArea.getText().length());

    }

    //----------------------------------------------------------------------------------------
    public void handleNetwork(String source, Network network) {
        StringBuffer sb = new StringBuffer();
        sb.append(" >>> handleNetwork, name =  " + network.getName() + "\n");
        sb.append("  source: " + source + "\n");
        sb.append("  species: " + network.getSpecies() + "\n");
        sb.append("  nodes: " + network.nodeCount() + "\n");
        sb.append("  edges: " + network.edgeCount() + "\n");
        if (network.getMetadata() == null) {
            sb.append("  no metadata");
        } else {
            sb.append("  metadata found, # of singles: ");
            sb.append(network.getMetadata().getSingleList().size());
        }

        sb.append("\n\n");
        textArea.append(sb.toString());
        textArea.setCaretPosition(textArea.getText().length());
    }

    //----------------------------------------------------------------------------------------
    protected void broadcastSampleList() {
        String[] nameList = {"YFL036W", "YFL037W", "YLR212C", "YLR213C",
                "YML085C", "YML086C", "YML123C", "YML124C"};

        String species = "Saccharomyces cerevisiae";
        Namelist gaggleNameList = new Namelist();
        gaggleNameList.setSpecies(species);
        gaggleNameList.setNames(nameList);
        try {
            boss.broadcastNamelist(myGaggleName, targetGoose, gaggleNameList);
        }
        catch (RemoteException rex) {
            System.err.println("SampleGoose: " + "rmi error calling boss.broadcast (nameList)");
            rex.printStackTrace();
        }

    } // broadcastSampleList

    //----------------------------------------------------------------------------------------
    protected void broadcastSampleCluster() {
        String[] rowNames = {"YFL036W", "YLR212C", "YML085C", "YML123C"};
        String[] columnNames = {"T000", "T120", "T240"};

        String species = "Saccharomyces cerevisiae";
        String clusterName = "Sample Cluster";

        try {
            boss.broadcastCluster(myGaggleName, targetGoose, new Cluster(clusterName, species, rowNames, columnNames));
        }
        catch (RemoteException rex) {
            System.err.println("SampleGoose: " + "rmi error calling boss.broadcast (cluster)");
            rex.printStackTrace();
        }

    } // broadcastSampleCluster

    //----------------------------------------------------------------------------------------
    protected void broadcastSampleMatrix() {
        org.systemsbiology.gaggle.core.datatypes.DataMatrix matrix = new org.systemsbiology.gaggle.core.datatypes.DataMatrix();

        matrix.setFullName("Demo Yeast created on the fly, meaningless data");
        matrix.setShortName("Demo Yeast");

        String[] columnTitles = {"T000", "T060", "T120", "T240"};
        String[] rowTitles = {"YFL036W", "YFL037W", "YLR212C", "YLR213C",
                "YML085C", "YML086C", "YML123C", "YML124C"};
        int dataRows = rowTitles.length;
        int dataColumns = columnTitles.length;
        matrix.setSize(dataRows, dataColumns);

        matrix.setSpecies("Saccharomyces cerevisiae");
        matrix.setRowTitlesTitle("GENE");
        matrix.setColumnTitles(columnTitles);
        matrix.setRowTitles(rowTitles);

        for (int r = 0; r < dataRows; r++)
            for (int c = 0; c < dataColumns; c++)
                matrix.set(r, c, (r * 0.38) + c * 0.09);

        matrix.setName("a sample matrix");
        try {
            boss.broadcastMatrix(myGaggleName, targetGoose, matrix);
        }
        catch (RemoteException rex) {
            System.err.println("SampleGoose: " + "rmi error calling boss.broadcast (matrix)");
            rex.printStackTrace();
        }

    } // broadcastSampleMatrix

    //----------------------------------------------------------------------------------------
    protected void broadcastSampleNetwork() {
        Interaction i0 = new Interaction("YFL036W", "YFL037W", "GeneCluster");
        Interaction i1 = new Interaction("YFL037W", "YLR212C", "GeneFusion");
        Interaction i2 = new Interaction("YFL037W", "YML085C", "GeneFusion");
        Interaction i3 = new Interaction("YFL037W", "YML124C", "GeneFusion");
        Interaction i4 = new Interaction("YLR212C", "YLR213C", "GeneCluster");
        Interaction i5 = new Interaction("YLR212C", "YML085C", "GeneFusion");
        Interaction i6 = new Interaction("YLR212C", "YML124C", "GeneFusion");
        Interaction i7 = new Interaction("YML123C", "YML124C", "GeneCluster");
        Interaction i8 = new Interaction("YML085C", "YML086C", "GeneCluster");
        Interaction i9 = new Interaction("YML085C", "YML124C", "GeneFusion");

        org.systemsbiology.gaggle.core.datatypes.Network network = new org.systemsbiology.gaggle.core.datatypes.Network();

        network.add(i0);
        network.add(i1);
        network.add(i2);
        network.add(i3);
        network.add(i4);
        network.add(i5);
        network.add(i6);
        network.add(i7);
        network.add(i8);
        network.add(i9);

        String species = "Saccharomyces cerevisiae";
        String[] nodeNames = {"YFL036W", "YFL037W", "YLR212C", "YLR213C",
                "YML085C", "YML086C", "YML123C", "YML124C"};
        for (int i = 0; i < nodeNames.length; i++) {
            network.addNodeAttribute(nodeNames[i], "moleculeType", "DNA");
            network.addNodeAttribute(nodeNames[i], "species", species);
        }

        network.addEdgeAttribute("YFL036W (GeneCluster) YFL037W", "score", new Double(0.5));
        network.addEdgeAttribute("YFL037W (GeneFusion) YLR212C", "score", new Double(0.4));
        network.addEdgeAttribute("YFL037W (GeneFusion) YML085C", "score", new Double(0.3));
        network.addEdgeAttribute("YFL037W (GeneFusion) YML124C", "score", new Double(0.2));
        network.addEdgeAttribute("YLR212C (GeneCluster) YLR213C", "score", new Double(0.1));
        network.addEdgeAttribute("YLR212C (GeneFusion) YML085C", "score", new Double(0.8));
        network.addEdgeAttribute("YLR212C (GeneFusion) YML124C", "score", new Double(0.75));
        network.addEdgeAttribute("YML123C (GeneCluster) YML124C", "score", new Double(0.55));
        network.addEdgeAttribute("YML085C (GeneCluster) YML086C", "score", new Double(0.45));
        network.addEdgeAttribute("YML085C (GeneFusion) YML124C", "score", new Double(0.35));
        network.setSpecies(species);
        network.setName("a sample network");

        try {
            boss.broadcastNetwork(myGaggleName, targetGoose, network);
        }
        catch (RemoteException rex) {
            System.err.println("SampleGoose: " + "rmi error calling boss.broadcast (network)");
            rex.printStackTrace();
        }

    } // broadcastSimpleNetwork

    //----------------------------------------------------------------------------------------
    protected void broadcastSampleTuple() {
        // todo change this to broadcast vng names (and change species accordingly)
        System.out.println("in SampleGoose.broadcastSampleTuple()");

        double[] values = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8};
        String[] rowNames = {"YFL036W", "YFL037W", "YLR212C", "YLR213C",
                "YML085C", "YML086C", "YML123C", "YML124C"};

        GaggleTuple gaggleTuple = new GaggleTuple();
        gaggleTuple.setName("a tuple list holding a frame of a movie");
        gaggleTuple.setSpecies("escargot");

        // todo - think of convenience methods that would allow doing this in one line:
        Single condition = new Single("condition", "a condition name");
        gaggleTuple.getMetadata().addSingle(condition);
        //

        for (int i = 0; i < rowNames.length; i++) {
            Tuple tuple = new Tuple();
            tuple.addSingle(new Single(null, rowNames[i]));
            tuple.addSingle(new Single(null, "log10 ratios"));
            tuple.addSingle(new Single(null, values[i]));
            gaggleTuple.getData().addSingle(new Single(tuple));
        }



        try {
            boss.broadcastTuple(myGaggleName, targetGoose, gaggleTuple);
        }
        catch (RemoteException rex) {
            System.err.println("SampleGoose: " + "rmi error calling boss.broadcastTuple");
            rex.printStackTrace();
        }

    }

    //----------------------------------------------------------------------------------------
    public void clearSelections() {
        System.out.println("clearSelections");
    }

    //----------------------------------------------------------------------------------------
    public int getSelectionCount() {
        return 0;
    }

    //----------------------------------------------------------------------------------------
    public String getName() {
        return myGaggleName;
    }

    //----------------------------------------------------------------------------------------
    public void setName(String newName) {
        myGaggleName = newName;
        setTitle(myGaggleName);
    }

    //----------------------------------------------------------------------------------------
    public void setGeometry(int x, int y, int width, int height) {
        System.out.println("setGeometry");
    }

    //----------------------------------------------------------------------------------------
    public void doBroadcastList() {

    }

    //----------------------------------------------------------------------------------------
    public void doHide() {
        setVisible(false);
    }

    //----------------------------------------------------------------------------------------
    public void doShow() {
        setAlwaysOnTop(true);
        setVisible(true);
        setAlwaysOnTop(false);

    }

    //----------------------------------------------------------------------------------------
    public void doExit() {
        connector.disconnectFromGaggle(true);
        System.exit(0);
    }

    public void update(String[] activeGooseNames) {
        this.activeGooseNames = activeGooseNames;

        MiscUtil.updateGooseChooser(gooseChooser, myGaggleName, activeGooseNames);

        if (textArea != null) {
            StringBuffer sb = new StringBuffer();
            sb.append(" >>> GOT AN UPDATE EVENT. New goose names are: \n");
            for (String gooseName : activeGooseNames) {
                sb.append(gooseName);
                sb.append("\n");
            }

            sb.append("\n\n");
            textArea.append(sb.toString());
            textArea.setCaretPosition(textArea.getText().length());
        }
    }


    public void setConnected(boolean connected, Boss boss) {
        this.boss = boss;
        System.out.println("SampleGoose: received connection status: " + connected);
        if (connectButton != null)
            connectButton.setEnabled(!connected);
        if (disconnectButton !=  null)
            disconnectButton.setEnabled(connected);
    }


    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        doExit();
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public static void main(String[] args) throws Exception {
        new SampleGoose();
    } // main
//-------------------------------------------------------------------------------------
} // SampleGoose
