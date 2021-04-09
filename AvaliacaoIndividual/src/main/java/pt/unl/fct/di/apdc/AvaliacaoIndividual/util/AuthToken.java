package pt.unl.fct.di.apdc.AvaliacaoIndividual.util;

public class AuthToken {

	public static final long EXPIRATION_TIME = 1000*60*60*2; //2h
	
	private String username;
	private String tokenID;
	private long creationData;
	private long expirationData;
	private String role;
	
	public AuthToken() {
	}
	
	public AuthToken(String username, String tokenID) {
		this.username = username;
		this.tokenID = tokenID;
		this.creationData = System.currentTimeMillis();
		this.expirationData = this.creationData + AuthToken.EXPIRATION_TIME;
		this.role = null;
	}
	
	public String getUsername() {
		return username;
	}
	public String getTokenID() {
		return tokenID;
	}
	public long getCreationData() {
		return creationData;
	}
	public long getExpirationData() {
		return expirationData;
	}
	public void setExpirationData(long expirationData) {
		this.expirationData = expirationData;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
}
