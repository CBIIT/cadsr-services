package gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc;

import gov.nih.nci.ncicb.cadsr.common.persistence.dao.ValueDomainV2DAO;
import gov.nih.nci.ncicb.cadsr.common.dto.ValueDomainV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.resource.ValueDomainV2;
import gov.nih.nci.ncicb.cadsr.common.resource.ValueDomain;
import gov.nih.nci.ncicb.cadsr.common.resource.ConceptDerivationRule;
import gov.nih.nci.ncicb.cadsr.common.resource.PermissibleValueV2;
import gov.nih.nci.ncicb.cadsr.common.resource.ValueMeaningV2;
import gov.nih.nci.ncicb.cadsr.common.dto.ConceptDerivationRuleTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.PermissibleValueV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ValueMeaningV2TransferObject;

import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.SqlParameter;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.List;

@Component("valueDomainV2DAO")
public class JDBCValueDomainV2DAOFB extends JDBCAdminComponentDAOFB implements ValueDomainV2DAO
{
	public JDBCValueDomainV2DAOFB() {
		super();
	}

	public ValueDomainV2 getValueDomainV2ById(String vdId) {
		ValueDomainQuery query = new ValueDomainQuery(getDataSource());
		query.setSql();

		// value domain fully populated but concept is not populated (nor
		// permissible values)
		ValueDomainV2 valueDomainV2 = null;
		List ret = query.getValueDomainById(vdId);
		if (ret != null && !ret.isEmpty()) {
			valueDomainV2 = (ValueDomainV2) ret.get(0);

			// get permissible values
			PermissibleValueQuery pvQuery = new PermissibleValueQuery(
					getDataSource());
			pvQuery.setSql();
			List permissibleValues = pvQuery.getPermissibleValuesByVDId(vdId);
			
			//System.out.println("In getValueDomainV2ById: calling getVMDesignationsAndDefinitions ");
			permissibleValues = getVMDesignationsAndDefinitions(permissibleValues);
			
			valueDomainV2.setPermissibleValueV2(permissibleValues);
		}

		return valueDomainV2;
	}
	
	protected List getVMDesignationsAndDefinitions(List permissibleValues) {
		//System.out.println("In getVMDesignationsAndDefinitions params: ");
		if (permissibleValues == null)
			return permissibleValues;
		
		//List<String> vmids = new ArrayList<String>();
		
		List<PermissibleValueV2> pvs = permissibleValues;
		for (PermissibleValueV2 pv : pvs) { 
			ValueMeaningV2 vm = pv.getValueMeaningV2();
			
			if (vm == null) continue;
			String vmSeqid = vm.getIdseq();
			
			if (vmSeqid != null && vmSeqid.length() > 0) {
				
				List des = this.getDesignations(vmSeqid, null);
				List defs = this.getDefinitions(vmSeqid);
				
				if (des != null && des.size() > 0)
					vm.setDesignations(des);
				
				if (defs != null && des.size() > 0)
					vm.setDefinitions(defs);
			}
		}
	
		return pvs;
	}

	// based on ValueDomainQuery from JDBCValueDomainDAO
	/**
	 * Inner class to get all valid values for each value domain
	 * 
	 */
	class ValueDomainQuery extends MappingSqlQuery {

		// vd_idseq is the key and vvList is the value (list of valid values)
		ValueDomainQuery(DataSource ds) {
			super();
			this.setDataSource(ds);
		}

		public void setSql() {
			String sql = " SELECT VD_IDSEQ, LONG_NAME, VERSION, VD_ID, ASL_NAME, UOML_NAME, MAX_LENGTH_NUM, MIN_LENGTH_NUM, DECIMAL_PLACE, HIGH_VALUE_NUM, LOW_VALUE_NUM, CONDR_IDSEQ, FORML_NAME, DTL_NAME from SBR.VALUE_DOMAINS_VIEW where VD_IDSEQ=?";
			setSql(sql);
			declareParameter(new SqlParameter("VD_IDSEQ", Types.VARCHAR));
			compile();
		}

		protected Object mapRow(ResultSet rs, int rownum) throws SQLException {

			// reaches to this point for each record retrieved.
			String vdomainIdSeq = rs.getString(1); // VD_IDSEQ

			ValueDomain vd = new ValueDomainV2TransferObject();
			vd.setVdIdseq(vdomainIdSeq);
			vd.setLongName(rs.getString(2));
			vd.setVersion(rs.getFloat(3));
			vd.setPublicId(rs.getInt(4));
			vd.setAslName(rs.getString(5));
			vd.setUnitOfMeasure(rs.getString(6));
			vd.setMaxLength(rs.getString(7));
			vd.setMinLength(rs.getString(8));
			vd.setDecimalPlace(rs.getString(9));
			vd.setHighValue(rs.getString(10));
			vd.setLowValue(rs.getString(11));
			vd.setDisplayFormat(rs.getString(13));
			vd.setDatatype(rs.getString(14));
			ConceptDerivationRule cdr = new ConceptDerivationRuleTransferObject();
			cdr.setIdseq(rs.getString(12));

			vd.setConceptDerivationRule(cdr);
			return vd;
		}

		public List getValueDomainById(String vdId) {
			Object[] obj = new Object[] { vdId };

			return execute(obj);

		}

	}

	class PermissibleValueQuery extends MappingSqlQuery {

		// vd_idseq is the key and vvList is the value (list of valid values)
		PermissibleValueQuery(DataSource ds) {
			super();
			this.setDataSource(ds);
		}

		public void setSql() {
			String sql = "select VALUE, vm.PUBLIC_ID, vm.VERSION, VM.VM_IDSEQ, vm.PREFERRED_DEFINITION, vm.LONG_NAME from CABIO31_VD_PV_VIEW vdpv, CABIO31_PV_VIEW pv, CABIO31_VM_VIEW vm where vdpv.pv_idseq = pv.pv_idseq and pv.vm_idseq = vm.vm_idseq and vdpv.vd_idseq=?";
			setSql(sql);
			declareParameter(new SqlParameter("VD_IDSEQ", Types.VARCHAR));
			compile();
		}

		protected Object mapRow(ResultSet rs, int rownum) throws SQLException {

			PermissibleValueV2 pv = new PermissibleValueV2TransferObject();
			pv.setValue(rs.getString("VALUE"));

			ValueMeaningV2 vm = new ValueMeaningV2TransferObject();
			vm.setPublicId(rs.getInt("PUBLIC_ID"));
			vm.setVersion(rs.getFloat("VERSION"));
			vm.setPreferredDefinition(rs.getString("PREFERRED_DEFINITION"));
			vm.setLongName(rs.getString("LONG_NAME"));
			vm.setIdseq(rs.getString("VM_IDSEQ"));

			pv.setValueMeaningV2(vm);

			return pv;
		}

		public List getPermissibleValuesByVDId(String vdId) {
			Object[] obj = new Object[] { vdId };

			return execute(obj);

		}
	}
}
