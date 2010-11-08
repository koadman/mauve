package org.gel.mauve.chado;

import java.io.*;
import java.nio.channels.*;
import java.sql.*;

import org.gel.mauve.XmfaViewerModel;

public class ChadoReader {
	private Connection conn;

	public ChadoReader(Connection conn){
		this.conn = conn;
	}

	public XmfaViewerModel loadAlignment(String name, File tmpDir) throws SQLException {
		PreparedStatement ps = conn.prepareStatement("SELECT "+name+" FROM alnmts");
		ResultSet rs = ps.executeQuery();
		Blob alnmtSrcBlob = rs.getBlob(1);
		ReadableByteChannel src = Channels.newChannel(alnmtSrcBlob.getBinaryStream());
		File alnmtFile = new File(tmpDir,name+".xmfa");
		FileChannel dest = null;
		XmfaViewerModel model = null;
		try {
			dest = new FileOutputStream(alnmtFile).getChannel();
			System.out.println("Storing alignment in temporary file " + alnmtFile.getAbsolutePath());
			src.read(dest.map(FileChannel.MapMode.READ_WRITE, 0, dest.size()));
			src.close();
			dest.close();
			ps.close();
			alnmtSrcBlob.free();
			model = new XmfaViewerModel(alnmtFile, null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return model;
	}
	
}
