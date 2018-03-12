package gov.nih.nci.ncicb.cadsr.common.bulkdownload;

public class FormBuilderUtil
{
	public static boolean validateIdSeqRequestParameter(String idSeq)
	{
		boolean valid = true;
		if (idSeq != null && idSeq.matches("[^a-zA-Z0-9-]"))  
			valid = false;
				
		return valid;
	}
	
	public static boolean validateNumber(String numStr)
	{
		boolean valid = true;
		if (numStr != null && numStr.matches("[^\\d]"))  /*    "^.*(%|<|>|\\$).*$"  */
			valid = false;
				
		return valid;
	}

}
