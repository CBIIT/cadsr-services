package gov.nih.nci.ncicb.cadsr.formbuilder.service;

import gov.nih.nci.ncicb.cadsr.common.exception.FatalException;
import gov.nih.nci.ncicb.cadsr.common.exception.NestedCheckedException;

public class ServiceStartupException extends FatalException {
  
  public ServiceStartupException(String msg) {
    super(msg,null);
  }

  public ServiceStartupException(
    String msg,
    Throwable cause) {
    super(cause);
  }
  
}
