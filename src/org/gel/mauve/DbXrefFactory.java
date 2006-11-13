/*
 * Created on May 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.gel.mauve;

import java.util.HashMap;


/**
 * A factory class that tracks databases known to have a GenBank xref
 * Use this class to map a GenBank db_xref to a URL
 * @author koadman
 */
public final class DbXrefFactory {
	private static DbXrefFactory instance = new DbXrefFactory();
	private HashMap handlers = new HashMap();
	
	public static DbXrefFactory getInstance(){
		return instance;
	}
	public void addHandler(DbXrefHandler dxh)
	{
		if(!handlers.containsKey(dxh.getDbId()))
			handlers.put(dxh.getDbId(),dxh);
	}
	public void removeHandler(String db_id)
	{
		handlers.remove(db_id);
	}
	public String getDbURL(String db_xref) throws UnknownDatabaseException
	{
		DbXrefHandler dxh = getHandler( db_xref );

		// the record id is to the right of the colon
		int loc = db_xref.indexOf(":");
		String feature_id = db_xref.substring(loc + 1, db_xref.length());
		return dxh.getURL(feature_id);
	}
	public String getDbName(String db_xref) throws UnknownDatabaseException
	{
		DbXrefHandler dxh = getHandler( db_xref );
		return dxh.getName();
	}
	public DbXrefHandler getHandler(String db_xref) throws UnknownDatabaseException
	{
		// the db name is stored to the left of the colon
		// get that and match it against registered DB handlers
		int loc = db_xref.indexOf(":");
		String db_id = db_xref.substring(0, loc);
		Object o = handlers.get(db_id);
		if( o == null ){
			throw new UnknownDatabaseException(db_id);
		}
		DbXrefHandler handler = (DbXrefHandler)o;
		return handler;
	}
	
	private DbXrefFactory()
	{
		DbXrefHandler dxh;
		dxh = AsapXrefHandler.getInstance();
		handlers.put(dxh.getDbId(), dxh);

		dxh = EricXrefHandler.getInstance();
		handlers.put(dxh.getDbId(), dxh);
		
		dxh = GiXrefHandler.getInstance();
		handlers.put(dxh.getDbId(), dxh);
	}
	
	public class UnknownDatabaseException extends Exception
	{
		UnknownDatabaseException(String message)
		{
			super("Database not registered: " + message);
		}
	}
}

final class AsapXrefHandler implements DbXrefHandler 
{
	private static AsapXrefHandler instance = new AsapXrefHandler();
	private AsapXrefHandler(){}
	public static DbXrefHandler getInstance()
	{
		return instance;
	}
	public String getDbId()
	{
		return "ASAP";
	}
	public String getName()
	{
		return "ASAPdb";
	}
	public String getURL(String feature_id)
	{
    	String my_url = "https://asap.ahabs.wisc.edu/asap/feature_info.php?FeatureID=" + feature_id;
    	return my_url;
	}
}

final class EricXrefHandler implements DbXrefHandler 
{
	private static EricXrefHandler instance = new EricXrefHandler();
	private EricXrefHandler(){}
	public static DbXrefHandler getInstance()
	{
		return instance;
	}
	public String getDbId()
	{
		return "ERIC";
	}
	public String getName()
	{
		return "ERICdb";
	}
	public String getURL(String feature_id)
	{
    	String my_url = "https://www.ericbrc.org/asap/feature_info.php?FeatureID=" + feature_id;
    	return my_url;
	}
}

final class GiXrefHandler implements DbXrefHandler 
{
	private static GiXrefHandler instance = new GiXrefHandler();
	private GiXrefHandler(){}
	public static DbXrefHandler getInstance()
	{
		return instance;
	}
	public String getDbId()
	{
		return "GI";
	}
	public String getName()
	{
		return "Entrez Protein";
	}
	public String getURL(String feature_id)
	{
    	String my_url = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Link&db=protein&dbFrom=protein&from_uid=" + feature_id;
    	return my_url;
	}
}

