package org.gel.mauve.chado;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtils {

	
	public static Connection getPostgreSQLConn(String url, String user, String pass) throws SQLException{
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new SQLException("Can't find PostgreSQL JDBC driver",e);
		}
		return DriverManager.getConnection(url, user, pass);
	}
}
