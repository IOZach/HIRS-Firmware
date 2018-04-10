package hirs_client;

public class Session {
	
	private String token_type = "";
	private String expires_in = "";
	private String access_token = "";
	private String refresh_token = "";
	
	public Session() {
		super();
		this.token_type = "";
		this.expires_in = "";
		this.access_token = "";
		this.refresh_token = "";
	}
	
	public Session(String token_type, String expires_in, String access_token, String refresh_token) {
		super();
		this.token_type = token_type;
		this.expires_in = expires_in;
		this.access_token = access_token;
		this.refresh_token = refresh_token;
	}
	
	public String getAccessToken() {
		return this.access_token;
	}
	
	public String getRefreshToken() {
		return this.refresh_token;
	}
	
	public void setAccessToken(String access_token) {
		this.access_token = access_token;
	}
	
	public long getExpiresIn() {
		return Long.parseLong(this.expires_in);
	}
	
	@Override
	public String toString() {
		return "token_type: "+this.token_type+", expires_in: "+this.expires_in+", access_token: "+this.access_token+", refresh_token: "+this.refresh_token;
	}

}
