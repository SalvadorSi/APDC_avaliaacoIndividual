package pt.unl.fct.di.apdc.AvaliacaoIndividual.util;

public class ChangePasswordData {

	private String username;
	private String passwordAntiga;
	private String passwordNova;
	private String confirmacaoNova;
	private String tokenID;

	public ChangePasswordData() {
	}
	
	public ChangePasswordData(String username, String passwordAntiga, String passwordNova, String confirmacaoNova, String tokenID) {
		this.username = username;
		this.passwordAntiga = passwordAntiga;
		this.passwordNova = passwordNova;
		this.confirmacaoNova = confirmacaoNova;
		this.tokenID = tokenID;
	}
	public boolean isNewPasswordValid() {
		if(passwordNova.equals(confirmacaoNova) && passwordNova.length()>=8)
			return true;
		return false;
	}
	public String getUsername() {
		return username;
	}
	public String getPasswordAntiga() {
		return passwordAntiga;
	}
	public String getPasswordNova() {
		return passwordNova;
	}
	public String getConfirmacaoNova() {
		return confirmacaoNova;
	}
	public String getTokenID() {
		return tokenID;
	}
}
