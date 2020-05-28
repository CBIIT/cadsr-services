package gov.nih.nci.cadsr.data;

import java.util.ArrayList;

public class XmlRow {
    public ArrayList<String> cellList = new ArrayList<>();

    @Override
    public String toString() {
        return cellList.toString();
    }
}
