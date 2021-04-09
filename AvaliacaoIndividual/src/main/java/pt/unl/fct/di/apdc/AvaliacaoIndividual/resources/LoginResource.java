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
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.AvaliacaoIndividual.util.AuthToken;
import pt.unl.fct.di.apdc.AvaliacaoIndividual.util.LoginData;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	
	private final Gson g = new Gson();

	public LoginResource() {
	}
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response doLogin(LoginData data) {
		LOG.fine("Attempt to log in user: " + data.getUsername());

		Key userKey = userKeyFactory.newKey(data.getUsername());
		Transaction txn = datastore.newTransaction();
		try {
			Entity user = txn.get(userKey);
			if(user == null) {
				//nao existe o user
				txn.rollback();
				LOG.warning("Failed login attempt for username: " + data.getUsername());
				return Response.status(Status.FORBIDDEN).entity("Unlucky, try again.").build();
			}
			//criar token
			AuthToken authToken = new AuthToken(data.getUsername(), user.getString("user_tokenID"));
			authToken.setRole((String) user.getString("user_role"));
			
			Key tokenKey = datastore.newKeyFactory()
					.setKind("Token")
					.newKey(authToken.getTokenID());
			Entity token = txn.get(tokenKey);
			if(token != null) {	//ja esta logged in
				txn.rollback();
				LOG.warning(data.getUsername() + " tryed to login while loged in.");
				return Response.status(Status.FORBIDDEN).entity("You are already logged in.").build();
			}
			//conta disabled, mas se for GA consegue dar login na mesma
			if(user.getString("user_state").equals("DISABLED") && ! user.getString("user_role").equals("GA")) { 
				txn.rollback();
				LOG.warning(data.getUsername() + " tryed to login while his account is disabled.");
				return Response.status(Status.FORBIDDEN).entity("Disabled account. Try later.").build();
			}
			
			String hashedPWD = (String) user.getString("user_pwd");
			if(hashedPWD.equals(DigestUtils.sha512Hex(data.getPassword()))) {
				 token = Entity.newBuilder(tokenKey)
						.set("token_id", authToken.getTokenID())
						.set("token_role", authToken.getRole())
						.set("token_username", authToken.getUsername())
						.set("token_start_time", authToken.getCreationData())
						.set("token_end_time", authToken.getExpirationData())
						.build();
				txn.put(token);
				txn.commit();
				
				LOG.info("User '" + data.getUsername() + "' logged in sucessefully.");
				return Response.ok(g.toJson(authToken)).build();
			}
			else {//pass errada
				txn.rollback();
				LOG.warning("Wrong password for username: " + data.getUsername());
				return Response.status(Status.FORBIDDEN).build();
			}
		} finally {
			if(txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
}
