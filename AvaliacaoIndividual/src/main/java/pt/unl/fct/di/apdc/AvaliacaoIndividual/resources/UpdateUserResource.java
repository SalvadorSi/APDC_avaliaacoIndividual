package pt.unl.fct.di.apdc.AvaliacaoIndividual.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.AvaliacaoIndividual.util.UpdateData;

@Path("/update")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class UpdateUserResource {

	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	public UpdateUserResource() {
	}
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(UpdateData data) {
		LOG.fine("Attempt to update user: " + data.getUsername());
		
		if(!data.validUpdate())
			return Response.status(Status.BAD_REQUEST).entity("Not valid update.").build();
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.getUsername());
		Transaction txn = datastore.newTransaction();
		try {
			Entity user = txn.get(userKey);
			if( user == null ){ //caso nao exista o user
				txn.rollback();
				LOG.warning("Failed update attempt for username: " + data.getUsername());
				return Response.status(Status.FORBIDDEN).entity("No such user.").build(); 
			}
			Key updaterTokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.getTokenID());
			Entity updaterToken = txn.get(updaterTokenKey);
			//updater is not logged in
			if(updaterToken == null) { 
				txn.rollback();
				LOG.warning(data.getTokenID() + " doesnt exist (not looged in).");
				return Response.status(Status.FORBIDDEN).entity("You are not logged in.").build();
			}
			//token do updater ja expirou
			if(updaterToken.getLong("token_end_time") < System.currentTimeMillis()) {
				txn.delete(updaterTokenKey);
				txn.commit();
				LOG.warning(data.getTokenID() + " expired.");
				return Response.status(Status.FORBIDDEN).entity("Your token expired.").build();
			}
			//username nao e igual ao username do token
			if(!updaterToken.getString("token_username").equals(data.getUsername())) {
				txn.rollback();
				LOG.warning(data.getTokenID() + " tryed to update other account.");
				return Response.status(Status.FORBIDDEN).entity("You cant update others.").build();
			}
			if(data.getEmail().equals(""))
				data.setEmail(user.getString("user_email"));
			if(data.getTelemovel().equals(""))
				data.setTelemovel(user.getString("user_telemovel"));
			if(data.getTelefone().equals(""))
				data.setTelefone(user.getString("user_telefone"));
			if(data.getMorada().equals(""))
				data.setMorada(user.getString("user_morada"));
			if(data.getMoradaAlternativa().equals(""))
				data.setMoradaAlternativa(user.getString("user_morada_alternativa"));
			if(data.getLocalidade().equals(""))
				data.setLocalidade(user.getString("user_localidade"));
			if(data.getCodigoPostal().equals(""))
				data.setCodigoPostal(user.getString("user_codigo_postal"));
			if(data.getPerfil().equals(""))
				data.setPerfil(user.getString("user_perfil"));
			user = Entity.newBuilder(user) 
					.set("user_email", data.getEmail())
					.set("user_telemovel", data.getTelemovel())
					.set("user_telefone", data.getTelefone())
					.set("user_morada", data.getMorada())
					.set("user_morada_alternativa", data.getMoradaAlternativa())
					.set("user_localidade", data.getLocalidade())
					.set("user_codigo_postal", data.getCodigoPostal())
					.set("user_perfil", data.getPerfil())
					.set("user_last_update_time", Timestamp.now())
					.build();
			
			txn.update(user);
			LOG.info("User updated " + data.getUsername());
			txn.commit();
			return Response.ok("{}").entity("User info updated.").build();
		} finally {
			if(txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
	
}
