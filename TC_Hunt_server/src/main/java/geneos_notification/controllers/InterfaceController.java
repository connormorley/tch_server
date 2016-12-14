package geneos_notification.controllers;

import java.io.File;
import java.io.FileOutputStream;

/*
 * Create by:	cmorley 10/09/2015
 * Description:	This is the main controller for the server and defines the commands that can be issued by cURL and the values and 
 * 				request types they must include in order to be accepted. Some key values for the server are stored here for 
 * 				convenience. Below is a list of the available commands that can be issued to the server with a description of their
 * 				operation. This class also, currently, contains the point of creation for the monitoring threads which are then 
 * 				controller and executed in the AlertController class. This Class also contains the list of logged in users which
 * 				is referenced for multiple device login's and whenever a change is made in relation to an account in order to verify
 * 				the user is who they say and are authorised to make those changes. The Class also contains a list of the currently
 * 				active threads within an object map which stores the critical data for those threads in order to control them 
 * 				externally.
*/

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import geneos_notification.loggers.LogObject;
import geneos_notification.loggers.LtA;
import laviathon_server.threads.HealthCheck;



@RestController
public class InterfaceController {

    private static String sqlKey;
    private static String serverPassword = "test";
    public static int sampleRate;
    public static ArrayList<JSONObject> currentDataviewEntityList;
    static LtA logA = new LogObject();

    
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////    	
    
    //Used to check if an attack is in progress to initiate the client into an attack mode instead of idle,
    //additionally is first instance where the client node is registered with the server for reference.
    @RequestMapping(value="/attackCheck", method=RequestMethod.POST)		
    public static String attackCheck(@RequestParam(value="deviceid", defaultValue="") String deviceID, @RequestParam(value="password", defaultValue="") String password) throws Exception 
    {
		if (password.equals(serverPassword)) 
			return UserController.attackCheck(deviceID); // Returns either yes or no as indication if an attack is running
			else
			return "Unauthorised access";
    }
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //This is used by the client to poll if the attack has generated a result or not depending on the attackID issued at initiation.
    @RequestMapping(value="/resultCheck", method=RequestMethod.POST)		
	public static String Check(@RequestParam(value="attackID", defaultValue="") String attackID, @RequestParam(value="password", defaultValue="") String password) throws Exception 
	{
    	if (password.equals(serverPassword)) 
    	{
    		Integer check = Integer.parseInt(attackID);
    		if(AttackController.attackResults.containsKey(check))
    			return AttackController.attackResults.get(check);
    		else
    		return "No result"; // 
    	}
    	else
    		return "Unauthorised access";
	}
     
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@RequestMapping(value = "/getJobBlock", method = RequestMethod.POST)
	public static String getJobBlock(@RequestParam(value = "password", defaultValue = "") String password) throws Exception {
				if(password.equals(serverPassword))
					return AttackController.target; // Issue request for the attack block which is returned as a Byte array
				else
					return null;

	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@RequestMapping(value = "/getAttackSequence", method = RequestMethod.POST)
	public static String getAttackSequence(@RequestParam(value="deviceid", defaultValue="") String deviceID, @RequestParam(value = "password", defaultValue = "") String password) throws Exception {
				if(password.equals(serverPassword))
					return Integer.toString(AttackController.decideAttackSequenceForClient(deviceID)); // Checks if the device is registered and returns the attack sequence for that device.
				else
					return "0"; // If device is unauthorised nothing is issued against device id so returned value is irrelevant
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@RequestMapping(value="/healthCheck", method=RequestMethod.POST)		
	public static String healthCheck(@RequestParam(value="deviceid", defaultValue="") String deviceID, @RequestParam(value="password", defaultValue="") String password) throws Exception 
	{
		if (AttackController.runningAttack == false)
		{
			return "abort";
		}
	    else if (password.equals(serverPassword)) {
		UserController.healthUpdate(deviceID); // Updates the health time of the device, this is used to indicate when it last checked in with the server for timeout control.
		return "";
		} else
		return "";
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@RequestMapping(value="/issueAttack", method=RequestMethod.POST)		
	public static String issueAttack(@RequestParam(value="attackblock", defaultValue="") String attackBlock, @RequestParam(value="password", defaultValue="") String password) throws Exception 
	{
		if (password.equals(serverPassword)){
			//System.out.println("command received " + password + "   " + attackBlock);
		AttackController.target = attackBlock; // Updates the health time of the device, this is used to indicate when it last checked in with the server for timeout control.
		
		int len = attackBlock.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(attackBlock.charAt(i), 16) << 4)
	                             + Character.digit(attackBlock.charAt(i+1), 16));
	    }
		
		//byte[] finalbytes = attackBlock.getBytes("ISO-8859-1");
		File outputFile = new File("testingTCFile");
		    try ( FileOutputStream outputStream = new FileOutputStream(outputFile); ) {
		        outputStream.write(data, 0, data.length);  //write the bytes and your done. 
		        outputStream.flush();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		
		AttackController.runningAttack = true;
		AttackController.currentSequence = new AtomicInteger(0);
		//AttackController.attackID = new AtomicInteger(1); //?
		AttackController.attackID.incrementAndGet();
		HealthCheck.startHealthCheckThread();
		return Integer.toString(AttackController.attackID.get());
		} else
		return "0";
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//Indicate to the server to stop the running attack, stores the found password in association with the attack ID within the system for retrieval by the client.
	@RequestMapping(value = "/passwordFound", method = RequestMethod.POST)
	public static void passwordFound(@RequestParam(value = "password", defaultValue = "") String password, @RequestParam(value="result", defaultValue="") String result) throws Exception {
		if (password.equals(serverPassword)) {
			AttackController.runningAttack = false;
			AttackController.attackResults.put(AttackController.attackID.get(), result);
			logA.doLog("INTERFACE" , "[INTERFACE]Password for attack sequence " + AttackController.attackID.get() + " had been identified as : " + result, "Info");
			System.out.println("password is " + result);
		} else
			return;
		}
	
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//Indicate to the server to stop the running attack, stores the found password in association with the attack ID within the system for retrieval by the client.
	@RequestMapping(value = "/abortAttack", method = RequestMethod.POST)
	public static void abortAttack(@RequestParam(value = "password", defaultValue = "") String password, @RequestParam(value="attackID", defaultValue="") String attackID) throws Exception {
		if (password.equals(serverPassword)) {
			AttackController.runningAttack = false;
			AttackController.attackResults.put(AttackController.attackID.get(), "User Aborted");
			logA.doLog("INTERFACE" , "[INTERFACE]Password for attack sequence " + AttackController.attackID.get() + " has been manually termianted by user.", "Info");
			System.out.println("Attack was manually terminated by user");
		} else
			return;
		}
	
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@RequestMapping(value = "/attackID", method = RequestMethod.POST)
	public static String checkAttackID(@RequestParam(value = "password", defaultValue = "") String password) throws Exception {
		if (password.equals(serverPassword) && AttackController.runningAttack == true) {
			return Integer.toString(AttackController.attackID.get());
		} else
			return "0"; 
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@RequestMapping(value = "/checkLive", method = RequestMethod.POST)
	public static String test(@RequestParam(value = "password", defaultValue = "") String password) throws Exception {
		if (password.equals(serverPassword)) {
			return "Connection ok";
		} else
			return "";
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static void setKeyData(String sql)
    {
    	sqlKey = sql;
    	DatabaseController.setAddress(sqlKey);
    }
    
    
}