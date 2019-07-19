package gov.nih.nci.cadsr.formloader.service.common;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nih.nci.ncicb.cadsr.common.dto.DataElementTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.QuestionTransferObject;

public class QuestionHelper {

	//JR423
	public static final void handleEmptyQuestionText(QuestionTransferObject questdto, DataElementTransferObject cdeDto) {
		final Logger logger = LoggerFactory.getLogger(QuestionHelper.class.getName());
		String questionText = null;
		//TODO to confirm "if preferred def of data element if it's not null. Otherwise, use long name of data element. if No CDE, use question text"
		if(StringUtils.isEmpty(questdto.getPreferredDefinition()))
		{
			if(cdeDto != null && !StringUtils.isEmpty(cdeDto.getPreferredDefinition())) {
				questionText = cdeDto.getPreferredDefinition();
			} 
			//FORMBUILD-529 
			/*else if(cdeDto != null && !StringUtils.isEmpty(cdeDto.getLongName())) {
				questionText = cdeDto.getLongName();
			}*/
			else
			{
				//FORMBUILD-622 we prevent null pointer on cdeDto for error cases cdeDto is null.
				String longText;
				if (cdeDto == null) {
					logger.warn("QuestionHelper.handleEmptyQuestionText received empty cdeDto for questdto: " + questdto);
					longText = "null";
				}
				else 
					longText = cdeDto.getLongName();
				questionText = "Data Element " + longText + " does not have Preferred Question Text";	//TODO test it and confirm that if is it not cdeDto.getLongName()
			}
			questdto.setLongName(questionText);
			questdto.setPreferredDefinition(questionText);
		}
	}
	
}