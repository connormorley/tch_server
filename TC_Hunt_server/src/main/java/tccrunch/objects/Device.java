package tccrunch.objects;

/*	Created by:		Connor Morley
 * 	Title:			TCrunch Server Device Object
 *  Version update:	2.2
 *  Notes:			Object class is used as a representation of attached nodes and contains their relative information. These objects are then catalogued against
 *  				individual ID criteria provided by the node for easy retrieval and reference during communication and organsisation by the server.
 *  
 *  References:		N/A
 */

public class Device {
	
	private String deviceID;
	private int attackSequence = 0;
	private Long healthBeats = 0L;
	private int status = 0;

	public Device(String d_id)
	{
		this.deviceID = d_id;
		this.setHealthBeats(System.currentTimeMillis());
	}

	private String getDeviceID() {
		return deviceID;
	}
	
	public int getAttackSequence() {
		return attackSequence;
	}

	public void setAttackSequence(int attackSequence) {
		this.attackSequence = attackSequence;
	}
	
	public Long getHealthBeats() {
		return healthBeats;
	}

	public void setHealthBeats(Long healthBeats) {
		this.healthBeats = healthBeats;
	}

}
