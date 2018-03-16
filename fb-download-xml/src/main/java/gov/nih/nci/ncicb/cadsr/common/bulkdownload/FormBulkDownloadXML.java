package gov.nih.nci.ncicb.cadsr.common.bulkdownload;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import gov.nih.nci.ncicb.cadsr.common.resource.Form;
import gov.nih.nci.ncicb.cadsr.common.resource.FormV2;
import gov.nih.nci.ncicb.cadsr.common.util.logging.Log;
import gov.nih.nci.ncicb.cadsr.common.util.logging.LogFactory;
import gov.nih.nci.ncicb.cadsr.formbuilder.ejb.impl.FormBuilderServiceImpl;
import gov.nih.nci.ncicb.cadsr.formbuilder.ejb.service.FormBuilderService;
import gov.nih.nci.ncicb.cadsr.common.resource.Context;

public class FormBulkDownloadXML {

	private static final Log logger = LogFactory.getLog(FormBulkDownloadXML.class.getName());
	private static final String convertedFormBegin = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<FormList>\n";
	private static final String convertedFormEnd = "\n</FormList>";	
	private static String dir;
	private static String separator;
	private static FormBuilderService service;
	private static String contextNameParam = "";//use only this context given by name; not null
	private static String contextInPath = "";//as "PhenX/" or just ""
	
	private static OutputStream startXMLFile(String formFileNameAppend) throws Exception {
		String xmlFilename = "dwld" + separator + contextInPath + dir + separator + "FormsDownload-" + formFileNameAppend + ".xml";
		logger.info("xmlFilename: " + xmlFilename);
		BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(xmlFilename));
		fileOut.write(convertedFormBegin.getBytes("UTF-8"));
		fileOut.flush();
		return fileOut;
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
	
	private static void init() throws Exception {
		try {//check we have a JDBC Driver class
			Class.forName("oracle.jdbc.driver.OracleDriver");
			logger.info("...oracle.jdbc.driver.OracleDriver loaded");
		} catch (ClassNotFoundException e) {
			logger.error("...Failed to load oracle.jdbc.driver.OracleDriver");
			e.printStackTrace();
			System.exit(-1);
		}

		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext cpCtx = new ClassPathXmlApplicationContext(
				new String[] { "application-context.xml" });
		Object objDs = cpCtx.getBean("dataSource");
		logger.debug(".......dataSource loaded: " + (objDs != null));

		Object daoFactory = cpCtx.getBean("daoFactory");
		logger.debug(".......daoFactory loaded: " + (daoFactory != null) + " " + daoFactory.getClass().getName());

		// FormBuilderServiceImpl
		Object formBuilderServiceImpl = cpCtx.getBean("formBuilderServiceImpl");
		logger.debug(".......formBuilderServiceImpl loaded: " + (formBuilderServiceImpl != null));
		service = (FormBuilderServiceImpl) (formBuilderServiceImpl);
		
		String dirSuffix = new SimpleDateFormat("yyyy-MMM-dd-HH-mm").format(new Date());
		
		Path path = Paths.get("dwld" + separator + contextInPath + dirSuffix);
		
		if (!Files.exists(path)) {
			Files.createDirectories(path);
			logger.info("creating the folder " + "dwld" + separator + contextInPath + dirSuffix);
		}
		
		dir = path.getFileName().toString();
		logger.info("xml file directory: " + dir);
	}
	
	private static int parseParamFormsPerFile(String[] args) throws Exception {
		if(args.length < 1) {
			  logger.error("Please provide the number of forms per file.");
		      throw new Exception("Please provide a number of forms per file.");
		}
		else {
			try {
				int formsPerFile = Integer.parseInt(args[0]);
				logger.info("Forms Per file: " +formsPerFile);
				if (formsPerFile <= 0) {
					throw new Exception("Please provide a valid number of forms per file: " + args[0]);
				}
				return formsPerFile;
			} catch (NumberFormatException ex) {
				logger.error("Please provide a valid number of forms per file: " + args[0]);
				ex.printStackTrace();
				throw ex;
		    }
		}
	}
	private static void parseParamFormContext(String[] args) throws Exception {
		if(args.length >= 2) {
			contextNameParam = args[1];
		}//otherwise ""

		if (contextNameParam.length() > 0) {
			contextInPath = contextNameParam + separator;
		}//otherwise ""
		
		logger.debug("contextInPath: " + contextInPath);
	}
	
