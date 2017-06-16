package com.dev.bruno.sentiments.facebook.resource;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.dev.bruno.sentiments.facebook.service.FacebookService;
import com.dev.bruno.sentiments.status.model.Response;

@Stateless
@Path("/facebook")
public class FacebookResource {

	@Inject
	private FacebookService service;
	
	@POST
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchOnFacebook() throws Exception {
		service.search();
		
		return new Response(true);
	}
}