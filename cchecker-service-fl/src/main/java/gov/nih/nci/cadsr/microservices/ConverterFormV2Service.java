/**
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.nih.nci.cadsr.data.ALSData;
import gov.nih.nci.cadsr.data.ALSDataDictionaryEntry;
import gov.nih.nci.cadsr.data.ALSField;
import gov.nih.nci.cadsr.data.ALSForm;
import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.cadsr.formloader.domain.ModuleDescriptor;
import gov.nih.nci.cadsr.formloader.domain.QuestionDescriptor;
import gov.nih.nci.cadsr.formloader.domain.QuestionDescriptor.ValidValue;
import gov.nih.nci.ncicb.cadsr.common.dto.ContextTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.DataElementTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.FormV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.FormValidValueTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ModuleTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ProtocolTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ProtocolTransferObjectExt;
import gov.nih.nci.ncicb.cadsr.common.dto.QuestionTransferObject;
import gov.nih.nci.ncicb.cadsr.common.resource.FormV2;
import gov.nih.nci.ncicb.cadsr.common.resource.FormValidValue;
import gov.nih.nci.ncicb.cadsr.common.resource.Module;
import gov.nih.nci.ncicb.cadsr.common.resource.Question;
/**
 * Create FormV2 object and FL XML from ALSForm.
 * 
 * @author asafievan
 *
 */
@Service
public class ConverterFormV2Service {
	private static final Logger logger = LoggerFactory.getLogger(ConverterFormV2Service.class);
	private static final String convertedFormEnd = "\n</forms>";
	//TODO check requirements for collection values
	private static final String startXmlFormat = "<forms>\n<collectionName>%s</collectionName>\n<collectionDescription>%s</collectionDescription>";
	public static final String fileFormLoaderPrefix = "FormLoader-";
	protected static String REPORT_FOLDER = CCheckerLoadFormService.REPORT_FOLDER;
	
	@Autowired
	private LoadFormService loadFormService;
	protected List<ProtocolTransferObject> mapProtocols(List<ProtocolTransferObjectExt> protocols, String alsProjectName) {
		List<ProtocolTransferObject> protocolList = new ArrayList<>();
		ProtocolTransferObject curr;
		if (protocols != null) {
			for (ProtocolTransferObjectExt protocolExt : protocols) {
				curr = new ProtocolTransferObject();
				curr.setLongName(protocolExt.getProtoIdseq());
				//FIXME fix protocol attributes based on caDSR and ALS data
				curr.setLongName(alsProjectName);
				curr.setProtocolId(alsProjectName);
				curr.setPreferredName(alsProjectName);
				curr.setPreferredDefinition(alsProjectName);
				protocolList.add(curr);
			}
		}
		return protocolList;
		
	}

