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

import pt.unl.fct.di.apdc.AvaliacaoIndividual.util.LogoutData;

@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {

	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	public LogoutResource() {
	}
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response logout(LogoutData data) {
		LOG.fine("Attempt to logout user: " + data.getUsername());
		Key tokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.getTokenID());
		Transaction txn = datastore.newTransaction();
		try {
			Entity token = txn.get(tokenKey);
			if(token == null) {
				//not logged in
				txn.rollback();
				LOG.warning(data.getUsername() + " is not logged in.");
				return Response.status(Status.BAD_REQUEST).entity("Not loggedin.").build();
			}
			else {
				//esta logged in e existe token
				String tokenUsername = (String) token.getString("token_username");
				if(!tokenUsername.equals(data.getUsername()))	// token nao e do user q tenta dar logout
				{
					txn.rollback();
					LOG.warning( data.getUsername() + " tryed to log out.");
					return Response.status(Status.BAD_REQUEST).entity("Not your token.").build();
				}
				txn.delete(tokenKey);
				txn.commit();
				
				LOG.info("User '" + data.getUsername() + "' logged out sucessefully.");
				return Response.ok("{}").entity(data.getUsername() + " logged out.").build();
			}
		} finally {
			if(txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
}
