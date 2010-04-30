package org.gel.mauve.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.GridLayout;
// these are needed to grab the users display screen size
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

//import org.gel.mauve.assembly.ScoreAssembly.ChangeCards;

public class AnalysisDisplayWindow extends JFrame {
	
	private static int xMax = Toolkit.getDefaultToolkit().getScreenSize().width;
	
	private int contentCount;
	
	private JPanel topBar;
	
	private JButton saveBtn;
	
	private JPanel content;
	
	private JPanel botBar;
	
	private JFrame frame;
	
	private Font font;
	
	private HashMap<String,JComponent> components;
	
	private String name;
	
	private int width;
	
	private int height;
	
	private CardLayout cardMngr;
	
	private ContentManager cc;
	
	public AnalysisDisplayWindow(String name, int width, int height) {
		contentCount = 0;
		font = new Font ("monospaced", Font.PLAIN, 12);
		frame = new JFrame(name);
		
		setLayout(new BorderLayout());
		this.name = name;
		this.width = width;
		this.height = height;
		frame.setSize(this.width, this.height);
		frame.setLocation(xMax-this.width, 0);
		components = new HashMap<String,JComponent>();
		cc = new ContentManager();
		topBar = new JPanel();
		BoxLayout tmp = new BoxLayout(topBar, BoxLayout.X_AXIS);
		topBar.setLayout(tmp);
		saveBtn = new JButton("Save");
		topBar.add(saveBtn);
		topBar.setSize(100, 100);
		saveBtn.addActionListener(cc);
		GridLayout tmp1 = new GridLayout(1,0);
		tmp1.setHgap(10);
		botBar = new JPanel(tmp1,true);
		content = new JPanel(new BorderLayout());
		topBar.setBorder(BorderFactory.createEmptyBorder(2,1,4,1));
		botBar.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
		cardMngr = new CardLayout();
		content.setLayout(cardMngr);
		content.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		JPanel tempPanel = new JPanel(new BorderLayout());
		tempPanel.add(topBar, BorderLayout.NORTH);
		tempPanel.add(botBar,BorderLayout.SOUTH);
		tempPanel.add(content, BorderLayout.CENTER);
		tempPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		frame.getContentPane().add(tempPanel, BorderLayout.CENTER);
		
	}
	
	public void showWindow(){
		if (components.size() == 0)
			throw new RuntimeException("Can't call showWindow() unless there are Panels to show");
		
		if (botBar.getComponentCount() <= 1){
			botBar.setVisible(false);
		} else {
			botBar.setVisible(true);
		}
		if (!frame.isVisible()) {
			frame.setLocation(xMax-this.width, 0);
			frame.setSize(width, height);	
		}
		frame.setVisible(true);
	}
	
	public void closeWindow(){
		frame.setVisible(false);
	}

	/**
	 * Adds a text pane to this display window.
	 * 
	 * @param cmd
	 * @param desc
	 * @param setTop
	 * @return the text area that gets displayed in the created pane
	 */
	public JTextArea addContentPanel(String cmd, String desc, boolean setTop){
		// create button, add to bottom bar, and add a listener
		JButton butn = new JButton(cmd);
		botBar.add(butn);
		butn.addActionListener(cc);
		JTextArea ta = new JTextArea(25, 25); 
		components.put(cmd, ta);
		ta.setFont(font);
		ta.setEditable(false);
		cc.addCard(cmd, desc);
		if (setTop){
			cardMngr.show(content, desc);
		}
		JScrollPane jsp = new JScrollPane(ta);
		content.add(desc, jsp);
		contentCount++;
		return ta;
	}
	
	/**
	 * Adds a table pane to this display window 
	 * 
	 * @param cmd
	 * @param desc
	 * @param setTop
	 * @return the table that gets displayed in the created pane
	 */
	public DefaultTableModel addContentTable(String cmd, String desc, boolean setTop){
		DefaultTableModel data = new DefaultTableModel();
		JButton butn = new JButton(cmd);
		botBar.add(butn);
		butn.addActionListener(cc);
		cc.addCard(cmd, desc);
		if (setTop){
			cardMngr.show(content, desc);
		}
		JTable table = new JTable(data);
		components.put(cmd, table);
		JScrollPane jsp = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		content.add(desc, jsp);
		return data;
	}
	
	
	private class ContentManager implements ActionListener {
		
		HashMap<String,String> btmBarMap;
		
		HashMap<String,String> topBarMap;
		
		String lastCmd;
		
		JPanel savePanel;
		
		
		public ContentManager(){
			savePanel = new JPanel();
			savePanel.setSize(200, 100);
			btmBarMap = new HashMap<String,String>();
			topBarMap = new HashMap<String,String>();
		}
		
		public void addCard(String cmd, String action){
			if (lastCmd == null)
				lastCmd = cmd;
			btmBarMap.put(cmd, action);
		}
		
		public void actionPerformed (ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equalsIgnoreCase("Save")){
				try {
					printText();
				} catch (Exception exc){
					
				}
			} else if (btmBarMap.containsKey(cmd)){
				String action = btmBarMap.get(cmd);
				lastCmd = cmd;
				cardMngr.show(content, action);
			} else {
				System.err.println("Illegal Action!");
			}
		}
		
		private void printText() throws Exception{
			File file = null;
			int option = -1;
			boolean selectFile = true;

			JFileChooser fc = new JFileChooser(System.getProperty("user.home"));
			while(selectFile){
				selectFile = false;
				option = fc.showSaveDialog(frame);
				if (option == JFileChooser.APPROVE_OPTION){
					file = fc.getSelectedFile();
					if (file.exists()){
						int result = JOptionPane.showConfirmDialog(frame,
										"The file " + file + " already exists.  Overwrite?", 
										"File exists", JOptionPane.YES_NO_OPTION);
						selectFile = result == JOptionPane.NO_OPTION;
					} else {
						file.createNewFile();
						selectFile = false;
					}
				} 
			}
			if (option != JFileChooser.CANCEL_OPTION){
				System.err.println("Writing "+frame.getTitle()+" - "+lastCmd+ " to " + file.getAbsolutePath());

				PrintStream out = (new PrintStream(file));
				JComponent toPrint = components.get(lastCmd);
				if (toPrint instanceof JTextArea){
					out.print(((JTextArea)toPrint).getText());
				} else if (toPrint instanceof JTable) {
					DefaultTableModel data = (DefaultTableModel) 
											((JTable) toPrint).getModel();
					int numCol = data.getColumnCount();
					int numRow = data.getRowCount();
					out.print(data.getColumnName(0));
					for (int colI = 1 ; colI < numCol; colI++){
						out.print("\t"+data.getColumnName(colI));
					}
					out.print("\n");
					for (int rowI = 0; rowI < numRow; rowI++){
						out.print(data.getValueAt(rowI, 0).toString());
						for (int colI = 0 ; colI < numCol; colI++){
							out.print("\t"+data.getValueAt(rowI, colI).
															toString());
						}
						out.print("\n");
					}
				}
				out.flush();
				out.close();
			}
				
		
		}
		
		public String getLastCommand(){
			return lastCmd;
		}
		
	}
	
	
	
}
