/**
 * Copyright (C) 2018 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.cchecker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author asafievan
 *
 */
public class ParameterValidator {
	public static final int EXPECTED_ID_SEQ_LENGTH = 36;
	private final static Logger logger = LoggerFactory.getLogger(ParameterValidator.class);

	//expected format is UID example: D6CA1C24-3726-672B-E034-0003BA12F5E7
	public static final String idSeqPattern = "^[A-Z0-9]{8}\\-[A-Z0-9]{4}\\-[A-Z0-9]{4}\\-[A-Z0-9]{4}\\-[A-Z0-9]{12}$";

	/**
	 * 
	 * @param idSeq
	 * @return true if validated
	 */
	public static boolean validateIdSeq(String idSeq) {
		if ((StringUtils.isNotBlank(idSeq)) && (idSeq.length() == EXPECTED_ID_SEQ_LENGTH)) {
			Pattern p = Pattern.compile(idSeqPattern);
			Matcher m = p.matcher(idSeq);
			return m.matches();
		}
		else {
			logger.debug("Wrong length");
			return false;
		}
	}
	public static final String  regexLettersCommaSeparated = "((\\s+)?([a-zA-Z]),?)+?$";
	
	public static boolean validateCommaSeparated(String pattern) {
		if (StringUtils.isNotBlank(pattern)) {
			Pattern p = Pattern.compile(regexLettersCommaSeparated);
			Matcher m = p.matcher(pattern);
			return m.matches();
		}
		return true;
	}
	public static boolean validatePublicIdWIthStar(String publicId) {
		if (StringUtils.isNotBlank(publicId)) {
			//digits and star symbols only are allowed in here
			Pattern p = Pattern.compile("^[0-9*]*$");
			Matcher m = p.matcher(publicId);
			return m.matches();
		}
		else return false;
	}	
}
