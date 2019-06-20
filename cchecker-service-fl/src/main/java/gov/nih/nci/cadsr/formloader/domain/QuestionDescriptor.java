package gov.nih.nci.cadsr.formloader.domain;

import java.util.ArrayList;
import java.util.List;

public class QuestionDescriptor {
	
	String questionSeqId;
	String publicId;
	String version;
	
	String questionText;
	
	String cdeSeqId;
	String cdePublicId;
	String cdeVersion;
	String cdeVdPublicId;
	String cdeVdVersion;
	
	//value domain fields that we need to check
	String datatypeName;
	String decimalPlace;
	String formatName;
	String highValueNumber;
	String lowValueNumber;
	String maximumLengthNumber;
	String minimumLengthNumber;
	String UOMName;
	
	List<ValidValue> validValues = new ArrayList<ValidValue>();
	String defaultValue;
	String instruction;
	
	boolean isEditable;
	boolean isMandatory;
	
	//Needed for loader only
	boolean skip = false;
	List<String> messages = new ArrayList<String>();
	
	public String getCdeVdPublicId() {
		return cdeVdPublicId;
	}

	public void setCdeVdPublicId(String cdeVdPublicId) {
		this.cdeVdPublicId = cdeVdPublicId;
	}

	public String getCdeVdVersion() {
		return cdeVdVersion;
	}

	public void setCdeVdVersion(String cdeVdVersion) {
		this.cdeVdVersion = cdeVdVersion;
	}

	public void addMessage(String msg) {
		messages.add(msg);
	}
	
	public List<String> getMessages() {
		return messages;
	}
	
	public String getQuestionSeqId() {
		return questionSeqId;
	}

	public void setQuestionSeqId(String questionSeqId) {
		this.questionSeqId = questionSeqId;
	}



	public String getPublicId() {
		return publicId;
	}



	public void setPublicId(String publicId) {
		this.publicId = publicId;
	}



	public String getVersion() {
		return version;
	}



	public void setVersion(String version) {
		this.version = version;
	}



