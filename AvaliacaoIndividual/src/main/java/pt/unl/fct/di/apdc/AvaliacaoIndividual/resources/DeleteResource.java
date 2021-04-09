package pt.unl.fct.di.apdc.AvaliacaoIndividual.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.AvaliacaoIndividual.util.DeleteData;

@Path("/delete")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class DeleteResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	public DeleteResource() {
	}
	

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response delete(DeleteData data) {
		LOG.fine("Attempt to delete user: " + data.getUsername());

		Key userAApagarKey = datastore.newKeyFactory().setKind("User").newKey(data.getUsername());
		Transaction txn = datastore.newTransaction();
		try {
			Entity userAApagar = txn.get(userAApagarKey);
			//nao existe o userAApagar
			if(userAApagar == null) {
				txn.rollback();
				LOG.warning(data.getUsername() + " doesnt exist. ");
				return Response.status(Status.FORBIDDEN).entity(data.getUsername() + " doesnt exist").build();
			}
			Key sniperTokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.getTokenID());
			Entity sniperToken = txn.get(sniperTokenKey); //token do sniper
			//token do sniper n existe (n ta logged in)
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
			Entity userSniper = txn.get(userSniperKey); //entity do sniper
			// apagar-se a si proprio e nao for o SU
			if(data.getUsername().equals(sniperToken.getString("token_username")) && ! userSniper.getString("user_role").equals("SU")) { 
				txn.delete(userAApagarKey);
				txn.delete(sniperTokenKey);
				txn.commit();
				LOG.info("User '" + data.getUsername() + "' deleted sucessefully.");
				return Response.ok("{}").entity("You deleted urself.").build();
			}
			//SU nao se consegue apagar a ele proprio
			if(userSniper.getString("user_role").equals("SU") && data.getUsername().equals(sniperToken.getString("token_username"))) {
				txn.rollback();
				LOG.warning(data.getUsername() + " tryed to delete himself (beeing SU).");
				return Response.status(Status.FORBIDDEN).entity("You cant delete yourself.").build();
			}
			//caso ele tente apagar outro e seja USER mandar erro
			if(userSniper.getString("user_role").equals("USER")){
				txn.rollback();
				LOG.warning(data.getUsername() + " tryed to delete other account.");
				return Response.status(Status.FORBIDDEN).entity("You cant delete other accoutns.").build();
			}
			
			Key userAApagarTokenKey = datastore.newKeyFactory().setKind("Token").newKey(userAApagar.getString("user_tokenID"));
			
			//SU consegue apagar todos
			if(userSniper.getString("user_role").equals("SU")) {
				txn.delete(userAApagarKey);
				if(userAApagarTokenKey != null)	//se ele estiver logged in apagar tb o token dele
					txn.delete(userAApagarTokenKey);
				txn.commit();
				LOG.info("User '" + data.getUsername() + "' deleted sucessefully.");
				return Response.ok("{}").entity("User deleted sucessefully.").build();
			}
			//gbo e ga apagar user
			if(userAApagar.getString("user_role").equals("USER")) {
				txn.delete(userAApagarKey);
				if(userAApagarTokenKey != null)	//se ele estiver logged in apagar tb o token dele
					txn.delete(userAApagarTokenKey);
				txn.commit();
				LOG.info("User '" + data.getUsername() + "' deleted sucessefully.");
				return Response.ok("{}").entity("User deleted sucessefully.").build();
			}
			txn.rollback();
			LOG.info("You cant delete him.");
			return Response.status(Status.FORBIDDEN).entity("You cant delete him.").build();
		} finally {
			if(txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
}
