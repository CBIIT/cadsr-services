package gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nih.nci.ncicb.cadsr.common.persistence.dao.AbstractDAOFactoryFB;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.AdminComponentDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.ClassificationSchemeDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.ConceptDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.ContactCommunicationV2DAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.ContextDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.DataElementDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.DerivedDataElementDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.FormCategoryDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.FormDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.FormInstructionDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.FormV2DAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.FormValidValueDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.FormValidValueInstructionDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.ModuleDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.ModuleInstructionDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.ModuleV2DAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.ProtocolDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.QuestionDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.QuestionInstructionDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.QuestionRepititionDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.ReferenceDocumentDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.ReferenceDocumentTypeDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.TriggerActionDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.UserManagerDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.UtilDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.ValueDomainDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.ValueDomainV2DAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.WorkFlowStatusDAO;

@Component("daoFactory")
public class JDBCDAOFactoryFB extends AbstractDAOFactoryFB
{
	@Autowired
	private FormDAO formDAO;

	@Autowired
	private FormV2DAO formV2DAO;

	@Autowired
	private AdminComponentDAO adminComponentDAO;

	@Autowired
	private ClassificationSchemeDAO classificationSchemeDAO;

	@Autowired
	private ModuleDAO moduleDAO;

	@Autowired
	private ModuleV2DAO moduleV2DAO;

	@Autowired
	private QuestionDAO questionDAO;

	@Autowired
	private QuestionRepititionDAO questionRepititionDAO;

	@Autowired
	private FormValidValueDAO formValidValueDAO;

	@Autowired
	private UserManagerDAO userManagerDAO;

	@Autowired
	private ContextDAO contextDAO;

	@Autowired
	private FormCategoryDAO formCategoryDAO;

	@Autowired
	private WorkFlowStatusDAO workFlowStatusDAO;

	@Autowired
	private FormInstructionDAO formInstructionDAO;

	@Autowired
	private ModuleInstructionDAO moduleInstructionDAO;

	@Autowired
	private QuestionInstructionDAO questionInstructionDAO;

	@Autowired
	private FormValidValueInstructionDAO formValidValueInstructionDAO;

	@Autowired
	private ValueDomainDAO valueDomainDAO;

	@Autowired
	private ValueDomainV2DAO valueDomainV2DAO;

	@Autowired
	private DerivedDataElementDAO derivedDataElementDAO;

	@Autowired
	private ConceptDAO conceptDAO;

	@Autowired
	private ReferenceDocumentDAO referenceDocumentDAO;

	@Autowired
	private UtilDAO utilDAO;

	@Autowired
	private ReferenceDocumentTypeDAO referenceDocumentTypeDAO;

	@Autowired
	private ProtocolDAO protocolDAO;

	@Autowired
	private TriggerActionDAO triggerActionDAO;

	@Autowired
	private DataElementDAO dataElementDAO;

	@Autowired
	private ContactCommunicationV2DAO contactCommunicationV2DAO;

	public JDBCDAOFactoryFB() {
	}

	public FormDAO getFormDAO() {
		return formDAO;
	}

	public void setFormDAO(FormDAO formDAO) {
		this.formDAO = formDAO;
	}

	public FormV2DAO getFormV2DAO() {
		return formV2DAO;
	}

	public void setFormV2DAO(FormV2DAO formV2DAO) {
		this.formV2DAO = formV2DAO;
	}

	public AdminComponentDAO getAdminComponentDAO() {
		return adminComponentDAO;
	}

	public void setAdminComponentDAO(AdminComponentDAO adminComponentDAO) {
		this.adminComponentDAO = adminComponentDAO;
	}

	public ClassificationSchemeDAO getClassificationSchemeDAO() {
		return classificationSchemeDAO;
	}

	public void setClassificationSchemeDAO(ClassificationSchemeDAO classificationSchemeDAO) {
		this.classificationSchemeDAO = classificationSchemeDAO;
	}

	public ModuleDAO getModuleDAO() {
		return moduleDAO;
	}

	public void setModuleDAO(ModuleDAO moduleDAO) {
		this.moduleDAO = moduleDAO;
	}

	public ModuleV2DAO getModuleV2DAO() {
		return moduleV2DAO;
	}

	public void setModuleV2DAO(ModuleV2DAO moduleV2DAO) {
		this.moduleV2DAO = moduleV2DAO;
	}

	public QuestionDAO getQuestionDAO() {
		return questionDAO;
	}

	public void setQuestionDAO(QuestionDAO questionDAO) {
		this.questionDAO = questionDAO;
	}

	public QuestionRepititionDAO getQuestionRepititionDAO() {
		return questionRepititionDAO;
	}

	public void setQuestionRepititionDAO(QuestionRepititionDAO questionRepititionDAO) {
		this.questionRepititionDAO = questionRepititionDAO;
	}

	public FormValidValueDAO getFormValidValueDAO() {
		return formValidValueDAO;
	}

	public void setFormValidValueDAO(FormValidValueDAO formValidValueDAO) {
		this.formValidValueDAO = formValidValueDAO;
	}

	public UserManagerDAO getUserManagerDAO() {
		return userManagerDAO;
	}

