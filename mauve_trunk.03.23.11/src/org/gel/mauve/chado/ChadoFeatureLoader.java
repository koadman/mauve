package org.gel.mauve.chado;

import java.sql.*;

public class ChadoFeatureLoader {
	
	// type_id -> cvterm 
	
	// join 
	
	private Connection conn;
	
	public ChadoFeatureLoader (Connection chado_conn){
		conn = chado_conn;
	}
	
	public void getFeatures(String genus, String species /*...and coming soon, String strain !!!! */) throws SQLException{
		String query = "SELECT organism_id FROM organism WHERE " +
							"genus = "+genus+" AND species = "+species+";";
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery(query);
		String org_id = rs.getString("organism_id");
		st = conn.createStatement();
		query = "SELECT * FROM feature WHERE organism_id = "+org_id;
		query = "SELECT * FROM feature INNER JOIN annotations WHERE feature.unique_name = annotations.organism_id = "+org_id;
		rs = st.executeQuery(query);
		int left = -1;
		int right = -1;
		int strand = 0;
		String type = null;
		while(rs.next()){
			left = rs.getInt("fmin");
			right = rs.getInt("fmax");
			strand = rs.getInt("strand");
			type = rs.getString("name");
			
		} 
	}
	
	private static String QUERY = 
	"SELECT featureprop.feature_id, cvterm.name, featureprop.value, featureloc.fmin, featureloc.fmax, featureloc.strand, feature.name, db.name, dbxref.accession, feature.dbxref_id, feature_dbxref.dbxref_id, dbxref.dbxref_id "+
    	"FROM featureprop, cvterm, featureloc, feature, feature_dbxref, dbxref, db "+ 
    	"WHERE organism_id = ? " +
    			"(featureprop.type_id = cvterm.cvterm_id) AND " +
        		"(featureprop.feature_id = featureloc.feature_id) AND "+ 
                "(featureprop.feature_id = feature.feature_id)  AND " +
                "(featureprop.feature_id = feature_dbxref.feature_id) AND "+
                "(feature_dbxref.dbxref_id = dbxref.dbxref_id) AND "+
                "(dbxref.db_id = db.db_id) "+
                "LIMIT 100 OFFSET 0";

}
