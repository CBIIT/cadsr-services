// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   DBUtil.java

package gov.nih.nci.ncicb.cadsr.common.util;

import gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc.util.DataSourceUtil;
import gov.nih.nci.ncicb.cadsr.common.persistence.jdbc.spring.OracleJBossNativeJdbcExtractor;
import gov.nih.nci.ncicb.cadsr.common.servicelocator.ObjectLocator;
import gov.nih.nci.ncicb.cadsr.common.servicelocator.ServiceLocator;
import gov.nih.nci.ncicb.cadsr.common.servicelocator.spring.SpringObjectLocatorImpl;
import gov.nih.nci.ncicb.cadsr.common.util.logging.Log;
import gov.nih.nci.ncicb.cadsr.common.util.logging.LogFactory;
import gov.nih.nci.ncicb.cadsr.formbuilder.ejb.service.FormBuilderService;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.Vector;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.support.WebApplicationContextUtils;

/*import oracle.cle.persistence.ConnectionManager;
import oracle.cle.persistence.ConnectionProvider;*/
import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;

public class DBUtil
{
	private static Log log = LogFactory.getLog(DBUtil.class.getName());
	public static final String CDEBROWSER_PROVIDER = "cdebrowser_bc4j";
	private Connection conn;
	private boolean isConnected;
	private boolean isOracleConnection;

	public DBUtil()
	{
		isConnected = false;
		isOracleConnection = false;
		conn = null;
	}

/*	public DataSource getDataSource(HttpServletRequest request)
	{
		ApplicationContext context =  WebApplicationContextUtils.getWebApplicationContext(request.getSession().getServletContext());
		DataSource ds = (DataSource) context.getBean("dataSource");

		if(log.isDebugEnabled())
			log.debug((new StringBuilder()).append("Return DataSource =  ").append(ds).toString());
		return ds;
	}

	public boolean getConnectionFromContainer(HttpServletRequest request) throws Exception
	{
		if(!isConnected)
			try
		{
				DataSource ds = getDataSource(request);
				conn = ds.getConnection();
				isConnected = true;
				isOracleConnection = false;
				log.info((new StringBuilder()).append("Connected to the database successfully using datasource ").append(ds.toString()).toString());
		}
		catch(Exception e)
		{
			log.error("Exception occurred in getConnectionFromContainer", e);
			throw new Exception("Exception in getConnectionFromContainer() ");
		}
		return isConnected;
	} */


	/*public boolean getOracleConnectionFromContainer()
        throws Exception
    {
        if(isConnected && !isOracleConnection)
            returnConnection();
        if(!isConnected)
            try
            {
                ConnectionManager manager = ConnectionManager.getInstance();
                ConnectionProvider provider = manager.getProvider("cdebrowser_bc4j");
                DataSource ds = DataSourceUtil.getOracleDataSource(provider.getConnectionString(), provider.getUserName(), provider.getPassword());
                conn = ds.getConnection();
                isConnected = true;
                isOracleConnection = true;
                log.info("Connected to the database successfully using datasource ");
            }
            catch(Exception e)
            {
                log.error("Exception occurred in getConnectionFromContainer", e);
                throw new Exception("Exception in getConnectionFromContainer() ");
            }
        return isConnected;
    }*/

