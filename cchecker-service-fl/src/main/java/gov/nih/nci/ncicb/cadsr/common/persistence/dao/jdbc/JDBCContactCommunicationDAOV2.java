package gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc;

import gov.nih.nci.ncicb.cadsr.common.dto.AddressTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ContactCommunicationV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ContactTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.PersonTransferObject;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.ContactCommunicationV2DAO;
import gov.nih.nci.ncicb.cadsr.common.resource.Address;
import gov.nih.nci.ncicb.cadsr.common.resource.Contact;
import gov.nih.nci.ncicb.cadsr.common.resource.ContactCommunicationV2;
import gov.nih.nci.ncicb.cadsr.common.resource.Person;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.object.MappingSqlQuery;

public class JDBCContactCommunicationDAOV2 extends JDBCAdminComponentDAOV2
		implements ContactCommunicationV2DAO {
	
	private static final Logger logger = LoggerFactory.getLogger(JDBCContactCommunicationDAOV2.class.getName());
	
	public JDBCContactCommunicationDAOV2(DataSource dataSource) {
		super(dataSource);
	}
	
	public int createContactCommnunicationForComponent(String ac_idseq, String org_idseq, ContactCommunicationV2TransferObject contact) {
		//Insert contact comm. record
		String newseqid = createContactCommunication(org_idseq, contact);
		
		return (newseqid == null || newseqid.length() == 0) ? 0 :
			createContactCommunicationComponentMapping(ac_idseq, org_idseq, contact);	
	}
	
	protected int createContactCommunicationComponentMapping(String ac_idseq, String org_idseq, ContactCommunicationV2TransferObject contact) {
		String seqid = generateGUID();
		String createdBy = contact.getCreatedBy();
		String sql = "INSERT INTO sbr.AC_CONTACTS_VIEW " +
                " (acc_idseq, org_idseq, ac_idseq, created_by) " +
           " VALUES (:seqid, :org_idseq, :ac_idseq, :createdBy)";
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("seqid", seqid);
		params.addValue("org_idseq", org_idseq); //un-nillable: otherwise, get "unique constraint violated" error
		params.addValue("ac_idseq", ac_idseq);
		params.addValue("createdBy", createdBy);
		
		try {
			int res = this.namedParameterJdbcTemplate.update(sql, params);
			return res;
		} catch (DataAccessException de) {
			logger.debug(de.getMessage());
			throw de;
		}
	}
	
	protected String createContactCommunication(String org_idseq, ContactCommunicationV2TransferObject contact) {
		String seqid = generateGUID();
		String sql = "INSERT INTO sbr.contact_comms_view " +
                " (ccomm_idseq, org_idseq, per_idseq, ctl_name, rank_order, " +
                "  cyber_address, created_By) " +
           " VALUES (:ccomm_idseq, :org_idseq, :per_idseq, :ctl_name, :rank_order, " +
               "   :cyber_address, :createdBy)";
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("ccomm_idseq", seqid);
		
		params.addValue("org_idseq", org_idseq); //un-nillable: otherwise, get "unique constraint violated" error
		params.addValue("per_idseq", null);
		
		params.addValue("ctl_name", contact.getType());
		
		params.addValue("rank_order", contact.getRankOrder()); //un-nullable
		params.addValue("cyber_address", contact.getValue());
		params.addValue("createdBy", contact.getCreatedBy());
		
		try {
			int res = this.namedParameterJdbcTemplate.update(sql, params);
			return seqid;
		} catch (DataAccessException de) {
			logger.debug(de.getMessage());
			throw de;
		}
	}
	
	public String getOrganizationIdseqByName(String org_name) {
		
		String sql = "select ORG_IDSEQ from sbr.ORGANIZATIONS_VIEW where name=:org_name";
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("org_name", org_name);
		
		List<String> seqids = this.namedParameterJdbcTemplate.query(sql, params, 
	     		new RowMapper<String>() {
	     	public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	     		return rs.getString("ORG_IDSEQ");
	         }
	     });
		
		return (seqids == null || seqids.size() == 0) ? null : seqids.get(0);
	}
	
	

	// (based on JDBCAdminComponentDAO#getContacts)
	public List<ContactCommunicationV2> getContactCommunicationV2sForAC(String acIdseq) {
		
		List<ContactCommunicationV2> ccV2List = new ArrayList();

		PersonContact2ByACIdQuery personQuery = new PersonContact2ByACIdQuery();
		personQuery.setDataSource(getDataSource());
		List<Contact> personContacts = personQuery.getPersonContacts(acIdseq);

		ContactCommunicationsV2Query commQuery = new ContactCommunicationsV2Query();
		commQuery.setDataSource(getDataSource());
		Iterator<Contact> perIter=personContacts.iterator();
		while (perIter.hasNext()) {
			Person person = perIter.next().getPerson();
			ccV2List.addAll(commQuery.getContactCommsbyPerson(person));
		}
		
		OrgContactDataByACIdQuery orgQuery = new OrgContactDataByACIdQuery();
		orgQuery.setDataSource(getDataSource());
		List<OrganizationData> orgContacts = orgQuery.getOrgContacts(acIdseq);
		Iterator<OrganizationData> orgIter=orgContacts.iterator();
		while (orgIter.hasNext()) {
			OrganizationData orgData = orgIter.next();
			ccV2List.addAll(commQuery.getContactCommsbyOrg(orgData));
		}

		return ccV2List;
	}

	class ContactCommunicationsV2Query extends MappingSqlQuery {
		ContactCommunicationsV2Query() {
			super();
		}

		public void setQuerySql(String idType, String idSeq) {
			String querySql = " select cc.CCOMM_IDSEQ, cc.CTL_NAME, cc.CYBER_ADDRESS, "
					+ " cc.RANK_ORDER, cc.DATE_CREATED, cc.CREATED_BY, cc.DATE_MODIFIED, cc.MODIFIED_BY "
					+ " from sbr.contact_comms_view cc "
					+ " where "
					+ idType
					+ " = '"
					+ idSeq
					+ "'"
					+ " and ( CTL_NAME='PHONE' OR CTL_NAME='EMAIL' OR CTL_NAME='FAX' OR CTL_NAME='In Person') "
					+ " ORDER BY rank_order";
			super.setSql(querySql);
			// Note: We are only supporting types in V2 form format. (i.e. no "MAIL")
		}

		protected Object mapRow(ResultSet rs, int rownum) throws SQLException {

			ContactCommunicationV2 cc = new ContactCommunicationV2TransferObject();
			cc.setId(rs.getString("ccomm_idseq"));
			cc.setType(rs.getString("ctl_name"));
			cc.setValue(rs.getString("cyber_address"));
			cc.setRankOrder(rs.getInt("rank_order"));
			cc.setDateCreated(rs.getTimestamp("date_created"));
			cc.setCreatedBy(rs.getString("created_by"));
			cc.setDateModified(rs.getTimestamp("date_modified"));
			cc.setModifiedBy(rs.getString("modified_by"));

			return cc;
		}

		protected List<ContactCommunicationV2> getContactCommsbyPerson(
				Person person) {
			this.setQuerySql("per_idseq", person.getId());
			List ccList = execute();

			Iterator it = ccList.iterator();
			while (it.hasNext()) {
				ContactCommunicationV2 cc = (ContactCommunicationV2) it.next();
				cc.setPerson(person);
				// TODO: Person is wrong format
			}

			return ccList;

		}

		protected List<ContactCommunicationV2> getContactCommsbyOrg(
				OrganizationData org) {
			this.setQuerySql("org_idseq", org.org_id);
			List ccList = execute();

			Iterator it = ccList.iterator();
			while (it.hasNext()) {
				ContactCommunicationV2 cc = (ContactCommunicationV2) it.next();
				cc.setOrganizationName(org.organizationName);
				cc.setOrganizationRAI(org.organizationRAI);
			}

			return ccList;
		}
		
	}

	// -- copied from JDBCAdminComponentDAO --- PersonContact extended to fill in needed fields, OrgContact slimmed down and added RAI

	class PersonContact2ByACIdQuery extends MappingSqlQuery {
		String last_accId = null;
		Contact currentContact = null;
		List contactList = new ArrayList();
		Person currPerson = null;

		PersonContact2ByACIdQuery() {
			super();
		}

		public void setQuerySql(String acidSeq) {
			String querySql = " SELECT acc.acc_idseq, acc.org_idseq, acc.per_idseq, acc.contact_role,"
					+ " per.LNAME, per.FNAME, addr.CADDR_IDSEQ,"
					+ " addr.ADDR_LINE1, addr.ADDR_LINE2, addr.CADDR_IDSEQ, addr.CITY, addr.POSTAL_CODE, addr.STATE_PROV,"
					+ " addr.COUNTRY, addr.rank_order as addr_rank_order, addr.atl_name, per.position "
					+ "  FROM sbr.ac_contacts_view acc, sbr.persons_view per, sbr.contact_addresses_view addr "
					+ " where  acc.ac_idseq = '"
					+ acidSeq
					+ "' and "
					+ " acc.per_idseq = per.per_idseq  and addr.PER_IDSEQ = per.PER_IDSEQ "
					+ " and (addr.atl_name = 'MAILING' or addr.atl_name = 'Package Delivery')"
					+ "   ORDER BY acc.acc_idseq, acc.rank_order ";
			// Note: We are only supporting type (atl_name) in V2 form format.
			super.setSql(querySql);
		}

		protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
			String accId = rs.getString("acc_idseq");

			Address address = new AddressTransferObject();
			address.setAddressLine1(rs.getString("addr_line1"));
			address.setAddressLine2(rs.getString("addr_line2"));
			address.setId(rs.getString("CADDR_IDSEQ"));
			address.setCity(rs.getString("city"));
			address.setPostalCode(rs.getString("POSTAL_CODE"));
			address.setState(rs.getString("STATE_PROV"));
			address.setCountry(rs.getString("COUNTRY"));
			address.setRank(rs.getInt("addr_rank_order"));
			address.setType(rs.getString("atl_name"));

			String personId = rs.getString("per_idseq");

			if (currPerson == null || !currPerson.getId().equals(personId)) {
				currPerson = new PersonTransferObject();
				currPerson.setFirstName(rs.getString("fname"));
				currPerson.setLastName(rs.getString("lname"));
				currPerson.setId(rs.getString("per_idseq"));
				currPerson.setPosition(rs.getString("position"));
				currPerson.setAddresses(new ArrayList());
			}

			currPerson.getAddresses().add(address);

			if (currentContact == null
					|| !currentContact.getIdseq().equals(accId)) {
				currentContact = new ContactTransferObject();
				currentContact.setIdseq(accId);
				currentContact.setContactRole(rs.getString("contact_role"));
				contactList.add(currentContact);
			}
			currentContact.setPerson(currPerson);

			return currentContact;
		}

		protected List getPersonContacts(String acIdSeq) {
			setQuerySql(acIdSeq);
			this.execute();
			return contactList;
		}
	}

	class OrganizationData {
		OrganizationData() {}
		protected String organizationName;
		protected String organizationRAI;
		protected String org_id;
	}

	class OrgContactDataByACIdQuery extends MappingSqlQuery {

		OrgContactDataByACIdQuery() {
			super();
		}

		public void setQuerySql(String acidSeq) {
			String querySql = " SELECT acc.acc_idseq, acc.rank_order, acc.org_idseq,"
					+ " org.name, org.rai"
					+ "  FROM sbr.ac_contacts_view acc, sbr.organizations_view org "
					+ " where  acc.ac_idseq = '"
					+ acidSeq
					+ "' and "
					+ " acc.org_idseq = org.org_idseq"
					+ "   ORDER BY acc.acc_idseq, acc.rank_order ";
			super.setSql(querySql);
		}

		protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
			OrganizationData currOrg = new OrganizationData();
			currOrg.org_id = rs.getString("org_idseq");
			currOrg.organizationName = rs.getString("name");
			currOrg.organizationRAI = rs.getString("rai");

			return currOrg;
		}

		protected List getOrgContacts(String acIdSeq) {
			setQuerySql(acIdSeq);
			return this.execute();
		}
	}	

	/**
	 * Returns all valid contact communication types
	 * @return
	 */
	public List<String> getAllContactCommunicationTypes() {
		String sql = "select distinct CTL_NAME from SBR.COMM_TYPES_LOV_VIEW " +
				" order by CTL_NAME";

		List rows = this.namedParameterJdbcTemplate.getJdbcOperations().queryForList(sql);

		List<String> cmTypes = new ArrayList<String>();
		for (Object row : rows) {
			cmTypes.add((String)((Map)row).get("CTL_NAME"));
		}

		return cmTypes;
	}
}

