package gov.nih.nci.cadsr.parser.helper;

import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class ParserHelper {
	
	/**
	 * Creates an index of the Columns that enables fetching values for any given field (column)
	 *
	 * The resulting map will have the field name (String) and its respective Column Index (Integer) 
	 * from an excel document.
	 *  
	 * @param sheet
	 * @return Map<String, Integer>
	 */
	@SuppressWarnings("unused")
	private static Map<String, Integer> createColIndex (Sheet sheet) {
		Row row = sheet.getRow(0);
		Map<String, Integer> colIdcs = new HashMap<String, Integer>(); 
	        for (Cell cell : row) {
	        	colIdcs.put(cell.getStringCellValue(), cell.getColumnIndex());
	        }
	        return colIdcs;
	}
	
}
