package pt.unl.fct.di.apdc.AvaliacaoIndividual.util;

public class RegisterData {
	
	private String username;
	private String password;
	private String email;
	private String confirmation;
	//private String role;

	public RegisterData() {
	}
	
	public RegisterData(String username, String email, String password, String confirmation) {
		this.username = username;
		this.password = password;
		this.confirmation = confirmation;
		this.email = email;
		//this.role = "USER";
	}

	public boolean validRegistration() {
		if(!password.equals(confirmation) || !email.contains("@")|| password.length()<8)
			return false;
		return true;
	}

	public String getConfirmation() {
		return confirmation;
	}
	public void setConfirmation(String confirmation) {
		this.confirmation = confirmation;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
