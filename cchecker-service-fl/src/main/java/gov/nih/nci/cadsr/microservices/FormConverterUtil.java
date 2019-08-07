/*
 * Copyright (C) 2019 FNLCR - All rights reserved.
 */
package gov.nih.nci.cadsr.microservices;

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
import org.exolab.castor.xml.Marshaller;
/**
 * Builds FL XML String for one FL Form.
 * 
 * @author asafievan
 *
 */
public class FormConverterUtil {
	private static Log log = LogFactory.getLog(FormConverterUtil.class.getName());
	
	static private FormConverterUtil _instance = null;
	//FinalFormCartTransformv33 used in FL
	//public static final String V1ExtendedToV2XSL = "/transforms/FinalFormCartTransformv33.xsl";
	//Remove public ID form element
	public static final String generateFormsXSL = "/transforms/GenerateFormsXml.xsl";
	//Stop using ConvertFormCartV1ExtendedToV2.xsl to be in syn with what GS has
	//public static final String V1ExtendedToV2XSL = "/transforms/ConvertFormCartV1ExtendedToV2.xsl";
	
	public static final String stripEmptyNodesXSL = "/transforms/remove-empty-nodes.xsl";
	protected Transformer transformerV1ToV2 = null;
	protected Transformer transformerStripEmpty = null;

	private String convertToV2(FormV2 crf) throws Exception
		{
			// Start with our standard conversion to xml (in V1 format)
			StringWriter writer = new StringWriter();
			try {
				Marshaller.marshal(crf, writer);
			} catch (Exception ex) {
				log.debug("FormV2 " + crf);
				throw ex;
			}
			
			try {
				 
				String content = writer.toString();
				//FIXME create a file with a session related name in cchecker directory
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
	/**
	 * 
	 * @param crf not null
	 * @return String FL XML String
	 * @throws Exception
	 */
	public String convertFormToV2 (FormV2 crf) throws Exception 
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
			InputStream xslStream = this.getClass().getResourceAsStream(generateFormsXSL);
			xslSource = new StreamSource(xslStream);
			InputStream xslStreamRemoveEmptyNodes = this.getClass().getResourceAsStream(stripEmptyNodesXSL); 
			xslSourceStripEmpty = new StreamSource(xslStreamRemoveEmptyNodes);
		}
		catch(Exception e) {
			log.error("FormConverterUtil error loading conversion xsl: " + generateFormsXSL + " OR " + stripEmptyNodesXSL + " exc: "+ e);
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