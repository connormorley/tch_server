package laviathon_server.threads;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;

import geneos_notification.controllers.AttackController;
import geneos_notification.controllers.UserController;
import geneos_notification.objects.Device;

public class HealthCheck {
	
	public static void startHealthCheckThread()
	{
	ExecutorService exec = Executors.newSingleThreadExecutor();
	Callable<Integer> callable = new Callable<Integer>() {
		@Override
		public Integer call() throws JSONException, InterruptedException{	
			while(AttackController.runningAttack == true) // For as long as the attack is running carry out health check
			{
				for(Map.Entry<String, Device> devEntry : UserController.nodes.entrySet())
				{
					if(devEntry.getValue().getHealthBeats() < (System.currentTimeMillis() - 10000))
					{
						AttackController.failedSequences.add(devEntry.getValue().getAttackSequence());
						UserController.nodes.remove(devEntry.getKey());
						//If the time is 10 seconds since last update then the node is seen as down, 
						//its sequence is set as failed and it is removed from the server.
					}
				}
				Thread.currentThread().sleep(10000); // Check runs in 10 second intervals.
			}
			return 1;
		}
	};
	}

}
