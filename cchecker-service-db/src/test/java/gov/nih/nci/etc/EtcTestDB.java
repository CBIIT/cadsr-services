package gov.nih.nci.etc;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.springframework.web.client.RestTemplate;

import gov.nih.nci.cadsr.data.ALSData;
//from   w  w  w  .  ja v a  2 s .com
public class EtcTestDB {
  public static void main(String[] args) throws Exception {
    Connection conn = getConnection();
    Statement st = conn.createStatement();
    
    
    File file = new File("myimage.gif");
    FileInputStream fis = new FileInputStream(file);
    PreparedStatement ps = conn.prepareStatement("insert into images values (?,?)");
    ps.setString(1, "10");
    ps.setBinaryStream(2, fis);
    ps.executeUpdate();
    
    
    retrieveAlsData();
    ResultSet rset = st.executeQuery("select b from images");
    InputStream stream = rset.getBinaryStream(1);
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    int a1 = stream.read();
    while (a1 >= 0) {
      output.write((char) a1);
      a1 = stream.read();
    }
    Image myImage = Toolkit.getDefaultToolkit().createImage(output.toByteArray());
    output.close();
    
    
    ps.close();
    
    fis.close();
    st.close();
    conn.close();
  }
  private static Connection getConnection() throws Exception {
    Class.forName("oracle.jdbc.OracleDriver");
    String url = "jdbc:oracle:thin:@ncidb-d110-d.nci.nih.gov:1551:DSRDEV";

    return DriverManager.getConnection(url, "SBR", "sBeNrW65dX");
  }
  private static ALSData retrieveAlsData() {
      RestTemplate restTemplate = new RestTemplate();
      ALSData alsData = restTemplate.getForObject("http://localhost:4801/rest/alsparserservice", ALSData.class);
      return alsData;
//https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html
  }
}