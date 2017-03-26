package tcrunch.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import tccrunch.objects.Device;
import tccrunch.threads.HealthCheck;

public class AttackController {

	public static AtomicInteger currentSequence;
	public static ArrayList<Integer> failedSequences = new ArrayList<Integer>();
    public static boolean runningAttack = false;
    public static String target;
    public static AtomicInteger attackID = new AtomicInteger(1);
    public static Map<Integer, String> attackResults = new HashMap<Integer, String>();
    public static String attackMethod;
    public static boolean dictionaryAttackOutOfWords = false;
    public static int benchmark = 500;
    public static int switchBenchmark = 0;
	
	public static int decideAttackSequenceForNode(String deviceID)
	{
		Device temp = UserController.nodes.get(deviceID);
		String storedARN = DatabaseController.checkDev(deviceID);
		
		if(storedARN.equals("none"))
		{
			try{
			temp.setHealthBeats(System.currentTimeMillis());
			if(!failedSequences.isEmpty())
			{
				int arn = failedSequences.get(0);
				temp.setAttackSequence(arn); // If there are any entries in the failedSequences use them first
				DatabaseController.removeFailedSequence(arn);
				DatabaseController.addARNCheck(arn, deviceID);
				failedSequences.remove(0);
				return temp.getAttackSequence();
			}
			else
			{
				int seq = currentSequence.getAndIncrement();
				temp.setAttackSequence(seq);
				DatabaseController.addARNCheck(seq, deviceID);
				return seq; // If there are no failed sequences, issue newest sequence.
			}
			} catch(Exception e)
			{
				System.out.println("Device ID : " + deviceID + " tried to connect but was not present in internal census. Re-connection issued to node.");
				return -1;
			}
		}
		else
		{
			int retArn = Integer.parseInt(storedARN);
			temp.setHealthBeats(System.currentTimeMillis());
			temp.setAttackSequence(retArn);
			DatabaseController.addARNCheck(retArn, deviceID);
			return retArn;
		}
	}
	
	public static void runHealthChecks()
	{
		HealthCheck.startHealthCheckThread(); // Initiate the health check cycle for devices to identify timeout/downed nodes
	}
	
	public static void updateBenchmark()
	{
		if(switchBenchmark < benchmark && switchBenchmark != 0)
		{
			benchmark = switchBenchmark;
			System.out.println("Benchmark changed to : " + benchmark);
		}
	}
}