package gov.nih.nci.cadsr.formloader.service.common;


import gov.nih.nci.cadsr.formloader.common.JsonUtil;
import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.cadsr.formloader.domain.ModuleDescriptor;
import gov.nih.nci.cadsr.formloader.domain.QuestionDescriptor;
import gov.nih.nci.cadsr.formloader.repository.impl.FormLoaderRepositoryImpl;
import gov.nih.nci.ncicb.cadsr.common.dto.DataElementTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.PermissibleValueV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.QuestionTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ReferenceDocumentTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ValueMeaningV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.util.ValueHolder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.CodeSource;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper is generally very application specific (i.e. very FormLoader specific).
 */
public class FormLoaderHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(FormLoaderHelper.class.getName());
	
	private static Properties properties = null;
	
	public static String getProperty(String key) {
		
		if (properties == null)
			properties = FormLoaderHelper.loadPropertiesFromClassPath();
		
		return (properties == null) ? "" : properties.getProperty(key);	    
	}
	
	public static Properties loadPropertiesFromClassPath() {
		Properties props = new Properties();
               
        try { 
        	InputStream	in = FormLoaderHelper.class.getClassLoader().getResourceAsStream("formloader.properties");
            props.load(in);
        } catch ( Exception e ) {
        	logger.error("Error!! Unable to open property file from classpath");
        }
        
        return props;
	}
	
	public static String checkInputFile(String xmlPath, String xmlName) 
			throws FormLoaderServiceException
	{
		if (xmlPath == null || xmlPath.length() == 0)
			throw new FormLoaderServiceException(FormLoaderServiceException.ERROR_FILE_INVALID,
					"Input file path on server is null or empty. Unable to validate form content.");

		if (xmlName == null || xmlName.length() == 0)
			throw new FormLoaderServiceException(FormLoaderServiceException.ERROR_FILE_INVALID,
					"Input file name is null or empty. Unable to validate form content.");
		
		String xmlPathName = xmlPath.endsWith("/") ? xmlPath + xmlName : xmlPath + "/" + xmlName;
		
		//xmlPath = FormLoaderHelper.resolveWindowsPathIfNecessary(xmlPath);
		
		File input = new File(xmlPathName);
		if (input == null || !input.exists() || !input.canRead())
			throw new FormLoaderServiceException(FormLoaderServiceException.ERROR_FILE_INVALID,
					"Input file [" + xmlPathName + "] is invalid. Does it exist? Unable to proceed to load form collection.");


		return xmlPathName;
	}
	
	public static String resolveWindowsPathIfNecessary(String pathName) {
		
		if (pathName == null) 
			return pathName;
		
		String osName = System.getProperty("os.name");;
		
		if (osName == null || !osName.startsWith("Windows"))
			return pathName;
		
		if (pathName.contains(":")) 
			return pathName;
		
		String workingDir = System.getProperty("user.dir");
	
		if (!workingDir.endsWith("/"))
			workingDir += "/";
		
		if (pathName.startsWith("."))
			pathName = pathName.substring(1);
		
		if (pathName.startsWith("/") || pathName.startsWith("\\"))
			pathName = pathName.substring(1);
		
		return workingDir + pathName;
	}

	public static XmlValidationError filePahtNameContainsError(String filePathName) {
		if (filePathName == null)
			return new XmlValidationError(XmlValidationError.XML_FILE_INVALID, "File path name is null", 0);
		else {
			File xml = new File(filePathName);
			if (!xml.exists() || !xml.canRead()) {
				return new XmlValidationError(XmlValidationError.XML_FILE_NOT_FOUND, 
						"File doesn't exist or can't be read at " + filePathName, 0);
			}
		}
		
		//if we're here, we're good
		return null;
	}
	
	public static String checkFilePathName(String filePathName) {
		if (filePathName == null)
			return "File path name is null";
		else {
			File xml = new File(filePathName);
			if (!xml.exists() || !xml.canRead()) {
				return "File doesn't exist or can't be read at " + filePathName;
			}
		}
		
		//if we're here, we're good
		return "";
	}
	
	public static XmlValidationError inputFilePathNamesContainsError(String xmlPathName, String xsdPathName) {
		
		XmlValidationError error = filePahtNameContainsError(xmlPathName);
		if (error == null) {
			error = filePahtNameContainsError(xsdPathName);
		}
		
		return error;
	}
	
	/**
	 * This mimicks the xpath function normalize-space(): whitespace normalized by stripping leading and 
	 * trailing whitespace and replacing sequences of whitespace characters by a single space.
	 * 
	 * @param input
	 * @return
	 */
	public static String normalizeSpace(String input) {
		if (input == null || input.length() == 0)
			return input;
		
		String in = input.trim();
		int len = in.length();
		
		char[] outArr = new char[len+1];
		int outIdx = 0;
		int spaceCnt = 0;
		for (int i = 0; i < len; i++) {
			char c = in.charAt(i);
			
			if (c != 32) {
				outArr[outIdx++] = c;
				spaceCnt = 0; 
				continue;
			}
			
			spaceCnt++;
			if (spaceCnt == 1)
				outArr[outIdx++] = c;
		}
		
		return new String(outArr).trim();
		
	}
	
	
	public static void saveCollectionListToFile (List<FormCollection> colls) {
		final String fileName = "collectionList.ser";

		String filePathName = fileName;
		File objs = new File(filePathName);

		try {
			if (!objs.exists()) {
				
				// Serialize data object to a file
				ObjectOutputStream out = new ObjectOutputStream(
						new FileOutputStream(filePathName));
				out.writeObject(colls);
				/*
				 * Iterator ite = pvs.keySet().iterator(); while (ite.hasNext())
				 * { out.writeObject(pvs.get(ite.next())); }
				 */
				out.close();

			}
		} catch (FileNotFoundException fne) {
			System.out.println(fne);
		} catch (IOException ioe) {
			System.out.println(ioe);
		}

		
	}
	
	public static void saveFormListToFile (List<FormDescriptor> forms) {
		final String fileName = "formList.ser";

		String filePathName = fileName;
		File objs = new File(filePathName);

		try {
			if (!objs.exists()) {
				
				// Serialize data object to a file
				ObjectOutputStream out = new ObjectOutputStream(
						new FileOutputStream(filePathName));
				out.writeObject(forms);
				/*
				 * Iterator ite = pvs.keySet().iterator(); while (ite.hasNext())
				 * { out.writeObject(pvs.get(ite.next())); }
				 */
				out.close();

			}
		} catch (FileNotFoundException fne) {
			System.out.println(fne);
		} catch (IOException ioe) {
			System.out.println(ioe);
		}

		
	}

	public static List<FormDescriptor> readFormListFromFile () {
		final String fileName = "formList.ser";

		String filePathName = fileName;
		File objs = new File(filePathName);
		List<FormDescriptor> forms = null;

		try {
			InputStream file = new FileInputStream(filePathName);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream (buffer);

			//deserialize the List
			forms = (List<FormDescriptor>)input.readObject();

		}
		catch(ClassNotFoundException ex){

		}
		catch(IOException ex){

		}


		return forms;

	}
	
	//For testing
	public static List<FormCollection> readCollectionListFromFile () {
		final String fileName = "collectionList.ser";

		String filePathName = fileName;
		File objs = new File(filePathName);
		List<FormCollection> colls = null;

		try {
			InputStream file = new FileInputStream(filePathName);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream (buffer);

			//deserialize the List
			colls = (List<FormCollection>)input.readObject();

		}
		catch(ClassNotFoundException ex){

		}
		catch(IOException ex){

		}


		return colls;

	}
	
	public static List<FormCollection> sortCollectionsByDate(List<FormCollection> collections) {
		
		Collections.sort(collections, new Comparator<FormCollection>(){
			public int compare(FormCollection o1, FormCollection o2) {
		        return o1.getDateCreated().compareTo(o2.getDateCreated());
		    }
		});
		
		return collections;
	}
	
	public static List<FormCollection> reversSortCollectionsByDate(List<FormCollection> collections) {
		
		List<FormCollection> colls = FormLoaderHelper.sortCollectionsByDate(collections);
		Collections.reverse(colls);
		
		return colls;
	}
	
	public static List<FormCollection> sortCollectionsByName(List<FormCollection> collections) {

		Collections.sort(collections, new Comparator<FormCollection>(){
			public int compare(FormCollection o1, FormCollection o2) {
				return o1.getNameWithRepeatIndicator().compareTo(o2.getNameWithRepeatIndicator());
			}
		});

		return collections;
	}
	
	public static List<FormCollection> reverseSortCollectionsByName(List<FormCollection> collections) {

		List<FormCollection> colls = FormLoaderHelper.sortCollectionsByName(collections);
		Collections.reverse(colls);

		return collections;
	}
	
	/**
	 * Format version number into #0.0 form
	 * @param versionNumber
	 * @return
	 */
	public static String formatVersion(float versionNumber) {
		NumberFormat formatter = new DecimalFormat("#0.0");
		return formatter.format(versionNumber);
	}
	
	public static boolean samePublicIdVersions(String publicId, String version, int publicIdInt, float versionInFloat) {
		if (publicId == null && publicIdInt > 0)
			return false;
		
		if (version == null && versionInFloat > 0)
			return false;
		
		try {
			if (Integer.parseInt(publicId) != publicIdInt)
				return false;
			
			if (Float.parseFloat(version) != versionInFloat)
				return false;
		} catch (NumberFormatException ne) {
			return false;
		}
		
		return true;
	}
	
	public static void OutputJaxpImplementationInfo() {
	    //logger.error(getJaxpImplementationInfo("DocumentBuilderFactory", DocumentBuilderFactory.newInstance().getClass()));
	    logger.error(getJaxpImplementationInfo("XPathFactory", XPathFactory.newInstance().getClass()));
	    //logger.error(getJaxpImplementationInfo("TransformerFactory", TransformerFactory.newInstance().getClass()));
	    logger.error(getJaxpImplementationInfo("SAXParserFactory", SAXParserFactory.newInstance().getClass()));
	}

	private static String getJaxpImplementationInfo(String componentName, Class componentClass) {
	    CodeSource source = componentClass.getProtectionDomain().getCodeSource();
	    return MessageFormat.format(
	            "{0} implementation: {1} loaded from: {2}",
	            componentName,
	            componentClass.getName(),
	            source == null ? "Java Runtime" : source.getLocation());
	}
	
	/**
	 * Collect public ids for questions and their cde from all modules of a form, so that
	 * we could query database with a list.
	 * @param modules
	 * @param questPublicIds
	 * @param questCdePublicIds
	 * @param formLoadType
	 */
	private static void collectPublicIdsForModules(List<ModuleDescriptor> modules, 
			List<String> questPublicIds, List<String> questCdePublicIds, String formLoadType) {
		
		if (modules == null) {
			logger.debug("Module list is null. Unable to collect public ids.");
			return;
		}
			
		for (ModuleDescriptor module : modules) {
			List<QuestionDescriptor> questions = module.getQuestions();
			
			for (QuestionDescriptor question : questions) {
				String questPubId = question.getPublicId();
				//Only need to validate question public id + version if it's an update form
				if (formLoadType.equals(FormDescriptor.LOAD_TYPE_UPDATE_FORM)
						&& questPubId != null && questPubId.length() > 0)
					questPublicIds.add(questPubId);
				
				String cdePublicId = question.getCdePublicId();
				if (cdePublicId != null && cdePublicId.length() > 0)
					questCdePublicIds.add(cdePublicId); 
				else
					question.addMessage("Question has not associated data element public id. Unable to validate");				
			}
			
			logger.debug("Collected " + questPublicIds.size() + " question public ids and " + questCdePublicIds.size() +
					" cde public ids in module [" + module.getPublicId() + "|" + module.getVersion() + "]");
		}
		
	}

	/**
	 * Populate PV and its VM as required for a form's question.
	 * @param form
	 * @param repository
	 * @comment Created specifically for JR417.
	 * @return
	 */
	public static final ValueHolder populateQuestionsPV(FormDescriptor form, FormLoaderRepositoryImpl repository) {
		String formLoadType = form.getLoadType();

		List<String> questPublicIds = new ArrayList<String>();
		List<String> questCdePublicIds = new ArrayList<String>();
		List<ModuleDescriptor> modules = form.getModules();
		collectPublicIdsForModules(modules, questPublicIds, questCdePublicIds, formLoadType);
		
		List<QuestionTransferObject> questDtos = repository.getQuestionsByPublicIds(questPublicIds);
//		System.out.println("FormLoaderHelper.java 0");
		List<DataElementTransferObject> cdeDtos = repository.getCDEsByPublicIds(questCdePublicIds);
		
//		System.out.println("FormLoaderHelper.java 1");
		HashMap<String, List<ReferenceDocumentTransferObject>> refdocDtos = 
				repository.getReferenceDocsByCdePublicIds(questCdePublicIds);
//		System.out.println("FormLoaderHelper.java 2");
		List<String> vdSeqIds = new ArrayList<String>();
//		System.out.println("FormLoaderHelper.java 3");
		if(cdeDtos != null) {	//JR417 not related to the ticket but just avoiding NPE during the test!
//			System.out.println("FormLoaderHelper.java 4");
			for (DataElementTransferObject de: cdeDtos) {
//				System.out.println("FormLoaderHelper.java 5");
				if(de != null) {	//JR417 not related to the ticket but just avoiding NPE during the test!
//					System.out.println("FormLoaderHelper.java 6");
					String vdseqId = de.getVdIdseq();
					if (vdseqId != null && vdseqId.length() > 0) {
						vdSeqIds.add(vdseqId);
					}
				}
			}
		}
		
		HashMap<String, List<PermissibleValueV2TransferObject>> pvDtos = 
				repository.getPermissibleValuesByVdIds(vdSeqIds);	//JR417 pv has the vpIdseq and vm has the vmIdseq after this successful call!

		ValueHolder vh = new ValueHolder(new QuestionsPVLoader(modules, questDtos, cdeDtos, refdocDtos, pvDtos));	//JR417 questDtos is null for some reason, but it's ok

		return vh;
	}
	
	/**
	 * Get the PV based on the valid value's passed.
	 * @param form
	 * @param repository
	 * @return
	 * @throws Exception
	 * @comment The assumption is that, the order of the valid value list is the same as the PV's. Created specifically for JR417.
	 */
	public static final PermissibleValueV2TransferObject getValidValuePV(QuestionDescriptor.ValidValue vValue, HashMap<String, List<PermissibleValueV2TransferObject>> pvDtos) throws Exception {
		PermissibleValueV2TransferObject ret = null;
		
		if(vValue == null) throw new Exception("Valid value is null.");
		if(pvDtos == null) throw new Exception("Permissible Values DTO list is null.");
		
		//ret = (PermissibleValueV2TransferObject) pvDtos.get(vvIndex);	//key is not 0,1, ... but F169098A-E8D2-306E-E034-0003BA3F9857, F54516A5-2717-25D2-E034-0003BA3F9857 and 85FA5C84-F008-BF1A-E040-BB89AD43366C
		//find the first PV that has the same value as the VV (c.f. https://wiki.nci.nih.gov/display/caDSR/Form+Builder+4.1+-+Form+Loader+System+Use+Cases)
        ArrayList<PermissibleValueV2TransferObject> pvs = null;
        PermissibleValueV2TransferObject pv = null;
		Iterator it = pvDtos.entrySet().iterator();
		boolean found = false;
		String value = null;
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        pvs = (ArrayList<PermissibleValueV2TransferObject>) pair.getValue();
	        for(int i=0; i<pvs.size(); i++) {
		        pv = pvs.get(i);
		        logger.debug(pair.getKey() + " = " + pv.getValue());
		        //FORMBUILD-448 failing to identify a pv based on the following comparison
		        //pv long name should be compared to vValue Meaning Text to find reliable matches.
		        if(pv.getValueMeaningV2() != null)
		        	value = pv.getValueMeaningV2().getLongName();
		        	//value = pv.getValueMeaningV2().getPreferredDefinition();
		        logger.debug("pv vm value = [" + value + "] vValue.getMeaningText() = [" + vValue.getMeaningText() + "]");
		        if(value != null && value.equals(vValue.getMeaningText())) {
		        	ret = pv;
		        	found = true;
		        	break;
		        }
	        }
	        if(found) {
	        	break;
	        }
	    }
		
		return ret;
	}

	public static final ValueMeaningV2TransferObject getVVPVVM(int vvIndex, int pvIndex, HashMap<String, List<PermissibleValueV2TransferObject>> pvDtos) throws Exception {
		ValueMeaningV2TransferObject ret = null;
		
		if(vvIndex < 0) throw new Exception("VV index must be zero or greater.");
		if(pvIndex < 0) throw new Exception("PV index must be zero or greater.");
		if(pvDtos == null) throw new Exception("Permissible Values DTO list is null.");
		
        ArrayList<PermissibleValueV2TransferObject> pvs = null;
        PermissibleValueV2TransferObject pv = null;
		Iterator it = pvDtos.entrySet().iterator();
		boolean found = false;
		int vvCount = 0;
		int pvCount = 0;
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        if(pvIndex == pvCount) {
		        pvs = (ArrayList<PermissibleValueV2TransferObject>) pair.getValue();
		        for(int i=0; i<pvs.size(); i++) {
			        pv = pvs.get(i);
			        logger.debug(pair.getKey() + " = " + pv.getValue());
			        if(pvIndex == pvCount) {
			        	ret = (ValueMeaningV2TransferObject) pv.getValueMeaningV2();
			        	found = true;
			        	break;
			        }
			        pvCount++;
		        }
		        if(found) {
		        	break;
		        }
	        }
	        vvCount++;
	        pvCount = 0;
	    }
		
		return ret;
	}
	
	/**
	 * Duplicated a module based on a repeatCount and an index.
	 * @param repeatCount
	 * @param modules
	 * @comment Created specifically for JR366.
	 */
	public static final List<ModuleDescriptor> handleModuleRepeat(List<ModuleDescriptor> modules) {
		int count = 1;
		List<ModuleDescriptor> ret = new ArrayList();
		for (ModuleDescriptor module : modules) {
			logger.debug("count [" + module.toString() + "]\n");
			if(module.getMaximumModuleRepeat() != null && Integer.valueOf(module.getMaximumModuleRepeat()) > 0) {
				int repeatCount = Integer.valueOf(module.getMaximumModuleRepeat());
				for(int i=0; i<repeatCount; i++) {
					//ModuleDescriptor cloned = (ModuleDescriptor) SerializationUtils.clone(module);
					ModuleDescriptor cloned = (ModuleDescriptor)JsonUtil.clone(module);
					//cloned.setPublicId(null);
					ret.add(cloned);
				}
			}
			ret.add(module);
		}
		return ret;
	}

}