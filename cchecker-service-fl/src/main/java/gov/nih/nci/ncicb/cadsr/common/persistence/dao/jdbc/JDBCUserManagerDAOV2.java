package gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc;

import gov.nih.nci.ncicb.cadsr.common.dto.ContextTransferObject;
import gov.nih.nci.ncicb.cadsr.common.dto.NCIUserTransferObject;
import gov.nih.nci.ncicb.cadsr.common.persistence.dao.UserManagerDAO;
import gov.nih.nci.ncicb.cadsr.common.resource.Context;
import gov.nih.nci.ncicb.cadsr.common.resource.NCIUser;
import gov.nih.nci.ncicb.cadsr.common.util.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.StoredProcedure;


public class JDBCUserManagerDAOV2 extends JDBCBaseDAOV2 implements UserManagerDAO {
 
	private static Logger logger = Logger.getLogger(JDBCUserManagerDAOV2.class.getName());
 
  public JDBCUserManagerDAOV2(DataSource dataSource) {
		super(dataSource);
	}
  
  public boolean userInGroup(
    String username,
    String groupName) {
    UserGroupQuery query = new UserGroupQuery(this.dataSource);
    String[] params = new String[2];
    params[0] = username;
    params[1] = groupName;

    Integer result = (Integer) query.findObject(params);

    if (result.intValue() < 1) {
      return false;
    }
    else {
      return true;
    }
  }
  /**
   * The retun map has key value as action:map
   * Each value itself is map with key value formtype:Map
   * Each e value it
   * @return 
   */
  public Map getPermissionsByAction()
  {
      return null;
  }  

