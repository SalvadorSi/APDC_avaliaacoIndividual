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

import pt.unl.fct.di.apdc.AvaliacaoIndividual.util.ChangeStateData;

@Path("/changestate")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeStateResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	public ChangeStateResource() {
	}
	

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changeState(ChangeStateData data) {
		LOG.fine("Attempting to change '" + data.getUsername() + "' to " + data.getNextState());
		
		Key userAMudarKey = datastore.newKeyFactory().setKind("User").newKey(data.getUsername());
		Transaction txn = datastore.newTransaction();
		try {
			Entity userAMudar = txn.get(userAMudarKey);
			//user que se quer n existe
			if(userAMudar == null) {
				txn.rollback();
				LOG.warning(data.getUsername() + " doesnt exist. ");
				return Response.status(Status.FORBIDDEN).entity(data.getUsername() + " doesnt exist").build();
			}
			Key sniperTokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.getTokenID());
			Entity sniperToken = txn.get(sniperTokenKey);
			//sniper not logged in
			if(sniperToken == null) { 
				txn.rollback();
				LOG.warning(data.getTokenID() + " doesnt exist (not looged in).");
				return Response.status(Status.FORBIDDEN).entity("You are not logged in.").build();
			}
			//token do sniper ja expirou
			if(sniperToken.getLong("token_end_time") < System.currentTimeMillis()) {
				txn.delete(sniperTokenKey);
				txn.commit();
				LOG.warning(data.getTokenID() + " expired.");
				return Response.status(Status.FORBIDDEN).entity("Your token expired.").build();
			}
			Key userSniperKey = datastore.newKeyFactory().setKind("User").newKey(sniperToken.getString("token_username"));
			Entity userSniper = txn.get(userSniperKey); 
			if(userSniper == null) {
				txn.rollback();
				LOG.warning("Sniper doesnt exist. ");
				return Response.status(Status.FORBIDDEN).entity("You dont exist").build();
			}
			//user n pode mudar nada
			if(userSniper.getString("user_role").equals("USER")) {
				txn.rollback();
				LOG.warning(sniperToken.getString("token_username") + " tryed to change someones state.");
				return Response.status(Status.FORBIDDEN).entity("You cant change states.").build();
			}
			//ng pode mudar o state do SU, nem ele proprio
			if(userAMudar.getString("user_role").equals("SU")) {
				txn.rollback();
				LOG.warning(sniperToken.getString("token_username") + " tryed to change SU state.");
				return Response.status(Status.FORBIDDEN).entity("You cant change SU state.").build();
			}
			
			Key userAMudarTokenKey = datastore.newKeyFactory().setKind("Token").newKey(userAMudar.getString("user_tokenID"));
			
			//GBO muda USER
			if(userSniper.getString("user_role").equals("GBO") && userAMudar.getString("user_role").equals("USER")) {
				userAMudar = Entity.newBuilder(userAMudar)
						.set("user_state", data.getNextState())
						.set("user_last_update_time", Timestamp.now())
						.build();
				txn.update(userAMudar);
				if(userAMudarTokenKey != null)	//se ele estiver logged in apagar o token dele
					txn.delete(userAMudarTokenKey);
				txn.commit();
				LOG.info("State changed successfully.");
				return Response.ok("{}").entity("State changed.").build();
			}
			//GA mudar GBO ou USER
			if(userSniper.getString("user_role").equals("GA") && (userAMudar.getString("user_role").equals("USER") || userAMudar.getString("user_role").equals("GBO"))) {
				userAMudar = Entity.newBuilder(userAMudar)
						.set("user_state", data.getNextState())
						.set("user_last_update_time", Timestamp.now())
						.build();
				txn.update(userAMudar);
				if(userAMudarTokenKey != null)	//se ele estiver logged in apagar o token dele
					txn.delete(userAMudarTokenKey);
				txn.commit();
				LOG.info("State changed successfully.");
				return Response.ok("{}").entity("State changed.").build();
			}
			//SU muda todos (menos o dele q ja ta tratado atras)
			if(userSniper.getString("user_role").equals("SU")) {
				userAMudar = Entity.newBuilder(userAMudar)
						.set("user_state", data.getNextState())
						.set("user_last_update_time", Timestamp.now())
						.build();
				txn.update(userAMudar);
				if(userAMudarTokenKey != null)	//se ele estiver logged in apagar o token dele
					txn.delete(userAMudarTokenKey);
				txn.commit();
				LOG.info("State changed successfully.");
				return Response.ok("{}").entity("State changed.").build();
			}
			
			txn.rollback();
			LOG.info("Nothing was changed.");
			return Response.ok("{}").entity("You dont have that permission.").build();
			
		} finally {
			if(txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
}
