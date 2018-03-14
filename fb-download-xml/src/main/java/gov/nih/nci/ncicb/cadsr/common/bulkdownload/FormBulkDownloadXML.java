package gov.nih.nci.ncicb.cadsr.common.bulkdownload;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import gov.nih.nci.ncicb.cadsr.common.exception.FatalException;
import gov.nih.nci.ncicb.cadsr.common.resource.Form;
import gov.nih.nci.ncicb.cadsr.common.resource.FormV2;
import gov.nih.nci.ncicb.cadsr.common.util.logging.Log;
import gov.nih.nci.ncicb.cadsr.common.util.logging.LogFactory;
import gov.nih.nci.ncicb.cadsr.formbuilder.ejb.impl.FormBuilderServiceImpl;
import gov.nih.nci.ncicb.cadsr.formbuilder.ejb.service.FormBuilderService;

public class FormBulkDownloadXML {

	private static final Log logger = LogFactory.getLog(FormBulkDownloadXML.class.getName());
	private static Integer FORMS_PER_FILE = 4;
	
	private static void writeXMLFile(String convertedForm, String formFileNameAppend) {
		try {
			convertedForm = convertedForm.replaceAll("\\<\\?xml(.+?)\\?\\>", "").trim();
			convertedForm = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<FormList>\n"+convertedForm+"\n</FormList>";
			byte[] xmlBytes = convertedForm.getBytes();
			// FIXME use platform path separator, and generally clean up
			Path path = Paths.get("dwld");
			if (!Files.exists(path)) {
	            try {
	                Files.createDirectories(path);
	            	logger.info("Directory dwld not present, so creating the folder....");			                
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
			logger.info("Writing XML file... ");				
			String dir = path.getFileName().toString();
			logger.debug("xml file directory: " + dir);
			String xmlFilename = dir + "/FormsDownload-"+formFileNameAppend;
			xmlFilename = xmlFilename + ".xml";
			logger.info("xmlFilename: " + xmlFilename);
			FileOutputStream fileOut = new FileOutputStream(xmlFilename);
			fileOut.write(xmlBytes);
			fileOut.flush();
			fileOut.close();				
		} catch (Exception exp) {
			logger.info("Exception converting CRF 2: " + exp);
			exp.printStackTrace();
		}		
		
	}
	
	public static void main(String[] args) throws Exception {
		logger.info("Starting download...");
		try {
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
		FormBuilderService service = (FormBuilderServiceImpl) (formBuilderServiceImpl);

		FormV2 crf = null;
		Form form = null;
		Collection forms = null;
		Object[] formArr = null;
		String formIdSeq;		
		String convertedForm = "";
		int formsCount = 0;
		int formsPerFile = FORMS_PER_FILE;		
		
		if(args.length != 1) {
			  logger.error("Please provide the number of forms to be downloaded per file.");
		      System.exit(1);
		    }
	    try {
	    	formsPerFile = Integer.parseInt(args[0]);
			logger.info("Forms Per file: " +formsPerFile);
	    } catch (NumberFormatException ex) {
	    	logger.error("Please provide the number of forms to be downloaded per file.");
	      System.exit(1);
	    }
		
		try {
			forms = service.getAllForms(null, null, null, "RELEASED", null, null, null, null, "latestVersion", null,
					null, null, "'TEST', 'Training'");
			logger.info("Forms size: " + forms.size());
			if (forms.size() > 0) {
				formArr = forms.toArray();
			}
			String formFileNameAppend = "";
			for (int i = 0; i < formArr.length; i++) {
				form = (Form) formArr[i];
				formIdSeq = form.getFormIdseq();

				logger.info("Form ID seq: " + formIdSeq);
				try {
					if (!FormBuilderUtil.validateIdSeqRequestParameter(formIdSeq))
						throw new FatalException("Invalid form download parameters.",
								new Exception("Invalid form download parameters."));

					crf = service.getFormDetailsV2(formIdSeq);
					formsCount++;
				} catch (Exception exp) {
					logger.info("Exception getting CRF: " + exp);					
					exp.printStackTrace();
				}

				try {
					convertedForm = convertedForm + FormConverterUtil.instance().convertFormToV2(crf);
					if (formsCount==1) {
						formFileNameAppend = ""+form.getPublicId();
						logger.info("Beginning Form ID "+formFileNameAppend + ":: Forms count: "+formsCount);
					}
					if (formsCount==formArr.length - 1 || formsCount == formsPerFile) {
						formFileNameAppend = formFileNameAppend+"-"+form.getPublicId();
						logger.info("Combined Form ID "+formFileNameAppend + ":: Forms count: "+formsCount);
					}					
					if (formsCount == formsPerFile || formsCount == formArr.length) {
						writeXMLFile(convertedForm, formFileNameAppend);
						convertedForm = "";
						formFileNameAppend = "";
						formsCount = 0;
					}
				} catch (Exception exp) {
					logger.info("Exception converting CRF 2: " + exp);
					exp.printStackTrace();
				}						
			}			

		} catch (Exception e) {
			logger.info("Exception getting All the forms: " + e);
			e.printStackTrace();
			return;
		}

	}
}
