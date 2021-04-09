package pt.unl.fct.di.apdc.AvaliacaoIndividual.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.util.logging.Logger;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.AvaliacaoIndividual.util.UserDataData;
import pt.unl.fct.di.apdc.AvaliacaoIndividual.util.UserSenderData;

@Path("/userdata")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class UserDataResource {

	
	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private final Gson g = new Gson();
	
	public UserDataResource() {
	}
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response userDataResource(UserDataData data) {
		LOG.fine("Attempt to update user: " + data.getUsername());
		
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.getUsername());	
		Transaction txn = datastore.newTransaction();
		try {
			Entity user = txn.get(userKey);
			if(user == null) {
				txn.rollback();
				LOG.warning("Failed to get data from (he doesnt exist): " + data.getUsername());
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
			//Key userUpdaterKey = datastore.newKeyFactory().setKind("User").newKey(updaterToken.getString("token_username"));
			//Entity userUpdater = txn.get(userUpdaterKey); //entity do sniper
			
			//caso n seja GBO nem SU n pode ver atributos de ninguem
			/*if( ! userUpdater.getString("user_role").equals("GBO") && ! userUpdater.getString("user_role").equals("SU")) { 
				txn.rollback();
				LOG.info(updaterToken.getString("token_username") + " tryed to get someones information (he is not GBO).");
				return Response.status(Status.FORBIDDEN).entity("You cant get that information.").build();
			}*/
			//caso seja GBO consegue de qq pessoa
			UserSenderData toSend = new UserSenderData(
					data.getUsername(),
					user.getString("user_email"),
					user.getString("user_telemovel"),
					user.getString("user_telefone"),
					user.getString("user_morada"),
					user.getString("user_morada_alternativa"),
					user.getString("user_localidade"),
					user.getString("user_codigo_postal"),
					user.getString("user_perfil"),
					user.getString("user_state"),
					user.getString("user_role")
					);
			
			
			//txn.update(user);
			txn.commit();
			LOG.info("Information sent.");
			return Response.ok(g.toJson(toSend)).build();
		} finally {		//SUPER IMPORTANTE
			if(txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
}
