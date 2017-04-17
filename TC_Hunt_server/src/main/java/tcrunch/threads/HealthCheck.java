package tcrunch.threads;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONException;

import tcrunch.controllers.AttackController;
import tcrunch.controllers.DatabaseController;
import tcrunch.controllers.InterfaceController;
import tcrunch.controllers.UserController;
import tcrunch.objects.Device;

/*	Created by:		Connor Morley
 * 	Title:			Health Check Thread
 *  Version update:	2.4
 *  Notes:			Class creates thread to monitor the node status, checks their health every 7.5 seconds to see if their last "ping" was over 10 seconds ago. 
 *  				If yes the node is removed from the user nodes and the attack sequence assigned to the node is added to the failed sequences queue to be 
 *  				picked up by the next available node OR the same node if it re-established contact with the server (although it will have to start the 
 *  				sequence from the start).
 *  
 *  References:		N/A
 */

public class HealthCheck {

	static Future<Integer> future;
	
	public static void startHealthCheckThread()
	{
	ExecutorService exec = Executors.newSingleThreadExecutor();
	Callable<Integer> callable = new Callable<Integer>() {
		@Override
		public Integer call() throws JSONException{	
			try {
				Thread.currentThread().sleep(7500);
			while(AttackController.runningAttack == true) // For as long as the attack is running carry out health check
			{
				for(Iterator<Map.Entry<String, Device>> iteration = UserController.nodes.entrySet().iterator(); iteration.hasNext();)
				{
					Map.Entry<String, Device> devEntry = iteration.next();
					if(devEntry.getValue().getHealthBeats() < (System.currentTimeMillis() - 15000))
					{
						AttackController.failedSequences.add(devEntry.getValue().getAttackSequence());
						DatabaseController.addFailedSequence(devEntry.getValue().getAttackSequence());
						DatabaseController.removeARNCheck(devEntry.getValue().getAttackSequence());
						System.out.println("Node: '" + devEntry.getKey() + "' has been removed and the attack sequence '" + devEntry.getValue().getAttackSequence() + "' has been added to the failed sequences.");
						iteration.remove();
						if(UserController.nodes.size() == InterfaceController.userWordlistsExpired)
						{
							InterfaceController.userWordlistsExpired = 0;
							AttackController.dictionaryAttackOutOfWords = false;
						}
					}
				}
				Thread.currentThread().sleep(2000); // Check runs in 2 second intervals.
			}
			return 1;
			} catch (InterruptedException e) {
				return 1;
			} catch(Exception e)
			{
				e.printStackTrace();
				AttackController.runHealthChecks();
				return 1;
			}
		}
	};
	future = exec.submit(callable);
	}
	
	public static void endHealtCheckThread()
	{
		future.cancel(true); //terminate the callable future thread, cause interrupt exception resulting in return;
	}

}
