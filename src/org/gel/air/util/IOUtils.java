package org.gel.air.util;

import java.io.BufferedInputStream;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Hashtable;

public class IOUtils {
	/**
	 * Copies <code>source</code> to <code>dest</code>.
	 * Overwrites or creates <code>dest</code> if necessary
	 * @param source source file
	 * @param dest destination file
	 * @throws IOException 
	 */
	public static void copyFile (File source, File dest) throws IOException {
		System.out.println("AJT0403: Copying...\n\t" + source.getAbsolutePath() +"\nto\n\t" + dest.getAbsolutePath());
	     FileChannel in = null, out = null;
	     try {          
	    	 if (!dest.exists())
	    		 dest.createNewFile();
	          in = new FileInputStream(source).getChannel();
	          out = new FileOutputStream(dest).getChannel();
	 
	          long size = in.size();
	          MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);
	 
	          out.write(buf);
	 
	     } finally {
	          if (in != null)          in.close();
	          if (out != null)     out.close();
	     }
	}
	
	/**
	 * for each arg that starts with a '-', puts it in as key with next arg as value
	 * @param args
	 * @return
	 */
	public static Hashtable <String, String> parseDashPairedArgs (String [] args) {
		int i = 0;
		Hashtable <String, String> pairs = new Hashtable <String, String> ();
		while (i < args.length) {
			if (args [i].charAt (0) == '-') {
				if (args [i].charAt(1) == '-') {
				int equals = args [i].indexOf('=');
				if (equals == -1)
					equals = args [i].length();
				pairs.put(args [i].substring(0, equals), 
						equals == args [i].length() ? "" :
						args [i].substring(equals + 1));
				i++;
				}
				else if (i + 1 < args.length)
					pairs.put(args [i++], args [i++]);
			}
			else
				i++;
		}
		return pairs;
	}
	
	public static void deleteDir (File dir) {
		try {
			File files [] = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files [i].isDirectory())
					deleteDir (files [i]);
				else
					files [i].delete();
			}
			dir.delete();
		} catch (Exception e) {
		}
	}

	public static void writeLongArray (DataOutputStream out, long [] data) {
		try {
			for (int i = 0; i < data.length; i++)
				out.writeLong(data [i]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static long [] readLongArray (DataInputStream in, int length) {
		try {
			long [] data = new long [length];
			for (int i = 0; i < data.length; i++)
				data [i] = in.readLong ();
			return data;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static DataInputStream getDataInputStream (String file) {
		try {
			return new DataInputStream (new BufferedInputStream (new FileInputStream (file)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static DataOutputStream getDataOutputStream (String file) {
		try {
			return new DataOutputStream (new BufferedOutputStream (
					new FileOutputStream (file)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

}
