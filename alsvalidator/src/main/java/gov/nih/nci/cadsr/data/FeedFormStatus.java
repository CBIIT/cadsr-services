/**
 * Copyright (C) 2019 Frederick National Laboratory for Cancer Research - All rights reserved.
 */
package gov.nih.nci.cadsr.data;

public class FeedFormStatus {
	private String currFormName;
	private int currFormNumber;
	private int countValidatedQuestions;
	public String getCurrFormName() {
		return currFormName;
	}
	public void setCurrFormName(String currFormName) {
		this.currFormName = currFormName;
	}
	public int getCurrFormNumber() {
		return currFormNumber;
	}
	public void setCurrFormNumber(int currFormNumber) {
		this.currFormNumber = currFormNumber;
	}
	public int getCountValidatedQuestions() {
		return countValidatedQuestions;
	}
	public void setCountValidatedQuestions(int countValidatedQuestions) {
		this.countValidatedQuestions = countValidatedQuestions;
	}
	@Override
	public String toString() {
		return "FeedFormStatus [currFormName=" + currFormName + ", currFormNumber=" + currFormNumber
				+ ", countValidatedQuestions=" + countValidatedQuestions + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + currFormNumber;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FeedFormStatus other = (FeedFormStatus) obj;
		if (currFormNumber != other.currFormNumber)
			return false;
		return true;
	}
	
}
