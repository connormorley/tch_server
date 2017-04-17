package tcrunch.controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import tcrunch.loggers.LogObject;
import tcrunch.loggers.LtA;
import tcrunch.threads.HealthCheck;

/*	Created by:		Connor Morley
 * 	Title:			TCrunch Server Database Interface Controller
 *  Version update:	2.3
 *  Notes:			Class is responsible for all interactions between the server application and the established MySQL database. 
 *  
 *  References:		N/A
 */

public class DatabaseController {
	
  private static String address = null;
  public static Random random = new Random(System.currentTimeMillis());
  static LtA logA = new LogObject();
  
  public static Connection SQLConnect(){
    try {
      Class.forName("com.mysql.jdbc.Driver");
      return DriverManager
          .getConnection(address);
    }
    catch(Exception e)
    {
    	logA.doLog("SQL" , "[SQL]Connection information issue, either driver or address : " + e.toString(), "Critical");
        throw new RuntimeException();
    }
  }

public static void execCustom(String query) {
	Connection conn = SQLConnect();
	try {
		Statement stmt = null;
		stmt = conn.createStatement();
		stmt.executeUpdate(query);
	} catch (SQLException e) {
		logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(), "Critical");
		e.printStackTrace();
		close(conn);
		throw new RuntimeException(e);
	} 
		close(conn);
	

}

	public static boolean checkRecoveredAttack() {
		Connection conn = SQLConnect();
		try {
			Statement stmt = null;
			ResultSet res = null;
			stmt = conn.createStatement();
			String query = "select * from attack_information where attack_running like 'yes';";
			res = stmt.executeQuery(query);
			if (!res.isBeforeFirst()) {
				close(conn);
				return false;
			}
			int aID = 0;
			int arn = 0;
			int balance = 0;
			String method = "";
			while(res.next())
			{
				aID = res.getInt(1);
				balance = res.getInt(2);
				method = res.getString(3);
			}
			query = "select arn from arn_sequences where attack_id = " + aID + " order by arn asc";
			res = stmt.executeQuery(query);
			if (!res.isBeforeFirst())
				arn = 0;
			else
			{
				res.next();
				arn = res.getInt(1);
			}
			AttackController.runningAttack = true;
			AttackController.benchmark = balance;
			AttackController.currentSequence = new AtomicInteger(arn);
			AttackController.attackMethod = method;
			AttackController.failedSequences.clear();
			AttackController.attackID = new AtomicInteger(aID);
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close(conn);
			throw new RuntimeException(e);
		}
		close(conn);
		return true;
	}

	public static void checkForRecoveredFailedSequences() {
		Connection conn = SQLConnect();
		try {
			Statement stmt = null;
			ResultSet res = null;
			int arnSet = 0;
			stmt = conn.createStatement();
			String query = "select failed_arn from failed_sequences where attack_id like " + AttackController.attackID.get() + ";";
			res = stmt.executeQuery(query);
			if (!res.isBeforeFirst()) {
				close(conn);
				return;
			}
			while (res.next()) {
				AttackController.failedSequences.add(res.getInt(1));
				if(arnSet < res.getInt(1))
					arnSet = res.getInt(1);
			}
			if(AttackController.currentSequence.get() == 0)
				AttackController.currentSequence = new AtomicInteger(arnSet + 1);
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close(conn);
			throw new RuntimeException(e);
		}
		close(conn);
		return;
}

	public static String checkDev(String deviceID) {
		Connection conn = SQLConnect();
		int arnSet = 00;
		try {
			Statement stmt = null;
			ResultSet res = null;
			stmt = conn.createStatement();
			String query = "select arn from arn_sequences where devid like '"
					+ deviceID + "';";
			res = stmt.executeQuery(query);
			if (!res.isBeforeFirst()) {
				close(conn);
				return "none";
			}
			while (res.next()) {
				arnSet = res.getInt(1);
			}
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close(conn);
			throw new RuntimeException(e);
		}
		close(conn);
		return Integer.toString(arnSet);
	}

	public static void getLastAID() {
		Connection conn = SQLConnect();
		try {
			Statement stmt = null;
			ResultSet res = null;
			stmt = conn.createStatement();
			String query = "select attack_id from attack_information order by attack_id desc;";
			res = stmt.executeQuery(query);
			if (!res.isBeforeFirst()) {
				close(conn);
				return;
			}
			res.next();
			AttackController.attackID.set(res.getInt(1));
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close(conn);
			throw new RuntimeException(e);
		}
		close(conn);
		return;
	}

	public static void addARNCheck(int arn, String deviceID) {
		Connection conn = SQLConnect();
		try {
			Statement stmt = null;
			ResultSet res = null;
			stmt = conn.createStatement();
			String query = "insert ignore into arn_sequences(attack_id, arn, devid) values("
					+ AttackController.attackID.get() + ", " + arn + ", '"+ deviceID +"' );";
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close(conn);
			throw new RuntimeException(e);
		}
		close(conn);
		return;
	}

	public static void removeARNCheck(int arn) {
		Connection conn = SQLConnect();
		try {
			Statement stmt = null;
			ResultSet res = null;
			stmt = conn.createStatement();
			String query = "delete from arn_sequences where arn = " + arn;
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close(conn);
			throw new RuntimeException(e);
		}
		close(conn);
	}

	public static void addFailedSequence(int arn) {
		Connection conn = SQLConnect();
		try {
			Statement stmt = null;
			ResultSet res = null;
			stmt = conn.createStatement();
			String query = "insert ignore into failed_sequences(attack_id, failed_arn) values(" + AttackController.attackID.get() + ", " + arn + " );";
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close(conn);
			throw new RuntimeException(e);
		}
		close(conn);
		return;
	}

	public static void removeFailedSequence(int arn) {
		Connection conn = SQLConnect();
		try {
			Statement stmt = null;
			ResultSet res = null;
			stmt = conn.createStatement();
			String query = "delete from failed_sequences where failed_arn = " + arn;
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close(conn);
			throw new RuntimeException(e);
		}
		close(conn);
		return;
	}

	public static void addAndStartAttackInformation() {
		Connection conn = SQLConnect();
		try {
			Statement stmt = null;
			ResultSet res = null;
			stmt = conn.createStatement();
			String query = "insert into attack_information(attack_id, balance_value, attack_method, attack_Start, attack_Running)"
					+ " values(" + AttackController.attackID.get() + ", " + AttackController.benchmark + ", '" + AttackController.attackMethod + "', " + System.currentTimeMillis() + ", 'yes');";
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close(conn);
			throw new RuntimeException(e);
		}
		close(conn);
		return;
	}

	public static void enterCompleteInformation(String result) {
		Connection conn = SQLConnect();
		try {
			Statement stmt = null;
			ResultSet res = null;
			stmt = conn.createStatement();
			result = result.replaceAll("\\\\", "\\\\\\\\");
			result = result.replaceAll("\"", "\\\\\"");
			String query = "update attack_information set attack_stop="+System.currentTimeMillis()+", attack_result='"+result+"' where attack_id="+AttackController.attackID.get()+";";
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close(conn);
			throw new RuntimeException(e);
		}
		close(conn);
		return;
	}

	public static void endAttack() {
		Connection conn = SQLConnect();
		try {
			Statement stmt = null;
			ResultSet res = null;
			stmt = conn.createStatement();
			String query = "update attack_information set attack_running='no' where attack_id = " + AttackController.attackID.get();
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close(conn);
			throw new RuntimeException(e);
		}
		close(conn);
		return;
	}
	
  public static void close(Connection conn) {
    try {
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
