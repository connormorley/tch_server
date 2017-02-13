package tccrunch.objects;

public class Device {
	
	private String deviceID;
	private int attackSequence = 0;
	private Long healthBeats = 0L;
	private int status = 0;

	public Device(String d_id)
	{
		this.deviceID = d_id;
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
