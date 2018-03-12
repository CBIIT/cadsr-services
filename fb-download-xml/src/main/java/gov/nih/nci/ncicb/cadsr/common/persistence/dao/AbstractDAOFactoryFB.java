package gov.nih.nci.ncicb.cadsr.common.persistence.dao;

import gov.nih.nci.ncicb.cadsr.common.persistence.PersistenceConstants;

public abstract class AbstractDAOFactoryFB implements PersistenceConstants
{

	public AbstractDAOFactoryFB()
	{
	}

	public abstract FormDAO getFormDAO();

	public abstract FormV2DAO getFormV2DAO();

	public abstract AdminComponentDAO getAdminComponentDAO();

	public abstract ClassificationSchemeDAO getClassificationSchemeDAO();

	public abstract ModuleDAO getModuleDAO();

	public abstract ModuleV2DAO getModuleV2DAO();

	public abstract QuestionDAO getQuestionDAO();

	public abstract QuestionRepititionDAO getQuestionRepititionDAO();

	public abstract FormValidValueDAO getFormValidValueDAO();

	public abstract UserManagerDAO getUserManagerDAO();

	public abstract ContextDAO getContextDAO();

	public abstract FormCategoryDAO getFormCategoryDAO();

	public abstract WorkFlowStatusDAO getWorkFlowStatusDAO();

	public abstract FormInstructionDAO getFormInstructionDAO();

	public abstract ModuleInstructionDAO getModuleInstructionDAO();

	public abstract QuestionInstructionDAO getQuestionInstructionDAO();

	public abstract FormValidValueInstructionDAO getFormValidValueInstructionDAO();

	public abstract ValueDomainDAO getValueDomainDAO();

	public abstract ValueDomainV2DAO getValueDomainV2DAO();

	public abstract DerivedDataElementDAO getDerivedDataElementDAO();

	public abstract ConceptDAO getConceptDAO();

	public abstract ReferenceDocumentDAO getReferenceDocumentDAO();

	public abstract UtilDAO getUtilDAO();

	public abstract ReferenceDocumentTypeDAO getReferenceDocumentTypeDAO();

	public abstract ProtocolDAO getProtocolDAO();

	public abstract TriggerActionDAO getTriggerActionDAO();

	public abstract DataElementDAO getDataElementDAO();

	public abstract ContactCommunicationV2DAO getContactCommunicationV2DAO();

}
