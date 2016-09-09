package geneos_notification.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import geneos_notification.objects.Device;
import laviathon_server.threads.HealthCheck;

public class AttackController {

	public static AtomicInteger currentSequence;
	public static ArrayList<Integer> failedSequences = new ArrayList<Integer>();
    public static boolean runningAttack = false;
    public static String target;
    public static AtomicInteger attackID;
	
	public static int decideAttackSequenceForClient(String deviceID)
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
}