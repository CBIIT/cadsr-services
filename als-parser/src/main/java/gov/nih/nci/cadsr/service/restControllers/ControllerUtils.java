/**
 * Copyright (C) 2016 Leidos Biomedical Research, Inc. - All rights reserved.
 */
package gov.nih.nci.cadsr.service.restControllers;

import java.util.List;

/**
 *
 * @author asafievan
 *
 */
public class ControllerUtils {
	/**

	/**
	 * 
	 * @param arr
	 * @return true if an array is null or empty
	 */
	public static boolean isArrayEmpty (List<String> arr) {
		return (arr == null) || (arr.isEmpty());
	}
	/**
	 * 
	 * @param arr
	 * @return true if an array is not null and is not empty
	 */
	public static boolean isArrayNotEmpty (List arr) {
		return (arr != null) && (! arr.isEmpty());
	}
	
}
