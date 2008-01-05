package org.gel.air.ja.msg;

/**
  *Interface for matching wildcarded namespaces.<p>
  *	
**/
public interface SubscriptionMatcher {

	/**
	  *Resolves namespace string to a state where it can be compared with others.
 	  *e.g., making sure that trailing slash is always present for pathnames.
	  *This method is called whenever a namespace is added to an instance of SubjectLists.
	  *If no changes need to be made to the subject space, return the passed string.
	  *@param subject  the namespace
	  *@return String  the resolved namespace
	**/
	public String makeSubjectAbsolute (String subject);


	/**
	  *Compares two namespaces for equality.  This method should be implemented
	  *so that order is irrelevant; matchSubjects (one, two) should return the same
	  *thing as matchSubjects (two, one) in all cases.
	  *@param first  the first namespace to compare
	  *@param second  the second namepsace to compare
	  *@return boolean  true if the namespaces are equal; i.e., if messages
	  *                 sent to one should be received by clients subscribed to the other.
	**/
	public boolean matchSubjects (String first, String second);


	/**
	  *Tests to see if a specified string contains wildcards.
	  *This is used by SubjectLists to provide maximum efficiency when
	  *distributing messages.
	  *@param subject  the string to test for wildcards
	  *@return boolean  true if the string contains wildcards, false otherwise
	**/
	public boolean hasWildcards (String subject);

}//interface Matcher
