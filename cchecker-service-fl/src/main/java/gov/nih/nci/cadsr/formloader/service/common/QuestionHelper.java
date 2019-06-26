package gov.nih.nci.cadsr.formloader.service.common;

import gov.nih.nci.cadsr.formloader.domain.QuestionDescriptor;
import gov.nih.nci.ncicb.cadsr.common.dto.DataElementTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.QuestionTransferObject;

import org.apache.commons.lang3.StringUtils;

public class QuestionHelper {

	//JR423
	public static final void handleEmptyQuestionText(QuestionTransferObject questdto, DataElementTransferObject cdeDto) {
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
				questionText = "Data Element " + cdeDto + " does not have Preferred Question Text";	//TODO test it and confirm that if is it not cdeDto.getLongName()
			}
			questdto.setLongName(questionText);
			questdto.setPreferredDefinition(questionText);
		}
	}
	
}