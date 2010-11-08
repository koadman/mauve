package org.gel.mauve.chado;

import java.nio.*;
import java.nio.channels.*;
import java.sql.*;
import java.io.*;

import org.gel.mauve.XmfaViewerModel;

public class ChadoWriter {
	private Connection conn;
	
	public ChadoWriter(Connection conn) {
		this.conn = conn;
	}
	
	public void insertAlignment(XmfaViewerModel model) throws SQLException{
		Blob alnmtDestBlob = conn.createBlob();
		PreparedStatement ps = conn.prepareStatement("INSERT INTO alignments VALUES (?)");
		ps.setBlob(1, alnmtDestBlob);
		WritableByteChannel dest = Channels.newChannel(alnmtDestBlob.setBinaryStream(1));
		FileChannel src = null;
		try {
			src = new FileInputStream(model.getSrc()).getChannel();
			dest.write(src.map(FileChannel.MapMode.READ_ONLY, 0, src.size()));
			dest.close();
			ps.close();
			src.close();
			alnmtDestBlob.free();
		} catch (FileNotFoundException e) {
			System.err.println("Bad source file: " + model.getSrc().getName());
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error inserting alignment into chado database");
			return;
		}
	}
	
}
