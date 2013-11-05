package edu.sjsu.cmpe.procurement.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.yammer.metrics.annotation.Timed;

import edu.sjsu.cmpe.procurement.domain.BookRequest;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcurementResource {
	private  Client client;
	
	public ProcurementResource(Client client) {
		this.client = client;
	}
	
	@GET
    @Timed(name = "get-root")
    public Response getRoot() {
	

	return Response.ok().build();
    }

}
