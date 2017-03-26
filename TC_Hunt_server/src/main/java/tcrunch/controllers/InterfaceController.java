package tcrunch.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

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
import java.util.logging.Logger;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tccrunch.loggers.LogObject;
import tccrunch.loggers.LtA;
import tccrunch.threads.HealthCheck;



@RestController
public class InterfaceController {

    private static String sqlKey;
    public static String serverPassword = "test";
    public static int sampleRate;
    public static ArrayList<JSONObject> currentDataviewEntityList;
    private final static Logger logger = Logger.getLogger(InterfaceController.class.getName());
    private static LtA logA = new LogObject();
    public static int userWordlistsExpired = 0;
    public static boolean emailNotify = false;
    public static boolean recovering = false;

    
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////    	
    
    //Used to check if an attack is in progress to initiate the node into an attack mode instead of idle,
    //additionally is first instance where the attack node is registered with the server for reference.
    @RequestMapping(value="/attackCheck", method=RequestMethod.POST)		
    public static String attackCheck(@RequestParam(value="deviceid", defaultValue="") String deviceID, @RequestParam(value="password", defaultValue="") String password) throws Exception 
    {
		if (password.equals(serverPassword)) 
			{
				if(recovering)
				{
					HealthCheck.startHealthCheckThread();
					recovering = false;
				}
				return UserController.attackCheck(deviceID); // Returns either yes or no as indication if an attack is running
			}
		else
			return "Unauthorised access";
    }
    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@RequestMapping(value = "/clientAttackCheck", method = RequestMethod.POST)
	public static String clientAttackCheck(@RequestParam(value = "deviceid", defaultValue = "") String deviceID,
			@RequestParam(value = "password", defaultValue = "") String password) throws Exception {
		if (password.equals(serverPassword))
			if(AttackController.runningAttack)
				return Integer.toString(AttackController.attackID.get());
			else
				return "no";
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

	@RequestMapping(value = "/issueBenchmark", method = RequestMethod.POST)
	public static void issueBenchmark(@RequestParam(value = "password", defaultValue = "") String password, @RequestParam(value = "benchmark", defaultValue = "") String benchmark)
			throws Exception {
		//If the benchmark (number of files per minute) stored is greater than the issued benchmark, replace the existing benchmark for the slower machine.
		if (password.equals(serverPassword))
			if(AttackController.benchmark > Integer.parseInt(benchmark) && AttackController.runningAttack == false)
			{
				AttackController.benchmark = Integer.parseInt(benchmark);
				System.out.println("Benchmark changed to : " + benchmark);
			}
			else if(AttackController.benchmark > Integer.parseInt(benchmark))
				AttackController.switchBenchmark = Integer.parseInt(benchmark);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@RequestMapping(value = "/getARN", method = RequestMethod.POST)
	public static String getARN(@RequestParam(value="deviceid", defaultValue="") String deviceID, @RequestParam(value = "password", defaultValue = "") String password) throws Exception {
				if(password.equals(serverPassword))
				{
					int arn = AttackController.decideAttackSequenceForNode(deviceID);
					//DatabaseController.updateCurrentArn(arn);
					return Integer.toString(arn); // Checks if the device is registered and returns the attack sequence for that device.
				}
				else
					return "0"; // If device is unauthorised nothing is issued against device id so returned value is irrelevant
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@RequestMapping(value = "/getBalance", method = RequestMethod.POST)
	public static String getBalance(
			@RequestParam(value = "password", defaultValue = "") String password) throws Exception {
		if (password.equals(serverPassword))
			return Integer.toString(AttackController.benchmark);
		else
			return "0";
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@RequestMapping(value = "/getAttackMethod", method = RequestMethod.POST)
	public static String getAttackMethod(@RequestParam(value = "deviceid", defaultValue = "") String deviceID, @RequestParam(value = "password", defaultValue = "") String password) throws Exception {
		if (password.equals(serverPassword))
			return AttackController.attackMethod; 
		else
			return "0"; 
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@RequestMapping(value="/healthCheck", method=RequestMethod.POST)		
	public static String healthCheck(@RequestParam(value="deviceid", defaultValue="") String deviceID, @RequestParam(value="password", defaultValue="") String password) throws Exception 
	{
		String ret = "";
		if (AttackController.runningAttack == false)
		{
			return "abort";
		}
	    else if (password.equals(serverPassword)) {
		ret = UserController.healthUpdate(deviceID); // Updates the health time of the device, this is used to indicate when it last checked in with the server for timeout control.
		return ret;
		} else
		return ret;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@RequestMapping(value="/issueAttack", method=RequestMethod.POST)		
	public static String issueAttack(@RequestParam(value="attackblock", defaultValue="") String attackBlock, @RequestParam(value="password", defaultValue="") String password, @RequestParam(value="attackmethod", defaultValue="") String attackMethod) throws Exception 
	{
		if (password.equals(serverPassword)){
			//System.out.println("command received " + password + "   " + attackBlock);
		AttackController.target = attackBlock; // Updates the health time of the device, this is used to indicate when it last checked in with the server for timeout control.
		
		File file = new File("testingTCFile");
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(attackBlock);
		fileWriter.flush();
		fileWriter.close();
		
		
/*		int len = attackBlock.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(attackBlock.charAt(i), 16) << 4)
	                             + Character.digit(attackBlock.charAt(i+1), 16));
	    }
		
		//byte[] finalbytes = attackBlock.getBytes("ISO-8859-1");
		File outputFile = new File("testingTCFile");
		    try ( FileOutputStream outputStream = new FileOutputStream(outputFile); ) {
		        outputStream.write(attackBlock, 0, attackBlock.length());  //write the bytes and your done. 
		        outputStream.flush();
		    } catch (Exception e) {
		        e.printStackTrace();
		    }*/
		
		AttackController.runningAttack = true;
		AttackController.currentSequence = new AtomicInteger(0);
		AttackController.attackMethod = attackMethod;
		AttackController.failedSequences.clear();
		//AttackController.attackID = new AtomicInteger(1); //?
		AttackController.attackID.incrementAndGet();
		DatabaseController.addAndStartAttackInformation();
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
			userWordlistsExpired = 0;
			if(emailNotify)
			EmailController.sendMail(AttackController.attackID.get(), result);
			AttackController.updateBenchmark();
			DatabaseController.enterCompleteInformation(result);
			DatabaseController.endAttack();
			AttackController.dictionaryAttackOutOfWords = false; // TEST
		} else
			return;
		}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//Indicate to the server to stop the running attack, marks the attack as exhausting the wordlist available.
	@RequestMapping(value = "/wordlistExhausted", method = RequestMethod.POST)
	public static void wordlistExhausted(@RequestParam(value = "password", defaultValue = "") String password) throws Exception {
		if (password.equals(serverPassword)) {
			userWordlistsExpired++;
			if(AttackController.dictionaryAttackOutOfWords == false)
				AttackController.dictionaryAttackOutOfWords = true; // TEST
			if(userWordlistsExpired == UserController.nodes.size())
			{
			AttackController.runningAttack = false;
			AttackController.attackResults.put(AttackController.attackID.get(), "Wordlist Exhausted");
			AttackController.dictionaryAttackOutOfWords = false; //TEST
			if(emailNotify)
			EmailController.sendMail(AttackController.attackID.get(), "Wordlist Exhausted");
			AttackController.updateBenchmark();
			DatabaseController.enterCompleteInformation("Wordlist Exhausted");
			DatabaseController.endAttack();
			logA.doLog("INTERFACE", "[INTERFACE]Wordlist exhasuted for attack " + AttackController.attackID.get(), "Info");
			}
		} else
			return;
	}
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//Indicate to the server to stop the running attack, marks the result of the scan as "user aborted". 
	@RequestMapping(value = "/abortAttack", method = RequestMethod.POST)
	public static void abortAttack(@RequestParam(value = "password", defaultValue = "") String password, @RequestParam(value="attackID", defaultValue="") String attackID) throws Exception {
		if (password.equals(serverPassword)) {
			AttackController.runningAttack = false;
			AttackController.attackResults.put(AttackController.attackID.get(), "User Aborted");
			AttackController.dictionaryAttackOutOfWords = false; // TEST
			AttackController.updateBenchmark();
			logA.doLog("INTERFACE" , "[INTERFACE]Password for attack sequence " + AttackController.attackID.get() + " has been manually termianted by user.", "Info");
			System.out.println("Attack was manually terminated by user");
			if(emailNotify)
			EmailController.sendMail(AttackController.attackID.get(), "Manually terminated");
			DatabaseController.enterCompleteInformation("User abort.");
			DatabaseController.endAttack();
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