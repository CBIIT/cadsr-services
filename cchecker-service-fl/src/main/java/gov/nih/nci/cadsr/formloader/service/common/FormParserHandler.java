package gov.nih.nci.cadsr.formloader.service.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.cadsr.formloader.domain.ModuleDescriptor;

import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;

/**
 * @author yangs8
 *
 */
public class FormParserHandler extends ParserHandler {
	private static Logger logger = Logger.getLogger(FormParserHandler.class.getName());
	
	FormDescriptor tempForm;
	int moduleCountForForm = 0;	
	String methodName;
	ArrayDeque<String> nodeQueue;
	int formCount = 0;
	
	boolean formProtocol = false;
	
	FormCollection formCollection; 

	public FormCollection getFormCollection() {
		return formCollection;
	}

	public void setFormCollection(FormCollection formCollection) {
		this.formCollection = formCollection;
	}


	public FormParserHandler(FormCollection aCollection) {
		super(new ArrayList<FormDescriptor>());
		
		formCollection = aCollection;
		formCollection.setForms(this.formList);
		formCount = 0;
		moduleCountForForm = 0;
		methodName = null;
		nodeQueue = new ArrayDeque<String>();
	}
	

	@Override
	public void handleStartElement(XMLStreamReader xmlreader) {
		if (xmlreader == null) {
			logger.debug("xmlreader is null!!!! Do nothing");
			return;
		}
			
		if (!xmlreader.hasName()) {
			logger.debug("Got an elem without name. Do nothing");
			return;
		}
		
		String localName = xmlreader.getLocalName();
		
		if (localName.equals(StaXParser.COLLECTION_NAME)) {
			this.methodName = StaXParser.COLLECTION_NAME; //quick fix. 
		} else if (localName.equals(StaXParser.COLLECTION_DESCRIPTION)) {
			this.methodName = StaXParser.COLLECTION_DESCRIPTION; //quick fix
		} else if (localName.equals(StaXParser.FORM)) {
			
			int lineNum = xmlreader.getLocation().getLineNumber();
			logger.debug("Got a form at line: " + lineNum);
			tempForm = new FormDescriptor();
			tempForm.setXml_line_begin(lineNum);
			this.methodName = null;
		} else if (localName.equals(StaXParser.CONTEXT) || 
				localName.equals(StaXParser.VERSION)  ||
				localName.equals(StaXParser.TYPE) ||
				localName.equals(StaXParser.WORKFLOW_STATUS) ||
				localName.equalsIgnoreCase(StaXParser.PUBLIC_ID) ||
				localName.equals(StaXParser.CHANGE_NOTE) || 
				localName.equals(StaXParser.CREATED_BY) || 
				localName.equals(StaXParser.MODIFIED_BY) ||
				localName.equals(StaXParser.PREFERRED_DEFINITION)  ||
				localName.equals(StaXParser.REGISTRATION_STATUS) ||
				localName.equals(StaXParser.CATEGORY_NAME)) {
			if (nodeQueue.peek().equals(StaXParser.FORM)) {
				this.methodName = getMethodName(localName);	
			}
		
		} else if (localName.equals(StaXParser.LONG_NAME)) {
			if (nodeQueue.peek().equals(StaXParser.PROTOCOL) && formProtocol == true) {
				this.methodName = getMethodName(StaXParser.PROTOCOL) + "Name"; 
			} else if (nodeQueue.peek().equals(StaXParser.FORM)) {
				this.methodName = getMethodName(localName);	
			}
		} else if (localName.equals(StaXParser.MODULE)) {
			tempForm.getModules().add(new ModuleDescriptor());
		} else if (localName.equalsIgnoreCase(StaXParser.TEXT)) {
			if (nodeQueue.peek().equals(StaXParser.HEADER_INSTRUCTION)) {
				this.methodName = getMethodName(StaXParser.HEADER_INSTRUCTION);
			} else if (nodeQueue.peek().equals(StaXParser.FOOTER_INSTRUCTION)) {
				this.methodName = getMethodName(StaXParser.FOOTER_INSTRUCTION);
			}
		} else if (localName.equals(StaXParser.PROTOCOL)) {
			if (nodeQueue.peek().equals(StaXParser.FORM))
				formProtocol = true;
		}
		
		//logger.debug("Pushing to node queue: " + localName);
		this.nodeQueue.push(localName);
		
	}