	public String getQuestionText() {
		return questionText;
	}



	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}



	public String getCdeSeqId() {
		return cdeSeqId;
	}



	public void setCdeSeqId(String cdeSeqId) {
		this.cdeSeqId = cdeSeqId;
	}



	public String getCdePublicId() {
		return cdePublicId;
	}



	public void setCdePublicId(String cdePublicId) {
		this.cdePublicId = cdePublicId;
	}



	public String getCdeVersion() {
		return cdeVersion;
	}



	public void setCdeVersion(String cdeVersion) {
		this.cdeVersion = cdeVersion;
	}



	public List<ValidValue> getValidValues() {
		return validValues;
	}



	public void setValidValues(List<ValidValue> validValues) {
		this.validValues = validValues;
	}



	public String getDefaultValue() {
		return defaultValue;
	}



	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}



	public String getInstruction() {
		return instruction;
	}



	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

	public void addInstruction(String newInstruction) {
		if (instruction == null || instruction.length() == 0)
			instruction = "[FormLoader]:" + newInstruction;
		else
			instruction += "; " + newInstruction;
	}

	public boolean isSkip() {
		return skip;
	}

	public void setSkip(boolean skip) {
		this.skip = skip;
	}

	public boolean isEditable() {
		return isEditable;
	}

	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}
	
	public void setEditable(String val) {
		if (val == null || val.length() == 0)
			setEditable(true); //should default to true?
		
		boolean yesno = (val.equalsIgnoreCase("yes") || val.equalsIgnoreCase("true")) ?
				true : false;
		
		setEditable(yesno);
	}

	public boolean isMandatory() {
		return isMandatory;
	}

	public void setMandatory(boolean isMandatory) {
		this.isMandatory = isMandatory;
	}
	
	public void setMandatory(String val) {
		if (val == null || val.length() == 0)
			setMandatory(true); //should default to true?
		
		boolean yesno = (val.equalsIgnoreCase("yes") || val.equalsIgnoreCase("true")) ?
				true : false;
		
		setMandatory(yesno);
	}

	public void addInstructionAndMessage(String message) {
		this.addInstruction(message);
		this.addMessage(message);
	}

	public String getDatatypeName() {
		return datatypeName;
	}

	public void setDatatypeName(String datatypeName) {
		this.datatypeName = datatypeName;
	}

	public String getDecimalPlace() {
		return decimalPlace;
	}

	public void setDecimalPlace(String decimalPlace) {
		this.decimalPlace = decimalPlace;
	}

	public String getFormatName() {
		return formatName;
	}

	public void setFormatName(String formatName) {
		this.formatName = formatName;
	}

	public String getHighValueNumber() {
		return highValueNumber;
	}

	public void setHighValueNumber(String highValueNumber) {
		this.highValueNumber = highValueNumber;
	}

	public String getLowValueNumber() {
		return lowValueNumber;
	}

	public void setLowValueNumber(String lowValueNumber) {
		this.lowValueNumber = lowValueNumber;
	}

	public String getMaximumLengthNumber() {
		return maximumLengthNumber;
	}

	public void setMaximumLengthNumber(String maximumLengthNumber) {
		this.maximumLengthNumber = maximumLengthNumber;
	}

	public String getMinimumLengthNumber() {
		return minimumLengthNumber;
	}

	public void setMinimumLengthNumber(String minimumLengthNumber) {
		this.minimumLengthNumber = minimumLengthNumber;
	}

	public String getUOMName() {
		return UOMName;
	}

	public void setUOMName(String uOMName) {
		UOMName = uOMName;
	}



	public class ValidValue {
		String value;
		String meaningText;
		boolean skip = false;
		
		//These will come back from value domain permissible value query
		String vdPermissibleValueSeqid;
		
		//Denise: value meaning public id_quetionpublicid_form_public_id_version_<x> x = 1, 2, 3
		String preferredName; //This happens to be the value meaning's public id
		String preferredDefinition;
		String longName;
		String instruction;
		String description;
		
		public boolean isSkip() {
			return skip;
		}
		public void setSkip(boolean skip) {
			this.skip = skip;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public String getMeaningText() {
			return meaningText;
		}
		public void setMeaningText(String valueMeaning) {
			this.meaningText = valueMeaning;
		}
		public String getVdPermissibleValueSeqid() {
			return vdPermissibleValueSeqid;
		}
		public void setVdPermissibleValueSeqid(String vdPermissibleValueSeqid) {
			this.vdPermissibleValueSeqid = vdPermissibleValueSeqid;
		}
		
		public String getPreferredName() {
			return preferredName;
		}
		public void setPreferredName(String preferredName) {
			this.preferredName = preferredName;
		}
		
		public String getPreferredDefinition() {
			return preferredDefinition;
		}
		public void setPreferredDefinition(String preferredDefinition) {
			this.preferredDefinition = preferredDefinition;
		}
		public String getInstruction() {
			return instruction;
		}
		public void setInstruction(String instruction) {
			this.instruction = instruction;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		
	}

	public boolean hasVDValueInDE() {
		
		return cdeVdPublicId != null && cdeVdPublicId.length() > 0
				|| cdeVdVersion != null && cdeVdVersion.length() > 0
				|| datatypeName != null && datatypeName.length() > 0
				|| decimalPlace != null && decimalPlace.length() > 0
				|| formatName != null && formatName.length() > 0
				|| highValueNumber != null && highValueNumber.length() > 0
				|| lowValueNumber != null && lowValueNumber.length() > 0
				|| maximumLengthNumber != null && maximumLengthNumber.length() > 0
				|| minimumLengthNumber != null && minimumLengthNumber.length() > 0
				|| UOMName != null && UOMName.length() > 0;
				
	}
	
	public boolean allPresentedVdFieldsMatched(String datatypeName, String decimalPlace, String formatName,
			String highValueNumber, String lowValueNumber, String maximumLengthNumber, 
			String minimumLengthNumber, String UOMName) {
		
		if (this.datatypeName != null && this.datatypeName.length() > 0 
				&& !this.datatypeName.equals(datatypeName))
			return false;
		
		if (this.decimalPlace != null && this.decimalPlace.length() > 0 
				&& !this.decimalPlace.equals(decimalPlace))
			return false;
		
		if (this.formatName != null && this.formatName.length() > 0 
				&& !this.formatName.equals(formatName))
			return false;
		
		if (this.highValueNumber != null && this.highValueNumber.length() > 0 
				&& !this.highValueNumber.equals(highValueNumber))
			return false;
		
		if (this.lowValueNumber != null && this.lowValueNumber.length() > 0 
				&& !this.lowValueNumber.equals(lowValueNumber))	//JR447 - lowValueNumber is 0 and yet highValueNumber is 10 (highValueNumber should be just lowValueNumber)
			return false;
		
		if (this.maximumLengthNumber != null && this.maximumLengthNumber.length() > 0 
				&& !this.maximumLengthNumber.equals(maximumLengthNumber))
			return false;
		
		if (this.minimumLengthNumber != null && this.minimumLengthNumber.length() > 0 
				&& !this.minimumLengthNumber.equals(minimumLengthNumber))
			return false;
		
		if (this.UOMName != null && this.UOMName.length() > 0 
				&& !this.UOMName.equals(UOMName))
			return false;
		
		return true;
	}
}
