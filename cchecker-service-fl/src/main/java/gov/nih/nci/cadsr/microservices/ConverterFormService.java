/**
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import org.springframework.stereotype.Service;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
/**
 * Create FL form from ALS Form Service.
 * 
 * @author asafievan
 *
 */
@Service
public class ConverterFormService {
	//FIXME not implemented
	public FormDescriptor convertAlsToCadsr(ALSForm alsForm, ALSData alsData) {
		return new FormDescriptor();
	}
}
