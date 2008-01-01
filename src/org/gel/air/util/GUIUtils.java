package org.gel.air.util;





import javax.swing.*;

import javax.swing.border.*;

import java.awt.*;

import java.awt.event.*;

import java.util.*;






/**

  *GUIHelper has helper methods for creating GUIs to prevent basic GUI code

  *duplication.

**/

public class GUIUtils {



	//the parent of modal dialogs

	protected Container covered;





	//borders for components

	public MatteBorder titled_panel_border, 

			main_panel_border, button_border;

	protected static boolean blocked;

	





	/**

	  *constructor GUIHelper initializes internal variables.

	  *@param responder The ActionListener added to components that need more than

	  *  generic behavior in response to an ActionEvent.

	  *@param parent The JFrame or JDialog that should be blocked by the display of a 

	  *  created JDialog

	**/

	public GUIUtils (Container parent, Color background) {

		covered = parent;

		titled_panel_border = BorderFactory.createMatteBorder (

				5, 5, 5, 5, background);

		main_panel_border = BorderFactory.createMatteBorder (

				15, 15, 15, 15, background);

		button_border = BorderFactory.createMatteBorder (

				8, 0, 0, 0, background);

	}//constructor ActivityComponentCreator





	public static void showMessageDialog (Container owned, String message, 
			String title, int type) {
		waitForWhat ();
		JOptionPane.showMessageDialog (owned, message, title, type);
		blocked = false;
	}//method showMessageDialog


	public static int showConfirmDialog (Container owned, String message,
			String title, int option) {
		waitForWhat ();
		int response = JOptionPane.showConfirmDialog (owned, message, title, option);
		blocked = false;
		return response;
	}//method showConfirmDialog







	/**

	  *makeIncompleteDataDialog informs a user that in order to proceed, certain information

	  *must be given

	  *@param specifics What data was incomplete and/or what to do about it

	**/

	public void makeIncompleteDataDialog (String specifics) {



		//display dialog
		waitForWhat ();

		JOptionPane.showMessageDialog (covered, "Information received was incomplete.  " +

				specifics, "Incomplete Data", 

				JOptionPane.WARNING_MESSAGE);
		blocked = false;

	}//method makeIncompleteDataDialog









	/**

	  *makeCompletedDialog creates a dialog informing a user that their request has been

	  *successfully completed

	  *@param message Specifics about what request has been completed

	**/

	public void makeCompletedDialog (String message) {



		//display dialog
		waitForWhat ();

		JOptionPane.showMessageDialog (covered, message,

				"Request Completed", 

				JOptionPane.INFORMATION_MESSAGE);
		blocked = false;

	}//method makeCompletedDialog











	/**

	  *makeChoiceDialog displays a list of choices and returns

	  *the choice a user makes.

	  *@param message Specifics about the choice

	  *@param choices All choices the user should be allowed to make
	  *@param lister The ProductLists from which the choices came.  Should be
	  *  null if event services are not being used.

	  *@return The choices the user made (null if they decide to cancel request).

	**/

	public Object [] makeChoiceDialog (boolean single, String message,

			String [] choices) {



		//must be final to be accessed from inner class, so must be in an array

		final Object [] response = new Object [1];

		response [0] = null;
		

		final JList options = new JList (new DefaultListModel ());

		increaseList (choices, options);


		if (single)

			options.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);

		else

