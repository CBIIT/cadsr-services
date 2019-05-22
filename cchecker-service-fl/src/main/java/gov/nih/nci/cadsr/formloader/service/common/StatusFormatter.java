package gov.nih.nci.cadsr.formloader.service.common;

import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.cadsr.formloader.domain.FormCollectionStatus;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.cadsr.formloader.domain.FormStatus;
import gov.nih.nci.cadsr.formloader.domain.ModuleStatus;
import gov.nih.nci.cadsr.formloader.domain.QuestionStatus;
import gov.nih.nci.cadsr.formloader.service.impl.LoadingServiceImpl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

public class StatusFormatter {
	private static Logger logger = Logger.getLogger(StatusFormatter.class.getName());
	
	/**
	 * Returns empty string if no messages found at form or question level
	 * 
	 * @param form
	 * @return
	 */
	public static String getStatusMessagesInXml(FormDescriptor form) {
		FormStatus formStatus = form.getStructuredStatus();
		
		if (!messagesExistInFormStatus(formStatus))
			return "";
		
		return StatusFormatter.transformStatus(formStatus);
	}
	
	public static String transformStatus(FormStatus formStatus) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(FormStatus.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			final StringWriter stringWriter = new StringWriter();
			jaxbMarshaller.marshal(formStatus, stringWriter);
			
			return stringWriter.toString();
			
		} catch (JAXBException e) {
			logger.error(e.getMessage());
		} 
		
		return "Form status xml generation failed";
	}
	
	/**
	 * Messages could exit at form or question level. Return true if any message exist
	 * @param formStatus
	 * @return
	 */
	public static boolean messagesExistInFormStatus(FormStatus formStatus) {
		if (formStatus == null)
			return false;
	
		if (formStatus.getMessages() != null && formStatus.getMessages().size() > 0)
			return true;
		
		List<ModuleStatus> moduleStatuses = formStatus.getModuleStatuses();
		
		if (moduleStatuses == null)
			return false;
		
		for (ModuleStatus mStatus : moduleStatuses) {
			List<QuestionStatus> qStatuses = mStatus.getQuestionStatuses();
			if (qStatuses == null)
				continue;
			
			for (QuestionStatus qStatus : qStatuses) {
				if (qStatus.getMessages() != null && qStatus.getMessages().size() > 0)
					return true;
			}
		}
		
		return false;
	}
	
	public static String getStatusInXml(FormDescriptor form) {
		FormStatus formStatus = form.getStructuredStatus();
		return StatusFormatter.transformStatus(formStatus);
	}
	
	public static String getStatusInXml(FormCollection coll) {
		FormCollectionStatus collStatus= coll.getStructuredStatus();
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(FormCollectionStatus.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			final StringWriter stringWriter = new StringWriter();
			
			jaxbMarshaller.marshal(collStatus, stringWriter);
			return stringWriter.toString();
			
		} catch (JAXBException e) {
			logger.error(e.getMessage());
		}
		
		return "Collection status xml generation failed";
	}
	
	public static void writeStatusToXml(String content, String fileNamePath) {
		
		if (content == null || content.length() == 0) {
			logger.debug("Content to write is null or empty. Nothing to write to file");
			return;
		}
		
		try {
			 
			File file = new File(fileNamePath);
 
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
 
			logger.debug("Done writing status to xml file");
 
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

}
