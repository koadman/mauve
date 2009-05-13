package org.gel.air.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.StringTokenizer;

public class IOUtils {

	public static int BUFFER_SIZE = 100000;
	public static void copyFile (File source, File dest) throws IOException {
	     FileChannel in = null, out = null;
	     try {          
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
	
	public static HashSet <Integer> readIntoSet (String file) {
		HashSet <Integer> vals = new HashSet <Integer> ();
		try {
			BufferedReader in = new BufferedReader (new FileReader (file));
			String input = in.readLine();
			while (input != null) {
				StringTokenizer toke = new StringTokenizer (input, " ");
				toke.nextToken();
				while (toke.hasMoreTokens())
					vals.add(Integer.parseInt(toke.nextToken ()));
				input = in.readLine();
			}
			in.close();
		} catch (Exception e) { 
			e.printStackTrace();
		}
		return vals;
	}
	
	public static long guaranteedSkip (InputStream in, long amount) throws IOException {
		long read = 0;
		while (read < amount && read > -1)
			read += in.skip(amount - read);
		return read;
	}
	
	public static long guaranteedRead (InputStream in, byte [] ret) throws IOException {
		int read = 0;
		while (read < ret.length && read > -1) {
			read += in.read(ret, read, ret.length - read);
		}
		return read;
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
	
	public static DataInputStream getDataInputStream (String file, int buffer) {
		try {
			return new DataInputStream (new BufferedInputStream (
					new FileInputStream (file), buffer));
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
