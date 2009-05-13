package org.gel.air.R;

import java.util.Collection;

public class RUtils {
	
	public static String toRConcat (String name, Collection c) {
		return toRConcat (name, c.toArray());
	}
	
	public static String toRConcat (String name, Object [] data) {
		StringBuffer out = new StringBuffer (name + "=c(");
		for (int i = 0; i < data.length; i++)
			out.append(data [i] + ",");
		out.replace (out.length() - 1, out.length(), ")");
		return out.toString ();
	}

}