	public Vector retrieveMultipleRecordsDB(String sqlStmt)
			throws SQLException
	{
		Vector dataToReturn;
		Statement stmt;
		ResultSet rs;
		//Vector rowData = null;
		dataToReturn = null;
		stmt = null;
		rs = null;
		//boolean isThereResult = false;
		try
		{
			dataToReturn = new Vector();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlStmt);
			int columnCount = rs.getMetaData().getColumnCount();
			for(boolean isThereResult = rs.next(); isThereResult; isThereResult = rs.next())
			{
				Vector rowData = new Vector();
				for(int i = 0; i < columnCount; i++)
					rowData.addElement(rs.getString(i + 1));

				dataToReturn.addElement(rowData);
			}

		}
		catch(SQLException sqle)
		{
			log.error("Exception in DBUtil.retrieveMultipleRecordsDB(String )");
			log.error((new StringBuilder()).append("The statement executed : ").append(sqlStmt).toString(), sqle);
			throw sqle;
		}
		if(rs != null)
		{
			rs.close();
			rs = null;
		}
		if(stmt != null)
		{
			stmt.close();
			stmt = null;
		}
		return dataToReturn;
	}

	public Vector retrieveMultipleRecordsDB(String tableName, String tableFields[], String whereClause)
			throws SQLException
	{
		Vector dataToReturn;
		Statement stmt;
		ResultSet rs;
		String stmt_str;
		//Vector rowData = null;
		dataToReturn = null;
		stmt = null;
		rs = null;
		//boolean isThereResult = false;
		stmt_str = null;
		try
		{
			dataToReturn = new Vector();
			stmt = conn.createStatement();
			stmt_str = "select ";
			for(int i = 0; i < tableFields.length; i++)
				if(i < tableFields.length - 1)
					stmt_str = (new StringBuilder()).append(stmt_str).append(tableFields[i]).append(", ").toString();
				else
					stmt_str = (new StringBuilder()).append(stmt_str).append(tableFields[i]).append(" from ").append(tableName).append(" ").append(whereClause).toString();

			log.debug((new StringBuilder()).append("statement").append(stmt_str).toString());
			rs = stmt.executeQuery(stmt_str);
			for(boolean isThereResult = rs.next(); isThereResult; isThereResult = rs.next())
			{
				Vector rowData = new Vector();
				for(int i = 0; i < tableFields.length; i++)
					rowData.addElement(rs.getString(i + 1));

				dataToReturn.addElement(rowData);
			}

		}
		catch(SQLException sqle)
		{
			log.error("  Exception in DBUtil.retrieveMultipleRecordsDB()");
			log.error((new StringBuilder()).append("  The statement executed : ").append(stmt_str).toString(), sqle);
			throw sqle;
		}
		if(rs != null)
		{
			rs.close();
			rs = null;
		}
		if(stmt != null)
		{
			stmt.close();
			stmt = null;
		}
		return dataToReturn;
	}

	public Vector retrieveRecordDB(String sqlStmt)
			throws SQLException
	{
		Vector dataToReturn;
		Statement stmt;
		ResultSet rs;
		Vector rowData = null;
		dataToReturn = null;
		stmt = null;
		rs = null;
		//boolean isThereResult = false;
		try
		{
			dataToReturn = new Vector();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlStmt);
			int columnCount = rs.getMetaData().getColumnCount();
			boolean isThereResult = rs.next();
			if(isThereResult)
			{
				for(int i = 0; i < columnCount; i++)
					dataToReturn.addElement(rs.getString(i + 1));

			}
		}
		catch(SQLException sqle)
		{
			log.error("  Exception in DBUtil.retrieveRecordDB(String)");
			log.error((new StringBuilder()).append("  The statement executed : ").append(sqlStmt).toString(), sqle);
			throw sqle;
		}
		if(rs != null)
		{
			rs.close();
			rs = null;
		}
		if(stmt != null)
		{
			stmt.close();
			stmt = null;
		}
		return dataToReturn;
	}

	public Vector retrieveRecordDB(String tableName, String tableFields[], String whereClause)
			throws SQLException
	{
		Vector dataToReturn;
		Statement stmt;
		ResultSet rs;
		String stmt_str;
		Vector rowData = null;
		dataToReturn = null;
		stmt = null;
		rs = null;
		//boolean isThereResult = false;
		stmt_str = null;
		try
		{
			dataToReturn = new Vector();
			stmt = conn.createStatement();
			stmt_str = "select ";
			for(int i = 0; i < tableFields.length; i++)
				if(i < tableFields.length - 1)
					stmt_str = (new StringBuilder()).append(stmt_str).append(tableFields[i]).append(", ").toString();
				else
					stmt_str = (new StringBuilder()).append(stmt_str).append(tableFields[i]).append(" from ").append(tableName).append(" ").append(whereClause).toString();

			rs = stmt.executeQuery(stmt_str);
			boolean isThereResult = rs.next();
			if(isThereResult)
			{
				for(int i = 0; i < tableFields.length; i++)
					dataToReturn.addElement(rs.getObject(i + 1));

			}
		}
		catch(SQLException sqle)
		{
			log.error("  Exception in DBUtil.retrieveRecordDB()");
			log.error((new StringBuilder()).append("  The statement executed : ").append(stmt_str).toString(), sqle);
			throw sqle;
		}
		if(rs != null)
		{
			rs.close();
			rs = null;
		}
		if(stmt != null)
		{
			stmt.close();
			stmt = null;
		}
		return dataToReturn;
	}

	public String getUniqueId(String idGenerator)
			throws SQLException
	{
		String id;
		Statement stmt;
		ResultSet rs;
		id = null;
		stmt = null;
		rs = null;
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery((new StringBuilder()).append("SELECT ").append(idGenerator).append(" FROM DUAL").toString());
			rs.next();
			id = rs.getString(1);
		}
		catch(SQLException sqle)
		{
			log.error("Exception in getUniqueId()", sqle);
			throw sqle;
		}
		if(rs != null)
		{
			rs.close();
			rs = null;
		}
		if(stmt != null)
		{
			stmt.close();
			stmt = null;
		}
		return id;
	}

	public static String getUniqueId(Connection con, String idGenerator)
			throws SQLException
	{
		String id;
		Statement stmt;
		ResultSet rs;
		id = null;
		stmt = null;
		rs = null;
		try
		{
			stmt = con.createStatement();
			rs = stmt.executeQuery((new StringBuilder()).append("SELECT ").append(idGenerator).append(" FROM DUAL").toString());
			rs.next();
			id = rs.getString(1);
		}
		catch(SQLException sqle)
		{
			log.error("Exception in getUniqueId()", sqle);
			throw sqle;
		}
		if(rs != null)
		{
			rs.close();
			rs = null;
		}
		if(stmt != null)
		{
			stmt.close();
			stmt = null;
		}
		return id;
	}

	public void returnConnection()
			throws SQLException
	{
		try
		{
			if(conn != null)
			{
				conn.close();
				isConnected = false;
			}
		}
		catch(SQLException sqle) { }
	}

	public Connection getConnection()
	{
		return conn;
	}

	public ResultSet executeQuery(String sqlStmt)
			throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = conn.createStatement(1004, 1007);
			rs = stmt.executeQuery(sqlStmt);
		}
		catch(SQLException ex)
		{
			log.error((new StringBuilder()).append("Exception occurred in executeQuery ").append(sqlStmt).toString(), ex);
			throw ex;
		}
		return rs;
	}

	private OracleConnection createOracleConnection(String dbURL, String username, String password)
			throws Exception
	{
		OracleDataSource ds = DataSourceUtil.getOracleDataSource(dbURL, username, password);
		return (OracleConnection)ds.getConnection();
	}

	public static OracleConnection extractOracleConnection(Connection conn)
			throws Exception
	{
		OracleJBossNativeJdbcExtractor extractor = new OracleJBossNativeJdbcExtractor();
		return extractor.doGetOracleConnection(conn);
	}

}