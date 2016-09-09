package geneos_notification.controllers;

import java.util.HashMap;
import java.util.Map;

import geneos_notification.objects.Device;

public class UserController {
	
	//public static ArrayList<String> nodes = new ArrayList<String>();
	public static Map<String, Device> nodes = new HashMap<String, Device>();
	
	public static String attackCheck(String deviceID)
	{
		String ret = "no";
		if(!nodes.containsKey(deviceID))
			nodes.put(deviceID, new Device(deviceID));
		if(AttackController.runningAttack)
			ret = "yes";
		return ret;
	}
	
	public static void healthUpdate(String deviceID)
	{
		nodes.get(deviceID).setHealthBeats(System.currentTimeMillis());
	}

}
