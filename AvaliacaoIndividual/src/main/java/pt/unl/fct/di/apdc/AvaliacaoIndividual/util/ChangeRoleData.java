package pt.unl.fct.di.apdc.AvaliacaoIndividual.util;

public class ChangeRoleData {
	
	private String username;
	private String nextRole;
	private String tokenID;

	public ChangeRoleData() {
	}
	
	public ChangeRoleData(String username, String nextRole, String tokenID) {
		this.username = username;
		this.nextRole = nextRole;
		this.tokenID = tokenID;
	}
	public String getUsername() {
		return username;
	}
	public String getTokenID() {
		return tokenID;
	}
	public String getNextRole() {
		return nextRole;
	}
}
