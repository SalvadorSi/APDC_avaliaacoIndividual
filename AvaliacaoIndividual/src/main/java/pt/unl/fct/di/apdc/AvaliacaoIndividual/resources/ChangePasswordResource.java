package pt.unl.fct.di.apdc.AvaliacaoIndividual.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.AvaliacaoIndividual.util.ChangePasswordData;

@Path("/changepassword")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangePasswordResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	public ChangePasswordResource() {
	}
	

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changePassword(ChangePasswordData data) {
		LOG.fine("Attempting to change '" + data.getUsername() + "' password.");
		
		if( ! data.isNewPasswordValid() ) {
			return Response.status(Status.BAD_REQUEST).entity("Password not valid.").build();
		}
		
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.getUsername());
		Transaction txn = datastore.newTransaction();
		try {
			Entity user = txn.get(userKey);
			if(user == null) {
				txn.rollback();
				LOG.warning(data.getUsername() + " doesnt exist. ");
				return Response.status(Status.FORBIDDEN).entity(data.getUsername() + " doesnt exist").build();
			}
			Key changerTokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.getTokenID());
			Entity changerToken = txn.get(changerTokenKey);
			//changer not logged in
			if(changerToken == null) { 
				txn.rollback();
				LOG.warning(data.getTokenID() + " doesnt exist (not looged in).");
				return Response.status(Status.FORBIDDEN).entity("You are not logged in.").build();
			}
			//token do changer ja expirou
			if(changerToken.getLong("token_end_time") < System.currentTimeMillis()) {
				txn.delete(changerTokenKey);
				txn.commit();
				LOG.warning(data.getTokenID() + " expired.");
				return Response.status(Status.FORBIDDEN).entity("Your token expired.").build();
			}
			//nao pode mudar a pass de outra pessoa
			if(! changerToken.getString("token_username").equals(data.getUsername())) {
				txn.rollback();
				LOG.warning(changerToken.getString("token_username") + " tryed to change " + data.getUsername() + "'s password.");
				return Response.status(Status.FORBIDDEN).entity("You cannot change others passwords.").build();
			}
			String hashedPWD = (String) user.getString("user_pwd");
			if(hashedPWD.equals(DigestUtils.sha512Hex(data.getPasswordAntiga()))) { //pass seja certa
				 user = Entity.newBuilder(user)
						.set("user_pwd", DigestUtils.sha512Hex(data.getPasswordNova()))
						.build();
				txn.update(user);
				txn.commit();
				LOG.info("User '" + data.getUsername() + "' updated password.");
				return Response.ok("{}").entity("Password updated.").build();
			}
			else {
				//pass errada
				txn.rollback();
				LOG.warning("Wrong password.");
				return Response.status(Status.FORBIDDEN).entity("Wrong password.").build();
			}
			
			
		} finally {
			if(txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
}