	@Override
	public void handleEndElement(XMLStreamReader xmlreader) {
		if (xmlreader == null) {
			logger.debug("xmlreader is null!!!! Do nothing");
			return;
		}
			
		if (!xmlreader.hasName()) {
			logger.debug("Got an elem without name. Do nothing");
			return;
		}
		
		String localName = xmlreader.getLocalName();
		
		if (localName.equals(StaXParser.FORM)) {
			int lineNum = xmlreader.getLocation().getLineNumber();
			logger.debug("End parsing a form at line: " + lineNum);
			 tempForm.setXml_line_end(lineNum);
			 tempForm.setIndex(formCount);
			 formCount++;
			 logger.debug(tempForm.toString());
			 
			 this.formList.add(tempForm);
			 
			 logger.debug("peak: " + (String)nodeQueue.peek());
			 logger.debug("peak first: " + (String)nodeQueue.peekFirst());
			 logger.debug("peak last: " + (String)nodeQueue.peekLast());
			 
		} else if (localName.equals(StaXParser.FORMS)) {
			this.formCollection.setForms(this.formList);
		} else if (localName.equals(StaXParser.PROTOCOL)) {
			formProtocol = false;
		}
		
		this.methodName = null;
		if (localName.equals(StaXParser.FORM))
			nodeQueue.clear();
		else {
			if (!nodeQueue.isEmpty())
				nodeQueue.pop();
		}
		
	}

	@Override
	public void handleCharacterElement(XMLStreamReader xmlreader) {
		if (xmlreader == null) {
			logger.debug("xmlreader is null!!!! Do nothing");
			return;
		}
		
		if (methodName == null)
			return;
		
		if (methodName.equals(StaXParser.COLLECTION_NAME)) {
			this.formCollection.setName(xmlreader.getText());
		} else if (methodName.equals(StaXParser.COLLECTION_DESCRIPTION)) {
			this.formCollection.setDescription(xmlreader.getText());
		} else {
			String currNode = (nodeQueue.peek() != null) ? nodeQueue.peek().toLowerCase() : null;

			if (currNode != null) {
				logger.debug("Queue head: " + nodeQueue.peek() + " | method name: " + methodName + " | value to set: " + xmlreader.getText());
				setFormProperty(methodName, xmlreader.getText());
			}
		}
		
	}
	
protected void setFormProperty(String methodName, String value) {	
		
		try {
			Class[] paramString = new Class[1];	
			paramString[0] = String.class;
			Class currClass = tempForm.getClass();
			Method method = currClass.getMethod(methodName, paramString);
			method.invoke(tempForm, value);
			
		} catch (SecurityException se) {
			logger.debug(se);
		} catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
            return;
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            return;
        } catch(Exception ex) {
			logger.debug(ex);
    	} catch (Throwable t) {
    		logger.debug(t);
    	}

	}
	
	/**
	 * Use this version is the method is in super class
	 * @param methodName
	 * @param value
	 */
	protected void setFormPropertyV2(String methodName, String value) {	
		
		try {
			Class[] paramString = new Class[1];	
			paramString[0] = String.class;
			Class currClass = tempForm.getClass();
			Method method = null;
			
			while (currClass != Object.class) {
			     try {
			          method = currClass.getDeclaredMethod(methodName, paramString);
			          break;
			     } catch (NoSuchMethodException ex) {
			    	 currClass = currClass.getSuperclass();
			     }
			}
			
			if (methodName.contains("Public")) {
				int i = 0;
				i++;
			}
			//Method method = tempForm.getClass().getSuperclass().getDeclaredMethod(methodName, paramString);
			method.invoke(tempForm, value);
			
		} catch (SecurityException se) {
			logger.debug(se);
		} catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
            return;
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            return;
        } catch(Exception ex) {
			logger.debug(ex);
    	} catch (Throwable t) {
    		logger.debug(t);
    	}

	}
	
}
