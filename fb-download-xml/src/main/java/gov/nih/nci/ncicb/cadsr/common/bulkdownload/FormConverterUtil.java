// This class provides utility functions for supporting the new form cart format

package gov.nih.nci.ncicb.cadsr.common.bulkdownload;

import gov.nih.nci.ncicb.cadsr.common.resource.FormV2;
import gov.nih.nci.ncicb.cadsr.common.util.logging.Log;
import gov.nih.nci.ncicb.cadsr.common.util.logging.LogFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;


public class FormConverterUtil {
	private static Log log = LogFactory.getLog(FormConverterUtil.class.getName());
	
	static private FormConverterUtil _instance = null;
	//Stop using ConvertFormCartV1ExtendedToV2.xsl to be in syn with what GS has
	public static final String V1ExtendedToV2XSL = "/transforms/FinalFormCartTransformv33.xsl";
	//public static final String V1ExtendedToV2XSL = "/transforms/ConvertFormCartV1ExtendedToV2.xsl";
	
	public static final String stripEmptyNodesXSL = "/transforms/remove-empty-nodes.xsl";
	protected Transformer transformerV1ToV2 = null;
	protected Transformer transformerStripEmpty = null;

	private String convertToV2(FormV2 crf) throws MarshalException, ValidationException, TransformerException
		{
			// Start with our standard conversion to xml (in V1 format)
			StringWriter writer = new StringWriter();
			try {
				Marshaller.marshal(crf, writer);
			} catch (MarshalException ex) {
				log.debug("FormV2 " + crf);
				throw ex;
			} catch (ValidationException ex) {
				// need exception handling	
				log.debug("FormV2 " + crf);
				throw ex;
			}
			
			try {
				 
				String content = writer.toString();
	 
				File file = new File("download1.xml");
	 
				// if file doesnt exists, then create it
				if (!file.exists()) {
					file.createNewFile();
				}
	 
				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(content);
				bw.close();
	 
				log.debug("Done");
	 
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// Now use our transformer to create V2 format
			Source xmlInput = new StreamSource(new StringReader(writer.toString()));
			ByteArrayOutputStream xmlOutputStream = new ByteArrayOutputStream();  
			Result xmlOutput = new StreamResult(xmlOutputStream);
			try {
				transformerV1ToV2.transform(xmlInput, xmlOutput);
			} catch (TransformerException e) {
				log.debug(writer.toString());
				throw e;
			}	
			
			String V2XML = xmlOutputStream.toString();
			return V2XML;
		}
	
	public String convertFormToV2 (FormV2 crf) throws MarshalException, ValidationException, TransformerException 
		{
			Source xmlInputV2Forms = new StreamSource(new StringReader(convertToV2(crf)));
			ByteArrayOutputStream xmlOutputStreamStripEmpty = new ByteArrayOutputStream();  
			Result xmlOutputStripEmpty = new StreamResult(xmlOutputStreamStripEmpty);
			
			try {
				// Strip empty nodes from the transformed v2 form xml file
				transformerStripEmpty.transform(xmlInputV2Forms, xmlOutputStripEmpty);
			} catch (TransformerException e) {
				log.debug(xmlInputV2Forms.toString());
				throw e;
			}	
					
			String V2XML = xmlOutputStreamStripEmpty.toString();
			return V2XML;
		}
		
	protected FormConverterUtil() {
		
		StreamSource xslSource = null;
		StreamSource xslSourceStripEmpty = null;
		try {
			InputStream xslStream = this.getClass().getResourceAsStream(V1ExtendedToV2XSL);
			xslSource = new StreamSource(xslStream);
			InputStream xslStreamRemoveEmptyNodes = this.getClass().getResourceAsStream(stripEmptyNodesXSL); 
			xslSourceStripEmpty = new StreamSource(xslStreamRemoveEmptyNodes);
		}
		catch(Exception e) {
			log.error("FormConverterUtil error loading conversion xsl: " + V1ExtendedToV2XSL + " OR " + stripEmptyNodesXSL + " exc: "+ e);
		}
		
		try {
			log.debug("creating transformerV1ToV2");			
			transformerV1ToV2 = net.sf.saxon.TransformerFactoryImpl.newInstance().newTransformer(xslSource);
			log.debug("creating transformerStripEmpty");
			transformerStripEmpty  = net.sf.saxon.TransformerFactoryImpl.newInstance().newTransformer(xslSourceStripEmpty);
		} catch (TransformerException e) {
			log.debug("transformerV1ToV2 exception: " + e.toString());
			log.debug("transformerV1ToV2 exception: " + e.getMessage());
		}	
	} 
	 
	 
	static public FormConverterUtil instance(){
		if (_instance == null) {
			_instance = new FormConverterUtil();
		}
		return _instance;
	}
  
}