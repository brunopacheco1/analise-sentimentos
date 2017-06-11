package com.dev.bruno.sentiments.status.resource;

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

import com.dev.bruno.sentiments.status.model.Response;
import com.dev.bruno.sentiments.status.model.Status;
import com.dev.bruno.sentiments.status.service.StatusService;

@Stateless
@Path("/status")
public class StatusResource {

	@Inject
	private StatusService service;
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response insert(Status status) {
		service.insert(status);
		
		return new Response(true);
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/process")
	public Response process() {
		service.processNullMachineSentiment();
		
		return new Response(true);
	}
	
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/machineSentiment/{sentiment}")
	public Response updateMachineSentiment(@PathParam("id") String id, @PathParam("sentiment") Long sentiment) {
		Status status = new Status();
		
		status.setId(id);
		status.setMachineSentiment(sentiment);
		
		service.update(status);
		
		return new Response(true);
	}
	
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/humanSentiment/{sentiment}")
	public Response updateHumanSentiment(@PathParam("id") String id, @PathParam("sentiment") Long sentiment) {
		Status status = new Status();
		
		status.setId(id);
		status.setHumanSentiment(sentiment);
		
		service.update(status);
		
		return new Response(true);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/notSentimentalized")
	public List<Status> listNullHumanSentiment(@QueryParam("limit") @DefaultValue("20") Integer limit) {
		return service.listNullHumanSentiment(limit);
	}
	
	@GET
	@Path("/sentimentalized/csv")
	@Produces(MediaType.TEXT_PLAIN)
	public File getFile() throws IOException {
		return service.getFile();
	}
}