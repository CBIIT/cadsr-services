/*
 * Copyright (C) Frederick National Laboratory for Cancer Research. - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author asafievan
 *
 */
public class MicroserviceUtils {
	private static final Logger logger = LoggerFactory.getLogger(MicroserviceUtils.class);
	/*
	FORMBUILD-651 exclude the following common sentence punctuation:
	period - .
	comma -  ,
	colon - :
	semi-colon - ;
	quotes - ' and "
	question mark = ?
	dash - - 
	parenthesis - ( and )
	 */
	final static String ignoredRegex = "[\\.,:;\"'\\?\\-\\(\\)]";
	/*
	 * In regex special characters of that set are period, question mark, dash, parenthesis.
	 */
	/**
	 * This method cleans up non-empty strings from ignored characters for further case insensitive comparison.
	 * If any string is empty the strings are not cleaned up from ignored characters for comparison.
	 * Two blank strings are considered equal.
	 * FORMBUILD-651 assumes that the strings are first checked for full equality for a MATCH, 
	 * and after that they are matched by this method for a WARNING, 
	 * if not matched by this method it is an ERROR.
	 * 
	 * @param cadsrStr String
	 * @param alsStr String
	 * @return true is the strings are the same in the sense of requirement FORMBUILD-651.
	 */
	public static boolean compareWithIgnore(String cadsrStr, String alsStr) {
		//here cadsrStr and alsStrs are both not blank
		if ((! (StringUtils.isBlank(cadsrStr)) && (! StringUtils.isBlank(alsStr)))) {
			String cadsrStrCleaned = removeIgnored(cadsrStr);
			String alsStrCleaned = removeIgnored(alsStr);
			return cadsrStrCleaned.equalsIgnoreCase(alsStrCleaned);
		}
		else if (((StringUtils.isBlank(cadsrStr)) && (!(StringUtils.isBlank(alsStr)))) ||
				((! StringUtils.isBlank(cadsrStr)) && ((StringUtils.isBlank(alsStr))))) 
		{
			return false;
		}
		else if ((StringUtils.isBlank(cadsrStr)) && (StringUtils.isBlank(alsStr))) {
			return true;
		}
		return false;
	}
	/**
	 * Returns a string with removed characters we want to ignore.
	 * 
	 * @param orig not null
	 * @return String.
	 */
	protected static String removeIgnored(String orig) {

		String processed = orig.replaceAll(ignoredRegex, "");
		
		return processed;
		
	}

}
