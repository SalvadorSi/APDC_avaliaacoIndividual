package pt.unl.fct.di.apdc.AvaliacaoIndividual.util;

public class LogoutData {

	private String username;
	private String tokenID;
	
	public LogoutData() {
	}
	
	public LogoutData(String username, String tokenID) {
		this.tokenID = tokenID;
		this.username = username;
	}
	
	public String getTokenID() {
		return tokenID;
	}
	public String getUsername() {
		return username;
	}
}
