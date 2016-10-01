package laviathon_server.threads;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONException;

import geneos_notification.controllers.AttackController;
import geneos_notification.controllers.UserController;
import geneos_notification.objects.Device;

public class HealthCheck {
	
	//Thread to monitor the node status, checks their health every 2 seconds to see if their last "ping" was over 10 seconds ago. If yes the node is removed from 
	//the user nodes and the attack sequence assigned to the node is added to the failed sequences queue to be picked up by the next available node OR the same
	//node if it re-established contact with the server (although it will have to start the sequence from the start).
	public static void startHealthCheckThread()
	{
	ExecutorService exec = Executors.newSingleThreadExecutor();
	Callable<Integer> callable = new Callable<Integer>() {
		@Override
		public Integer call() throws JSONException, InterruptedException{	
			Thread.currentThread().sleep(7500);
			while(AttackController.runningAttack == true) // For as long as the attack is running carry out health check
			{
				for(Map.Entry<String, Device> devEntry : UserController.nodes.entrySet())
				{
					if(devEntry.getValue().getHealthBeats() < (System.currentTimeMillis() - 10000))
					{
						AttackController.failedSequences.add(devEntry.getValue().getAttackSequence());
						System.out.println("Node: '" + devEntry.getKey() + "' has been removed and the attack sequence '" + devEntry.getValue().getAttackSequence() + "' has been added to the failed sequences.");
						UserController.nodes.remove(devEntry.getKey());
					}
				}
				Thread.currentThread().sleep(2000); // Check runs in 2 second intervals.
			}
			return 1;
		}
	};
	Future<Integer> future = exec.submit(callable);
	}

}
