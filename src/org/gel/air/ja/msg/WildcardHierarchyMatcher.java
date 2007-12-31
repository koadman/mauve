package org.gel.air.ja.msg;


import java.awt.Point;
import java.util.Random;



/**
  *Class for matching wildcarded hierarchical namespaces.  The namespace strings should
  *use the following syntax:<p>
  *
  *<ul>
  *<li>Backslashes ('\') or forward slashes ('/') may be used to separate hierarchy levels.
  *    Note that when makeSubjectAbsolute is called, backslashes will be resolved to forward
  *    slashes, and all subject spaces will end with a forward slash.
  *<li>Asterisks ('*') may be used to match zero or more character within one hierarchy level.
  *    They may never be used to replace back or forward slashes.
  *<li>Ellipses ('...') may be used to match zero or more entire levels of hierarchy.
  *</ul><p>
  *
  *Examples (we used '\' instead of '/' to keep the java compiler from interpreting our examples as the end of the documentation):
  *<ul>
  *<li>'...\' would match absolutely anthing.
  *<li>'stuff*\bill*bob\' would match 'stuff\billbob\', 'stuffy\billy joe bob\', or
  *    'stuffed in a can\bill threw a brick at bob\', but would not match
  *    'estuffy\billbob\' or 'stuffie\'
  *<li>'frog\...\parakeet\' would match 'frog\denture\albino\parakeet\' or 'frog\parakeet\',
  *     but would not match 'froggy\parakeet\' or '\foo\frog\blah\parakeet\'
  *<li>'*lunacy*\...\' would match 'rutabaga*calamity\' or 'endlesslunacyabounds\this\that\the other thing\',
	  but would not match 'dogs\this\that\'
  *</ul><p>
  *
  *@author the twins (twins@isx.com)
**/
public class WildcardHierarchyMatcher implements SubscriptionMatcher {

	/**
	  *Converts all backslashes to forward slashes, and ensures that
	  *the string ends with a forward slash.
	  *@param name  the subject namespace to make fully qualified
	  *@return String  the fully qualified namespace
	**/
	public String makePathAbsolute (String name) {
		//name = name.replace ('\\', '/');
		if (name.charAt (0) == '/')
			name = name.substring (1);
		if (name.charAt (name.length () - 1) != '/')
			name += '/';
		return name;
	}//method makePathAbsolute

	/**
	  *Converts all backslashes to forward slashes, and ensures that
	  *the string ends with a forward slash.  This method implements the
	  *Matcher interface method, and is equivalent to makePathAbsolute.
	  *@param name  the subject namespace to make fully qualified
	  *@return String  the fully qualified namespace
	**/
	public String makeSubjectAbsolute (String name) {
		return makePathAbsolute (name);
	}

	/**
	  *Returns true if the string contains an asterisk ('*') or ellipsis ('...').
	  *This method implements the Matcher interface method.
	  *@param subject  the namespace to check for wildcards
	  *@return boolean  true if the namespace contained wildcards
	**/
	public boolean hasWildcards (String subject) {
		return subject.indexOf ("*") != -1 || subject.indexOf ("...") != -1;
	}//method hasWildcards

	/**
	  *Compares two namespaces to see if messages sent to one should be received by
	  *parties subscribed to the other.  Implements the Matcher interface method.
	  *@param one  the first namespace to compare
	  *@param two  the second namespace to compare
	  *@return boolean  true if the namespaces match
	**/
	public boolean matchSubjects (String one, String two) {
		int index1 = 0;
		int index2 = 0;
		int size1 = one.length ();
		int size2 = two.length ();
		int type1 = 0;
		int type2 = 0;
		int slash1 = 0;
		int slash2 = 0;
		while (index1 < size1 && index2 < size2) {
			type1 = getCharType (one, index1, size1);
			type2 = getCharType (two, index2, size2);
			if (type1 == 0 && type2 == 0 && one.charAt (index1) !=
					two.charAt (index2))
				return false;
			if (type1 == 2 || type2 == 2)
				return dotMatch (one, two, index1, index2, size1, size2);
			if (type1 == 1 || type2 == 1) {
				Point p = null;
				if ((p = asteriskMatch (one, two, index1, index2, false)) != null) {
					index1 = p.x + 1;
					index2 = p.y + 1;
					continue;
				}
				else
					return false;
			}
			index1++;
			index2++;
		}
		while (index1 < size1) {
			if (index1 + 3 < size1 && one.substring (index1, index1 + 3).equals ("..."))
				index1 += 4;
			else
				return false;
		}
		while (index2 < size2) {
			if (index2 + 3 < size2 && two.substring (index2, index2 + 3).equals ("..."))
				index2 += 4;
			else
				return false;
		}
		return true;
	}//method matchSubjects


