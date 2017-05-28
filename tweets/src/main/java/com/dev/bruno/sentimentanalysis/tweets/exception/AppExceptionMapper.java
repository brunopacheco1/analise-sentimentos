package com.dev.bruno.sentimentanalysis.tweets.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.dev.bruno.sentimentanalysis.tweets.model.Response;

@Provider
public class AppExceptionMapper implements ExceptionMapper<AppException> {
    
    @Override
	public Response toResponse(AppException e) {
		Response response = new Response(e.getMessage());
		
		return Response.status(Status.CONFLICT).entity(response).type(MediaType.APPLICATION_JSON).build();
	}
}
