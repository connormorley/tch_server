package geneos_notification.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import geneos_notification.objects.Device;
import laviathon_server.threads.HealthCheck;

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
    public static int switchBenchmark;
	
	public static int decideAttackSequenceForNode(String deviceID)
	{
		Device temp = UserController.nodes.get(deviceID);
		temp.setHealthBeats(System.currentTimeMillis());
		if(!failedSequences.isEmpty())
		{
			temp.setAttackSequence(failedSequences.get(0)); // If there are any entries in the failedSequences use them first
			failedSequences.remove(0);
			return temp.getAttackSequence();
		}
		else
		{
			int seq = currentSequence.getAndIncrement();
			temp.setAttackSequence(seq);
			return seq; // If there are no failed sequences, issue newest sequence.
		}
	}
	
	public static void runHealthChecks()
	{
		HealthCheck.startHealthCheckThread(); // Initiate the health check cycle for devices to identify timeout/downed nodes
	}
	
	public static void updateBenchmark()
	{
		if(switchBenchmark > benchmark)
		{
			benchmark = switchBenchmark;
			System.out.println("Benchmark changed to : " + benchmark);
		}
	}
}