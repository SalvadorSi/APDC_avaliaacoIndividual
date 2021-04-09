package pt.unl.fct.di.apdc.AvaliacaoIndividual.util;

public class UserSenderData {

	private String username;
	private String email;
	private String telemovel;
	private String telefone;
	private String morada;
	private String moradaAlternativa;
	private String localidade;
	private String codigoPostal;
	private String perfil;
	private String estado;
	private String role;
	
	public UserSenderData() {
	}
	
	public UserSenderData(String username, String email, String telemovel, String telefone, 
			String morada, String moradaAlternativa, String localidade, String codigoPostal, String perfil, String estado, String role) {

		this.username = username;
		this.email = email;
		this.telemovel = telemovel;
		this.telefone = telefone;
		this.morada = morada;
		this.moradaAlternativa = moradaAlternativa;
		this.localidade = localidade;
		this.codigoPostal = codigoPostal;
		this.perfil = perfil;
		this.estado = estado;
		this.role = role;
	}
}
