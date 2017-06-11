package com.dev.bruno.sentiments.status.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.dev.bruno.sentiments.status.model.Response;

@Provider
public class AppExceptionMapper implements ExceptionMapper<AppException> {
    
    @Override
	public javax.ws.rs.core.Response toResponse(AppException e) {
		Response response = new Response(e.getMessage());
		
		return javax.ws.rs.core.Response.status(Status.CONFLICT).entity(response).type(MediaType.APPLICATION_JSON).build();
	}
}