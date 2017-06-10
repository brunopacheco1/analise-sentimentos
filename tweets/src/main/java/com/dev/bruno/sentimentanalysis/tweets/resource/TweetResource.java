package com.dev.bruno.sentimentanalysis.tweets.resource;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.dev.bruno.sentimentanalysis.tweets.model.Response;
import com.dev.bruno.sentimentanalysis.tweets.model.Tweet;
import com.dev.bruno.sentimentanalysis.tweets.service.TweetService;

@Stateless
@Path("/tweet")
public class TweetResource {

	@Inject
	private TweetService service;
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insert(Tweet tweet) {
		service.insert(tweet);
		
		return new Response(true);
	}
	
	@POST
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response search() throws Exception {
		service.search();
		
		return new Response(true);
	}
	
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(Tweet tweet) {
		service.update(tweet);
		
		return new Response(true);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Tweet> listNullHumanSentiment(@QueryParam("limit") @DefaultValue("20") Integer limit) {
		return service.listNullHumanSentiment(limit);
	}
	
	@GET
	@Path("/file/download")
	@Produces(MediaType.TEXT_PLAIN)
	public File getFile() throws IOException {
		return service.getFile();
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Tweet get(@PathParam("id") String id) {
		return service.get(id);
	}
}