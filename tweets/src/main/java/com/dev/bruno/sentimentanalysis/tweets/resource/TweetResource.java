package com.dev.bruno.sentimentanalysis.tweets.resource;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
	public Response insert(Tweet tweet) {
		service.update(tweet);
		
		return new Response(true);
	}
	
	@POST
	@Path("/process")
	public Response process() {
		service.processTweets();
		
		return new Response(true);
	}
	
	@GET
	public List<Tweet> listNullHumanSentiment(@QueryParam("limit") @DefaultValue("20") Integer limit) {
		return service.listNullHumanSentiment(limit);
	}
	
	@GET
	@Path("/{id}")
	public Tweet get(@PathParam("id") String id) {
		return service.get(id);
	}
}