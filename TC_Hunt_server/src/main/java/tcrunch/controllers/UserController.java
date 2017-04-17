package tcrunch.controllers;

import java.util.HashMap;
import java.util.Map;

import tcrunch.objects.Device;

/*	Created by:		Connor Morley
 * 	Title:			TCrunch Server User Controller
 *  Version update:	2.4
 *  Notes:			Class is used to create and handle Device objects and provide attack ID of running attack at time of poll. Class is also responsible for updating 
 *  				device objects health values which are used within the health checker thread to ensure nodes do not time out. 
 *  
 *  References:		N/A
 */

public class UserController {
	
	public static Map<String, Device> nodes = new HashMap<String, Device>();
	
	public static String attackCheck(String deviceID) {
		String ret = "no";
		if (!nodes.containsKey(deviceID))	//	If the node is not currently listed within the system, add it. 
			nodes.put(deviceID, new Device(deviceID));
		if (AttackController.runningAttack) // If attack is running at time of check issue the node an ARN.
		{
			ret = getRunningAttackID(deviceID, ret);
		}
		return ret;
	}

	private static String getRunningAttackID(String deviceID, String ret) {
		if (AttackController.attackMethod.equals("Dictionary") && !AttackController.dictionaryAttackOutOfWords)
			ret = Integer.toString(AttackController.attackID.get());
		else if (AttackController.attackMethod.equals("Brute Force"))
			ret = Integer.toString(AttackController.attackID.get());
		return ret;
	}
	
	public static String healthUpdate(String deviceID)
	{
		//If the node is on record then update, if not on record the system has been removed but is active. Send abort to trigger reconnection. 
		if (nodes.containsKey(deviceID)) {
			nodes.get(deviceID).setHealthBeats(System.currentTimeMillis());
			return "";
		} else
			return "abort";
	}

}
