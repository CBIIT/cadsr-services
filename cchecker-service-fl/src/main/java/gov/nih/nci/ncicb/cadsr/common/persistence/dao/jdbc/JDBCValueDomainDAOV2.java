package gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc;

import gov.nih.nci.ncicb.cadsr.common.dto.ConceptDerivationRuleTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.PermissibleValueV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ValueDomainV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ValueMeaningV2TransferObject;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.ValueDomainDAOV2;
import gov.nih.nci.ncicb.cadsr.common.resource.ConceptDerivationRule;
import gov.nih.nci.ncicb.cadsr.common.resource.ValueDomain;
import gov.nih.nci.ncicb.cadsr.common.resource.ValueDomainV2;
import gov.nih.nci.ncicb.cadsr.common.resource.ValueMeaningV2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.object.MappingSqlQuery;

/**
 * Mod. from JDBCValueDomainV2DAO
 * @author yangs8
 *
 */
public class JDBCValueDomainDAOV2 extends JDBCAdminComponentDAOV2 implements
	ValueDomainDAOV2 {
	
	private static final Logger logger = LoggerFactory.getLogger(JDBCValueDomainDAOV2.class.getName());
	
	 public JDBCValueDomainDAOV2(DataSource dataSource) {
		  super(dataSource);
	  }

	public ValueDomainV2 getValueDomainV2ById(String vdId) {
		ValueDomainQuery query = new ValueDomainQuery(dataSource);
		query.setSql();

		// value domain fully populated but concept is not populated (nor
		// permissible values)
		ValueDomainV2 valueDomainV2 = null;
		List ret = query.getValueDomainById(vdId);
		if (ret != null && !ret.isEmpty()) {
			valueDomainV2 = (ValueDomainV2) ret.get(0);

			// get permissible values
			PermissibleValueQuery pvQuery = new PermissibleValueQuery(dataSource);
			pvQuery.setSql();
			List permissibleValues = pvQuery.getPermissibleValuesByVDId(vdId);
			//debug
			if (permissibleValues.size() == 0) {
				logger.debug("VD seqid [" + vdId + "] has no permissible values from db");
			} else
				logger.debug("VD seqid [" + vdId + "] has " + permissibleValues.size() + " permissible values from db");
			valueDomainV2.setPermissibleValueV2(permissibleValues);
		}

		return valueDomainV2;
	}
	
	public List<PermissibleValueV2TransferObject> getPermissibleValuesByVdId(String vdId) {
		ValueDomainQuery query = new ValueDomainQuery(dataSource);
		query.setSql();

		// get permissible values
		PermissibleValueQuery pvQuery = new PermissibleValueQuery(dataSource);
		pvQuery.setSql();
		List<PermissibleValueV2TransferObject> permissibleValues = pvQuery.getPermissibleValuesByVDId(vdId);
		//debug
		if (permissibleValues.size() == 0) {
			logger.debug("VD seqid [" + vdId + "] has no permissible values from db");
		} else
			logger.debug("VD seqid [" + vdId + "] has " + permissibleValues.size() + " permissible values from db");

		

		return permissibleValues;
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

		//FORMBUILD-500: Added VD_TYPE_FLAG to the select
		public void setSql() {
			String sql = " SELECT VD_IDSEQ, LONG_NAME, VERSION, VD_ID, ASL_NAME, UOML_NAME," +
					" MAX_LENGTH_NUM, MIN_LENGTH_NUM, DECIMAL_PLACE, HIGH_VALUE_NUM, " +
					" LOW_VALUE_NUM, CONDR_IDSEQ, FORML_NAME, DTL_NAME, VD_TYPE_FLAG from SBR.VALUE_DOMAINS_VIEW where VD_IDSEQ=?";
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
			vd.setVDType(rs.getString(15));
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
			String sql = "select VALUE, vm.PUBLIC_ID, vm.VERSION, vm.PREFERRED_DEFINITION, vm.LONG_NAME " +
					" from CABIO31_VD_PV_VIEW vdpv, CABIO31_PV_VIEW pv, CABIO31_VM_VIEW vm " +
					" where vdpv.pv_idseq = pv.pv_idseq and pv.vm_idseq = vm.vm_idseq and vdpv.vd_idseq=?";
			setSql(sql);
			declareParameter(new SqlParameter("VD_IDSEQ", Types.VARCHAR));
			compile();
		}

		protected Object mapRow(ResultSet rs, int rownum) throws SQLException {

			PermissibleValueV2TransferObject pv = new PermissibleValueV2TransferObject();
			pv.setValue(rs.getString("VALUE"));

			ValueMeaningV2 vm = new ValueMeaningV2TransferObject();
			vm.setPublicId(rs.getInt("PUBLIC_ID"));
			vm.setVersion(rs.getFloat("VERSION"));
			vm.setPreferredDefinition(rs.getString("PREFERRED_DEFINITION"));
			vm.setLongName(rs.getString("LONG_NAME"));

			pv.setValueMeaningV2(vm);	//JR417 vm pub id is good

			return pv;
		}

		public List<PermissibleValueV2TransferObject> getPermissibleValuesByVDId(String vdId) {
			Object[] obj = new Object[] { vdId };

			return execute(obj);

		}
	}
	
	public HashMap<String, List<PermissibleValueV2TransferObject>> getPermissibleValuesByVdIds(List<String> vdSeqIds) {
		
		String sql = 
    			"select vdpv.vd_idseq, vdpv.vp_idseq, pv.VALUE, vm.PUBLIC_ID, vm.VERSION, " +
    					" vm.PREFERRED_DEFINITION, vm.LONG_NAME, vm.DESCRIPTION, VM.VM_IDSEQ " +
    					" from CABIO31_VD_PV_VIEW vdpv, CABIO31_PV_VIEW pv, CABIO31_VM_VIEW vm " +
    					" where vdpv.pv_idseq = pv.pv_idseq and pv.vm_idseq = vm.vm_idseq and vdpv.vd_idseq in (:seqIds) " +
    					" order by vdpv.vd_idseq";

    	MapSqlParameterSource params = new MapSqlParameterSource();
    	params.addValue("seqIds", vdSeqIds);
    	
    	final HashMap<String, List<PermissibleValueV2TransferObject>> pvsMap = 
    			new HashMap<String, List<PermissibleValueV2TransferObject>>();
    	
    	List<PermissibleValueV2TransferObject> des = 
    			this.namedParameterJdbcTemplate.query(sql, params, 
    					new RowMapper<PermissibleValueV2TransferObject>() {
    				public PermissibleValueV2TransferObject mapRow(ResultSet rs, int rowNum) throws SQLException {
    					
    					String vdSeqId = rs.getString("VD_IDSEQ");				
    					
    					PermissibleValueV2TransferObject pv = new PermissibleValueV2TransferObject();
    					
    					pv.setValue(rs.getString("VALUE")); 
    					pv.setIdseq(rs.getString("VP_IDSEQ"));	//JR417 this is THE vdPvIdSeq! Not sure why is it populated during content validation but it is what it is!

    					ValueMeaningV2TransferObject vm = new ValueMeaningV2TransferObject();
    					vm.setPublicId(rs.getInt("PUBLIC_ID")); //this is also the preferred name for some reason
    					vm.setVersion(rs.getFloat("VERSION"));
    					vm.setPreferredName(String.valueOf(vm.getPublicId()));  //JR417 new
    					vm.setPreferredDefinition(rs.getString("PREFERRED_DEFINITION"));
    					vm.setLongName(rs.getString("LONG_NAME"));
    					vm.setDescription(rs.getString("DESCRIPTION").trim());
    					vm.setIdseq(rs.getString("VM_IDSEQ"));
    					
    					pv.setValueMeaningV2(vm);
    					
    					if (!pvsMap.containsKey(vdSeqId)) 
    						pvsMap.put(vdSeqId, new ArrayList<PermissibleValueV2TransferObject>());
    						
    					pvsMap.get(vdSeqId).add(pv);    					

    					return pv;
    				}
    			});
    	

    	return pvsMap;
    }


	public List<String> getDefinitionTextByVMId(String vmSeqId) {
		
		String sql = 
    			"select * from  CABIO31_DEFINITIONS_VIEW def where def.AC_IDSEQ =:vmSeqId";

    	MapSqlParameterSource params = new MapSqlParameterSource();
    	params.addValue("vmSeqId", vmSeqId);
    	
    	final HashMap<String, List<PermissibleValueV2TransferObject>> pvsMap = 
    			new HashMap<String, List<PermissibleValueV2TransferObject>>();
    	
    	List<String> defTexts = 
    			this.namedParameterJdbcTemplate.query(sql, params, 
    					new RowMapper<String>() {
    				public String mapRow(ResultSet rs, int rowNum) throws SQLException {
    					
    					 return rs.getString("DEFINITION");
    				}
    			});
    	

    	return defTexts;
    }
	
public List<String> getDesignationNamesByVMId(String vmSeqId) {
		
	String sql = 
    			"select * from  CABIO31_DESIGNATIONS_VIEW def where def.AC_IDSEQ =:vmSeqId";

    	MapSqlParameterSource params = new MapSqlParameterSource();
    	params.addValue("vmSeqId", vmSeqId);
    	
    	final HashMap<String, List<PermissibleValueV2TransferObject>> pvsMap = 
    			new HashMap<String, List<PermissibleValueV2TransferObject>>();
    	
    	List<String> desNames = 
    			this.namedParameterJdbcTemplate.query(sql, params, 
    					new RowMapper<String>() {
    				public String mapRow(ResultSet rs, int rowNum) throws SQLException {
    					
    					 return rs.getString("NAME");
    				}
    			});
    	

    	return desNames;
    }

}