  public boolean validUser(
    String userName,
    String password) {
    boolean validUser = false;
    Connection conn = null;
    try {
      conn = this.dataSource.getConnection(userName, password);
      
      // The Query below is to make sure connection db is established
      // In some case it was noticed that just making the connection did
      // not make a call to the db
      
      conn.getMetaData();
      UserEnabledQuery enabledQuery = new UserEnabledQuery((this.dataSource));
      //Check if the user account is enabled
      validUser = ((Boolean)enabledQuery.findObject(userName)).booleanValue();
    }
    catch (SQLException e) {
      validUser = false;
        try {
		  if(this.dataSource != null && this.dataSource.getConnection() != null) { 
				System.out.println("JDBCUserManagerDAOV2.java#validUser error: user [" + userName + "] jdbcurl [" + this.dataSource.getConnection().toString() + "]");
		  } else {
			  System.out.println("JDBCUserManagerDAOV2.java#validUser dataSource or connection is null!");
		  }
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
	  
    return validUser;
  }

  public NCIUser getNCIUser(String username) {
    UserQuery userQuery = new UserQuery(this.dataSource);
    NCIUser user = (NCIUser) userQuery.findObject(username);
    if(user!=null)
      user.setContextsByRole(this.getContextsForAllRoles(user.getUsername(),"QUEST_CONTENT"));
    return user;
  }

  public Map getContextsForAllRoles(
    String username,
    String acType) {
    Map out = new HashMap();
    ContextsQuery qry = new ContextsQuery(this.dataSource);
    out = qry.getContexts(username, acType);

    return out;
  }

  public static void main(String[] args) {
    //ServiceLocator locator = new SimpleServiceLocator();
    //JDBCDAOFactory factory =
   //   (JDBCDAOFactory) new JDBCDAOFactory().getDAOFactory(locator);
    //UserManagerDAO dao = factory.getUserManagerDAO();

    //NCIUser user = (NCIUser) dao.getNCIUser("sbrext");
    //System.out.println(user);
    //System.out.println(dao.validUser("jasur","jasur"));
    /**Map result = dao.getContextsForAllRoles("SBREXT", "QUEST_CONTENT");
    Set keys = result.keySet();
    Iterator it = keys.iterator();

    while (it.hasNext()) {
      String role = (String) it.next();
      List l = (List) result.get(role);
      System.out.println("Role: " + role + " contexts count: " + l.size());
      Iterator iter = l.iterator();
      while (iter.hasNext()) {
        System.out.println("Context Name: "+((Context)iter.next()).getName());
      }
    }**/
  }

  // inner class
  class UserGroupQuery extends MappingSqlQuery {
    UserGroupQuery(DataSource ds) {
      super(
        ds,
        "SELECT COUNT(*) from USER_BR_VIEW_EXT where UA_NAME=UPPER(?) AND BRL_NAME=UPPER(?)");
      declareParameter(new SqlParameter("UA_NAME", Types.VARCHAR));
      declareParameter(new SqlParameter("BRL_NAME", Types.VARCHAR));
      compile();
    }

    protected Object mapRow(
      ResultSet rs,
      int rownum) throws SQLException {
      return new Integer(rs.getInt(1));
    }
  }

  // inner class end    
  // inner class
  class UserQuery extends MappingSqlQuery {
    UserQuery(DataSource ds) {
      super(ds, "SELECT *  from USER_ACCOUNTS_VIEW where UA_NAME=UPPER(?)");
      declareParameter(new SqlParameter("UA_NAME", Types.VARCHAR));
      compile();
    }

    protected Object mapRow(
      ResultSet rs,
      int rownum) throws SQLException {
      NCIUserTransferObject user =
        new NCIUserTransferObject(rs.getString("UA_NAME"));
      
      //added for GF1224 - lock the form
      user.setEmailAddress(rs.getString("electronic_mail_address"));
      user.setPhoneNumber(rs.getString("PHONE_NUMBER"));
      return user;
    }
  }
  //Check if the user is enable
  class UserEnabledQuery extends MappingSqlQuery {
    UserEnabledQuery(DataSource ds) {
      super(ds, "SELECT ENABLED_IND  from USER_ACCOUNTS_VIEW where UA_NAME like UPPER(?)");
      declareParameter(new SqlParameter("UA_NAME", Types.VARCHAR));
      compile();
    }

    protected Object mapRow(
      ResultSet rs,
      int rownum) throws SQLException {

      return new Boolean(StringUtils.toBoolean((String)rs.getString(1)));
    }
  }  

  // inner class end    
  private class ContextsQuery extends StoredProcedure {
    Map out = new HashMap();
    List results = new ArrayList();

    public ContextsQuery(DataSource ds) {
      super(ds, "cadsr_security_util.get_contexts_list");
      declareParameter(new SqlParameter("p_ua_name", Types.VARCHAR));
      declareParameter(new SqlParameter("p_actl_name", Types.VARCHAR));
      declareParameter(
        new SqlOutParameter(
          "p_contexts", oracle.jdbc.OracleTypes.CURSOR,
          new RowCallbackHandlerImpl()));
      compile();
    }

    public Map getContexts(
      String username,
      String acType) {
      Map in = new HashMap();
      in.put("p_ua_name", username);
      in.put("p_actl_name", acType);
      execute(in);

      Iterator it = results.iterator();
      Map mp = new HashMap();
      String roleName;
      Context conte;

      while (it.hasNext()) {
        mp = (Map) it.next();
        roleName = (String) mp.get("BusinessRole");
        conte = new ContextTransferObject();
        conte.setConteIdseq((String) mp.get("ConteIdseq"));
        conte.setName((String) mp.get("ConteName"));
        if (out.containsKey(roleName)) {
          List availList = (List) out.get(roleName);
          //availList.add((String) mp.get("ConteIdseq"));
          availList.add(conte);
        }
        else {
          List contextsList = new ArrayList();
          //contextsList.add((String) mp.get("ConteIdseq"));
          contextsList.add(conte);
          out.put(roleName, contextsList);
        }
      }

      return out;
    }

    private class RowCallbackHandlerImpl implements RowCallbackHandler {
      public void processRow(ResultSet rs) throws SQLException {
        Map row = new HashMap();
        row.put("ConteIdseq", (String) rs.getString(1));
        row.put("ConteName", (String) rs.getString(2));
        row.put("BusinessRole", (String) rs.getString(3));
        results.add(row);
      }
    }
  }
}
