package hirs_client;

public class Sensor {
    	    
    // Sensor Information
	private int id;
	private int customer_id;
	private int sensor_type_id;
	private int place_id;
	private int sensor_value;
	private String created_at;
	private String updated_at;
	private int channel_id;
	
	public Sensor() {
		this.id = 0;
		this.customer_id = 0;
		this.sensor_type_id = 0;
		this.place_id = 0;
		this.sensor_value = 0;
		this.created_at = "";
		this.updated_at = "";
		this.channel_id = 0;
	}
	
	public Sensor(int id, int customer_id, int sensor_type_id, int sensor_value, int place_id, String created_at, String updated_at, int channel_id) {
		super();
		this.id = id;
		this.customer_id = customer_id;
		this.sensor_type_id = sensor_type_id;
		this.place_id = place_id;
		this.sensor_value = sensor_value;
		this.created_at = created_at;
		this.updated_at = updated_at;
		this.channel_id = channel_id;
	}
	
	public int getChannelId() {
		return this.channel_id;
	}
	
	public void setChannelId(int channel_id) {
		this.channel_id = channel_id;
	}
	
	public int getSensorId() {
		return id;
	}
	
	public void setSensorId(int id) {
		this.id = id;
	}
	
	public int getCustomerId() {
		return customer_id;
	}
	
	public void setCustomerId(int customer_id) {
		this.customer_id = customer_id;
	}
	
	
	public int getSensorPlaceId() {
		return place_id;
	}
	
	public void setSensorPlaceId(int place_id) {
		this.place_id = place_id;
	}
	
	public int getSensorTypeId() {
		return sensor_type_id;
	}
	
	public void setSensorTypeId(int sensor_type_id) {
		this.sensor_type_id = sensor_type_id;
	}
	
	public int getSensorValue() {
		return sensor_value;
	}
	
	public void setSensorValue(int sensor_value) {
		this.sensor_value = sensor_value;
	}

	@Override
	public String toString() {
		return "Sensor [id=" + this.id + ", customer_id=" + this.customer_id + ", sensor_type_id=" + this.sensor_type_id + ", place_id=" + this.place_id+ ", sensor_value=" + this.sensor_value + "]";
	}
	
}
	

