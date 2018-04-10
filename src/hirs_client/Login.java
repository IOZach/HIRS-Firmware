package hirs_client;

public class Login {
	
	private String username = "";
	private String password = "";
	
	public Login() {
		this.username = "";
		this.password = "";
	}
	
	public Login(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}
	
	@Override
	public String toString() {
		return "Login [username=" + this.username + ", password=" + this.password + "]";
	}

}
