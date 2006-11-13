/*
 * Created on May 1, 2005
 *
 */
package org.gel.mauve;

/**
 * An interface classes that map a GenBank db_xref to a web database URL
 * @author koadman
 */
public interface DbXrefHandler 
{
	/** returns the static internal instance of this handler */
//	public static DbXrefHandler getInstance();
	/** Returns the name of the database as it appears in GenBank db_xref entries*/
	public String getDbId();
	/** Returns the english name of the database */
	public String getName();
	/** Constructs a URL for the given feature ID */
	public String getURL(String feature_id);
}