	private static int parseParamFormAmount(String[] args) throws Exception {
		int formAmount = 0;
		if(args.length >= 3) {
			try {
				formAmount = Integer.parseInt(args[2]);
				logger.info("Forms amount: " + formAmount);
				
			} catch (NumberFormatException ex) {
				logger.error("Please provide the number of forms to be downloaded per file.");
				ex.printStackTrace();
				throw ex;
		    }
		}
		return formAmount;
	}
	private static List<String> collectFormIdseq (Collection forms, int formAmount) {
		@SuppressWarnings("rawtypes")
		Iterator iter = forms.iterator();
		int idx = formAmount;
		Form form;
		List<String> idseqList = new ArrayList<>(formAmount);
		while((iter.hasNext()) && (idx > 0)) {
			form = (Form)iter.next();
			idseqList.add(form.getFormIdseq());
			idx--;
		}
		return idseqList;
	}
	private static int calcNumberOfGroups(final int lengthOfCollection, final int maxRecords) {
		return (lengthOfCollection / maxRecords) + 
			(((lengthOfCollection % maxRecords) == 0)? 0 : 1);
	}
	
	private static String findContextIdSeq() throws Exception {
		Collection contextsObj = service.getAllContexts();
		for (Object obj : contextsObj) {
			Context currContext = (Context)obj;
			if (contextNameParam.equals(currContext.getName())) {
				return currContext.getConteIdseq();
			}				
		}
		if (contextNameParam.length() > 0) {
			logger.error("Context not found: " + contextNameParam);
			throw new Exception("Please provide a valid Context name: " + contextNameParam);
		}
		else return contextNameParam;
	}
	
	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		logger.info("Starting XML Forms download...");
		separator = System.getProperty("file.separator");
		
		//parse parameters
		int formsPerFile = parseParamFormsPerFile(args); //how many forms to load in one file which shall be more than 0
		parseParamFormContext(args);
		int formAmount = parseParamFormAmount(args); //how many forms to load
		
		init();

		FormV2 crf = null;

		String conteIdseq = findContextIdSeq();

		Collection forms;
		if (conteIdseq.length() > 0) {
			logger.info("Forms from Context with conteIdseq: " + conteIdseq);
			forms = service.getAllForms(null, null, conteIdseq, "RELEASED", null, null, null, null, "latestVersion", null,
					null, null, null);
		}
		else {
			logger.info("Forms from all Contexts");
			forms = service.getAllForms(null, null, null, "RELEASED", null, null, null, null, "latestVersion", null,
				null, null, "'TEST', 'Training'");
		}
		logger.info("Forms amount found in DB: " + forms.size());
		if (formAmount == 0) {//not provided
			formAmount = forms.size();
		}
		formAmount = (forms.size() < formAmount) ? forms.size() : formAmount;
		if (formAmount <= 0) {//no form found
			logger.info("No forms amount to download, formAmount: " + formAmount);
			return;
		}
		
		logger.info("Forms amount to download: " + formAmount);
		
		int numGroups = calcNumberOfGroups(formAmount, formsPerFile);
		logger.info("Files amount: " + numGroups);
		
		List<String> idseqList = collectFormIdseq(forms, formAmount);

		int currIdseqListIdx = 0;//global IDSEQ number in the array

		for (int groupId = 1; groupId <= numGroups; groupId++) {
			String formFileNameAppend = ""+ groupId;
			logger.info("the next file: " + formFileNameAppend);
			//start a new file
			OutputStream currFileOut = startXMLFile(formFileNameAppend);
			int numValue = 0; 
			for (int idxInGroup = 0; ((idxInGroup < formsPerFile) && (currIdseqListIdx < formAmount)); idxInGroup++,  currIdseqListIdx++) {
				String formIdSeq = idseqList.get(currIdseqListIdx);
				logger.debug("Form ID seq: " + formIdSeq);

				if (!FormBuilderUtil.validateIdSeqRequestParameter(formIdSeq)) {
					logger.error("!!! Invalid form IDSEQ skipped: " + formIdSeq);
					continue;//go to the next form
				}
				String currentForm;
				try {
					crf = service.getFormDetailsV2(formIdSeq);
					currentForm = FormConverterUtil.instance().convertFormToV2(crf);
				
				} catch (Exception exp) {
					logger.error("Exception getting CRF: " + exp);
					logger.error("!!! Form IDSEQ skipped: " + formIdSeq);
					exp.printStackTrace();
					continue;//go to the next form
				}
				
				numValue++;//form number in the group starts from 1
				
				currentForm = currentForm.replaceAll("\\<\\?xml(.+?)\\?\\>", "").trim();
				currentForm = currentForm.replace("<form>", "<form num=\"" + numValue + "\">");
				if (numValue > 1) {
					currFileOut.write('\n');
				}
				currFileOut.write(currentForm.toString().getBytes("UTF-8"));//download the current form
				currFileOut.flush();
			}//one form processing loop
			
			closeXMLFile(currFileOut);
		}
		long finished = System.currentTimeMillis();
		logger.info(".....Download time in minutes: " + (((finished - start)/1000)/60));
	}
}
