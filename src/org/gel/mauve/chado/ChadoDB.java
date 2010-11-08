package org.gel.mauve.chado;


import java.sql.*;

import org.biojava.bio.seq.Sequence;

public class ChadoDB {
	// cvterm_id's 
	private static int contig = 251;
	
	private Connection conn;
	private String url;
	private String user;
	private String pass;
	
	/**
	 * 
	 * @param loc location of PostgreSQL instance
	 * @param user user with access to PostgreSQL
	 * @param pass the user's password
	 * @throws SQLException 
	 */
	public ChadoDB(String loc, String user, String pass) throws SQLException{
		url = "jdbc:postgresql:"+loc+"chado";
		this.user = user;
		this.pass = pass;
		conn = DBUtils.getPostgreSQLConn(url, user, pass);
		
	}
	
	public void reconnect() throws SQLException{
		if (conn.isClosed())
			conn = DBUtils.getPostgreSQLConn(url, user, pass);
	}
	
	public void insertContig(String org_name, String ctgName, Sequence seq) throws SQLException{

		int org_id =  getOrgId(org_name, conn);
		String sql = 
			"INSERT INTO feature (organism_id, name, uniquename,residues,seqlen,type_id)" +
						" VALUES (?,?,?,?,?,?)";
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setInt(1, org_id);
		stmt.setString(2, (ctgName.length()<255?ctgName:ctgName.substring(0, 255)));
		stmt.setString(3, ctgName);
		stmt.setString(4, seq.seqString());
		stmt.setInt(5, seq.length());
		stmt.setInt(6, contig);
		stmt.execute();
	}
	
	
	public void getGenome(String org_name) throws SQLException{
		int org_id = getOrgId(org_name,conn);
		
	}
	
	
	public static int getOrgId(String name, Connection chadoDB) throws SQLException{
	
		ResultSet rs = getOrg(name,chadoDB);
		return rs.getInt("organism_id");
	}
	
	private static ResultSet getOrg(String name, Connection chadoDB) throws SQLException{
		String sql = "SELECT * FROM organism WHERE common_name LIKE '?';";
		PreparedStatement stmt = chadoDB.prepareStatement(sql);
		stmt.setString(1, name);
		return stmt.executeQuery();
	}
	
	
	
	

	
	
}
