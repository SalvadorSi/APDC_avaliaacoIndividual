package pt.unl.fct.di.apdc.AvaliacaoIndividual.util;

public class UserDataData {

	private String username;
	private String tokenID;

	public UserDataData() {
	}
	
	public UserDataData(String username, String tokenID) {
		this.username = username;
		this.tokenID = tokenID;
	}
	public String getUsername() {
		return username;
	}
	public String getTokenID() {
		return tokenID;
	}
}
