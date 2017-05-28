package com.dev.bruno.sentimentanalysis.tweets.resource;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.dev.bruno.sentimentanalysis.tweets.model.Response;
import com.dev.bruno.sentimentanalysis.tweets.model.Tweet;
import com.dev.bruno.sentimentanalysis.tweets.service.TweetService;

@Produces(MediaType.APPLICATION_JSON)
@Stateless
@Path("/tweet")
public class TweetResource {

	@Inject
	private TweetService service;
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(Tweet tweet) {
		service.update(tweet);
		
		return new Response(true);
	}
}