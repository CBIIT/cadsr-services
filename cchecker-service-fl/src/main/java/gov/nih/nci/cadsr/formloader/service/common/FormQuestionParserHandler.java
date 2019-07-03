package gov.nih.nci.cadsr.formloader.service.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.List;

import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.cadsr.formloader.domain.ModuleDescriptor;
import gov.nih.nci.cadsr.formloader.domain.QuestionDescriptor;

import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormQuestionParserHandler extends ParserHandler {
	private static final Logger logger = LoggerFactory
			.getLogger(FormQuestionParserHandler.class.getName());

	ArrayDeque<String> nodeQueue;

	FormDescriptor currForm;
	ModuleDescriptor currModule;
	QuestionDescriptor currQuestion;
	QuestionDescriptor.ValidValue currValidValue;
	
	Object objToSetProperty;

	int form_idx = -1;
	int module_idx = -1;
	int question_idx = -1;

	String methodName;

	public FormQuestionParserHandler(List<FormDescriptor> forms) {
		super(forms);
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

		if (localName.equals(StaXParser.FORM)) {
			if (++form_idx > this.formList.size() - 1) {
				logger.error("Parsing form idx is out of sync with form list to work on!!");
				return;
			}
			currForm = this.formList.get(form_idx);
			this.methodName = null;

		} else if (localName.equals(StaXParser.MODULE)) {
			module_idx++;
			if (module_idx > this.currForm.getModules().size() - 1) {
				logger.error("Parsing module idx is out of sync with module list of the form to work on!!");
				return;
			}
			currModule = currForm.getModules().get(module_idx);
		} else if (localName.equals(StaXParser.QUESTION)) {
			question_idx++;
			currQuestion = new QuestionDescriptor();
			currModule.getQuestions().add(currQuestion);
			
		} else if (localName.equalsIgnoreCase(StaXParser.PUBLIC_ID)
				|| localName.equalsIgnoreCase(StaXParser.VERSION)
				|| localName.equalsIgnoreCase(StaXParser.MODIFIED_BY)
				|| localName.equals(StaXParser.MODULE_MAX_REPEAT)) {
			if (nodeQueue.peek().equals(StaXParser.MODULE)) {
				this.objToSetProperty = currModule;
				this.methodName = getMethodName(localName);
			} else if (nodeQueue.peek().equals(StaXParser.QUESTION)) {
				this.objToSetProperty = currQuestion;
				this.methodName = getMethodName(localName);
			} else if (nodeQueue.peek().equals(StaXParser.DATA_ELEMENT)) {
				this.objToSetProperty = currQuestion;
				//There could be dataElement under dataElement so this is a dirty way to handle that
				if (localName.equalsIgnoreCase(StaXParser.PUBLIC_ID) && currQuestion.getCdePublicId() == null 
						||localName.equalsIgnoreCase(StaXParser.VERSION) && currQuestion.getCdeVersion() == null )
					this.methodName = getMethodName(StaXParser.PREFIX_CDE + localName);
			} else if (nodeQueue.peek().equals(StaXParser.VALUE_DOMAIN)) {
				this.objToSetProperty = currQuestion;
				if (localName.equalsIgnoreCase(StaXParser.PUBLIC_ID) && currQuestion.getCdeVdPublicId() == null 
						||localName.equalsIgnoreCase(StaXParser.VERSION) && currQuestion.getCdeVdVersion() == null )
					this.methodName = getMethodName(StaXParser.PREFIX_CDE_VD + localName);
			}

		} else if  (localName.equalsIgnoreCase(StaXParser.QUESTION_TEXT)
				|| localName.equalsIgnoreCase(StaXParser.DEFAULT_VALUE)
				|| localName.equalsIgnoreCase(StaXParser.IS_EDITABLE) 
				|| localName.equalsIgnoreCase(StaXParser.IS_MANDATORY)){
			if (nodeQueue.peek().equals(StaXParser.QUESTION)) {
				this.objToSetProperty = currQuestion;
				this.methodName = getMethodName(localName);
			}
		} else if  (localName.equalsIgnoreCase(StaXParser.VALID_VALUE)) {
			if (nodeQueue.peek().equals(StaXParser.QUESTION)) {
				QuestionDescriptor.ValidValue vValue = currQuestion.new ValidValue();
				currQuestion.getValidValues().add(vValue);
				this.currValidValue = vValue;
				this.objToSetProperty = vValue;
			}
		} else if (localName.equalsIgnoreCase(StaXParser.VALUE)
				|| localName.equalsIgnoreCase(StaXParser.MEANING_TEXT)
				|| localName.equalsIgnoreCase(StaXParser.DESCRIPTION)) {
			if (nodeQueue.peek().equals(StaXParser.VALID_VALUE))
				this.methodName = getMethodName(localName);
		} else if (localName.equalsIgnoreCase(StaXParser.INSTRUCTION)) {
			if (nodeQueue.peek().equals(StaXParser.QUESTION))
				this.objToSetProperty = currQuestion;
			else if (nodeQueue.peek().equals(StaXParser.VALID_VALUE))
				this.objToSetProperty = currValidValue;
		} else if (localName.equalsIgnoreCase(StaXParser.TEXT)) {
			if (nodeQueue.peek().equals(StaXParser.INSTRUCTION)) {
				this.methodName = getMethodName(StaXParser.INSTRUCTION);
			}
		} else if (localName.equals(StaXParser.PREFERRED_DEFINITION)) {
			if (nodeQueue.peek().equals(StaXParser.MODULE)) {
				this.objToSetProperty = currModule;
				this.methodName = getMethodName(StaXParser.PREFERRED_DEFINITION);
			}
		}  else if (localName.equals(StaXParser.LONG_NAME) 
				|| localName.equals(StaXParser.CREATED_BY)) {
			if (nodeQueue.peek().equals(StaXParser.MODULE)) {
				this.objToSetProperty = currModule;
				this.methodName = getMethodName(localName);
			}
		} else if (localName.equals(StaXParser.DATATYPE_NAME) 
				||localName.equals(StaXParser.DECIMAL_PLACE) 
				||localName.equals(StaXParser.FORMAT_NAME) 
				||localName.equals(StaXParser.HIGH_VALUE_NUMBER) 
				||localName.equals(StaXParser.LOW_VALUE_NUMBER) 
				||localName.equals(StaXParser.MAXIMUM_LENGTH_NUMBER) 
				||localName.equals(StaXParser.MINIMUM_LENGTH_NUMBER)  
				||localName.equals(StaXParser.UOM_NAME) ) {
			if (nodeQueue.peek().equals(StaXParser.VALUE_DOMAIN)) {
				this.objToSetProperty = this.currQuestion;
				this.methodName = getMethodName(localName);
			}

		}
		
		
		
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

		this.methodName = null;
		if (localName.equals(StaXParser.FORM)) {
			logger.debug("Finished parsing a form");
			module_idx = -1;
			nodeQueue.clear();
		} else {
			if (localName.equals(StaXParser.MODULE))
				question_idx = -1;
			
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

		String currNode = (nodeQueue.peek() != null) ? nodeQueue.peek()
				.toLowerCase() : null;

		if (currNode != null && methodName != null) {
			logger.debug("Queue head: " + nodeQueue.peek() + " | method name: "
					+ methodName + " | value to set: " + xmlreader.getText());
			setProperty(this.objToSetProperty, methodName, xmlreader.getText());
		}
		
		methodName = null;
	}

	protected void setProperty(Object targetObj, String methodName, String value) {

		try {
			Class[] paramString = new Class[1];
			paramString[0] = String.class;
			Class currClass = targetObj.getClass();
			Method method = currClass.getMethod(methodName, paramString);

			method.invoke(targetObj, value);

		} catch (SecurityException se) {
			logger.error("setProperty: ", se);
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
			return;
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
			return;
		} catch (Exception ex) {
			logger.error("setProperty: ", ex);
		} catch (Throwable t) {
			logger.error("setProperty: ", t);
		}

	}

}
