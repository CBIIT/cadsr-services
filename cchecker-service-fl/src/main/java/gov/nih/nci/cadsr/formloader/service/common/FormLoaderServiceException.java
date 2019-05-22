package gov.nih.nci.cadsr.formloader.service.common;


public class FormLoaderServiceException extends Exception {
	
	public static final int ERROR_NO_ERROR = 0;
	public static final int ERROR_MALFORMED_XML = 1;
	public static final int ERROR_EMPTY_FORM_LIST = 2;
	public static final int ERROR_FILE_INVALID = 3;
	public static final int ERROR_COLLECTION_NULL = 4;
	public static final int ERROR_USER_INVALID = 5;
	public static final int ERROR_XML_EXCEPTION = 6;
	public static final int ERROR_COLLECTION_NAME_MISSING = 7;
	public static final int ERROR_FORMS_ELEMENT_MISSING = 7;
	
	protected int errorCode;
	
	//seems this is the only type that has an error object. Others are just messages.
	XmlValidationError error; 
	
	public  FormLoaderServiceException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public FormLoaderServiceException(int errorCode, String message, XmlValidationError error)  {
		super(message);
		this.errorCode = errorCode;
		this.error = error;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public XmlValidationError getError() {
		return error;
	}

	public void setError(XmlValidationError error) {
		this.error = error;
	}
	

}
