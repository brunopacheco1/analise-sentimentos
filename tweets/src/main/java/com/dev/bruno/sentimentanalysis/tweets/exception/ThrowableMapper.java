package com.dev.bruno.sentimentanalysis.tweets.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.dev.bruno.sentimentanalysis.tweets.model.Response;

@Provider
public class ThrowableMapper implements ExceptionMapper<Throwable> {

	protected Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Override
	public javax.ws.rs.core.Response toResponse(Throwable t) {
		Response response = new Response("Erro não esperado.");
		
		logger.log(Level.SEVERE, t.getMessage(), t);
		
		return javax.ws.rs.core.Response.status(Status.INTERNAL_SERVER_ERROR).entity(response).type(MediaType.APPLICATION_JSON).build();
	}
}