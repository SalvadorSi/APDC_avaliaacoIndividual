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

import pt.unl.fct.di.apdc.AvaliacaoIndividual.util.ChangeRoleData;

@Path("/changerole")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeRoleResource {
	
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	public ChangeRoleResource() {
	}
	

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changeRole(ChangeRoleData data) {
		LOG.fine("Attempting to change '" + data.getUsername() + "' to " + data.getNextRole());
		
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
			//user nao pode mudar a role de ng
			if(userSniper.getString("user_role").equals("USER")) {
				txn.rollback();
				LOG.warning(sniperToken.getString("token_username") + " tryed to change someones role.");
				return Response.status(Status.FORBIDDEN).entity("You cant change roles.").build();
			}
			//nao se pode ter mais nenhum SU
			if(data.getNextRole().equals("SU")) {
				txn.rollback();
				LOG.warning(sniperToken.getString("token_username") + " tryed to change someones role to SU.");
				return Response.status(Status.FORBIDDEN).entity("There can only be one SU.").build();
			}
			//SU pode mudar tudo
			if(userSniper.getString("user_role").equals("SU")) {
				userAMudar = Entity.newBuilder(userAMudar)
						.set("user_role", data.getNextRole())
						.set("user_last_update_time", Timestamp.now())
						.build();
				txn.update(userAMudar);
				txn.commit();
				LOG.info("Role changed successfully.");
				return Response.ok("{}").entity("Role changed.").build();
			}
			//mudar de USER para GBO
			if(data.getNextRole().equals("GBO") && userAMudar.getString("user_role").equals("USER")) {
				//so o ga e q pode mudar para gbo
				if(userSniper.getString("user_role").equals("GA")) {
					userAMudar = Entity.newBuilder(userAMudar)
							.set("user_role", data.getNextRole())
							.set("user_last_update_time", Timestamp.now())
							.build();
					txn.update(userAMudar);
					txn.commit();
					LOG.info("Role changed successfully.");
					return Response.ok("{}").entity("Role changed.").build();
				}
				txn.rollback();
				LOG.warning(sniperToken.getString("token_username") + " tryed to change someones role to GBO.");
				return Response.status(Status.FORBIDDEN).entity("No permissions to change this role.").build();
			}
			//mudar de USER para GA
			/*if(data.getNextRole().equals("GA")) {
				
			}
			//tentar mudar para a mesma role
			/*if(data.getNextRole().equals(userAMudar.getString("user_role"))) {
				txn.rollback();
				LOG.info("Tryed to change to the same role: " + data.getNextRole());
				return Response.ok("{}").entity("Successfully changed his/hers role.").build();
			}*/
			
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
