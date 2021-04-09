package pt.unl.fct.di.apdc.AvaliacaoIndividual.util;

public class UpdateData {
	
	private String username;
	private String email;
	private String telemovel;
	private String telefone;
	private String morada;
	private String moradaAlternativa;
	private String localidade;
	private String codigoPostal;
	private String perfil;
	private String tokenID;

	public UpdateData() {
	}
	
	public UpdateData(String username,String email, String telemovel, String telefone, 
			String morada, String moradaAlternativa, String localidade, String codigoPostal, String perfil, String tokenID) {
		this.username = username;
		this.email = email;
		this.telemovel = telemovel;
		this.telefone = telefone;
		this.morada = morada;
		this.moradaAlternativa = moradaAlternativa;
		this.localidade = localidade;
		this.codigoPostal = codigoPostal;
		this.perfil = perfil;
		this.tokenID = tokenID;
	}
	
	public boolean validUpdate() {
		if(validTelefone()
				&& validTelemovel()
				&& validMoradaAlternativa()
				&& validCodigoPostal()
				&& validEmail())
			return true;
		return false;
	}
	private boolean validEmail() {
		if(email.equals(""))
			return true;
		if(!email.contains("@"))
			return false;
		return true;
	}
	private boolean validNumber(String numero) {
		if(numero.length() == 13
				&& numero.charAt(0) == '+' 
				&& numero.charAt(1) == '3' 
				&& numero.charAt(2) == '5'
				&& numero.charAt(3) == '1')
			return true;
		return false;
	}
	private boolean validTelefone() {
		if(validNumber(telefone) || telefone.equals(""))
			return true;
		return false;
	}
	private boolean validTelemovel() {
		if(telemovel.equals(""))
			return true;
		if(validNumber(telemovel)
				&& telemovel.charAt(4) == '9'
				&& (telemovel.charAt(5) == '1' 
						|| telemovel.charAt(5) == '2'
						|| telemovel.charAt(5) == '3'
						|| telemovel.charAt(5) == '6'))
			return true;
		return false;
	}
	private boolean validMoradaAlternativa() {
		if(moradaAlternativa.equals(""))
			return true;
		if(!moradaAlternativa.equals(morada))
			return true;
		return false;
	}
	private boolean validCodigoPostal() {
		if(codigoPostal.equals(""))
			return true;
		if(codigoPostal.charAt(4) == '-')
			return true;
		return false;
	}
	public String getPerfil() {
		return perfil;
	}
	public void setPerfil(String perfil) {
		this.perfil = perfil;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getUsername() {
		return username;
	}
	public String getTelemovel() {
		return telemovel;
	}
	public void setTelemovel(String telemovel) {
		this.telemovel = telemovel;
	}
	public String getTelefone() {
		return telefone;
	}
	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}
	public String getMorada() {
		return morada;
	}
	public void setMorada(String morada) {
		this.morada = morada;
	}
	public String getMoradaAlternativa() {
		return moradaAlternativa;
	}
	public void setMoradaAlternativa(String moradaAlternativa) {
		this.moradaAlternativa = moradaAlternativa;
	}
	public String getLocalidade() {
		return localidade;
	}
	public void setLocalidade(String localidade) {
		this.localidade = localidade;
	}
	public String getCodigoPostal() {
		return codigoPostal;
	}
	public void setCodigoPostal(String codigoPostal) {
		this.codigoPostal = codigoPostal;
	}
	public String getTokenID() {
		return tokenID;
	}
}
