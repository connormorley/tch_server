package tcrunch.controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import tccrunch.loggers.LogObject;
import tccrunch.loggers.LtA;
import tccrunch.threads.HealthCheck;

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

	public static boolean checkRecoveredAttack() {
		SQLConnect();
		try {
			stmt = conn.createStatement();
			String query = "select * from attack_information where attack_running like 'yes';";
			res = stmt.executeQuery(query);
			if (!res.isBeforeFirst()) {
				close();
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
			HealthCheck.startHealthCheckThread();
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close();
			throw new RuntimeException(e);
		}
		close();
		return true;
	}
	
///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

	public static void checkForRecoveredFailedSequences() {
		SQLConnect();
		try {
			stmt = conn.createStatement();
			String query = "select failed_arn from failed_sequences where attack_id like " + AttackController.attackID.get() + ";";
			res = stmt.executeQuery(query);
			if (!res.isBeforeFirst()) {
				close();
				return;
			}
			while (res.next()) {
				AttackController.failedSequences.add(res.getInt(1));
			}
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close();
			throw new RuntimeException(e);
		}
		close();
		return;
}
	
///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

	public static void getLatAID() {
		SQLConnect();
		try {
			stmt = conn.createStatement();
			String query = "select attack_id from attack_information order by attack_id desc;";
			res = stmt.executeQuery(query);
			if (!res.isBeforeFirst()) {
				close();
				return;
			}
			while (res.next()) {
				AttackController.attackID.set(res.getInt(1));
			}
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close();
			throw new RuntimeException(e);
		}
		close();
		return;
	}
	
///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

	public static void addARNCheck(int arn) {
		SQLConnect();
		try {
			stmt = conn.createStatement();
			String query = "insert ignore into arn_sequences(attack_id, arn) values("
					+ AttackController.attackID.get() + ", " + arn + " );";
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close();
			throw new RuntimeException(e);
		}
		close();
		return;
	}
	
///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

	public static void removeARNCheck(int arn) {
		SQLConnect();
		try {
			stmt = conn.createStatement();
			String query = "delete from arn_sequences where arn = " + arn;
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close();
			throw new RuntimeException(e);
		}
		close();
	}
	
///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

	public static void addFailedSequence(int arn) {
		SQLConnect();
		try {
			stmt = conn.createStatement();
			String query = "insert into failed_sequences(attack_id, failed_arn) values(" + AttackController.attackID.get() + ", " + arn + " );";
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close();
			throw new RuntimeException(e);
		}
		close();
		return;
	}
	
///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

	public static void removeFailedSequence(int arn) {
		SQLConnect();
		try {
			stmt = conn.createStatement();
			String query = "delete from failed_sequences where failed_arn = " + arn;
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close();
			throw new RuntimeException(e);
		}
		close();
		return;
	}
	
///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

	public static void addAndStartAttackInformation() {
		SQLConnect();
		try {
			stmt = conn.createStatement();
			String query = "insert into attack_information(attack_id, balance_value, attack_method, attack_Start, attack_Running)"
					+ " values(" + AttackController.attackID.get() + ", " + AttackController.benchmark + ", '" + AttackController.attackMethod + "', " + System.currentTimeMillis() + ", 'yes');";
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close();
			throw new RuntimeException(e);
		}
		close();
		return;
	}
	
///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

	public static void enterCompleteInformation(String result) {
		SQLConnect();
		try {
			stmt = conn.createStatement();
			String query = "update attack_information set attack_stop="+System.currentTimeMillis()+", attack_result='"+result+"' where attack_id="+AttackController.attackID.get()+";";
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close();
			throw new RuntimeException(e);
		}
		close();
		return;
	}
	
///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

	public static void endAttack() {
		SQLConnect();
		try {
			stmt = conn.createStatement();
			String query = "update attack_information set attack_running='no' where attack_id = " + AttackController.attackID.get();
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close();
			throw new RuntimeException(e);
		}
		close();
		return;
	}
	
///////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////

	/*public static void updateCurrentArn(int arn) {
		SQLConnect();
		try {
			stmt = conn.createStatement();
			String query = "update attack_information set last_completed_arn = " + arn + " where attack_id = " + AttackController.attackID.get();
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			logA.doLog("SQL", "[SQL]Query error while retrieving custom dataset \nError is : " + e.toString(),
					"Critical");
			e.printStackTrace();
			close();
			throw new RuntimeException(e);
		}
		close();
		return;
	}*/

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
