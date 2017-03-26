package tcrunch.controllers;

import java.util.HashMap;
import java.util.Map;

import tccrunch.objects.Device;

public class UserController {
	
	//public static ArrayList<String> nodes = new ArrayList<String>();
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