	/**
	  *Compares two strings (possibly with asterisks) from the specified starting indices
	  *to the next forward slash to see if they're equivalent.
	  *@param one  the first namespace
	  *@param two  the second namespace
	  *@param start1  the index to start at in the first namespace
	  *@param start2  the index to start at in the second namespace
	  *@param correct_end  true to always return the index of the next slash
	  *       in string two
	  *@return Point  there are three possible return configurations:
	  *<ul>
	  *<li>A point with two non-negative integers.  This indicates the strings
	  *matched, and the two values in the point are the indices of the next slashes
	  *in each string.
	  *<li>A point with one non-negative integer, in the y field.  This indicates that
	  *the strings didn't match, and the y value will be the index of the next slash in
	  *the second string.  Will happen if strings don't match, and correct_end is true.
	  *<li>Null.  This indicates that the strings didn't match.  Will happen if strings
	  *don't match, and correct_end is false.
	**/
	protected Point asteriskMatch (String one, String two, int start1, int start2,
			boolean correct_end) {
		int start_from = start1;
		int slash1 = one.indexOf ('/', start1);
		int slash2 = two.indexOf ('/', start2);
		int end1 = slash1 - 1;
		int end2 = slash2 - 1;
		while (start1 <= end1 && start2 <= end2) {
			char c1 = one.charAt (start1);
			char c2 = two.charAt (start2);
			if (c1 == '*' || c2 == '*')
				break;
			if (c1 != c2)
				return correct_end ? new Point (-1, slash2) : null;
			start1++;
			start2++;
		}
		while (end1 > start1 && end2 > start2) {
			char c1 = one.charAt (end1);
			char c2 = two.charAt (end2);
			if (c1 == '*' || c2 == '*')
				break;
			if (c1 != c2)
				return correct_end ? new Point (-1, slash2) : null;
			end1--;
			end2--;
		}
		char st1 = one.charAt (start1);
		char st2 = two.charAt (start2);
		char en1 = one.charAt (end1);
		char en2 = two.charAt (end2);
		if ((en1 == '*' && st2 == '*') || (en2 == '*' && st1 == '*') ||
				(st1 == '*' && en1 == '*' && (start1 == end1 ||
				two.substring (start2, end2).indexOf ('*') > -1)) ||
				(st2 == '*' && en2 == '*' && (start2 == end2 ||
				one.substring (start1, end1).indexOf ('*') > -1)))
			return new Point (slash1, slash2);
		if (st2 == '*') {
			String temp = one;
			one = two;
			two = temp;
			int t1 = start1;
			start1 = start2;
			start2 = t1;
			t1 = end1;
			end1 = end2;
			end2 = t1;
			t1 = slash1;
			slash1 = slash2;
			slash2 = t1;
		}
		while (start1 < end1 && start2 < end2) {
			if (one.charAt (start1) == '*')
				start_from = ++start1;
			else {
				if (one.charAt (start1) == two.charAt (start2))
					start1++;
				else
					start1 = start_from;
				start2++;
			}
		}
		boolean ended = start1 == end1;
		if (!ended) {
			if (correct_end)
				start1 = -1;
			else
				return null;
		}
		return new Point (slash1, slash2);
	}//method asteriskMatch


