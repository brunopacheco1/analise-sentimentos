package com.dev.bruno.sentiments.status.resource;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.dev.bruno.sentiments.status.model.Response;
import com.dev.bruno.sentiments.twitter.TwitterService;

@Stateless
@Path("/twitter")
public class TwitterResource {

	@Inject
	private TwitterService service;
	
	@POST
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchOnTwitter() throws Exception {
		service.search();
		
		return new Response(true);
	}
}