			options.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);



		//can't have ambiguous constructor

		final JDialog choose;

		if (covered instanceof JFrame)

			choose = new JDialog ((JFrame) covered, "Option", true);

		else

			choose = new JDialog ((JDialog) covered, "Option", true);


		JComponent shower = (JComponent) choose.getContentPane ();

		shower.setBorder (main_panel_border);



		//if user chooses OK and something is selected, retrieve it

		ActionListener actor = new ActionListener () {

			public void actionPerformed (ActionEvent e) {

				if (options.getSelectedValues () != null)

					response [0] = options.getSelectedValues ();

				choose.setVisible (false);

				choose.dispose ();



			}

		};



		//add buttons and show

		JPanel buttons = addOkayCancel (choose, actor);

		shower.add (buttons, BorderLayout.SOUTH);

		shower.add (new JLabel (message), BorderLayout.NORTH);

		shower.add (new JScrollPane (options));

		choose.setSize (250, 250);

		centerComponent (choose);
		waitForWhat ();

		choose.setVisible (true);
		blocked = false;


		//return user's choice

		return (Object []) response [0];

	}//method makeChoiceDialog









	/**

	  *makeButtonList creates  a JList that can be added to or removed from

	  *through the use of buttons.  Also allows for a third choice to be given.

	  *@param label The list's title.

	  *@param third_button A button containing a third choice a user may make.

	  *@return A JPanel containing the (empty) list and buttons.

	**/


	public JPanel makeButtonList (ActionListener hearer, String label, 
			String third_button) {

		JPanel holder = new JPanel ();

		holder.setBorder (titled_panel_border);

		JList chosen = new JList ();

		chosen.setModel (new DefaultListModel ());

		JPanel buttons = new JPanel (new GridLayout (1, 3, 10, 0));

		buttons.setBorder (titled_panel_border);



		//create an identifier based off of the label so the button

		//can be easily traced to its related list

		char identifier = label.charAt (0);

		JButton temp = null;



		//if there is a third button, add it, otherwise a blank label

		//serves as a place holder

		//use class listener to all buttons, as specific behavior is necessary

		if (third_button != null) {

			temp = new JButton (third_button);

			temp.addActionListener (hearer);

			temp.setMnemonic (identifier);

			buttons.add (temp);

		}

		else 

			buttons.add (new JLabel ());



		//add other buttons

		temp = new JButton ("Add");

		temp.addActionListener (hearer);

		temp.setMnemonic (identifier);

		buttons.add (temp);

		temp = new JButton ("Remove");

		temp.addActionListener (hearer);

		temp.setMnemonic (identifier);

		buttons.add (temp);



		//put components together

		holder.setLayout (new BorderLayout ());

		if (label != null)

			holder.add (new JLabel (label), BorderLayout.NORTH);

		holder.add (buttons, BorderLayout.SOUTH);

		JScrollPane viewer = new JScrollPane (chosen);

		holder.add (viewer);

		return holder;

	}//method makeButtonList







	/**

	  *getPart returns all the Components of a specified class found within

	  *a particular container.

	  *@param which What class of component to look for.

	  *@param start The container that should be looked inside.

	  *@return A list of all components with the specified class

	**/

	public static LinkedList getPart (Class which, Container start) {



		//get array of all components in the container

		Component [] all_parts = start.getComponents (); 

		LinkedList parts = new LinkedList ();



		//for each component

		for (int i = 0; i < all_parts.length; i++) {



			//if it is a container, recursively call method

			if (all_parts [i] instanceof Container)

				parts.addAll (getPart (which, 

						(Container) all_parts [i]));



			//if it is the proper class, add it to the list

			if (which.isInstance (all_parts [i]))

				parts.add (all_parts [i]);

		}

		return parts;

	}//method getPart









	/**

	  *makeLabeledPanel creates a nicely spaced panel with a title and

	  *the specified component

	  *@param display The component that should be focused on.

	  *@param label The title it should be given.

	  *@return a JPanel with the title and display.

	**/

	public JPanel makeLabeledPanel (Component display, String label, boolean border) {

		JPanel temp = new JPanel (new BorderLayout ()) {
			public boolean isFocusTraversable () {
				return true;
			}
		};


		JLabel title = new JLabel (label);
		title.setFont (title.getFont ().deriveFont (Font.BOLD));
		temp.add (title, BorderLayout.NORTH);



		//if display is a text area, set appropriate defaults

		if (display instanceof JTextArea) {

			((JTextArea) display).setLineWrap (true);

			((JTextArea) display).setWrapStyleWord (true);

			JScrollPane awful = new JScrollPane (display);

			temp.add (awful);

		}



		//doesn't need any extra help

		else if (display instanceof JList || display instanceof JEditorPane)

			temp.add (new JScrollPane (display));


		//this assures that Components such as TextFields or JComboBoxes

		//don't become bloated (they are given only their preferred size vertically

		else {

			JPanel sizer = new JPanel (new BorderLayout ());

			sizer.add (display, BorderLayout.NORTH);

			temp.add (sizer);

		}

		if (border)
			temp.setBorder (titled_panel_border);

		return temp;

	}//method makeLabeledPanel



	public static LinkedList dissectBorder (Border bord) {
		LinkedList l = new LinkedList ();
		if (bord instanceof CompoundBorder) {
			l.addAll (dissectBorder (((CompoundBorder) bord).getInsideBorder ()));
			l.addAll (dissectBorder (((CompoundBorder) bord).getOutsideBorder ()));
		}
		else
			l.add (bord);
		return l;
	}

	/**

	  *increaseList takes an array of values and adds it to a list.

	  *The method it chooses depends on the type of component being

	  *added to.  If you are trying to add an array of length one to a JList
	  *that contains an array of objects that has null values, it will die.

	  *@param add The objects that must be added.

	  *@param holder What holds the choices.

	**/

	public static void increaseList (Object [] add, JComponent holder) {

		if (add == null)

			return;


		synchronized (holder) {

			//if being added to a TextArea, put String version of each

			//choice on a separate line

			if (holder instanceof JTextArea) {

				for (int i = 0; i < add.length; i++)

					((JTextArea) holder).append (add [i] + "\n");

			}



			//if being added to JComboBox, add to combo model

			else if (holder instanceof JComboBox) {

				DefaultComboBoxModel model = (DefaultComboBoxModel)

						((JComboBox) holder).getModel ();

				for (int i = 0; i < add.length; i++)

					model.addElement (add [i]);

			}



			//if being added to JList, add to list model

			else if (holder instanceof JList) {

				DefaultListModel model = (DefaultListModel)

						((JList) holder).getModel ();
				if (add.length == 1 && model.size () != 0) {
					Object [] stuff = model.toArray ();
					int index = GroupUtils.findIfNotPresent (0, stuff.length, 
							stuff, add [0]);
					model.add (index, add [0]);
				}
				else

					for (int i = 0; i < add.length; i++)

						model.addElement (add [i]);

			}
		}

	}//method increaseList



	public static boolean findInList (Object find, JComponent holder, boolean remove) {
		synchronized (holder) {
			if (holder instanceof JTextArea) {
				String text = ((JTextArea) holder).getText ();
				int index = text.indexOf (find + "\n");
				if (index != -1) {
					if (remove) {
						StringBuffer temp = new StringBuffer (text);
						if (index != 0)
							index--;
						temp.replace (index, ((String) find).length () - 2, "");
						((JTextArea) holder).setText (temp.toString ());
					}
					return true;
				}
			}
			else if (holder instanceof JComboBox) {
				DefaultComboBoxModel model = (DefaultComboBoxModel)
						((JComboBox) holder).getModel ();
				if (remove) {
					model.removeElement (find);
					return true;
				}
				else
					return model.getIndexOf (find) > -1;
			}
			else if (holder instanceof JList) {
				DefaultListModel model = (DefaultListModel)
						((JList) holder).getModel ();
				int index = model.indexOf (find);
				if (remove && index > -1) {
					model.remove (index);
					System.out.println (model.size ());
					Thread.currentThread ().yield ();
				}
				return index > -1;
			}
		}
		return false;
	}//method removeFromList
	



	/**

	  *addOkayCancel creates a panel containing buttons saying "OK" and "Cancel"

	  *@param holder The JComponent that will contain the buttons.

	  *@param okay The ActionListener that should be added to the okay button

	  *@return A JPanel containing an okay and cancel button with appropriate listeners

	**/

	public JPanel addOkayCancel (final Container holder, ActionListener okay) {

		JPanel buttons = new JPanel (new FlowLayout (FlowLayout.RIGHT));

		buttons.setBorder (button_border);

		JButton temp = new JButton ("Cancel");



		//cancel can have a generic listener

		temp.addActionListener (new ActionListener () {

			public void actionPerformed (ActionEvent e) {

				if (holder instanceof JDialog) {

					((JDialog) holder).setVisible (false);

					((JDialog) holder).dispose ();

				}

				else if (holder instanceof JFrame) {

					((JFrame) holder).setVisible (false);

					((JFrame) holder).dispose ();
					//System.exit (0);

				}

			}

		});



		//okay gets specialized listener

		JButton temp2 = new JButton ("OK");
		temp2.addActionListener (okay);



		//force them to be the same size

		temp2.setPreferredSize (temp.getPreferredSize ());

		buttons.add (temp2);

		buttons.add (temp);

		return buttons;

	}//method addOKCancel



	/**
	  *getOwner gets the top-most blocking dialog of a window.
	  *@param which The window to start with
	**/
	public static Container getOwner (Container which) {
		/*Window best = null;
		Window owner = ((Window) which).getOwner ();
		while (owner != null) {
			System.out.println ("here");
			if (owner instanceof JDialog)
				best = owner;
			owner = owner.getOwner ();
		}
		if (best != null)
			return (JDialog) best;
		else*/
			return which;
	}

	public static void waitForWhat () {
		while (blocked) {
			Thread.currentThread ().yield ();
		}
		blocked = true;
	}//method waitForWhat
		



	//(AIR didn't write this method)

	/******************************************************************************

	 *  Places the given frame in the center of the screen. 

	 *  

	 *

	 *  @param c	the Component to center

	 ******************************************************************************/



	public static void centerComponent( Component c )

	{



		Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();

		Dimension component_size = c.getSize();



		c.setLocation( screen_size.width/2 - component_size.width/2,

				screen_size.height/2 - component_size.height/2 );



	}


	public static void makeAllTransparent (Container thing) {
		Iterator itty = getPart (JComponent.class, thing).iterator ();
		while (itty.hasNext ())
			((JComponent) itty.next ()).setOpaque (false);
	}

}//class ActivityComponentCreator
