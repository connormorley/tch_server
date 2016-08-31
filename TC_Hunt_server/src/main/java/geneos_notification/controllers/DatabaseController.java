package geneos_notification.controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import geneos_notification.loggers.LogObject;
import geneos_notification.loggers.LtA;

public class DatabaseController {
	
  private static String address = null;
  private static Connection conn = null;
  private static Statement stmt = null;
  private static ResultSet res = null;
  private static ResultSet res1 = null;
  public static Random random = new Random(System.currentTimeMillis());
  static LtA logA = new LogObject();

///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////
  
  public static void SQLConnect(){
    try {
      // This will load the MySQL driver, each DB has its own driver
      Class.forName("com.mysql.jdbc.Driver");
      // Setup the connection with the DB
      conn = DriverManager
          .getConnection(address);
    }
    catch(Exception e)
    {
    	logA.doLog("SQL" , "[SQL]Connection information issue, either driver or address : " + e.toString(), "Critical");
        //System.out.println(e);
        throw new RuntimeException();
    }
  }

///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

public static void execCustom(String query) {
	SQLConnect();
	try {
		stmt = conn.createStatement();
		stmt.executeUpdate(query);
	} catch (SQLException e) {
		logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(), "Critical");
		e.printStackTrace();
		close();
		throw new RuntimeException(e);
	} 
		close();
	

}

///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

  public static void close() {
    try {
      if (res != null) {
        res.close();
      }

      if (stmt != null) {
        stmt.close();
      }

      if (conn != null) {
        conn.close();
      }
    } catch (Exception e) {
    	logA.doLog("SQL" , "[SQL]SQL connection has failed to close! \nError is : " + e.toString(), "Critical");

    }
  }
  
  
  
  public static void setAddress(String submittedAddress)
  {
	  address = submittedAddress;
  }
  
  

} 
