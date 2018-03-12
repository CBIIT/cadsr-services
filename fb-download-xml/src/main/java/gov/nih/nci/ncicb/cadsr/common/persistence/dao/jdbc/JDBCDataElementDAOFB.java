package gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc;

import gov.nih.nci.ncicb.cadsr.common.dto.ValidValueTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ValueMeaningTransferObject;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.DataElementDAO;
import gov.nih.nci.ncicb.cadsr.common.resource.ValidValue;
import gov.nih.nci.ncicb.cadsr.common.resource.ValueMeaning;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component("dataElementDAO")
public class JDBCDataElementDAOFB extends JDBCAdminComponentDAOFB implements DataElementDAO
{

	public JDBCDataElementDAOFB() {
		super();
	}

	public Map<String, ValidValue> getPermissibleValues(Collection<String> deIdSeqs) {
		if (deIdSeqs == null || deIdSeqs.size() < 1) {
			return new HashMap<String, ValidValue>();
		}
		JdbcTemplate jdbcTemplate = new JdbcTemplate(getDataSource());
		String qry = "select a.*, c.DE_IDSEQ as cdeIdSeq, sbrext_common_routines.return_number(a.VALUE) display_order from PERMISSIBLE_VALUES_VIEW a, VD_PVS_VIEW b, DATA_ELEMENTS_VIEW c " +
				"where a.PV_IDSEQ=b.PV_IDSEQ and b.VD_IDSEQ=c.VD_IDSEQ and c.de_idseq in ( ";
		for (String deIdSeq: deIdSeqs) {
			qry += "'"+deIdSeq+"',";
		}
		if (qry.lastIndexOf(",") == -1) {
			qry += "'')";
		}
		else {
			qry = qry.substring(0, qry.length() - 1)+")";
		}

		qry += " order by display_order, upper(a.VALUE)";

		Map<String, ValidValue> pvMap  = (Map<String, ValidValue>)jdbcTemplate.query(qry, new ResultSetExtractor() {
			public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
				final Map<String, List<ValidValue>> pvMap = new HashMap<String, List<ValidValue>>();
				Map<String, List<ValidValue>> vmIdMap = new HashMap<String, List<ValidValue>>();
				while (rs.next()) {
					ValidValue vv = new ValidValueTransferObject();
					vv.setShortMeaningValue(rs.getString(2));
					vv.setShortMeaning(rs.getString(2));
					vv.setShortMeaningDescription(rs.getString(4));
					vv.setBeginDate(rs.getString(5));

					String cdeIdSeq = rs.getString("cdeIdSeq");
					List<ValidValue> vvList = null;

					if (pvMap.get(cdeIdSeq) == null) {
						vvList = new ArrayList<ValidValue>();
						pvMap.put(cdeIdSeq, vvList);
					}
					else {
						vvList = pvMap.get(cdeIdSeq);
					}
					vvList.add(vv);
					pvMap.put(cdeIdSeq, vvList);

					String vmIdSeq = rs.getString(13);

					List<ValidValue> vmVV = null;
					if (vmIdMap.get(vmIdSeq) == null) {
						vmVV = new ArrayList<ValidValue>();
						vmIdMap.put(vmIdSeq, vmVV);
					}
					else {
						vmVV = vmIdMap.get(vmIdSeq);
					}
					vmVV.add(vv);
				}

				Map<String, ValueMeaning> vms = getValueMeanings(vmIdMap.keySet());

				for (String vmId: vmIdMap.keySet()) {
					ValueMeaning vm = vms.get(vmId);
					if (vm != null) {
						for (ValidValue valVal: vmIdMap.get(vmId)) {
							valVal.setValueMeaning(vm);
						}
					}
				}
				return pvMap;
			}
		});

		return pvMap;
	}

	public Map<String, ValueMeaning> getValueMeanings(Collection<String> vmIds) {
		if (vmIds == null || vmIds.size() < 1) {
			return new HashMap<String, ValueMeaning>();
		}
		String qry = "select * from VALUE_MEANINGS_VIEW where VM_IDSEQ in ( ";
		for (String vmId: vmIds) {
			qry += "'"+vmId+"',";
		}
		qry = qry.substring(0, qry.length()-1)+")";

		JdbcTemplate jdbcTemplate = new JdbcTemplate(getDataSource());
		Map<String, ValueMeaning> vmMap = (Map<String, ValueMeaning>)jdbcTemplate.query(qry, new ResultSetExtractor() {
			public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
				Map<String, ValueMeaning> vmMap = new HashMap<String, ValueMeaning>();
				while (rs.next()) {
					ValueMeaning vm = new ValueMeaningTransferObject();
					vm.setPreferredName(rs.getString(1));
					vm.setPreferredDefinition(rs.getString(2));
					String vmIdSeq = rs.getString(11);
					vm.setLongName(rs.getString(14));
					vm.setDefinitions(getDefinitions(vmIdSeq));
					vm.setDesignations(getDesignations(vmIdSeq, null));

					vmMap.put(vmIdSeq, vm);
				}
				return vmMap;
			}
		});

		return vmMap;
	}

	public boolean isDEDerived(final String deIdSeq) {

		final String qry = "select * from complex_data_elements_view where p_de_idseq = ?";

		JdbcTemplate jdbcTemplate = new JdbcTemplate(getDataSource());
		Object result = jdbcTemplate.query(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement(qry);
				ps.setString(1, deIdSeq);
				return ps;
			}
		}, new ResultSetExtractor() {
			public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
				if (rs.next()) {
					return new Boolean(true);
				}
				else {
					return new Boolean(false);
				}
			}
		});
		return (Boolean)result;
	}
}
