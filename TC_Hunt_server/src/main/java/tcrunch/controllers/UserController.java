package tcrunch.controllers;

import java.util.HashMap;
import java.util.Map;

import tccrunch.objects.Device;

public class UserController {
	
	//public static ArrayList<String> nodes = new ArrayList<String>();
	public static Map<String, Device> nodes = new HashMap<String, Device>();
	
	public static String attackCheck(String deviceID)
	{
		String ret = "no";
		if(!nodes.containsKey(deviceID))
			nodes.put(deviceID, new Device(deviceID));
		if(AttackController.runningAttack)
			if(AttackController.attackMethod.equals("Dictionary") && !AttackController.dictionaryAttackOutOfWords)
				ret = Integer.toString(AttackController.attackID.get());
			else if(AttackController.attackMethod.equals("Brute Force"))
			ret = Integer.toString(AttackController.attackID.get());
		return ret;
	}
	
	public static void healthUpdate(String deviceID)
	{
		nodes.get(deviceID).setHealthBeats(System.currentTimeMillis());
	}

}
