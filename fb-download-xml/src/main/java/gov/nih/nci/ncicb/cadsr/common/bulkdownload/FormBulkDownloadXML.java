package gov.nih.nci.ncicb.cadsr.common.bulkdownload;

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

//import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import gov.nih.nci.ncicb.cadsr.common.exception.FatalException;
import gov.nih.nci.ncicb.cadsr.common.resource.FormV2;
import gov.nih.nci.ncicb.cadsr.common.util.logging.Log;
import gov.nih.nci.ncicb.cadsr.common.util.logging.LogFactory;
import gov.nih.nci.ncicb.cadsr.formbuilder.ejb.impl.FormBuilderServiceImpl;
import gov.nih.nci.ncicb.cadsr.formbuilder.ejb.service.FormBuilderService;

/*import gov.nih.nci.ncicb.cadsr.loader.UserSelections;
import gov.nih.nci.ncicb.cadsr.loader.util.BeansAccessor;
import gov.nih.nci.ncicb.cadsr.loader.util.PropertyAccessor;*/
//@ComponentScan(basePackages = "gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc")

public class FormBulkDownloadXML {
	
	private static final Log logger = LogFactory.getLog(FormBulkDownloadXML.class.getName());
		
	public static void main(String[] args) throws Exception {
		//FIXME download XML RELEASED forms not just one form
		logger.info("Starting download...");
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			logger.info("...oracle.jdbc.driver.OracleDriver loaded");
		} catch(ClassNotFoundException e) {
				logger.error("...Failed to load oracle.jdbc.driver.OracleDriver");
			  e.printStackTrace();
			  System.exit(-1);
		}
		
		//FIXME this is for feasibility only working with one Form
		String formIdSeq;
		if (args.length > 0) {
			formIdSeq = args[0];
		}
		else {
			formIdSeq = "D71EB130-996E-9982-E040-BB89AD435BA6";
		}
		logger.info("Form ID seq: "+ formIdSeq);
		
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext cpCtx = new ClassPathXmlApplicationContext(new String[]{"application-context.xml"});
		Object objDs = cpCtx.getBean("dataSource");
		logger.info(".......dataSource loaded: "+ (objDs != null));

		Object daoFactory = cpCtx.getBean("daoFactory");
		logger.info(".......daoFactory loaded: "+ (daoFactory != null) + " " + daoFactory.getClass().getName());

		//FormBuilderServiceImpl
		Object formBuilderServiceImpl = cpCtx.getBean("formBuilderServiceImpl");
		logger.info(".......formBuilderServiceImpl loaded: " + (formBuilderServiceImpl != null));
		FormBuilderService service = (FormBuilderServiceImpl)(formBuilderServiceImpl);

		FormV2 crf = null;
		try {
			if (!FormBuilderUtil.validateIdSeqRequestParameter(formIdSeq))
				throw new FatalException("Invalid form download parameters.", new Exception("Invalid form download parameters."));
			
			crf = service.getFormDetailsV2(formIdSeq);
		} catch (Exception exp) {
			logger.info("Exception getting CRF 1: "+ exp);
			exp.printStackTrace();
		}

		try {
			String convertedForm = FormConverterUtil.instance().convertFormToV2(crf);  
			//FIXME use platform path separator, and generally clean up
			Path path = Paths.get("dwld");
			String dir = path.getFileName().toString();
			logger.info("xml file directory: "+ dir);
			String xmlFilename = dir +"/Form"  + crf.getPublicId() + "_v" + crf.getVersion();

			//xmlFilename = xmlFilename.replace('/', '_').replace('.', '_');
			xmlFilename = xmlFilename + ".xml";
			logger.info("xmlFilename: "+ xmlFilename);
			FileOutputStream fileOut = new FileOutputStream(xmlFilename);
			byte[] xmlBytes = convertedForm.getBytes();
			fileOut.write(xmlBytes);
			fileOut.flush();
			fileOut.close();
		} catch (Exception exp) {
			logger.info("Exception converting CRF 2: "+ exp);
			exp.printStackTrace();
		}
	}
}
