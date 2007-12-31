package org.gel.mauve;

import java.util.prefs.Preferences;

public class PreferencesDump {
	public static void main (String [] args) {
		try {
			Preferences p = Preferences.userNodeForPackage (ModelBuilder.class);
			String [] childName = p.childrenNames ();
			for (int i = 0; i < childName.length; i++) {
				Preferences c = p.node (childName[i]);
				System.out.println ("Node: " + c.name ());
				String [] keys = c.keys ();
				for (int j = 0; j < keys.length; j++) {
					System.out.println (keys[j] + ": "
							+ c.get (keys[j], "Not set"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}
}