	/**
	  *Compares two strings (at least one of which contains an ellipsis) starting
	  *at the specified indices.
	  *@param one  the first namespace
	  *@param two  the second namespace
	  *@param start1  the index to start at in the first namespace
	  *@param start2  the index to start at in the second namespace
	  *@param size1  the size of the first string
	  *@param size2  the size of the second string
	  *@return boolean  true if the strings match from the specified indices on
	**/
	protected boolean dotMatch (String one, String two, int start1, int start2,
			int size1, int size2) {
		int end1 = one.lastIndexOf ("/", size1 - 2) + 1;
		int end2 = two.lastIndexOf ("/", size2 - 2) + 1;
		int lastslash1 = size1 - 1;
		int lastslash2 = size2 - 1;
		Point p = null;
		while (start1 < end1 && start2 < end2) {
			if ((start1 + 3 < size1 && one.substring (start1, start1 + 3).equals ("...")) ||
					(start2 + 3 < size2 && two.substring (start2, start2 + 3).equals ("...")))
				break;
			p = asteriskMatch (one, two, start1, start2, true);
			if (p.x == -1)
				return false;
			start1 = p.x + 1;
			start2 = p.y + 1;
		}
		while (end1 > start1 && end2 > start2) {
			if ((end1 + 3 < size1 && one.substring (end1, end1 + 3).equals ("...")) ||
					(end2 + 3 < size2 && two.substring (end2, end2 + 3).equals ("...")))
				break;
			p = asteriskMatch (one, two, end1, end2, true);
			if (p.x == -1)
				return false;
			lastslash1 = end1 - 1;
			lastslash2 = end2 - 1;
			end1 = one.lastIndexOf ("/", end1 - 2) + 1;
			end2 = two.lastIndexOf ("/", end2 - 2) + 1;
		}

		boolean st1 = start1 + 3 < size1 && one.substring (start1, start1 + 3).equals ("...");
		boolean st2 = start2 + 3 < size2 && two.substring (start2, start2 + 3).equals ("...");
		boolean en1 = end1 + 3 < size1 && one.substring (end1, end1 + 3).equals ("...");
		boolean en2 = end2 + 3 < size2 && two.substring (end2, end2 + 3).equals ("...");
		if ((st1 && en2) || (st2 && en1) || (st1 && en1 && (start1 == end1 ||
				two.substring (start2, end2).indexOf ("...") > -1)) ||
				(st2 && en2 && (start2 == end2 ||
				one.substring (start1, end1).indexOf ("...") > -1)))
			return true;
		if (en2 || st2) {
			String temp = one;
			one = two;
			two = temp;
			int t1 = start1;
			start1 = start2;
			start2 = t1;
			end2 = lastslash1;
			end1 = lastslash2;
			t1 = size1;
			size1 = size2;
			size2 = t1;
		}
		else {
			end1 = lastslash1;
			end2 = lastslash2;
		}
		int starts_from = start1;
		while (start1 < end1 && start2 < end2) {
			if (start1 + 3 < size1 && one.substring (start1, start1 + 3).equals ("...")) {
				starts_from = start1 + 4;			
				start1 += 4;
			}
			else {
				if ((p = asteriskMatch (one, two, start1, start2, true)).x != -1) 
					start1 = p.x + 1;
				else
					start1 = starts_from;
				start2 = p.y + 1;
			}
		}
		while (start1 < end1) {
			if (start1 + 3 < size1 && one.substring (start1, start1 + 3).equals ("..."))
				start1 += 4;
			else
				return false;
		}
		return start1 == end1 + 1;
	}//method dotMatch

	/**
	  *Determines what type of character (asterisk, ellipsis, or other) starts at a
	  *specified index in a string.
	  *@param s  the string
	  *@param index  the index to check
	  *@param size  the size of the string
	  *@return int  0 for a "normal" (non-wildcard) character, 1 for an asterisk,
	  *             and 2 for an ellipsis.
	**/
	protected int getCharType (String s, int index, int size) {
		if (s.charAt (index) == '*')
			return 1;
		if (index + 3 < size && s.substring (index, index + 3).equals ("..."))
			return 2;
		return 0;
	}//method getCharType

	/*public String makeDumbString (String [] words) {
		int levels = (int) (Math.random () * 6) + 1;
		String s = "";
		for (int i = 0; i < levels; i++) {
			if (Math.random () < 0.2)
				s += ".../";
			else {
				StringBuffer two_words = new StringBuffer (words [(int) (Math.random () * words.length)] +
						words [(int) (Math.random () * words.length)]);
				int stars = (int) (new Random ().nextGaussian () + 1.25);
				for (int j = 0; j < stars; j++) {
					int offset = (int) (Math.random () * two_words.length ());
					int len = (int) (Math.random () * (two_words.length () - offset));
					two_words.replace (offset, len + offset, "*");
				}
				s += two_words.toString () + "/";
			}
		}
		return s;
	}*///method makeDumbString

			

	/*public static void main (String [] args) {
		String [] words = {"blah", "foo", "buzzzy", "bij", "stuff", "arthur",
			"bah"};
		if (loop) {
			String [] matchers = new String [20000];
			for (int i = 0; i < 20000; i++)
				matchers [i] = makeDumbString (words);
			long time = System.currentTimeMillis ();
			for (int i = 0; i < 19999; i++)
				matchSubjects (matchers [i], matchers [i + 1]);
			time = System.currentTimeMillis ();
			for (int i = 0; i < 19999; i++)
				TipcSvc.matchSubjects (matchers [i].substring (0, matchers [i].length () -1),
						matchers [i + 1].substring (0, matchers [i + 1].length () - 1));
		}
		else
			System.out.println (matchSubjects (args [0], args [1]));
	}*/
}