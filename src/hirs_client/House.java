package hirs_client;

public class House {
	
	private int id = 0;
	private String name = "";
	private String housenotes = "";
	private int customer_id = 0;
	private String created_at;
	private String updated_at;
	
	public House() {
		this.id = 0;
		this.name = "";
		this. housenotes = "";
		this.customer_id = 0;
		this.created_at = "";
		this.updated_at = "";
	}
	
	public House(int id, String name, String housenotes, int customer_id, String created_at, String updated_at) {
		this.id = id;
		this.name = name;
		this.housenotes = housenotes;
		this.customer_id = customer_id;
		this.created_at = created_at;
		this.updated_at = updated_at;
	}
	
	public String getHouseNotes() {
		return this.housenotes;
	}
	
	public void setHouseNotes(String housenotes) {
		this.housenotes = housenotes;
	}

}
