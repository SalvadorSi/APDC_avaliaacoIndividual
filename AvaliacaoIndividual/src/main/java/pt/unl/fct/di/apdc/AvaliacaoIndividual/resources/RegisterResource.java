package pt.unl.fct.di.apdc.AvaliacaoIndividual.resources;

import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.AvaliacaoIndividual.util.RegisterData;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {

	private static final Logger LOG = Logger.getLogger(RegisterResource.class.getName());
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	public RegisterResource() {
	}
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response register(RegisterData data) {
		LOG.fine("Attempt to register user: " + data.getUsername());
		
		//checks input data
		if( ! data.validRegistration() ) {
			return Response.status(Status.BAD_REQUEST).entity("Missing or wrong parameter.").build();
		}
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.getUsername());
		
		Transaction txn = datastore.newTransaction();
		try {
			Entity user = txn.get(userKey);
			if( user != null ){ //caso ja exista o user
				txn.rollback();
				return Response.status(Status.FORBIDDEN).entity("User already exists.").build(); 
			}
			else {
				user = Entity.newBuilder(userKey)
						.set("user_pwd", DigestUtils.sha512Hex(data.getPassword()))
						.set("user_email", data.getEmail())
						.set("user_role", "USER")
						.set("user_state", "ENABLED")
						.set("user_perfil", "PUBLICO")
						.set("user_tokenID", UUID.randomUUID().toString())
						.set("user_creation_time", Timestamp.now())
						.set("user_telemovel", "")
						.set("user_telefone", "")
						.set("user_morada", "")
						.set("user_morada_alternativa", "")
						.set("user_localidade", "")
						.set("user_codigo_postal", "")
						.build();
				txn.add(user);
				LOG.info("User registered " + data.getUsername());
				txn.commit();
				return Response.ok("{}").entity("User registered.").build();
			}
		} finally {		//SUPER IMPORTANTE
			if(txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
}
