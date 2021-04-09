package pt.unl.fct.di.apdc.AvaliacaoIndividual.util;

public class ChangeStateData {

	private String username;
	private String nextState;
	private String tokenID;

	public ChangeStateData() {
	}
	
	public ChangeStateData(String username, String nextState, String tokenID) {
		this.username = username;
		this.nextState = nextState;
		this.tokenID = tokenID;
	}
	public String getUsername() {
		return username;
	}
	public String getTokenID() {
		return tokenID;
	}
	public String getNextState() {
		return nextState;
	}
}