	private static OutputStream startXMLFile(String formFileNameAppend, String ownerFileName) throws Exception {
		//String separator = System.getProperty("file.separator");
		//String xmlFilename = "dwld" + separator + contextInPath + dir + separator + "FormsDownload-" + formFileNameAppend + ".xml";
		//FIXME dump to /local cchecker sub-directory
		String xmlFilename = formFileNameAppend + ".xml";
		logger.info("xmlFilename: " + xmlFilename);
		String beginXml = String.format(startXmlFormat, ownerFileName, ownerFileName);
		BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(xmlFilename));
		fileOut.write(beginXml.getBytes("UTF-8"));
		fileOut.flush();
		return fileOut;
	}
	
	
	public FormV2 convertFDToFormV2(FormDescriptor formDesc, List<ProtocolTransferObject> protocols) {
		
		FormV2 formV2= startFormV2(formDesc, protocols);

		List<Module> moduleList = new ArrayList<>();

		formV2.setModules(moduleList);
		List<ModuleDescriptor> moduleDescriptorList = formDesc.getModules();
		if (moduleDescriptorList == null)
			return formV2;
		for (ModuleDescriptor moduleDescriptor : moduleDescriptorList) {
			Module module = mapModuleTransferObject();
			moduleList.add(module);
			List<QuestionDescriptor> questionDescriptorList = moduleDescriptor.getQuestions();
			int idxQuestion = 0;
			if (questionDescriptorList != null) {
				idxQuestion = 0;
				List<Question> questionList= new ArrayList<>();
				module.setQuestions(questionList);
				for (QuestionDescriptor questionDescriptor : questionDescriptorList) {
					Question question = mapQuestionTransferObject(questionDescriptor, idxQuestion++);
					questionList.add(question);
					List<ValidValue> validValueList = questionDescriptor.getValidValues();
					if (validValueList != null) {
						List<FormValidValue> formValidValueList = new ArrayList<>();
						question.setValidValues(formValidValueList);
						int idxValidValue = 0;
						for (ValidValue validValue : validValueList) {
							FormValidValue vv = mapValidValue(validValue, idxValidValue++);
							formValidValueList.add(vv);
						}
					}
				}
			}
		}
		return formV2;
	}
	
	protected FormV2 startFormV2(FormDescriptor formDesc, List<ProtocolTransferObject> protocols) {
		FormV2 form = new FormV2TransferObject();
		// QC_IDSEQ we do not need
		// form.setIdseq();
		form.setLongName(formDesc.getLongName()); // LONG_NAME
		// FIXME is this the right info???
		form.setPreferredName(formDesc.getPreferredDefinition()); // PREFERRED_NAME taken from alsForm getFormOid

		// setContext(new ContextTransferObject(rs.getString("context_name")));
		ContextTransferObject contextTransferObject = new ContextTransferObject();
		contextTransferObject.setConteIdseq(formDesc.getContextSeqid()); // CONTE_IDSEQ
		contextTransferObject.setName(formDesc.getContext()); // CONTEXT_NAME
		form.setContext(contextTransferObject);
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		form.setDateCreated(timestamp);
		form.setDateModified(timestamp);

		// multiple protocols will be set later

		form.setFormType("CRF"); // TYPE
		form.setAslName(FormDescriptor.LOAD_TYPE_NEW); // WORKFLOW
		form.setVersion(new Float(1.0)); // VERSION
		form.setPreferredDefinition(formDesc.getPreferredDefinition()); // PREFERRED_DEFINITION
		//TODO to be confirmed
		//form.setCreatedBy("GUEST"); // CREATED_BY is ignored by FL
		
		form.setProtocols(protocols);
		return form;
      }

	protected Module mapModuleTransferObject() {
		Module module = new ModuleTransferObject();
		//we do not need most of the fields in ALS XML
		// module.setModuleIdseq(); // MOD_IDSEQ
		// module.setVersion(); //version
		// module.setConteIdseq(); //context idseq
		// module.setAslName(); //workflow status
		// module.setPreferredName();
		module.setPreferredDefinition("No Definition");
		module.setLongName("Default Module"); // LONG_NAME
		// module.setDisplayOrder(); // DISPLAY_ORDER
		// module.setNumberOfRepeats();//repeat_no
		// module.setPublicId();
		return module;
	}
	
	protected Question mapQuestionTransferObject(QuestionDescriptor questionDescriptor, int displayOrder) {
		Question question = new QuestionTransferObject();
		//the commented fields are not used by FL
		//question.setQuesIdseq();  //QUES_IDSEQ
		question.setLongName(questionDescriptor.getQuestionText());   // LONG_NAME
		//display order is overwritten by FL
		question.setDisplayOrder(displayOrder); // DISPLAY_ORDER
		//question.setAslName();//Workflow
		question.setPreferredDefinition(questionDescriptor.getQuestionText());
		//TODO do we have Mandatory in ALS?
		//question.setMandatory();
		//question.setPublicId();
		//question.setVersion();
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		question.setDateCreated(timestamp);
		question.setDateModified(timestamp);

		//question.setEditable();

		//question.setDeDerived(false);
		String strPublicId = questionDescriptor.getCdePublicId();
		String strVersion = questionDescriptor.getCdeVersion();
		Float cdeQuestionVersion = 1f;
		if(strPublicId !=null)
		{
			try {
				cdeQuestionVersion = Float.valueOf(strVersion);
			}
			catch (Exception e) {
				logger.error("Error in mapQuestionTransferObject on CDE Version: " + strVersion + " " + e);
			}
			DataElementTransferObject dataElementTransferObject =
					new DataElementTransferObject();       
			//dataElementTransferObject.setDeIdseq(); // DE_IDSEQ
			//dataElementTransferObject.setLongCDEName(); // DOC_TEXT 
			dataElementTransferObject.setVersion(cdeQuestionVersion); // VERSION
			//dataElementTransferObject.setLongName(); // DE_LONG_NAME
			dataElementTransferObject.setCDEId(strPublicId);
			//dataElementTransferObject.setAslName();
			//dataElementTransferObject.setPreferredName();
			//dataElementTransferObject.setPreferredDefinition();
			//dataElementTransferObject.setCreatedBy();
			//dataElementTransferObject.setContextName();
			question.setDataElement(dataElementTransferObject); 

			//dataElementTransferObject.setValueDomain(valueDomainV2TransferObject);
		}
		//we do not set up question instructions
        //question.setInstructions(qInstructions);
		return question;
	}
	
	protected FormValidValue mapValidValue (ValidValue validValue, int displayOrder) {
		//FIXME check if this mapping is correct
		FormValidValue fvv = new FormValidValueTransferObject();
		fvv.setFormValueMeaningText(validValue.getMeaningText());
		fvv.setFormValueMeaningDesc(validValue.getPreferredDefinition());
		//FIXME where does value go
		fvv.setPreferredDefinition(validValue.getPreferredDefinition());
		fvv.setShortMeaning(validValue.getValue());
		fvv.setPreferredName(validValue.getValue());
		fvv.setLongName(validValue.getValue());
		//we do not have VM info
//		ValueMeaning vm = new ValueMeaningTransferObject();
//		vm.setLongName(validValue.getValue());
//		fvv.setValueMeaning(vm);
		fvv.setDisplayOrder(displayOrder);
		return fvv;
	}
	/***********************below is FD create code**************************/
	/**
	 * Return a Module
	 * @param alsForm
	 * @param alsData
	 * @return ModuleDescriptor
	 */
	private ModuleDescriptor addModule(ALSForm alsForm, ALSData alsData) {
		ModuleDescriptor defaultModule = new ModuleDescriptor();
		defaultModule.setLongName("Default Module");
		defaultModule.setPreferredDefinition("No Definition");
		defaultModule.setQuestions(addQuestions(alsForm, alsData));
		return defaultModule;
	}

	/**
	 * Return a list of questions
	 * @param alsForm
	 * @param alsData
	 * @return List<QuestionDescriptor>
	 */	
	private List<QuestionDescriptor> addQuestions (ALSForm alsForm, ALSData alsData) {
		List<QuestionDescriptor> questions = new ArrayList<QuestionDescriptor>();
		String formOid = null;
		for (ALSField alsField : alsData.getFields()) {
			formOid = alsField.getFormOid();
			if (formOid != null) {
				if (!"FORM_OID".equals(alsField.getFieldOid())) {
					if (formOid.equals(alsForm.getFormOid())) {
						questions.add(addSingleQuestion(alsField, alsData));
						formOid = null;
					}
				}
			}
		}
		return questions;
	}
	
	
	/**
	 * Return a single question and its valid values filled up
	 * @param alsForm
	 * @param alsData
	 * @return QuestionDescriptor
	 */		
	private QuestionDescriptor addSingleQuestion (ALSField alsField, ALSData alsData) {
		QuestionDescriptor question = new QuestionDescriptor();
		//question.setDisplayOrder(alsField.getOrdinal()); // Not present at the moment
		if (alsField.getPreText()!=null && (!alsField.getPreText().isEmpty())) {
			question.setQuestionText(alsField.getPreText()); 
		}
		String[] idVersion = extractIdVersion(alsField.getDraftFieldName());
		if (idVersion[0] != null) {
			if (NumberUtils.isCreatable(idVersion[0]) && NumberUtils.isCreatable(idVersion[1])
					&& NumberUtils.isCreatable(idVersion[2])) {
				question.setCdePublicId(idVersion[0]);
				question.setCdeVersion(idVersion[1] + "." + idVersion[2]);
			}
		}
		if (alsField.getDataDictionaryName()!=null) {
			question.setValidValues(addValidValues(alsField.getDataDictionaryName(), alsData.getDataDictionaryEntries(), question));
		}
		return question;
	}
	
	
	/**
	 * Return a list of Valid Values for the question
	 * @param alsForm
	 * @param alsData
	 * @return List<ValidValue>
	 */			
	private List<ValidValue> addValidValues (String ddeName, Map<String, ALSDataDictionaryEntry> ddeMap, QuestionDescriptor question) {
		List<ValidValue> validValues = new ArrayList<ValidValue>();
		for (String key : ddeMap.keySet()) {
			if (key.equals(ddeName)) {
				for (int i = 0; i < ddeMap.get(key).getCodedData().size(); i++) {
					ValidValue validVal = question.new ValidValue();
					//validVal.setDisplayOrder(ddeMap.get(key).getOrdinal()); // Not present at the moment 
					validVal.setValue(ddeMap.get(key).getCodedData().get(i));
					validVal.setMeaningText(ddeMap.get(key).getUserDataString().get(i));
					validVal.setPreferredDefinition("No definition");				
					validValues.add(validVal);
				}
			}							
		}			
		return validValues;
	}
	
	
	/**
	 * Splits & extracts the Public ID and Version from a string of format [PIDXXXXX_Vxx_x]
	 * @param draftFieldName
	 * @return String[]
	 */
	protected static String[] extractIdVersion (String draftFieldName) {
		String publicid_prefix = "PID";
		String version_prefix = "_V";		
		String[] idVersionSplitElements = new String[3];
		if (draftFieldName.indexOf(publicid_prefix) > -1 && draftFieldName.indexOf(version_prefix) > -1) {
			String idVn = draftFieldName.substring(draftFieldName.indexOf(publicid_prefix),
					draftFieldName.length());
			String id = idVn.substring(3, idVn.indexOf("_"));
			String version = (idVn.substring(idVn.indexOf(version_prefix) + 2, idVn.length()));
			id = id.trim();
			String[] versionTokens = version.split("\\_");
			version = versionTokens[0] + "." + versionTokens[1];
			idVersionSplitElements[0] = id;
			idVersionSplitElements[1] = versionTokens[0];
			idVersionSplitElements[2] = versionTokens[1];
		}
		return idVersionSplitElements;
	}	
	/**
	 * This method writes XML file with FL XML.
	 * 
	 * @param sessionId
	 * @param contextName
	 * @param contextIdseq
	 * @param alsData
	 * @param selForms
	 * @param protocols
	 * @throws Exception
	 */
	public void prepareXmlFile(String sessionId, String contextName, String contextIdseq, ALSData alsData, 
			List<String> selForms, List<ProtocolTransferObjectExt> protocols) throws Exception {
		String formFileNameAppend = REPORT_FOLDER + fileFormLoaderPrefix + sessionId;
		logger.info("Form XML file: " + formFileNameAppend);
		//start a new file
		OutputStream currFileOut = startXMLFile(formFileNameAppend, alsData.getFileName());
		List<ProtocolTransferObject> protocolList = mapProtocols(protocols, alsData.getCrfDraft().getProjectName());
		List<ALSForm> alsFormList = alsData.getForms();
		for (ALSForm alsForm : alsFormList) {
			if (selForms.contains(alsForm.getFormOid())) {
				FormCollection fc = loadFormService.mapAlsForm(contextName, contextIdseq, alsData, alsForm, protocols);
				if (fc == null) continue;//process the next ALS Form
				List<FormDescriptor> forms = fc.getForms();
				if (forms == null) continue;
				FormDescriptor fd = forms.get(0);//we always have just one form here, but we could set up a loop
				if (fd == null) continue;
				logger.debug("prepareXmlFile Form LongName: " + fd.getLongName());
	
				String currentForm;
				try {
					
					FormV2 crf = convertFDToFormV2(fd, protocolList);
					currentForm = FormConverterUtil.instance().convertFormToV2(crf);
				
				} catch (Exception exp) {
					logger.error("Exception getting CRF: " + exp);
					logger.error("!!! Form IDSEQ skipped: " + fd.getLongName());
					exp.printStackTrace();
					continue;//go to the next form
				}
				
				currentForm = currentForm.replaceAll("\\<\\?xml(.+?)\\?\\>", "").trim();
				currFileOut.write(currentForm.toString().getBytes("UTF-8"));//download the current form
				currFileOut.flush();
			}//one form processing loop
		}
		closeXMLFile(currFileOut);
	}
	private static void closeXMLFile(OutputStream fileOut) {
		try {
			fileOut.write(convertedFormEnd.getBytes("UTF-8"));
			fileOut.flush();
			fileOut.close();				
		} catch (Exception exp) {
			logger.info("Exception converting CRF 2: " + exp);
			exp.printStackTrace();
		}		
	}

}
