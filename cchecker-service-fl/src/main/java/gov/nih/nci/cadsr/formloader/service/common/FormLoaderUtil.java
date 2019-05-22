package gov.nih.nci.cadsr.formloader.service.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gov.nih.nci.cadsr.formloader.domain.FormCollection;
import gov.nih.nci.cadsr.formloader.domain.FormDescriptor;
import gov.nih.nci.ncicb.cadsr.common.dto.PermissibleValueV2TransferObject;

/**
 * Utility class can be application specific but does not need to be. 
 * It can be used in other caDSR tool if needed.
 */
public class FormLoaderUtil {

	public static final void printFormLoader(FormCollection forms) {
		if(forms != null) {
			List fs = forms.getForms();
		    for(int i=0; i<fs.size(); i++) {
		        FormDescriptor f = (FormDescriptor)fs.get(i);
		    }
		}
	}

	public static final void printFormLoader(List forms) {
		if(forms != null) {
		    for(int i=0; i<forms.size(); i++) {
		        FormDescriptor f = (FormDescriptor)forms.get(i);
		    }
		}
	}

}
