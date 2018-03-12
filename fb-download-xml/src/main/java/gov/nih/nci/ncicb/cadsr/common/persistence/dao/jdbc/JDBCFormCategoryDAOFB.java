package gov.nih.nci.ncicb.cadsr.common.persistence.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.stereotype.Component;

import gov.nih.nci.ncicb.cadsr.common.persistence.dao.FormCategoryDAO;

@Component("formCategoryDAO")
public class JDBCFormCategoryDAOFB extends JDBCBaseDAOFB implements FormCategoryDAO
{
	public JDBCFormCategoryDAOFB() {
		super();
	}

	/**
	 * Gets all the form/template categories. This info is maintained in
	 * Table:QC_DISPLAY_LOV_EXT  Column:QCDL_NAME
	 *
	 * @return <b>Collection</b> Collection of categories (Strings)
	 */
	public Collection getAllCategories() {
		Collection col = new ArrayList();
		CategoryQuery query = new CategoryQuery();
		query.setDataSource(getDataSource());
		query.setSql();

		return query.execute(); // retrieves all records
	}

	public static void main(String[] args) {

		JDBCFormCategoryDAOFB test = new JDBCFormCategoryDAOFB();

		Collection coll = test.getAllCategories();

		System.out.println(coll);
		/*
    for (Iterator it = coll.iterator(); it.hasNext();) {
      Object anObject = it.next();
    }
		 */
	}

	/**
	 * Inner class that accesses database to get all the form/template
	 * categories.
	 */
	class CategoryQuery extends MappingSqlQuery {
		CategoryQuery() {
			super();
		}

		public void setSql() {
			super.setSql("select QCDL_NAME from SBREXT.QC_DISPLAY_LOV_VIEW_EXT order by upper(QCDL_NAME)");
		}

		protected Object mapRow(
				ResultSet rs,
				int rownum) throws SQLException {
			// handles only one row
			return rs.getString("QCDL_NAME");
		}
	}
}
