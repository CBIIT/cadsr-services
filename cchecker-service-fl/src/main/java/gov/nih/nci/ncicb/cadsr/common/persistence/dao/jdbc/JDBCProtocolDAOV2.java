package gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc;

import gov.nih.nci.ncicb.cadsr.common.dto.ContextTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.ProtocolTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.bc4j.BC4JClassificationsTransferObject;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.ProtocolDAO;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc.JDBCClassificationSchemeDAOV2.ClassificationByNameQuery;
import gov.nih.nci.ncicb.cadsr.common.resource.Classification;
import gov.nih.nci.ncicb.cadsr.common.resource.Context;
import gov.nih.nci.ncicb.cadsr.common.resource.Protocol;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.object.MappingSqlQuery;


public class JDBCProtocolDAOV2 extends JDBCBaseDAOV2 implements ProtocolDAO {
	public JDBCProtocolDAOV2(DataSource dataSource) {
		super(dataSource);
	}

  /**
   * Gets a protocol by the primary key.
   *
   * @return a protocol.
   */
  public Protocol getProtocolByPK(String idseq) {
      ProtocolQuery query = new ProtocolQuery();
      query.setDataSource(getDataSource());
      query.setSql();
      List protocols = query.execute(idseq);
      if (protocols==null || protocols.isEmpty()){
          return null;
      }else{
        return (Protocol)protocols.get(0);
      }  
  }
  
  /**
   * Gets the Classification
   *
   * @return <b>Collection</b> Collection of ContextTransferObjects
   */
  public Protocol getProtocolByName(String longName) {
  	ProtocolByNameQuery query = new ProtocolByNameQuery();
     query.setDataSource(getDataSource());
     query.setSql(longName);
     List result = (List) query.execute();
     Protocol protocol = null;
     if (result.size() != 0) {
    	 protocol = (Protocol) (query.execute().get(0));
     }

     return protocol;
  }
  /**
   * Inner class that accesses database to get a protocol by primary key
   *
   */
    class ProtocolQuery extends MappingSqlQuery {
    
    String sql = " SELECT p.proto_idseq, p.version, p.conte_idseq, p.preferred_name, p.preferred_definition, p.asl_name, p.long_name, p.LATEST_VERSION_IND, p.begin_date, p.END_DATE, p.PROTOCOL_ID, p.TYPE, p.PHASE, p.LEAD_ORG, p.origin, p.PROTO_ID, c.name contextname " + 
                 " from sbrext.protocols_view_ext p, sbr.contexts_view c where p.PROTO_IDSEQ=? and  p.CONTE_IDSEQ = c.CONTE_IDSEQ";

    
    ProtocolQuery(){
      super();
    }

    public void setSql(){
      super.setSql(sql);
      declareParameter(new SqlParameter("PROTO_IDSEQ", Types.VARCHAR));      
      compile();
    }
    protected Object mapRow(ResultSet rs, int rownum) throws SQLException {
        Protocol protocol = new ProtocolTransferObject();
        protocol.setProtoIdseq(rs.getString(1));
        protocol.setVersion(rs.getFloat(2)); 
        protocol.setConteIdseq(rs.getString(3)); 
        protocol.setPreferredName(rs.getString(4));
        protocol.setPreferredDefinition(rs.getString(5));
        protocol.setAslName(rs.getString(6));
        protocol.setLongName(rs.getString(7));
        protocol.setLatestVersionInd(rs.getString(8));
        protocol.setBeginDate(rs.getDate(9));
        protocol.setEndDate(rs.getDate(10));
        protocol.setProtocolId(rs.getString(11));
        protocol.setType(rs.getString(12));
        protocol.setPhase(rs.getString(13));
        protocol.setLeadOrg(rs.getString(14));
        protocol.setOrigin(rs.getString(15));
        Float publicId = rs.getFloat(16);
        protocol.setPublicId(publicId.intValue());
        String contextName = rs.getString(17);
        Context context = new ContextTransferObject();
        context.setConteIdseq(rs.getString(3));
        context.setName(contextName);
        protocol.setContext(context);
      return protocol;
    }
  }
    
    /**
     * Inner class that accesses database to get all the contexts in caDSR
     *
     */
    class ProtocolByNameQuery extends MappingSqlQuery {

      public ProtocolByNameQuery(){
        super();
      }

      public void setSql(String longName){
        super.setSql("SELECT proto_idseq from sbrext.protocols_view_ext where upper(long_name)  = '" + longName.toUpperCase() + "'");
       }
     /**
      * 
      */
      protected Object mapRow(ResultSet rs, int rownum) throws SQLException {

    	  Protocol protocol = new ProtocolTransferObject();
    	  protocol.setProtoIdseq(rs.getString(1));
    	  return protocol;
      }

    }  

    public String getProtocolSeqidByPreferredName(String shortName, String contextseqid) {
    	
    	String sql = 
    			"select proto_idseq, protocol_Id, conte_idseq, long_name from sbrext.protocols_view_ext pv " +
    					" where PV.PREFERRED_NAME=:shortName and conte_idseq=:contextseqid";

    	MapSqlParameterSource params = new MapSqlParameterSource();
    	params.addValue("shortName", shortName);
    	params.addValue("contextseqid", contextseqid);

    	List<String> des = 
    			this.namedParameterJdbcTemplate.query(sql, params, 
    					new RowMapper<String>() {
    				public String mapRow(ResultSet rs, int rowNum) throws SQLException {
    					
    					return rs.getString("proto_idseq");
    				}
    			});

    	return (des == null || des.size() == 0) ? "" : des.get(0);

    }
    
    public List<String> getProtocolIdseqByPreferredName(String shortName) {
    	
    	String sql = 
    			"select proto_idseq, protocol_Id, conte_idseq, long_name from sbrext.protocols_view_ext pv " +
    					" where PV.PREFERRED_NAME=:shortName";

    	MapSqlParameterSource params = new MapSqlParameterSource();
    	params.addValue("shortName", shortName);

    	List<String> des = 
    			this.namedParameterJdbcTemplate.query(sql, params, 
    					new RowMapper<String>() {
    				public String mapRow(ResultSet rs, int rowNum) throws SQLException {
    					
    					return rs.getString("proto_idseq");
    				}
    			});

    	return des;

    }

}
