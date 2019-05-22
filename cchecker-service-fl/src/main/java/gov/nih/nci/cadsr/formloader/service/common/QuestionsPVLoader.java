package gov.nih.nci.cadsr.formloader.service.common;

import gov.nih.nci.cadsr.formloader.domain.ModuleDescriptor;
import gov.nih.nci.ncicb.cadsr.common.dto.DataElementTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.PermissibleValueV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.QuestionTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ReferenceDocumentTransferObject;
import gov.nih.nci.ncicb.cadsr.common.util.ValueLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuestionsPVLoader implements ValueLoader {
	final int TOTAL = 5;	//total members
	public static final int MODULE_INDEX = 0;
	public static final int QUESTION_INDEX = 1;
	public static final int CDE_INDEX = 2;
	public static final int REF_DOC_INDEX = 3;
	public static final int PV_INDEX = 4;
	List<ModuleDescriptor> modules;           //1    
	List<QuestionTransferObject> questDtos;   //2
	List<DataElementTransferObject> cdeDtos;  //3
	HashMap<String, List<ReferenceDocumentTransferObject>> refdocDtos;  //4
	HashMap<String, List<PermissibleValueV2TransferObject>> pvDtos;     //5

    public QuestionsPVLoader(
    		List<ModuleDescriptor> modules,           //1    
    		List<QuestionTransferObject> questDtos,   //2
    		List<DataElementTransferObject> cdeDtos,  //3
    		HashMap<String, List<ReferenceDocumentTransferObject>> refdocDtos,  //4
    		HashMap<String, List<PermissibleValueV2TransferObject>> pvDtos     //5
    		) {
        this.modules = modules;
        this.questDtos = questDtos;
        this.cdeDtos = cdeDtos;
        this.refdocDtos = refdocDtos;
        this.pvDtos = pvDtos;
    }

	public Object load() {
    	List data = new ArrayList(TOTAL);
    	data.add(modules);
    	data.add(questDtos);
    	data.add(cdeDtos);
    	data.add(refdocDtos);
    	data.add(pvDtos);
        
    	return data;
    }
}