	public void setUserManagerDAO(UserManagerDAO userManagerDAO) {
		this.userManagerDAO = userManagerDAO;
	}

	public ContextDAO getContextDAO() {
		return contextDAO;
	}

	public void setContextDAO(ContextDAO contextDAO) {
		this.contextDAO = contextDAO;
	}

	public FormCategoryDAO getFormCategoryDAO() {
		return formCategoryDAO;
	}

	public void setFormCategoryDAO(FormCategoryDAO formCategoryDAO) {
		this.formCategoryDAO = formCategoryDAO;
	}

	public WorkFlowStatusDAO getWorkFlowStatusDAO() {
		return workFlowStatusDAO;
	}

	public void setWorkFlowStatusDAO(WorkFlowStatusDAO workFlowStatusDAO) {
		this.workFlowStatusDAO = workFlowStatusDAO;
	}

	public FormInstructionDAO getFormInstructionDAO() {
		return formInstructionDAO;
	}

	public void setFormInstructionDAO(FormInstructionDAO formInstructionDAO) {
		this.formInstructionDAO = formInstructionDAO;
	}

	public ModuleInstructionDAO getModuleInstructionDAO() {
		return moduleInstructionDAO;
	}

	public void setModuleInstructionDAO(ModuleInstructionDAO moduleInstructionDAO) {
		this.moduleInstructionDAO = moduleInstructionDAO;
	}

	public QuestionInstructionDAO getQuestionInstructionDAO() {
		return questionInstructionDAO;
	}

	public void setQuestionInstructionDAO(QuestionInstructionDAO questionInstructionDAO) {
		this.questionInstructionDAO = questionInstructionDAO;
	}

	public FormValidValueInstructionDAO getFormValidValueInstructionDAO() {
		return formValidValueInstructionDAO;
	}

	public void setFormValidValueInstructionDAO(FormValidValueInstructionDAO formValidValueInstructionDAO) {
		this.formValidValueInstructionDAO = formValidValueInstructionDAO;
	}
	
	public ValueDomainDAO getValueDomainDAO() {
		return valueDomainDAO;
	}

	public void setValueDomainDAO(ValueDomainDAO valueDomainDAO) {
		this.valueDomainDAO = valueDomainDAO;
	}

	public ValueDomainV2DAO getValueDomainV2DAO() {
		return valueDomainV2DAO;
	}

	public void setValueDomainV2DAO(ValueDomainV2DAO valueDomainV2DAO) {
		this.valueDomainV2DAO = valueDomainV2DAO;
	}

	public DerivedDataElementDAO getDerivedDataElementDAO() {
		return derivedDataElementDAO;
	}

	public void setDerivedDataElementDAO(DerivedDataElementDAO derivedDataElementDAO) {
		this.derivedDataElementDAO = derivedDataElementDAO;
	}

	public ConceptDAO getConceptDAO() {
		return conceptDAO;
	}

	public void setConceptDAO(ConceptDAO conceptDAO) {
		this.conceptDAO = conceptDAO;
	}

	public ReferenceDocumentDAO getReferenceDocumentDAO() {
		return referenceDocumentDAO;
	}

	public void setReferenceDocumentDAO(ReferenceDocumentDAO referenceDocumentDAO) {
		this.referenceDocumentDAO = referenceDocumentDAO;
	}

	public UtilDAO getUtilDAO() {
		return utilDAO;
	}

	public void setUtilDAO(UtilDAO utilDAO) {
		this.utilDAO = utilDAO;
	}

	public ReferenceDocumentTypeDAO getReferenceDocumentTypeDAO() {
		return referenceDocumentTypeDAO;
	}

	public void setReferenceDocumentTypeDAO(ReferenceDocumentTypeDAO referenceDocumentTypeDAO) {
		this.referenceDocumentTypeDAO = referenceDocumentTypeDAO;
	}

	public ProtocolDAO getProtocolDAO() {
		return protocolDAO;
	}

	public void setProtocolDAO(ProtocolDAO protocolDAO) {
		this.protocolDAO = protocolDAO;
	}

	public TriggerActionDAO getTriggerActionDAO() {
		return triggerActionDAO;
	}

	public void setTriggerActionDAO(TriggerActionDAO triggerActionDAO) {
		this.triggerActionDAO = triggerActionDAO;
	}

	public DataElementDAO getDataElementDAO() {
		return dataElementDAO;
	}

	public void setDataElementDAO(DataElementDAO dataElementDAO) {
		this.dataElementDAO = dataElementDAO;
	}

	public ContactCommunicationV2DAO getContactCommunicationV2DAO() {
		return contactCommunicationV2DAO;
	}

	public void setContactCommunicationV2DAO(ContactCommunicationV2DAO contactCommunicationV2DAO) {
		this.contactCommunicationV2DAO = contactCommunicationV2DAO;
	}

	public static void main(String[] args) {
		/**
		 * JDBCDAOFactory factory = (JDBCDAOFactory)new
		 * JDBCDAOFactory().getDAOFactory((ServiceLocator)new
		 * TestServiceLocatorImpl()); FormDAO dao = factory.getFormDAO();
		 * Collection test = dao.getFormsByContext("Context1");
		 * System.out.println(test);
		 */
	}
}
